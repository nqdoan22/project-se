# 02_Domain Model_Vibe Coding

## 1. Mục tiêu

Tài liệu này ghi lại cách tạo và cập nhật:

- 02_Domain Model.md

Mục tiêu:

- Xác định bounded contexts, entities, value objects.
- Định nghĩa quan hệ và ràng buộc nghiệp vụ.
- Mapping BR/FR từ PRD sang domain.

## 2. Công cụ và tool đã sử dụng

| Tool                  | Mục đích                                | Đầu ra                      |
| --------------------- | --------------------------------------- | --------------------------- |
| functions.read_file   | Đọc PRD và domain draft                 | Danh sách concept cần model |
| functions.grep_search | Tìm thuật ngữ trùng lặp/không nhất quán | Chuẩn hóa naming            |
| functions.apply_patch | Sửa model và phần diễn giải             | Domain doc final            |
| functions.file_search | Kiểm tra tên file liên kết              | Traceability hợp lệ         |

## 3. Prompt chính đã dùng

1. "Xây dựng Domain Model cho IMS theo DDD lite: bounded contexts, aggregates, entities, value objects, enums, invariants."
2. "Viết class diagram mermaid bao gồm Material, InventoryLot, QCTest, ProductionBatch, BatchComponent, InventoryTransaction, LabelTemplate, User."
3. "Diễn giải quy tắc nghiệp vụ theo BR-ID và chỉ ra aggregate nào enforce rule."
4. "Bổ sung domain events có thể mở rộng event-driven về sau."

## 4. Prompt refine

1. "Kiểm tra sự phù hợp với PRD FR-01..FR-10 và bổ sung mapping ở cuối tài liệu."
2. "Loại bỏ trường không tồn tại trong codebase để tránh model ảo."
3. "Làm rõ ownership relation giữa ProductionBatch và BatchComponent."
4. "Bổ sung constraints cho lot status transition và sample lot relation."

## 5. Quy trình làm việc với AI

1. Lấy requirement IDs từ PRD.
2. Sinh domain draft và diagram.
3. Soát lại với code entities hiện có.
4. Chỉnh sửa invariants, enum, và mapping.
5. Chốt tài liệu và khóa tên thuật ngữ.

## 6. Tiêu chí chấp nhận

- Có bounded contexts và aggregate roots rõ ràng.
- Có relation + cardinality + ràng buộc nghiệp vụ.
- Có mapping tới FR/BR trong PRD.
- Có tính nhất quán với architecture và API docs.

## 7. Ghi chú minh bạch

- AI đề xuất cấu trúc model ban đầu.
- Nhóm đã xác thực lại theo code và nghiệp vụ thực tế trước khi chốt.
