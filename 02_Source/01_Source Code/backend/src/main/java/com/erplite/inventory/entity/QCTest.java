package com.erplite.inventory.entity;

import com.erplite.inventory.converter.TestTypeConverter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "QCTests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QCTest {

    @Id
    @Column(name = "test_id", length = 36)
    private String testId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private InventoryLot lot;

    @Convert(converter = TestTypeConverter.class)
    @Column(name = "test_type", nullable = false, length = 20)
    private TestType testType;

    @Column(name = "test_method", length = 100)
    private String testMethod;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "test_result", length = 500)
    private String testResult;

    @Column(name = "acceptance_criteria", length = 500)
    private String acceptanceCriteria;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false, length = 20)
    private ResultStatus resultStatus;

    @Column(name = "performed_by", length = 50)
    private String performedBy;

    @Column(name = "verified_by", length = 50)
    private String verifiedBy;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        if (testId == null || testId.isBlank()) {
            testId = UUID.randomUUID().toString();
        }
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }

    public enum TestType {
        IDENTITY("Identity"),
        POTENCY("Potency"),
        MICROBIAL("Microbial"),
        GROWTH_PROMOTION("Growth Promotion"),
        PHYSICAL("Physical"),
        CHEMICAL("Chemical");

        private final String dbValue;

        TestType(String dbValue) {
            this.dbValue = dbValue;
        }

        @JsonValue
        public String getDbValue() { return dbValue; }

        @JsonCreator
        public static TestType fromJson(String value) {
            for (TestType t : values()) {
                if (t.dbValue.equals(value) || t.name().equals(value)) return t;
            }
            throw new IllegalArgumentException("Unknown TestType: " + value);
        }
    }

    public enum ResultStatus {
        Pass, Fail, Pending
    }
}
