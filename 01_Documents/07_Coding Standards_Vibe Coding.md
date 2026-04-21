# 07_Coding Standards_Vibe Coding

## 1. Mục tiêu

Tài liệu này ghi lại cách tạo/cập nhật:

- 07_Coding Standards.md

Mục tiêu:

- Chốt coding rules cho backend/frontend.
- Định nghĩa quality gates và công cụ enforcement.
- Đảm bảo style + security + testability nhất quán.

## 2. Công cụ và tool đã sử dụng

| Tool                  | Mục đích                          | Đầu ra                   |
| --------------------- | --------------------------------- | ------------------------ |
| functions.read_file   | Đọc source và standards draft     | Rule set ban đầu         |
| functions.grep_search | Tìm conventions đang tồn tại      | Chuẩn hóa theo thực tế   |
| functions.apply_patch | Cập nhật coding standards         | Tài liệu standards final |
| functions.get_errors  | Kiểm tra lỗi lint/build liên quan | Xác thực tính khả thi    |

## 3. Prompt chính đã dùng

1. "Viết Coding Standards cho IMS gồm naming, layering, exception handling, validation, logging, testing, security, git workflow."
2. "Tách phần backend Java và frontend React rõ ràng."
3. "Thêm section công cụ enforce: formatter/linter/test command/pre-merge checks."
4. "Bổ sung coding anti-patterns cần tránh và ví dụ ngắn."

## 4. Prompt refine

1. "Đồng bộ với stack hiện tại (Spring Boot 4, Java 21, React 19, Vite 7)."
2. "Thêm quy tắc API versioning /api/v1 và error response consistency."
3. "Bổ sung quy tắc bảo mật: không hardcode secret, role check tại backend."
4. "Rút gọn quy tắc trùng lặp, ưu tiên quy tắc có thể kiểm tra được."

## 5. Quy trình làm việc với AI

1. Thu thập conventions đang có trong repo.
2. Sinh standards draft theo module.
3. Rà soát với team và sửa để khả thi.
4. Add quality gate command.
5. Chốt bản final.

## 6. Tiêu chí chấp nhận

- Rule rõ, ngắn gọn, đo được.
- Có mapping tool enforce.
- Không mâu thuẫn với codebase hiện tại.
- Có bao gồm security và test discipline.

## 7. Ghi chú minh bạch

- AI đề xuất bộ quy tắc ban đầu.
- Nhóm lọc lại theo khả năng áp dụng thực tế trong dự án.
