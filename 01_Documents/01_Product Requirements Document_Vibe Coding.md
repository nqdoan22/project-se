# 01_Product Requirements Document_Vibe Coding

## 1. Mục tiêu

Tài liệu này ghi lại cách nhóm đã sử dụng AI tools để tạo và cập nhật file PRD:

- 01_Product Requirements Document.md

Mục tiêu:

- Chuẩn hóa scope nghiệp vụ IMS.
- Định danh FR/NFR/BR rõ ràng.
- Tạo traceability 1-1 cho các tài liệu sau.

## 2. Công cụ và tool đã sử dụng

| Tool                  | Mục đích                          | Đầu ra kỳ vọng                          |
| --------------------- | --------------------------------- | --------------------------------------- |
| functions.read_file   | Đọc bản nháp và version cũ        | Hiểu context và phát hiện khoảng trống  |
| functions.grep_search | Tìm mismatch term, role, endpoint | Danh sách điểm lệch cần sửa             |
| functions.apply_patch | Chỉnh sửa có kiểm soát            | Bản PRD hoàn chỉnh, ít thay đổi dư thừa |
| functions.file_search | Kiểm tra file liên quan           | Đảm bảo map đúng tên tài liệu           |

## 3. Prompt chính đã dùng

1. "Viết lại Product Requirements Document cho Inventory Management System theo hướng enterprise, gồm: roles, pain points, business goals, luồng nghiệp vụ, FR-01..FR-10, NFR, BR, data dictionary, và traceability."
2. "Chuẩn hóa role model thành 4 vai trò Admin, Manager, QC, Operator và cập nhật toàn bộ section liên quan."
3. "Thêm bảng truy vết FR -> Domain -> Backlog -> Architecture -> API -> Test để làm baseline cho rà soát."
4. "Soát lỗi xung đột endpoint và thống nhất base path là /api/v1 trong toàn bộ PRD."

## 4. Prompt refine (iterative)

1. "Rút gọn phần mô tả dài dòng, giữ cấu trúc bảng và ID requirement để dễ review."
2. "Bổ sung acceptance criteria cho các FR quan trọng: QC evaluate, split lot, batch usage, report."
3. "Kiểm tra tính nhất quán business rules giữa FR và workflow."
4. "Thêm phần out-of-scope để tránh phạm vi mở rộng không kiểm soát."

## 5. Quy trình làm việc với AI

1. Thu thập context từ file cũ và tài liệu kiến trúc/backlog.
2. Sinh bản PRD draft theo khung mục chuẩn.
3. Rà soát thủ công từng section và chốt tên requirement IDs.
4. Chỉnh sửa vòng 2 cho tính nhất quán endpoint/role.
5. Chốt bản cuối khi traceability 1-1 đạt yêu cầu.

## 6. Tiêu chí chấp nhận kết quả

- Có đầy đủ roles, problems, goals, workflows.
- Có danh sách FR/NFR/BR đặt tên nhất quán.
- Có traceability matrix đúng tên tài liệu đích.
- Không còn xung đột role và endpoint.

## 7. Ghi chú minh bạch

- AI hỗ trợ sinh nội dung và đề xuất cấu trúc.
- Nhóm đã review thủ công, sửa nội dung theo context dự án, và chịu trách nhiệm bản cuối.
