# 04 - Source_Vibe Coding

## 1. Mục tiêu tài liệu

Tài liệu này ghi lại các **công cụ tạo sinh (AI tools)** và các **prompts chính** mà nhóm đã sử dụng để hỗ trợ tạo mới, chỉnh sửa, hoặc cải tiến các sản phẩm trong thư mục `02_Source`, bao gồm:

- `01_Source Code/backend/` — Backend Spring Boot
- `01_Source Code/frontend/` — Frontend React + Vite
- `01_Source Code/database/` — Schema và data SQL
- `01_Source Code/scripts/` — Shell scripts tiện ích
- `01_Source Code/docker-compose.yml` — Cấu hình Docker Compose

> Tất cả đầu ra từ công cụ AI đều được nhóm xem xét, kiểm tra và chỉnh sửa bởi con người trước khi đưa vào sản phẩm chính thức.

---

## 2. Công cụ sử dụng

| Công cụ | Mục đích sử dụng |
| --- | --- |
| **ChatGPT (GPT-4o)** | Tạo mã nguồn backend (entity, service, controller, repository), SQL schema, shell scripts |
| **Claude (claude-sonnet)** | Review code, refactor cấu trúc, viết unit test và tài liệu kỹ thuật |
| **GitHub Copilot** | Gợi ý code inline trong IDE (IntelliJ IDEA, VS Code) |
| **Antigravity (Google DeepMind)** | Hỗ trợ đa bước: phân tích codebase, tạo/cập nhật tài liệu, debug |
| **Cursor AI** | Chỉnh sửa nhiều file đồng thời qua chat context |

---

## 3. Backend — Spring Boot (Java 21 / Gradle)

### 3.1 Tạo cấu trúc project ban đầu

**Công cụ:** ChatGPT (GPT-4o)

**Prompt chính:**

```
Tạo một Spring Boot 4.x project với Java 21 và Gradle cho hệ thống quản lý kho vật tư theo lô (Inventory Management System). 
Tech stack:
- Spring Boot Web, Data JPA, Validation, Security, OAuth2 Resource Server
- MySQL database
- Keycloak 24 làm IAM (JWT token validation)
- Lombok

Cấu trúc package: com.erplite.inventory
Bao gồm: entity (User, Material, InventoryLot, InventoryTransaction, LabelTemplate, ProductionBatch, BatchComponent, QCTest),
controller, service, repository, dto, exception, config.

Tạo application.properties với placeholder biến môi trường cho DB và Keycloak.
Tạo file .env.example đầy đủ.
```

**Kết quả thu được:** Cấu trúc project ban đầu, `build.gradle`, `application.properties`, `.env.example`.

**Chỉnh sửa thủ công:** Điều chỉnh phiên bản Spring Boot, thêm cấu hình `bootRun` để đọc `.env`, bổ sung RAG dependencies sau.

---

### 3.2 Tạo JPA Entities

**Công cụ:** ChatGPT (GPT-4o)

**Prompt chính:**

```
Dựa vào schema SQL sau, hãy tạo các JPA entity class tương ứng bằng Java 21 + Spring Boot.
Yêu cầu:
- Dùng Lombok (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- Dùng @Entity, @Table, @Id, @GeneratedValue
- FK relationships dùng @ManyToOne, @OneToMany
- Enum fields dùng @Enumerated(EnumType.STRING)
- UUID cho primary key

[Dán schema SQL dbscript.sql vào đây]
```

**Kết quả thu được:** Các file entity: `User.java`, `Material.java`, `InventoryLot.java`, `InventoryTransaction.java`, `LabelTemplate.java`, `ProductionBatch.java`, `BatchComponent.java`, `QCTest.java`.

**Chỉnh sửa thủ công:** Bổ sung custom converter cho enum phức tạp, điều chỉnh cascade type, thêm validation annotation.

---

### 3.3 Tạo Business Logic (Service Layer)

**Công cụ:** ChatGPT (GPT-4o) + Claude

**Prompt chính:**

```
Tạo InventoryLotService với Spring Boot. Service này cần xử lý các nghiệp vụ:
1. Receive lot: tạo lot mới với status Quarantine, tạo Receipt transaction.
2. Update QC result: nếu all QCTest pass → lot Accepted; nếu có bất kỳ Fail → lot Rejected.
3. Confirm usage: chỉ cho phép nếu lot status = Accepted, quantity đủ; tạo Usage transaction âm.
4. Split sample lot: tạo lot con (is_sample=true, parent_lot_id), tạo transaction tách mẫu.
5. Adjust inventory: tạo Adjustment transaction, cập nhật quantity.

Business rules:
- BR-02: Lot chỉ được dùng cho sản xuất khi Accepted
- BR-03: Nếu bất kỳ QC test nào Fail thì lot → Rejected
- BR-04: Khi quantity = 0 → lot có thể chuyển Depleted

Seed với transaction để đảm bảo atomic.
```

**Kết quả thu được:** `InventoryLotService.java`, `ProductionBatchService.java`, `QCTestService.java` với business logic cơ bản.

**Chỉnh sửa thủ công:** Bổ sung xử lý concurrent usage (pessimistic lock), idempotency key, handling edge case khi lot đã Depleted.

---

### 3.4 Tạo REST Controllers + DTOs

**Công cụ:** ChatGPT (GPT-4o)

**Prompt chính:**

```
Tạo MaterialController và MaterialService với Spring Boot REST API.
Base path: /api/v1/materials
Endpoints:
- GET /api/v1/materials → list all, hỗ trợ filter theo material_type
- GET /api/v1/materials/{id} → get by id
- POST /api/v1/materials → create (validate part_number unique)
- PUT /api/v1/materials/{id} → update
- DELETE /api/v1/materials/{id} → delete

Tạo DTOs: MaterialRequest (input validation), MaterialResponse (output).
Dùng @Valid, @PreAuthorize("hasRole('Admin')") cho các endpoint write.
Xử lý exception với @ControllerAdvice.
```

**Kết quả thu được:** Controller và DTO framework chuẩn. Áp dụng pattern tương tự cho 8 controllers còn lại.

**Chỉnh sửa thủ công:** Điều chỉnh role mapping từ Keycloak JWT (`realm_access.roles`), thêm pagination, bổ sung response envelope.

---

### 3.5 Cấu hình Security (Keycloak + Spring Security)

**Công cụ:** ChatGPT (GPT-4o)

**Prompt chính:**

```
Cấu hình Spring Security cho Spring Boot 4.x để:
1. Tích hợp Keycloak 24 làm OAuth2 Resource Server (JWT validation).
2. Extract roles từ JWT claim "realm_access.roles" thay vì scope.
3. SecurityConfig: yêu cầu JWT cho tất cả endpoint trừ /api/v1/health.
4. DevSecurityConfig: profile "dev" — cho phép tất cả request (mock auth).
5. Dùng JwkSetUri thay vì issuer-uri vì Docker network dùng service name khác browser.

Cung cấp JwtAuthenticationConverter để map roles từ Keycloak token.
```

**Kết quả thu được:** `SecurityConfig.java`, `DevSecurityConfig.java`, custom `JwtAuthenticationConverter`.

**Chỉnh sửa thủ công:** Xử lý trường hợp token từ browser dùng `localhost:9090` nhưng backend trong Docker xác thực qua `keycloak:8080`.

---

### 3.6 Tính năng RAG (AI Addon — Qdrant + OpenAI)

**Công cụ:** Claude (claude-sonnet) + ChatGPT

**Prompt chính:**

```
Implement RAG (Retrieval-Augmented Generation) cho hệ thống IMS với:
- Qdrant Cloud làm vector database (REST API, port 6333, TLS)
- OpenAI text-embedding-3-small để embed dữ liệu inventory
- Sync worker: định kỳ (configurable interval) sync dữ liệu mới từ MySQL → Qdrant
- Chat endpoint: nhận câu hỏi tự nhiên, embed query → search Qdrant → generate answer với GPT-4o-mini

Không dùng Spring AI hay SDK nặng — chỉ dùng Spring RestClient (đã có sẵn).
Cấu trúc package: com.erplite.inventory.rag
Bao gồm: RagProperties, embed/, ingest/, query/, store/, sync/, session/, controller/, dto/
```

**Kết quả thu được:** Toàn bộ module RAG trong `src/main/java/com/erplite/inventory/rag/`.

**Chỉnh sửa thủ công:** Điều chỉnh batch size, xử lý retry khi Qdrant API rate limit, chuẩn hóa payload schema.

---

### 3.7 Unit Tests

**Công cụ:** Claude (claude-sonnet) + Antigravity

**Prompt chính:**

```
Viết unit tests cho InventoryLotService với JUnit 5 + Mockito.
Cần cover:
1. receiveLot() — success, material not found, invalid quantity
2. updateStatusFromQC() — all pass → Accepted; any fail → Rejected
3. confirmUsage() — success, lot not Accepted, insufficient quantity
4. splitSampleLot() — success, parent not Accepted
5. adjustInventory() — success, invalid adjustment

Mock tất cả repository dependencies.
Verify cả happy path và exception cases.
Tên test method theo convention: methodName_condition_expectedResult
```

**Kết quả thu được:** Framework test chuẩn. Áp dụng cho 8 service classes → **137 unit tests** tổng cộng.

**Chỉnh sửa thủ công:** Sửa mock setup cho concurrent test case, bổ sung edge case cho Depleted lot.

---

## 4. Frontend — React 19 + Vite 7

### 4.1 Setup project và cấu hình Keycloak

**Công cụ:** ChatGPT (GPT-4o)

**Prompt chính:**

```
Tạo React 19 + Vite 7 project cho IMS frontend.
Dependencies: react-router-dom v7, axios, keycloak-js v26.

Yêu cầu:
1. KeycloakProvider.jsx: wrap app, init Keycloak, tự động redirect login nếu chưa auth.
2. keycloak.js: cấu hình realm="ims", clientId="ims-frontend", đọc URL từ env VITE_KEYCLOAK_URL.
3. Axios instance trong api.js: tự động attach Authorization header từ Keycloak token, auto-refresh token khi 401.
4. Protected routes: chỉ render khi đã authenticated.

Keycloak flow: PKCE + Implicit disabled.
```

**Kết quả thu được:** `auth/keycloak.js`, `auth/KeycloakProvider.jsx`, `services/api.js`.

**Chỉnh sửa thủ công:** Xử lý race condition khi Keycloak chưa init xong, thêm loading spinner, xử lý silent refresh.

---

### 4.2 Tạo API client methods

**Công cụ:** ChatGPT (GPT-4o)

**Prompt chính:**

```
Dựa vào danh sách REST endpoints của backend IMS, tạo các API client functions trong services/api.js:
- materialApi: list (filter by type), getById, create, update, delete
- lotApi: list (filter by status/material), getById, receive, updateStatus, split, adjust
- qcTestApi: getByLot, create, update result
- productionBatchApi: list, getById, create, addComponent, confirmComponent, updateStatus
- labelApi: listTemplates, generateLotLabel, generateBatchLabel
- reportApi: dashboard, nearExpiry, lotTrace, qcSummary
- userApi: list, getById, create, update, deactivate

Dùng axios instance đã có (tự động attach JWT).
```

**Kết quả thu được:** Toàn bộ `services/api.js` với đầy đủ API methods.

**Chỉnh sửa thủ công:** Điều chỉnh endpoint path, xử lý query params, thêm error handling.

---

### 4.3 Các trang UI chính

**Công cụ:** ChatGPT (GPT-4o) + GitHub Copilot

**Prompt chính (ví dụ cho trang Lots):**

```
Tạo trang LotsPage cho React với các tính năng:
- Danh sách inventory lots với filter theo status (Quarantine/Accepted/Rejected/Depleted) và material.
- Hiển thị badge màu theo status.
- Modal tạo lot mới (receive lot form).
- Modal xem chi tiết lot: thông tin lot, danh sách QC tests, lịch sử transactions.
- Nút tách sample lot.
- Responsive table với pagination.

Dùng useState, useEffect, axios từ api.js.
Style bằng CSS module hoặc inline style (không dùng Tailwind vì dự án dùng Tailwind CLI riêng).
```

**Kết quả thu được:** Các trang: `Dashboard`, `LotsPage`, `MaterialsPage`, `QCTestsPage`, `BatchesPage`, `BatchComponentsPage`, `LabelsPage`, `UsersPage`.

**Chỉnh sửa thủ công:** Điều chỉnh layout responsive, sửa state management phức tạp (modal + form + list refresh), xử lý loading/error states.

---

### 4.4 E2E Tests với Playwright

**Công cụ:** Claude (claude-sonnet)

**Prompt chính:**

```
Tạo Playwright e2e tests cho IMS frontend.
Config: baseURL=http://localhost:5173, browser=chromium, timeout=30s.
Test cases cho luồng chính:
1. Login qua Keycloak redirect → về dashboard.
2. Tạo material mới → hiển thị trong danh sách.
3. Receive lot → lot xuất hiện với status Quarantine.
4. Nhập QC test → lot chuyển Accepted.

Dùng page.goto, page.fill, page.click, page.waitForURL.
```

**Kết quả thu được:** `playwright.config.js`, thư mục `e2e/` với test files.

**Chỉnh sửa thủ công:** Xử lý Keycloak redirect flow trong test môi trường, thêm wait conditions.

---

## 5. Database — SQL Schema & Data

### 5.1 Thiết kế schema

**Công cụ:** ChatGPT (GPT-4o)

**Prompt chính:**

```
Thiết kế MySQL schema cho IMS với các bảng:
Users, Materials, LabelTemplates, InventoryLots, InventoryTransactions, 
ProductionBatches, BatchComponents, QCTests.

Yêu cầu:
- PK dạng VARCHAR(36) (UUID)
- ENUM cho status fields
- FK constraints đầy đủ
- created_date, modified_date với DEFAULT CURRENT_TIMESTAMP ON UPDATE
- Index cho FK columns và filter columns thường dùng

Đảm bảo nhất quán với domain model trong tài liệu PRD.
```

**Kết quả thu được:** `database/dbscript.sql` với đầy đủ 8 bảng, constraints, indexes.

**Chỉnh sửa thủ công:** Điều chỉnh ENUM values cho phù hợp nghiệp vụ thực tế, bổ sung NULL/NOT NULL constraints.

### 5.2 Migration scripts cho RAG

**Công cụ:** ChatGPT (GPT-4o)

**Prompt chính:**

```
Tạo SQL migration files cho RAG feature:
- V002: thêm indexes để tối ưu RAG sync query
- V003: tạo bảng rag_sync_state (theo dõi record đã sync chưa)
- V004: tạo bảng rag_chat_session (lưu lịch sử chat RAG)

Naming convention: V00X__description.sql
```

**Kết quả thu được:** `database/migrations/V002__add_rag_sync_indexes.sql`, `V003__create_rag_system_tables.sql`, `V004__create_rag_chat_sessions.sql`.

---

## 6. Scripts — Shell Utilities

### 6.1 Script khởi tạo Qdrant Collection

**Công cụ:** ChatGPT (GPT-4o)

**Prompt chính:**

```
Viết bash script init-qdrant.sh để:
1. Tạo Qdrant collection "ims_rag" với 1536 dimensions (OpenAI embedding), Cosine distance.
2. Tạo payload indexes cho các fields: source_table, source_pk, lot_status, result_status, 
   material_part_number, material_id, batch_status, transaction_type.
3. Dùng Qdrant REST API (PUT /collections/{name}).
4. Đọc QDRANT_HOST, QDRANT_API_KEY từ environment variables.
5. Idempotent: chạy lại an toàn (PUT là idempotent).

Thêm error handling với set -euo pipefail.
```

**Kết quả thu được:** `scripts/init-qdrant.sh`.

**Chỉnh sửa thủ công:** Xử lý lỗi 4xx khi index đã tồn tại (dùng `|| true`), thêm verification step cuối.

### 6.2 Script chạy Database Migrations

**Công cụ:** ChatGPT (GPT-4o)

**Prompt chính:**

```
Viết bash script run-migrations.sh để chạy tuần tự các SQL migration files 
(V002, V003, V004) vào MySQL database.
Đọc DB_HOST, DB_PORT, DB_USER, DB_PASS, DB_NAME từ environment.
Dùng mysql client với --ssl-mode=REQUIRED.
Thêm verification query sau khi chạy xong.
Error handling với set -euo pipefail, kiểm tra mysql client có sẵn.
```

**Kết quả thu được:** `scripts/run-migrations.sh`.

---

## 7. Docker & CI/CD

### 7.1 Docker Compose

**Công cụ:** ChatGPT (GPT-4o)

**Prompt chính:**

```
Tạo docker-compose.yml cho IMS với 3 services:
1. keycloak: image quay.io/keycloak/keycloak:24.0, port 9090:8080, health check.
2. backend: build từ ./backend, đọc .env file, override Keycloak URIs để dùng Docker service name 
   (không dùng localhost bên trong container).
3. frontend: build từ ./frontend với VITE_API_BASE và VITE_KEYCLOAK_URL args, port 3000:80.

Dependencies: backend depends_on keycloak (healthy), frontend depends_on backend.
```

**Kết quả thu được:** `docker-compose.yml` đầy đủ với healthcheck và dependency ordering.

**Chỉnh sửa thủ công:** Giải quyết vấn đề JWT issuer mismatch (browser dùng `localhost:9090` nhưng backend validate qua `keycloak:8080`) bằng cách disable issuer validation, chỉ giữ JWK set URI validation.

### 7.2 GitHub Actions CI/CD

**Công cụ:** Claude (claude-sonnet) + Antigravity

**Prompt chính:**

```
Tạo GitHub Actions workflow ci-cd.yml cho IMS:
1. Trigger: push develop (CI only), PR vào main (CI gate), push main (CI + CD).
2. Job backend CI: setup Java 21 Temurin, cache Gradle, chạy unit tests service layer, 
   build JAR, upload test report artifact.
3. Job frontend CI: setup Node 20, cache npm, npm ci, lint, build với dummy env vars.
4. Job deploy (chỉ khi push main và cả 2 CI pass):
   - Trigger Render deploy hooks qua secrets
   - Poll backend health check endpoint tối đa 10 phút
   - Deployment summary

Sử dụng GitHub environment "production" cho CD job.
```

**Kết quả thu được:** `.github/workflows/ci-cd.yml` với 3 jobs đầy đủ.

**Chỉnh sửa thủ công:** Điều chỉnh working-directory paths (vì repo có cấu trúc nhiều lớp), thêm logic retry cho health check, xử lý Render cold start delays.

---

## 8. Tổng kết quá trình Vibe Coding

| Thành phần | % AI-generated | % Chỉnh sửa thủ công | Ghi chú |
| --- | --- | --- | --- |
| Entity classes | 80% | 20% | Chủ yếu sửa enum, cascade |
| Service layer business logic | 60% | 40% | Concurrent handling, edge cases |
| REST Controllers + DTO | 75% | 25% | Role mapping, pagination |
| Security config | 50% | 50% | Docker/Keycloak URI mismatch |
| RAG module | 55% | 45% | Tự thiết kế architecture |
| Frontend UI pages | 65% | 35% | State management, styling |
| SQL schema | 70% | 30% | Business rules, indexes |
| Docker/CI/CD | 60% | 40% | Path configs, secret management |
| Unit tests | 70% | 30% | Edge case coverage |
| Shell scripts | 80% | 20% | Error handling, idempotency |

> **Nguyên tắc của nhóm:** AI tạo khung ban đầu và gợi ý giải pháp — nhóm chịu trách nhiệm review, kiểm tra tính đúng đắn với nghiệp vụ, chạy thử và chỉnh sửa thủ công trước khi merge vào nhánh chính.
