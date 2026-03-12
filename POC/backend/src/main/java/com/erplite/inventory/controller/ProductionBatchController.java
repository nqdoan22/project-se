package com.erplite.inventory.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.erplite.inventory.controller.ProductionBatchController.ProductionBatchRequest.Component;
import com.erplite.inventory.entity.BatchComponents;
import com.erplite.inventory.entity.ProductionBatches;
import com.erplite.inventory.repository.BatchComponentsRepository;
import com.erplite.inventory.repository.ProductionBatchesRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/production")
public class ProductionBatchController {

    private final ProductionBatchesRepository productionBatchesRepository;
    private final BatchComponentsRepository batchComponentsRepository;
    
    public ProductionBatchController(ProductionBatchesRepository productionBatchesRepository, BatchComponentsRepository batchComponentsRepository) {
        this.productionBatchesRepository = productionBatchesRepository;
        this.batchComponentsRepository = batchComponentsRepository;
    }

    // move this outside the class later
    @Transactional
    private void insertItems (ProductionBatchRequest request) {
        ProductionBatches productionBatches = new ProductionBatches();
        productionBatches.setProductId(request.getProduct_id());
        productionBatches.setBatchNumber(request.getBatch_number());
        productionBatches.setBatchSize(request.getBatch_size());
        productionBatches.setUnitOfMeasure(request.getUnit_of_measure());
        productionBatches.setManufactureDate(request.getManufacture_date());
        productionBatches.setExpirationDate(request.getExpiration_date());
        ProductionBatches record = productionBatchesRepository.save(productionBatches);

        // List<Component> components = request.getComponents();
        for (Component item : request.getComponents()) {
            BatchComponents batchComponents = new BatchComponents();
            batchComponents.setBatchId(record.getId());
            batchComponents.setLotId(item.getLot_id());
            batchComponents.setPlannedQuantity(item.getPlanned_quantity());
            batchComponents.setUnitOfMeasure(item.getUnit_of_measure());
            batchComponents.setAddedBy(item.getAdded_by());
            batchComponents.setAdditionDate(new Date());
            batchComponentsRepository.save(batchComponents);
        }
    }


    @PostMapping
    public ResponseEntity<String> createProductionBatch(@Valid @RequestBody ProductionBatchRequest request) {
        // If the request is valid, save records and return a 201 Created response
        insertItems(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Production batch created successfully");
    }

    public static class ProductionBatchRequest {

        @NotBlank(message = "Product ID is required")
        private Long product_id;

        @NotBlank(message = "Batch number is required")
        private String batch_number;

        @NotNull(message = "Batch size is required")
        private Integer batch_size;

        @NotBlank(message = "Unit of measure is required")
        private String unit_of_measure;

        @NotNull(message = "Manufacture date is required")
        private Date manufacture_date;

        @NotNull(message = "Expiration date is required")
        private Date expiration_date;

        @NotNull(message = "Components are required")
        @Size(min = 1, message = "At least one component is required")
        private List<Component> components;

        // Getters and setters
        public Long getProduct_id() {
            return product_id;
        }

        public void setProduct_id(Long product_id) {
            this.product_id = product_id;
        }

        public String getBatch_number() {
            return batch_number;
        }

        public void setBatch_number(String batch_number) {
            this.batch_number = batch_number;
        }

        public Integer getBatch_size() {
            return batch_size;
        }

        public void setBatch_size(Integer batch_size) {
            this.batch_size = batch_size;
        }

        public String getUnit_of_measure() {
            return unit_of_measure;
        }

        public void setUnit_of_measure(String unit_of_measure) {
            this.unit_of_measure = unit_of_measure;
        }

        public Date getManufacture_date() {
            return manufacture_date;
        }

        public void setManufacture_date(Date manufacture_date) {
            this.manufacture_date = manufacture_date;
        }

        public Date getExpiration_date() {
            return expiration_date;
        }

        public void setExpiration_date(Date expiration_date) {
            this.expiration_date = expiration_date;
        }

        public List<Component> getComponents() {
            return components;
        }

        public void setComponents(List<Component> components) {
            this.components = components;
        }

        public static class Component {

            @NotBlank(message = "Lot ID is required")
            private Long lot_id;

            @NotNull(message = "Planned quantity is required")
            private Integer planned_quantity;

            @NotBlank(message = "Unit of measure is required")
            private String unit_of_measure;

            private String added_by = null;

            // Getters and setters
            public Long getLot_id() {
                return lot_id;
            }

            public void setLot_id(Long lot_id) {
                this.lot_id = lot_id;
            }

            public Integer getPlanned_quantity() {
                return planned_quantity;
            }

            public void setPlanned_quantity(Integer planned_quantity) {
                this.planned_quantity = planned_quantity;
            }

            public String getUnit_of_measure() {
                return unit_of_measure;
            }

            public void setUnit_of_measure(String unit_of_measure) {
                this.unit_of_measure = unit_of_measure;
            }

            public String getAdded_by() {
                return added_by;
            }

            public void setAdded_by(String added_by) {
                this.added_by = added_by;
            }
        }
    }
}
