# 04_Product Backlog_Vibe Coding

## 1. Mục tiêu

Tài liệu này ghi lại cách tạo/cập nhật:

- 04_Product Backlog.md

Mục tiêu:

- Tổ chức backlog theo Epic -> Story -> Acceptance Criteria.
- Gắn ưu tiên và release plan rõ ràng.
- Đảm bảo tiếp nối trực tiếp với PRD FR/NFR.

## 2. Công cụ và tool đã sử dụng

| Tool                  | Mục đích                        | Đầu ra                     |
| --------------------- | ------------------------------- | -------------------------- |
| functions.read_file   | Đọc PRD và backlog cũ           | Danh sách item cần bổ sung |
| functions.grep_search | Tìm FR-ID và phạm vi đã cam kết | Tránh sót story            |
| functions.apply_patch | Chỉnh sửa bảng Epic/Story       | Backlog chuẩn hóa          |
| functions.file_search | Kiểm tra tài liệu liên quan     | Liên kết traceability đúng |

## 3. Prompt chính đã dùng

1. "Tạo Product Backlog cho IMS với Epic E1..E5, story IDs, priority, estimate, và release target."
2. "Mỗi story cần có acceptance criteria ngắn gọn, testable, và map tới FR-ID."
3. "Bổ sung Definition of Done và quality gates cho từng release."
4. "Lập kế hoạch R1-R3 theo giá trị nghiệp vụ và phụ thuộc kỹ thuật."

## 4. Prompt refine

1. "Cân bằng scope giữa backend/frontend/test/deploy tasks."
2. "Tách story quá lớn thành story nhỏ để dễ tracking."
3. "Bổ sung story liên quan security, logging, và observability cơ bản."
4. "Đồng bộ tên role theo Admin/Manager/QC/Operator."

## 5. Quy trình làm việc với AI

1. Trích FR/NFR từ PRD.
2. Sinh backlog draft theo Epic.
3. Review thủ công với architecture constraints.
4. Điều chỉnh estimate và release sequencing.
5. Chốt backlog + traceability.

## 6. Tiêu chí chấp nhận

- Tất cả FR quan trọng đều có story cover.
- Story có acceptance criteria rõ và đo được.
- Có release plan và DoD.
- Không xung đột với architecture/deployment.

## 7. Ghi chú minh bạch

- AI hỗ trợ phân rã Epic/Story nhanh.
- Nhóm quyết định cuối cùng về ưu tiên và plan giao hàng.
