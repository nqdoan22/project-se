# 09 - System Evaluation and Validation

## 1. Mục tiêu tài liệu

Tài liệu này mô tả:

- Cách đăng ký/cài đặt công cụ để kiểm thử hệ thống.
- Cách thực thi kiểm thử theo kế hoạch.
- Kết quả thu được và cách đánh giá đạt/chưa đạt.
- Kết quả khảo sát người dùng thử nghiệm.
- So sánh hệ thống với một số hệ thống tương tự.

Tài liệu đồng bộ 1-1 với PRD, Product Backlog và Architecture.

## 2. Phạm vi đánh giá

### 2.1 Chức năng

- FR-01 đến FR-10 trong PRD.

### 2.2 Phi chức năng

- NFR-01: RBAC và bảo mật truy cập.
- NFR-02: hiệu năng thao tác chính.
- NFR-03: độ tin cậy và toàn vẹn dữ liệu.
- NFR-04: truy xuất và báo cáo phục vụ tuân thủ.

## 3. Môi trường kiểm thử

### 3.1 Môi trường

- Frontend: React + Vite.
- Backend: Spring Boot.
- Database: MySQL.
- IAM: Keycloak.
- Orchestration local: docker-compose.

### 3.2 Dữ liệu kiểm thử

- Bộ dữ liệu mẫu gồm:
  - 30 materials.
  - 300 lots (phân bố theo Quarantine/Accepted/Rejected/Depleted).
  - 1200 inventory transactions.
  - 400 QC tests.
  - 80 production batches.

## 4. Công cụ kiểm thử và cách cài đặt

### 4.1 Công cụ cho API và integration

- Postman: test API chức năng.
- Newman: chạy bộ test Postman trên CLI/CI.

Cách cài đặt:

1. Cài Postman desktop.
2. Cài Node.js.
3. Cài Newman:

```bash
npm install -g newman
```

4. Import collection và environment vào Postman.

### 4.2 Công cụ load/performance

- k6 để test hiệu năng endpoint chính.

Cách cài đặt:

1. Tải k6 theo hệ điều hành.
2. Chạy script load test:

```bash
k6 run tests/perf/core-flow.js
```

### 4.3 Công cụ bảo mật và quality gate

- OWASP ZAP (baseline scan) cho API.
- SonarQube (nếu có) để theo dõi code quality.

## 5. Chiến lược kiểm thử

### 5.1 Mức kiểm thử

- Unit test: service/domain rules.
- Integration test: API + DB + security.
- End-to-end test: luồng nghiệp vụ chính.
- Performance test: endpoint có tải cao.
- Security test: token, role, endpoint protection.

### 5.2 Tiêu chí pass/fail

- Tất cả testcase P1 bắt buộc pass.
- P2/P3 cho phép tồn đọng có kế hoạch sửa.
- Không có lỗi blocker ở luồng nghiệp vụ chính.

## 6. Danh sách testcase theo PRD

| TC ID | Mô tả testcase                      | Map PRD      | Kết quả mong đợi                           |
| ----- | ----------------------------------- | ------------ | ------------------------------------------ |
| TC-01 | Tạo material với part_number hợp lệ | FR-02        | Tạo thành công                             |
| TC-02 | Tạo material trùng part_number      | FR-02        | Trả conflict                               |
| TC-03 | Receive lot hợp lệ                  | FR-03, BR-01 | Lot tạo ở Quarantine + receipt transaction |
| TC-04 | Nhập QCTest và all pass             | FR-04, BR-03 | Lot Accepted                               |
| TC-05 | Nhập QCTest có fail                 | FR-04, BR-03 | Lot Rejected                               |
| TC-06 | Tạo sample lot                      | FR-07, BR-07 | Tạo lot con + giao dịch tách mẫu           |
| TC-07 | Tạo production batch                | FR-05        | Batch Planned                              |
| TC-08 | Confirm usage lot Accepted đủ tồn   | FR-06, BR-02 | Tạo Usage transaction, trừ tồn             |
| TC-09 | Confirm usage lot không Accepted    | FR-06, BR-02 | Bị chặn thao tác                           |
| TC-10 | Complete batch khi đủ điều kiện     | FR-05, BR-05 | Batch Complete                             |
| TC-11 | In nhãn Raw Material                | FR-08        | Preview/print thành công                   |
| TC-12 | Truy xuất lịch sử lot-batch         | FR-09, FR-10 | Hiện đầy đủ timeline                       |
| TC-13 | Role không hợp lệ gọi API nhạy cảm  | NFR-01       | 403 Forbidden                              |
| TC-14 | Endpoint chính dưới ngưỡng SLO      | NFR-02       | p95 trong ngưỡng mục tiêu                  |

## 7. Kết quả kiểm thử tổng hợp

### 7.1 Thống kê test

| Nhóm test      | Số testcase | Pass | Fail | Blocked |
| -------------- | ----------: | ---: | ---: | ------: |
| Functional API |          48 |   45 |    3 |       0 |
| Integration    |          22 |   20 |    2 |       0 |
| E2E core flow  |          10 |    9 |    1 |       0 |
| Security       |          12 |   11 |    1 |       0 |
| Performance    |           6 |    5 |    1 |       0 |
| Tổng           |          98 |   90 |    8 |       0 |

### 7.2 Lỗi tồn đọng chính

- ISS-01: Usage khi concurrent request có thể gây race condition tồn kho.
- ISS-02: Một số endpoint report chưa tối ưu query ở dữ liệu lớn.
- ISS-03: Message lỗi validation chưa đồng nhất ở module QC.

### 7.3 Kế hoạch xử lý

- Áp dụng lock/transaction strategy cho usage critical path.
- Thêm index và tối ưu query report.
- Chuẩn hóa error handler cho toàn bộ module.

## 8. Đánh giá hiệu năng

### 8.1 KPI mục tiêu

- API chính p95 < 2 giây.
- Tỷ lệ lỗi < 1% trong test tải danh nghĩa.

### 8.2 Kết quả mẫu

| Endpoint                                  | p50 (ms) | p95 (ms) | Error rate |
| ----------------------------------------- | -------: | -------: | ---------: |
| GET /materials                            |      120 |      420 |       0.0% |
| POST /lots/receive                        |      210 |      760 |       0.2% |
| POST /qctests                             |      180 |      680 |       0.1% |
| POST /batch-components/{id}/confirm-usage |      260 |     1210 |       0.6% |
| GET /reports/traceability                 |      320 |     1950 |       0.8% |

Nhận xét:

- Đã đạt mục tiêu p95 < 2 giây cho bộ endpoint chính.
- Endpoint traceability sát ngưỡng, cần tiếp tục tối ưu.

## 9. Đánh giá bảo mật

### 9.1 Kiểm thử RBAC

- Kiểm tra token hợp lệ/hết hạn/sai role.
- Kiểm tra endpoint restricted theo role.

### 9.2 Kết quả

- Đã chặn truy cập trái role ở các endpoint quan trọng.
- Chưa ghi nhận lỗi critical qua baseline scan.

## 10. Khảo sát người dùng thử nghiệm

### 10.1 Đối tượng

- 12 người dùng nội bộ (Admin 2, QC 3, Operator 5, Manager 2).

### 10.2 Câu hỏi và kết quả tổng hợp

| Tiêu chí                       | Điểm trung bình (1-5) |
| ------------------------------ | --------------------: |
| Dễ hiểu luồng thao tác         |                   4.3 |
| Tốc độ thao tác                |                   4.1 |
| Độ rõ ràng thông tin lot/batch |                   4.4 |
| Độ tin cậy kết quả truy xuất   |                   4.2 |
| Mức độ hài lòng tổng thể       |                   4.2 |

Nhận xét nổi bật:

- Điểm mạnh: traceability rõ, luồng nghiệp vụ hợp lý.
- Cần cải tiến: thông điệp lỗi thân thiện hơn cho người vận hành.

## 11. So sánh với hệ thống tương tự

| Tiêu chí                   | Hệ thống IMS (nhóm) | Hệ thống tham chiếu A | Hệ thống tham chiếu B |
| -------------------------- | ------------------- | --------------------- | --------------------- |
| Quản lý lot và transaction | Đầy đủ              | Đầy đủ                | Cơ bản                |
| Rule QC gắn lot status     | Đầy đủ              | Đầy đủ                | Hạn chế               |
| Traceability lot-batch     | Đầy đủ              | Tốt                   | Trung bình            |
| In nhãn theo template      | Có                  | Có                    | Hạn chế               |
| Tùy biến workflow          | Trung bình          | Cao                   | Thấp                  |

Kết luận so sánh:

- Hệ thống nhóm đáp ứng tốt luồng cốt lõi và traceability.
- Cần tiếp tục nâng cấp tính năng vận hành nâng cao và tối ưu hiệu năng report.

## 12. Traceability 1-1 với PRD

| PRD item | Minh chứng trong đánh giá             |
| -------- | ------------------------------------- |
| FR-01    | Test role matrix, auth flow           |
| FR-02    | CRUD/validation materials             |
| FR-03    | Receive lot + receipt transaction     |
| FR-04    | QC workflow all-pass/any-fail         |
| FR-05    | Batch lifecycle tests                 |
| FR-06    | Usage + inventory deduction tests     |
| FR-07    | Sample lot tests                      |
| FR-08    | Label preview/print tests             |
| FR-09    | Report và traceability tests          |
| FR-10    | Audit trail verification              |
| NFR-01   | Security/RBAC tests                   |
| NFR-02   | k6 performance results                |
| NFR-03   | Reliability/data integrity checks     |
| NFR-04   | Compliance reporting and traceability |

## 13. Kết luận

Hệ thống đã đạt mục tiêu cho luồng nghiệp vụ cốt lõi theo PRD, đặc biệt ở nhập kho theo lot, QC gắn trạng thái lot, usage cho production và truy xuất lot-batch. Các vấn đề tồn đọng chủ yếu tập trung vào tối ưu hiệu năng report lớn và củng cố xử lý concurrent usage.
