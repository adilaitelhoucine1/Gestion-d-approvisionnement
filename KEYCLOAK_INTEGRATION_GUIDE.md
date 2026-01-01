# 🔐 Keycloak Integration - Complete Guide

## ✅ What Has Been Implemented

Your Spring Boot application now supports **DUAL AUTHENTICATION**:
1. **Custom JWT** (your existing system)
2. **Keycloak JWT** (new integration)

Both authentication methods work side-by-side without breaking existing functionality.

---

## 📁 Files Modified/Created

### Created:
- `src/main/java/com/tricol/gestionstock/security/HybridJwtAuthenticationFilter.java`

### Modified:
- `src/main/java/com/tricol/gestionstock/security/SecurityConfig.java`
- `src/main/resources/application.properties`

---

## 🔧 Keycloak Configuration

### Server Details:
- **URL**: http://localhost:8180
- **Admin Username**: admin
- **Admin Password**: admin

### Realm Configuration:
- **Realm Name**: gestion-stock-realm
- **Client ID**: gestion-stock-client
- **Client Secret**: 3Ne8gWKgJk6cgRGAGxoGaUTp4eaZxfff

### Roles Created:
- ADMIN
- USER
- MANAGER

### Test User:
- **Username**: keycloak-admin
- **Password**: admin123
- **Roles**: ADMIN

---

## 🚀 How to Run

### 1. Start Keycloak (if not running):
```bash
docker start keycloak
```

### 2. Build and Run Spring Boot:
```bash
mvn clean install
mvn spring-boot:run
```

---

## 🧪 Testing Both Authentication Systems

### Test 1: Custom JWT (Your Existing System)

**Login with your existing user:**
```bash
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "your_existing_username",
    "password": "your_password"
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

**Use the token:**
```bash
curl -X GET 'http://localhost:8080/api/produits' \
  -H 'Authorization: Bearer YOUR_CUSTOM_TOKEN'
```

---

### Test 2: Keycloak JWT

**Get Keycloak token:**
```bash
curl -X POST 'http://localhost:8180/realms/gestion-stock-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password' \
  -d 'client_id=gestion-stock-client' \
  -d 'client_secret=3Ne8gWKgJk6cgRGAGxoGaUTp4eaZxfff' \
  -d 'username=keycloak-admin' \
  -d 'password=admin123'
```

**Response:**
```json
{
  "access_token": "eyJhbGc...",
  "expires_in": 300,
  "refresh_token": "eyJhbGc...",
  "token_type": "Bearer"
}
```

**Use Keycloak token:**
```bash
curl -X GET 'http://localhost:8080/api/produits' \
  -H 'Authorization: Bearer KEYCLOAK_ACCESS_TOKEN'
```

---

## 🔍 How It Works

```
User Request with JWT Token
         ↓
HybridJwtAuthenticationFilter
         ↓
    ┌────┴────┐
    ↓         ↓
Custom JWT  Keycloak JWT
Validator   Validator
    ↓         ↓
    └────┬────┘
         ↓
   Authentication Success
         ↓
   Access Granted
```

**The filter tries:**
1. First: Validate as Custom JWT
2. If fails: Validate as Keycloak JWT
3. If both fail: Request is rejected

---

## 📊 Comparison

| Feature | Custom JWT | Keycloak JWT |
|---------|-----------|--------------|
| **Login Endpoint** | `/api/auth/login` | Keycloak server |
| **User Storage** | MySQL database | Keycloak database |
| **Token Format** | Your custom JWT | Keycloak JWT |
| **Roles** | From your DB | From Keycloak |
| **Use Case** | Existing users | New users/SSO |

---

## 🎯 Benefits

✅ **No Breaking Changes** - Existing users continue working  
✅ **Gradual Migration** - Move users to Keycloak over time  
✅ **SSO Ready** - Can add Google/Facebook login via Keycloak  
✅ **Centralized Auth** - Manage users in Keycloak UI  
✅ **Token Refresh** - Automatic token renewal  
✅ **Multi-Factor Auth** - Available in Keycloak  

---

## 🔐 Security Notes

- Keep client secret secure (don't commit to Git)
- Use HTTPS in production
- Configure proper CORS settings
- Set strong passwords for Keycloak admin
- Use proper database for Keycloak in production (not H2)

---

## 🐛 Troubleshooting

### Keycloak not accessible:
```bash
docker ps  # Check if running
docker logs keycloak  # Check logs
```

### Application fails to start:
- Check if Keycloak is running on port 8180
- Verify client secret in application.properties
- Check Maven dependencies are installed

### Token validation fails:
- Verify realm name matches
- Check client ID is correct
- Ensure user has roles assigned in Keycloak

---

## 📚 Next Steps

1. ✅ Test both authentication methods
2. ✅ Create more users in Keycloak
3. ✅ Configure role-based access in controllers using `@PreAuthorize`
4. ✅ Set up social login (Google, Facebook) in Keycloak
5. ✅ Configure production Keycloak with PostgreSQL

---

## 🆘 Support

If you encounter issues:
1. Check application logs
2. Check Keycloak logs: `docker logs keycloak`
3. Verify Keycloak configuration in admin console
4. Test token generation separately

---

**Integration completed successfully!** 🎉
