# 07 - Coding Standards

## 1. Mục tiêu

Tài liệu này quy định các coding standards và coding conventions bắt buộc cho IMS, đồng bộ với PRD v2.

Mục tiêu:

- Giữ code nhất quán và dễ bảo trì.
- Giảm lỗi nghiệp vụ do coding style không đồng nhất.
- Hỗ trợ review, test, và truy vết thay đổi.

## 2. Phạm vi áp dụng

- Backend: Java 21 + Spring Boot.
- Frontend: React + Vite.
- Database scripts: MySQL.
- Documentation: Markdown.

## 3. Nguyên tắc chung

- Readability first: ưu tiên dễ đọc hơn clever code.
- Single responsibility: class/function tập trung một mục đích.
- Explicit business rules: rule nghiệp vụ phải rõ ràng trong service layer.
- Fail fast: validate input sớm, trả lỗi rõ nghĩa.
- Backward compatibility có kiểm soát cho API contracts.

## 4. Quy ước đặt tên

## 4.1 Backend

- Package: lowercase, theo domain.
- Class: PascalCase.
- Method/field: camelCase.
- Constant: UPPER_SNAKE_CASE.
- Enum values: PascalCase nếu map với DB enum hiện tại; nếu sử dụng uppercase thì map rõ với converter.

Ví dụ:

- MaterialService
- updateLotStatus
- MAX_PAGE_SIZE

## 4.2 Frontend

- Component: PascalCase.
- Hook: useXxx.
- Utility function: camelCase.
- File CSS module: kebab-case hoặc theo component.

## 4.3 Database

- Table/column: snake_case.
- FK index đặt tên theo pattern fk*<table>*<ref_table>.
- Index đặt tên theo pattern idx*<table>*<column>.

## 5. Coding standards backend (Java)

## 5.1 Class và layer

- Controller chỉ xử lý HTTP concerns.
- Service xử lý business rules.
- Repository chỉ truy cập dữ liệu.
- DTO tách biệt request/response, không expose entity trực tiếp.

## 5.2 Validation

- Bắt buộc sử dụng Jakarta validation cho request DTO.
- Validation nghiệp vụ bổ sung tại service.

## 5.3 Exception handling

- Dùng global exception handler.
- Error response thống nhất envelope và error code.
- Không nuốt exception không log.

## 5.4 Transaction

- Use case ghi dữ liệu phải đặt trong transaction.
- Use case nhạy cảm quantity phải có cơ chế chống concurrent update.

## 5.5 Logging

- Log theo mức: ERROR, WARN, INFO, DEBUG.
- Không log secret/token/password.
- Gắn requestId cho log có thể truy vết.

## 5.6 Test

- Unit test cho business rules BR-01..BR-07.
- Integration test cho endpoint critical.
- Tên testcase rõ nghĩa: should*<expected>\_when*<condition>.

## 6. Coding standards frontend (React)

## 6.1 Component architecture

- Tách smart/container và presentational khi màn hình phức tạp.
- Logic gọi API đặt trong services/hooks, tránh viết trực tiếp trong JSX lớn.

## 6.2 State management

- State local cho UI state nhỏ.
- State async qua query layer, tránh duplicate state.

## 6.3 Form

- Có validation client-side cho required field và format cơ bản.
- Message lỗi phải rõ ràng với người vận hành.

## 6.4 UI/UX consistency

- Cùng quy ước màu cho lot status:
  - Quarantine: vàng
  - Accepted: xanh
  - Rejected: đỏ
  - Depleted: xám

## 7. API standards

- Base path: /api/v1.
- Dùng noun cho resources, verb cho action đặc biệt cần thiết.
- Pagination thống nhất query params page, size, sort.
- HTTP status và error code nhất quán theo API spec.

## 8. Security standards

- Xác thực và phân quyền qua Keycloak.
- Role set thống nhất: Admin, Manager, QC, Operator.
- Không hard-code credentials vào source.
- Mandatory review với endpoint write quan trọng.

## 9. Git và code review standards

- Branch naming: type/scope-short-description.
- Commit message: Conventional Commits có mô tả rõ.
- PR bắt buộc có:
  - Mục tiêu thay đổi.
  - Ảnh hưởng tới FR/NFR/BR nào.
  - Cách test lại.

## 10. Definition of Done cho code

- Build pass.
- Test pass theo phạm vi thay đổi.
- Lint/format pass.
- Có cập nhật docs nếu thay đổi contract/rules.
- Có reviewer chấp thuận.

## 11. Công cụ hỗ trợ standards

- Backend:
  - Spotless/Checkstyle (nếu bật trong pipeline).
  - JUnit + Mockito + Spring test.
- Frontend:
  - ESLint.
  - Prettier (nếu áp dụng).
- Security:
  - Dependency scan và basic SAST (nếu có).

## 12. Traceability với PRD

| PRD item       | Quy định standards liên quan                   |
| -------------- | ---------------------------------------------- |
| FR-01, NFR-01  | Security standards, role conventions           |
| FR-03..FR-07   | Backend service rules, transaction standards   |
| FR-08          | API/UI consistency cho label flows             |
| FR-09, FR-10   | Logging, audit, error handling, test standards |
| NFR-02..NFR-04 | Performance/testing/reliability standards      |
