package com.erplite.inventory.service;

import com.erplite.inventory.dto.batch.*;
import com.erplite.inventory.dto.common.PagedResponse;
import com.erplite.inventory.entity.*;
import com.erplite.inventory.entity.InventoryLot.LotStatus;
import com.erplite.inventory.entity.InventoryTransaction.TransactionType;
import com.erplite.inventory.entity.Material.MaterialType;
import com.erplite.inventory.entity.ProductionBatch.BatchStatus;
import com.erplite.inventory.exception.BusinessException;
import com.erplite.inventory.exception.ResourceNotFoundException;
import com.erplite.inventory.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionBatchServiceTest {

    @Mock private ProductionBatchRepository batchRepository;
    @Mock private BatchComponentRepository componentRepository;
    @Mock private MaterialRepository materialRepository;
    @Mock private InventoryLotRepository lotRepository;
    @Mock private InventoryTransactionRepository transactionRepository;
    @InjectMocks private ProductionBatchService productionBatchService;

    private static final Pageable PAGE = PageRequest.of(0, 10);

    private Material buildProduct() {
        return Material.builder()
                .materialId("prod1")
                .partNumber("PROD-001")
                .materialName("IMS Vitamin D3 Tablet 1000IU")
                .materialType(MaterialType.DIETARY_SUPPLEMENT)
                .build();
    }

    private InventoryLot buildLot(String id) {
        return InventoryLot.builder()
                .lotId(id)
                .material(Material.builder().materialId("mat1").partNumber("PN-001")
                        .materialName("Vitamin D3").materialType(MaterialType.API).build())
                .manufacturerLot("LOT-001")
                .quantity(new BigDecimal("50.0"))
                .unitOfMeasure("kg")
                .status(LotStatus.Accepted)
                .receivedDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(2))
                .storageLocation("Shelf-A")
                .isSample(false)
                .build();
    }

    private ProductionBatch buildBatch(String id, BatchStatus status) {
        return ProductionBatch.builder()
                .batchId(id)
                .product(buildProduct())
                .batchNumber("BATCH-2026-001")
                .batchSize(new BigDecimal("50.0"))
                .unitOfMeasure("kg")
                .manufactureDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(status)
                .components(new ArrayList<>())
                .build();
    }

    // ── listBatches ────────────────────────────────────────────────────────

    @Test
    void listBatches_noFilter_callsFindAll() {
        when(batchRepository.findAll(PAGE)).thenReturn(new PageImpl<>(List.of(buildBatch("b1", BatchStatus.PLANNED))));

        PagedResponse<BatchResponse> result = productionBatchService.listBatches(null, null, PAGE);

        assertThat(result.getContent()).hasSize(1);
        verify(batchRepository).findAll(PAGE);
    }

    @Test
    void listBatches_statusFilter_callsFindByStatus() {
        when(batchRepository.findByStatus(BatchStatus.PLANNED, PAGE))
                .thenReturn(new PageImpl<>(List.of(buildBatch("b1", BatchStatus.PLANNED))));

        productionBatchService.listBatches(BatchStatus.PLANNED, null, PAGE);

        verify(batchRepository).findByStatus(BatchStatus.PLANNED, PAGE);
    }

    @Test
    void listBatches_statusAndProductFilter_callsCombinedQuery() {
        when(batchRepository.findByStatusAndProduct_MaterialId(BatchStatus.IN_PROGRESS, "prod1", PAGE))
                .thenReturn(new PageImpl<>(List.of()));

        productionBatchService.listBatches(BatchStatus.IN_PROGRESS, "prod1", PAGE);

        verify(batchRepository).findByStatusAndProduct_MaterialId(BatchStatus.IN_PROGRESS, "prod1", PAGE);
    }

    // ── createBatch ────────────────────────────────────────────────────────

    @Test
    void createBatch_success_createsBatchWithPlannedStatus() {
        BatchCreateRequest req = new BatchCreateRequest();
        req.setProductId("prod1");
        req.setBatchNumber("BATCH-2026-001");
        req.setBatchSize(new BigDecimal("50.0"));
        req.setUnitOfMeasure("kg");
        req.setManufactureDate(LocalDate.now());
        req.setExpirationDate(LocalDate.now().plusYears(2));

        ProductionBatch savedBatch = buildBatch("b1", BatchStatus.PLANNED);

        when(batchRepository.existsByBatchNumber("BATCH-2026-001")).thenReturn(false);
        when(materialRepository.findById("prod1")).thenReturn(Optional.of(buildProduct()));
        when(batchRepository.save(any(ProductionBatch.class))).thenReturn(savedBatch);

        BatchResponse result = productionBatchService.createBatch(req);

        assertThat(result.getBatchNumber()).isEqualTo("BATCH-2026-001");
        assertThat(result.getStatus()).isEqualTo(BatchStatus.PLANNED);
    }

    @Test
    void createBatch_duplicateBatchNumber_throwsBusinessException() {
        BatchCreateRequest req = new BatchCreateRequest();
        req.setBatchNumber("DUPLICATE");
        when(batchRepository.existsByBatchNumber("DUPLICATE")).thenReturn(true);

        assertThatThrownBy(() -> productionBatchService.createBatch(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DUPLICATE");

        verify(batchRepository, never()).save(any());
    }

    @Test
    void createBatch_productNotFound_throwsResourceNotFoundException() {
        BatchCreateRequest req = new BatchCreateRequest();
        req.setBatchNumber("BATCH-NEW");
        req.setProductId("missing");

        when(batchRepository.existsByBatchNumber("BATCH-NEW")).thenReturn(false);
        when(materialRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productionBatchService.createBatch(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Material");
    }

    // ── updateBatchStatus ──────────────────────────────────────────────────

    @Test
    void updateBatchStatus_plannedToInProgress_succeeds() {
        ProductionBatch batch = buildBatch("b1", BatchStatus.PLANNED);
        BatchStatusRequest req = new BatchStatusRequest();
        req.setStatus(BatchStatus.IN_PROGRESS);

        when(batchRepository.findById("b1")).thenReturn(Optional.of(batch));
        when(batchRepository.save(batch)).thenReturn(batch);

        BatchResponse result = productionBatchService.updateBatchStatus("b1", req);

        assertThat(result.getStatus()).isEqualTo(BatchStatus.IN_PROGRESS);
    }

    @Test
    void updateBatchStatus_batchNotFound_throwsResourceNotFoundException() {
        when(batchRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productionBatchService.updateBatchStatus("x", new BatchStatusRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── addComponent ──────────────────────────────────────────────────────

    @Test
    void addComponent_lotNotFound_throwsResourceNotFoundException() {
        when(batchRepository.findById("b1")).thenReturn(Optional.of(buildBatch("b1", BatchStatus.PLANNED)));
        when(lotRepository.findById("missing")).thenReturn(Optional.empty());

        BatchComponentRequest req = new BatchComponentRequest();
        req.setLotId("missing");

        assertThatThrownBy(() -> productionBatchService.addComponent("b1", req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("InventoryLot");
    }

    @Test
    void addComponent_batchNotFound_throwsResourceNotFoundException() {
        when(batchRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productionBatchService.addComponent("x", new BatchComponentRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ProductionBatch");
    }

    // ── confirmComponent ──────────────────────────────────────────────────

    @Test
    void confirmComponent_notFound_throwsResourceNotFoundException() {
        when(componentRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productionBatchService.confirmComponent("x", new ComponentConfirmRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("BatchComponent");
    }
}
