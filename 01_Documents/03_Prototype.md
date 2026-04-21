# 03 - Prototype

## 1. Mục tiêu

Tài liệu này trình bày prototype giao diện theo từng bước cho luồng nghiệp vụ chính:

- Nhận vật tư vào kho theo lot.
- QC kiểm nghiệm và duyệt trạng thái lot.
- Tạo production batch và cấp phát nguyên liệu.
- Hoàn tất batch và in nhãn thành phẩm.

Prototype đồng bộ 1-1 với PRD, Domain Model, Product Backlog, Architecture.

## 2. Luồng nghiệp vụ được prototype

### 2.1 Luong chinh

```mermaid
flowchart LR
  A[Dang nhap] --> B[Tao Material]
  B --> C[Receive Lot]
  C --> D[In nhan Raw Material]
  D --> E[Nhập kết quả QC]
  E --> F{Kết quả QC}
  F -->|All Pass| G[Lot Accepted]
  F -->|Any Fail| H[Lot Rejected]
  G --> I[Tao Production Batch]
  I --> J[Them lot vao Batch Components]
  J --> K[Xac nhan actual usage]
  K --> L[Batch Complete]
  L --> M[In nhan Finished Product]
```

### 2.2 Luong phu

- Tach sample lot tu lot cha va in nhan Sample.
- Adjustment/Transfer/Disposal khi co ngoai le van hanh.

## 3. Danh sach man hinh prototype

| Screen ID | Tên màn hình                    | Mục tiêu                                       | Map PRD             |
| --------- | ------------------------------- | ---------------------------------------------- | ------------------- |
| P-01      | Login                           | Xác thực người dùng, khởi tạo context role     | FR-01               |
| P-02      | Material List + Create/Edit     | Quản lý master data Materials                  | FR-02               |
| P-03      | Receive Lot                     | Tạo lot mới ở Quarantine + Receipt transaction | FR-03, BR-01        |
| P-04      | Raw Label Preview/Print         | In nhãn lot sau khi nhập kho                   | FR-08               |
| P-05      | QC Test Entry                   | Nhập QCTest theo lot                           | FR-04               |
| P-06      | QC Approval Result              | Đổi trạng thái lot theo kết quả QC             | FR-04, BR-03        |
| P-07      | Sample Lot Creation             | Tách lot mẫu từ lot cha                        | FR-07, BR-07        |
| P-08      | Production Batch Create         | Tạo batch cho product                          | FR-05               |
| P-09      | Batch Components + Usage        | Chọn lot hợp lệ, xác nhận actual usage         | FR-06, BR-02, BR-06 |
| P-10      | Batch Complete + Finished Label | Chuyển Complete và in nhãn thành phẩm          | FR-05, FR-08, BR-05 |
| P-11      | Traceability Report             | Truy xuất lot -> transaction -> batch          | FR-09, FR-10        |

## 4. Chi tiet prototype theo tung man hinh

## 4.1 P-01 Login

### User role

- Admin
- Manager
- QC
- Operator

### UI components

- Username
- Password
- Login button

### Validation va behavior

- Bat buoc username va password.
- Sai thong tin hien thi loi xac thuc.
- Dang nhap thanh cong dieu huong dashboard theo role.

## 4.2 P-02 Material List + Create/Edit

### UI components

- Bang danh sach materials (part_number, material_name, material_type).
- Filter: keyword, material_type.
- Nut Create, Edit.

### Form fields

- part_number (required, unique)
- material_name (required)
- material_type (required)
- storage_conditions
- specification_document

### Acceptance points

- Luu thanh cong tao record moi.
- Trung part_number thi chan va thong bao conflict.

## 4.3 P-03 Receive Lot

### UI components

- Form tiep nhan lot.
- Panel xem nhanh thong tin material.

### Form fields

- material_id (required)
- lot_code (required)
- quantity (required, > 0)
- unit_of_measure (required)
- received_date (required)
- expiration_date (required)
- storage_location

### System behavior

- Tao InventoryLot voi status = Quarantine.
- Tu dong tao InventoryTransaction type Receipt (+quantity).
- Hien thong bao tao lot thanh cong.

## 4.4 P-04 Raw Label Preview/Print

### UI components

- Label template selector (Raw Material).
- Label preview panel.
- Print button.

### Data binding

- lot_id, material_name, manufacturer_lot, expiration_date, storage_location.

### System behavior

- Không bắt buộc tạo record mới trong DB nếu chưa có bảng log in nhãn.

## 4.5 P-05 QC Test Entry

### UI components

- Chon lot can test.
- Form nhập kết quả QC.
- Danh sach test da nhap.

### Form fields

- lot_id (required)
- test_type (required)
- test_method (required)
- test_date (required)
- result_value
- acceptance_criteria
- result_status (Pending/Pass/Fail)
- performed_by (required)
- verified_by

### Existing mockup

- Hinh prototype co san:

![trang QC](./images/qc.png)

## 4.6 P-06 QC Approval Result

### UI components

- Bang tong hop test theo lot.
- Nut Approve/Reject lot (neu role cho phep).

### Rule hien thi

- Tat ca test Pass -> lot de xuat Accepted.
- Co it nhat 1 test Fail -> lot de xuat Rejected.

### System behavior

- Cập nhật InventoryLot.status theo kết quả tổng hợp.

## 4.7 P-07 Sample Lot Creation

### UI components

- Chon lot cha.
- Form tach sample quantity.
- Preview thong tin lot con.

### Behavior

- Tao lot con is_sample=true, parent_lot_id = lot cha.
- Ghi giao dịch tách mẫu (Split/Receipt theo quy tắc hệ thống).
- Ho tro in nhan Sample.

## 4.8 P-08 Production Batch Create

### UI components

- Form tao batch.

### Form fields

- product_id (required)
- batch_number (required)
- batch_size (required)
- unit_of_measure (required)
- manufacture_date
- expiration_date

### Behavior

- Tao batch moi o status Planned.

## 4.9 P-09 Batch Components + Usage

### UI components

- Chon lot cho batch.
- Nhap planned_quantity va actual_quantity.
- Bang components da them.

### Rule

- Chi cho chon lot Accepted, con ton, chua het han.
- Confirm usage tao transaction Usage am.

### Data update

- Trừ InventoryLots.quantity theo actual usage.
- Nếu quantity về 0 thì có thể chuyển Depleted.

## 4.10 P-10 Batch Complete + Finished Label

### UI components

- Nut chuyen status batch.
- Preview nhan Finished Product.

### Behavior

- Chỉ cho Complete khi đạt điều kiện usage.
- In nhãn Finished Product sau khi Complete.

## 4.11 P-11 Traceability Report

### UI components

- Filter theo lot, material, date range, status.
- Bảng transaction timeline.
- Tổng hợp KPI cơ bản.

### Existing mockup

- Hinh prototype report co san:

![trang report](./images/report%20prototype.png)

## 5. Prototype states va error states

- Empty state: chưa có dữ liệu lot/batch.
- Loading state: đang tải danh sách lot/material.
- Validation error state: thiếu trường bắt buộc, sai định dạng.
- Business rule error state:
  - Không được usage lot không Accepted.
  - Không được usage lot không đủ tồn.
  - Không được Complete batch nếu chưa đủ điều kiện.
- Permission denied state: role không đủ quyền thao tác.

## 6. Traceability 1-1 voi PRD

| PRD   | Prototype Screen                               |
| ----- | ---------------------------------------------- |
| FR-01 | P-01                                           |
| FR-02 | P-02                                           |
| FR-03 | P-03                                           |
| FR-04 | P-05, P-06                                     |
| FR-05 | P-08, P-10                                     |
| FR-06 | P-09                                           |
| FR-07 | P-07                                           |
| FR-08 | P-04, P-10                                     |
| FR-09 | P-11                                           |
| FR-10 | P-11 + audit hints trên các màn hình giao dịch |

## 7. Kế hoạch nâng cấp prototype

- Chuyển từ low-fidelity sang clickable prototype theo role.
- Bổ sung luồng Rejected lot và luồng Disposal chi tiết.
- Bổ sung màn hình role matrix cho Admin.
- Bổ sung dashboard KPI theo NFR-02/NFR-04.
