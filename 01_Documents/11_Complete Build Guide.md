# 11 – Complete Build Guide: Inventory Management System (IMS)

**System:** Inventory Management System (IMS)
**Version:** 2.1 (cập nhật 12/03/2026 — đồng bộ theo `dbscript.sql`)
**Date:** 10/03/2026
**Scope:** Full system — all modules from setup to deployment

---

## Table of Contents

1. [Prerequisites & Environment Setup](#phase-0-prerequisites--environment-setup)
2. [Database Setup](#phase-1-database-setup)
3. [Backend Foundation (Done)](#phase-2-backend-foundation--already-implemented)
4. [Backend – QC Testing Module](#phase-3-backend--qc-testing-module)
5. [Backend – Production Batch Module](#phase-4-backend--production-batch-module)
6. [Backend – Label Management Module](#phase-5-backend--label-management-module)
7. [Backend – Reports & Inventory Dashboard](#phase-6-backend--reports--inventory-dashboard)
8. [Frontend Foundation (Done)](#phase-7-frontend-foundation--already-implemented)
9. [Frontend – Inventory Lots UI (Complete)](#phase-8-frontend--inventory-lots-ui)
10. [Frontend – QC Testing UI](#phase-9-frontend--qc-testing-ui)
11. [Frontend – Production Batch UI](#phase-10-frontend--production-batch-ui)
12. [Frontend – Label Management UI](#phase-11-frontend--label-management-ui)
13. [Frontend – Dashboard & Reports UI](#phase-12-frontend--dashboard--reports-ui)
14. [Testing Strategy](#phase-13-testing-strategy)
15. [Deployment](#phase-14-deployment)

---

## Implementation Status Summary

| Module                           | Backend        | Frontend       |
| -------------------------------- | -------------- | -------------- |
| Material CRUD                    | ✅ Done        | ✅ Done        |
| Inventory Lot (Receive + Status) | ✅ Done        | 🔄 Partial     |
| Inventory Transactions           | ✅ Done        | 🔄 Partial     |
| QC Testing                       | ❌ Not started | ❌ Not started |
| Production Batch                 | ❌ Not started | ❌ Not started |
| Label Management                 | ❌ Not started | ❌ Not started |
| Reports / Dashboard              | ❌ Not started | ❌ Not started |
| User Management / RBAC           | ❌ Not started | ❌ Not started |

---

## Phase 0: Prerequisites & Environment Setup

### 0.1 Required Tools

| Tool                     | Version                 | Purpose                  |
| ------------------------ | ----------------------- | ------------------------ |
| JDK                      | 21+ (LTS)               | Backend runtime          |
| Gradle                   | 9.3+ (wrapper included) | Backend build            |
| Node.js                  | 20+ LTS                 | Frontend runtime         |
| npm                      | 10+                     | Frontend package manager |
| MySQL                    | 8.0+                    | Database                 |
| Git                      | Any                     | Version control          |
| IntelliJ IDEA or VS Code | Latest                  | IDE                      |

### 0.2 Project Structure

```
project-se/
├── 01_Documents/           # All design documents
├── 02_Source/
│   └── 01_Source Code/
│       ├── backend/        # Spring Boot (Gradle) project
│       │   └── src/main/java/com/erplite/inventory/
│       │       ├── controller/
│       │       ├── dto/
│       │       ├── entity/
│       │       ├── exception/
│       │       ├── repository/
│       │       └── service/
│       ├── frontend/       # React + Vite project
│       │   └── src/
│       │       ├── components/   # Shared UI components
│       │       ├── pages/        # Page-level components
│       │       └── services/     # API client (Axios)
│       └── dbscript.sql    # Full DB schema reference
└── 03_Deployment/
```

### 0.3 Start Backend

```bash
cd "02_Source/01_Source Code/backend"
./gradlew bootRun
# Server starts at http://localhost:8080
```

### 0.4 Start Frontend

```bash
cd "02_Source/01_Source Code/frontend"
npm install
npm run dev
# Dev server starts at http://localhost:5173
```

### 0.5 Key Configuration: `application.properties`

File: `backend/src/main/resources/application.properties`

```properties
spring.application.name=inventory

server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/inventory_management
spring.datasource.username=root
spring.datasource.password=<your_password>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> **Note:** Set `ddl-auto=validate` in production after schema is stable.

---

## Phase 1: Database Setup

### 1.1 Create the Database

```sql
CREATE DATABASE IF NOT EXISTS inventory_management
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE inventory_management;
```

### 1.2 Full Schema (8 Tables)

Run the complete `dbscript.sql` or create tables in the following order (respecting FK dependencies):

**Order of creation:**

1. `Users`
2. `Materials`
3. `LabelTemplates`
4. `InventoryLots` (FK → Materials, self-ref)
5. `InventoryTransactions` (FK → InventoryLots)
6. `ProductionBatches` (FK → Materials)
7. `BatchComponents` (FK → ProductionBatches, InventoryLots)
8. `QCTests` (FK → InventoryLots)

```sql
-- 1. Users
CREATE TABLE Users (
    user_id      VARCHAR(36) PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    email        VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(100) NOT NULL,
    role         ENUM('Admin','InventoryManager','QualityControl','Production','Viewer') NOT NULL DEFAULT 'Viewer',
    is_active    BOOLEAN DEFAULT TRUE,
    last_login   DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Materials
CREATE TABLE Materials (
    material_id            VARCHAR(20)  PRIMARY KEY,
    part_number            VARCHAR(20)  NOT NULL UNIQUE,
    material_name          VARCHAR(100) NOT NULL,
    material_type          ENUM('API', 'Excipient', 'Dietary Supplement', 'Container', 'Closure', 'Process Chemical', 'Testing Material') NOT NULL,
    storage_conditions     VARCHAR(100) NULL,
    specification_document VARCHAR(50)  NULL,
    created_date           DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. LabelTemplates
CREATE TABLE LabelTemplates (
    template_id      VARCHAR(20)  PRIMARY KEY,
    template_name    VARCHAR(100) NOT NULL,
    label_type       ENUM('Raw Material','Sample','Intermediate','Finished Product','API','Status') NOT NULL,
    template_content TEXT NOT NULL,
    width            DECIMAL(5,2) NOT NULL,
    height           DECIMAL(5,2) NOT NULL,
    created_date     DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 4. InventoryLots
CREATE TABLE InventoryLots (
    lot_id                 VARCHAR(36)   PRIMARY KEY,
    material_id            VARCHAR(20)   NOT NULL,
    manufacturer_name      VARCHAR(100)  NOT NULL,
    manufacturer_lot       VARCHAR(50)   NOT NULL,
    supplier_name          VARCHAR(100)  NULL,
    received_date          DATE          NOT NULL,
    expiration_date        DATE          NOT NULL,
    in_use_expiration_date DATE          NULL,
    status                 ENUM('Quarantine', 'Accepted', 'Rejected', 'Depleted') NOT NULL,
    quantity               DECIMAL(10,3) NOT NULL,
    unit_of_measure        VARCHAR(10)   NOT NULL,
    storage_location       VARCHAR(50)   NULL,
    is_sample              BOOLEAN       DEFAULT FALSE,
    parent_lot_id          VARCHAR(36)   NULL,
    po_number              VARCHAR(30)   NULL,
    receiving_form_id      VARCHAR(50)   NULL,
    created_date           DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lots_material FOREIGN KEY (material_id)   REFERENCES Materials(material_id),
    CONSTRAINT fk_lots_parent   FOREIGN KEY (parent_lot_id) REFERENCES InventoryLots(lot_id)
);

-- 5. InventoryTransactions
CREATE TABLE InventoryTransactions (
    transaction_id   VARCHAR(36)   PRIMARY KEY,
    lot_id           VARCHAR(36)   NOT NULL,
    transaction_type ENUM('Receipt', 'Usage', 'Split', 'Transfer', 'Adjustment', 'Disposal') NOT NULL,
    quantity         DECIMAL(10,3) NOT NULL,
    unit_of_measure  VARCHAR(10)   NOT NULL,
    reference_id     VARCHAR(50)   NULL,
    notes            TEXT          NULL,
    performed_by     VARCHAR(50)   NOT NULL,
    transaction_date DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_date     DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_txn_lot FOREIGN KEY (lot_id) REFERENCES InventoryLots(lot_id)
);

-- 6. ProductionBatches
CREATE TABLE ProductionBatches (
    batch_id         VARCHAR(36)   PRIMARY KEY,
    product_id       VARCHAR(20)   NOT NULL,
    batch_number     VARCHAR(50)   NOT NULL UNIQUE,
    batch_size       DECIMAL(10,3) NOT NULL,
    unit_of_measure  VARCHAR(10)   NOT NULL,
    manufacture_date DATE          NOT NULL,
    expiration_date  DATE          NOT NULL,
    status           ENUM('Planned', 'In Progress', 'Complete', 'Rejected') NOT NULL,
    created_date     DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_batch_product FOREIGN KEY (product_id) REFERENCES Materials(material_id)
);

-- 7. BatchComponents
CREATE TABLE BatchComponents (
    component_id     VARCHAR(36)   PRIMARY KEY,
    batch_id         VARCHAR(36)   NOT NULL,
    lot_id           VARCHAR(36)   NOT NULL,
    planned_quantity DECIMAL(10,3) NOT NULL,
    actual_quantity  DECIMAL(10,3) NULL,
    unit_of_measure  VARCHAR(10)   NOT NULL,
    addition_date    DATETIME      NULL,
    added_by         VARCHAR(50)   NULL,
    created_date     DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comp_batch FOREIGN KEY (batch_id) REFERENCES ProductionBatches(batch_id),
    CONSTRAINT fk_comp_lot   FOREIGN KEY (lot_id)   REFERENCES InventoryLots(lot_id)
);

-- 8. QCTests
CREATE TABLE QCTests (
    test_id             VARCHAR(36)  PRIMARY KEY,
    lot_id              VARCHAR(36)  NOT NULL,
    test_type           ENUM('Identity', 'Potency', 'Microbial', 'Growth Promotion', 'Physical', 'Chemical') NOT NULL,
    test_method         VARCHAR(100) NOT NULL,
    test_date           DATE         NOT NULL,
    test_result         VARCHAR(100) NOT NULL,
    acceptance_criteria VARCHAR(200) NULL,
    result_status       ENUM('Pass', 'Fail', 'Pending') NOT NULL DEFAULT 'Pending',
    performed_by        VARCHAR(50)  NOT NULL,
    verified_by         VARCHAR(50)  NULL,
    created_date        DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_qc_lot FOREIGN KEY (lot_id) REFERENCES InventoryLots(lot_id)
);
```

> **Tip:** `spring.jpa.hibernate.ddl-auto=update` will auto-create or alter tables on startup.
> For initial setup, run the SQL above manually for a clean, predictable schema.

---

## Phase 2: Backend Foundation — Already Implemented

The following is **already built** and functional. Review these patterns before adding new modules.

### 2.1 Established Package Structure

```
com.erplite.inventory/
├── InventoryApplication.java       # @SpringBootApplication entry point
├── controller/
│   ├── MaterialController.java     # ✅ Done
│   └── InventoryLotController.java # ✅ Done
├── dto/
│   ├── MaterialRequestDTO.java     # ✅ Done (with @Valid annotations)
│   ├── MaterialResponseDTO.java    # ✅ Done (static fromEntity factory)
│   ├── InventoryLotRequestDTO.java # ✅ Done
│   └── InventoryLotResponseDTO.java# ✅ Done
├── entity/
│   ├── Material.java               # ✅ Done (UUID PK, @PrePersist)
│   ├── InventoryLot.java           # ✅ Done
│   └── InventoryTransaction.java   # ✅ Done
├── exception/
│   ├── ResourceNotFoundException.java # ✅ Done
│   ├── BusinessException.java         # ✅ Done
│   └── GlobalExceptionHandler.java    # ✅ Done (@RestControllerAdvice)
├── repository/
│   ├── MaterialRepository.java         # ✅ Done
│   ├── InventoryLotRepository.java     # ✅ Done
│   └── InventoryTransactionRepository.java # ✅ Done
└── service/
    ├── MaterialService.java        # ✅ Done
    └── InventoryLotService.java    # ✅ Done
```

### 2.2 Coding Conventions to Follow

Every new module must follow the same patterns:

| Layer            | Convention                                                                                                                                 |
| ---------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| **Entity**       | `@Entity`, `@Table(name="snake_case")`, UUID PK generated in `@PrePersist`, Lombok `@Data @NoArgsConstructor @AllArgsConstructor @Builder` |
| **Request DTO**  | `@Data`, Jakarta `@NotBlank`/`@NotNull`/`@Size` validations                                                                                |
| **Response DTO** | `@Data`, static `fromEntity(Entity e)` factory method                                                                                      |
| **Repository**   | `extends JpaRepository<Entity, String>`, custom finders by convention                                                                      |
| **Service**      | `@Service @RequiredArgsConstructor @Transactional(readOnly=true)`, write methods annotated `@Transactional`                                |
| **Controller**   | `@RestController @RequestMapping("/api/...") @RequiredArgsConstructor @CrossOrigin(origins="*")`                                           |
| **Exceptions**   | Throw `ResourceNotFoundException("Entity", "field", value)` for 404; `BusinessException("message")` for business rule violations           |

### 2.3 Existing API Endpoints (Reference)

```
GET    /api/materials                   → list (query: keyword, type)
GET    /api/materials/{id}              → detail
POST   /api/materials                   → create
PUT    /api/materials/{id}              → update
DELETE /api/materials/{id}              → delete (blocked if lots exist)

GET    /api/lots                        → list (query: materialId, status)
GET    /api/lots/{id}                   → detail
POST   /api/lots/receive                → create lot (status=Quarantine)
PATCH  /api/lots/{id}/status            → update status
GET    /api/lots/{id}/transactions      → transaction history
```

---

## Phase 3: Backend – QC Testing Module

This phase implements Epic E2.3 and E2.4: creating QC tests for lots and auto-updating lot status.

### 3.1 Create Entity: `QCTest.java`

**File:** `entity/QCTest.java`

```java
package com.erplite.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "QCTests")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class QCTest {

    @Id
    @Column(name = "test_id", length = 36)
    private String testId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private InventoryLot lot;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false)
    private TestType testType;

    @Column(name = "test_method", nullable = false, length = 100)
    private String testMethod;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    @Column(name = "test_result", nullable = false, length = 100)
    private String testResult;

    @Column(name = "acceptance_criteria", length = 200)
    private String acceptanceCriteria;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false)
    private ResultStatus resultStatus = ResultStatus.Pending;

    @Column(name = "performed_by", nullable = false, length = 50)
    private String performedBy;

    @Column(name = "verified_by", length = 50)
    private String verifiedBy;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        if (testId == null || testId.isBlank()) testId = UUID.randomUUID().toString();
        createdDate = modifiedDate = LocalDateTime.now();
        if (resultStatus == null) resultStatus = ResultStatus.Pending;
    }

    @PreUpdate
    protected void onUpdate() { modifiedDate = LocalDateTime.now(); }

    public enum TestType { Identity, Potency, Microbial, GrowthPromotion, Physical, Chemical }
    public enum ResultStatus { Pass, Fail, Pending }
}
```

### 3.2 Create DTOs

**File:** `dto/QCTestRequestDTO.java`

```java
@Data
public class QCTestRequestDTO {

    @NotBlank
    private String lotId;

    @NotNull
    private QCTest.TestType testType;

    @NotBlank @Size(max = 100)
    private String testMethod;

    @NotNull
    private LocalDate testDate;

    @NotBlank @Size(max = 100)
    private String testResult;

    @Size(max = 200)
    private String acceptanceCriteria;

    @NotNull
    private QCTest.ResultStatus resultStatus;   // Pass / Fail / Pending

    @NotBlank @Size(max = 50)
    private String performedBy;

    @Size(max = 50)
    private String verifiedBy;
}
```

**File:** `dto/QCTestResponseDTO.java`

```java
@Data
public class QCTestResponseDTO {
    private String testId;
    private String lotId;
    private String lotManufacturerLot;
    private QCTest.TestType testType;
    private String testMethod;
    private LocalDate testDate;
    private String testResult;
    private String acceptanceCriteria;
    private QCTest.ResultStatus resultStatus;
    private String performedBy;
    private String verifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    public static QCTestResponseDTO fromEntity(QCTest t) {
        QCTestResponseDTO dto = new QCTestResponseDTO();
        dto.setTestId(t.getTestId());
        dto.setLotId(t.getLot().getLotId());
        dto.setLotManufacturerLot(t.getLot().getManufacturerLot());
        dto.setTestType(t.getTestType());
        dto.setTestMethod(t.getTestMethod());
        dto.setTestDate(t.getTestDate());
        dto.setTestResult(t.getTestResult());
        dto.setAcceptanceCriteria(t.getAcceptanceCriteria());
        dto.setResultStatus(t.getResultStatus());
        dto.setPerformedBy(t.getPerformedBy());
        dto.setVerifiedBy(t.getVerifiedBy());
        dto.setCreatedDate(t.getCreatedDate());
        dto.setModifiedDate(t.getModifiedDate());
        return dto;
    }
}
```

### 3.3 Create Repository: `QCTestRepository.java`

```java
@Repository
public interface QCTestRepository extends JpaRepository<QCTest, String> {
    List<QCTest> findByLot_LotId(String lotId);
    List<QCTest> findByLot_LotIdAndResultStatus(String lotId, QCTest.ResultStatus status);
    boolean existsByLot_LotIdAndResultStatus(String lotId, QCTest.ResultStatus status);
    long countByLot_LotId(String lotId);
}
```

### 3.4 Create Service: `QCTestService.java`

**Business rules:**

- Cannot add tests to a lot that is `Rejected` or `Depleted`.
- After recording a result, if **all** tests for the lot are `Pass` → set lot to `Accepted`.
- If **any** test is `Fail` → set lot to `Rejected`.
- Lot auto-update triggers only when `resultStatus` is `Pass` or `Fail` (not `Pending`).

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QCTestService {

    private final QCTestRepository qcTestRepository;
    private final InventoryLotRepository lotRepository;
    private final InventoryTransactionRepository transactionRepository;

    public List<QCTestResponseDTO> getTestsByLot(String lotId) {
        findLotOrThrow(lotId);
        return qcTestRepository.findByLot_LotId(lotId).stream()
                .map(QCTestResponseDTO::fromEntity).toList();
    }

    public QCTestResponseDTO getTestById(String testId) {
        return QCTestResponseDTO.fromEntity(findTestOrThrow(testId));
    }

    @Transactional
    public QCTestResponseDTO createTest(QCTestRequestDTO dto) {
        InventoryLot lot = findLotOrThrow(dto.getLotId());

        if (lot.getStatus() == InventoryLot.LotStatus.Rejected ||
            lot.getStatus() == InventoryLot.LotStatus.Depleted) {
            throw new BusinessException("Cannot add QC test to a lot with status: " + lot.getStatus());
        }

        QCTest test = QCTest.builder()
                .lot(lot)
                .testType(dto.getTestType())
                .testMethod(dto.getTestMethod())
                .testDate(dto.getTestDate())
                .testResult(dto.getTestResult())
                .acceptanceCriteria(dto.getAcceptanceCriteria())
                .resultStatus(dto.getResultStatus())
                .performedBy(dto.getPerformedBy())
                .verifiedBy(dto.getVerifiedBy())
                .build();

        test = qcTestRepository.save(test);
        evaluateAndUpdateLotStatus(lot);
        return QCTestResponseDTO.fromEntity(test);
    }

    @Transactional
    public QCTestResponseDTO updateTestResult(String testId, QCTestRequestDTO dto) {
        QCTest test = findTestOrThrow(testId);
        test.setTestResult(dto.getTestResult());
        test.setResultStatus(dto.getResultStatus());
        test.setVerifiedBy(dto.getVerifiedBy());
        test = qcTestRepository.save(test);
        evaluateAndUpdateLotStatus(test.getLot());
        return QCTestResponseDTO.fromEntity(test);
    }

    // -----------------------------------------------------------------------
    // Internal: auto-evaluate lot status after any QC result change
    // -----------------------------------------------------------------------
    private void evaluateAndUpdateLotStatus(InventoryLot lot) {
        // Only act on Quarantine lots
        if (lot.getStatus() != InventoryLot.LotStatus.Quarantine) return;

        List<QCTest> tests = qcTestRepository.findByLot_LotId(lot.getLotId());
        if (tests.isEmpty()) return;

        boolean anyFail    = tests.stream().anyMatch(t -> t.getResultStatus() == QCTest.ResultStatus.Fail);
        boolean anyPending = tests.stream().anyMatch(t -> t.getResultStatus() == QCTest.ResultStatus.Pending);

        if (anyFail) {
            lot.setStatus(InventoryLot.LotStatus.Rejected);
            lotRepository.save(lot);
            recordStatusTransaction(lot, "QC Failed → Rejected");
        } else if (!anyPending) {
            // All tests are Pass
            lot.setStatus(InventoryLot.LotStatus.Accepted);
            lotRepository.save(lot);
            recordStatusTransaction(lot, "All QC Passed → Accepted");
        }
    }

    private void recordStatusTransaction(InventoryLot lot, String notes) {
        InventoryTransaction tx = InventoryTransaction.builder()
                .lot(lot)
                .transactionType(InventoryTransaction.TransactionType.Adjustment)
                .quantity(java.math.BigDecimal.ZERO)
                .notes(notes)
                .performedBy("system")
                .build();
        transactionRepository.save(tx);
    }

    private InventoryLot findLotOrThrow(String id) {
        return lotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryLot", "id", id));
    }

    private QCTest findTestOrThrow(String id) {
        return qcTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QCTest", "id", id));
    }
}
```

### 3.5 Create Controller: `QCTestController.java`

| Method | Endpoint                  | Description         |
| ------ | ------------------------- | ------------------- |
| `GET`  | `/api/qctests?lotId={id}` | All tests for a lot |
| `GET`  | `/api/qctests/{id}`       | Test detail         |
| `POST` | `/api/qctests`            | Create new test     |
| `PUT`  | `/api/qctests/{id}`       | Update test result  |

```java
@RestController
@RequestMapping("/api/qctests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QCTestController {

    private final QCTestService qcTestService;

    @GetMapping
    public ResponseEntity<List<QCTestResponseDTO>> getTests(
            @RequestParam(required = false) String lotId) {
        if (lotId == null || lotId.isBlank())
            throw new BusinessException("lotId query parameter is required");
        return ResponseEntity.ok(qcTestService.getTestsByLot(lotId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QCTestResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(qcTestService.getTestById(id));
    }

    @PostMapping
    public ResponseEntity<QCTestResponseDTO> create(@Valid @RequestBody QCTestRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(qcTestService.createTest(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QCTestResponseDTO> update(
            @PathVariable String id, @Valid @RequestBody QCTestRequestDTO dto) {
        return ResponseEntity.ok(qcTestService.updateTestResult(id, dto));
    }
}
```

### 3.6 Extend `InventoryLotService` — Add Sample Lot Split

Add the following method to the existing `InventoryLotService`:

```java
@Transactional
public InventoryLotResponseDTO splitSampleLot(String parentLotId, BigDecimal sampleQty, String performedBy) {
    InventoryLot parent = findLotOrThrow(parentLotId);

    if (parent.getStatus() != LotStatus.Accepted && parent.getStatus() != LotStatus.Quarantine) {
        throw new BusinessException("Can only split from Quarantine or Accepted lots");
    }
    if (parent.getQuantity().compareTo(sampleQty) < 0) {
        throw new BusinessException("Insufficient quantity for split");
    }

    // Deduct from parent
    parent.setQuantity(parent.getQuantity().subtract(sampleQty));
    if (parent.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
        parent.setStatus(LotStatus.Depleted);
    }
    lotRepository.save(parent);
    recordTransaction(parent, TransactionType.Split, sampleQty.negate(), null,
            "Split to sample lot", performedBy);

    // Create child (sample) lot
    InventoryLot sample = InventoryLot.builder()
            .material(parent.getMaterial())
            .manufacturerLot(parent.getManufacturerLot())
            .quantity(sampleQty)
            .unitOfMeasure(parent.getUnitOfMeasure())
            .status(LotStatus.Quarantine)
            .receivedDate(LocalDate.now())
            .expirationDate(parent.getExpirationDate())
            .storageLocation(parent.getStorageLocation())
            .isSample(true)
            .parentLot(parent)
            .build();
    sample = lotRepository.save(sample);
    recordTransaction(sample, TransactionType.Split, sampleQty,
            parentLotId, "Split from parent lot", performedBy);

    return InventoryLotResponseDTO.fromEntity(sample);
}
```

Also add the endpoint to `InventoryLotController`:

```java
/**
 * POST /api/lots/{id}/split
 * Body: { "sampleQty": 0.500, "performedBy": "jdoe" }
 */
@PostMapping("/{id}/split")
public ResponseEntity<InventoryLotResponseDTO> splitLot(
        @PathVariable String id,
        @RequestBody Map<String, Object> body) {
    BigDecimal sampleQty = new BigDecimal(body.get("sampleQty").toString());
    String performedBy = (String) body.getOrDefault("performedBy", "");
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(lotService.splitSampleLot(id, sampleQty, performedBy));
}
```

---

## Phase 4: Backend – Production Batch Module

Implements Epic E3: plan batches, assign components, confirm usage, and complete batches.

### 4.1 Create Entities

**File:** `entity/ProductionBatch.java`

```java
@Entity
@Table(name = "ProductionBatches")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductionBatch {

    @Id
    @Column(name = "batch_id", length = 36)
    private String batchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Material product;

    @Column(name = "batch_number", nullable = false, unique = true, length = 50)
    private String batchNumber;

    @Column(name = "batch_size", nullable = false, precision = 10, scale = 3)
    private BigDecimal batchSize;

    @Column(name = "unit_of_measure", nullable = false, length = 10)
    private String unitOfMeasure;

    @Column(name = "manufacture_date", nullable = false)
    private LocalDate manufactureDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BatchStatus status = BatchStatus.Planned;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL)
    private List<BatchComponent> components = new ArrayList<>();

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        if (batchId == null || batchId.isBlank()) batchId = UUID.randomUUID().toString();
        createdDate = modifiedDate = LocalDateTime.now();
        if (status == null) status = BatchStatus.Planned;
    }

    @PreUpdate
    protected void onUpdate() { modifiedDate = LocalDateTime.now(); }

    public enum BatchStatus { Planned, InProgress, Complete, Rejected }
}
```

**File:** `entity/BatchComponent.java`

```java
@Entity
@Table(name = "BatchComponents")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BatchComponent {

    @Id
    @Column(name = "component_id", length = 36)
    private String componentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private ProductionBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private InventoryLot lot;

    @Column(name = "planned_quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal plannedQuantity;

    @Column(name = "actual_quantity", precision = 10, scale = 3)
    private BigDecimal actualQuantity;  // null until confirmed

    @Column(name = "unit_of_measure", nullable = false, length = 10)
    private String unitOfMeasure;

    @Column(name = "addition_date")
    private LocalDateTime additionDate;

    @Column(name = "added_by", length = 50)
    private String addedBy;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        if (componentId == null || componentId.isBlank()) componentId = UUID.randomUUID().toString();
        createdDate = modifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { modifiedDate = LocalDateTime.now(); }
}
```

### 4.2 Create DTOs

**`ProductionBatchRequestDTO.java`** — fields: `productId`, `batchNumber`, `batchSize`, `unitOfMeasure`, `manufactureDate`, `expirationDate`; all required.

**`ProductionBatchResponseDTO.java`** — all fields including `List<BatchComponentResponseDTO> components`, static `fromEntity` factory.

**`BatchComponentRequestDTO.java`** — fields: `batchId`, `lotId`, `plannedQuantity`, `unitOfMeasure`, `addedBy`.

**`BatchComponentResponseDTO.java`** — all fields including `lotId`, `materialName`, `lotStatus`, `availableQuantity`.

**`ConfirmUsageRequestDTO.java`** — fields: `actualQuantity`, `performedBy`.

### 4.3 Create Repositories

```java
// ProductionBatchRepository.java
public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, String> {
    boolean existsByBatchNumber(String batchNumber);
    List<ProductionBatch> findByStatus(ProductionBatch.BatchStatus status);
    List<ProductionBatch> findByProduct_MaterialId(String productId);
}

// BatchComponentRepository.java
public interface BatchComponentRepository extends JpaRepository<BatchComponent, String> {
    List<BatchComponent> findByBatch_BatchId(String batchId);
    boolean existsByBatch_BatchIdAndLot_LotId(String batchId, String lotId);
}
```

### 4.4 Create Service: `ProductionBatchService.java`

**Business rules:**

- Only `Accepted` lots with `expirationDate >= today` and `quantity >= plannedQuantity` can be added as components.
- Confirm usage: deduct `actualQuantity` from `InventoryLot.quantity`, create `Usage` transaction, auto-set lot to `Depleted` if quantity reaches 0.
- Batch can only be set to `Complete` when all components have a non-null `actualQuantity`.
- Status transitions: `Planned → InProgress → Complete | Rejected`. No transition from `Complete` or `Rejected`.

**Methods to implement:**

| Method                                                                    | Description                                 |
| ------------------------------------------------------------------------- | ------------------------------------------- |
| `getAllBatches(BatchStatus status)`                                       | List batches, optional filter by status     |
| `getBatchById(String id)`                                                 | Detail with components                      |
| `createBatch(ProductionBatchRequestDTO dto)`                              | Create with status=Planned                  |
| `addComponent(BatchComponentRequestDTO dto)`                              | Add lot to batch                            |
| `confirmActualUsage(String componentId, ConfirmUsageRequestDTO dto)`      | Record actual qty, create Usage transaction |
| `updateBatchStatus(String id, BatchStatus newStatus, String performedBy)` | Advance status with validation              |

**Key snippet — confirm actual usage:**

```java
@Transactional
public BatchComponentResponseDTO confirmActualUsage(String componentId, ConfirmUsageRequestDTO dto) {
    BatchComponent component = batchComponentRepository.findById(componentId)
            .orElseThrow(() -> new ResourceNotFoundException("BatchComponent", "id", componentId));

    ProductionBatch batch = component.getBatch();
    if (batch.getStatus() != ProductionBatch.BatchStatus.InProgress) {
        throw new BusinessException("Batch must be In Progress to confirm usage");
    }

    InventoryLot lot = component.getLot();
    if (lot.getStatus() != InventoryLot.LotStatus.Accepted) {
        throw new BusinessException("Lot must be Accepted to use in production");
    }
    if (lot.getQuantity().compareTo(dto.getActualQuantity()) < 0) {
        throw new BusinessException("Insufficient lot quantity. Available: " + lot.getQuantity());
    }
    if (lot.getExpirationDate() != null && lot.getExpirationDate().isBefore(LocalDate.now())) {
        throw new BusinessException("Lot has expired");
    }

    // Deduct from lot
    lot.setQuantity(lot.getQuantity().subtract(dto.getActualQuantity()));
    if (lot.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
        lot.setStatus(InventoryLot.LotStatus.Depleted);
    }
    lotRepository.save(lot);

    // Record Usage transaction
    InventoryTransaction tx = InventoryTransaction.builder()
            .lot(lot)
            .transactionType(InventoryTransaction.TransactionType.Usage)
            .quantity(dto.getActualQuantity().negate())
            .referenceId(batch.getBatchNumber())
            .notes("Used in batch: " + batch.getBatchNumber())
            .performedBy(dto.getPerformedBy())
            .build();
    transactionRepository.save(tx);

    // Update component
    component.setActualQuantity(dto.getActualQuantity());
    component.setAdditionDate(LocalDateTime.now());
    component.setAddedBy(dto.getPerformedBy());
    return BatchComponentResponseDTO.fromEntity(batchComponentRepository.save(component));
}
```

### 4.5 Create Controller: `ProductionBatchController.java`

| Method  | Endpoint                                        | Description                             |
| ------- | ----------------------------------------------- | --------------------------------------- |
| `GET`   | `/api/batches`                                  | List batches (query: status, productId) |
| `GET`   | `/api/batches/{id}`                             | Batch detail with components            |
| `POST`  | `/api/batches`                                  | Create batch                            |
| `PATCH` | `/api/batches/{id}/status`                      | Advance status                          |
| `POST`  | `/api/batches/{id}/components`                  | Add component                           |
| `PATCH` | `/api/batches/components/{componentId}/confirm` | Confirm actual usage                    |

---

## Phase 5: Backend – Label Management Module

Implements Epic E1.3 and E4: CRUD for label templates and label generation.

### 5.1 Create Entity: `LabelTemplate.java`

```java
@Entity
@Table(name = "LabelTemplates")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LabelTemplate {

    @Id
    @Column(name = "template_id", length = 20)
    private String templateId;

    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @Enumerated(EnumType.STRING)
    @Column(name = "label_type", nullable = false)
    private LabelType labelType;

    @Column(name = "template_content", nullable = false, columnDefinition = "TEXT")
    private String templateContent;   // HTML/Mustache template with {{placeholders}}

    @Column(name = "width", nullable = false, precision = 5, scale = 2)
    private BigDecimal width;

    @Column(name = "height", nullable = false, precision = 5, scale = 2)
    private BigDecimal height;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = modifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { modifiedDate = LocalDateTime.now(); }

    public enum LabelType {
        RawMaterial, Sample, Intermediate, FinishedProduct, API, Status
    }
}
```

### 5.2 Label Template Format

Templates use `{{placeholder}}` syntax. Supported placeholders by label type:

| Label Type        | Available Placeholders                                                                                                                                     |
| ----------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `RawMaterial`     | `{{lotId}}`, `{{materialName}}`, `{{partNumber}}`, `{{manufacturerLot}}`, `{{expirationDate}}`, `{{storageLocation}}`, `{{quantity}}`, `{{unitOfMeasure}}` |
| `Sample`          | All RawMaterial placeholders + `{{parentLotId}}`, `{{sampleDate}}`, `{{isSample}}`                                                                         |
| `FinishedProduct` | `{{batchNumber}}`, `{{productName}}`, `{{batchSize}}`, `{{unitOfMeasure}}`, `{{manufactureDate}}`, `{{expirationDate}}`                                    |
| `Status`          | `{{lotId}}`, `{{materialName}}`, `{{status}}`, `{{statusDate}}`                                                                                            |

### 5.3 Create DTOs and Repository

**`LabelTemplateRequestDTO.java`** — fields: `templateId`, `templateName`, `labelType`, `templateContent`, `width`, `height`.

**`LabelTemplateResponseDTO.java`** — all fields, static `fromEntity` factory.

**`GenerateLabelRequestDTO.java`** — fields: `templateId`, `sourceType` (LOT or BATCH), `sourceId`.

**`GenerateLabelResponseDTO.java`** — fields: `templateId`, `labelType`, `renderedContent` (HTML string), `width`, `height`.

```java
public interface LabelTemplateRepository extends JpaRepository<LabelTemplate, String> {
    List<LabelTemplate> findByLabelType(LabelTemplate.LabelType labelType);
}
```

### 5.4 Create Service: `LabelService.java`

**Key method — generate label:**

```java
@Transactional(readOnly = true)
public GenerateLabelResponseDTO generateLabel(GenerateLabelRequestDTO dto) {
    LabelTemplate template = templateRepository.findById(dto.getTemplateId())
            .orElseThrow(() -> new ResourceNotFoundException("LabelTemplate", "id", dto.getTemplateId()));

    Map<String, String> variables = resolveVariables(dto.getSourceType(), dto.getSourceId());
    String rendered = renderTemplate(template.getTemplateContent(), variables);

    GenerateLabelResponseDTO response = new GenerateLabelResponseDTO();
    response.setTemplateId(template.getTemplateId());
    response.setLabelType(template.getLabelType());
    response.setRenderedContent(rendered);
    response.setWidth(template.getWidth());
    response.setHeight(template.getHeight());
    return response;
}

private String renderTemplate(String content, Map<String, String> vars) {
    for (Map.Entry<String, String> entry : vars.entrySet()) {
        content = content.replace("{{" + entry.getKey() + "}}",
                                  entry.getValue() != null ? entry.getValue() : "");
    }
    return content;
}

private Map<String, String> resolveVariables(String sourceType, String sourceId) {
    Map<String, String> vars = new HashMap<>();
    if ("LOT".equalsIgnoreCase(sourceType)) {
        InventoryLot lot = lotRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryLot", "id", sourceId));
        vars.put("lotId",           lot.getLotId());
        vars.put("materialName",    lot.getMaterial().getMaterialName());
        vars.put("partNumber",      lot.getMaterial().getPartNumber());
        vars.put("manufacturerLot", lot.getManufacturerLot());
        vars.put("expirationDate",  String.valueOf(lot.getExpirationDate()));
        vars.put("storageLocation", lot.getStorageLocation());
        vars.put("quantity",        lot.getQuantity().toPlainString());
        vars.put("unitOfMeasure",   lot.getUnitOfMeasure());
        vars.put("parentLotId",     lot.getParentLot() != null ? lot.getParentLot().getLotId() : "");
        vars.put("sampleDate",      String.valueOf(lot.getReceivedDate()));
        vars.put("status",          lot.getStatus().name());
        vars.put("statusDate",      String.valueOf(LocalDate.now()));
    } else if ("BATCH".equalsIgnoreCase(sourceType)) {
        ProductionBatch batch = batchRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductionBatch", "id", sourceId));
        vars.put("batchNumber",     batch.getBatchNumber());
        vars.put("productName",     batch.getProduct().getMaterialName());
        vars.put("batchSize",       batch.getBatchSize().toPlainString());
        vars.put("unitOfMeasure",   batch.getUnitOfMeasure());
        vars.put("manufactureDate", String.valueOf(batch.getManufactureDate()));
        vars.put("expirationDate",  String.valueOf(batch.getExpirationDate()));
    }
    return vars;
}
```

### 5.5 Create Controller: `LabelController.java`

| Method   | Endpoint                     | Description                            |
| -------- | ---------------------------- | -------------------------------------- |
| `GET`    | `/api/labels/templates`      | List templates (query: labelType)      |
| `GET`    | `/api/labels/templates/{id}` | Template detail                        |
| `POST`   | `/api/labels/templates`      | Create template                        |
| `PUT`    | `/api/labels/templates/{id}` | Update template                        |
| `DELETE` | `/api/labels/templates/{id}` | Delete template                        |
| `POST`   | `/api/labels/generate`       | Generate label (returns rendered HTML) |

---

## Phase 6: Backend – Reports & Inventory Dashboard

Implements Epic E5: real-time inventory view, transaction history, and summary reports.

### 6.1 Create DTOs for Reports

```java
// InventoryDashboardDTO.java
@Data
public class InventoryDashboardDTO {
    private long totalMaterials;
    private long totalLots;
    private long quarantineLots;
    private long acceptedLots;
    private long rejectedLots;
    private long depletedLots;
    private long nearExpiryLots;   // expiring within 30 days
    private long activeBatches;    // InProgress batches
}

// LotSummaryDTO.java — used for near-expiry & low-stock lists
@Data
public class LotSummaryDTO {
    private String lotId;
    private String materialName;
    private String partNumber;
    private BigDecimal quantity;
    private String unitOfMeasure;
    private LotStatus status;
    private LocalDate expirationDate;
    private long daysUntilExpiry;
}
```

### 6.2 Create Service: `ReportService.java`

**Methods to implement:**

| Method                                      | Description                                                 |
| ------------------------------------------- | ----------------------------------------------------------- |
| `getDashboard()`                            | Count aggregates for all lot statuses                       |
| `getNearExpiryLots(int days)`               | Lots expiring within N days (default 30)                    |
| `getLotTraceability(String lotId)`          | Full lot detail + all transactions + QC tests + batch usage |
| `getQCReport(LocalDate from, LocalDate to)` | Pass/fail counts and rates per material                     |

**Near-expiry query — add to `InventoryLotRepository`:**

```java
@Query("SELECT l FROM InventoryLot l WHERE l.expirationDate BETWEEN :today AND :cutoff AND l.status = 'Accepted'")
List<InventoryLot> findNearExpiry(@Param("today") LocalDate today, @Param("cutoff") LocalDate cutoff);
```

### 6.3 Create Controller: `ReportController.java`

| Method | Endpoint                           | Description                 |
| ------ | ---------------------------------- | --------------------------- |
| `GET`  | `/api/reports/dashboard`           | Inventory dashboard summary |
| `GET`  | `/api/reports/near-expiry?days=30` | Near-expiry lots            |
| `GET`  | `/api/reports/lots/{id}/trace`     | Full lot traceability       |
| `GET`  | `/api/reports/qc?from=&to=`        | QC pass/fail report         |

---

## Phase 7: Frontend Foundation — Already Implemented

The following is **already built** in the frontend.

### 7.1 Existing Structure

```
frontend/src/
├── App.jsx                     ✅ Routing (/ → /materials, /materials, /lots)
├── main.jsx                    ✅ React root mount
├── index.css                   ✅ Global CSS variables and utility classes
├── App.css                     ✅ Layout styles (app-layout, main-content)
├── components/
│   ├── Sidebar.jsx             ✅ Navigation sidebar
│   ├── Modal.jsx               ✅ Reusable modal wrapper
│   └── StatusBadge.jsx         ✅ Colored badge by lot status
├── pages/
│   ├── MaterialsPage.jsx       ✅ Full CRUD with filter, table, modals
│   └── LotsPage.jsx            🔄 Partial — see Phase 8
└── services/
    └── api.js                  ✅ Axios client for materials + lots
```

### 7.2 Key CSS Class Conventions

These CSS classes are already defined and must be reused:

| Class                                                     | Purpose                              |
| --------------------------------------------------------- | ------------------------------------ |
| `.page-header` / `.page-header-left`                      | Top bar with title and action button |
| `.page-body`                                              | Main content area                    |
| `.card`                                                   | White bordered content card          |
| `.filter-bar`                                             | Flex row for filters                 |
| `.filter-chips` / `.chip.active`                          | Type filter chips                    |
| `.table-wrapper > table`                                  | Styled data table                    |
| `.btn .btn-primary .btn-outline .btn-danger .btn-sm`      | Button variants                      |
| `.form-control`                                           | Input/select styling                 |
| `.form-group .form-full .form-grid .form-label .required` | Form layout                          |
| `.modal-body .modal-footer`                               | Modal sections                       |
| `.alert .alert-error .alert-success`                      | Alert messages                       |
| `.loading-center .spinner`                                | Loading state                        |
| `.empty-state .empty-icon`                                | Empty list state                     |
| `.td-mono .td-primary .text-muted`                        | Table cell variants                  |
| `.type-badge`                                             | Material type chip                   |

### 7.3 API Client Pattern

All new modules must add their API calls to `services/api.js` following the established pattern:

```js
export const qcTestApi = {
  getByLot: (lotId) => api.get("/qctests", { params: { lotId } }),
  getById: (id) => api.get(`/qctests/${id}`),
  create: (data) => api.post("/qctests", data),
  update: (id, data) => api.put(`/qctests/${id}`, data),
};

export const batchApi = {
  getAll: (params = {}) => api.get("/batches", { params }),
  getById: (id) => api.get(`/batches/${id}`),
  create: (data) => api.post("/batches", data),
  updateStatus: (id, status, by) =>
    api.patch(`/batches/${id}/status`, { status, performedBy: by }),
  addComponent: (id, data) => api.post(`/batches/${id}/components`, data),
  confirmUsage: (componentId, data) =>
    api.patch(`/batches/components/${componentId}/confirm`, data),
};

export const labelApi = {
  getTemplates: (params = {}) => api.get("/labels/templates", { params }),
  getTemplate: (id) => api.get(`/labels/templates/${id}`),
  create: (data) => api.post("/labels/templates", data),
  update: (id, data) => api.put(`/labels/templates/${id}`, data),
  delete: (id) => api.delete(`/labels/templates/${id}`),
  generate: (data) => api.post("/labels/generate", data),
};

export const reportApi = {
  getDashboard: () => api.get("/reports/dashboard"),
  getNearExpiry: (days = 30) =>
    api.get("/reports/near-expiry", { params: { days } }),
  traceability: (lotId) => api.get(`/reports/lots/${lotId}/trace`),
  qcReport: (from, to) => api.get("/reports/qc", { params: { from, to } }),
};
```

---

## Phase 8: Frontend – Inventory Lots UI

Complete the partially-built `LotsPage.jsx` and add supporting components.

### 8.1 Directory Structure

```
src/pages/
    LotsPage.jsx            # Main page (list + receive form)
src/components/
    StatusBadge.jsx         # ✅ Already exists
    TransactionHistory.jsx  # New — transaction list for a lot
    SplitLotModal.jsx       # New — split sample lot form
```

### 8.2 Complete `LotsPage.jsx`

**Required features:**

1. **Filter bar** — filter by `status` chips (All / Quarantine / Accepted / Rejected / Depleted) and `materialId` dropdown.
2. **Lot table** — columns: Lot ID (truncated), Material, Mfr Lot, Qty + UOM, Status badge, Expiration, Actions.
3. **Receive Lot button** → opens receive modal.
4. **Row actions:** View details (expand row or drawer), Update Status, Split Sample, View Transactions.
5. **Receive Lot form fields:**
   - Material (dropdown from `/api/materials`)
   - Manufacturer Lot number (text)
   - Quantity (number, > 0)
   - Unit of Measure (text, e.g. kg, L, pcs)
   - Received Date (date, default today)
   - Expiration Date (date, required)
   - Storage Location (text)
   - Performed By (text)
6. **Status update** — inline PATCH with a `<select>` showing only valid next statuses.
7. **Transaction drawer** — slide-in panel showing `GET /api/lots/{id}/transactions`.

### 8.3 Key Implementation Notes for Lots UI

```jsx
// Status transition rules (mirror backend)
const VALID_TRANSITIONS = {
  Quarantine: ["Accepted", "Rejected"],
  Accepted: ["Depleted"],
  Rejected: ["Depleted"],
  Depleted: [],
};

// Near-expiry warning: highlight row if expirationDate < 30 days from today
const isNearExpiry = (dateStr) => {
  const expiry = new Date(dateStr);
  const today = new Date();
  const diff = (expiry - today) / (1000 * 60 * 60 * 24);
  return diff >= 0 && diff <= 30;
};
```

---

## Phase 9: Frontend – QC Testing UI

### 9.1 Directory Structure

```
src/pages/
    QCTestsPage.jsx         # List of tests for a selected lot
src/components/
    QCTestForm.jsx          # Create/edit test form
    QCResultBadge.jsx       # Pass=green, Fail=red, Pending=yellow
```

### 9.2 `QCTestsPage.jsx` — Features

1. **Lot selector** — search/select a lot by Lot ID or material name.
2. **Test table** — columns: Test Type, Method, Date, Result, Criteria, Status badge, Performed By, Verified By.
3. **Add Test button** → opens `QCTestForm` modal.
4. **Auto-refresh** after create/update — re-fetch lot detail to show updated lot status.
5. **Visual status summary** — show counts: `Pending: N`, `Pass: N`, `Fail: N`.
6. **Inline edit** — click result to update `resultStatus` and `verifiedBy`.

### 9.3 `QCTestForm.jsx` — Fields

| Field               | Type   | Notes                                                                  |
| ------------------- | ------ | ---------------------------------------------------------------------- |
| Test Type           | Select | Identity / Potency / Microbial / GrowthPromotion / Physical / Chemical |
| Test Method         | Text   | Required                                                               |
| Test Date           | Date   | Required, default today                                                |
| Test Result         | Text   | e.g. "1.02 mg/g"                                                       |
| Acceptance Criteria | Text   | e.g. "0.95–1.05 mg/g"                                                  |
| Result Status       | Select | Pending / Pass / Fail                                                  |
| Performed By        | Text   | Required                                                               |
| Verified By         | Text   | Optional, secondary reviewer                                           |

### 9.4 Route Addition

```jsx
// In App.jsx
<Route path="/qctests" element={<QCTestsPage />} />
```

Add to `Sidebar.jsx`:

```jsx
{ path: '/qctests', label: '🔬 QC Testing' }
```

---

## Phase 10: Frontend – Production Batch UI

### 10.1 Directory Structure

```
src/pages/
    BatchesPage.jsx         # List of batches
    BatchDetailPage.jsx     # Detail with components
src/components/
    BatchForm.jsx           # Create batch form
    BatchStatusBadge.jsx    # Planned=blue, InProgress=orange, Complete=green, Rejected=red
    AddComponentModal.jsx   # Add lot to batch
    ConfirmUsageModal.jsx   # Enter actual quantity
```

### 10.2 `BatchesPage.jsx` — Features

1. **Filter** by `status` chips (Planned / InProgress / Complete / Rejected).
2. **Batch table** — columns: Batch Number, Product, Batch Size, Manufacture Date, Expiry, Status, Actions.
3. **Create Batch button** → `BatchForm` modal.
4. **Row click** → navigate to `BatchDetailPage`.

### 10.3 `BatchDetailPage.jsx` — Features

1. **Batch header** — show all batch fields + current status with advance/reject buttons.
2. **Status advance button** — shows next valid status (`Planned→InProgress→Complete`).
3. **Components table** — columns: Material Name, Lot ID, Planned Qty, Actual Qty, UOM, Status, Actions.
4. **Add Component button** → `AddComponentModal` (only active when status = `InProgress`).
5. **Confirm Usage button** per component → `ConfirmUsageModal`.
6. **Validation badge** — show green checkmark when all components have `actualQuantity != null`.

### 10.4 Key Implementation Notes

```jsx
const BATCH_STATUS_TRANSITIONS = {
  Planned: ["InProgress"],
  InProgress: ["Complete", "Rejected"],
  Complete: [],
  Rejected: [],
};

// Only show Accepted, non-expired, non-depleted lots in AddComponentModal
const availableLots = lots.filter(
  (l) => l.status === "Accepted" && new Date(l.expirationDate) > new Date(),
);
```

### 10.5 Route Addition

```jsx
<Route path="/batches"     element={<BatchesPage />} />
<Route path="/batches/:id" element={<BatchDetailPage />} />
```

Add to `Sidebar.jsx`:

```jsx
{ path: '/batches', label: '🏭 Production Batches' }
```

---

## Phase 11: Frontend – Label Management UI

### 11.1 Directory Structure

```
src/pages/
    LabelsPage.jsx          # Template list + generate label
src/components/
    LabelTemplateForm.jsx   # Create/edit template
    LabelPreview.jsx        # Render generated label HTML in iframe/div
```

### 11.2 `LabelsPage.jsx` — Features

1. **Template table** — columns: Template ID, Name, Type, Dimensions (W×H), Actions.
2. **Filter** by `labelType`.
3. **Create/Edit Template** → `LabelTemplateForm` modal (template content uses a `<textarea>` for HTML).
4. **Generate Label section:**
   - Select template (dropdown).
   - Select source type: LOT or BATCH.
   - Enter source ID (text or searchable dropdown).
   - Click **Generate** → call `POST /api/labels/generate`.
   - Render the returned HTML in `LabelPreview`.
5. **Print button** on preview → `window.print()` with the label content.

### 11.3 `LabelPreview.jsx`

```jsx
export default function LabelPreview({ html, width, height }) {
  return (
    <div
      className="label-preview-wrapper"
      style={{
        width: `${width}cm`,
        height: `${height}cm`,
        border: "1px solid #ccc",
        padding: 8,
      }}
      dangerouslySetInnerHTML={{ __html: html }}
    />
  );
}
```

> **Security note:** Template content is admin-controlled. Sanitize with DOMPurify if user-submitted templates are allowed in the future.

### 11.4 Route Addition

```jsx
<Route path="/labels" element={<LabelsPage />} />
```

---

## Phase 12: Frontend – Dashboard & Reports UI

### 12.1 Directory Structure

```
src/pages/
    DashboardPage.jsx       # Inventory summary cards + near-expiry table
    ReportsPage.jsx         # QC report with date range filter
```

### 12.2 `DashboardPage.jsx` — Features

1. **Summary cards** — call `GET /api/reports/dashboard`:
   - Total Materials, Total Lots, Quarantine, Accepted, Rejected, Near Expiry.
   - Use colored card UI: Quarantine=yellow, Accepted=green, Rejected=red, Near Expiry=orange.
2. **Near-Expiry table** — call `GET /api/reports/near-expiry?days=30`:
   - Columns: Material, Lot ID, Qty, Expiry Date, Days Remaining, Status.
   - Rows sorted by `daysUntilExpiry` ascending.
   - Row turns red if `daysUntilExpiry <= 7`.
3. **Auto-refresh** — re-fetch every 5 minutes or on manual refresh button click.

### 12.3 `ReportsPage.jsx` — Features

1. **Date range picker** (from / to) for QC report.
2. **QC table** — columns: Material, Total Tests, Passed, Failed, Pass Rate %.
3. **Lot Traceability section** — enter Lot ID to fetch full trace (lot info + transactions + QC tests + batch usage).

### 12.4 Route Addition

```jsx
<Route path="/"          element={<DashboardPage />} />
<Route path="/dashboard" element={<DashboardPage />} />
<Route path="/reports"   element={<ReportsPage />} />
```

Update `Sidebar.jsx` to include all navigation entries:

```jsx
const NAV_ITEMS = [
  { path: "/dashboard", label: "📊 Dashboard" },
  { path: "/materials", label: "🧪 Materials" },
  { path: "/lots", label: "📦 Inventory Lots" },
  { path: "/qctests", label: "🔬 QC Testing" },
  { path: "/batches", label: "🏭 Production Batches" },
  { path: "/labels", label: "🏷 Labels" },
  { path: "/reports", label: "📋 Reports" },
];
```

---

## Phase 13: Testing Strategy

### 13.1 Backend Unit Tests (JUnit 5 + Mockito)

Create test files under `src/test/java/com/erplite/inventory/service/`.

**`MaterialServiceTest.java`** — tests:

- `createMaterial_success`
- `createMaterial_throwsWhenPartNumberDuplicate`
- `updateMaterial_throwsWhenPartNumberTakenByOther`
- `deleteMaterial_throwsWhenLotsExist`

**`InventoryLotServiceTest.java`** — tests:

- `receiveNewLot_createsLotWithQuarantineStatus`
- `receiveNewLot_createsReceiptTransaction`
- `updateLotStatus_quarantineToAccepted_succeeds`
- `updateLotStatus_quarantineToComplete_throws` (invalid transition)
- `splitSampleLot_deductsParentAndCreatesChild`
- `splitSampleLot_throwsWhenInsufficientQuantity`

**`QCTestServiceTest.java`** — tests:

- `createTest_pendingDoesNotChangeStatus`
- `createTest_allPass_changesLotToAccepted`
- `createTest_anyFail_changesLotToRejected`
- `createTest_throwsWhenLotIsRejected`

**`ProductionBatchServiceTest.java`** — tests:

- `createBatch_succeeds`
- `confirmUsage_deductsLotQuantity`
- `confirmUsage_throwsWhenLotNotAccepted`
- `confirmUsage_throwsWhenInsufficientQty`
- `confirmUsage_setsLotDepleted_whenQuantityReachesZero`

**Run all tests:**

```bash
./gradlew test
# Report: build/reports/tests/test/index.html
```

### 13.2 Backend Integration Tests (Full Flow)

Use Postman or `curl` to test the full workflow end-to-end:

```bash
BASE=http://localhost:8080/api

# 1. Create material
curl -s -X POST $BASE/materials -H "Content-Type: application/json" \
  -d '{"partNumber":"MAT-001","materialName":"Vitamin D3","materialType":"API"}' | jq .

# 2. Receive a lot
LOT=$(curl -s -X POST $BASE/lots/receive -H "Content-Type: application/json" \
  -d '{"materialId":"<id>","quantity":25.5,"unitOfMeasure":"kg",
       "expirationDate":"2027-01-01","performedBy":"admin"}')
LOT_ID=$(echo $LOT | jq -r '.lotId')

# 3. Verify lot status = Quarantine
curl -s $BASE/lots/$LOT_ID | jq '.status'

# 4. Add QC tests (all Pass)
curl -s -X POST $BASE/qctests -H "Content-Type: application/json" \
  -d "{\"lotId\":\"$LOT_ID\",\"testType\":\"Identity\",\"testMethod\":\"HPLC\",
       \"testDate\":\"2026-03-10\",\"testResult\":\"Conforms\",
       \"resultStatus\":\"Pass\",\"performedBy\":\"qc_user\"}"

# 5. Verify lot status changed to Accepted
curl -s $BASE/lots/$LOT_ID | jq '.status'

# 6. Create production batch
BATCH=$(curl -s -X POST $BASE/batches -H "Content-Type: application/json" \
  -d '{"productId":"<id>","batchNumber":"BATCH-001","batchSize":10,
       "unitOfMeasure":"kg","manufactureDate":"2026-03-10",
       "expirationDate":"2028-03-10"}')
BATCH_ID=$(echo $BATCH | jq -r '.batchId')

# 7. Advance to InProgress
curl -s -X PATCH $BASE/batches/$BATCH_ID/status \
  -H "Content-Type: application/json" \
  -d '{"status":"InProgress","performedBy":"operator"}'

# 8. Add component
curl -s -X POST $BASE/batches/$BATCH_ID/components \
  -H "Content-Type: application/json" \
  -d "{\"lotId\":\"$LOT_ID\",\"plannedQuantity\":2.0,
       \"unitOfMeasure\":\"kg\",\"addedBy\":\"operator\"}"

# 9. Confirm actual usage
COMP_ID=$(curl -s $BASE/batches/$BATCH_ID | jq -r '.components[0].componentId')
curl -s -X PATCH $BASE/batches/components/$COMP_ID/confirm \
  -H "Content-Type: application/json" \
  -d '{"actualQuantity":2.0,"performedBy":"operator"}'

# 10. Verify lot quantity reduced from 25.5 to 23.5
curl -s $BASE/lots/$LOT_ID | jq '.quantity'
```

### 13.3 Frontend Manual Checklist

Work through these flows in the browser (http://localhost:5173):

**Materials:**

- [ ] Create material — appears in list
- [ ] Search by keyword and filter by type
- [ ] Edit material — data updates correctly
- [ ] Delete material with linked lots — shows error
- [ ] Delete material without lots — succeeds

**Lots:**

- [ ] Receive new lot — appears with `Quarantine` status
- [ ] Filter lots by status
- [ ] View transaction history for a lot
- [ ] Update lot status (Quarantine → Accepted)
- [ ] Split sample lot — parent quantity decreases, child lot created

**QC Tests:**

- [ ] Add Pending test — lot stays Quarantine
- [ ] Update test to Pass — if all Pass, lot becomes Accepted
- [ ] Add Fail test — lot becomes Rejected
- [ ] Cannot add test to Rejected lot

**Production Batches:**

- [ ] Create batch — status Planned
- [ ] Advance to InProgress
- [ ] Add lot component (only Accepted lots shown)
- [ ] Confirm usage — lot quantity deducts
- [ ] Complete batch when all components confirmed

**Labels:**

- [ ] Create template with HTML content
- [ ] Generate label for a lot — preview renders
- [ ] Generate label for a batch — placeholders populated

---

## Phase 14: Deployment

### 14.1 Build Artifacts

**Backend:**

```bash
cd "02_Source/01_Source Code/backend"
./gradlew bootJar
# Output: build/libs/inventory-0.0.1-SNAPSHOT.jar
```

**Frontend:**

```bash
cd "02_Source/01_Source Code/frontend"
npm run build
# Output: dist/ folder (static files)
```

### 14.2 Environment Configuration for Production

Before deploying, update `application.properties`:

```properties
# Switch to validate (never auto-recreate schema in production)
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Use environment variables for secrets
spring.datasource.password=${DB_PASSWORD}
```

### 14.3 Docker Compose (Recommended)

Create `02_Source/01_Source Code/docker-compose.yml`:

```yaml
version: "3.8"

services:
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
      MYSQL_DATABASE: inventory_management
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./dbscript.sql:/docker-entrypoint-initdb.d/init.sql

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/inventory_management
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: ${DB_ROOT_PASSWORD}
    depends_on:
      - db

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

**Backend `Dockerfile`:**

```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Frontend `Dockerfile`:**

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

**Frontend `nginx.conf`:**

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    # React Router — all paths serve index.html
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy API calls to backend
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**Start everything:**

```bash
cd "02_Source/01_Source Code"
docker compose up --build
```

### 14.4 CORS Update for Production

In production, replace the wildcard `@CrossOrigin(origins = "*")` on all controllers with the actual domain:

```java
@CrossOrigin(origins = "https://your-domain.com")
```

Or configure globally in a `WebMvcConfigurer` bean:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://your-domain.com")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
    }
}
```

### 14.5 Final Deployment Checklist

- [ ] MySQL schema initialized with `dbscript.sql`
- [ ] `application.properties` has `ddl-auto=validate` and no hardcoded passwords
- [ ] Backend JAR builds successfully (`./gradlew bootJar`)
- [ ] Frontend builds successfully (`npm run build`)
- [ ] API base URL in `frontend/src/services/api.js` updated for production (or uses relative `/api/`)
- [ ] CORS origins restricted to production domain
- [ ] Docker images build and containers start cleanly
- [ ] All Phase 13 integration tests pass against the production-like environment

---

## Recommended Implementation Order

```
Week 1   Phase 1 (DB) + Phase 2 review
Week 2   Phase 3 (QC backend) + sample split
Week 3   Phase 4 (Production Batch backend)
Week 4   Phase 5 (Label backend) + Phase 6 (Reports backend)
Week 5   Phase 8 (Lots UI complete)
Week 6   Phase 9 (QC UI) + Phase 10 (Batch UI)
Week 7   Phase 11 (Label UI) + Phase 12 (Dashboard UI)
Week 8   Phase 13 (Full testing) + Phase 14 (Deployment)
```

---

## Quick Reference: All API Endpoints

| Module    | Method | Path                                   | Description               |
| --------- | ------ | -------------------------------------- | ------------------------- |
| Materials | GET    | `/api/materials`                       | List (keyword, type)      |
|           | GET    | `/api/materials/{id}`                  | Detail                    |
|           | POST   | `/api/materials`                       | Create                    |
|           | PUT    | `/api/materials/{id}`                  | Update                    |
|           | DELETE | `/api/materials/{id}`                  | Delete                    |
| Lots      | GET    | `/api/lots`                            | List (materialId, status) |
|           | GET    | `/api/lots/{id}`                       | Detail                    |
|           | POST   | `/api/lots/receive`                    | Receive new lot           |
|           | PATCH  | `/api/lots/{id}/status`                | Update status             |
|           | POST   | `/api/lots/{id}/split`                 | Split sample lot          |
|           | GET    | `/api/lots/{id}/transactions`          | Transaction history       |
| QC Tests  | GET    | `/api/qctests?lotId=`                  | Tests for a lot           |
|           | GET    | `/api/qctests/{id}`                    | Detail                    |
|           | POST   | `/api/qctests`                         | Create test               |
|           | PUT    | `/api/qctests/{id}`                    | Update result             |
| Batches   | GET    | `/api/batches`                         | List (status, productId)  |
|           | GET    | `/api/batches/{id}`                    | Detail + components       |
|           | POST   | `/api/batches`                         | Create batch              |
|           | PATCH  | `/api/batches/{id}/status`             | Update status             |
|           | POST   | `/api/batches/{id}/components`         | Add component             |
|           | PATCH  | `/api/batches/components/{id}/confirm` | Confirm usage             |
| Labels    | GET    | `/api/labels/templates`                | List templates            |
|           | GET    | `/api/labels/templates/{id}`           | Detail                    |
|           | POST   | `/api/labels/templates`                | Create template           |
|           | PUT    | `/api/labels/templates/{id}`           | Update template           |
|           | DELETE | `/api/labels/templates/{id}`           | Delete template           |
|           | POST   | `/api/labels/generate`                 | Generate label HTML       |
| Reports   | GET    | `/api/reports/dashboard`               | Summary counts            |
|           | GET    | `/api/reports/near-expiry?days=`       | Near-expiry lots          |
|           | GET    | `/api/reports/lots/{id}/trace`         | Lot traceability          |
|           | GET    | `/api/reports/qc?from=&to=`            | QC pass/fail report       |
