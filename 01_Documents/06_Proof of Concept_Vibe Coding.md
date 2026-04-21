# 06_Proof of Concept_Vibe Coding

## 1. Mục tiêu

Tài liệu này ghi lại cách tạo/cập nhật:

- 06_Proof of Concept.md

Mục tiêu:

- Chứng minh khả thi kỹ thuật cho tính năng khó.
- Có kịch bản, bước thực hiện, kết quả, và bài học.
- Có liên hệ trực tiếp tới rủi ro kỹ thuật trong PRD/Architecture.

## 2. Công cụ và tool đã sử dụng

| Tool                      | Mục đích                       | Đầu ra                     |
| ------------------------- | ------------------------------ | -------------------------- |
| functions.read_file       | Đọc context POC + architecture | Chọn đúng technical risk   |
| functions.grep_search     | Tìm endpoint/service liên quan | Cơ sở viết kịch bản POC    |
| functions.apply_patch     | Cập nhật POC report            | Tài liệu POC đầy đủ        |
| functions.run_in_terminal | Kiểm tra nhanh command/mô tả   | Minh chứng runbook phù hợp |

## 3. Prompt chính đã dùng

1. "Viết Proof of Concept cho IMS, tập trung các bài toán: status transition, concurrent update, role enforcement."
2. "Mỗi scenario cần có: mục tiêu, setup, steps, expected, actual, kết luận."
3. "Bổ sung section limitation và hướng hardening để đưa vào kế hoạch release."
4. "Thêm mapping scenario -> risk -> architecture decision."

## 4. Prompt refine

1. "Tăng tính cụ thể của kết quả (pass/fail, metric, thông điệp lỗi)."
2. "Tách rõ POC code/path và phần code production."
3. "Bổ sung đề xuất test tự động cho scenario đã pass."
4. "Chuẩn hóa ngôn ngữ để dễ trình bày vấn đáp."

## 5. Quy trình làm việc với AI

1. Chọn 2-3 technical risks có tác động cao.
2. Sinh report draft cho từng scenario.
3. Đối chiếu kết quả với codebase và test logs.
4. Chỉnh sửa kết luận + lessons learned.
5. Chốt bản final.

## 6. Tiêu chí chấp nhận

- Có scenario khó và có kết quả cụ thể.
- Có bài học và đề xuất tiếp theo.
- Có liên kết tới rủi ro kiến trúc.
- Tài liệu đủ để bảo vệ trước hội đồng.

## 7. Ghi chú minh bạch

- AI hỗ trợ viết cấu trúc report.
- Nhóm xác nhận lại kết quả với code/runbook trước khi nộp.
