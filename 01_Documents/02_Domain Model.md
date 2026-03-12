# 02 – Domain Model

**Hệ thống:** Inventory Management System (IMS)  
**Phiên bản:** 1.1  
**Ngày cập nhật:** 12/03/2026

---

## 1. Sơ đồ tổng quan

![Domain Model Diagram](./images/domain%20model.png)

---

## 2. Danh sách Entity

### Users

Người dùng hệ thống. Quản lý quyền truy cập theo vai trò (RBAC) và cung cấp thông tin audit cho mọi thao tác.

- **PK:** `user_id` (UUID)
- **Thuộc tính chính:** `username`, `email`, `password` (BCrypt), `role`, `is_active`, `last_login`
- **Roles:** `Admin` · `InventoryManager` · `QualityControl` · `Production` · `Viewer`

---

### Materials

Master data nguyên vật liệu và thành phẩm. Là điểm khởi đầu của mọi luồng nghiệp vụ.

- **PK:** `material_id` (VD: `MAT-001`)
- **Thuộc tính chính:** `part_number` (unique), `material_name`, `material_type`, `storage_conditions`, `specification_document`
- **material_type:** `API` · `Excipient` · `Dietary Supplement` · `Container` · `Closure` · `Process Chemical` · `Testing Material`
- **Quan hệ:**
  - 1 Material → N InventoryLots (`material_id`)
  - 1 Material → N ProductionBatches (`product_id`) — khi Material là thành phẩm

---

### LabelTemplates

Template HTML dùng để in nhãn. Không lưu log nhãn đã in — chỉ lưu template tái sử dụng.

- **PK:** `template_id` (VD: `TPL-RM-01`)
- **Thuộc tính chính:** `template_name`, `label_type`, `template_content` (HTML với `{{placeholder}}`), `width`, `height`
- **label_type:** `Raw Material` · `Sample` · `Intermediate` · `Finished Product` · `API` · `Status`

---

### InventoryLots

Đại diện cho một lô vật tư cụ thể đã nhập kho. Là trung tâm của luồng nghiệp vụ.

- **PK:** `lot_id` (UUID)
- **FK:** `material_id` → Materials · `parent_lot_id` → self (self-referencing cho sample lot)
- **Thuộc tính chính:** `manufacturer_name`, `manufacturer_lot`, `supplier_name`, `received_date`, `expiration_date`, `in_use_expiration_date`, `quantity`, `unit_of_measure`, `storage_location`, `is_sample`, `po_number`, `receiving_form_id`
- **status:** `Quarantine` → `Accepted` / `Rejected` → `Depleted`
- **Quan hệ:**
  - 1 Lot → N InventoryTransactions
  - 1 Lot → N QCTests
  - 1 Lot → N BatchComponents (nhiều batch dùng cùng một lot)
  - 1 Lot (cha) → N Lots (con sample, qua `parent_lot_id`)

---

### InventoryTransactions

Audit trail đầy đủ của mọi biến động số lượng trên từng lot.

- **PK:** `transaction_id` (UUID)
- **FK:** `lot_id` → InventoryLots
- **Thuộc tính chính:** `transaction_type`, `quantity` (±), `unit_of_measure`, `reference_id`, `notes`, `performed_by`, `transaction_date`
- **transaction_type:** `Receipt` · `Usage` · `Split` · `Transfer` · `Adjustment` · `Disposal`

---

### ProductionBatches

Đợt sản xuất. Liên kết đến Materials qua `product_id` — sản phẩm đầu ra là một loại Material.

- **PK:** `batch_id` (UUID)
- **FK:** `product_id` → Materials
- **Thuộc tính chính:** `batch_number` (unique), `batch_size`, `unit_of_measure`, `manufacture_date`, `expiration_date`
- **status:** `Planned` → `In Progress` → `Complete` / `Rejected`
- **Quan hệ:**
  - 1 Batch → N BatchComponents

---

### BatchComponents

Bảng nối BatchComponents ↔ InventoryLots. Lưu cả định mức và thực tế tiêu thụ.

- **PK:** `component_id` (UUID)
- **FK:** `batch_id` → ProductionBatches · `lot_id` → InventoryLots
- **Thuộc tính chính:** `planned_quantity`, `actual_quantity` (NULL cho đến khi xác nhận), `unit_of_measure`, `addition_date`, `added_by`

---

### QCTests

Kết quả kiểm nghiệm chất lượng cho từng lot. Kết quả QC quyết định trạng thái cuối của lot.

- **PK:** `test_id` (UUID)
- **FK:** `lot_id` → InventoryLots
- **Thuộc tính chính:** `test_type`, `test_method`, `test_date`, `test_result`, `acceptance_criteria`, `result_status`, `performed_by`, `verified_by`
- **test_type:** `Identity` · `Potency` · `Microbial` · `Growth Promotion` · `Physical` · `Chemical`
- **result_status:** `Pending` → `Pass` / `Fail`

---

## 3. Quan hệ giữa các Entity

| Quan hệ                   | Từ                | Đến                   | Loại         | FK              |
| ------------------------- | ----------------- | --------------------- | ------------ | --------------- |
| Vật tư → Lô kho           | Materials         | InventoryLots         | 1 : N        | `material_id`   |
| Vật tư → Lô sản xuất      | Materials         | ProductionBatches     | 1 : N        | `product_id`    |
| Lô kho → Giao dịch        | InventoryLots     | InventoryTransactions | 1 : N        | `lot_id`        |
| Lô kho → Kiểm nghiệm      | InventoryLots     | QCTests               | 1 : N        | `lot_id`        |
| Lô kho → Lot con          | InventoryLots     | InventoryLots         | 1 : N (self) | `parent_lot_id` |
| Lô kho → Thành phần batch | InventoryLots     | BatchComponents       | 1 : N        | `lot_id`        |
| Lô sản xuất → Thành phần  | ProductionBatches | BatchComponents       | 1 : N        | `batch_id`      |

---

## 4. Vòng đời trạng thái

### InventoryLot.status

```
(nhập kho) → Quarantine
  → Accepted   [khi tất cả QCTests đều Pass]
  → Rejected   [khi có ít nhất 1 QCTest Fail]
  → Depleted   [khi quantity = 0 sau Usage/Disposal]
```

### ProductionBatch.status

```
(tạo mới) → Planned
  → In Progress  [khi bắt đầu sản xuất]
  → Complete     [khi xác nhận xong actual usage]
  → Rejected     [khi lô bị từ chối]
```

### QCTest.result_status

```
(tạo mới) → Pending
  → Pass  [kết quả đạt tiêu chí]
  → Fail  [kết quả không đạt]
```
