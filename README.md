# Inventory Management System (IMS)

A web-based application for pharmaceutical and manufacturing environments. It manages the full lifecycle of raw materials — from receipt and quality control through production batching and finished product labeling.

**Tech stack:** React + Tailwind CSS · Java Spring Boot · MySQL · Keycloak (OIDC/RBAC) · Docker Compose

---

## Prerequisites

1. **Java Development Kit (JDK) 17+**
   - Download from [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) or [OpenJDK](https://openjdk.org/)
   - Verify: `java -version`

2. **Node.js (LTS) and npm**
   - Download from [nodejs.org](https://nodejs.org/)
   - Verify: `node -v` and `npm -v`

3. **Gradle**
   - Install from [gradle.org](https://gradle.org/install/)
   - Verify: `gradle -v`

4. **Git**
   - Download from [git-scm.com](https://git-scm.com/)
   - Verify: `git --version`

---

## Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd 02_Source/01_Source\ Code/backend
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Start the backend server:
   ```bash
   ./gradlew bootRun
   ```

---

## Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd 02_Source/01_Source\ Code/frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

---

## Database Setup

1. Ensure a MySQL server is installed and running.
2. Use the provided SQL scripts in `02_Source/01_Source Code/database/` to set up the schema and seed data:
   - `dbscript.sql` — database schema
   - `test_data.sql` — seed / test data
3. Update the database connection details in `backend/src/main/resources/application.properties`.

---

## Further Reading

- **Deployment (Docker Compose):** `03_Deployment/04_Production Deployment Guide.md`
- **Keycloak configuration:** `03_Deployment/05_Keycloak Setup Guide.md`
- **User guide:** `03_Deployment/03_User Guide.md`
- **Project description:** `01_Documents/PROJECT_DESCRIPTION.txt`
