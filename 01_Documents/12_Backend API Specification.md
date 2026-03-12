# 12 – Backend API Specification

**System:** Inventory Management System (IMS)
**Version:** 1.1 (cập nhật 12/03/2026 — đồng bộ theo `dbscript.sql`)
**Date:** 10/03/2026
**Base URL:** `http://localhost:8080` (development) · `https://api.your-domain.com` (production)

---

## Table of Contents

1. [General Conventions](#1-general-conventions)
2. [Authentication & Authorization](#2-authentication--authorization)
3. [Standard Error Responses](#3-standard-error-responses)
4. [Module 1 – Material Management](#4-module-1--material-management)
5. [Module 2 – Inventory Lots](#5-module-2--inventory-lots)
6. [Module 3 – Inventory Transactions](#6-module-3--inventory-transactions)
7. [Module 4 – QC Testing](#7-module-4--qc-testing)
8. [Module 5 – Production Batches](#8-module-5--production-batches)
9. [Module 6 – Label Management](#9-module-6--label-management)
10. [Module 7 – Reports & Dashboard](#10-module-7--reports--dashboard)
11. [Module 8 – User Management](#11-module-8--user-management)
12. [Cross-Cutting Concerns](#12-cross-cutting-concerns)
13. [Role–Permission Matrix](#13-rolenpermission-matrix)

---

## 1. General Conventions

### 1.1 URL Structure

```
/api/v1/{module}/{resource}
```

All endpoints are prefixed with `/api/v1`. The existing codebase uses `/api/` without versioning — this spec adopts `/api/v1/` for all new and refactored endpoints going forward.

| Segment    | Example                        | Description                         |
| ---------- | ------------------------------ | ----------------------------------- |
| `/api/v1`  | `/api/v1`                      | Fixed prefix + version              |
| `{module}` | `materials`, `lots`, `batches` | Module/resource collection          |
| `{id}`     | `/{uuid}`                      | Specific resource identifier (UUID) |
| `{sub}`    | `/transactions`, `/components` | Sub-resource collection             |

### 1.2 HTTP Methods

| Method   | Semantics                                                 |
| -------- | --------------------------------------------------------- |
| `GET`    | Read — idempotent, no side effects                        |
| `POST`   | Create a new resource                                     |
| `PUT`    | Full replacement of an existing resource                  |
| `PATCH`  | Partial update (status transitions, single-field changes) |
| `DELETE` | Remove a resource (hard or soft delete)                   |

### 1.3 Date & Number Formats

| Type     | Format                                  | Example                 |
| -------- | --------------------------------------- | ----------------------- |
| Date     | `YYYY-MM-DD` (ISO 8601)                 | `"2026-03-10"`          |
| DateTime | `YYYY-MM-DDTHH:mm:ss` (ISO 8601, UTC)   | `"2026-03-10T14:30:00"` |
| Decimal  | JSON number with up to 3 decimal places | `25.500`                |
| UUID     | RFC 4122 lowercase hyphenated string    | `"a1b2c3d4-..."`        |

### 1.4 Pagination (list endpoints)

All collection endpoints support optional pagination via query parameters:

| Parameter | Type    | Default            | Description              |
| --------- | ------- | ------------------ | ------------------------ |
| `page`    | integer | `0`                | Zero-based page index    |
| `size`    | integer | `20`               | Items per page (max 100) |
| `sort`    | string  | `createdDate,desc` | Field + direction        |

**Paginated response wrapper:**

```json
{
  "content": [
    /* array of items */
  ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

> Endpoints that are unlikely to return large result sets (e.g. `/qctests?lotId=`) may return a plain array without pagination.

### 1.5 Content Type

All requests and responses use `Content-Type: application/json`.

---

## 2. Authentication & Authorization

### 2.1 Overview

Authentication is handled by **Keycloak** (OAuth 2.0 / OpenID Connect). The backend validates JWT access tokens on every request.

```
Client → Keycloak  : POST /realms/ims/protocol/openid-connect/token
Keycloak → Client  : { access_token, refresh_token, expires_in }
Client → Backend   : Authorization: Bearer <access_token>
Backend → Keycloak : Validate token (via public key / introspection)
Backend → Client   : Resource response (or 401/403)
```

### 2.2 Login

**Not handled by the IMS backend.** Clients authenticate directly against Keycloak.

```
POST https://keycloak-host/realms/ims/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
&client_id=ims-frontend
&username=jdoe
&password=secret
```

**Response:**

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiJ9...",
  "expires_in": 300,
  "refresh_token": "eyJhbGciOiJSUzI1NiJ9...",
  "token_type": "Bearer"
}
```

### 2.3 Token Refresh

```
POST https://keycloak-host/realms/ims/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token
&client_id=ims-frontend
&refresh_token=<refresh_token>
```

### 2.4 Using Tokens with the Backend

Every API call (except health-check) must include:

```
Authorization: Bearer <access_token>
```

### 2.5 JWT Claims Used by the Backend

The backend extracts the following claims from the JWT:

| Claim                | Description                         | Example                |
| -------------------- | ----------------------------------- | ---------------------- |
| `sub`                | User UUID (maps to `Users.user_id`) | `"a1b2-..."`           |
| `preferred_username` | Username (used as `performed_by`)   | `"jdoe"`               |
| `email`              | User email                          | `"j.doe@company.com"`  |
| `realm_access.roles` | Array of Keycloak realm roles       | `["InventoryManager"]` |

### 2.6 User Roles

| Role               | Description                                          |
| ------------------ | ---------------------------------------------------- |
| `Admin`            | Full access to all modules including user management |
| `InventoryManager` | Manage materials, receive lots, perform transactions |
| `QualityControl`   | Create and record QC tests, update lot status via QC |
| `Production`       | Plan and manage production batches                   |
| `Viewer`           | Read-only access to all non-sensitive data           |

### 2.7 Error Responses for Auth Failures

| Scenario               | HTTP Code          | Error Body                                                          |
| ---------------------- | ------------------ | ------------------------------------------------------------------- |
| No token provided      | `401 Unauthorized` | `{ "error": "Unauthorized", "message": "Authentication required" }` |
| Expired token          | `401 Unauthorized` | `{ "error": "Unauthorized", "message": "Token expired" }`           |
| Invalid/tampered token | `401 Unauthorized` | `{ "error": "Unauthorized", "message": "Invalid token" }`           |
| Role insufficient      | `403 Forbidden`    | `{ "error": "Forbidden", "message": "Insufficient permissions" }`   |

---

## 3. Standard Error Responses

All errors follow a consistent envelope:

```json
{
  "timestamp": "2026-03-10T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Human-readable description of the problem"
}
```

**Validation error** (HTTP `422 Unprocessable Entity`):

```json
{
  "timestamp": "2026-03-10T14:30:00",
  "status": 422,
  "error": "Validation Failed",
  "details": {
    "partNumber": "Part number is required",
    "quantity": "Quantity must be greater than 0"
  }
}
```

### Standard HTTP Status Codes

| Code                        | Meaning                                 | When Used                           |
| --------------------------- | --------------------------------------- | ----------------------------------- |
| `200 OK`                    | Success                                 | GET, PUT, PATCH                     |
| `201 Created`               | Resource created                        | POST                                |
| `204 No Content`            | Success, no body                        | DELETE                              |
| `400 Bad Request`           | Invalid input / business rule violation | Business exceptions                 |
| `401 Unauthorized`          | Not authenticated                       | Missing/expired JWT                 |
| `403 Forbidden`             | Not authorized                          | Wrong role                          |
| `404 Not Found`             | Resource does not exist                 | `ResourceNotFoundException`         |
| `409 Conflict`              | Duplicate unique field                  | Duplicate part_number, batch_number |
| `422 Unprocessable Entity`  | Bean validation failed                  | `@NotBlank`, `@Size` violations     |
| `500 Internal Server Error` | Unexpected failure                      | Unhandled exceptions                |

---

## 4. Module 1 – Material Management

**Base path:** `/api/v1/materials`

Materials are the master catalog of all raw ingredients, APIs, excipients, and products. Every inventory lot and production batch references a material record.

---

### 4.1 `GET /api/v1/materials`

**Description:** Retrieve all materials with optional keyword search and type filter.

**Required roles:** `Admin`, `InventoryManager`, `QualityControl`, `Production`, `Viewer`

**Query Parameters:**

| Parameter | Type    | Required | Description                                                                                                                                |
| --------- | ------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| `keyword` | string  | No       | Search by `materialName` (case-insensitive contains)                                                                                       |
| `type`    | enum    | No       | Filter by `materialType`. Values: `API`, `Excipient`, `Dietary Supplement`, `Container`, `Closure`, `Process Chemical`, `Testing Material` |
| `page`    | integer | No       | Default `0`                                                                                                                                |
| `size`    | integer | No       | Default `20`                                                                                                                               |
| `sort`    | string  | No       | Default `createdDate,desc`                                                                                                                 |

**Response `200 OK`:**

```json
{
  "content": [
    {
      "materialId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "partNumber": "MAT-001",
      "materialName": "Vitamin D3 100K",
      "materialType": "API",
      "storageConditions": "Store at 2–8°C, protect from light",
      "specificationDocument": "https://docs.company.com/specs/MAT-001.pdf",
      "createdDate": "2026-03-01T09:00:00",
      "modifiedDate": "2026-03-05T11:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 4.2 `GET /api/v1/materials/{id}`

**Description:** Retrieve a single material by UUID.

**Required roles:** All roles

**Path Parameters:**

| Parameter | Type | Description   |
| --------- | ---- | ------------- |
| `id`      | UUID | Material UUID |

**Response `200 OK`:**

```json
{
  "materialId": "a1b2c3d4-...",
  "partNumber": "MAT-001",
  "materialName": "Vitamin D3 100K",
  "materialType": "API",
  "storageConditions": "Store at 2–8°C, protect from light",
  "specificationDocument": "https://docs.company.com/specs/MAT-001.pdf",
  "createdDate": "2026-03-01T09:00:00",
  "modifiedDate": "2026-03-05T11:30:00"
}
```

**Errors:**

| Condition          | Code  | Message                              |
| ------------------ | ----- | ------------------------------------ |
| Material not found | `404` | `"Material not found with id: {id}"` |

---

### 4.3 `POST /api/v1/materials`

**Description:** Create a new material record.

**Required roles:** `Admin`, `InventoryManager`

**Request Body:**

```json
{
  "partNumber": "MAT-001",
  "materialName": "Vitamin D3 100K",
  "materialType": "API",
  "storageConditions": "Store at 2–8°C, protect from light",
  "specificationDocument": "https://docs.company.com/specs/MAT-001.pdf"
}
```

**Field Validation:**

| Field                   | Type   | Required | Constraints                                                                                                      |
| ----------------------- | ------ | -------- | ---------------------------------------------------------------------------------------------------------------- |
| `partNumber`            | string | Yes      | Max 50 chars; must be unique across all materials                                                                |
| `materialName`          | string | Yes      | Max 200 chars                                                                                                    |
| `materialType`          | enum   | Yes      | One of: `API`, `Excipient`, `Dietary Supplement`, `Container`, `Closure`, `Process Chemical`, `Testing Material` |
| `storageConditions`     | string | No       | Max 100 chars                                                                                                    |
| `specificationDocument` | string | No       | Max 50 chars (mã tài liệu kỹ thuật)                                                                              |

**Business Rules:**

- `partNumber` must be globally unique. Returns `409 Conflict` if duplicate.

**Response `201 Created`:** Full material object (same as GET by ID).

**Errors:**

| Condition              | Code  | Message                                 |
| ---------------------- | ----- | --------------------------------------- |
| Duplicate `partNumber` | `409` | `"Part number already exists: MAT-001"` |
| Validation failure     | `422` | Field-level error map                   |

---

### 4.4 `PUT /api/v1/materials/{id}`

**Description:** Full update of a material record. All fields are replaced.

**Required roles:** `Admin`, `InventoryManager`

**Request Body:** Same schema as `POST /api/v1/materials`.

**Business Rules:**

- `partNumber` uniqueness check excludes the current record (allows keeping the same part number).

**Response `200 OK`:** Updated material object.

**Errors:**

| Condition                              | Code  | Message                                                   |
| -------------------------------------- | ----- | --------------------------------------------------------- |
| Material not found                     | `404` | `"Material not found with id: {id}"`                      |
| `partNumber` taken by another material | `409` | `"Part number already used by another material: MAT-001"` |

---

### 4.5 `DELETE /api/v1/materials/{id}`

**Description:** Permanently delete a material. Blocked if any inventory lots reference it.

**Required roles:** `Admin`

**Response `204 No Content`**

**Errors:**

| Condition          | Code  | Message                                                                                   |
| ------------------ | ----- | ----------------------------------------------------------------------------------------- |
| Material not found | `404` | `"Material not found with id: {id}"`                                                      |
| Linked lots exist  | `400` | `"Cannot delete material with existing inventory lots. Deactivate the material instead."` |

---

## 5. Module 2 – Inventory Lots

**Base path:** `/api/v1/lots`

Inventory lots represent physical batches of material received into stock. Each lot has a lifecycle status and tracks all quantity movements via transactions.

**Lot Status Lifecycle:**

```
Quarantine ──(QC pass)──► Accepted ──(depleted)──► Depleted
           ──(QC fail)──► Rejected ──(manual)──────► Depleted
```

---

### 5.1 `GET /api/v1/lots`

**Description:** List inventory lots with optional filters.

**Required roles:** All roles

**Query Parameters:**

| Parameter    | Type    | Required | Description                                          |
| ------------ | ------- | -------- | ---------------------------------------------------- |
| `materialId` | UUID    | No       | Filter lots for a specific material                  |
| `status`     | enum    | No       | `Quarantine`, `Accepted`, `Rejected`, `Depleted`     |
| `nearExpiry` | boolean | No       | If `true`, returns only lots expiring within 30 days |
| `isSample`   | boolean | No       | Filter sample vs. regular lots                       |
| `page`       | integer | No       | Default `0`                                          |
| `size`       | integer | No       | Default `20`                                         |
| `sort`       | string  | No       | Default `receivedDate,desc`                          |

**Response `200 OK`:**

```json
{
  "content": [
    {
      "lotId": "b2c3d4e5-...",
      "materialId": "a1b2c3d4-...",
      "materialName": "Vitamin D3 100K",
      "partNumber": "MAT-001",
      "manufacturerName": "Pharma Corp",
      "manufacturerLot": "PC-2026-0312",
      "supplierName": "Global Chem Supply",
      "quantity": 25.5,
      "unitOfMeasure": "kg",
      "status": "Quarantine",
      "receivedDate": "2026-03-10",
      "expirationDate": "2028-03-01",
      "inUseExpirationDate": null,
      "storageLocation": "Warehouse-A / Shelf-3",
      "isSample": false,
      "parentLotId": null,
      "poNumber": "PO-2026-0045",
      "receivingFormId": "RF-2026-0099",
      "createdDate": "2026-03-10T08:00:00",
      "modifiedDate": "2026-03-10T08:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 5.2 `GET /api/v1/lots/{id}`

**Description:** Retrieve full detail of a single inventory lot.

**Required roles:** All roles

**Response `200 OK`:** Single lot object (same schema as list item).

**Errors:**

| Condition     | Code  | Message                                  |
| ------------- | ----- | ---------------------------------------- |
| Lot not found | `404` | `"InventoryLot not found with id: {id}"` |

---

### 5.3 `POST /api/v1/lots/receive`

**Description:** Receive a new shipment — creates an inventory lot with `status = Quarantine` and records a `Receipt` transaction.

**Required roles:** `Admin`, `InventoryManager`

**Request Body:**

```json
{
  "materialId": "a1b2c3d4-...",
  "manufacturerName": "Pharma Corp",
  "manufacturerLot": "PC-2026-0312",
  "supplierName": "Global Chem Supply",
  "quantity": 25.5,
  "unitOfMeasure": "kg",
  "receivedDate": "2026-03-10",
  "expirationDate": "2028-03-01",
  "inUseExpirationDate": null,
  "storageLocation": "Warehouse-A / Shelf-3",
  "poNumber": "PO-2026-0045",
  "receivingFormId": "RF-2026-0099",
  "performedBy": "jdoe"
}
```

**Field Validation:**

| Field                 | Type    | Required | Constraints                                                     |
| --------------------- | ------- | -------- | --------------------------------------------------------------- |
| `materialId`          | UUID    | Yes      | Must exist in Materials                                         |
| `manufacturerName`    | string  | Yes      | Max 100 chars                                                   |
| `manufacturerLot`     | string  | Yes      | Max 50 chars                                                    |
| `quantity`            | decimal | Yes      | > 0, max 10 digits / 3 decimal places                           |
| `unitOfMeasure`       | string  | Yes      | Max 10 chars (e.g. `kg`, `L`, `pcs`)                            |
| `receivedDate`        | date    | Yes      | Ngày nhận hàng                                                  |
| `expirationDate`      | date    | Yes      | Must be in the future                                           |
| `supplierName`        | string  | No       | Max 100 chars                                                   |
| `inUseExpirationDate` | date    | No       | Hạn dùng sau khi mở                                             |
| `storageLocation`     | string  | No       | Max 50 chars                                                    |
| `poNumber`            | string  | No       | Max 30 chars                                                    |
| `receivingFormId`     | string  | No       | Max 50 chars                                                    |
| `performedBy`         | string  | No       | Username; defaults to authenticated user's `preferred_username` |

**Business Rules:**

- Lot is always created with `status = Quarantine`.
- A `Receipt` transaction is automatically recorded with `quantity = +receivedQty`.
- `isSample` is always `false` on receive; use the `/split` endpoint to create samples.

**Side Effects:**

- Creates 1 row in `inventory_lots`.
- Creates 1 row in `inventory_transactions` (type `Receipt`, positive quantity).

**Response `201 Created`:** Full lot object.

**Errors:**

| Condition                    | Code  | Message                                   |
| ---------------------------- | ----- | ----------------------------------------- |
| `materialId` not found       | `404` | `"Material not found with id: {id}"`      |
| `expirationDate` in the past | `400` | `"Expiration date must be in the future"` |
| `quantity` ≤ 0               | `422` | `"Quantity must be greater than 0"`       |

---

### 5.4 `PATCH /api/v1/lots/{id}/status`

**Description:** Manually update the status of an inventory lot. Used for operational overrides outside of automatic QC-driven transitions.

**Required roles:** `Admin`, `InventoryManager`

**Request Body:**

```json
{
  "status": "Accepted",
  "performedBy": "jdoe",
  "notes": "Manual override — QC confirmed offline"
}
```

**Allowed Transitions:**

| From         | To (allowed)              |
| ------------ | ------------------------- |
| `Quarantine` | `Accepted`, `Rejected`    |
| `Accepted`   | `Depleted`                |
| `Rejected`   | `Depleted`                |
| `Depleted`   | _(none — terminal state)_ |

**Side Effects:**

- Creates 1 `Adjustment` transaction row with `quantity = 0` and a status-change note.

**Response `200 OK`:** Updated lot object.

**Errors:**

| Condition                            | Code  | Message                                              |
| ------------------------------------ | ----- | ---------------------------------------------------- |
| Lot not found                        | `404` | `"InventoryLot not found with id: {id}"`             |
| Invalid transition                   | `400` | `"Invalid status transition: Quarantine → Depleted"` |
| Lot is already terminal (`Depleted`) | `400` | `"Invalid status transition: Depleted → Accepted"`   |

---

### 5.5 `POST /api/v1/lots/{id}/split`

**Description:** Split a quantity from an existing lot to create a new sample child lot. Used when QC needs a physical sample for testing.

**Required roles:** `Admin`, `InventoryManager`, `QualityControl`

**Request Body:**

```json
{
  "sampleQuantity": 0.5,
  "storageLocation": "QC Lab / Fridge-1",
  "performedBy": "qc_user"
}
```

**Field Validation:**

| Field             | Type    | Required | Constraints                                    |
| ----------------- | ------- | -------- | ---------------------------------------------- |
| `sampleQuantity`  | decimal | Yes      | > 0; must be ≤ parent lot's current `quantity` |
| `storageLocation` | string  | No       | Max 100 chars                                  |
| `performedBy`     | string  | No       | Defaults to authenticated user                 |

**Business Rules:**

- Parent lot must have `status = Quarantine` or `Accepted`.
- `sampleQuantity` must not exceed parent lot's `quantity`.
- Parent `quantity` decreases by `sampleQuantity`. If reaches 0, parent → `Depleted`.
- New child lot is created with `isSample = true`, `parentLotId = {id}`, `status = Quarantine`.

**Side Effects:**

- Parent lot: `quantity` decreases; `status` may become `Depleted`.
- Parent lot: 1 `Split` transaction row (negative quantity).
- Child lot: created with `isSample = true`.
- Child lot: 1 `Split` transaction row (positive quantity).

**Response `201 Created`:** Full child (sample) lot object.

**Errors:**

| Condition             | Code  | Message                                                |
| --------------------- | ----- | ------------------------------------------------------ |
| Lot not found         | `404` | `"InventoryLot not found with id: {id}"`               |
| Invalid lot status    | `400` | `"Can only split from Quarantine or Accepted lots"`    |
| Insufficient quantity | `400` | `"Insufficient quantity for split. Available: 25.500"` |

---

### 5.6 `POST /api/v1/lots/{id}/adjust`

**Description:** Record an ad-hoc quantity adjustment (stock count correction, write-off, etc.).

**Required roles:** `Admin`, `InventoryManager`

**Request Body:**

```json
{
  "adjustmentQuantity": -1.2,
  "reason": "Spillage during routine handling",
  "performedBy": "jdoe"
}
```

**Field Validation:**

| Field                | Type    | Required | Constraints                                                                  |
| -------------------- | ------- | -------- | ---------------------------------------------------------------------------- |
| `adjustmentQuantity` | decimal | Yes      | May be positive (add) or negative (subtract); resulting quantity must be ≥ 0 |
| `reason`             | string  | Yes      | Max 500 chars                                                                |
| `performedBy`        | string  | No       | Defaults to authenticated user                                               |

**Business Rules:**

- Lot must be `Accepted` for a positive or negative adjustment.
- Resulting `quantity` cannot be negative (returns `400`).
- If resulting quantity = 0, lot automatically transitions to `Depleted`.

**Side Effects:**

- 1 `Adjustment` transaction row with the signed quantity.
- Lot `quantity` updated.
- If new quantity = 0, lot `status` → `Depleted`.

**Response `200 OK`:** Updated lot object.

---

### 5.7 `POST /api/v1/lots/{id}/transfer`

**Description:** Move a lot to a different storage location.

**Required roles:** `Admin`, `InventoryManager`

**Request Body:**

```json
{
  "newStorageLocation": "Warehouse-B / Shelf-7",
  "performedBy": "jdoe",
  "notes": "Relocated due to temperature zone requirements"
}
```

**Side Effects:**

- Lot `storageLocation` updated.
- 1 `Transfer` transaction row recorded (quantity = 0, notes = old → new location).

**Response `200 OK`:** Updated lot object.

---

### 5.8 `POST /api/v1/lots/{id}/dispose`

**Description:** Dispose of a lot (destroy rejected or expired material).

**Required roles:** `Admin`, `InventoryManager`

**Business Rules:**

- Lot must be `Rejected` or `Depleted` to be disposed. Active `Accepted` lots cannot be disposed directly.

**Request Body:**

```json
{
  "disposalQuantity": 25.5,
  "reason": "QC failure — out of specification",
  "performedBy": "jdoe"
}
```

**Side Effects:**

- 1 `Disposal` transaction row (negative quantity = full disposal amount).
- Lot `quantity` → 0 and `status` → `Depleted` (if not already).

**Response `200 OK`:** Updated lot object.

**Errors:**

| Condition                         | Code  | Message                                                   |
| --------------------------------- | ----- | --------------------------------------------------------- |
| Lot is `Accepted` or `Quarantine` | `400` | `"Disposal only permitted for Rejected or Depleted lots"` |

---

## 6. Module 3 – Inventory Transactions

**Base path:** `/api/v1/lots/{lotId}/transactions`

Transactions are **immutable** records of every quantity change to a lot. They are always created as a side-effect of other operations (receive, split, QC status change, usage, etc.) and cannot be created, edited, or deleted directly.

---

### 6.1 `GET /api/v1/lots/{lotId}/transactions`

**Description:** Retrieve the full transaction history for a given lot, ordered by date descending.

**Required roles:** All roles

**Query Parameters:**

| Parameter | Type | Required | Description                                                                                    |
| --------- | ---- | -------- | ---------------------------------------------------------------------------------------------- |
| `type`    | enum | No       | Filter by `transactionType`: `Receipt`, `Usage`, `Split`, `Transfer`, `Adjustment`, `Disposal` |

**Response `200 OK`:**

```json
[
  {
    "transactionId": "c3d4e5f6-...",
    "lotId": "b2c3d4e5-...",
    "transactionType": "Receipt",
    "quantity": 25.5,
    "transactionDate": "2026-03-10T08:00:00",
    "referenceId": null,
    "notes": "Initial stock receipt",
    "performedBy": "jdoe"
  }
]
```

**Response Fields:**

| Field             | Description                                                               |
| ----------------- | ------------------------------------------------------------------------- |
| `quantity`        | Positive = stock in; Negative = stock out                                 |
| `referenceId`     | Batch number, PO number, or related entity ID                             |
| `transactionType` | One of: `Receipt`, `Usage`, `Split`, `Transfer`, `Adjustment`, `Disposal` |

**Errors:**

| Condition     | Code  | Message                                     |
| ------------- | ----- | ------------------------------------------- |
| Lot not found | `404` | `"InventoryLot not found with id: {lotId}"` |

---

## 7. Module 4 – QC Testing

**Base path:** `/api/v1/qctests`

QC tests are performed against inventory lots to determine whether material is acceptable for production use. When all tests for a lot pass, the lot is automatically promoted to `Accepted`. Any failing test moves the lot to `Rejected`.

**QC Result → Lot Status Mapping:**

| QC Result State                   | Lot Status Result              |
| --------------------------------- | ------------------------------ |
| All `Pass`, no `Pending`          | `Quarantine → Accepted`        |
| Any `Fail` (regardless of others) | `Quarantine → Rejected`        |
| Mix of `Pass` + `Pending`         | No change (still `Quarantine`) |

---

### 7.1 `GET /api/v1/qctests`

**Description:** List QC tests for a given lot.

**Required roles:** All roles

**Query Parameters:**

| Parameter | Type | Required | Description            |
| --------- | ---- | -------- | ---------------------- |
| `lotId`   | UUID | Yes      | Lot to fetch tests for |

**Response `200 OK`:**

```json
[
  {
    "testId": "d4e5f6a7-...",
    "lotId": "b2c3d4e5-...",
    "lotManufacturerLot": "PC-2026-0312",
    "testType": "Identity",
    "testMethod": "HPLC-UV",
    "testDate": "2026-03-12",
    "testResult": "Conforms to specification",
    "acceptanceCriteria": "NLT 99.0% purity",
    "resultStatus": "Pass",
    "performedBy": "qc_analyst",
    "verifiedBy": "qc_supervisor",
    "createdDate": "2026-03-12T10:00:00",
    "modifiedDate": "2026-03-12T14:30:00"
  }
]
```

---

### 7.2 `GET /api/v1/qctests/{id}`

**Description:** Retrieve a single QC test record.

**Required roles:** All roles

**Response `200 OK`:** Single test object (same schema as list item).

**Errors:**

| Condition      | Code  | Message                            |
| -------------- | ----- | ---------------------------------- |
| Test not found | `404` | `"QCTest not found with id: {id}"` |

---

### 7.3 `POST /api/v1/qctests`

**Description:** Record a new QC test against a lot.

**Required roles:** `Admin`, `QualityControl`

**Request Body:**

```json
{
  "lotId": "b2c3d4e5-...",
  "testType": "Identity",
  "testMethod": "HPLC-UV",
  "testDate": "2026-03-12",
  "testResult": "Conforms to specification",
  "acceptanceCriteria": "NLT 99.0% purity",
  "resultStatus": "Pass",
  "performedBy": "qc_analyst",
  "verifiedBy": "qc_supervisor"
}
```

**Field Validation:**

| Field                | Type   | Required | Constraints                                                                   |
| -------------------- | ------ | -------- | ----------------------------------------------------------------------------- |
| `lotId`              | UUID   | Yes      | Must be an existing lot                                                       |
| `testType`           | enum   | Yes      | `Identity`, `Potency`, `Microbial`, `GrowthPromotion`, `Physical`, `Chemical` |
| `testMethod`         | string | Yes      | Max 100 chars                                                                 |
| `testDate`           | date   | Yes      | Cannot be in the future                                                       |
| `testResult`         | string | Yes      | Max 100 chars                                                                 |
| `acceptanceCriteria` | string | No       | Max 200 chars                                                                 |
| `resultStatus`       | enum   | Yes      | `Pass`, `Fail`, `Pending`                                                     |
| `performedBy`        | string | Yes      | Max 50 chars                                                                  |
| `verifiedBy`         | string | No       | Max 50 chars; required when `resultStatus = Pass` or `Fail`                   |

**Business Rules:**

- Cannot add tests to lots with `status = Rejected` or `Depleted`.
- After saving, the system evaluates all tests for this lot and may automatically update the lot status (see mapping above).

**Side Effects:**

- If lot status changes automatically: 1 `Adjustment` transaction row is recorded with `performedBy = "system"`.

**Response `201 Created`:** Full test object.

**Errors:**

| Condition                         | Code  | Message                                                |
| --------------------------------- | ----- | ------------------------------------------------------ |
| Lot not found                     | `404` | `"InventoryLot not found with id: {lotId}"`            |
| Lot is Rejected or Depleted       | `400` | `"Cannot add QC test to a lot with status: Rejected"`  |
| `verifiedBy` missing on Pass/Fail | `422` | `"verifiedBy is required when result is Pass or Fail"` |

---

### 7.4 `PUT /api/v1/qctests/{id}`

**Description:** Update an existing QC test record — used to change the result from `Pending` to `Pass`/`Fail`, or to add the `verifiedBy` field.

**Required roles:** `Admin`, `QualityControl`

**Request Body:** Same schema as `POST /api/v1/qctests` (full replacement).

**Business Rules:**

- After update, lot status re-evaluation runs automatically.
- Cannot change `lotId` on update.

**Response `200 OK`:** Updated test object.

---

### 7.5 `GET /api/v1/qctests/summary`

**Description:** Get a pass/fail summary for a lot — shows total, passed, failed, and pending counts.

**Required roles:** All roles

**Query Parameters:**

| Parameter | Type | Required |
| --------- | ---- | -------- |
| `lotId`   | UUID | Yes      |

**Response `200 OK`:**

```json
{
  "lotId": "b2c3d4e5-...",
  "lotStatus": "Quarantine",
  "totalTests": 3,
  "passed": 2,
  "failed": 0,
  "pending": 1
}
```

---

## 8. Module 5 – Production Batches

**Base path:** `/api/v1/batches`

A production batch represents a manufacturing run of a finished product. It consumes inventory lots as components, tracking planned vs. actual quantities used.

**Batch Status Lifecycle:**

```
Planned ──► InProgress ──► Complete
                       ──► Rejected
```

---

### 8.1 `GET /api/v1/batches`

**Description:** List production batches with optional filters.

**Required roles:** All roles

**Query Parameters:**

| Parameter   | Type    | Required | Description                                     |
| ----------- | ------- | -------- | ----------------------------------------------- |
| `status`    | enum    | No       | `Planned`, `InProgress`, `Complete`, `Rejected` |
| `productId` | UUID    | No       | Filter by product (material)                    |
| `page`      | integer | No       | Default `0`                                     |
| `size`      | integer | No       | Default `20`                                    |
| `sort`      | string  | No       | Default `createdDate,desc`                      |

**Response `200 OK`:**

```json
{
  "content": [
    {
      "batchId": "e5f6a7b8-...",
      "productId": "a1b2c3d4-...",
      "productName": "IMS-Tablet-500mg",
      "batchNumber": "BATCH-2026-001",
      "batchSize": 100.0,
      "unitOfMeasure": "kg",
      "manufactureDate": "2026-03-15",
      "expirationDate": "2028-03-15",
      "status": "Planned",
      "componentCount": 3,
      "confirmedComponentCount": 0,
      "createdDate": "2026-03-10T10:00:00",
      "modifiedDate": "2026-03-10T10:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 8.2 `GET /api/v1/batches/{id}`

**Description:** Retrieve full batch detail, including all components.

**Required roles:** All roles

**Response `200 OK`:**

```json
{
  "batchId": "e5f6a7b8-...",
  "productId": "a1b2c3d4-...",
  "productName": "IMS-Tablet-500mg",
  "batchNumber": "BATCH-2026-001",
  "batchSize": 100.0,
  "unitOfMeasure": "kg",
  "manufactureDate": "2026-03-15",
  "expirationDate": "2028-03-15",
  "status": "InProgress",
  "createdDate": "2026-03-10T10:00:00",
  "modifiedDate": "2026-03-11T08:00:00",
  "components": [
    {
      "componentId": "f6a7b8c9-...",
      "lotId": "b2c3d4e5-...",
      "materialName": "Vitamin D3 100K",
      "partNumber": "MAT-001",
      "lotStatus": "Accepted",
      "lotAvailableQuantity": 23.5,
      "plannedQuantity": 2.0,
      "actualQuantity": 2.0,
      "unitOfMeasure": "kg",
      "additionDate": "2026-03-11T08:00:00",
      "addedBy": "operator1"
    }
  ]
}
```

---

### 8.3 `POST /api/v1/batches`

**Description:** Create a new production batch with `status = Planned`.

**Required roles:** `Admin`, `InventoryManager`, `Production`

**Request Body:**

```json
{
  "productId": "a1b2c3d4-...",
  "batchNumber": "BATCH-2026-001",
  "batchSize": 100.0,
  "unitOfMeasure": "kg",
  "manufactureDate": "2026-03-15",
  "expirationDate": "2028-03-15"
}
```

**Field Validation:**

| Field             | Type    | Required | Constraints                               |
| ----------------- | ------- | -------- | ----------------------------------------- |
| `productId`       | UUID    | Yes      | Must be an existing material              |
| `batchNumber`     | string  | Yes      | Max 50 chars; globally unique             |
| `batchSize`       | decimal | Yes      | > 0                                       |
| `unitOfMeasure`   | string  | Yes      | Max 10 chars                              |
| `manufactureDate` | date    | Yes      | Cannot be in the past by more than 7 days |
| `expirationDate`  | date    | Yes      | Must be after `manufactureDate`           |

**Response `201 Created`:** Full batch object (without components list).

**Errors:**

| Condition                                 | Code  | Message                                            |
| ----------------------------------------- | ----- | -------------------------------------------------- |
| Duplicate `batchNumber`                   | `409` | `"Batch number already exists: BATCH-2026-001"`    |
| `productId` not found                     | `404` | `"Material not found with id: {productId}"`        |
| `expirationDate` before `manufactureDate` | `400` | `"Expiration date must be after manufacture date"` |

---

### 8.4 `PATCH /api/v1/batches/{id}/status`

**Description:** Advance or reject the batch status.

**Required roles:** `Admin`, `Production`

**Request Body:**

```json
{
  "status": "InProgress",
  "performedBy": "operator1"
}
```

**Allowed Transitions:**

| From         | To (allowed)           |
| ------------ | ---------------------- |
| `Planned`    | `InProgress`           |
| `InProgress` | `Complete`, `Rejected` |
| `Complete`   | _(none — terminal)_    |
| `Rejected`   | _(none — terminal)_    |

**Business Rules for `Complete`:**

- All components must have a non-null `actualQuantity` before the batch can be marked `Complete`.

**Response `200 OK`:** Updated batch object.

**Errors:**

| Condition             | Code  | Message                                                                   |
| --------------------- | ----- | ------------------------------------------------------------------------- |
| Batch not found       | `404` | `"ProductionBatch not found with id: {id}"`                               |
| Invalid transition    | `400` | `"Invalid batch status transition: Planned → Complete"`                   |
| Incomplete components | `400` | `"Cannot complete batch: 2 component(s) have not confirmed actual usage"` |

---

### 8.5 `POST /api/v1/batches/{id}/components`

**Description:** Add an inventory lot as a component to a batch.

**Required roles:** `Admin`, `InventoryManager`, `Production`

**Business Rules:**

- Batch must be in `InProgress` or `Planned` status.
- Lot must have `status = Accepted`.
- Lot must not be expired (`expirationDate >= today`).
- Same lot cannot be added twice to the same batch.

**Request Body:**

```json
{
  "lotId": "b2c3d4e5-...",
  "plannedQuantity": 2.0,
  "unitOfMeasure": "kg",
  "addedBy": "operator1"
}
```

**Field Validation:**

| Field             | Type    | Required | Constraints                          |
| ----------------- | ------- | -------- | ------------------------------------ |
| `lotId`           | UUID    | Yes      | Must be an Accepted, non-expired lot |
| `plannedQuantity` | decimal | Yes      | > 0                                  |
| `unitOfMeasure`   | string  | Yes      | Max 10 chars                         |
| `addedBy`         | string  | No       | Defaults to authenticated user       |

**Response `201 Created`:** Full component object.

**Errors:**

| Condition                  | Code  | Message                                                                       |
| -------------------------- | ----- | ----------------------------------------------------------------------------- |
| Lot not found              | `404` | `"InventoryLot not found with id: {lotId}"`                                   |
| Lot not Accepted           | `400` | `"Lot must be Accepted to be used in production. Current status: Quarantine"` |
| Lot expired                | `400` | `"Lot has expired. Expiration date: 2025-12-01"`                              |
| Duplicate lot in batch     | `409` | `"Lot is already added to this batch"`                                        |
| Batch is Complete/Rejected | `400` | `"Cannot add components to a batch with status: Complete"`                    |

---

### 8.6 `PATCH /api/v1/batches/components/{componentId}/confirm`

**Description:** Confirm the actual quantity of an ingredient used in production. This deducts the quantity from the inventory lot and records a `Usage` transaction.

**Required roles:** `Admin`, `InventoryManager`, `Production`

**Request Body:**

```json
{
  "actualQuantity": 2.0,
  "performedBy": "operator1"
}
```

**Field Validation:**

| Field            | Type    | Required | Constraints                                   |
| ---------------- | ------- | -------- | --------------------------------------------- |
| `actualQuantity` | decimal | Yes      | > 0; must not exceed lot's current `quantity` |

**Business Rules:**

- Batch must be `InProgress`.
- Lot must still be `Accepted` and not expired.
- `actualQuantity` must not exceed `lot.quantity`.
- If `lot.quantity - actualQuantity = 0`, lot status → `Depleted`.

**Side Effects:**

- Lot `quantity` decreases by `actualQuantity`.
- If lot quantity reaches 0, lot `status` → `Depleted`.
- 1 `Usage` transaction row (negative quantity, `referenceId = batchNumber`).

**Response `200 OK`:**

```json
{
  "componentId": "f6a7b8c9-...",
  "lotId": "b2c3d4e5-...",
  "plannedQuantity": 2.0,
  "actualQuantity": 2.0,
  "unitOfMeasure": "kg",
  "additionDate": "2026-03-11T09:30:00",
  "addedBy": "operator1"
}
```

**Errors:**

| Condition             | Code  | Message                                                           |
| --------------------- | ----- | ----------------------------------------------------------------- |
| Component not found   | `404` | `"BatchComponent not found with id: {componentId}"`               |
| Batch not InProgress  | `400` | `"Batch must be In Progress to confirm usage"`                    |
| Lot not Accepted      | `400` | `"Lot must be Accepted to use in production"`                     |
| Insufficient quantity | `400` | `"Insufficient lot quantity. Available: 5.000, Requested: 7.000"` |
| Lot expired           | `400` | `"Lot has expired"`                                               |

---

## 9. Module 6 – Label Management

**Base path:** `/api/v1/labels`

Labels are generated from configurable HTML templates. The system supports templates for raw material receiving, QC samples, finished products, and status notifications.

**Supported Label Types:**

| Type              | Triggered By                          |
| ----------------- | ------------------------------------- |
| `RawMaterial`     | Lot receive (auto-generated)          |
| `Sample`          | Lot split (auto-generated)            |
| `FinishedProduct` | Batch completion (auto-generated)     |
| `Status`          | Lot status change (Accepted/Rejected) |
| `Intermediate`    | Manual generation                     |
| `API`             | Manual generation                     |

---

### 9.1 `GET /api/v1/labels/templates`

**Description:** List all label templates, optionally filtered by type.

**Required roles:** All roles

**Query Parameters:**

| Parameter   | Type | Required | Description          |
| ----------- | ---- | -------- | -------------------- |
| `labelType` | enum | No       | Filter by label type |

**Response `200 OK`:**

```json
[
  {
    "templateId": "TPL-RM-01",
    "templateName": "Standard Raw Material Label",
    "labelType": "RawMaterial",
    "templateContent": "<div>{{materialName}}<br/>Lot: {{lotId}}</div>",
    "width": 10.0,
    "height": 5.0,
    "createdDate": "2026-01-15T09:00:00",
    "modifiedDate": "2026-02-01T11:00:00"
  }
]
```

---

### 9.2 `GET /api/v1/labels/templates/{id}`

**Description:** Retrieve a single label template by ID.

**Required roles:** All roles

**Errors:**

| Condition          | Code  | Message                                   |
| ------------------ | ----- | ----------------------------------------- |
| Template not found | `404` | `"LabelTemplate not found with id: {id}"` |

---

### 9.3 `POST /api/v1/labels/templates`

**Description:** Create a new label template.

**Required roles:** `Admin`

**Request Body:**

```json
{
  "templateId": "TPL-RM-02",
  "templateName": "Custom Raw Material Label",
  "labelType": "RawMaterial",
  "templateContent": "<div style='font-size:12pt'><b>{{materialName}}</b><br/>Lot: {{lotId}}<br/>Exp: {{expirationDate}}<br/>{{storageLocation}}</div>",
  "width": 10.0,
  "height": 5.0
}
```

**Field Validation:**

| Field             | Type    | Required | Constraints                                                                 |
| ----------------- | ------- | -------- | --------------------------------------------------------------------------- |
| `templateId`      | string  | Yes      | Max 20 chars; unique                                                        |
| `templateName`    | string  | Yes      | Max 100 chars                                                               |
| `labelType`       | enum    | Yes      | `RawMaterial`, `Sample`, `Intermediate`, `FinishedProduct`, `API`, `Status` |
| `templateContent` | string  | Yes      | HTML with `{{placeholder}}` tokens                                          |
| `width`           | decimal | Yes      | In cm; > 0                                                                  |
| `height`          | decimal | Yes      | In cm; > 0                                                                  |

**Supported Placeholders by Label Type:**

| Placeholder            | Available in                                   |
| ---------------------- | ---------------------------------------------- |
| `{{lotId}}`            | RawMaterial, Sample, Intermediate, API, Status |
| `{{materialName}}`     | RawMaterial, Sample, Intermediate, API, Status |
| `{{partNumber}}`       | RawMaterial, Sample, Intermediate, API         |
| `{{manufacturerLot}}`  | RawMaterial, Sample                            |
| `{{manufacturerName}}` | RawMaterial, Sample                            |
| `{{quantity}}`         | RawMaterial, Sample                            |
| `{{unitOfMeasure}}`    | RawMaterial, Sample, FinishedProduct           |
| `{{expirationDate}}`   | RawMaterial, Sample, FinishedProduct           |
| `{{storageLocation}}`  | RawMaterial, Sample                            |
| `{{receivedDate}}`     | RawMaterial                                    |
| `{{parentLotId}}`      | Sample                                         |
| `{{isSample}}`         | Sample                                         |
| `{{batchNumber}}`      | FinishedProduct                                |
| `{{productName}}`      | FinishedProduct                                |
| `{{batchSize}}`        | FinishedProduct                                |
| `{{manufactureDate}}`  | FinishedProduct                                |
| `{{status}}`           | Status                                         |
| `{{statusDate}}`       | Status                                         |

**Response `201 Created`:** Full template object.

**Errors:**

| Condition              | Code  | Message                                   |
| ---------------------- | ----- | ----------------------------------------- |
| Duplicate `templateId` | `409` | `"Template ID already exists: TPL-RM-02"` |

---

### 9.4 `PUT /api/v1/labels/templates/{id}`

**Description:** Full update of a label template (replace all fields).

**Required roles:** `Admin`

**Request Body:** Same schema as POST.

**Response `200 OK`:** Updated template object.

---

### 9.5 `DELETE /api/v1/labels/templates/{id}`

**Description:** Delete a label template.

**Required roles:** `Admin`

**Response `204 No Content`**

---

### 9.6 `POST /api/v1/labels/generate`

**Description:** Generate a rendered label by populating a template with data from a lot or batch.

**Required roles:** `Admin`, `InventoryManager`, `QualityControl`, `Production`

**Request Body:**

```json
{
  "templateId": "TPL-RM-01",
  "sourceType": "LOT",
  "sourceId": "b2c3d4e5-..."
}
```

**Field Validation:**

| Field        | Type   | Required | Constraints                                                |
| ------------ | ------ | -------- | ---------------------------------------------------------- |
| `templateId` | string | Yes      | Must be an existing template                               |
| `sourceType` | enum   | Yes      | `LOT` or `BATCH`                                           |
| `sourceId`   | UUID   | Yes      | Must be an existing lot (for `LOT`) or batch (for `BATCH`) |

**Business Rules:**

- The label type of the template must be compatible with the source type:
  - `RawMaterial`, `Sample`, `Status`, `Intermediate`, `API` → source must be `LOT`
  - `FinishedProduct` → source must be `BATCH`

**Response `200 OK`:**

```json
{
  "templateId": "TPL-RM-01",
  "templateName": "Standard Raw Material Label",
  "labelType": "RawMaterial",
  "renderedContent": "<div><b>Vitamin D3 100K</b><br/>Lot: b2c3d4e5-...<br/>Exp: 2028-03-01</div>",
  "width": 10.0,
  "height": 5.0,
  "generatedAt": "2026-03-10T14:30:00"
}
```

**Errors:**

| Condition          | Code  | Message                                                          |
| ------------------ | ----- | ---------------------------------------------------------------- |
| Template not found | `404` | `"LabelTemplate not found with id: {templateId}"`                |
| Source not found   | `404` | `"InventoryLot not found with id: {sourceId}"`                   |
| Type mismatch      | `400` | `"Template type 'FinishedProduct' requires source type 'BATCH'"` |

---

## 10. Module 7 – Reports & Dashboard

**Base path:** `/api/v1/reports`

Reporting endpoints provide real-time inventory visibility and compliance data. All report endpoints are read-only.

---

### 10.1 `GET /api/v1/reports/dashboard`

**Description:** Retrieve inventory dashboard summary counts.

**Required roles:** All roles

**Response `200 OK`:**

```json
{
  "totalMaterials": 45,
  "totalActiveLots": 120,
  "byStatus": {
    "Quarantine": 15,
    "Accepted": 90,
    "Rejected": 5,
    "Depleted": 10
  },
  "nearExpiryLots": 8,
  "activeBatches": 3,
  "failedQCLast30Days": 2,
  "asOf": "2026-03-10T14:00:00"
}
```

---

### 10.2 `GET /api/v1/reports/near-expiry`

**Description:** List Accepted lots expiring within N days.

**Required roles:** All roles

**Query Parameters:**

| Parameter | Type    | Required | Default | Description           |
| --------- | ------- | -------- | ------- | --------------------- |
| `days`    | integer | No       | `30`    | Expiry window in days |
| `page`    | integer | No       | `0`     | Pagination            |
| `size`    | integer | No       | `20`    | Pagination            |

**Response `200 OK`:**

```json
{
  "content": [
    {
      "lotId": "b2c3d4e5-...",
      "materialName": "Vitamin D3 100K",
      "partNumber": "MAT-001",
      "quantity": 5.5,
      "unitOfMeasure": "kg",
      "status": "Accepted",
      "storageLocation": "Warehouse-A / Shelf-3",
      "expirationDate": "2026-03-25",
      "daysUntilExpiry": 15
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 8,
  "totalPages": 1
}
```

---

### 10.3 `GET /api/v1/reports/lots/{lotId}/trace`

**Description:** Full traceability report for a single lot — includes lot metadata, all transactions, all QC tests, and any production batches the lot was used in.

**Required roles:** All roles

**Response `200 OK`:**

```json
{
  "lot": {
    "lotId": "b2c3d4e5-...",
    "materialName": "Vitamin D3 100K",
    "partNumber": "MAT-001",
    "status": "Accepted",
    "quantity": 21.5,
    "receivedDate": "2026-03-10",
    "expirationDate": "2028-03-01"
  },
  "transactions": [
    {
      "transactionType": "Usage",
      "quantity": -2.0,
      "transactionDate": "2026-03-15T10:00:00",
      "referenceId": "BATCH-2026-001",
      "performedBy": "operator1"
    },
    {
      "transactionType": "Receipt",
      "quantity": 25.5,
      "transactionDate": "2026-03-10T08:00:00",
      "performedBy": "jdoe"
    }
  ],
  "qcTests": [
    {
      "testType": "Identity",
      "resultStatus": "Pass",
      "testDate": "2026-03-12",
      "performedBy": "qc_analyst"
    }
  ],
  "usedInBatches": [
    {
      "batchId": "e5f6a7b8-...",
      "batchNumber": "BATCH-2026-001",
      "productName": "IMS-Tablet-500mg",
      "status": "Complete",
      "actualQuantityUsed": 2.0
    }
  ]
}
```

**Errors:**

| Condition     | Code  | Message                                     |
| ------------- | ----- | ------------------------------------------- |
| Lot not found | `404` | `"InventoryLot not found with id: {lotId}"` |

---

### 10.4 `GET /api/v1/reports/qc`

**Description:** QC pass/fail summary report filtered by date range.

**Required roles:** `Admin`, `InventoryManager`, `QualityControl`

**Query Parameters:**

| Parameter | Type | Required | Description                            |
| --------- | ---- | -------- | -------------------------------------- |
| `from`    | date | Yes      | Start of date range (test_date ≥ from) |
| `to`      | date | Yes      | End of date range (test_date ≤ to)     |

**Response `200 OK`:**

```json
{
  "period": {
    "from": "2026-01-01",
    "to": "2026-03-10"
  },
  "summary": {
    "totalTests": 85,
    "passed": 78,
    "failed": 4,
    "pending": 3,
    "passRate": 95.12
  },
  "byMaterial": [
    {
      "materialId": "a1b2c3d4-...",
      "materialName": "Vitamin D3 100K",
      "totalTests": 12,
      "passed": 11,
      "failed": 1,
      "passRate": 91.67
    }
  ]
}
```

---

### 10.5 `GET /api/v1/reports/inventory`

**Description:** Current inventory snapshot — all lots grouped by material with total quantities per status.

**Required roles:** All roles

**Query Parameters:**

| Parameter      | Type | Required | Description             |
| -------------- | ---- | -------- | ----------------------- |
| `status`       | enum | No       | Filter by lot status    |
| `materialType` | enum | No       | Filter by material type |

**Response `200 OK`:**

```json
[
  {
    "materialId": "a1b2c3d4-...",
    "partNumber": "MAT-001",
    "materialName": "Vitamin D3 100K",
    "materialType": "API",
    "lots": {
      "Quarantine": { "count": 1, "totalQuantity": 10.0 },
      "Accepted": { "count": 2, "totalQuantity": 46.5 },
      "Rejected": { "count": 0, "totalQuantity": 0 },
      "Depleted": { "count": 1, "totalQuantity": 0 }
    },
    "totalAvailable": 46.5,
    "unit": "kg"
  }
]
```

---

## 11. Module 8 – User Management

**Base path:** `/api/v1/users`

> **Note:** The current implementation (`UserController.java`) is a basic CRUD stub without proper security. This spec defines the target design with full RBAC enforcement. User creation and role assignment should ultimately be managed via Keycloak's admin API; the IMS User table is a local mirror for profile and audit purposes.

---

### 11.1 `GET /api/v1/users`

**Description:** List all system users.

**Required roles:** `Admin`

**Query Parameters:**

| Parameter  | Type    | Required | Description            |
| ---------- | ------- | -------- | ---------------------- |
| `role`     | enum    | No       | Filter by role         |
| `isActive` | boolean | No       | Filter active/inactive |
| `page`     | integer | No       | Default `0`            |
| `size`     | integer | No       | Default `20`           |

**Response `200 OK`:**

```json
{
  "content": [
    {
      "userId": "a1b2c3d4-...",
      "username": "jdoe",
      "email": "j.doe@company.com",
      "role": "InventoryManager",
      "isActive": true,
      "lastLogin": "2026-03-10T08:00:00",
      "createdDate": "2026-01-01T09:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 12,
  "totalPages": 1
}
```

> **Security:** Password is never included in response bodies.

---

### 11.2 `GET /api/v1/users/{id}`

**Description:** Get user detail by UUID.

**Required roles:** `Admin`; or any role to view their **own** profile (`/api/v1/users/me`).

**Response `200 OK`:** Single user object (password excluded).

---

### 11.3 `GET /api/v1/users/me`

**Description:** Get the currently authenticated user's own profile.

**Required roles:** Any authenticated user.

**Response `200 OK`:** Single user object (password excluded).

---

### 11.4 `POST /api/v1/users`

**Description:** Register a new user in the IMS. Syncs with Keycloak user creation.

**Required roles:** `Admin`

**Request Body:**

```json
{
  "username": "jdoe",
  "email": "j.doe@company.com",
  "password": "TemporaryP@ss1",
  "role": "InventoryManager"
}
```

**Field Validation:**

| Field      | Type   | Required | Constraints                                                           |
| ---------- | ------ | -------- | --------------------------------------------------------------------- |
| `username` | string | Yes      | Max 50 chars; unique; alphanumeric + underscore only                  |
| `email`    | string | Yes      | Max 100 chars; valid email format; unique                             |
| `password` | string | Yes      | Min 8 chars; must contain uppercase, lowercase, digit                 |
| `role`     | enum   | Yes      | `Admin`, `InventoryManager`, `QualityControl`, `Production`, `Viewer` |

**Business Rules:**

- `username` must be globally unique.
- `email` must be globally unique.
- Password is hashed before persistence (BCrypt).

**Response `201 Created`:** User object (password excluded).

**Errors:**

| Condition            | Code  | Message                                                                          |
| -------------------- | ----- | -------------------------------------------------------------------------------- |
| Duplicate `username` | `409` | `"Username already exists: jdoe"`                                                |
| Duplicate `email`    | `409` | `"Email already exists: j.doe@company.com"`                                      |
| Weak password        | `422` | `"password: Must be at least 8 characters with uppercase, lowercase, and digit"` |

---

### 11.5 `PUT /api/v1/users/{id}`

**Description:** Update user profile and role.

**Required roles:** `Admin`

**Request Body:**

```json
{
  "username": "jdoe",
  "email": "j.doe.updated@company.com",
  "role": "Production",
  "isActive": true
}
```

> **Note:** Password is not updated via this endpoint. Use `PATCH /api/v1/users/{id}/password` for password changes.

**Response `200 OK`:** Updated user object.

---

### 11.6 `PATCH /api/v1/users/{id}/password`

**Description:** Change a user's password. Admin can change any user's password; a user may change their own.

**Required roles:** `Admin` (any user), or self (own password with current password verification).

**Request Body:**

```json
{
  "currentPassword": "OldP@ss1",
  "newPassword": "NewSecureP@ss2"
}
```

**Response `200 OK`:** `{ "message": "Password updated successfully" }`

---

### 11.7 `DELETE /api/v1/users/{id}`

**Description:** Deactivate a user (`isActive = false`). Does not hard-delete to preserve audit history.

**Required roles:** `Admin`

**Business Rules:**

- Cannot deactivate your own account.
- Cannot deactivate the last active Admin.

**Response `204 No Content`**

---

### 11.8 `GET /api/v1/users/{id}/activity`

**Description:** View recent activity (last 50 operations) for a user.

**Required roles:** `Admin`

**Response `200 OK`:**

```json
[
  {
    "action": "LOT_RECEIVED",
    "entityType": "InventoryLot",
    "entityId": "b2c3d4e5-...",
    "timestamp": "2026-03-10T08:00:00",
    "details": "Received 25.5 kg of Vitamin D3 100K"
  }
]
```

---

## 12. Cross-Cutting Concerns

### 12.1 Health Check

```
GET /api/v1/health
```

**Response `200 OK`:** (No auth required)

```json
{
  "status": "UP",
  "database": "UP",
  "timestamp": "2026-03-10T14:00:00"
}
```

### 12.2 Audit Logging

Every state-changing request is automatically logged. The audit log captures:

| Field         | Source                          |
| ------------- | ------------------------------- |
| `userId`      | JWT `sub` claim                 |
| `username`    | JWT `preferred_username` claim  |
| `action`      | Determined by controller method |
| `entityType`  | Module name                     |
| `entityId`    | Primary key of affected entity  |
| `timestamp`   | Server time at execution        |
| `ipAddress`   | Client IP from HTTP header      |
| `requestBody` | Sanitized (passwords redacted)  |

### 12.3 CORS Configuration

In development, `@CrossOrigin(origins = "*")` is used per controller.
In production, configure a global `CorsConfig` bean:

```java
registry.addMapping("/api/**")
    .allowedOrigins("https://your-domain.com")
    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    .allowedHeaders("Authorization", "Content-Type")
    .allowCredentials(true);
```

### 12.4 Request Size Limits

| Content                 | Limit                   |
| ----------------------- | ----------------------- |
| JSON body               | 2 MB                    |
| `templateContent` field | Unlimited (TEXT column) |

Configure in `application.properties`:

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 12.5 Rate Limiting (Production Recommendation)

Implement via API Gateway or Spring Rate Limiter:

- Unauthenticated: 20 requests/minute per IP
- Authenticated: 300 requests/minute per user

### 12.6 API Versioning Strategy

- Current codebase: `/api/` (no versioning)
- Target: `/api/v1/` for all new/refactored endpoints
- Breaking changes → `/api/v2/` with deprecation notice on `/api/v1/`
- Non-breaking additions (new fields, new endpoints) → no version bump required

---

## 13. Role–Permission Matrix

`✅` = allowed, `🔐` = own data only, `❌` = forbidden

| Endpoint                               | Admin | InventoryManager | QualityControl | Production | Viewer |
| -------------------------------------- | ----- | ---------------- | -------------- | ---------- | ------ |
| **Materials**                          |       |                  |                |            |        |
| GET /materials                         | ✅    | ✅               | ✅             | ✅         | ✅     |
| POST /materials                        | ✅    | ✅               | ❌             | ❌         | ❌     |
| PUT /materials/{id}                    | ✅    | ✅               | ❌             | ❌         | ❌     |
| DELETE /materials/{id}                 | ✅    | ❌               | ❌             | ❌         | ❌     |
| **Inventory Lots**                     |       |                  |                |            |        |
| GET /lots                              | ✅    | ✅               | ✅             | ✅         | ✅     |
| POST /lots/receive                     | ✅    | ✅               | ❌             | ❌         | ❌     |
| PATCH /lots/{id}/status                | ✅    | ✅               | ❌             | ❌         | ❌     |
| POST /lots/{id}/split                  | ✅    | ✅               | ✅             | ❌         | ❌     |
| POST /lots/{id}/adjust                 | ✅    | ✅               | ❌             | ❌         | ❌     |
| POST /lots/{id}/transfer               | ✅    | ✅               | ❌             | ❌         | ❌     |
| POST /lots/{id}/dispose                | ✅    | ✅               | ❌             | ❌         | ❌     |
| GET /lots/{id}/transactions            | ✅    | ✅               | ✅             | ✅         | ✅     |
| **QC Testing**                         |       |                  |                |            |        |
| GET /qctests                           | ✅    | ✅               | ✅             | ✅         | ✅     |
| POST /qctests                          | ✅    | ❌               | ✅             | ❌         | ❌     |
| PUT /qctests/{id}                      | ✅    | ❌               | ✅             | ❌         | ❌     |
| GET /qctests/summary                   | ✅    | ✅               | ✅             | ✅         | ✅     |
| **Production Batches**                 |       |                  |                |            |        |
| GET /batches                           | ✅    | ✅               | ✅             | ✅         | ✅     |
| POST /batches                          | ✅    | ✅               | ❌             | ✅         | ❌     |
| PATCH /batches/{id}/status             | ✅    | ❌               | ❌             | ✅         | ❌     |
| POST /batches/{id}/components          | ✅    | ✅               | ❌             | ✅         | ❌     |
| PATCH /batches/components/{id}/confirm | ✅    | ✅               | ❌             | ✅         | ❌     |
| **Labels**                             |       |                  |                |            |        |
| GET /labels/templates                  | ✅    | ✅               | ✅             | ✅         | ✅     |
| POST /labels/templates                 | ✅    | ❌               | ❌             | ❌         | ❌     |
| PUT /labels/templates/{id}             | ✅    | ❌               | ❌             | ❌         | ❌     |
| DELETE /labels/templates/{id}          | ✅    | ❌               | ❌             | ❌         | ❌     |
| POST /labels/generate                  | ✅    | ✅               | ✅             | ✅         | ❌     |
| **Reports**                            |       |                  |                |            |        |
| GET /reports/dashboard                 | ✅    | ✅               | ✅             | ✅         | ✅     |
| GET /reports/near-expiry               | ✅    | ✅               | ✅             | ✅         | ✅     |
| GET /reports/lots/{id}/trace           | ✅    | ✅               | ✅             | ✅         | ✅     |
| GET /reports/qc                        | ✅    | ✅               | ✅             | ❌         | ❌     |
| GET /reports/inventory                 | ✅    | ✅               | ✅             | ✅         | ✅     |
| **Users**                              |       |                  |                |            |        |
| GET /users                             | ✅    | ❌               | ❌             | ❌         | ❌     |
| GET /users/me                          | ✅    | ✅               | ✅             | ✅         | ✅     |
| POST /users                            | ✅    | ❌               | ❌             | ❌         | ❌     |
| PUT /users/{id}                        | ✅    | ❌               | ❌             | ❌         | ❌     |
| PATCH /users/{id}/password             | ✅    | 🔐               | 🔐             | 🔐         | 🔐     |
| DELETE /users/{id}                     | ✅    | ❌               | ❌             | ❌         | ❌     |
| GET /users/{id}/activity               | ✅    | ❌               | ❌             | ❌         | ❌     |
| **Health**                             |       |                  |                |            |        |
| GET /health                            | ✅    | ✅               | ✅             | ✅         | ✅     |
