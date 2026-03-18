package com.erplite.inventory.service;

import com.erplite.inventory.dto.batch.*;
import com.erplite.inventory.dto.common.PagedResponse;
import com.erplite.inventory.entity.BatchComponent;
import com.erplite.inventory.entity.InventoryLot;
import com.erplite.inventory.entity.InventoryTransaction.TransactionType;
import com.erplite.inventory.entity.Material;
import com.erplite.inventory.entity.ProductionBatch;
import com.erplite.inventory.entity.ProductionBatch.BatchStatus;
import com.erplite.inventory.exception.BusinessException;
import com.erplite.inventory.exception.ResourceNotFoundException;
import com.erplite.inventory.repository.BatchComponentRepository;
import com.erplite.inventory.repository.InventoryLotRepository;
import com.erplite.inventory.repository.InventoryTransactionRepository;
import com.erplite.inventory.repository.MaterialRepository;
import com.erplite.inventory.repository.ProductionBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionBatchService {

    private final ProductionBatchRepository batchRepository;
    private final BatchComponentRepository componentRepository;
    private final MaterialRepository materialRepository;
    private final InventoryLotRepository lotRepository;
    private final InventoryTransactionRepository transactionRepository;

    public PagedResponse<BatchResponse> listBatches(BatchStatus status, String productId, Pageable pageable) {
        Page<ProductionBatch> page;
        if (status != null && productId != null) {
            page = batchRepository.findByStatusAndProduct_MaterialId(status, productId, pageable);
        } else if (status != null) {
            page = batchRepository.findByStatus(status, pageable);
        } else if (productId != null) {
            page = batchRepository.findByProduct_MaterialId(productId, pageable);
        } else {
            page = batchRepository.findAll(pageable);
        }
        return PagedResponse.from(page.map(b -> BatchResponse.from(b, false)));
    }

    public BatchResponse getBatchById(String id) {
        ProductionBatch batch = findBatchOrThrow(id);
        return BatchResponse.from(batch, true);
    }

    @Transactional
    public BatchResponse createBatch(BatchCreateRequest req) {
        if (batchRepository.existsByBatchNumber(req.getBatchNumber())) {
            throw new BusinessException("Batch number already exists: " + req.getBatchNumber());
        }
        Material product = materialRepository.findById(req.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Material", "id", req.getProductId()));
        ProductionBatch batch = ProductionBatch.builder()
            .product(product)
            .batchNumber(req.getBatchNumber())
            .batchSize(req.getBatchSize())
            .unitOfMeasure(req.getUnitOfMeasure())
            .manufactureDate(req.getManufactureDate())
            .expirationDate(req.getExpirationDate())
            .status(BatchStatus.PLANNED)
            .build();
        return BatchResponse.from(batchRepository.save(batch), false);
    }

    @Transactional
    public BatchResponse updateBatchStatus(String id, BatchStatusRequest req) {
        ProductionBatch batch = findBatchOrThrow(id);
        batch.setStatus(req.getStatus());
        return BatchResponse.from(batchRepository.save(batch), true);
    }

    @Transactional
    public ComponentResponse addComponent(String batchId, BatchComponentRequest req) {
        ProductionBatch batch = findBatchOrThrow(batchId);
        if (batch.getStatus() != BatchStatus.PLANNED) {
            throw new BusinessException("Cannot add component to batch with status: " + batch.getStatus());
        }
        InventoryLot lot = lotRepository.findById(req.getLotId())
            .orElseThrow(() -> new ResourceNotFoundException("InventoryLot", "id", req.getLotId()));
        BatchComponent component = BatchComponent.builder()
            .batch(batch)
            .lot(lot)
            .plannedQuantity(req.getPlannedQuantity())
            .unitOfMeasure(req.getUnitOfMeasure())
            .additionDate(LocalDateTime.now())
            .addedBy(req.getAddedBy())
            .build();
        return ComponentResponse.from(componentRepository.save(component));
    }

    @Transactional
    public ComponentResponse confirmComponent(String componentId, ComponentConfirmRequest req) {
        BatchComponent component = componentRepository.findById(componentId)
            .orElseThrow(() -> new ResourceNotFoundException("BatchComponent", "id", componentId));
        component.setActualQuantity(req.getActualQuantity());
        component = componentRepository.save(component);

        InventoryLot lot = component.getLot();
        com.erplite.inventory.entity.InventoryTransaction tx =
            com.erplite.inventory.entity.InventoryTransaction.builder()
                .lot(lot)
                .transactionType(TransactionType.Usage)
                .quantity(req.getActualQuantity().negate())
                .unitOfMeasure(component.getUnitOfMeasure())
                .referenceId(component.getBatch().getBatchId())
                .notes("Used in batch: " + component.getBatch().getBatchNumber())
                .performedBy(req.getPerformedBy())
                .build();
        transactionRepository.save(tx);
        return ComponentResponse.from(component);
    }

    private ProductionBatch findBatchOrThrow(String id) {
        return batchRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ProductionBatch", "id", id));
    }

    @Transactional
    public void deleteComponent(String componentId) {
        BatchComponent component = componentRepository.findById(componentId)
            .orElseThrow(() -> new ResourceNotFoundException("BatchComponent", "id", componentId));
        if (component.getActualQuantity() != null) {
            throw new BusinessException("Cannot delete component that has been confirmed");
        }
        ProductionBatch batch = component.getBatch();
        if (batch.getStatus() != BatchStatus.PLANNED) {
            throw new BusinessException("Cannot delete component from batch with status: " + batch.getStatus());
        }
        componentRepository.delete(component);
    }

    @Transactional
    public ComponentResponse updateComponent(String componentId, BatchComponentRequest req) {
        BatchComponent component = componentRepository.findById(componentId)
            .orElseThrow(() -> new ResourceNotFoundException("BatchComponent", "id", componentId));
        if (component.getActualQuantity() != null) {
            throw new BusinessException("Cannot modify component that has been confirmed");
        }
        ProductionBatch batch = component.getBatch();
        if (batch.getStatus() != BatchStatus.PLANNED) {
            throw new BusinessException("Cannot modify component in batch with status: " + batch.getStatus());
        }
        component.setPlannedQuantity(req.getPlannedQuantity());
        component.setUnitOfMeasure(req.getUnitOfMeasure());
        return ComponentResponse.from(componentRepository.save(component));
    }
}