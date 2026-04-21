# 04 - Product Backlog

**Hệ thống Quản lý Kho - Inventory Management System (IMS)**

Ngày cập nhật: 20/04/2026  
Phiên bản: 2.0 (đồng bộ 1-1 với PRD mới)

## 1. Nguyên tắc xây dựng backlog

- Mỗi epic/story đều truy vết được về FR/NFR/BR trong PRD.
- Ưu tiên triển khai theo luồng giá trị end-to-end: nhận vật tư -> QC -> cấp phát -> hoàn tất batch -> truy xuất.
- Acceptance Criteria tập trung vào kết quả kiểm chứng được.

## 2. Product backlog theo epic

### Epic E1 - Identity, Role, Audit (map FR-01, FR-10, NFR-01)

| ID   | User Story                                                                          | Priority | Acceptance Criteria                                                                 | Map PRD       |
| ---- | ----------------------------------------------------------------------------------- | -------- | ----------------------------------------------------------------------------------- | ------------- |
| E1.1 | Là Admin, tôi muốn quản lý vai trò người dùng để kiểm soát quyền thao tác           | High     | Admin/Manager/QC/Operator có ma trận quyền rõ ràng; API từ chối thao tác trái quyền | FR-01, NFR-01 |
| E1.2 | Là hệ thống, tôi muốn ghi nhận người thực hiện và thời điểm cho thao tác quan trọng | High     | Mọi thao tác tạo/sửa nghiệp vụ có performed_by và timestamp                         | FR-10         |
| E1.3 | Là Admin, tôi muốn xem lịch sử thay đổi để audit nội bộ                             | High     | Có thể truy vấn lịch sử theo entity, user, thời gian                                | FR-10         |

### Epic E2 - Master Data và Label Template (map FR-02, FR-08)

| ID   | User Story                                                              | Priority | Acceptance Criteria                                                  | Map PRD |
| ---- | ----------------------------------------------------------------------- | -------- | -------------------------------------------------------------------- | ------- |
| E2.1 | Là Admin, tôi muốn CRUD Materials để chuẩn hóa danh mục vật tư/sản phẩm | High     | CRUD đầy đủ; part_number unique; validate material_type              | FR-02   |
| E2.2 | Là Admin/Manager, tôi muốn tìm kiếm Materials nhanh theo mã/tên/loại    | High     | Hỗ trợ filter part_number, material_name, material_type              | FR-02   |
| E2.3 | Là Admin, tôi muốn CRUD LabelTemplates theo loại nhãn                   | High     | Hỗ trợ Raw Material/Sample/Finished Product/Status; preview template | FR-08   |

### Epic E3 - Receiving và vòng đời lot (map FR-03, FR-04, FR-07, BR-01..BR-04)

| ID   | User Story                                                            | Priority | Acceptance Criteria                                                                 | Map PRD      |
| ---- | --------------------------------------------------------------------- | -------- | ----------------------------------------------------------------------------------- | ------------ |
| E3.1 | Là Operator, tôi muốn tạo lot khi nhận hàng để ghi nhận tồn ban đầu   | High     | Tạo lot bắt buộc trường chính; status mặc định Quarantine; sinh Receipt transaction | FR-03, BR-01 |
| E3.2 | Là Operator, tôi muốn in nhãn Raw Material sau khi tạo lot            | High     | Nhãn sinh từ template và dữ liệu lot/material, có preview trước in                  | FR-08        |
| E3.3 | Là QC, tôi muốn nhập nhiều QCTest cho một lot                         | High     | Mỗi lot có thể có nhiều QCTest; trạng thái test Pending/Pass/Fail                   | FR-04        |
| E3.4 | Là hệ thống, tôi muốn tự đổi trạng thái lot theo kết quả QC           | High     | All pass -> Accepted; any fail -> Rejected                                          | FR-04, BR-03 |
| E3.5 | Là Operator/QC, tôi muốn tách sample lot từ lot cha                   | Medium   | Tạo lot con is_sample=true, parent_lot_id; ghi transaction tách mẫu                 | FR-07, BR-07 |
| E3.6 | Là Operator, tôi muốn thực hiện Adjustment/Transfer/Disposal có lý do | Medium   | Transaction lưu rõ loại, số lượng, lý do, người thực hiện                           | FR-03        |
| E3.7 | Là hệ thống, tôi muốn tự chuyển lot sang Depleted khi quantity về 0   | High     | quantity=0 thì trạng thái cập nhật Depleted theo rule                               | BR-04        |

### Epic E4 - Production batch và usage (map FR-05, FR-06, BR-02, BR-05, BR-06)

| ID   | User Story                                                         | Priority | Acceptance Criteria                                                                 | Map PRD             |
| ---- | ------------------------------------------------------------------ | -------- | ----------------------------------------------------------------------------------- | ------------------- |
| E4.1 | Là Manager/Operator, tôi muốn tạo Production Batch cho sản phẩm    | High     | Batch tạo mới ở Planned, có batch_number và thông tin bắt buộc                      | FR-05               |
| E4.2 | Là Operator, tôi muốn thêm lot nguyên liệu vào batch               | High     | Chỉ hiển thị lot hợp lệ; lưu planned_quantity                                       | FR-05, BR-02        |
| E4.3 | Là Operator, tôi muốn xác nhận actual usage để hệ thống tự trừ tồn | High     | Sinh Usage transaction âm; cập nhật tồn lot; chặn khi lot không hợp lệ/không đủ tồn | FR-06, BR-02, BR-06 |
| E4.4 | Là Manager, tôi muốn chuyển trạng thái batch đúng luồng            | High     | Planned -> In Progress -> Complete/Rejected; Complete khi đạt điều kiện usage       | FR-05, BR-05        |
| E4.5 | Là Operator, tôi muốn in nhãn Finished Product khi batch Complete  | High     | Nhãn lấy dữ liệu batch/material từ template Finished Product                        | FR-08               |

### Epic E5 - Traceability, báo cáo và vận hành (map FR-09, NFR-02..NFR-04)

| ID   | User Story                                                                   | Priority | Acceptance Criteria                                                   | Map PRD |
| ---- | ---------------------------------------------------------------------------- | -------- | --------------------------------------------------------------------- | ------- |
| E5.1 | Là Manager/QC/Admin, tôi muốn xem dashboard tồn kho theo trạng thái/hạn dùng | High     | Có filter status, material_type, near-expiry, location                | FR-09   |
| E5.2 | Là Manager/Admin, tôi muốn truy xuất lịch sử lot-batch end-to-end            | High     | Xem đầy đủ transaction theo lot, bao gồm reference tới batch/QC       | FR-09   |
| E5.3 | Là hệ thống, tôi cần phản hồi thao tác chính trong mục tiêu dưới 2 giây      | High     | Đạt SLO cho truy vấn lot, batch, transaction trên môi trường kiểm thử | NFR-02  |
| E5.4 | Là Admin, tôi muốn có backup định kỳ và giám sát health hệ thống             | Medium   | Có backup schedule, health check, cảnh báo cơ bản                     | NFR-03  |
| E5.5 | Là QA/Compliance, tôi muốn xuất báo cáo phục vụ kiểm tra nội bộ              | Medium   | Xuất báo cáo theo thời gian, trạng thái QC, lot rejected              | NFR-04  |

## 3. Release plan

### Release R1 (Core flow)

- E1.1, E1.2
- E2.1, E2.3
- E3.1, E3.3, E3.4
- E4.1, E4.2, E4.3

Mục tiêu: vận hành được luồng cốt lõi nhận lot -> QC -> usage cho batch.

### Release R2 (Operational completeness)

- E1.3
- E2.2
- E3.2, E3.5, E3.6, E3.7
- E4.4, E4.5
- E5.1, E5.2

Mục tiêu: hoàn thiện in nhãn, sample, traceability toàn diện.

### Release R3 (NFR and compliance)

- E5.3, E5.4, E5.5

Mục tiêu: đáp ứng hiệu năng, backup, báo cáo tuân thủ.

## 4. Definition of Done áp dụng cho backlog

- Story có acceptance criteria rõ và test được.
- API/logic đáp ứng business rule liên quan trong PRD.
- Có kiểm thử phù hợp (unit/integration hoặc test nghiệp vụ tương đương).
- Có log/audit cho thao tác quan trọng.
- Cập nhật tài liệu nếu thay đổi phạm vi hoặc rule.

## 5. Traceability matrix PRD -> Backlog

| PRD    | Story IDs        |
| ------ | ---------------- |
| FR-01  | E1.1             |
| FR-02  | E2.1, E2.2       |
| FR-03  | E3.1, E3.6       |
| FR-04  | E3.3, E3.4       |
| FR-05  | E4.1, E4.2, E4.4 |
| FR-06  | E4.3             |
| FR-07  | E3.5             |
| FR-08  | E2.3, E3.2, E4.5 |
| FR-09  | E5.1, E5.2       |
| FR-10  | E1.2, E1.3       |
| NFR-01 | E1.1             |
| NFR-02 | E5.3             |
| NFR-03 | E5.4             |
| NFR-04 | E5.5             |

Backlog sẽ được rà soát sau mỗi Sprint Review và cập nhật nếu có thay đổi ở PRD.
