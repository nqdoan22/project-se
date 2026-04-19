package com.erplite.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InventoryLotRequestDTO {

    @NotBlank(message = "Material ID is required")
    private String materialId;

    @NotBlank(message = "Manufacturer lot is required")
    @Size(max = 100)
    private String manufacturerLot;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotBlank(message = "Unit of measure is required")
    @Size(max = 20)
    private String unitOfMeasure;

    private LocalDate receivedDate;

    private LocalDate expirationDate;

    @Size(max = 100)
    private String storageLocation;

    private String performedBy;
}
