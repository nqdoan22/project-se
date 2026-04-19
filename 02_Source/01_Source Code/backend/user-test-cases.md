# Unit Test Cases - User Service

## Overview
This document outlines comprehensive unit test cases for the User Service, covering user account management, authentication integration, password management, and access control. Test cases validate user CRUD operations, role-based access, JWT integration, and security-related functionality.

---

## Test Cases Table

| Test ID | Test Case Name | Test Values | Tested Function | Auto Testing Functions |
|---|---|---|---|---|
| 135 | **LIST USERS - No Filter** | role: null, isActive: null | `listUsers()` | `listUsers_noFilter_callsFindAll()` |
| 136 | **LIST USERS - Filter by Role** | role: "Admin" | `listUsers()` | `listUsers_roleFilter_callsFindByRole()` |
| 137 | **LIST USERS - Filter by Active Status** | isActive: true | `listUsers()` | `listUsers_isActiveFilter_callsFindByIsActive()` |
| 138 | **LIST USERS - Filter by Role and Active Status** | role: "Production", isActive: false | `listUsers()` | `listUsers_roleAndIsActiveFilter_callsCombinedQuery()` |
| 139 | **GET USER BY ID - Success** | userId: valid UUID, username: "admin", role: "Viewer" | `getUserById()` | `getUserById_found_returnsUserResponse()` |
| 140 | **GET USER BY ID - Not Found** | userId: non-existent UUID | `getUserById()` | `getUserById_notFound_throwsResourceNotFoundException()` |
| 141 | **GET USER BY JWT SUB - Success** | jwtSub: "sub-123" (exists) | `getUserByJwtSub()` | `getUserByJwtSub_found_returnsUser()` |
| 142 | **GET USER BY JWT SUB - Not Found** | jwtSub: "unknown-sub" | `getUserByJwtSub()` | `getUserByJwtSub_notFound_throwsResourceNotFoundException()` |
| 143 | **CREATE USER - Success** | username: "newuser", email: "newuser@ims.com", password: "Password@1", role: "Production" | `createUser()` | `createUser_success_savesAndReturnsUser()` |
| 144 | **CREATE USER - Duplicate Username** | username: "existing_user" (already in use) | `createUser()` | `createUser_duplicateUsername_throwsBusinessException()` |
| 145 | **CREATE USER - Duplicate Email** | email: "taken@ims.com" (already in use) | `createUser()` | `createUser_duplicateEmail_throwsBusinessException()` |
| 146 | **UPDATE USER - Success** | userId: valid UUID, newUsername: "newname", newEmail: "newname@ims.com", newRole: "InventoryManager" | `updateUser()` | `updateUser_success_updatesFieldsAndSaves()` |
| 147 | **UPDATE USER - Not Found** | userId: non-existent UUID | `updateUser()` | `updateUser_notFound_throwsResourceNotFoundException()` |
| 148 | **CHANGE PASSWORD - As Admin (No Current Password Check)** | userId: valid UUID, currentPassword: null, newPassword: "NewPass@123", requesterRole: "Admin" | `changePassword()` | `changePassword_asAdmin_skipsCurrentPasswordCheck()` |
| 149 | **CHANGE PASSWORD - As Non-Admin with Correct Current** | userId: valid UUID, currentPassword: "correct_password", newPassword: "NewPass@123" | `changePassword()` | `changePassword_asNonAdmin_correctCurrentPassword_succeeds()` |
| 150 | **CHANGE PASSWORD - As Non-Admin with Wrong Current** | userId: valid UUID, currentPassword: "wrong_password" | `changePassword()` | `changePassword_asNonAdmin_wrongCurrentPassword_throwsBusinessException()` |
| 151 | **CHANGE PASSWORD - User Not Found** | userId: non-existent UUID | `changePassword()` | `changePassword_userNotFound_throwsResourceNotFoundException()` |
| 152 | **DEACTIVATE USER - Success** | userId: valid UUID, requesterUserId: different user | `deactivateUser()` | `deactivateUser_success_setsIsActiveFalse()` |
| 153 | **DEACTIVATE USER - Self-Deactivation Blocked** | userId: valid UUID, requesterUserId: same user (self) | `deactivateUser()` | `deactivateUser_selfDeactivation_throwsBusinessException()` |
| 154 | **DEACTIVATE USER - User Not Found** | userId: non-existent UUID | `deactivateUser()` | `deactivateUser_notFound_throwsResourceNotFoundException()` |
| 155 | **GET USER ACTIVITY - Success** | userId: valid UUID | `getUserActivity()` | `getUserActivity_found_returnsEmptyList()` |
| 156 | **GET USER ACTIVITY - User Not Found** | userId: non-existent UUID | `getUserActivity()` | `getUserActivity_notFound_throwsResourceNotFoundException()` |

---

## Test Case Categories

### 1. List Users Operations
- **No Filter**: Retrieve all users with pagination
- **Role Filter**: Find users with specific role (Admin, Production, etc.)
- **Active Status Filter**: Find active (isActive=true) or inactive (isActive=false) users
- **Combined Filters**: Filter by role AND active status simultaneously
- **Pagination**: Applied to all list operations

### 2. Get User Operations
- **By User ID**: Retrieve user by primary key
  - Success: Returns user details (ID, username, email, role, active status)
  - Not Found: Throws ResourceNotFoundException
- **By JWT Sub**: Retrieve user by JWT subject claim (OpenID Connect)
  - Maps JWT sub claim to user ID in system
  - Supports SSO authentication flow
  - Not Found: Throws ResourceNotFoundException

### 3. Create User Operations
- **Success Path**: Unique username and email, valid role, valid password
- **User Data**: Username, email, password, role, isActive (default true)
- **Password Handling**: Stored securely (hashed/encrypted)
- **Uniqueness Constraints**:
  - Username must be globally unique
  - Email must be globally unique
- **Business Logic Failures**:
  - Duplicate username (case-insensitive check)
  - Duplicate email (case-insensitive check)
- **Default Values**: New users created as isActive=true

### 4. Update User Operations
- **Success Path**: Valid user ID, updated field values
- **Field Updates**: Supports updating username, email, role
- **Resource Failures**: User does not exist
- **Note**: Password changes handled separately via changePassword operation

### 5. Change Password Operations
- **Admin Mode**: Admin users can reset any user's password without current password verification
  - Use case: Account recovery, user locked out
  - Admin changes require appropriate authorization level
- **Non-Admin Mode**: Regular users must verify current password before changing
  - Use case: Self-service password change
  - Current password verified against stored hash
  - Wrong current password: Throws BusinessException, no change applied
- **Password Security**: New password validated (strength requirements if enforced)
- **Resource Failures**: User does not exist
- **Data Persistence**: New password hashed and stored

### 6. Deactivate User Operations
- **Success Path**: Valid user ID, requester is different user
- **Status Change**: Sets user.isActive = false (soft delete)
- **Restrictions**:
  - Self-deactivation blocked (user cannot deactivate own account)
  - Prevents accidental lockout scenarios
- **Resource Failures**: User does not exist
- **Note**: Deactivation is reversible (different from hard delete)

### 7. Get User Activity Operations
- **Activity History**: Retrieves user's action/audit log
- **Resource Failures**: User does not exist
- **Data**: May include login times, operations performed, etc.
- **Empty Results**: Returns empty list if no activity recorded

---

## Business Rules Tested

**User Creation & Uniqueness:**
- Usernames must be globally unique (case-insensitive)
- Emails must be globally unique (case-insensitive)
- All new users created as active (isActive=true)
- Role assignment required at creation time

**User Authentication & SSO:**
- Users can be retrieved by JWT subject claim (SSO integration)
- Supports OpenID Connect / OAuth2 flows
- User mapped to internal system via JWT sub

**Password Management:**
- Admin users can reset other users' passwords without verification
- Non-admin users must verify current password before changing
- Password changes create new hash (not reversible to old password)
- Invalid current password prevents any change

**Account Deactivation:**
- Users can be deactivated (soft delete) by authorized users
- Users cannot deactivate their own accounts (prevent self-lockout)
- Deactivated users have isActive=false

**Role-Based Access:**
- Users assigned roles (Admin, Production, InventoryManager, Viewer, etc.)
- Roles control system access via @PreAuthorize annotations
- Filtering by role returns users with specific authority level

---

## User Roles

| Role | Description | Privileges |
|------|-------------|-----------|
| **Admin** | System administrator | Full system access, user management, password reset |
| **Production** | Production manager | Batch creation, component management |
| **InventoryManager** | Inventory specialist | Lot operations, transfers, adjustments |
| **QCAnalyst** | Quality control | QC test creation, result recording |
| **Viewer** | Read-only access | View data, generate reports, cannot modify |

---

## Test Data Patterns

- **User IDs**: UUIDs for user identifiers; "u1", "u2" (test fixtures)
- **Usernames**: "admin", "newuser", "existing_user" (unique per system)
- **Emails**: "admin@ims.com", "newuser@ims.com", "taken@ims.com" (unique per system)
- **Passwords**: "Password@1", "NewPass@123", "correct_password", "wrong_password" (test scenarios)
- **Roles**: Admin, Production, InventoryManager, QCAnalyst, Viewer (enumerated)
- **JWT Sub**: "sub-123", "unknown-sub" (OpenID Connect identifiers)

---

## Password Policy

- Minimum length: Enforced (specific length varies by system config)
- Complexity: May require uppercase, lowercase, numbers, special characters
- Hashing: Passwords hashed with salt (bcrypt or similar)
- History: Old passwords not reusable (if enforced)

---

## Integration Points

**Authentication Service:**
- JWT token validation decodes sub claim
- User mapped to internal system via JWT sub claim

**Authorization Service:**
- User role checked for @PreAuthorize annotations
- Role determines operation permissions

**Audit/Logging Service:**
- User activity tracked (login, operations performed)
- changePassword operations logged for compliance

---

## Access Control

**Who Can Perform Operations:**
- **List Users**: Admin or authorized users
- **Get User**: Self or Admin
- **Create User**: Admin only
- **Update User**: Self (limited fields) or Admin
- **Change Password**: Self (with current verification) or Admin (without)
- **Deactivate User**: Admin only (cannot self-deactivate)
- **Get Activity**: Self or Admin

---

## Security Considerations

**Password Security:**
- Passwords never transmitted in plaintext
- Current password verified before allowing change (non-admin)
- Failed password changes do not reveal whether old password was wrong vs other errors

**Account Lockout:**
- Self-deactivation prevented (cannot lock own account)
- Admin can reset if user locked out

**Audit Trail:**
- All user operations logged
- Password change attempts tracked
- Deactivation records who deactivated whom

---

## Notes

- All test cases assume request DTOs are structurally valid (Bean Validation annotations respected)
- Users are soft-deleted via isActive flag (records preserved for audit)
- JWT sub claim enables SSO/federated identity integration
- Admin password reset bypasses current password verification (emergency access)
- Each test verifies appropriate exception types (ResourceNotFoundException, BusinessException)
- Role-based access control enforced separately via @PreAuthorize annotations
- User activity may include login history, data modifications, report generation, etc.
