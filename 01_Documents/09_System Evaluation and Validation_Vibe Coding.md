# 09_System Evaluation and Validation_Vibe Coding

## 1. Mục tiêu

Tài liệu này ghi lại cách tạo/cập nhật:

- 09_System Evaluation and Validation.md

Mục tiêu:

- Mô tả quy trình test, công cụ, kết quả, so sánh hệ thống.
- Có minh chứng video bắt buộc theo checklist.
- Tạo căn cứ đánh giá chất lượng hệ thống trước khi nộp.

## 2. Công cụ và tool đã sử dụng

| Tool                      | Mục đích                            | Đầu ra                   |
| ------------------------- | ----------------------------------- | ------------------------ |
| functions.read_file       | Đọc evaluation draft + checklist    | Danh sách section cần có |
| functions.grep_search     | Tìm placeholder, test term, kết quả | Phát hiện chỗ thiếu      |
| functions.apply_patch     | Chỉnh sửa tài liệu đánh giá         | Evaluation doc final     |
| functions.run_in_terminal | Kiểm tra lệnh test/build tham chiếu | Tăng độ tin cậy runbook  |

## 3. Prompt chính đã dùng

1. "Viết tài liệu System Evaluation and Validation cho IMS: setup công cụ, test method, test matrix, kết quả, benchmark/cơ sở so sánh."
2. "Thêm section security, performance cơ bản, và user feedback summary."
3. "Bổ sung bảng traceability từ testcase sang FR/NFR."
4. "Thêm section video minh chứng quá trình cài đặt công cụ + chạy test + kết quả."

## 4. Prompt refine

1. "Kiểm tra sự phù hợp với test artifacts trong repo (unit report, e2e output nếu có)."
2. "Làm rõ tiêu chí pass/fail cho từng nhóm test."
3. "Đánh dấu phần còn hạn chế và backlog test tiếp theo."
4. "Chuẩn hóa từ vựng với PRD và Architecture để tránh lệch thuật ngữ."

## 5. Quy trình làm việc với AI

1. Tổng hợp phạm vi đánh giá cần có.
2. Sinh khung test matrix + kết quả.
3. Đối chiếu với artifacts và reports thực tế.
4. Chỉnh sửa theo feedback team.
5. Chốt bản final.

## 6. Tiêu chí chấp nhận

- Có setup + method + results rõ ràng.
- Có traceability test -> requirement.
- Có mục so sánh hệ thống tương tự.
- Có section video minh chứng bắt buộc.

## 7. Ghi chú minh bạch

- AI hỗ trợ cấu trúc và diễn đạt tài liệu.
- Nhóm đã kiểm tra lại bằng minh chứng test thực tế trước khi chốt.
