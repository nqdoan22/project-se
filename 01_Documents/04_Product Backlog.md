# 04 – Product Backlog

**Hệ thống:** Inventory Management System (IMS)
**Ngày lập:** 01/03/2026
**Phiên bản:** 1.1 (cập nhật 12/03/2026 — đồng bộ theo `dbscript.sql`)

## 1. Epic 1 – Quản lý Master Data & Danh mục

| #    | User Story / Feature                                                                       | Ưu tiên | Acceptance Criteria (chính)                                                                                                                                                                                                                |
| ---- | ------------------------------------------------------------------------------------------ | ------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| E1.1 | Là Admin, tôi muốn tạo/sửa/xóa danh mục Nguyên vật liệu (Materials) để quản lý master data | High    | - CRUD đầy đủ cho Materials<br>- `part_number` unique<br>- `material_type` enum: `API`, `Excipient`, `Dietary Supplement`, `Container`, `Closure`, `Process Chemical`, `Testing Material`<br>- Lưu `specification_document` (mã hoặc link) |
| E1.2 | Là Admin/Manager, tôi muốn xem danh sách Materials với bộ lọc & tìm kiếm nhanh             | High    | - Tìm theo `part_number`, `material_name`, `material_type`<br>- Hiển thị `storage_conditions` và `specification_document`                                                                                                                  |
| E1.3 | Là Admin, tôi muốn quản lý Label Templates (tạo, sửa, preview)                             | High    | - CRUD LabelTemplates<br>- `label_type` enum: `Raw Material`, `Sample`, `Intermediate`, `Finished Product`, `API`, `Status`<br>- `template_content` (HTML với `{{placeholder}}`), `width`, `height`                                        |

## 2. Epic 2 – Nhập kho & Quản lý Lot (Receiving & Lot Lifecycle)

| #    | User Story / Feature                                                               | Ưu tiên | Acceptance Criteria (chính)                                                                                                                                                                                                                                                                                                                |
| ---- | ---------------------------------------------------------------------------------- | ------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| E2.1 | Là Operator, tôi muốn tạo mới Inventory Lot khi nhận hàng (Receive Material)       | High    | - Tạo lot mới → `status = Quarantine`<br>- Ghi InventoryTransaction `Receipt +qty`<br>- Bắt buộc: `material_id`, `manufacturer_name`, `manufacturer_lot`, `quantity`, `unit_of_measure`, `received_date`, `expiration_date`<br>- Tuỳ chọn: `supplier_name`, `storage_location`, `po_number`, `receiving_form_id`, `in_use_expiration_date` |
| E2.2 | Hệ thống tự động generate & cho phép in nhãn Raw Material ngay sau khi tạo lot     | High    | - Sử dụng LabelTemplate loại Raw Material<br>- Populate: lot_id, material_name, expiration, manufacturer_lot, storage, QR/barcode                                                                                                                                                                                                          |
| E2.3 | Là QC, tôi muốn tạo nhiều QCTest cho một lot và ghi kết quả (Identity, Potency, …) | High    | - Tạo nhiều record QCTests cho 1 lot_id<br>- result_status: Pending → Pass/Fail<br>- Có performed_by + verified_by                                                                                                                                                                                                                         |
| E2.4 | Hệ thống tự động cập nhật lot.status khi hoàn tất QC                               | High    | - All tests Pass → Quarantine → Accepted<br>- Có Fail → Quarantine → Rejected<br>- Ghi InventoryTransaction (Status Change) nếu cần                                                                                                                                                                                                        |
| E2.5 | Là Operator/QC, tôi muốn tạo Sample lot từ lot gốc (tách mẫu)                      | Medium  | - Tạo lot mới: is_sample=true, parent_lot_id<br>- Ghi transaction Split (-qty cha, +qty con)<br>- In nhãn Sample (dùng template loại Sample)                                                                                                                                                                                               |
| E2.6 | Là Operator, tôi muốn thực hiện các giao dịch khác: Adjustment, Transfer, Disposal | Medium  | - Adjustment: +/– qty + lý do<br>- Disposal: chỉ cho lot Rejected/Depleted<br>- Transfer: thay đổi storage_location                                                                                                                                                                                                                        |
| E2.7 | Hệ thống tự động chuyển lot → Depleted khi quantity ≤ 0                            | High    | - quantity = 0 → status = Depleted<br>- Không cho phép Usage/Production nếu Depleted                                                                                                                                                                                                                                                       |

## 3. Epic 3 – Quản lý Đợt sản xuất (Production Batch)

| #    | User Story / Feature                                                                               | Ưu tiên | Acceptance Criteria (chính)                                                                                                                                                             |
| ---- | -------------------------------------------------------------------------------------------------- | ------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| E3.1 | Là Manager/Operator, tôi muốn tạo mới Production Batch cho một sản phẩm                            | High    | - Chọn product_id (Materials)<br>- status khởi tạo = Planned<br>- Ghi batch_number (auto hoặc manual), batch_size, UOM, planned dates                                                   |
| E3.2 | Là Operator, tôi muốn thêm nguyên liệu (lot) vào batch (Batch Components)                          | High    | - Thêm planned_quantity cho từng lot<br>- Hiển thị tồn kho khả dụng (chỉ Accepted, not Depleted, not Expired)                                                                           |
| E3.3 | Là Operator, tôi muốn xác nhận actual usage → trừ tồn kho tự động                                  | High    | - Cập nhật actual_quantity<br>- Tự động tạo InventoryTransaction Usage –actual_qty<br>- Cập nhật InventoryLots.quantity<br>- Không cho phép nếu lot không đủ tồn hoặc status ≠ Accepted |
| E3.4 | Là Operator/Manager, tôi muốn chuyển trạng thái batch: Planned → In Progress → Complete / Rejected | High    | - Chỉ Complete khi tất cả components đã usage đủ<br>- Khi Complete → tự động gợi ý in nhãn Finished Product                                                                             |
| E3.5 | Hệ thống tự động generate & in nhãn Finished Product khi batch Complete                            | High    | - Dùng LabelTemplate loại Finished Product<br>- Populate: batch_number, product_name, manufacture_date, expiration_date, batch_size, QR                                                 |

## 4. Epic 4 – In nhãn & Quản lý trạng thái (Labeling & Status Change)

| #    | User Story / Feature                                                        | Ưu tiên | Acceptance Criteria (chính)                                                                     |
| ---- | --------------------------------------------------------------------------- | ------- | ----------------------------------------------------------------------------------------------- |
| E4.1 | Hệ thống hỗ trợ in nhãn Status khi lot/batch thay đổi trạng thái quan trọng | Medium  | - Quarantine → Accepted → in Status label “Accepted”<br>- Rejected → in “Rejected – Do Not Use” |
| E4.2 | Operator có thể in lại nhãn bất kỳ lúc nào cho lot/batch hiện có            | Medium  | - Chọn lot/batch → chọn label_type → preview → print                                            |

## 5. Epic 5 – Báo cáo, Audit Trail & Giám sát

| #    | User Story / Feature                                                                      | Ưu tiên | Acceptance Criteria (chính)                                                                                                                                                                                             |
| ---- | ----------------------------------------------------------------------------------------- | ------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| E5.1 | Là Manager/QC/Admin, tôi muốn xem tồn kho real-time (theo material/lot/status/expiration) | High    | - Dashboard tồn kho<br>- Filter: status, material_type, near expiry (<30 ngày), location                                                                                                                                |
| E5.2 | Là Manager/Admin, tôi muốn xem lịch sử giao dịch của bất kỳ lot nào (full traceability)   | High    | - Trace từ lot → tất cả InventoryTransactions<br>- Bao gồm reference (batch, order, QC test…)                                                                                                                           |
| E5.3 | Hệ thống ghi audit log mọi thao tác thay đổi dữ liệu quan trọng                           | High    | - Dựa vào `InventoryTransactions` (lịch sử giao dịch lot), `QCTests` (lịch sử QC) và các field `created_date`/`modified_date` trên mọi bảng<br>- Không cho phép xóa/sửa trực tiếp history trong `InventoryTransactions` |
| E5.4 | Là Admin, tôi muốn xem báo cáo tuân thủ (QC pass rate, rejected lots, usage theo batch…)  | Medium  | - Báo cáo xuất Excel/PDF<br>- Thời gian < 30 giây                                                                                                                                                                       |

## 6. Epic 6 – Bảo mật, Hiệu năng & Vận hành (NFR liên quan)

| #    | User Story / Feature                                        | Ưu tiên | Acceptance Criteria (chính)                                                                          |
| ---- | ----------------------------------------------------------- | ------- | ---------------------------------------------------------------------------------------------------- |
| E6.1 | Hệ thống phải phân quyền theo vai trò (RBAC)                | High    | - Admin, Manager, QC, Operator có quyền khác nhau<br>- Không cho Operator duyệt QC, xóa transaction… |
| E6.2 | Hệ thống chặn tự động các lot không dùng được               | High    | - Không cho Usage/Production nếu: status ≠ Accepted, quantity < required, expired                    |
| E6.3 | API response time < 2 giây cho các thao tác chính           | High    | - Load lot list, transaction history, batch detail                                                   |
| E6.4 | Hỗ trợ ít nhất 100 concurrent users & 10.000 giao dịch/ngày | High    | - Stress test đạt yêu cầu                                                                            |
| E6.5 | Tự động backup hàng ngày & có monitoring/alert              | Medium  | - Health check endpoint<br>- Alert khi DB latency cao, disk full…                                    |

## 7. Epic 7 – Các tính năng bổ trợ (Nice-to-have / Phase 2)

- Tích hợp barcode/QR scanner khi nhận hàng & usage
- Gửi email/Slack notification khi lot near expiry hoặc QC Fail
- Export dữ liệu tuân thủ theo định dạng XML/CSV cho cơ quan quản lý
- Dashboard KPI: % lot pass QC, inventory turnover, rejected quantity ratio

**Tổng số story hiện tại (ước tính):** ~35–40 story chính  
**Ưu tiên triển khai Phase 1:** Epic 1 → 2 → 3 → 4 → 5 (core warehouse + production flow + traceability)

Cập nhật backlog sẽ được thực hiện sau mỗi Sprint Review & Refinement.
