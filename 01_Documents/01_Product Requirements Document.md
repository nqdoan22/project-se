## 1. Tổng quan luồng (workflow) theo quan hệ bảng:

```
flowchart LR
  U[Users]:::t

  M[Materials]:::t -->|1:N material_id| L[InventoryLots]:::t
  L -->|1:N lot_id| T[InventoryTransactions]:::t
  L -->|1:N lot_id| QC[QCTests]:::t

  M -->|1:N product_id| PB[ProductionBatches]:::t
  PB -->|1:N batch_id| BC[BatchComponents]:::t
  BC -->|N:1 lot_id| L

  LT[LabelTemplates]:::t -. used by .-> L
  LT -. used by .-> PB

classDef t fill:#f7f7f7,stroke:#333,stroke-width:1px;
```

## 2.“Bảng nào có dữ liệu gì” (những cột quan trọng theo workflow)

### A. Users (người thực hiện thao tác)

- Dùng để điền các trường audit như performed_by, added_by, verified_by.
- Các cột chính: user_id, username, email, password, role, is_active, last_login, created_date, modified_date.

### B. Materials (master data vật tư / sản phẩm)

- Là “gốc” để tạo lot và cũng là “product” cho batch (thông qua product_id).
- Cột chính: material_id, part_number, material_name, material_type, storage_conditions, specification_document, created_date, modified_date.

### C. InventoryLots (tồn kho theo lô)

- Đại diện cho “một lần nhập” hoặc “một lô vật tư”: có status, quantity, ngày nhận/hết hạn…
- Các trường quan trọng theo biến động:
  - status: Quarantine / Accepted / Rejected / Depleted
  - quantity: số lượng hiện tại (bị tăng/giảm theo transaction)
  - is_sample + parent_lot_id: phục vụ tách sample từ lot gốc

### D. InventoryTransactions (lịch sử tăng/giảm theo lot)

- Ghi nhận movement: Receipt / Usage / Split / Transfer / Adjustment / Disposal
- Trường quan trọng:
  - transaction_type
  - quantity (dương/âm tuỳ loại)
  - lot_id
  - reference_id (VD batch_number / order…)
  - performed_by (ai làm)
  - transaction_date

### E. QCTests (kết quả kiểm nghiệm cho lot)

- Gắn theo lot_id, nhiều record cho 1 lot.
- Quan trọng nhất:
  - test_type, test_method, test_date
  - result_status: Pass / Fail / Pending
  - performed_by, verified_by

### F. ProductionBatches (đợt sản xuất)

- Có status: Planned / In Progress / Complete / Rejected
- Liên kết sản phẩm qua product_id trỏ sang Materials.
- Trường chính: batch_id, product_id, batch_number, batch_size, unit_of_measure, manufacture_date, expiration_date, status.

### G. BatchComponents (định mức & thực tế nguyên liệu dùng cho batch)

- Link batch ↔ lot, có planned_quantity và actual_quantity.
- Khi có actual usage, sẽ “trigger” InventoryTransaction Usage âm trên lot tương ứng (theo example flow).

### H. LabelTemplates (template in nhãn)

- Lưu template (không phải log nhãn đã in).
- Nhãn được tạo bằng cách chọn label_type phù hợp và “populate” template_content bằng dữ liệu từ InventoryLot hoặc ProductionBatch.

## 3.Workflow chi tiết theo từng bước (và “giá trị nào đổi như thế nào”)

- Bước 0 — User đăng nhập (chuẩn bị audit)
  - Bảng ảnh hưởng: Users
  - Khi user login: có thể cập nhật last_login (nullable → timestamp).
  - Sau này mọi thao tác sẽ ghi performed_by/added_by/verified_by.
  - Giá trị thay đổi:
    Users.last_login: NULL → 2025-01-29 14:30:00 (ví dụ trong tài liệu).
- Bước 1 — Tạo Material (master data)
  - Bảng tạo mới: Materials  
    Ví dụ: tạo MAT-001 (Vitamin D3 100K).
  - Giá trị thay đổi  
    Tạo 1 dòng mới trong Materials:
    - material_id = MAT-001
    - material_type = API (ví dụ)
    - created_date/modified_date = NOW
- Bước 2 — Nhập kho: tạo InventoryLot + ghi Receipt transaction
  - Theo tài liệu: nhận lot lot-uuid-001 cho MAT-001 với 25.5 kg và tạo InventoryTransaction loại Receipt +25.5.  
    Bảng tạo mới
    - InventoryLots (tạo 1 dòng mới)
    - lot_id = lot-uuid-001
    - material_id = MAT-001
    - status: thường bắt đầu ở Quarantine (vì có QC sau đó mới Accepted)
    - quantity = 25.500
    - unit_of_measure = kg
    - received_date, expiration_date, …
    - InventoryTransactions (tạo 1 dòng mới)
    - transaction_type = Receipt
    - quantity = +25.500
    - lot_id = lot-uuid-001
    - performed_by = jdoe (ví dụ)
  - Giá trị thay đổi
    - InventoryLots.quantity: 0 → 25.500
    - InventoryLots.status: (khởi tạo) Quarantine
    - InventoryTransactions: thêm record Receipt
- Bước 3 — In nhãn Raw Material (không tạo bảng mới, chỉ “generate”)
  - Tài liệu mô tả: dùng LabelTemplate TPL-RM-01 loại Raw Material, populate template bằng dữ liệu của lot.
  - Bảng đọc dữ liệu
    - LabelTemplates: chọn theo label_type = Raw Material (có template_content, width/height).
    - InventoryLots + Materials: lấy field để đổ vào template (material_name, lot_id, manufacturer_lot, expiration_date, storage_location…).
  - Giá trị thay đổi
    - Không bắt buộc thay đổi DB (schema không có bảng “PrintedLabels/LabelRuns”). Chỉ là hành vi “generate + print”.
- Bước 4 — QC cho lot: tạo QCTests, và cập nhật status của lot theo kết quả
  - Tài liệu: tạo QC test (Identity, Potency) cho lot-uuid-001; khi tất cả Pass → lot.status = Accepted.
  - Bảng tạo mới
    - QCTests: tạo 1..n record cho lot_id = lot-uuid-001:
    - test_type (Identity/Potency/…)
    - result_status (Pending → Pass/Fail)
    - performed_by, verified_by
  - Giá trị thay đổi  
    Trong QCTests:
    - result_status: Pending → Pass (hoặc Fail)

    Trong InventoryLots:
    - status: Quarantine → Accepted khi “all pass”

  (Trường hợp fail: Rejected; trường hợp hết hàng: Depleted)

- Bước 5 — Tạo sample lot từ lot gốc (nếu có)
  - Tài liệu: nếu tạo sample lot từ lot-uuid-001 với is_sample: true thì in nhãn Sample, có thông tin parent lot, sample date…
  - Bảng tạo mới / thay đổi
    - InventoryLots:
      - Tạo lot mới (sample lot):
        - is_sample = true
        - parent_lot_id = lot-uuid-001
        - quantity = sample_qty
    - InventoryTransactions (thường sẽ có để cân tồn)
      - Gợi ý theo schema transaction_type = Split để tách từ lot cha sang lot con (schema có Split).
      - Lot cha: ghi Split -sample_qty
      - Lot con: ghi Receipt/Split +sample_qty (tuỳ rule bạn thiết kế; schema cho phép Split và Receipt)
  - Giá trị thay đổi (điển hình)
    - Lot cha (lot-uuid-001): quantity giảm theo sample
    - Lot con (sample): quantity tăng tương ứng
    - In nhãn Sample: chọn LabelTemplate label_type = Sample

- Bước 6 — Tạo ProductionBatch cho sản phẩm (product là 1 Material)
  - Tài liệu: tạo batch-uuid-001 cho PROD-001 (một Material).
  - Bảng tạo mới
    - ProductionBatches:
      - product_id = PROD-001 (FK → Materials)
      - batch_number, batch_size, manufacture_date, expiration_date
      - status khởi tạo: Planned (theo enum)
  - Giá trị thay đổi
    - Tạo 1 dòng mới; về sau status thường chuyển:
      - Planned → In Progress → Complete (hoặc Rejected)

- Bước 7 — Add nguyên liệu vào batch: BatchComponents + trừ tồn bằng Usage transaction
  - Tài liệu: BatchComponent link batch-uuid-001 ↔ lot-uuid-001 planned/actual 2 kg, và trigger InventoryTransaction Usage -2kg trên lot đó.
  - Bảng tạo mới / thay đổi
    - BatchComponents (tạo mới)
      - batch_id = batch-uuid-001
      - lot_id = lot-uuid-001
      - planned_quantity = 2.000
      - actual_quantity = 2.000 (có thể ban đầu NULL rồi cập nhật sau)
      - addition_date, added_by
    - InventoryTransactions (tạo mới)
      - transaction_type = Usage
      - quantity = -2.000
      - reference_id có thể là batch_number/batch_id (schema cho phép)
      - performed_by = user thao tác
    - InventoryLots (update)
      - quantity: 25.500 → 23.500 (trừ 2.000)
      - Nếu về 0 thì status có thể chuyển Depleted (enum có).

- Bước 8 — Hoàn tất batch: cập nhật status + in nhãn Finished Product
  - Tài liệu: khi batch-uuid-001.status đổi sang Complete thì in nhãn Finished Product, populate bằng batch data (batch_number, product_name, manufacture_date, expiration_date, batch_size…).
  - Bảng thay đổi
    - ProductionBatches.status: In Progress → Complete
  - Bảng đọc để in nhãn
    - LabelTemplates chọn label_type = Finished Product
    - ProductionBatches + Materials (để lấy product_name/material_name)

- Bước 9 — Khi status lot/batch đổi: có thể in nhãn Status
  - Tài liệu nói: nếu kết quả QC làm đổi trạng thái lot thì có thể generate nhãn Status để dán physical lot.
  - Chi tiết label generation: status label xuất hiện khi lot/batch status đổi (vd Quarantine → Accepted).

## 4. "Bảng biến động" (chốt lại: bước nào làm thay đổi field nào)

### A. InventoryLots (biến động mạnh nhất)

- quantity thay đổi khi có InventoryTransactions (Receipt/Usage/Split/…)
- status thay đổi theo QC & tình trạng tồn:
  - Quarantine → Accepted khi QC pass hết
  - Rejected nếu fail
  - Depleted nếu quantity về 0
- is_sample, parent_lot_id thay đổi khi tạo sample lot

### B. BatchComponents

- planned_quantity thường set ngay khi plan
- actual_quantity có thể:
  - NULL lúc tạo → cập nhật sau khi cân/đong thực tế
  - Khi actual_quantity được xác nhận, hệ thống tạo InventoryTransactions(Usage -actual) theo example flow.

### C. ProductionBatches

- status: Planned → In Progress → Complete/Rejected
- Khi status = Complete → in nhãn Finished Product.

### D. QCTests

- result_status: Pending → Pass/Fail
- Có thể dùng verified_by để "duyệt" kết quả.
