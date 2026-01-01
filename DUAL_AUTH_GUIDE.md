# 🔐 Dual Authentication System - Complete Implementation Guide

## ✅ System Overview

Your application now supports **TWO AUTHENTICATION METHODS**:

### 1. **Local Authentication (Internal Users)**
- Users stored in your MySQL database
- Login with username/password
- Endpoint: `/api/auth/login`
- Returns custom JWT token

### 2. **OAuth2/Keycloak SSO (External Users)**
- Users from Google, GitHub, Facebook, etc.
- Automatically synced to your database
- Password field is NULL for OAuth2 users
- Returns custom JWT token after sync

---

## 📁 Files Created/Modified

### ✅ Created:
1. `service/auth/OAuth2UserSyncService.java` - Syncs OAuth2 users to DB
2. `security/OAuth2LoginSuccessHandler.java` - Handles OAuth2 login success
3. `DUAL_AUTH_GUIDE.md` - This guide

### ✅ Modified:
1. `security/SecurityConfig.java` - Added OAuth2 login support
2. `application.properties` - Added OAuth2 client configuration

---

## 🔧 How It Works

```
┌─────────────────────────────────────────────────────────┐
│                  User Login Request                      │
└────────────────┬────────────────────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
┌──────────────┐   ┌──────────────────┐
│   Local      │   │   OAuth2/SSO     │
│   Login      │   │   (Keycloak)     │
└──────┬───────┘   └────────┬─────────┘
       │                    │
       │                    ▼
       │           ┌─────────────────┐
       │           │ Check if user   │
       │           │ exists in DB?   │
       │           └────┬────────┬───┘
       │                │        │
       │                │ No     │ Yes
       │                ▼        ▼
       │           ┌────────┐ ┌────────┐
       │           │ Create │ │ Update │
       │           │ User   │ │ User   │
       │           └────┬───┘ └───┬────┘
       │                │         │
       └────────────────┴─────────┘
                        │
                        ▼
              ┌──────────────────┐
              │ Generate JWT     │
              │ Token            │
              └─────────┬────────┘
                        │
                        ▼
              ┌──────────────────┐
              │ Return Token     │
              │ to Client        │
              └──────────────────┘
```

---

## 🚀 Setup Instructions

### STEP 1: Configure Google OAuth (Optional)

1. Go to: https://console.cloud.google.com/
2. Create project → APIs & Services → Credentials
3. Create OAuth 2.0 Client ID
4. **Authorized redirect URIs**:
   ```
   http://localhost:8180/realms/gestion-stock-realm/broker/google/endpoint
   ```
5. Copy Client ID and Secret

**In Keycloak:**
- Identity providers → Add provider → Google
- Paste Client ID and Secret
- Save

### STEP 2: Configure GitHub OAuth (Optional)

1. Go to: https://github.com/settings/developers
2. New OAuth App
3. **Authorization callback URL**:
   ```
   http://localhost:8180/realms/gestion-stock-realm/broker/github/endpoint
   ```
4. Copy Client ID and Secret

**In Keycloak:**
- Identity providers → Add provider → GitHub
- Paste Client ID and Secret
- Save

### STEP 3: Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

---

## 🧪 Testing

### Test 1: Local Authentication (Internal Users)

**Request:**
```bash
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "roles": ["ADMIN"]
  }
}
```

---

### Test 2: OAuth2/SSO Authentication

**Browser Flow:**

1. Open: `http://localhost:8080/oauth2/authorization/keycloak`
2. You'll see Keycloak login page with options:
   - 🔵 Sign in with Google
   - 🔵 Sign in with GitHub
   - 🔵 Sign in with Facebook
3. Click any provider
4. Login with your account
5. **Automatic Process:**
   - User is authenticated by provider
   - Keycloak receives user info
   - Your app receives OAuth2 user
   - `OAuth2UserSyncService` checks if user exists in DB
   - If not exists: Creates new user with NULL password
   - If exists: Updates last login
   - Generates JWT token
   - Returns token to client

**Response (JSON):**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 86400000,
  "user": {
    "id": 5,
    "username": "john.doe",
    "email": "john.doe@gmail.com",
    "firstName": "John",
    "lastName": "Doe",
    "provider": "google"
  }
}
```

---

## 📊 Database Schema

### Users Table After OAuth2 Login:

| id | username | email | password | first_name | last_name | role_id | last_login |
|----|----------|-------|----------|------------|-----------|---------|------------|
| 1 | admin | admin@test.com | $2a$10$... | Admin | User | 1 | 2024-01-15 |
| 2 | john.doe | john@gmail.com | **NULL** | John | Doe | 2 | 2024-01-16 |
| 3 | jane.smith | jane@github.com | **NULL** | Jane | Smith | 2 | 2024-01-16 |

**Note:** OAuth2 users have `password = NULL`

---

## 🔍 Key Features

### OAuth2UserSyncService

**Responsibilities:**
1. Check if user exists by email
2. If exists: Update last login
3. If not exists: Create new user with:
   - Username from OAuth2 provider
   - Email from OAuth2 provider
   - First name and last name
   - **NULL password** (important!)
   - Default role: USER
   - Enabled account

**Code Logic:**
```java
@Transactional
public UserApp syncOAuth2User(OAuth2User oauth2User, String provider) {
    String email = oauth2User.getAttribute("email");
    
    return userAppRepository.findByEmail(email)
            .map(existingUser -> updateExistingUser(existingUser, oauth2User))
            .orElseGet(() -> createNewUser(oauth2User, username, email, provider));
}
```

---

### OAuth2LoginSuccessHandler

**Responsibilities:**
1. Receive OAuth2 authentication
2. Call `OAuth2UserSyncService` to sync user
3. Generate JWT tokens (access + refresh)
4. Return JSON response with tokens

**Flow:**
```
OAuth2 Success → Sync User → Generate JWT → Return JSON
```

---

## 🎯 Authentication Comparison

| Feature | Local Auth | OAuth2/SSO |
|---------|-----------|------------|
| **Endpoint** | `/api/auth/login` | `/oauth2/authorization/keycloak` |
| **User Storage** | MySQL | MySQL (synced) |
| **Password** | Required | NULL |
| **Registration** | Manual | Automatic |
| **Token Type** | Custom JWT | Custom JWT |
| **Providers** | N/A | Google, GitHub, Facebook, etc. |
| **Use Case** | Internal users | External users |

---

## 🔐 Security Considerations

### Local Users:
- ✅ Password is hashed with BCrypt
- ✅ Can change password
- ✅ Can reset password
- ✅ Stored in your database

### OAuth2 Users:
- ✅ No password stored (NULL)
- ✅ Cannot login with local auth
- ✅ Must use OAuth2 provider
- ✅ Automatically synced to DB
- ✅ Can be assigned roles/permissions

---

## 📝 API Endpoints

### Local Authentication:
```
POST /api/auth/login          - Login with username/password
POST /api/auth/register       - Register new user
POST /api/auth/refresh        - Refresh token
GET  /api/auth/me             - Get current user
```

### OAuth2 Authentication:
```
GET  /oauth2/authorization/keycloak  - Start OAuth2 flow
GET  /login/oauth2/code/keycloak     - OAuth2 callback (automatic)
```

### Protected Resources:
```
GET  /api/produits            - Requires JWT token (from either auth method)
GET  /api/fournisseurs        - Requires JWT token
POST /api/commandes           - Requires JWT token
```

---

## 🐛 Troubleshooting

### Issue: OAuth2 user not created in DB

**Check:**
1. Is `OAuth2UserSyncService` being called?
2. Check application logs for errors
3. Verify default role "USER" exists in database
4. Check database connection

### Issue: JWT token not generated after OAuth2 login

**Check:**
1. Is `OAuth2LoginSuccessHandler` configured?
2. Check `JwtUtils` is working
3. Verify user was synced to database

### Issue: Cannot login with OAuth2

**Check:**
1. Keycloak is running: `docker ps`
2. Identity provider configured in Keycloak
3. Redirect URIs match exactly
4. Client ID and Secret are correct

---

## ✅ Verification Checklist

- [ ] Keycloak running on port 8180
- [ ] Realm created: gestion-stock-realm
- [ ] Client created: gestion-stock-client
- [ ] Identity providers configured (Google, GitHub, etc.)
- [ ] OAuth2UserSyncService created
- [ ] OAuth2LoginSuccessHandler created
- [ ] SecurityConfig updated with OAuth2 support
- [ ] application.properties updated
- [ ] Default role "USER" exists in database
- [ ] Application builds successfully
- [ ] Local login works
- [ ] OAuth2 login works
- [ ] Users synced to database

---

## 🎉 Success Criteria

✅ **Local users** can login with username/password  
✅ **OAuth2 users** can login with Google/GitHub/Facebook  
✅ **OAuth2 users** are automatically created in database  
✅ **OAuth2 users** have NULL password  
✅ **Both auth methods** return JWT tokens  
✅ **JWT tokens** work for all API endpoints  
✅ **Users** can be managed in database  

---

## 📚 Next Steps

1. ✅ Add more OAuth2 providers (Microsoft, LinkedIn)
2. ✅ Implement user profile management
3. ✅ Add role-based access control
4. ✅ Implement account linking (link OAuth2 to existing account)
5. ✅ Add audit logging for OAuth2 logins
6. ✅ Configure production Keycloak with PostgreSQL

---

**Implementation Complete!** 🎉

Your application now supports both local and OAuth2/SSO authentication with automatic user synchronization to your database.
