# 01 - Deployment Package

Thư mục này chứa toàn bộ các tập tin cần thiết để triển khai hệ thống IMS lên môi trường production.

## Cấu trúc thư mục

```
01_Deployment_Package/
├── README.md                        ← File này
├── render.yaml                      ← Cấu hình Render (backend service)
├── vercel.json                      ← Cấu hình Vercel (frontend service)
├── docker-compose.prod.yml          ← Docker Compose cho môi trường production self-hosted
├── backend.env.example              ← Mẫu biến môi trường cho backend trên Render
├── frontend.env.example             ← Mẫu biến môi trường cho frontend trên Vercel
└── keycloak-realm-export.json       ← Export cấu hình Keycloak realm (dùng để import nhanh)
```

## Nền tảng triển khai

| Thành phần | Nền tảng | URL production |
| --- | --- | --- |
| Frontend (React) | **Vercel** | https://project-se.vercel.app |
| Backend (Spring Boot) | **Render** | https://project-se-24rr.onrender.com |
| Database (MySQL) | **Aiven** | *(private — xem biến môi trường)* |
| IAM (Keycloak) | **Render** | https://ims-keycloak.onrender.com |

## Hướng dẫn sử dụng

Xem chi tiết tại: `03_Deployment/02_Deployment Guide.md`
