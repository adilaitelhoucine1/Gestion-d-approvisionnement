# 🔐 Dual Authentication - Simplified Guide

## ✅ What You Have

**TWO authentication methods:**

### 1. **Local Authentication** (Internal Users)
- Users in your MySQL database
- Login: `/api/auth/login`
- Username + Password
- Returns JWT token

### 2. **Keycloak Authentication** (External Users)
- Users in Keycloak
- Login: Browser flow via Keycloak
- Auto-synced to your DB with **NULL password**
- Returns JWT token

**NO external providers (Google, GitHub, etc.) - Just Keycloak users!**

---

## 🚀 How to Use

### Method 1: Local Login (Your existing users)

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
  "user": {...}
}
```

---

### Method 2: Keycloak Login (Keycloak users)

**Step 1:** Open browser:
```
http://localhost:8080/oauth2/authorization/keycloak
```

**Step 2:** You'll be redirected to Keycloak login page

**Step 3:** Login with Keycloak user:
- Username: `keycloak-admin`
- Password: `admin123`

**Step 4:** After login, you get JSON response:
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 86400000,
  "user": {
    "id": 5,
    "username": "keycloak-admin",
    "email": "keycloak@test.com",
    "firstName": "Keycloak",
    "lastName": "Admin"
  }
}
```

**Step 5:** User is automatically created in your database:
```sql
SELECT * FROM users WHERE username = 'keycloak-admin';
-- password column will be NULL
```

---

## 🔍 What Happens Behind the Scenes

### Keycloak Login Flow:

```
1. User clicks login → Redirected to Keycloak
2. User enters credentials in Keycloak
3. Keycloak validates user
4. Keycloak redirects back to your app
5. OAuth2LoginSuccessHandler receives user info
6. OAuth2UserSyncService checks if user exists in DB
   - If NO: Creates new user with NULL password
   - If YES: Updates last_login
7. Generate JWT token
8. Return token to user
```

---

## 📊 Database After Keycloak Login

```sql
SELECT id, username, email, password, first_name, last_name 
FROM users;
```

**Result:**
```
| id | username        | email              | password    | first_name | last_name |
|----|-----------------|--------------------|-----------  |------------|-----------|
| 1  | admin           | admin@test.com     | $2a$10$...  | Admin      | User      |
| 2  | keycloak-admin  | keycloak@test.com  | NULL        | Keycloak   | Admin     |
```

**Key Point:** Keycloak users have `password = NULL`

---

## 🧪 Testing

### Test 1: Local User
```bash
# Login
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username": "admin", "password": "password"}'

# Use token
curl -X GET 'http://localhost:8080/api/produits' \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

### Test 2: Keycloak User
```bash
# Open in browser
http://localhost:8080/oauth2/authorization/keycloak

# After login, copy the accessToken from response

# Use token
curl -X GET 'http://localhost:8080/api/produits' \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

---

## ✅ Summary

| Feature | Local Auth | Keycloak Auth |
|---------|-----------|---------------|
| **Endpoint** | `/api/auth/login` | `/oauth2/authorization/keycloak` |
| **User Storage** | MySQL | MySQL (synced) |
| **Password** | Hashed | NULL |
| **Login Method** | API call | Browser redirect |
| **Token** | JWT | JWT |
| **Use Case** | Internal users | External users |

---

## 🎯 Key Points

✅ **Local users** → Have password in DB  
✅ **Keycloak users** → Have NULL password in DB  
✅ **Both** → Get JWT token  
✅ **Both** → Can access all APIs  
✅ **Keycloak users** → Auto-created in DB on first login  

---

## 🔧 Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

---

**That's it! Simple dual authentication without external providers!** 🎉
