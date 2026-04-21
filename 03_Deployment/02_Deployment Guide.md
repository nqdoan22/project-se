# 02 - Deployment Guide

## 1. Muc tieu

Huong dan cho IT Administrator cach trien khai IMS theo huong dong nhat voi architecture hien tai:

- Frontend
- Backend
- MySQL
- Keycloak

Tai lieu nay dung huong container deployment de de tai lap tren may moi.

## 2. Yeu cau he thong

- Docker Engine va Docker Compose.
- Git.
- Port mo:
  - 3000 hoac 5173 cho frontend (tuy cau hinh)
  - 8080 cho backend
  - 9090 cho Keycloak
  - 3306 cho MySQL

## 3. Cau truc thu muc lien quan

- 02_Source/01_Source Code/docker-compose.yml
- 02_Source/01_Source Code/backend/Dockerfile
- 02_Source/01_Source Code/frontend/Dockerfile
- 02_Source/01_Source Code/database/dbscript.sql

## 4. Chuan bi bien moi truong

Can kiem tra va cap nhat cac bien sau truoc khi deploy:

- Database credentials.
- Keycloak issuer va jwk uri.
- Frontend API base URL.
- Secret va password production.

Khuyen nghi:

- Khong commit secret that vao repository.
- Dung file .env trong moi truong server.

## 5. Trien khai bang Docker Compose

## 5.1 Build va start

Tu thu muc 02_Source/01_Source Code:

```bash
docker compose up -d --build
```

## 5.2 Kiem tra container

```bash
docker compose ps
```

## 5.3 Kiem tra log neu co loi

```bash
docker compose logs backend --tail=100
docker compose logs keycloak --tail=100
docker compose logs mysql --tail=100
docker compose logs frontend --tail=100
```

## 6. Kiem thu sau trien khai

## 6.1 Kiem tra dich vu

- Frontend truy cap duoc.
- Backend health endpoint tra ve thanh cong.
- Keycloak login page mo duoc.

## 6.2 Kiem tra API

- Goi endpoint co token va khong token.
- Endpoint can auth phai tra 401 neu thieu token.
- Endpoint role-protected phai tra 403 neu role khong hop le.

## 6.3 Kiem tra luong nghiep vu toi thieu

- Tao material.
- Receive lot.
- Tao QC test va evaluate.
- Tao batch va confirm usage.
- Truy xuat report.

## 7. Trien khai production

Huong dan production chi tiet xem tai:

- 03_Deployment/04_Production Deployment Guide.md
- 03_Deployment/05_Keycloak Setup Guide.md

## 8. Van hanh va bao tri

- Restart service:

```bash
docker compose restart backend
```

- Cap nhat image va redeploy:

```bash
docker compose pull
docker compose up -d --build
```

- Stop he thong:

```bash
docker compose down
```

- Full reset bao gom volume:

```bash
docker compose down -v
```

## 9. Video trien khai

- Link video demo deployment:
  - https://www.youtube.com/watch?v=IMS_DEPLOYMENT_VIDEO_ID
