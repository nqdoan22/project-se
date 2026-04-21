# 08 - Project Management

## 1. Mục tiêu

Tài liệu này mô tả cách nhóm quản lý dự án IMS gồm:

- Cơ cấu nhân sự và vai trò.
- Kế hoạch phạm vi, tiến độ, mốc chính.
- Ước lượng kích cỡ, effort và chi phí.
- Công cụ hợp tác, theo dõi công việc và quản lý lỗi.

## 2. Thông tin nhóm

## 2.1 Thành viên và vai trò

| STT | Họ tên       | Vai trò chính      | Vai trò phụ          |
| --- | ------------ | ------------------ | -------------------- |
| 1   | Nguyễn Văn Xanh | Team Lead, Developer | Test and Validation         |
| 2   | Nguyễn Quang Doãn   | Deployment, CI/CD            |   |
| 3   | Dương Đặng Thành Lâm    | Management, Planning, Docs           | Prototyping   |
| 4   | Hoàng Túy Minh  | Developer                 | Test and Validation  |

Ghi chú:

- Tên thành viên có thể cập nhật theo danh sách chính thức của nhóm trước khi nộp cuối kỳ.

## 2.2 RACI tổng quan

| Công việc              | Lead | Backend | Frontend | QA  | DevOps |
| ---------------------- | ---- | ------- | -------- | --- | ------ |
| PRD và Domain          | A    | R       | C        | C   | C      |
| API và Business rules  | A    | R       | C        | C   | C      |
| UI và Prototype        | C    | C       | R        | C   | C      |
| Test và Evaluation     | C    | C       | C        | R   | C      |
| Deployment và Vận hành | C    | C       | C        | C   | R      |

Ký hiệu:

- R: Responsible
- A: Accountable
- C: Consulted

## 3. Phạm vi quản lý backlog

- Product backlog quản lý theo Epic E1..E5 và release R1..R3.
- Mỗi story được map tới FR/NFR trong PRD.
- Backlog refinement thực hiện hằng tuần.

## 4. Ước lượng kích cỡ và effort

## 4.1 Ước lượng theo story points

| Epic                           | Story points ước lượng |
| ------------------------------ | ---------------------: |
| E1 Identity and Audit          |                     18 |
| E2 Master Data and Label       |                     20 |
| E3 Receiving and Lot lifecycle |                     28 |
| E4 Production and Usage        |                     26 |
| E5 Reporting and NFR           |                     22 |
| Tổng                           |                    114 |

## 4.2 Ước lượng effort

- Số thành viên tham gia: 5.
- Năng suất trung bình: 10-12 story points/sprint.
- Số sprint dự kiến: 8-10 sprint.

## 4.3 Ước lượng thời gian và chi phí

- Tổng effort dự kiến: 900-1100 person-hours.
- Thời gian thực hiện: 11 tuần học + bổ sung trước nộp cuối.
- Chi phí cơ hội ước tính nội bộ: theo effort giờ công của nhóm.

## 5. Kế hoạch tiến độ và mốc quan trọng

| Mốc | Nội dung                            | Tuần dự kiến |
| --- | ----------------------------------- | ------------ |
| M1  | Chốt PRD, Domain, Prototype         | Tuần 2       |
| M2  | Hoàn thành core flow R1             | Tuần 5       |
| M3  | Demo giữa kỳ + tài liệu đợt 1       | Tuần 7       |
| M4  | Hoàn thành R2 và phần deployment    | Tuần 9       |
| M5  | Hoàn thành evaluation và user guide | Tuần 10      |
| M6  | Đóng gói nộp cuối kỳ                | Tuần 11      |

## 6. Quy trình làm việc

- Weekly planning và review.
- Daily async update trên kênh giao tiếp nhóm.
- Definition of Done thống nhất với Product Backlog.
- Mọi thay đổi quan trọng phải cập nhật tài liệu liên quan.

## 7. Team building

- Link video buổi team building:
  - https://www.youtube.com/watch?v=IMS_TEAM_BUILDING_VIDEO_ID

## 8. Hệ thống giao tiếp nội bộ

- Nền tảng đề xuất: Discord hoặc Slack.
- Link hệ thống:
  - https://discord.com/channels/IMS_TEAM_WORKSPACE
- Minh chứng mời giảng viên vào hệ thống:
  - Ảnh chụp màn hình: 01_Documents/images/invite-communication-admin.png

## 9. Hệ thống quản lý dự án

- Nền tảng đề xuất: Jira hoặc Trello.
- Link hệ thống:
  - https://trello.com/b/IMS_PROJECT_BOARD
- Minh chứng mời giảng viên:
  - Ảnh chụp màn hình: 01_Documents/images/invite-project-management-admin.png

## 10. Hệ thống quản lý lỗi

- Nền tảng đề xuất: GitHub Issues.
- Link hệ thống:
  - https://github.com/nqdoan22/project-se/issues
- Minh chứng mời giảng viên:
  - Ảnh chụp màn hình: 01_Documents/images/invite-issue-tracker-admin.png

## 11. Risk management

| Risk                          | Ảnh hưởng  | Xác suất   | Giải pháp                |
| ----------------------------- | ---------- | ---------- | ------------------------ |
| Lệch tài liệu và code         | Cao        | Trung bình | Rà chéo docs mỗi sprint  |
| Thiếu thời gian trước hạn nộp | Cao        | Trung bình | Chốt scope R1/R2 trước   |
| Lỗi RBAC và IAM               | Cao        | Trung bình | Test role matrix định kỳ |
| Hiệu năng report dữ liệu lớn  | Trung bình | Trung bình | Tối ưu query và index    |

## 12. Truyền thông và báo cáo

- Báo cáo tiến độ hằng tuần với format:
  - Đã hoàn thành
  - Đang thực hiện
  - Blockers
  - Kế hoạch tuần tới

## 13. Traceability với tài liệu khác

- PRD: business goals và requirements.
- Product Backlog: implementation scope và release plan.
- System Evaluation: kết quả kiểm thử và validation.
- Deployment/User Guide: khả năng vận hành và sử dụng.

## 14. Ghi chú nộp bài

- Toàn bộ link và ảnh minh chứng cần được cập nhật bằng dữ liệu thực tế của nhóm trước khi nộp cuối kỳ.
- Nếu nhóm chia điểm theo tỷ lệ riêng, thông tin phải được đặt nhất quán trên trang bìa tất cả sản phẩm.
