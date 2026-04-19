package com.erplite.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "InventoryLots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLot {

    @Id
    @Column(name = "lot_id", length = 36)
    private String lotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "manufacturer_lot", nullable = false, length = 100)
    private String manufacturerLot;

    @Builder.Default
    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LotStatus status = LotStatus.Quarantine;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "storage_location", length = 100)
    private String storageLocation;

    @Builder.Default
    @Column(name = "is_sample")
    private Boolean isSample = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_lot_id")
    private InventoryLot parentLot;

    @Column(name = "manufacturer_name", nullable = false, length = 100)
    private String manufacturerName;

    @Column(name = "supplier_name", length = 100)
    private String supplierName;

    @Column(name = "in_use_expiration_date")
    private LocalDate inUseExpirationDate;

    @Column(name = "po_number", length = 30)
    private String poNumber;

    @Column(name = "receiving_form_id", length = 50)
    private String receivingFormId;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        if (lotId == null || lotId.isBlank()) {
            lotId = UUID.randomUUID().toString();
        }
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (status == null)
            status = LotStatus.Quarantine;
        if (isSample == null)
            isSample = false;
        if (quantity == null)
            quantity = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }

    public enum LotStatus {
        Quarantine, Accepted, Rejected, Depleted
    }
}
