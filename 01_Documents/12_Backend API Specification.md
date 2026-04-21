# 12 - Backend API Specification

**System:** Inventory Management System (IMS)  
**Version:** 2.0  
**Date:** 20/04/2026  
**Purpose:** API contract đồng bộ 1-1 với PRD v2, Domain Model v2, Product Backlog v2, Architecture v2.

## 1. Mục tiêu

Tài liệu đặc tả giao tiếp backend cho:

- REST API (luồng chính).
- GraphQL API (truy vấn linh hoạt cho dashboard/report).
- gRPC API (tích hợp service-nội bộ cho sự kiện và truy xuất nhanh).

## 2. Quy ước chung

## 2.1 Base URL

- REST: /api/v1
- GraphQL: /graphql
- gRPC package: ims.v1

## 2.2 Content type

- REST request/response: application/json
- GraphQL: application/json
- gRPC: protobuf

## 2.3 Date và number format

- Date: YYYY-MM-DD
- DateTime: ISO-8601 UTC
- Decimal quantity: tối đa 3 chữ số thập phân

## 2.4 Response envelope

### Success

```json
{
  "success": true,
  "data": {},
  "meta": {
    "requestId": "req-123",
    "timestamp": "2026-04-20T10:00:00Z"
  }
}
```

### Error

```json
{
  "success": false,
  "error": {
    "code": "BUSINESS_RULE_VIOLATION",
    "message": "Lot must be Accepted before usage",
    "details": {}
  },
  "meta": {
    "requestId": "req-123",
    "timestamp": "2026-04-20T10:00:00Z"
  }
}
```

## 2.5 HTTP status codes

- 200 OK
- 201 Created
- 204 No Content
- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict
- 422 Unprocessable Entity
- 500 Internal Server Error

## 3. Authentication va Authorization

## 3.1 IAM

- Su dung Keycloak.
- Backend nhan Bearer token va validate JWT.

## 3.2 Roles

- Admin
- Manager
- QC
- Operator

## 3.3 Rule truy cap

- Operator: thao tac van hanh lot, usage, in nhan.
- QC: nhap va duyet QC.
- Manager: quan ly batch, report, giam sat.
- Admin: toan quyen cau hinh va quan tri.

## 4. Business rules ap dung toan API

- BR-01: Lot moi phai o Quarantine.
- BR-02: Chi lot Accepted moi duoc usage.
- BR-03: Any QC fail => lot Rejected.
- BR-04: Quantity lot thay doi qua transaction.
- BR-05: Batch Complete khi du dieu kien usage.
- BR-06: Usage phai ghi transaction va cap nhat ton.
- BR-07: Sample lot phai co parent_lot_id va is_sample=true.

## 5. REST API

## 5.1 Materials

### GET /api/v1/materials

- Mô tả: danh sách materials.
- Query: keyword, materialType, page, size, sort.
- Roles: Admin, Manager, QC, Operator.

### POST /api/v1/materials

- Mô tả: tạo material.
- Roles: Admin.
- Validation:
  - partNumber required, unique
  - materialName required
  - materialType required

Request body:

```json
{
  "partNumber": "MAT-001",
  "materialName": "Vitamin D3 100K",
  "materialType": "API",
  "storageConditions": "Store 2-8C",
  "specificationDocument": "https://docs/specs/MAT-001.pdf"
}
```

### GET /api/v1/materials/{materialId}

- Mô tả: xem chi tiết material.
- Roles: Admin, Manager, QC, Operator.

### PUT /api/v1/materials/{materialId}

- Mô tả: cập nhật material.
- Roles: Admin.

### DELETE /api/v1/materials/{materialId}

- Mô tả: xóa material (soft delete nếu đã được tham chiếu).
- Roles: Admin.

## 5.2 Inventory lots

### POST /api/v1/lots/receive

- Mô tả: nhập kho lot mới.
- Roles: Operator, Manager.
- Rule: tạo lot status Quarantine + tạo Receipt transaction.

Request body:

```json
{
  "materialId": "mat-uuid-001",
  "lotCode": "LOT-2026-0001",
  "quantity": 25.5,
  "unitOfMeasure": "kg",
  "receivedDate": "2026-04-20",
  "expirationDate": "2027-04-20",
  "storageLocation": "RM-A-01"
}
```

### GET /api/v1/lots

- Mô tả: danh sách lot.
- Query: materialId, status, nearExpiryDays, page, size.
- Roles: Admin, Manager, QC, Operator.

### GET /api/v1/lots/{lotId}

- Mô tả: chi tiết lot.
- Roles: Admin, Manager, QC, Operator.

### PATCH /api/v1/lots/{lotId}/status

- Mô tả: cập nhật status lot theo quyền và context.
- Roles: QC, Manager.
- Rule:
  - Chỉ phép Accepted/Rejected trong QC flow.
  - Depleted xuất hiện theo quantity=0 hoặc rule hệ thống.

## 5.3 QC tests

### POST /api/v1/qctests

- Mô tả: tạo bản ghi QC test cho lot.
- Roles: QC.

Request body:

```json
{
  "lotId": "lot-uuid-001",
  "testType": "Identity",
  "testMethod": "USP <123>",
  "testDate": "2026-04-20",
  "resultStatus": "Pass",
  "performedBy": "qc.user",
  "verifiedBy": "qc.supervisor"
}
```

### GET /api/v1/qctests

- Mô tả: truy vấn test theo lot.
- Query: lotId, resultStatus.
- Roles: QC, Manager, Admin.

### POST /api/v1/lots/{lotId}/qc-evaluate

- Mô tả: tổng hợp kết quả test và cập nhật lot status.
- Roles: QC, Manager.
- Rule: all pass => Accepted; any fail => Rejected.

## 5.4 Sample lot

### POST /api/v1/lots/{lotId}/sample

- Mô tả: tách sample lot từ lot cha.
- Roles: QC, Operator, Manager.
- Rule: tạo lot con isSample=true, parentLotId, và transaction tách mẫu.

Request body:

```json
{
  "sampleQuantity": 0.2,
  "unitOfMeasure": "kg",
  "reason": "QC Retain Sample"
}
```

## 5.5 Inventory transactions

### GET /api/v1/transactions

- Mô tả: danh sách transaction.
- Query: lotId, type, fromDate, toDate, page, size.
- Roles: Admin, Manager, QC, Operator.

### POST /api/v1/transactions/adjustment

- Mô tả: điều chỉnh tồn.
- Roles: Operator, Manager.

### POST /api/v1/transactions/transfer

- Mô tả: chuyển vị trí lưu trữ.
- Roles: Operator, Manager.

### POST /api/v1/transactions/disposal

- Mô tả: tiêu hủy lot theo quy định.
- Roles: Manager.

## 5.6 Production batches

### POST /api/v1/batches

- Mô tả: tạo production batch.
- Roles: Manager, Operator.
- Rule: status khởi tạo Planned.

### GET /api/v1/batches

- Mô tả: danh sách batch.
- Query: productId, status, page, size.
- Roles: Admin, Manager, Operator.

### GET /api/v1/batches/{batchId}

- Mô tả: chi tiết batch.
- Roles: Admin, Manager, Operator.

### PATCH /api/v1/batches/{batchId}/status

- Mô tả: đổi status batch.
- Roles: Manager, Operator.
- Rule: Planned -> In Progress -> Complete/Rejected.

## 5.7 Batch components và usage

### POST /api/v1/batches/{batchId}/components

- Mô tả: thêm lot vào batch.
- Roles: Operator, Manager.
- Rule: lot phải hợp lệ cho sản xuất.

Request body:

```json
{
  "lotId": "lot-uuid-001",
  "plannedQuantity": 2.0,
  "unitOfMeasure": "kg"
}
```

### PATCH /api/v1/batch-components/{componentId}/confirm-usage

- Mô tả: xác nhận actual usage.
- Roles: Operator, Manager.
- Rule:
  - lot phải Accepted.
  - đủ tồn.
  - tạo Usage transaction âm.
  - cập nhật InventoryLot.quantity.

Request body:

```json
{
  "actualQuantity": 2.0,
  "performedBy": "operator.user"
}
```

## 5.8 Labels

### GET /api/v1/label-templates

- Mô tả: danh sách templates.
- Roles: Admin, Manager, QC, Operator.

### POST /api/v1/label-templates

- Mô tả: tạo template.
- Roles: Admin.

### POST /api/v1/labels/preview

- Mô tả: tạo preview nhãn theo template và data lot/batch.
- Roles: Admin, Manager, QC, Operator.

Request body:

```json
{
  "labelType": "Raw Material",
  "resourceType": "LOT",
  "resourceId": "lot-uuid-001"
}
```

### POST /api/v1/labels/print

- Mô tả: trigger in nhãn.
- Roles: Admin, Manager, QC, Operator.

## 5.9 Reports và traceability

### GET /api/v1/reports/inventory-summary

- Mô tả: tổng hợp tồn kho theo status/material/location.
- Roles: Admin, Manager, QC.

### GET /api/v1/reports/traceability

- Mô tả: truy xuất lot -> transaction -> batch -> QC.
- Query: lotId hoặc batchId.
- Roles: Admin, Manager, QC.

## 6. GraphQL API

## 6.1 Endpoint

- POST /graphql

## 6.2 Schema rút gọn

```graphql
type Query {
  materials(
    keyword: String
    materialType: String
    page: Int
    size: Int
  ): MaterialPage
  lots(status: String, materialId: ID, page: Int, size: Int): LotPage
  batch(batchId: ID!): ProductionBatch
  traceability(lotId: ID, batchId: ID): TraceabilityResult
}

type Mutation {
  receiveLot(input: ReceiveLotInput!): InventoryLot
  createQCTest(input: CreateQCTestInput!): QCTest
  evaluateLotQC(lotId: ID!): InventoryLot
  createBatch(input: CreateBatchInput!): ProductionBatch
  confirmUsage(componentId: ID!, actualQuantity: Float!): BatchComponent
}
```

## 6.3 Su dung

- GraphQL được ưu tiên cho dashboard và trang truy vấn tổng hợp.
- Quyền truy cập vẫn dựa trên token và role từ Keycloak.

## 7. gRPC API

## 7.1 Mục tiêu

- Hỗ trợ tích hợp nội bộ giữa các backend service.
- Đồng bộ sự kiện usage, status change và traceability nhanh.

## 7.2 Proto rut gon

```proto
syntax = "proto3";

package ims.v1;

service InventoryService {
  rpc ReceiveLot(ReceiveLotRequest) returns (ReceiveLotResponse);
  rpc ConfirmUsage(ConfirmUsageRequest) returns (ConfirmUsageResponse);
  rpc GetLotTraceability(GetLotTraceabilityRequest) returns (GetLotTraceabilityResponse);
}

message ReceiveLotRequest {
  string material_id = 1;
  string lot_code = 2;
  double quantity = 3;
  string uom = 4;
}

message ReceiveLotResponse {
  string lot_id = 1;
  string status = 2;
}

message ConfirmUsageRequest {
  string batch_component_id = 1;
  double actual_quantity = 2;
}

message ConfirmUsageResponse {
  string component_id = 1;
  double remaining_lot_quantity = 2;
}

message GetLotTraceabilityRequest {
  string lot_id = 1;
}

message GetLotTraceabilityResponse {
  string lot_id = 1;
  repeated string transaction_ids = 2;
  repeated string batch_ids = 3;
}
```

## 8. Validation rules

- quantity > 0 cho receive lot va planned/actual usage.
- expiration_date >= received_date.
- batch status transition hợp lệ theo state machine.
- transaction_type phải nằm trong tập giá trị cho phép.
- không cho usage lot Rejected/Depleted/không Accepted.

## 9. Error codes (business level)

| Code                     | Meaning                        |
| ------------------------ | ------------------------------ |
| VALIDATION_ERROR         | Dữ liệu đầu vào không hợp lệ   |
| DUPLICATE_PART_NUMBER    | partNumber đã tồn tại          |
| LOT_NOT_FOUND            | lotId không tồn tại            |
| LOT_NOT_ACCEPTED         | lot chua Accepted de usage     |
| INSUFFICIENT_QUANTITY    | lot không đủ số lượng          |
| INVALID_BATCH_TRANSITION | status transition không hợp lệ |
| PERMISSION_DENIED        | role không đủ quyền            |
| CONCURRENCY_CONFLICT     | dữ liệu thay đổi đồng thời     |

## 10. Idempotency và concurrency

- Endpoint usage nhạy cảm hỗ trợ idempotency-key (để tránh trừ tồn lặp).
- Các thao tác cập nhật số lượng sử dụng DB transaction.
- Khuyến nghị optimistic locking trên inventory_lots.

## 11. Audit logging

Mỗi API thay đổi dữ liệu quan trọng phải ghi:

- actor (username/userId)
- action
- entity type/id
- before/after snapshot tối thiểu cho field quan trọng
- timestamp
- requestId

## 12. API test checklist

- Auth: missing token, expired token, wrong role.
- Validation: required fields, format, range.
- Business rule: BR-01..BR-07.
- Data integrity: quantity consistency, traceability chain.
- Performance: endpoint p95 theo NFR-02.

## 13. Traceability 1-1 voi PRD

| PRD item | API contract minh chung               |
| -------- | ------------------------------------- |
| FR-01    | Auth/RBAC section + role restrictions |
| FR-02    | Materials endpoints                   |
| FR-03    | POST /lots/receive + transactions     |
| FR-04    | qctests + qc-evaluate endpoint        |
| FR-05    | batches endpoints                     |
| FR-06    | confirm-usage endpoint                |
| FR-07    | /lots/{lotId}/sample                  |
| FR-08    | label templates + preview/print       |
| FR-09    | report/traceability endpoints         |
| FR-10    | audit logging section                 |
| NFR-01   | Keycloak + role policy                |
| NFR-02   | performance checklist                 |
| NFR-03   | transaction/concurrency strategy      |
| NFR-04   | traceability and report APIs          |

## 14. Open points

- Chot danh sach endpoint public cua phase 1 truoc release.
- Chot format file xuat bao cao (csv/xlsx/pdf).
- Chot chinh sach luu tru log audit va retention.
