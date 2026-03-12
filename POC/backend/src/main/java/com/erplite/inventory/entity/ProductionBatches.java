package com.erplite.inventory.entity;

import jakarta.persistence.*;
import jakarta.persistence.AttributeConverter;
// import jakarta.persistence.criteria.CriteriaBuilder.In;

import java.util.Date;

@Entity
@Table(name = "ProductionBatches")
public class ProductionBatches {

    private enum Status {
        Planned("Planned"), 
        InProgress("In Progress"),
        Complete("Complete"), 
        Rejected("Rejected");

        private final String enumValue;
        private Status(String enumValue) {
            this.enumValue = enumValue;
        }
        public String getEnumValue() {
            return enumValue;
        }
    }

    @Converter(autoApply = true)
    private static class StatusConverter implements AttributeConverter<Status, String> {

        @Override
        public String convertToDatabaseColumn(Status status) {
            if (status == null) {
                return null;
            }
            return status.getEnumValue();
        }

        @Override
        public Status convertToEntityAttribute(String dbData) {
            if (dbData == null) {
                return null;
            }
            for (Status status : Status.values()) {
                if (status.getEnumValue().equals(dbData)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown value: " + dbData);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @Column(name = "batch_size", nullable = false, precision = 10, scale = 3)
    private Integer batchSize;

    @Column(name = "unit_of_measure", nullable = false)
    private String unitOfMeasure;

    @Column(name = "manufacture_date", nullable = false)
    private Date manufactureDate;

    @Column(name = "expiration_date", nullable = false)
    private Date expirationDate;

    @Convert(converter = StatusConverter.class)
    private Status status;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public Date getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(Date manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}