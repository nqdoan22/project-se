# Project: Inventory Management System (IMS)

## Tech Stack
- **Backend:** Spring Boot 4.0.2, Java 21, Gradle 8.14
- **Frontend:** React 19, Vite 7, Tailwind CSS, Axios
- **Database:** MySQL (`inventory_management`)
- **Auth:** Keycloak 24.0 (OAuth2 + JWT) — used by BOTH backend and frontend
- **Deploy:** Docker Compose, Railway (production)

## Project Root
`/home/xanh/project-se`

## Source Code Paths

### Backend
- **Root:** `02_Source/01_Source Code/backend`
- **Main package:** `com.erplite.inventory`
- **Source:** `02_Source/01_Source Code/backend/src/main/java/com/erplite/inventory/`
  - `config/` — SecurityConfig.java, DevSecurityConfig.java
  - `controller/` — REST controllers (MaterialController, InventoryLotController, LabelController, QCTestController, ProductionBatchController, ReportController, UserController, HealthController)
  - `service/` — Business logic
  - `entity/` — JPA entities (User, Material, InventoryLot, InventoryTransaction, LabelTemplate, ProductionBatch, QCTest, BatchComponent)
  - `repository/` — Spring Data JPA repositories
  - `dto/` — DTOs organized by feature (batch/, common/, label/, lot/, material/, qc/, report/, transaction/, user/)
  - `exception/` — Exception handling
  - `converter/` — Type converters

### Frontend
- **Root:** `02_Source/01_Source Code/frontend`
- **Source:** `02_Source/01_Source Code/frontend/src/`
  - `auth/keycloak.js` — Keycloak instance config (realm: `ims`, client: `ims-frontend`)
  - `auth/KeycloakProvider.jsx` — React auth provider
  - `services/api.js` — Axios instance + all API client methods (materialApi, lotApi, qcTestApi, productionBatchApi, labelApi, reportApi, userApi)
  - `components/` — Reusable components (Modal, Sidebar, StatusBadge)
  - `features/material/` — Material feature module
  - `pages/` — Page components (Dashboard, Batches, Lots, Materials, QCTests, Labels, Users, BatchComponents)

### Database
- **Root:** `02_Source/01_Source Code/database`
- `dbscript.sql` — Schema creation
- `test_data.sql` — Test data

## Configuration Files

### Backend Config
- `02_Source/01_Source Code/backend/src/main/resources/application.properties` — Spring Boot config (DB, JPA, Keycloak JWT)
- `02_Source/01_Source Code/backend/.env` — Environment variables (DB credentials, Keycloak URIs) — NOT committed
- `02_Source/01_Source Code/backend/.env.example` — Env template
- `02_Source/01_Source Code/backend/build.gradle` — Dependencies

### Frontend Config
- `02_Source/01_Source Code/frontend/.env.local` — Dev environment (Keycloak URL, API base)
- `02_Source/01_Source Code/frontend/.env.production` — Production environment
- `02_Source/01_Source Code/frontend/package.json` — Dependencies
- `02_Source/01_Source Code/frontend/vite.config.js` — Vite config
- `02_Source/01_Source Code/frontend/nginx.conf` — Nginx config for Docker

### Keycloak Config
- **Realm:** `ims`
- **Frontend Client ID:** `ims-frontend`
- **Dev URL:** `http://localhost:9090`
- **Prod URL:** `https://keycloak-production-841b.up.railway.app`
- Backend validates JWT via `KEYCLOAK_ISSUER_URI` and `KEYCLOAK_JWK_SET_URI` env vars
- Frontend uses `keycloak-js` library for login/token management
- JWT roles extracted from `realm_access.roles` claim

### Docker
- `02_Source/01_Source Code/docker-compose.yml` — Keycloak (9090), Backend (8080), Frontend (3000)
- `02_Source/01_Source Code/backend/Dockerfile`
- `02_Source/01_Source Code/frontend/Dockerfile`

## API
- **Base path:** `/api/v1`
- **Public endpoints:** `/api/v1/health`
- **All other endpoints:** require JWT authentication

## Security Notes
- Backend: OAuth2 Resource Server validates Keycloak JWT tokens (SecurityConfig.java)
- Dev profile: mock auth with all routes permitted (DevSecurityConfig.java, `spring.profiles.active=dev`)
- Frontend: Axios interceptor attaches JWT token, auto-refreshes on 401

## Documentation
- `01_Documents/PROJECT_DESCRIPTION.txt`
- `03_Deployment/02_Deployment Guide.md`
- `03_Deployment/04_Production Deployment Guide.md`
- `03_Deployment/05_Keycloak Setup Guide.md`
