# 01 – Product Requirements Document

**Hệ thống:** Inventory Management System (IMS)
**Phiên bản:** 1.1
**Ngày cập nhật:** 12/03/2026

---

## 1. Tổng quan luồng (Workflow)

```
flowchart LR
  U[Users]:::t

  M[Materials]:::t -->|1:N material_id| L[InventoryLots]:::t
  L -->|1:N lot_id| T[InventoryTransactions]:::t
  L -->|1:N lot_id| QC[QCTests]:::t
  L -->|self-ref parent_lot_id| L

  M -->|1:N product_id| PB[ProductionBatches]:::t
  PB -->|1:N batch_id| BC[BatchComponents]:::t
  BC -->|N:1 lot_id| L

  LT[LabelTemplates]:::t -. used by .-> L
  LT -. used by .-> PB

classDef t fill:#f7f7f7,stroke:#333,stroke-width:1px;
```

---

## 2. Mô tả từng bảng (Schema Reference)

### A. Users

Lưu thông tin người dùng hệ thống. Dùng để kiểm soát truy cập (RBAC) và điền các trường audit.

| Cột             | Kiểu                | Ghi chú                                                                   |
| --------------- | ------------------- | ------------------------------------------------------------------------- |
| `user_id`       | VARCHAR(36) PK      | UUID                                                                      |
| `username`      | VARCHAR(50) UNIQUE  | Tên đăng nhập                                                             |
| `email`         | VARCHAR(100) UNIQUE | Email                                                                     |
| `password`      | VARCHAR(100)        | BCrypt hash                                                               |
| `role`          | ENUM                | `Admin` / `InventoryManager` / `QualityControl` / `Production` / `Viewer` |
| `is_active`     | BOOLEAN             | Tài khoản còn hiệu lực                                                    |
| `last_login`    | DATETIME NULL       | Lần đăng nhập cuối                                                        |
| `created_date`  | DATETIME            | Auto                                                                      |
| `modified_date` | DATETIME            | Auto ON UPDATE                                                            |

---

### B. Materials

Master data vật tư / sản phẩm. Là "gốc" để tạo `InventoryLot` và cũng là `product_id` trong `ProductionBatches`.

| Cột                      | Kiểu               | Ghi chú                                                                                                        |
| ------------------------ | ------------------ | -------------------------------------------------------------------------------------------------------------- |
| `material_id`            | VARCHAR(20) PK     | VD: `MAT-001`                                                                                                  |
| `part_number`            | VARCHAR(20) UNIQUE | Mã phần                                                                                                        |
| `material_name`          | VARCHAR(100)       | Tên vật tư                                                                                                     |
| `material_type`          | ENUM               | `API` / `Excipient` / `Dietary Supplement` / `Container` / `Closure` / `Process Chemical` / `Testing Material` |
| `storage_conditions`     | VARCHAR(100) NULL  | Điều kiện bảo quản                                                                                             |
| `specification_document` | VARCHAR(50) NULL   | Mã/link tài liệu kỹ thuật                                                                                      |
| `created_date`           | DATETIME           | Auto                                                                                                           |
| `modified_date`          | DATETIME           | Auto ON UPDATE                                                                                                 |

---

### C. LabelTemplates

Lưu template in nhãn (không phải log nhãn đã in). Nhãn được tạo bằng cách chọn `label_type` phù hợp và populate `template_content` bằng dữ liệu thực tế.

| Cột                | Kiểu           | Ghi chú                                                                            |
| ------------------ | -------------- | ---------------------------------------------------------------------------------- |
| `template_id`      | VARCHAR(20) PK | VD: `TPL-RM-01`                                                                    |
| `template_name`    | VARCHAR(100)   | Tên template                                                                       |
| `label_type`       | ENUM           | `Raw Material` / `Sample` / `Intermediate` / `Finished Product` / `API` / `Status` |
| `template_content` | TEXT           | HTML với placeholder `{{field}}`                                                   |
| `width`            | DECIMAL(5,2)   | Chiều rộng nhãn (cm)                                                               |
| `height`           | DECIMAL(5,2)   | Chiều cao nhãn (cm)                                                                |
| `created_date`     | DATETIME       | Auto                                                                               |
| `modified_date`    | DATETIME       | Auto ON UPDATE                                                                     |

---

### D. InventoryLots

Đại diện cho "một lần nhập" hoặc "một lô vật tư". Đây là bảng biến động mạnh nhất trong hệ thống.

| Cột                      | Kiểu                       | Ghi chú                                             |
| ------------------------ | -------------------------- | --------------------------------------------------- |
| `lot_id`                 | VARCHAR(36) PK             | UUID                                                |
| `material_id`            | VARCHAR(20) FK → Materials |                                                     |
| `manufacturer_name`      | VARCHAR(100)               | Nhà sản xuất                                        |
| `manufacturer_lot`       | VARCHAR(50)                | Số lô của nhà sản xuất                              |
| `supplier_name`          | VARCHAR(100) NULL          | Nhà cung cấp                                        |
| `received_date`          | DATE                       | Ngày nhận hàng                                      |
| `expiration_date`        | DATE                       | Ngày hết hạn                                        |
| `in_use_expiration_date` | DATE NULL                  | Hạn dùng sau khi mở                                 |
| `status`                 | ENUM                       | `Quarantine` / `Accepted` / `Rejected` / `Depleted` |
| `quantity`               | DECIMAL(10,3)              | Số lượng hiện tại                                   |
| `unit_of_measure`        | VARCHAR(10)                | Đơn vị (kg, pcs, L…)                                |
| `storage_location`       | VARCHAR(50) NULL           | Vị trí kho                                          |
| `is_sample`              | BOOLEAN                    | TRUE nếu là sample lot                              |
| `parent_lot_id`          | VARCHAR(36) NULL FK → self | Lot cha (chỉ dùng khi `is_sample = TRUE`)           |
| `po_number`              | VARCHAR(30) NULL           | Purchase Order number                               |
| `receiving_form_id`      | VARCHAR(50) NULL           | Mã phiếu nhận hàng                                  |
| `created_date`           | DATETIME                   | Auto                                                |
| `modified_date`          | DATETIME                   | Auto ON UPDATE                                      |

---

### E. InventoryTransactions

Ghi nhận toàn bộ lịch sử tăng/giảm của từng lot (audit trail đầy đủ).

| Cột                | Kiểu                           | Ghi chú                                                                |
| ------------------ | ------------------------------ | ---------------------------------------------------------------------- |
| `transaction_id`   | VARCHAR(36) PK                 | UUID                                                                   |
| `lot_id`           | VARCHAR(36) FK → InventoryLots |                                                                        |
| `transaction_type` | ENUM                           | `Receipt` / `Usage` / `Split` / `Transfer` / `Adjustment` / `Disposal` |
| `quantity`         | DECIMAL(10,3)                  | Dương (nhập) hoặc âm (xuất)                                            |
| `unit_of_measure`  | VARCHAR(10)                    | Đơn vị                                                                 |
| `reference_id`     | VARCHAR(50) NULL               | VD: batch_number, RF number, lot_id đối tác                            |
| `notes`            | TEXT NULL                      | Ghi chú tự do                                                          |
| `performed_by`     | VARCHAR(50)                    | Username thực hiện                                                     |
| `transaction_date` | DATETIME                       | Thời điểm giao dịch                                                    |
| `created_date`     | DATETIME                       | Auto                                                                   |

---

### F. ProductionBatches

Đợt sản xuất. `product_id` trỏ sang `Materials` (sản phẩm là một loại vật tư kiểu `Dietary Supplement` hoặc `API`).

| Cột                | Kiểu                       | Ghi chú                                             |
| ------------------ | -------------------------- | --------------------------------------------------- |
| `batch_id`         | VARCHAR(36) PK             | UUID                                                |
| `product_id`       | VARCHAR(20) FK → Materials | Sản phẩm được sản xuất                              |
| `batch_number`     | VARCHAR(50) UNIQUE         | VD: `BATCH-2026-001`                                |
| `batch_size`       | DECIMAL(10,3)              | Quy mô lô sản xuất                                  |
| `unit_of_measure`  | VARCHAR(10)                | Đơn vị                                              |
| `manufacture_date` | DATE                       | Ngày sản xuất                                       |
| `expiration_date`  | DATE                       | Ngày hết hạn thành phẩm                             |
| `status`           | ENUM                       | `Planned` / `In Progress` / `Complete` / `Rejected` |
| `created_date`     | DATETIME                   | Auto                                                |
| `modified_date`    | DATETIME                   | Auto ON UPDATE                                      |

---

### G. BatchComponents

Liên kết batch ↔ lot; ghi định mức (planned) và thực tế (actual) nguyên liệu tiêu thụ.

| Cột                | Kiểu                               | Ghi chú                             |
| ------------------ | ---------------------------------- | ----------------------------------- |
| `component_id`     | VARCHAR(36) PK                     | UUID                                |
| `batch_id`         | VARCHAR(36) FK → ProductionBatches |                                     |
| `lot_id`           | VARCHAR(36) FK → InventoryLots     |                                     |
| `planned_quantity` | DECIMAL(10,3)                      | Định mức kế hoạch                   |
| `actual_quantity`  | DECIMAL(10,3) NULL                 | Thực tế (NULL cho đến khi xác nhận) |
| `unit_of_measure`  | VARCHAR(10)                        | Đơn vị                              |
| `addition_date`    | DATETIME NULL                      | Thời điểm thêm vào batch            |
| `added_by`         | VARCHAR(50) NULL                   | Username thêm                       |
| `created_date`     | DATETIME                           | Auto                                |
| `modified_date`    | DATETIME                           | Auto ON UPDATE                      |

---

### H. QCTests

Kết quả kiểm nghiệm chất lượng cho từng lot. Một lot có thể có nhiều records.

| Cột                   | Kiểu                           | Ghi chú                                                                             |
| --------------------- | ------------------------------ | ----------------------------------------------------------------------------------- |
| `test_id`             | VARCHAR(36) PK                 | UUID                                                                                |
| `lot_id`              | VARCHAR(36) FK → InventoryLots |                                                                                     |
| `test_type`           | ENUM                           | `Identity` / `Potency` / `Microbial` / `Growth Promotion` / `Physical` / `Chemical` |
| `test_method`         | VARCHAR(100)                   | VD: `HPLC-UV`, `USP <61>`                                                           |
| `test_date`           | DATE                           | Ngày thực hiện                                                                      |
| `test_result`         | VARCHAR(100)                   | Kết quả thực tế                                                                     |
| `acceptance_criteria` | VARCHAR(200) NULL              | Tiêu chí chấp nhận                                                                  |
| `result_status`       | ENUM                           | `Pass` / `Fail` / `Pending`                                                         |
| `performed_by`        | VARCHAR(50)                    | Người thực hiện                                                                     |
| `verified_by`         | VARCHAR(50) NULL               | Người duyệt kết quả                                                                 |
| `created_date`        | DATETIME                       | Auto                                                                                |
| `modified_date`       | DATETIME                       | Auto ON UPDATE                                                                      |

---

## 3. Workflow chi tiết theo từng bước

### Bước 0 — User đăng nhập

| Bảng    | Field        | Thay đổi                     |
| ------- | ------------ | ---------------------------- |
| `Users` | `last_login` | `NULL` → timestamp đăng nhập |

Sau khi đăng nhập, mọi thao tác tiếp theo sẽ ghi `performed_by` / `added_by` / `verified_by` bằng `username` của user đó.

---

### Bước 1 — Tạo Material (master data)

| Bảng        | Hành động         |
| ----------- | ----------------- |
| `Materials` | INSERT 1 dòng mới |

**Ví dụ:** Tạo `MAT-001` – _Vitamin D3 100,000 IU/g_, `material_type = API`, bảo quản 2–8°C, `specification_document = SPEC-MAT-001`.

---

### Bước 2 — Nhập kho: tạo InventoryLot + ghi Receipt transaction

| Bảng                    | Hành động         | Giá trị thay đổi                                   |
| ----------------------- | ----------------- | -------------------------------------------------- |
| `InventoryLots`         | INSERT 1 dòng mới | `status = Quarantine`, `quantity = +25.500`        |
| `InventoryTransactions` | INSERT 1 record   | `transaction_type = Receipt`, `quantity = +25.500` |

**Bắt buộc khi tạo lot:** `manufacturer_name`, `manufacturer_lot`, `received_date`, `expiration_date`, `quantity`, `unit_of_measure`.
**Tuỳ chọn:** `supplier_name`, `storage_location`, `po_number`, `receiving_form_id`, `in_use_expiration_date`.

---

### Bước 3 — In nhãn Raw Material (không thay đổi DB)

| Bảng đọc                      | Mục đích                                                                                                           |
| ----------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| `LabelTemplates`              | Chọn template `label_type = Raw Material` (lấy `template_content`, `width`, `height`)                              |
| `InventoryLots` + `Materials` | Populate placeholder: `{{materialName}}`, `{{manufacturerLot}}`, `{{expirationDate}}`, `{{storageLocation}}`, v.v. |

> Schema không có bảng log nhãn đã in — đây là hành động "generate + print" thuần tuý, không ghi DB.

---

### Bước 4 — QC cho lot: tạo QCTests, cập nhật lot.status

| Bảng            | Hành động                         | Giá trị thay đổi                                             |
| --------------- | --------------------------------- | ------------------------------------------------------------ |
| `QCTests`       | INSERT 1–n records cho `lot_id`   | `result_status = Pending` ban đầu                            |
| `QCTests`       | UPDATE từng record khi có kết quả | `result_status`: `Pending → Pass` hoặc `Fail`                |
| `InventoryLots` | UPDATE `status`                   | `Quarantine → Accepted` (all Pass) hoặc `Rejected` (có Fail) |

---

### Bước 5 — Tạo Sample lot từ lot gốc

| Bảng                      | Hành động        | Giá trị thay đổi                                                            |
| ------------------------- | ---------------- | --------------------------------------------------------------------------- |
| `InventoryLots`           | INSERT lot con   | `is_sample = TRUE`, `parent_lot_id = <lot_cha_id>`, `quantity = sample_qty` |
| `InventoryTransactions`   | INSERT 2 records | Lot cha: `Split` `−sample_qty` · Lot con: `Split` `+sample_qty`             |
| `InventoryLots` (lot cha) | UPDATE           | `quantity` giảm theo `sample_qty`                                           |

**In nhãn Sample:** Dùng `LabelTemplate` với `label_type = Sample`; populate `{{parentLotId}}`, `{{quantity}}`, `{{expirationDate}}`, v.v.

---

### Bước 6 — Tạo ProductionBatch

| Bảng                | Hành động         | Giá trị thay đổi   |
| ------------------- | ----------------- | ------------------ |
| `ProductionBatches` | INSERT 1 dòng mới | `status = Planned` |

`product_id` phải là `material_id` hợp lệ trong `Materials`.

---

### Bước 7 — Thêm nguyên liệu vào batch + Usage transaction

| Bảng                    | Hành động           | Giá trị thay đổi                                                                         |
| ----------------------- | ------------------- | ---------------------------------------------------------------------------------------- |
| `BatchComponents`       | INSERT mới          | `planned_quantity` đặt ngay; `actual_quantity = NULL`                                    |
| `BatchComponents`       | UPDATE khi xác nhận | `actual_quantity = <giá trị thực>`                                                       |
| `InventoryTransactions` | INSERT              | `transaction_type = Usage`, `quantity = −actual_quantity`, `reference_id = batch_number` |
| `InventoryLots`         | UPDATE              | `quantity` giảm; nếu về `0` → `status = Depleted`                                        |

> **Ràng buộc:** Chỉ cho phép Usage nếu lot có `status = Accepted` và `quantity ≥ actual_quantity`.

---

### Bước 8 — Hoàn tất batch + in nhãn Finished Product

| Bảng                | Hành động | Giá trị thay đổi                   |
| ------------------- | --------- | ---------------------------------- |
| `ProductionBatches` | UPDATE    | `status`: `In Progress → Complete` |

**In nhãn Finished Product:** Dùng `LabelTemplate` với `label_type = Finished Product`; populate bằng dữ liệu từ `ProductionBatches` + `Materials`.

---

### Bước 9 — In nhãn Status khi lot đổi trạng thái

Khi QC làm thay đổi `status` của lot (VD: `Quarantine → Accepted` hoặc `→ Rejected`), hệ thống generate nhãn Status để dán vào lô vật lý.

**Dùng:** `LabelTemplate` với `label_type = Status`; populate `{{status}}`, `{{lotId}}`, `{{materialName}}`, `{{statusDate}}`.

---

## 4. Bảng biến động — Tổng kết

### A. InventoryLots (biến động mạnh nhất)

| Field                        | Thay đổi khi nào                                                                             |
| ---------------------------- | -------------------------------------------------------------------------------------------- |
| `quantity`                   | Receipt (+), Usage (−), Split (±), Adjustment (±), Disposal (−)                              |
| `status`                     | `Quarantine → Accepted` (QC all pass) · `→ Rejected` (QC fail) · `→ Depleted` (quantity = 0) |
| `storage_location`           | Transaction `Transfer`                                                                       |
| `is_sample`, `parent_lot_id` | Tạo sample lot                                                                               |

### B. BatchComponents

| Field              | Thay đổi khi nào                                                           |
| ------------------ | -------------------------------------------------------------------------- |
| `planned_quantity` | Set khi plan batch                                                         |
| `actual_quantity`  | `NULL → giá trị thực` khi xác nhận; kích hoạt InventoryTransaction `Usage` |

### C. ProductionBatches

| Chuyển trạng thái        | Điều kiện                                  |
| ------------------------ | ------------------------------------------ |
| `Planned → In Progress`  | Bắt đầu sản xuất                           |
| `In Progress → Complete` | Hoàn tất; trigger in nhãn Finished Product |
| `In Progress → Rejected` | Lô bị từ chối                              |

### D. QCTests

| Field           | Thay đổi khi nào                     |
| --------------- | ------------------------------------ |
| `result_status` | `Pending → Pass/Fail` khi có kết quả |
| `verified_by`   | Khi supervisor duyệt kết quả         |
