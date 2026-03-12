# Keycloak Setup Guide

This guide provides step-by-step instructions to set up Keycloak to connect with the Inventory Management System (IMS) application.

---

## Step 1: Install and Run Keycloak

### Option A: Using Docker (Easiest)

```bash
docker run -d \
  --name keycloak \
  -p 9090:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest \
  start-dev
```

### Option B: Download and Run Locally

1. Download from [keycloak.org](https://www.keycloak.org/downloads)
2. Extract and run:

```bash
cd keycloak-<version>/bin
./kc.sh start-dev
```

**Access Keycloak admin console:** http://localhost:9090
- **Username:** admin
- **Password:** admin

---

## Step 2: Create Realm "ims"

1. Go to **http://localhost:9090/admin** and log in
2. Click the dropdown next to "Keycloak" in the top-left corner
3. Click **Create Realm**
4. Enter realm name: `ims`
5. Click **Create**

---

## Step 3: Create Roles

In the **ims** realm:

1. In the left sidebar, click **Realm roles**
2. Click **Create role** button
3. Create the following roles one by one:
   - `Admin`
   - `InventoryManager`
   - `QualityControl`
   - `Production`

For each role:
- Enter the role name
- Click **Create**

---

## Step 4: Create OAuth2 Client

1. In the left sidebar, click **Clients**
2. Click **Create client**

### General Settings

- **Client ID:** `inventory-app`
- **Client type:** `OpenID Connect`
- Click **Next**

### Capability Config

- Enable: `Client authentication` (toggle ON)
- Leave other settings at default
- Click **Next**

### Login Settings

- **Valid redirect URIs:**
  ```
  http://localhost:5173/*
  http://localhost:3000/*
  ```
- **Valid post logout redirect URIs:**
  ```
  http://localhost:5173
  ```
- **Web origins:**
  ```
  http://localhost:5173
  ```
- Click **Save**

### Get Client Secret

1. Go to the **Credentials** tab
2. Copy the **Client Secret** (you'll need this for frontend integration)

---

## Step 5: Create Users

### Create Test User

1. In the left sidebar, click **Users**
2. Click **Add user**
3. Fill in user details:
   - **Username:** `testuser`
   - **Email:** `test@example.com`
   - **Email verified:** Toggle ON
   - Click **Create**

### Set User Password

1. Go to the **Credentials** tab of the created user
2. Click **Set password**
3. Enter password: (choose any password)
4. Confirm password: (same)
5. Toggle **Temporary** OFF
6. Click **Set password**

### Assign Roles to User

1. Go to the **Role mapping** tab
2. Click **Assign role**
3. Filter by "realm roles"
4. Select `Admin` (or other roles as needed)
5. Click **Assign**

Repeat this process to assign multiple roles if needed.

---

## Step 6: Verify Keycloak Configuration

### Test Token Endpoint

Use the following curl command to verify your Keycloak setup:

```bash
curl -X POST http://localhost:9090/realms/ims/protocol/openid-connect/token \
  -d "client_id=inventory-app" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "username=testuser" \
  -d "password=testuser_password" \
  -d "grant_type=password"
```

**Expected Response:**

You should receive a JSON response containing:
- `access_token` - JWT token with your user's roles in `realm_access.roles`
- `refresh_token`
- `expires_in`
- Other token metadata

### Verify JWT Token

You can decode the JWT to verify roles are included:

1. Go to [jwt.io](https://jwt.io)
2. Paste the `access_token` from the response
3. You should see in the payload:
   ```json
   {
     "realm_access": {
       "roles": ["Admin", "default-roles-ims"]
     }
   }
   ```

---

## Step 7: Connect Backend

Your backend is already configured to use Keycloak! No changes needed.

### Current Backend Configuration

**File:** `backend/src/main/resources/application.properties`

```properties
# Keycloak JWT
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/ims
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9090/realms/ims/protocol/openid-connect/certs
```

The backend will:
- Validate JWT tokens from Keycloak
- Extract roles from `realm_access.roles` claim
- Require authentication for all endpoints except `/api/v1/health`

---

## Step 8: Connect Frontend (Optional)

If you want to implement frontend login with Keycloak, follow these steps:

### Install Keycloak JS Adapter

```bash
cd frontend
npm install keycloak-js
```

### Update App.jsx

Replace or update your `src/App.jsx` with Keycloak initialization:

```javascript
import { useEffect, useState } from 'react';
import Keycloak from 'keycloak-js';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import api from './services/api';
import Sidebar from './components/Sidebar';
import MaterialsPage from './pages/MaterialsPage';
import LotsPage from './pages/LotsPage';
import QCTestsPage from './pages/QCTestsPage';
import BatchesPage from './pages/BatchesPage';
import LabelsPage from './pages/LabelsPage';
import DashboardPage from './pages/DashboardPage';
import UsersPage from './pages/UsersPage';

let keycloak;

export default function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    keycloak = new Keycloak({
      url: 'http://localhost:9090',
      realm: 'ims',
      clientId: 'inventory-app',
    });

    keycloak
      .init({
        onLoad: 'login-required',
        checkLoginIframe: false,
      })
      .then((authenticated) => {
        setIsAuthenticated(authenticated);
        if (authenticated) {
          // Add token to axios requests
          api.defaults.headers.common['Authorization'] = `Bearer ${keycloak.token}`;
          
          // Refresh token before expiry
          keycloak.onTokenExpired = () => {
            keycloak.updateToken(30).then(() => {
              api.defaults.headers.common['Authorization'] = `Bearer ${keycloak.token}`;
            });
          };
        }
        setIsLoading(false);
      })
      .catch((error) => {
        console.error('Keycloak initialization failed', error);
        setIsLoading(false);
      });
  }, []);

  if (isLoading) {
    return <div>Loading authentication...</div>;
  }

  if (!isAuthenticated) {
    return <div>Not authenticated</div>;
  }

  return (
    <BrowserRouter>
      <div className="app-layout">
        <Sidebar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Navigate to="/materials" replace />} />
            <Route path="/materials" element={<MaterialsPage />} />
            <Route path="/lots" element={<LotsPage />} />
            <Route path="/qctests" element={<QCTestsPage />} />
            <Route path="/batches" element={<BatchesPage />} />
            <Route path="/labels" element={<LabelsPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/users" element={<UsersPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export { keycloak };
```

### Add Logout Button

Update your `Sidebar.jsx` to include a logout button:

```javascript
import { keycloak } from '../App';

export default function Sidebar() {
  const handleLogout = () => {
    keycloak.logout({ redirectUri: 'http://localhost:5173' });
  };

  return (
    <aside className="sidebar">
      {/* ... existing sidebar content ... */}
      <button onClick={handleLogout}>Logout</button>
    </aside>
  );
}
```

---

## Summary: Current App Configuration

✅ **Backend is already configured for Keycloak:**
- Realm: `ims`
- Server URL: `http://localhost:9090`
- JWT validation from JWK set
- Role extraction from `realm_access.roles`
- Profiles:
  - **dev** - Uses mock authentication (no Keycloak needed)
  - **production** - Uses real Keycloak JWT validation

✅ **To use Keycloak:**
1. Follow Steps 1-6 above to set up Keycloak server
2. Switch from `dev` profile to production in `application.properties`
3. (Optional) Implement frontend login following Step 8

---

## Troubleshooting

### Backend still uses mock authentication
- Check `application.properties`: `spring.profiles.active=dev` should be changed to a different profile
- Rebuild backend: `./gradlew build && ./gradlew bootRun`

### JWT token validation fails
- Verify Keycloak is running on `http://localhost:9090`
- Check that realm is named `ims`
- Verify client ID is `inventory-app`
- Test token endpoint in Step 6

### Frontend redirects to Keycloak login but doesn't work
- Verify `Valid redirect URIs` include `http://localhost:5173/*`
- Check browser console for CORS errors
- Clear browser cookies and try again

### Token expiry issues
- Frontend code includes token refresh logic
- Backend will reject expired tokens automatically

---

## Additional Resources

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Keycloak Docker Hub](https://hub.docker.com/r/keycloak/keycloak)
- [Spring Security OAuth2 Docs](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Keycloak JS Adapter](https://www.keycloak.org/docs/latest/securing_apps/#_javascript_adapter)
