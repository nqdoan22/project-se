# 10 – Build Guide: Material Management Module

**Hệ thống:** Inventory Management System (IMS)
**Phiên bản:** 1.1 (cập nhật 12/03/2026 — đồng bộ theo `dbscript.sql`)
**Tập trung:** Module Quản lý Vật tư (Material Management) — Epic E1 & E2

---

## Tổng quan module

Module Material Management là **nền tảng** của toàn bộ hệ thống. Mọi hoạt động (nhập kho, sản xuất, QC…) đều phụ thuộc vào master data của Materials.

**Phạm vi:**

- CRUD nguyên vật liệu (Materials)
- Quản lý Inventory Lot (nhập kho, trạng thái lot)
- Ghi lịch sử giao dịch (InventoryTransactions)
- Tìm kiếm, lọc danh sách

**Stack hiện tại:**

- Backend: Java Spring Boot, JPA/Hibernate, MySQL (`inventory_management`)
- Package gốc: `com.erplite.inventory`
- Frontend: React + Tailwind (chưa triển khai)

---

## Checklist tiến độ

- [ ] Phase 1 – Chuẩn bị & Database
- [ ] Phase 2 – Backend: Material CRUD
- [ ] Phase 3 – Backend: InventoryLot & Transaction
- [ ] Phase 4 – Frontend: Material Management UI
- [ ] Phase 5 – Frontend: Lot & Transaction UI
- [ ] Phase 6 – Kiểm thử & Tích hợp

---

## Phase 1 – Chuẩn bị & Database

### 1.1 Tạo schema MySQL

Tạo database nếu chưa có:

```sql
CREATE DATABASE IF NOT EXISTS inventory_management
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 1.2 Tạo các bảng

```sql
-- Bảng Materials (master data vật tư)
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

-- Bảng InventoryLots (tồn kho theo lô)
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

-- Bảng InventoryTransactions (lịch sử giao dịch)
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
```

### 1.3 Kiểm tra cấu hình `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_management
spring.datasource.username=root
spring.datasource.password=<your_password>
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

## Phase 2 – Backend: Material CRUD

Theo convention đã có (tham khảo `User`, `UserService`, `UserController`), áp dụng cùng pattern cho Material.

### 2.1 Tạo Entity: `Material.java`

**Vị trí:** `src/main/java/com/erplite/inventory/entity/Material.java`

```java
package com.erplite.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "materials")
@Data @NoArgsConstructor @AllArgsConstructor
public class Material {

    @Id
    @Column(name = "material_id", length = 36)
    private String materialId;

    @Column(name = "part_number", nullable = false, unique = true, length = 50)
    private String partNumber;

    @Column(name = "material_name", nullable = false, length = 200)
    private String materialName;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false)
    private MaterialType materialType;

    @Column(name = "storage_conditions", length = 100)
    private String storageConditions;

    @Column(name = "specification_document", length = 50)
    private String specificationDocument;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = modifiedDate = LocalDateTime.now();
        if (materialId == null) materialId = java.util.UUID.randomUUID().toString();
    }

    @PreUpdate
    protected void onUpdate() { modifiedDate = LocalDateTime.now(); }

    public enum MaterialType {
        API, Excipient, Dietary_Supplement, Container, Closure, Process_Chemical, Testing_Material
    }
}
```

### 2.2 Tạo DTO: `MaterialDTO.java`

**Vị trí:** `src/main/java/com/erplite/inventory/dto/MaterialDTO.java`

```java
package com.erplite.inventory.dto;

import com.erplite.inventory.entity.Material.MaterialType;
import lombok.Data;

@Data
public class MaterialDTO {
    private String partNumber;
    private String materialName;
    private MaterialType materialType;  // API / Excipient / Dietary_Supplement / Container / Closure / Process_Chemical / Testing_Material
    private String storageConditions;
    private String specificationDocument;
}
```

### 2.3 Tạo Repository: `MaterialRepository.java`

**Vị trí:** `src/main/java/com/erplite/inventory/repository/MaterialRepository.java`

```java
package com.erplite.inventory.repository;

import com.erplite.inventory.entity.Material;
import com.erplite.inventory.entity.Material.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, String> {
    Optional<Material> findByPartNumber(String partNumber);
    List<Material> findByMaterialType(MaterialType type);
    List<Material> findByMaterialNameContainingIgnoreCase(String keyword);
    boolean existsByPartNumber(String partNumber);
}
```

### 2.4 Tạo Service: `MaterialService.java`

**Vị trí:** `src/main/java/com/erplite/inventory/service/MaterialService.java`

Các method cần implement:

| Method                                                              | Mô tả                              |
| ------------------------------------------------------------------- | ---------------------------------- |
| `List<Material> getAllMaterials()`                                  | Lấy tất cả vật tư                  |
| `Optional<Material> getMaterialById(String id)`                     | Lấy theo ID                        |
| `List<Material> searchMaterials(String keyword, MaterialType type)` | Tìm kiếm + lọc                     |
| `Material createMaterial(MaterialDTO dto)`                          | Tạo mới, check `partNumber` unique |
| `Material updateMaterial(String id, MaterialDTO dto)`               | Cập nhật                           |
| `void deleteMaterial(String id)`                                    | Xoá (nếu chưa có lot liên kết)     |

> ⚠️ **Validation:** Không cho xoá Material nếu đã có InventoryLot liên kết.

### 2.5 Tạo Controller: `MaterialController.java`

**Vị trí:** `src/main/java/com/erplite/inventory/controller/MaterialController.java`

| HTTP Method | Endpoint              | Chức năng                                            |
| ----------- | --------------------- | ---------------------------------------------------- |
| `GET`       | `/api/materials`      | Lấy danh sách (hỗ trợ query param `?keyword=&type=`) |
| `GET`       | `/api/materials/{id}` | Lấy chi tiết theo ID                                 |
| `POST`      | `/api/materials`      | Tạo material mới                                     |
| `PUT`       | `/api/materials/{id}` | Cập nhật material                                    |
| `DELETE`    | `/api/materials/{id}` | Xoá material                                         |

### 2.6 Kiểm thử API (Material)

Sử dụng Postman hoặc `curl`:

```bash
# Tạo mới
curl -X POST http://localhost:8080/api/materials \
  -H "Content-Type: application/json" \
  -d '{"partNumber":"MAT-001","materialName":"Vitamin D3 100K","materialType":"API"}'

# Lấy danh sách
curl http://localhost:8080/api/materials

# Tìm kiếm theo keyword
curl "http://localhost:8080/api/materials?keyword=Vitamin"

# Xoá
curl -X DELETE http://localhost:8080/api/materials/MAT-001
```

---

## Phase 3 – Backend: InventoryLot & Transaction

### 3.1 Tạo Entity: `InventoryLot.java`

**Vị trí:** `src/main/java/com/erplite/inventory/entity/InventoryLot.java`

Fields cần có:

- `lotId` (UUID, PK)
- `material` (`@ManyToOne` → `Material`)
- `manufacturerLot`, `quantity`, `unitOfMeasure`
- `status` (`LotStatus` enum: `Quarantine`, `Accepted`, `Rejected`, `Depleted`)
- `receivedDate`, `expirationDate`, `storageLocation`
- `isSample`, `parentLot` (self-reference `@ManyToOne`)
- `createdDate`, `modifiedDate`

### 3.2 Tạo Entity: `InventoryTransaction.java`

**Vị trí:** `src/main/java/com/erplite/inventory/entity/InventoryTransaction.java`

Fields cần có:

- `transactionId` (UUID, PK)
- `lot` (`@ManyToOne` → `InventoryLot`)
- `transactionType` (`TransactionType` enum: `Receipt`, `Usage`, `Split`, `Transfer`, `Adjustment`, `Disposal`)
- `quantity` (dương/âm tuỳ loại)
- `transactionDate`, `referenceId`, `notes`, `performedBy`

### 3.3 Tạo Service: `InventoryLotService.java`

| Method                                           | Logic nghiệp vụ                                              |
| ------------------------------------------------ | ------------------------------------------------------------ |
| `receiveNewLot(dto)`                             | Tạo lot (status=Quarantine) + ghi Receipt transaction (+qty) |
| `getLotsByMaterial(materialId)`                  | Lấy lot theo material                                        |
| `getLotsByStatus(status)`                        | Lọc theo trạng thái                                          |
| `updateLotStatus(lotId, newStatus)`              | Cập nhật status + ghi Status Change transaction              |
| `deductQuantity(lotId, qty, refId, performedBy)` | Trừ tồn + ghi Usage transaction; nếu qty=0 → Depleted        |
| `splitSampleLot(parentLotId, sampleQty)`         | Tạo sample lot (is_sample=true) + ghi Split transaction      |

> **Business rule quan trọng:**
>
> - Không Usage nếu `status ≠ Accepted`
> - Không Usage nếu `quantity < required`
> - Không Usage nếu `expirationDate < today`
> - Tự động chuyển → `Depleted` khi `quantity ≤ 0`

### 3.4 Tạo Controller: `InventoryLotController.java`

| HTTP Method | Endpoint                      | Chức năng                                              |
| ----------- | ----------------------------- | ------------------------------------------------------ |
| `GET`       | `/api/lots`                   | Danh sách lot (filter: status, materialId, nearExpiry) |
| `GET`       | `/api/lots/{id}`              | Chi tiết lot                                           |
| `POST`      | `/api/lots/receive`           | Nhập kho (tạo lot mới)                                 |
| `PATCH`     | `/api/lots/{id}/status`       | Cập nhật status                                        |
| `POST`      | `/api/lots/{id}/split`        | Tách sample lot                                        |
| `GET`       | `/api/lots/{id}/transactions` | Lịch sử giao dịch của lot                              |

---

## Phase 4 – Frontend: Material Management UI

### 4.1 Cấu trúc thư mục

```
src/features/material/
├── pages/
│   ├── MaterialListPage.jsx       # Danh sách + tìm kiếm
│   └── MaterialDetailPage.jsx     # Chi tiết + sửa/xoá
├── components/
│   ├── MaterialTable.jsx          # Bảng danh sách
│   ├── MaterialForm.jsx           # Form tạo/sửa
│   └── MaterialFilter.jsx         # Bộ lọc (type, keyword)
└── services/
    └── materialApi.js             # Axios calls đến /api/materials
```

### 4.2 Xây dựng `materialApi.js`

```js
import axios from "axios";
const BASE = "http://localhost:8080/api/materials";

export const getAllMaterials = (params) => axios.get(BASE, { params });
export const getMaterialById = (id) => axios.get(`${BASE}/${id}`);
export const createMaterial = (data) => axios.post(BASE, data);
export const updateMaterial = (id, data) => axios.put(`${BASE}/${id}`, data);
export const deleteMaterial = (id) => axios.delete(`${BASE}/${id}`);
```

### 4.3 Xây dựng `MaterialListPage.jsx`

Tính năng:

- Hiển thị bảng `MaterialTable` với cột: Part Number, Name, Type, Storage, Actions
- Tích hợp `MaterialFilter` (lọc theo type, tìm theo keyword)
- Nút **"+ Thêm vật tư"** mở modal `MaterialForm`
- Phân trang (page size 20)

### 4.4 Xây dựng `MaterialForm.jsx`

Fields trong form:
| Field | Kiểu input | Validation |
|-------|-----------|-----------|
| Part Number | Text | Required, unique |
| Material Name | Text | Required |
| Material Type | Select (`API`, `Excipient`, `Dietary Supplement`, `Container`, `Closure`, `Process Chemical`, `Testing Material`) | Required |
| Storage Conditions | Textarea | Optional |
| Specification Document | Text (mã hoặc link) | Optional |

### 4.5 Routing

Thêm vào `App.js`:

```jsx
<Route path="/materials"     element={<MaterialListPage />} />
<Route path="/materials/:id" element={<MaterialDetailPage />} />
```

---

## Phase 5 – Frontend: Inventory Lot UI

### 5.1 Cấu trúc thư mục

```
src/features/inventory/
├── pages/
│   ├── LotListPage.jsx
│   └── LotDetailPage.jsx
├── components/
│   ├── LotTable.jsx
│   ├── ReceiveLotForm.jsx         # Form nhập kho
│   ├── LotStatusBadge.jsx         # Badge màu theo status
│   └── TransactionHistory.jsx     # Bảng lịch sử giao dịch
└── services/
    └── inventoryApi.js
```

### 5.2 Xây dựng `ReceiveLotForm.jsx`

Form nhập kho cần có:

- Chọn Material (dropdown từ `/api/materials`)
- Manufacturer Lot number
- Quantity + Unit of Measure
- Received Date (default: hôm nay)
- Expiration Date
- Storage Location

**Sau khi submit thành công:** Hiển thị thông báo và link đến trang chi tiết lot vừa tạo.

### 5.3 Hiển thị `LotStatusBadge`

| Status     | Màu badge  |
| ---------- | ---------- |
| Quarantine | 🟡 Vàng    |
| Accepted   | 🟢 Xanh lá |
| Rejected   | 🔴 Đỏ      |
| Depleted   | ⚫ Xám     |

---

## Phase 6 – Kiểm thử & Tích hợp

### 6.1 Unit Test Backend

Viết test cho `MaterialService` và `InventoryLotService`:

- `createMaterial_shouldThrow_whenPartNumberDuplicate()`
- `receiveNewLot_shouldCreateLot_withQuarantineStatus()`
- `deductQuantity_shouldThrow_whenLotNotAccepted()`
- `deductQuantity_shouldSetDepleted_whenQuantityBecomesZero()`
- `splitSampleLot_shouldDecreaseParentQuantity()`

**Công cụ:** JUnit 5 + Mockito

### 6.2 Integration Test (API)

```bash
# Chạy server
./gradlew bootRun

# Test flow đầy đủ:
# 1. POST /api/materials → tạo MAT-001
# 2. POST /api/lots/receive → tạo lot với status=Quarantine
# 3. GET /api/lots/{id} → kiểm tra status=Quarantine
# 4. PATCH /api/lots/{id}/status body={status:Accepted} → chuyển Accepted
# 5. GET /api/lots/{id}/transactions → xem lịch sử
```

### 6.3 Frontend Integration Test

Kiểm tra các luồng sau trên trình duyệt:

- [ ] Tạo Material mới → xuất hiện trong danh sách
- [ ] Tìm kiếm Material theo keyword/type
- [ ] Sửa Material → dữ liệu cập nhật đúng
- [ ] Xoá Material đã có Lot → hiển thị lỗi phù hợp
- [ ] Nhập kho Lot mới → lot xuất hiện với status Quarantine
- [ ] Xem lịch sử giao dịch của Lot

---

## Thứ tự triển khai đề xuất

```
[Tuần 1] Phase 1 (DB) → Phase 2 (Material CRUD backend)
[Tuần 2] Phase 3 (Lot & Transaction backend)
[Tuần 3] Phase 4 (Material UI frontend)
[Tuần 4] Phase 5 (Lot UI frontend) → Phase 6 (Testing)
```

---

## Liên kết tài liệu liên quan

- [Product Requirements Document](./01_Product%20Requirements%20Document.md)
- [Product Backlog](./04_Product%20Backlog.md) — Epic E1, E2
- [Architecture](./05_Architecture.md)
- [Domain Model](./02_Domain%20Model.md)
