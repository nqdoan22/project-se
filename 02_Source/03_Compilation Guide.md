# 03 - Compilation Guide

## 1. Mục tiêu tài liệu

Tài liệu này hướng dẫn **một nhà phát triển (Developer)** thực hiện toàn bộ các bước để:

- Cài đặt môi trường phát triển từ đầu trên một máy tính sạch.
- Cấu hình các biến môi trường cần thiết.
- Biên dịch, chạy và kiểm thử mã nguồn hệ thống IMS (Inventory Management System) trên máy local.

> ⚠️ Các giảng viên sẽ **KHÔNG** giải quyết các thắc mắc về điểm số nếu mã nguồn không biên dịch được theo tài liệu này (minh chứng bằng các ảnh chụp màn hình).

---

## 2. Tổng quan kiến trúc hệ thống

Hệ thống IMS gồm 3 thành phần chính:

| Thành phần | Công nghệ | Cổng mặc định |
| --- | --- | --- |
| Backend API | Spring Boot 4.0.2 / Java 21 / Gradle | `8080` |
| Frontend UI | React 19 / Vite 7 / Axios | `3000` (Docker) hoặc `5173` (dev) |
| Identity & Access | Keycloak 24.0 (OAuth2 + JWT) | `9090` |
| Cơ sở dữ liệu | MySQL (Aiven cloud hoặc local) | `3306` |
| Vector Store (RAG) | Qdrant Cloud | `6333` |

Cấu trúc thư mục mã nguồn:

```
02_Source/
└── 01_Source Code/
    ├── backend/           # Spring Boot application
    ├── frontend/          # React + Vite application
    ├── database/          # SQL schema và test data
    ├── keycloak-deploy/   # Cấu hình Keycloak realm
    ├── scripts/           # Shell scripts tiện ích
    └── docker-compose.yml # Chạy toàn bộ hệ thống bằng Docker
```

---

## 3. Yêu cầu phần mềm

### 3.1 Bắt buộc

| Phần mềm | Phiên bản tối thiểu | Ghi chú |
| --- | --- | --- |
| **JDK** | 21 (Temurin/OpenJDK) | Gradle Wrapper đã bao gồm, không cần cài Gradle riêng |
| **Node.js** | 20 LTS | Cần cho frontend |
| **npm** | 10+ | Đi kèm Node.js |
| **Docker Desktop** | 24+ | Để chạy toàn bộ stack bằng Docker Compose |
| **Git** | 2.40+ | Để clone repository |

### 3.2 Tùy chọn (khi chạy không dùng Docker)

| Phần mềm | Ghi chú |
| --- | --- |
| **MySQL** 8.0+ | Nếu muốn chạy DB local thay vì dùng Aiven |
| **Keycloak** 24.0 | Nếu muốn chạy Keycloak local riêng |
| **mysql-client** | Cần để chạy script migration thủ công |
| **curl**, **bash** | Cần để chạy các shell scripts trong `scripts/` |

---

## 4. Lấy mã nguồn từ GitHub

### 4.1 Truy cập Source Control (GitHub)

Repository của nhóm được lưu trữ trên GitHub:

- **URL Repository:** `https://github.com/nqdoan22/project-se`
- **Nhánh chính:** `main`
- **Nhánh phát triển:** `develop`

> 📌 Thông tin mời tham gia hệ thống GitHub với vai trò Collaborator, link và ảnh chụp hành động mời xem tại **mục 9** của tài liệu này.

### 4.2 Clone repository

```bash
git clone https://github.com/nqdoan22/project-se.git
cd project-se
```

### 4.3 Vào thư mục mã nguồn

```bash
cd "02_Source/01_Source Code"
```

---

## 5. Cấu hình biến môi trường

### 5.1 Backend (`.env`)

Sao chép file mẫu rồi điền thông tin thực tế:

```bash
cp backend/.env.example backend/.env
```

Mở file `backend/.env` và điền các giá trị:

```dotenv
# ── Database (Aiven MySQL hoặc local MySQL) ───────────────────────────────────
DB_HOST=your-mysql-host          # Ví dụ: mysql-xxx.aivencloud.com
DB_PORT=3306
DB_NAME=inventory_management
DB_USER=your_db_user
DB_PASSWORD=your_db_password
DB_SSL_MODE=REQUIRED

DB_JDBC_URL=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=true&requireSSL=true&sslMode=REQUIRED&verifyServerCertificate=true

# ── Keycloak (khi chạy backend NGOÀI Docker, dùng localhost) ─────────────────
KEYCLOAK_ISSUER_URI=http://localhost:9090/realms/ims
KEYCLOAK_JWK_SET_URI=http://localhost:9090/realms/ims/protocol/openid-connect/certs

# ── Qdrant Cloud (AI/RAG feature) ────────────────────────────────────────────
QDRANT_HOST=your-cluster.cloud.qdrant.io
QDRANT_PORT=6333
QDRANT_USE_TLS=true
QDRANT_API_KEY=your_qdrant_api_key
QDRANT_COLLECTION=ims_rag

# ── OpenAI (embedding + chat cho RAG) ────────────────────────────────────────
OPENAI_API_KEY=sk-...
OPENAI_EMBEDDING_MODEL=text-embedding-3-small
OPENAI_CHAT_MODEL=gpt-4o-mini

# ── RAG sync worker ───────────────────────────────────────────────────────────
RAG_SYNC_ENABLED=true
RAG_SYNC_INTERVAL_MS=60000
RAG_SYNC_BATCH_SIZE=500
```

> ⚠️ File `.env` **KHÔNG** được commit lên Git. File này đã có trong `.gitignore`.

### 5.2 Frontend (`.env.local`)

Tạo file `.env.local` trong thư mục `frontend/`:

```bash
# frontend/.env.local
VITE_API_BASE=http://localhost:8080/api/v1
VITE_KEYCLOAK_URL=http://localhost:9090
```

---

## 6. Khởi tạo cơ sở dữ liệu

### 6.1 Tạo schema

Kết nối đến MySQL và chạy script tạo bảng:

```bash
mysql -h <DB_HOST> -P 3306 -u <DB_USER> -p <DB_NAME> < database/dbscript.sql
```

Script này tạo các bảng: `Users`, `Materials`, `LabelTemplates`, `InventoryLots`, `InventoryTransactions`, `ProductionBatches`, `BatchComponents`, `QCTests`.

### 6.2 Nạp dữ liệu kiểm thử (tùy chọn)

```bash
mysql -h <DB_HOST> -P 3306 -u <DB_USER> -p <DB_NAME> < database/test_data.sql
```

### 6.3 Chạy migration cho RAG (nếu dùng tính năng AI)

```bash
DB_HOST=<host> DB_PORT=3306 DB_USER=<user> DB_PASS=<password> DB_NAME=inventory_management \
  bash scripts/run-migrations.sh
```

Script này chạy lần lượt:
- `V002__add_rag_sync_indexes.sql`
- `V003__create_rag_system_tables.sql`
- `V004__create_rag_chat_sessions.sql`

---

## 7. Cách 1 — Chạy toàn bộ hệ thống bằng Docker Compose (khuyên dùng)

### 7.1 Điều kiện tiên quyết

- Docker Desktop đang chạy.
- File `backend/.env` đã được cấu hình (xem mục 5.1).

### 7.2 Build và khởi động

```bash
cd "02_Source/01_Source Code"
docker compose up --build
```

Docker Compose sẽ khởi động 3 services theo thứ tự:

1. **keycloak** (cổng `9090`) — đợi health check pass.
2. **backend** (cổng `8080`) — đợi Keycloak healthy.
3. **frontend** (cổng `3000`) — đợi backend sẵn sàng.

### 7.3 Kiểm tra hệ thống

Sau khi tất cả container khởi động thành công:

| Thành phần | URL kiểm tra |
| --- | --- |
| Frontend UI | http://localhost:3000 |
| Backend Health | http://localhost:8080/api/v1/health |
| Keycloak Admin | http://localhost:9090 (admin/admin) |

### 7.4 Dừng hệ thống

```bash
docker compose down
```

---

## 8. Cách 2 — Chạy từng thành phần riêng lẻ (phát triển)

### 8.1 Khởi động Keycloak

```bash
docker run -d \
  -p 9090:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:24.0 start-dev
```

Truy cập `http://localhost:9090`, đăng nhập admin/admin, tạo realm `ims` và client `ims-frontend`.

Chi tiết cấu hình Keycloak xem tại `03_Deployment/05_Keycloak Setup Guide.md`.

### 8.2 Biên dịch và chạy Backend

```bash
cd "02_Source/01_Source Code/backend"

# Cấp quyền thực thi Gradle Wrapper (Linux/macOS)
chmod +x gradlew

# Chạy ứng dụng (tự đọc .env)
./gradlew bootRun

# Hoặc trên Windows
gradlew.bat bootRun
```

Backend sẽ khởi động tại `http://localhost:8080`.

**Build JAR (không chạy test):**

```bash
./gradlew bootJar --no-daemon -x test
# Output: build/libs/inventory-0.0.1-SNAPSHOT.jar
```

**Chạy JAR trực tiếp:**

```bash
java -jar build/libs/inventory-0.0.1-SNAPSHOT.jar
```

### 8.3 Chạy Unit Tests (Backend)

```bash
cd "02_Source/01_Source Code/backend"

# Chạy toàn bộ unit tests service layer
./gradlew test --no-daemon --tests "com.erplite.inventory.service.*"

# Chạy tất cả tests
./gradlew test

# Xem báo cáo HTML
# Sau khi chạy xong, mở file:
# build/reports/tests/test/index.html
```

Thống kê: **137 unit tests** trên 8 service (InventoryLot, Material, ProductionBatch, QCTest, InventoryTransaction, Label, Report, User).

### 8.4 Chạy Frontend (mode phát triển)

```bash
cd "02_Source/01_Source Code/frontend"

# Cài đặt dependencies
npm ci

# Kiểm tra linting
npm run lint

# Khởi động dev server (port 5173)
npm run dev
```

Frontend sẽ khởi động tại `http://localhost:5173`.

**Build production bundle:**

```bash
npm run build
# Output: dist/
```

### 8.5 Chạy E2E Tests (Frontend)

```bash
cd "02_Source/01_Source Code/frontend"

# Cài đặt Playwright browsers (lần đầu)
npx playwright install chromium

# Chạy E2E tests
npx playwright test

# Xem báo cáo
npx playwright show-report
```

---

## 9. Truy cập hệ thống Source Control (GitHub)

### 9.1 Thông tin mời tham gia

- **URL Repository:** https://github.com/nqdoan22/project-se
- **Quyền mời:** Collaborator (với vai trò Write hoặc Admin)
- **Cách mời:**
  1. Truy cập **Settings → Collaborators and teams** trong repository.
  2. Nhấn **Add people** và nhập email/username cần mời.
  3. Chọn quyền phù hợp và gửi lời mời.

> 📷 **[Ảnh chụp hành động mời tham gia GitHub]**
> *Thay thế dòng này bằng ảnh chụp màn hình giao diện mời Collaborator trên GitHub.*

---

## 10. Truy cập hệ thống Build & Tích hợp tự động (GitHub Actions)

### 10.1 Thông tin CI/CD

Hệ thống CI/CD của nhóm sử dụng **GitHub Actions** với workflow tại `.github/workflows/ci-cd.yml`.

Luồng CI/CD:

| Trigger | Hành động |
| --- | --- |
| Push lên `develop` | Chạy CI: build backend + frontend |
| Pull Request vào `main` | Chạy CI: gate trước khi merge |
| Push lên `main` | CI + CD: deploy lên Render (production) |

Jobs trong pipeline:
- **CI — Backend:** Set up JDK 21 → Run unit tests → Build JAR → Upload test report
- **CI — Frontend:** Set up Node 20 → Install deps → Lint → Build
- **CD — Deploy:** Trigger Render deploy hooks → Poll health check

### 10.2 Xem CI/CD

- Truy cập tab **Actions** trong repository GitHub để theo dõi kết quả từng pipeline run.
- URL: `https://github.com/nqdoan22/project-se/actions`

> 📷 **[Ảnh chụp hành động mời tham gia hệ thống CI/CD]**
> *Thay thế dòng này bằng ảnh chụp màn hình giao diện GitHub Actions và/hoặc lời mời Collaborator để xem CI/CD.*

---

## 11. Video hướng dẫn biên dịch và chạy hệ thống

> 🎬 **[Link YouTube — Demo cài đặt môi trường, biên dịch và chạy mã nguồn]**
>
> *Thay thế dòng này bằng link YouTube thực tế của nhóm, ví dụ: https://youtu.be/xxxxxxxxxx*
>
> Video bao gồm:
> - Cài đặt JDK 21, Node.js 20, Docker Desktop
> - Clone repository từ GitHub
> - Cấu hình file `.env`
> - Khởi tạo database bằng `dbscript.sql`
> - Chạy toàn bộ hệ thống bằng `docker compose up --build`
> - Truy cập và đăng nhập vào Frontend UI qua Keycloak

---

## 12. Xử lý lỗi thường gặp

| Lỗi | Nguyên nhân | Giải pháp |
| --- | --- | --- |
| `DB_JDBC_URL not set` | Thiếu biến môi trường DB | Kiểm tra file `backend/.env` đã có đầy đủ |
| `Connection refused :9090` | Keycloak chưa khởi động | Đợi Keycloak health check pass hoặc chạy `docker compose up keycloak` trước |
| `gradlew: Permission denied` | Thiếu quyền thực thi | Chạy `chmod +x gradlew` |
| `npm ERR! peer dep missing` | Node version không phù hợp | Dùng Node.js 20 LTS (`nvm use 20`) |
| Port `8080` đã bị chiếm | Ứng dụng khác đang dùng | Dừng ứng dụng khác hoặc đổi port trong `docker-compose.yml` |
| JWT validation failed | Keycloak issuer URI sai | Với Docker Compose: `KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/ims`; với local: dùng `localhost:9090` |

---

## 13. Traceability với tài liệu liên quan

- Thông tin triển khai lên môi trường production: `03_Deployment/02_Deployment Guide.md`
- Hướng dẫn cấu hình Keycloak: `03_Deployment/05_Keycloak Setup Guide.md`
- Unit test coverage và chi tiết test cases: `02_Source/01_Source Code/backend/backend-unit-tests-plan.md`
- Kiến trúc hệ thống: `01_Documents/05_Architecture.md`
