# Production Deployment Guide — Backend + Keycloak

This guide covers deploying the IMS backend and Keycloak together on a single production server using Docker Compose.

---

## Architecture Overview

```
Browser (user)
    │
    ├──► Keycloak  :9090   (authentication — issues JWT tokens)
    ├──► Backend   :8080   (API — validates JWT tokens)
    └──► Frontend  :5173   (or :80 via nginx)

Internal Docker network:
    backend ──► keycloak:8080  (fetch public keys for JWT validation)
    backend ──► mysql:3306     (app database)
    keycloak ──► postgres:5432 (Keycloak's own database)
```

---

## Step 1: Create Production `docker-compose.yml`

Replace the existing `docker-compose.yml` with the following full production version:

```yaml
services:
  # ── Keycloak database ────────────────────────────────────────────────────────
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: keycloak_pass
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - internal

  # ── Keycloak ─────────────────────────────────────────────────────────────────
  keycloak:
    image: quay.io/keycloak/keycloak:24.0
    command: start
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
      KC_DB_USERNAME: keycloak
      KC_DB_PASSWORD: keycloak_pass
      KC_HOSTNAME: localhost # change to your domain in real prod
      KC_HTTP_ENABLED: "true" # set to false if using HTTPS
      KC_HOSTNAME_STRICT: "false"
      KC_HOSTNAME_STRICT_HTTPS: "false"
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin # CHANGE THIS in real production
    ports:
      - "9090:8080"
    depends_on:
      - postgres
    networks:
      - internal
      - external

  # ── App database ─────────────────────────────────────────────────────────────
  mysql:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: 3333
      MYSQL_DATABASE: inventory_management
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/dbscript.sql:/docker-entrypoint-initdb.d/01_schema.sql
      - ./database/test_data.sql:/docker-entrypoint-initdb.d/02_data.sql
    ports:
      - "3306:3306"
    networks:
      - internal

  # ── Backend ──────────────────────────────────────────────────────────────────
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/inventory_management
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: 3333
      # External URL — must match what's inside JWT tokens (what browser/frontend uses)
      KEYCLOAK_ISSUER_URI: http://localhost:9090/realms/ims
      # Internal URL — used by backend to fetch Keycloak public keys (Docker network)
      KEYCLOAK_JWK_URI: http://keycloak:8080/realms/ims/protocol/openid-connect/certs
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - keycloak
    networks:
      - internal
      - external

volumes:
  postgres_data:
  mysql_data:

networks:
  internal:
  external:
```

> **Key point:** `KC_HOSTNAME: localhost` must match the URL the browser uses to reach Keycloak. The JWT `iss` claim will contain this hostname. The backend `KEYCLOAK_ISSUER_URI` must match it exactly.

---

## Step 2: Create Backend `Dockerfile`

Create `backend/Dockerfile`:

```dockerfile
# ── Build stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test

# ── Run stage ─────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Step 3: Update `application.properties` for Production Profile

Create `backend/src/main/resources/application-prod.properties`:

```properties
# Server
server.port=8080

# MySQL (overridden by Docker env vars)
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

# Keycloak JWT
# issuer-uri: must match the JWT iss claim (external URL the browser uses)
spring.security.oauth2.resourceserver.jwt.issuer-uri=${KEYCLOAK_ISSUER_URI}
# jwk-set-uri: used internally to fetch public keys (Docker internal network)
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${KEYCLOAK_JWK_URI}
```

---

## Step 4: Deploy

### 4a. Build and start all services

```bash
cd "02_Source/01_Source Code"
docker compose up -d --build
```

Check all containers are running:

```bash
docker compose ps
```

Expected output:

```
NAME         STATUS
postgres     Up
keycloak     Up
mysql        Up
backend      Up
```

### 4b. Check logs if something is wrong

```bash
docker compose logs keycloak --tail=50
docker compose logs backend --tail=50
docker compose logs mysql --tail=50
```

---

## Step 5: Configure Keycloak (first-time only)

Once Keycloak is running, follow `05_Keycloak Setup Guide.md` to:

1. Create realm `ims`
2. Create client `ims-frontend`
3. Create 5 realm roles
4. Create 5 users

Keycloak data is now stored in **PostgreSQL** (`postgres_data` volume) — it persists across container restarts.

---

## Step 6: Verify Backend + Keycloak Work Together

### 6a. Check Keycloak is reachable

```bash
curl http://localhost:9090/realms/ims/.well-known/openid-configuration
```

Should return JSON with `issuer`, `token_endpoint`, `jwks_uri` etc.

### 6b. Get a test token from Keycloak

```bash
curl -s -X POST http://localhost:9090/realms/ims/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ims-frontend" \
  -d "username=admin" \
  -d "password=Admin@123" | jq .
```

Copy the `access_token` value from the response.

### 6c. Call the backend API with the token

```bash
TOKEN="<paste access_token here>"

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/batches
```

Expected: **200 OK** with JSON data.

### 6d. Call without token — should be rejected

```bash
curl http://localhost:8080/api/v1/batches
```

Expected: **401 Unauthorized**

### 6e. Test role-based access

Get a token for `operator1` (Operator role) and try a protected endpoint:

```bash
TOKEN=$(curl -s -X POST http://localhost:9090/realms/ims/protocol/openid-connect/token \
  -d "grant_type=password&client_id=ims-frontend&username=operator1&password=Admin@123" \
  | jq -r .access_token)

# Should work for read endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/batches

# Should return 403 for endpoints restricted to Manager/Admin
curl -s -o /dev/null -w "%{http_code}" \
  -X POST http://localhost:8080/api/v1/batches \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

## Common Issues

| Problem                          | Cause                                        | Fix                                                                          |
| -------------------------------- | -------------------------------------------- | ---------------------------------------------------------------------------- |
| Backend fails to start           | Keycloak not ready yet                       | Add `restart: on-failure` to backend service or wait and retry               |
| `401` on all requests            | Wrong `issuer-uri` — JWT `iss` doesn't match | Make sure `KC_HOSTNAME` in Keycloak matches `KEYCLOAK_ISSUER_URI` in backend |
| `403` for valid user             | Role not assigned in Keycloak                | Go to Keycloak admin → Users → Role mapping                                  |
| Keycloak data lost on restart    | Using `start-dev` (in-memory DB)             | Use `start` command with Postgres volume (this guide already does this)      |
| Backend can't reach Keycloak JWK | Wrong internal URL                           | Use `http://keycloak:8080` (Docker service name), not `localhost`            |

---

## Stopping and Restarting

```bash
# Stop all services (data is preserved in volumes)
docker compose down

# Stop and DELETE all data (full reset)
docker compose down -v

# Restart only the backend (after code changes)
docker compose up -d --build backend
```
