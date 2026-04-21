# 03_Prototype_Vibe Coding

## 1. Mục tiêu

Tài liệu này ghi lại cách tạo/cập nhật:

- 03_Prototype.md

Mục tiêu:

- Mô tả UI flow theo từng bước của luồng nghiệp vụ chính.
- Làm rõ input/output, trạng thái, và lỗi thường gặp.
- Mapping màn hình với FR trong PRD.

## 2. Công cụ và tool đã sử dụng

| Tool                  | Mục đích                           | Đầu ra                     |
| --------------------- | ---------------------------------- | -------------------------- |
| functions.read_file   | Đọc PRD + pages hiện có            | Danh sách screen cần mô tả |
| functions.grep_search | Tìm tên route/feature trong source | Đồng bộ naming UI          |
| functions.apply_patch | Chỉnh sửa prototype doc            | Tài liệu prototype final   |
| functions.file_search | Kiểm tra đường dẫn tài nguyên      | Tránh link sai             |

## 3. Prompt chính đã dùng

1. "Viết Prototype document theo kiểu screen-by-screen cho IMS, gồm main flow và exception flow."
2. "Mô tả ít nhất 1 luồng chính end-to-end: login -> receive lot -> qc evaluate -> batch -> report."
3. "Mỗi screen cần có mục tiêu, field, action, expected result, validation, error state."
4. "Thêm bảng traceability từ screen P-01..P-11 sang FR-ID."

## 4. Prompt refine

1. "Chuẩn hóa tên screen và tên action theo frontend pages hiện có."
2. "Bổ sung thông điệp lỗi người dùng cho các tình huống 401/403/validation fail."
3. "Thêm ghi chú responsive cơ bản (desktop/mobile)."
4. "Rút gọn mô tả dài, ưu tiên step để dễ demo."

## 5. Quy trình làm việc với AI

1. Trích xuất các flow cần demo từ PRD.
2. Sinh danh sách screen và transitions.
3. Soát với source frontend để đồng bộ tên chức năng.
4. Tinh chỉnh cho rõ testable outcome.
5. Chốt bản final.

## 6. Tiêu chí chấp nhận

- Có ít nhất 1 business flow chính đầy đủ.
- Có state thành công/thất bại cho màn hình.
- Có map P-ID -> FR-ID.
- Người đọc có thể demo theo tài liệu không bị lạc.

## 7. Ghi chú minh bạch

- AI hỗ trợ tạo skeleton và mô tả step.
- Nhóm sửa lại theo UI thực tế và tiếp tục cập nhật khi giao diện đổi.
