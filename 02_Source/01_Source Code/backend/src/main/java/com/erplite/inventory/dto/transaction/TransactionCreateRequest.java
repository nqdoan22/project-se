package com.erplite.inventory.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionCreateRequest {

    @NotBlank(message = "Lot ID is required")
    private String lotId;

    @NotBlank(message = "Transaction type is required")
    private String transactionType;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    @NotBlank(message = "Unit of measure is required")
    @Size(max = 20)
    private String unitOfMeasure;

    @Size(max = 50)
    private String referenceId;

    @Size(max = 500)
    private String notes;

    @NotBlank(message = "Performed by is required")
    @Size(max = 50)
    private String performedBy;

    private LocalDateTime transactionDate;
}
