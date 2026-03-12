# Keycloak Setup Guide

## Step 1: Start Keycloak

From `02_Source/01_Source Code/`:

```bash
docker compose up keycloak -d
```

Wait ~30 seconds, then open: http://localhost:9090
Login with **admin / admin**

---

## Step 2: Create Realm

1. Click the realm dropdown (top-left, shows "Keycloak")
2. Click **Create realm**
3. Set **Realm name**: `ims`
4. Click **Create**

---

## Step 3: Create Client (Frontend OIDC)

1. Go to **Clients** → **Create client**
2. Fill in:

| Field | Value |
|---|---|
| Client ID | `ims-frontend` |
| Client type | `OpenID Connect` |

3. Click **Next**
4. Set:

| Field | Value |
|---|---|
| Client authentication | **OFF** (public client) |
| Standard flow | ON |
| Direct access grants | ON |

5. Click **Next**
6. Set:

| Field | Value |
|---|---|
| Valid redirect URIs | `http://localhost:5173/*` |
| Valid post-logout redirect URIs | `http://localhost:5173/*` |
| Web origins | `http://localhost:5173` |

7. Click **Save**

---

## Step 4: Create Realm Roles

Go to **Realm roles** → **Create role** (repeat 5 times):

- `Admin`
- `InventoryManager`
- `QualityControl`
- `Production`
- `Viewer`

---

## Step 5: Create Users & Assign Roles

For each user:
1. Go to **Users** → **Add user**
2. Set **Username** → **Create**
3. Go to **Credentials** tab → **Set password** → enter password → turn **Temporary** OFF → **Save**
4. Go to **Role mapping** tab → **Assign role** → select the role → **Assign**

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin@123` | `Admin` |
| `inv_manager` | `Admin@123` | `InventoryManager` |
| `qc_analyst` | `Admin@123` | `QualityControl` |
| `prod_operator` | `Admin@123` | `Production` |
| `viewer1` | `Admin@123` | `Viewer` |

---

## Verification

1. Start frontend: `npm run dev` inside `frontend/`
2. Browser should redirect to Keycloak login page
3. Login as `admin` / `Admin@123` → redirected back to the app
4. Sidebar shows username `admin` and a Logout button
5. Open DevTools → Network → any API request should have `Authorization: Bearer eyJ...`
