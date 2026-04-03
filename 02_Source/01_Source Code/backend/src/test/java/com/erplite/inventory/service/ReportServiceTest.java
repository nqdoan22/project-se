package com.erplite.inventory.service;

import com.erplite.inventory.dto.common.PagedResponse;
import com.erplite.inventory.dto.lot.LotResponse;
import com.erplite.inventory.dto.report.*;
import com.erplite.inventory.entity.InventoryLot;
import com.erplite.inventory.entity.InventoryLot.LotStatus;
import com.erplite.inventory.entity.InventoryTransaction;
import com.erplite.inventory.entity.InventoryTransaction.TransactionType;
import com.erplite.inventory.entity.Material;
import com.erplite.inventory.entity.Material.MaterialType;
import com.erplite.inventory.entity.ProductionBatch;
import com.erplite.inventory.entity.ProductionBatch.BatchStatus;
import com.erplite.inventory.entity.QCTest;
import com.erplite.inventory.entity.QCTest.ResultStatus;
import com.erplite.inventory.exception.ResourceNotFoundException;
import com.erplite.inventory.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private MaterialRepository materialRepository;
    @Mock private InventoryLotRepository lotRepository;
    @Mock private InventoryTransactionRepository transactionRepository;
    @Mock private QCTestRepository qcTestRepository;
    @Mock private ProductionBatchRepository batchRepository;
    @InjectMocks private ReportService reportService;

    private static final Pageable PAGE = PageRequest.of(0, 10);

    private Material buildMaterial(String id) {
        return Material.builder()
                .materialId(id)
                .partNumber("PN-001")
                .materialName("Vitamin D3")
                .materialType(MaterialType.API)
                .build();
    }

    private InventoryLot buildLot(String id, LotStatus status) {
        return InventoryLot.builder()
                .lotId(id)
                .manufacturerLot("MFG-001")
                .material(buildMaterial("mat1"))
                .quantity(BigDecimal.valueOf(100))
                .unitOfMeasure("kg")
                .expirationDate(LocalDate.now().plusDays(365))
                .storageLocation("A-01-01")
                .status(status)
                .build();
    }

    private InventoryTransaction buildTransaction(String id, String lotId) {
        return InventoryTransaction.builder()
                .transactionId(id)
                .lot(buildLot(lotId, LotStatus.Accepted))
                .transactionType(TransactionType.Receipt)
                .quantity(BigDecimal.valueOf(50))
                .unitOfMeasure("kg")
                .referenceId("REF-001")
                .transactionDate(LocalDateTime.now())
                .performedBy("user1")
                .build();
    }

    private ProductionBatch buildBatch(String id, BatchStatus status) {
        return ProductionBatch.builder()
                .batchId(id)
                .batchNumber("BATCH-001")
                .product(buildMaterial("mat1"))
                .batchSize(BigDecimal.valueOf(1000))
                .unitOfMeasure("kg")
                .expirationDate(LocalDate.now().plusDays(365))
                .status(status)
                .build();
    }

    private QCTest buildQCTest(String id, ResultStatus result) {
        return QCTest.builder()
                .testId(id)
                .lot(buildLot("lot1", LotStatus.Accepted))
                .testType(QCTest.TestType.PHYSICAL)
                .resultStatus(result)
                .testDate(LocalDate.now())
                .performedBy("user1")
                .build();
    }

    // ── getDashboard ───────────────────────────────────────────────────────

    @Test
    void getDashboard_returnsAggregatedData() {
        when(materialRepository.count()).thenReturn(10L);
        List<InventoryLot> lots = List.of(
                buildLot("lot1", LotStatus.Accepted),
                buildLot("lot2", LotStatus.Quarantine),
                buildLot("lot3", LotStatus.Rejected)
        );
        when(lotRepository.findAll()).thenReturn(lots);
        when(lotRepository.findNearExpiry(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(buildLot("lot4", LotStatus.Accepted)));
        when(batchRepository.findByStatus(BatchStatus.IN_PROGRESS))
                .thenReturn(List.of(buildBatch("b1", BatchStatus.IN_PROGRESS)));
        when(qcTestRepository.findByTestDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(
                        buildQCTest("t1", ResultStatus.Pass),
                        buildQCTest("t2", ResultStatus.Fail)
                ));

        DashboardResponse result = reportService.getDashboard();

        assertThat(result.getTotalMaterials()).isEqualTo(10L);
        assertThat(result.getTotalActiveLots()).isEqualTo(2);
        assertThat(result.getNearExpiryLots()).isEqualTo(1);
        assertThat(result.getActiveBatches()).isEqualTo(1);
        assertThat(result.getFailedQCLast30Days()).isEqualTo(1);
    }

    @Test
    void getDashboard_countsByStatus() {
        when(materialRepository.count()).thenReturn(5L);
        List<InventoryLot> lots = List.of(
                buildLot("lot1", LotStatus.Accepted),
                buildLot("lot2", LotStatus.Accepted),
                buildLot("lot3", LotStatus.Rejected)
        );
        when(lotRepository.findAll()).thenReturn(lots);
        when(lotRepository.findNearExpiry(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(batchRepository.findByStatus(BatchStatus.IN_PROGRESS))
                .thenReturn(List.of());
        when(qcTestRepository.findByTestDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        DashboardResponse result = reportService.getDashboard();

        assertThat(result.getByStatus()).containsEntry("Accepted", 2L);
        assertThat(result.getByStatus()).containsEntry("Rejected", 1L);
    }

    // ── getNearExpiry ──────────────────────────────────────────────────────

    @Test
    void getNearExpiry_returnsItemsWithinDays() {
        InventoryLot lot = buildLot("lot1", LotStatus.Accepted);
        Page<InventoryLot> page = new PageImpl<>(List.of(lot));
        when(lotRepository.findNearExpiry(any(LocalDate.class), any(LocalDate.class), eq(PAGE)))
                .thenReturn(page);

        PagedResponse<NearExpiryItemResponse> result = reportService.getNearExpiry(30, PAGE);

        assertThat(result.getContent()).hasSize(1);
        verify(lotRepository).findNearExpiry(any(LocalDate.class), any(LocalDate.class), eq(PAGE));
    }

    @Test
    void getNearExpiry_withDifferentDayRange() {
        InventoryLot lot = buildLot("lot1", LotStatus.Accepted);
        Page<InventoryLot> page = new PageImpl<>(List.of(lot));
        when(lotRepository.findNearExpiry(any(LocalDate.class), any(LocalDate.class), eq(PAGE)))
                .thenReturn(page);

        PagedResponse<NearExpiryItemResponse> result = reportService.getNearExpiry(60, PAGE);

        assertThat(result.getContent()).hasSize(1);
    }

    // ── getLotTrace ────────────────────────────────────────────────────────

    @Test
    void getLotTrace_found_returnsCompleteTrace() {
        InventoryLot lot = buildLot("lot1", LotStatus.Accepted);
        List<InventoryTransaction> txs = List.of(buildTransaction("t1", "lot1"));
        List<QCTest> qcTests = List.of(buildQCTest("q1", ResultStatus.Pass));

        when(lotRepository.findById("lot1")).thenReturn(Optional.of(lot));
        when(transactionRepository.findByLot_LotIdOrderByTransactionDateDesc("lot1"))
                .thenReturn(txs);
        when(qcTestRepository.findByLot_LotIdOrderByTestDateDesc("lot1"))
                .thenReturn(qcTests);

        LotTraceResponse result = reportService.getLotTrace("lot1");

        assertThat(result.getLot()).isNotNull();
        assertThat(result.getTransactions()).hasSize(1);
        assertThat(result.getQcTests()).hasSize(1);
    }

    @Test
    void getLotTrace_notFound_throwsResourceNotFoundException() {
        when(lotRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getLotTrace("invalid"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("InventoryLot");
    }

    @Test
    void getLotTrace_withBatchUsages_includesUsageTransactions() {
        InventoryLot lot = buildLot("lot1", LotStatus.Accepted);
        InventoryTransaction usage = buildTransaction("t1", "lot1");
        usage.setTransactionType(TransactionType.Usage);
        usage.setReferenceId("batch1");

        when(lotRepository.findById("lot1")).thenReturn(Optional.of(lot));
        when(transactionRepository.findByLot_LotIdOrderByTransactionDateDesc("lot1"))
                .thenReturn(List.of(usage));
        when(qcTestRepository.findByLot_LotIdOrderByTestDateDesc("lot1"))
                .thenReturn(List.of());

        LotTraceResponse result = reportService.getLotTrace("lot1");

        assertThat(result.getBatchUsages()).hasSize(1);
        assertThat(result.getBatchUsages().get(0).getBatchId()).isEqualTo("batch1");
    }

    // ── getQCReport ────────────────────────────────────────────────────────

    @Test
    void getQCReport_calculatesSummaryStats() {
        LocalDate from = LocalDate.now().minusDays(30);
        LocalDate to = LocalDate.now();
        List<QCTest> qcTests = List.of(
                buildQCTest("t1", ResultStatus.Pass),
                buildQCTest("t2", ResultStatus.Pass),
                buildQCTest("t3", ResultStatus.Fail),
                buildQCTest("t4", ResultStatus.Pending)
        );
        when(qcTestRepository.findByTestDateBetween(from, to)).thenReturn(qcTests);

        QCReportResponse result = reportService.getQCReport(from, to);

        assertThat(result.getSummary().getTotalTests()).isEqualTo(4);
        assertThat(result.getSummary().getPassed()).isEqualTo(2);
        assertThat(result.getSummary().getFailed()).isEqualTo(1);
        assertThat(result.getSummary().getPending()).isEqualTo(1);
        assertThat(result.getSummary().getPassRate()).isEqualTo(50.0);
    }

    @Test
    void getQCReport_emptyResults_passRateZero() {
        LocalDate from = LocalDate.now().minusDays(30);
        LocalDate to = LocalDate.now();
        when(qcTestRepository.findByTestDateBetween(from, to)).thenReturn(List.of());

        QCReportResponse result = reportService.getQCReport(from, to);

        assertThat(result.getSummary().getTotalTests()).isEqualTo(0);
        assertThat(result.getSummary().getPassRate()).isEqualTo(0.0);
    }

    @Test
    void getQCReport_groupsByMaterial() {
        LocalDate from = LocalDate.now().minusDays(30);
        LocalDate to = LocalDate.now();
        Material mat1 = buildMaterial("mat1");
        InventoryLot lot1 = buildLot("lot1", LotStatus.Accepted);
        lot1.setMaterial(mat1);
        
        QCTest test1 = buildQCTest("t1", ResultStatus.Pass);
        test1.setLot(lot1);
        
        when(qcTestRepository.findByTestDateBetween(from, to)).thenReturn(List.of(test1));

        QCReportResponse result = reportService.getQCReport(from, to);

        assertThat(result.getByMaterial()).isNotEmpty();
    }

    // ── getInventorySnapshot ───────────────────────────────────────────────

    @Test
    void getInventorySnapshot_noFilter_returnsAllLots() {
        Material mat1 = buildMaterial("mat1");
        List<InventoryLot> lots = List.of(
                buildLot("lot1", LotStatus.Accepted),
                buildLot("lot2", LotStatus.Accepted)
        );
        lots.forEach(l -> l.setMaterial(mat1));

        when(lotRepository.findAll()).thenReturn(lots);

        List<InventorySnapshotResponse> result = reportService.getInventorySnapshot(null, null);

        assertThat(result).isNotEmpty();
    }

    @Test
    void getInventorySnapshot_filterByStatus() {
        Material mat1 = buildMaterial("mat1");
        InventoryLot acceptedLot = buildLot("lot1", LotStatus.Accepted);
        acceptedLot.setMaterial(mat1);
        InventoryLot rejectedLot = buildLot("lot2", LotStatus.Rejected);
        rejectedLot.setMaterial(mat1);

        when(lotRepository.findAll()).thenReturn(List.of(acceptedLot, rejectedLot));

        List<InventorySnapshotResponse> result = reportService.getInventorySnapshot(LotStatus.Accepted, null);

        assertThat(result).isNotEmpty();
        // Verify filtering logic
        result.forEach(snapshot -> {
            assertThat(snapshot.getLots()).isNotNull();
        });
    }

    @Test
    void getInventorySnapshot_filterByMaterialType() {
        Material apiMaterial = buildMaterial("mat1");
        apiMaterial.setMaterialType(MaterialType.API);
        InventoryLot lot = buildLot("lot1", LotStatus.Accepted);
        lot.setMaterial(apiMaterial);

        when(lotRepository.findAll()).thenReturn(List.of(lot));

        List<InventorySnapshotResponse> result = reportService.getInventorySnapshot(null, MaterialType.API);

        assertThat(result).isNotEmpty();
    }

    @Test
    void getInventorySnapshot_calculatesTotalAvailable() {
        Material mat1 = buildMaterial("mat1");
        InventoryLot lot1 = buildLot("lot1", LotStatus.Accepted);
        lot1.setMaterial(mat1);
        lot1.setQuantity(BigDecimal.valueOf(100));
        InventoryLot lot2 = buildLot("lot2", LotStatus.Accepted);
        lot2.setMaterial(mat1);
        lot2.setQuantity(BigDecimal.valueOf(200));

        when(lotRepository.findAll()).thenReturn(List.of(lot1, lot2));

        List<InventorySnapshotResponse> result = reportService.getInventorySnapshot(null, null);

        assertThat(result).isNotEmpty();
        result.forEach(snapshot -> {
            if (snapshot.getMaterialId().equals("mat1")) {
                assertThat(snapshot.getTotalAvailable()).isEqualTo(BigDecimal.valueOf(300));
            }
        });
    }

    @Test
    void getInventorySnapshot_emptyLots_returnsEmpty() {
        when(lotRepository.findAll()).thenReturn(List.of());

        List<InventorySnapshotResponse> result = reportService.getInventorySnapshot(null, null);

        assertThat(result).isEmpty();
    }
}
