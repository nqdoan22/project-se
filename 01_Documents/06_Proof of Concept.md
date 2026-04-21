# 06 - Proof of Concept

## 1. Mục tiêu

Proof of Concept này kiểm chứng tính khả thi của những điểm kỹ thuật khó trong IMS, thay vì chỉ mô tả ý tưởng.

Mục tiêu POC:

- Kiểm chứng luồng nghiệp vụ lot -> QC -> usage -> batch.
- Kiểm chứng RBAC thông qua Keycloak với backend API.
- Kiểm chứng toàn vẹn tồn kho khi concurrent usage.
- Kiểm chứng khả năng truy xuất dữ liệu lot-batch.

## 2. Bài toán kỹ thuật được chọn

POC tập trung 3 bài toán:

1. Đồng bộ trạng thái lot theo kết quả QC (all pass / any fail).
2. Đảm bảo trừ tồn chính xác khi xác nhận usage đồng thời.
3. Xác thực và phân quyền role thông qua JWT từ Keycloak.

Lý do chọn:

- Đây là các điểm rủi ro cao nhất trong nghiệp vụ IMS.
- Nếu 3 điểm này không đạt, hệ thống dễ sai dữ liệu và vi phạm tuân thủ.

## 3. Phạm vi POC

Trong phạm vi:

- Backend API cho receive lot, qctest, confirm usage, traceability.
- Kết nối Keycloak và MySQL.
- Kịch bản concurrent usage ở mức endpoint.

Ngoài phạm vi:

- UI production đầy đủ.
- Báo cáo nâng cao và analytics.
- Tối ưu hiệu năng toàn hệ thống ở quy mô lớn.

## 4. Môi trường và công cụ

- Java 21 + Spring Boot.
- MySQL.
- Keycloak.
- Postman/Newman.
- Script test concurrent (POC folder).

Vị trí mã nguồn minh chứng:

- Thư mục: POC/backend và POC/frontend.

## 5. Kịch bản thử nghiệm

## 5.1 Kịch bản A - QC status transition

Bước thực hiện:

1. Tạo lot mới (status Quarantine).
2. Tạo nhiều QCTest cho lot.
3. Trường hợp 1: tất cả test Pass.
4. Trường hợp 2: có ít nhất 1 test Fail.

Kỳ vọng:

- Trường hợp 1: lot -> Accepted.
- Trường hợp 2: lot -> Rejected.

Kết quả:

- Đạt kỳ vọng ở cả 2 trường hợp.

## 5.2 Kịch bản B - Concurrent usage

Bước thực hiện:

1. Tạo lot Accepted với quantity có hạn.
2. Gửi nhiều request confirm usage gần đồng thời.
3. Kiểm tra quantity sau cùng và transaction log.

Kỳ vọng:

- Không âm tồn.
- Không bị duplicate trừ tồn do retry.

Kết quả:

- Ban đầu phát hiện nguy cơ race condition.
- Sau khi áp dụng transaction strategy + idempotency key, kết quả ổn định hơn.

## 5.3 Kịch bản C - RBAC với Keycloak

Bước thực hiện:

1. Tạo token cho role Admin, Manager, QC, Operator.
2. Gọi endpoint read/write với từng role.
3. Kiểm tra 401/403 cho trường hợp không hợp lệ.

Kỳ vọng:

- Role hợp lệ được phép thao tác đúng phạm vi.
- Role không hợp lệ bị chặn.

Kết quả:

- Đạt yêu cầu, cần tiếp tục duy trì role matrix nhất quán.

## 6. Kết quả tổng hợp POC

| Bài toán             | Trạng thái       | Ghi chú                                 |
| -------------------- | ---------------- | --------------------------------------- |
| QC status transition | Đạt              | Rule all-pass/any-fail hoạt động đúng   |
| Concurrent usage     | Đạt có điều kiện | Cần transaction và idempotency          |
| RBAC Keycloak        | Đạt              | Cần giữ naming role nhất quán toàn docs |

## 7. Bài học rút ra

- Business rules quan trọng phải đặt ở backend service, không chỉ ở frontend.
- Cần có chiến lược concurrency từ đầu cho use case trừ tồn.
- IAM naming conventions phải đồng bộ giữa docs, code và Keycloak config.

## 8. Ảnh hưởng đến kiến trúc và backlog

- Architecture bổ sung hướng transaction/locking ở critical path usage.
- Backlog bổ sung công việc hardening cho error handling và idempotency.
- Evaluation bổ sung test case concurrent usage và RBAC matrix.

## 9. Hạn chế của POC

- Chưa test ở quy mô tải cao như production.
- Chưa bao phủ tất cả edge cases cho report lớn.
- Chưa hoàn thiện bộ script benchmark chuyên sâu.

## 10. Kết luận

POC xác nhận hướng giải pháp hiện tại có tính khả thi cho luồng nghiệp vụ cốt lõi IMS. Các rủi ro còn lại đã được nhận diện và đưa vào backlog/kế hoạch hardening trước giai đoạn nộp cuối kỳ.
