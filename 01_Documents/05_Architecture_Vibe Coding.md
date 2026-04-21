# 05_Architecture_Vibe Coding

## 1. Mục tiêu

Tài liệu này ghi lại cách tạo/cập nhật:

- 05_Architecture.md

Mục tiêu:

- Trình bày architecture theo nhiều góc nhìn: context, logical, process, development, data, security, deployment.
- Chốt công nghệ và lý do lựa chọn.
- Traceability với PRD và backlog.

## 2. Công cụ và tool đã sử dụng

| Tool                  | Mục đích                             | Đầu ra                       |
| --------------------- | ------------------------------------ | ---------------------------- |
| functions.read_file   | Đọc PRD, backlog, API spec           | Input cho architecture views |
| functions.grep_search | Tìm thông tin stack/cấu hình thực tế | Đồng bộ docs và code         |
| functions.apply_patch | Chỉnh sửa architecture doc           | Bản architecture final       |
| functions.file_search | Kiểm tra file triển khai/build       | Tham chiếu chính xác         |

## 3. Prompt chính đã dùng

1. "Viết Architecture document cho IMS theo 4+1 views mở rộng: system context, logical, process, development, deployment, data, security."
2. "Thêm mermaid diagrams để mô tả luồng từ frontend -> backend -> db -> keycloak."
3. "Bổ sung ADR ngắn gọn cho các quyết định công nghệ quan trọng."
4. "Thêm bảng traceability FR -> architecture decisions/components."

## 4. Prompt refine

1. "Đồng bộ Java 21, Spring Boot, React/Vite, Keycloak, MySQL theo state hiện tại của source."
2. "Làm rõ phần bảo mật RBAC, JWT validation, và audit trail."
3. "Nếu microservice chưa tách thật, ghi rõ strategy hiện tại và hướng mở rộng."
4. "Rút gọn nội dung lặp lại, ưu tiên points có khả năng demo."

## 5. Quy trình làm việc với AI

1. Tổng hợp stack và constraints từ source.
2. Sinh architecture draft + diagram.
3. Cross-check với deployment và API spec.
4. Fix mismatch naming/version.
5. Chốt bản final với ADR + traceability.

## 6. Tiêu chí chấp nhận

- Có đầy đủ các view cần thiết.
- Công nghệ khớp với source code thực tế.
- Có security và deployment considerations.
- Có traceability tới PRD/backlog.

## 7. Ghi chú minh bạch

- AI đề xuất khung architecture và sơ đồ.
- Nhóm review và sửa theo implementation thực tế của repo.
