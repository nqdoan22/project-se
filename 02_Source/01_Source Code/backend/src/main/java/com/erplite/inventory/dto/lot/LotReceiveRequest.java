package com.erplite.inventory.dto.lot;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LotReceiveRequest {

    @NotBlank(message = "Material ID is required")
    private String materialId;

    @NotBlank(message = "Manufacturer name is required")
    @Size(max = 100)
    private String manufacturerName;

    @NotBlank(message = "Manufacturer lot is required")
    @Size(max = 100)
    private String manufacturerLot;

    @Size(max = 100)
    private String supplierName;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotBlank(message = "Unit of measure is required")
    @Size(max = 20)
    private String unitOfMeasure;

    private LocalDate receivedDate;

    @NotNull(message = "Expiration date is required")
    private LocalDate expirationDate;

    private LocalDate inUseExpirationDate;

    @Size(max = 100)
    private String storageLocation;

    @Size(max = 30)
    private String poNumber;

    @Size(max = 50)
    private String receivingFormId;

    private String performedBy;
}
