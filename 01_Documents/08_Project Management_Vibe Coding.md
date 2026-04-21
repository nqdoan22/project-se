# 08_Project Management_Vibe Coding

## 1. Mục tiêu

Tài liệu này ghi lại cách tạo/cập nhật:

- 08_Project Management.md

Mục tiêu:

- Tổng hợp tổ chức nhóm, vai trò, estimate, milestone, risk.
- Đáp ứng các minh chứng bắt buộc (team building video, hệ thống cộng tác, issue tracker).
- Hỗ trợ quản trị tiến độ và trách nhiệm.

## 2. Công cụ và tool đã sử dụng

| Tool                  | Mục đích                                  | Đầu ra                   |
| --------------------- | ----------------------------------------- | ------------------------ |
| functions.read_file   | Đọc checklist nộp và PM draft             | Danh sách mục bắt buộc   |
| functions.grep_search | Tìm chỗ thiếu minh chứng/video            | Danh sách cần bổ sung    |
| functions.apply_patch | Chỉnh sửa PM doc                          | PM doc đồng bộ checklist |
| functions.file_search | Kiểm tra tồn tại đường dẫn ảnh/tài nguyên | Tránh tham chiếu sai     |

## 3. Prompt chính đã dùng

1. "Viết Project Management doc cho IMS: team roles, RACI, estimation, milestones, risk register, communication rules."
2. "Bổ sung section riêng cho các minh chứng bắt buộc theo checklist nộp môn học."
3. "Tạo bảng kế hoạch release theo timeline và ownership rõ ràng."
4. "Thêm section chấp nhận thay đổi scope và cơ chế review hằng tuần."

## 4. Prompt refine

1. "Kiểm tra đủ các mục bắt buộc: member info, team building video, communication invite, PM tool invite, issue tracker invite."
2. "Đánh dấu các link đang placeholder để thay thế trước ngày nộp."
3. "Bổ sung risk và mitigation cho mất thành viên, trễ milestone, và xung đột tài liệu-code."
4. "Rút gọn phần dài dòng, ưu tiên bảng và action items."

## 5. Quy trình làm việc với AI

1. Trích checklist cần nộp thành task list.
2. Sinh PM draft theo cấu trúc bắt buộc.
3. Soát gap về minh chứng và links.
4. Cập nhật và khóa các section mandatory.
5. Chốt bản final.

## 6. Tiêu chí chấp nhận

- Có đầy đủ các section bắt buộc theo checklist.
- Có estimate, milestone, risk management.
- Có đường dẫn minh chứng rõ ràng.
- Có thể dùng trực tiếp khi nộp và vấn đáp.

## 7. Ghi chú minh bạch

- AI hỗ trợ tổng hợp khung PM.
- Nhóm tự bổ sung dữ liệu thực tế (video, screenshot invite, links thật) trước khi nộp.
