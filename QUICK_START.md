# 🚀 Quick Start - Dual Authentication System

## ✅ What's Implemented

Your application now has **COMPLETE DUAL AUTHENTICATION**:

1. **Local Auth** - Username/password (existing users)
2. **OAuth2/SSO** - Google, GitHub, Facebook (auto-sync to DB)

---

## 📦 Files Created

```
✅ service/auth/OAuth2UserSyncService.java
✅ security/OAuth2LoginSuccessHandler.java  
✅ controller/AuthTestController.java
✅ DUAL_AUTH_GUIDE.md (detailed guide)
✅ QUICK_START.md (this file)
```

---

## 🏃 Quick Start

### 1. Build & Run
```bash
cd /home/ad/Desktop/Youcode\ Project/Gestion-d-approvisionnement/gestion-stock
mvn clean install
mvn spring-boot:run
```

### 2. Test Local Auth
```bash
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username": "admin", "password": "password"}'
```

### 3. Test OAuth2 Auth
Open browser: `http://localhost:8080/oauth2/authorization/keycloak`

---

## 🔑 Key Points

### OAuth2 Users:
- ✅ Automatically created in your DB
- ✅ Password field is **NULL**
- ✅ Cannot use local login
- ✅ Must use OAuth2 provider
- ✅ Get JWT token after sync

### Local Users:
- ✅ Stored in your DB with password
- ✅ Login with `/api/auth/login`
- ✅ Get JWT token immediately

### Both Methods:
- ✅ Return same JWT token format
- ✅ Work with all your APIs
- ✅ Support role-based access

---

## 🧪 Test Endpoints

```bash
# Check authentication type
curl http://localhost:8080/api/test/auth-type \
  -H "Authorization: Bearer YOUR_TOKEN"

# Check current user
curl http://localhost:8080/api/test/whoami \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📊 Database After OAuth2 Login

```sql
SELECT id, username, email, password, first_name, last_name 
FROM users;
```

**Result:**
```
| id | username   | email              | password    | first_name | last_name |
|----|------------|--------------------|-------------|------------|-----------|
| 1  | admin      | admin@test.com     | $2a$10$...  | Admin      | User      |
| 2  | john.doe   | john@gmail.com     | NULL        | John       | Doe       |
| 3  | jane.smith | jane@github.com    | NULL        | Jane       | Smith     |
```

---

## 🎯 Next Steps

1. Configure Google OAuth in Keycloak
2. Configure GitHub OAuth in Keycloak
3. Test both authentication methods
4. Verify users are synced to database

---

## 📚 Full Documentation

See `DUAL_AUTH_GUIDE.md` for complete details.

---

**Ready to test!** 🎉
