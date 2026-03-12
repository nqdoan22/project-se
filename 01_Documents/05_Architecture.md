# 05 – Architecture

**Hệ thống:** Inventory Management System (IMS)
**Phiên bản:** 1.1
**Ngày cập nhật:** 12/03/2026

---

## 1. Logical View

![Logical Architecture](./images/architecture.png)

### Mô tả các tầng

| Tầng         | Công nghệ                                 | Vai trò                                                   |
| ------------ | ----------------------------------------- | --------------------------------------------------------- |
| **Frontend** | React + Vite + Tailwind CSS               | SPA chạy trên browser; giao tiếp với Backend qua REST API |
| **Backend**  | Java 21 · Spring Boot 3 · Spring Data JPA | Xử lý business logic, expose REST API                     |
| **Database** | MySQL 8.0                                 | Lưu trữ toàn bộ dữ liệu (8 bảng, xem `dbscript.sql`)      |

---

## 2. Development View

### Cấu trúc thư mục gốc

```
project-se/
├── 01_Documents/           # Tài liệu dự án
├── 02_Source/
│   └── 01_Source Code/
│       ├── backend/        # Spring Boot (Gradle) project
│       ├── frontend/       # React + Vite project
│       └── dbscript.sql    # Schema DB chuẩn (source of truth)
└── 03_Deployment/
```

---

### Frontend (`frontend/`)

```
frontend/
├── index.html
├── vite.config.js
├── package.json
└── src/
    ├── main.jsx            # Entry point
    ├── App.jsx             # Routes chính
    ├── assets/
    ├── components/         # Shared UI components (Modal, Sidebar, StatusBadge…)
    ├── pages/              # Page-level components theo module
    │   ├── DashboardPage.jsx
    │   ├── MaterialsPage.jsx
    │   ├── LotsPage.jsx
    │   ├── BatchesPage.jsx
    │   ├── QCTestsPage.jsx
    │   ├── LabelsPage.jsx
    │   └── UsersPage.jsx
    └── services/
        └── api.js          # Axios client gọi Backend REST API
```

**Dependencies chính:** React, React Router, Axios, Tailwind CSS.

---

### Backend (`backend/`)

```
backend/
├── build.gradle            # Dependencies & build config
├── settings.gradle
├── gradlew / gradlew.bat   # Gradle wrapper
└── src/
    └── main/
        ├── java/com/erplite/inventory/
        │   ├── InventoryApplication.java   # @SpringBootApplication
        │   ├── controller/                 # REST Controllers
        │   ├── dto/                        # Request/Response DTOs
        │   ├── entity/                     # JPA Entities (8 bảng)
        │   ├── exception/                  # Custom exceptions & handler
        │   ├── repository/                 # JpaRepository interfaces
        │   └── service/                    # Business logic
        └── resources/
            └── application.properties      # DB config, server port
```

**Dependencies chính:** Spring Boot 3, Spring Data JPA, MySQL Connector, Lombok, Spring Validation.  
**Build tool:** Gradle (Wrapper included — không dùng Maven/pom.xml).

---

## 3. Cấu hình chính

### `application.properties`

```properties
spring.application.name=inventory
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/inventory_management
spring.datasource.username=root
spring.datasource.password=<your_password>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> **Lưu ý:** Đặt `ddl-auto=validate` sau khi schema ổn định để tránh thay đổi schema không mong muốn.

---

## 4. API Convention

- **Base URL (dev):** `http://localhost:8080`
- **Prefix:** `/api/v1/`
- **Format:** JSON (`Content-Type: application/json`)
- **Auth:** JWT Bearer Token (xem `12_Backend API Specification.md`)

| Module             | Path prefix                       |
| ------------------ | --------------------------------- |
| Materials          | `/api/v1/materials`               |
| Inventory Lots     | `/api/v1/lots`                    |
| Transactions       | `/api/v1/transactions`            |
| QC Tests           | `/api/v1/qctests`                 |
| Production Batches | `/api/v1/batches`                 |
| Batch Components   | `/api/v1/batches/{id}/components` |
| Labels             | `/api/v1/labels`                  |
| Users              | `/api/v1/users`                   |

---

## 5. Luồng dữ liệu (Data Flow)

```
Browser (React SPA)
    │  HTTP REST (JSON)
    ▼
Spring Boot Backend (port 8080)
    │  JDBC / JPA
    ▼
MySQL Database (port 3306)
  └── inventory_management
        ├── Users
        ├── Materials
        ├── LabelTemplates
        ├── InventoryLots
        ├── InventoryTransactions
        ├── ProductionBatches
        ├── BatchComponents
        └── QCTests
```
