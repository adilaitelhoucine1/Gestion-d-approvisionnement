# JWT Authentication System - Documentation

## 📋 Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Configuration](#configuration)
4. [Workflow](#workflow)
5. [Components](#components)
6. [Security Flow](#security-flow)
7. [Token Management](#token-management)
8. [Error Handling](#error-handling)

---

## 🎯 Overview

This document explains the JWT (JSON Web Token) authentication system implemented for the Tricol Stock Management Application. The system provides secure authentication and authorization using Spring Security and JWT tokens.

### Key Features
- **Stateless Authentication**: No session management required
- **JWT-based Security**: Access tokens and refresh tokens
- **Role-based Authorization**: ADMIN, RESPONSABLE_ACHATS, MAGASINIER, CHEF_ATELIER
- **Permission-based Access Control**: Dynamic permissions per user
- **Secure Token Handling**: HMAC-SHA algorithm with secret key
- **Comprehensive Error Handling**: Proper HTTP responses for unauthorized access

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT                               │
│  (Web Browser / Mobile App / Postman)                      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ HTTP Request + JWT Token
                     │ (Authorization: Bearer <token>)
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              JWT Authentication Filter                      │
│  - Extracts JWT from Authorization header                  │
│  - Validates token                                          │
│  - Loads user details                                       │
│  - Sets authentication in SecurityContext                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ Valid Token
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Security Filter Chain                   │
│  - Authorization checks                                     │
│  - Permission validation                                    │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ Authorized
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                 REST Controllers                            │
│  - Process business logic                                   │
│  - Return response                                          │
└─────────────────────────────────────────────────────────────┘
```

---

## ⚙️ Configuration

### application.properties

```properties
# JWT Configuration
app.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D635166546A576E5A7234753778214125442A
app.jwt.expiration-ms=86400000
app.jwt.refresh-expiration-ms=604800000
```

### Configuration Explained

| Property | Value | Description |
|----------|-------|-------------|
| `app.jwt.secret` | Base64 encoded string | Secret key used to sign JWT tokens (HMAC-SHA-512) |
| `app.jwt.expiration-ms` | 86400000 (24 hours) | Access token expiration time in milliseconds |
| `app.jwt.refresh-expiration-ms` | 604800000 (7 days) | Refresh token expiration time in milliseconds |

#### 🔐 Security Note
- The JWT secret is a **256-bit key** encoded in Base64
- This key should be **stored securely** in production (environment variables, secrets manager)
- **Never commit** production secrets to version control
- The secret is used with **HMAC-SHA-512** algorithm for signing tokens

#### ⏰ Token Expiration Times
- **Access Token (24 hours)**: Short-lived for security
  - User must re-authenticate after 24 hours
  - Reduces risk if token is compromised
  
- **Refresh Token (7 days)**: Longer-lived for user convenience
  - Used to obtain new access tokens without re-login
  - Stored securely by client
  - Can be revoked by server

---

## 🔄 Workflow

### 1. User Registration Flow

```
┌──────┐                ┌──────────┐                ┌──────────┐
│Client│                │  Server  │                │ Database │
└──┬───┘                └────┬─────┘                └────┬─────┘
   │                         │                           │
   │ POST /api/auth/register │                           │
   │ {username, password,    │                           │
   │  email, firstName,      │                           │
   │  lastName}              │                           │
   ├────────────────────────>│                           │
   │                         │                           │
   │                         │ Hash password             │
   │                         │ (BCrypt)                  │
   │                         │                           │
   │                         │ Save user                 │
   │                         │ (No role assigned)        │
   │                         ├──────────────────────────>│
   │                         │                           │
   │                         │ User saved                │
   │                         │<──────────────────────────┤
   │                         │                           │
   │ 201 Created             │                           │
   │ {message: "User         │                           │
   │  registered"}           │                           │
   │<────────────────────────┤                           │
   │                         │                           │
```

**Key Points:**
- New users have **NO role** by default
- Password is hashed using **BCrypt**
- User cannot access any protected resources until admin assigns a role

---

### 2. User Login Flow

```
┌──────┐                ┌──────────┐                ┌──────────┐
│Client│                │  Server  │                │ Database │
└──┬───┘                └────┬─────┘                └────┬─────┘
   │                         │                           │
   │ POST /api/auth/login    │                           │
   │ {username, password}    │                           │
   ├────────────────────────>│                           │
   │                         │                           │
   │                         │ Load user by username     │
   │                         ├──────────────────────────>│
   │                         │                           │
   │                         │ User + Roles + Permissions│
   │                         │<──────────────────────────┤
   │                         │                           │
   │                         │ Verify password           │
   │                         │ (BCrypt.matches)          │
   │                         │                           │
   │                         │ Generate Access Token     │
   │                         │ - Subject: username       │
   │                         │ - IssuedAt: now           │
   │                         │ - Expiration: now + 24h   │
   │                         │ - Sign with secret        │
   │                         │                           │
   │                         │ Generate Refresh Token    │
   │                         │ - Subject: username       │
   │                         │ - IssuedAt: now           │
   │                         │ - Expiration: now + 7d    │
   │                         │ - Sign with secret        │
   │                         │                           │
   │ 200 OK                  │                           │
   │ {                       │                           │
   │   accessToken: "eyJ...", │                           │
   │   refreshToken: "eyJ..",│                           │
   │   tokenType: "Bearer",  │                           │
   │   expiresIn: 86400000,  │                           │
   │   user: {               │                           │
   │     id, username, email,│                           │
   │     roles, permissions  │                           │
   │   }                     │                           │
   │ }                       │                           │
   │<────────────────────────┤                           │
   │                         │                           │
```

**Key Points:**
- Server validates username and password
- Two tokens generated: **Access Token** (short-lived) and **Refresh Token** (long-lived)
- Tokens contain username in the **subject** claim
- Tokens are **signed** with the secret key
- User information returned includes roles and permissions

---

### 3. Authenticated Request Flow

```
┌──────┐                ┌──────────┐                ┌──────────┐
│Client│                │  Server  │                │ Database │
└──┬───┘                └────┬─────┘                └────┬─────┘
   │                         │                           │
   │ GET /api/produits       │                           │
   │ Authorization: Bearer   │                           │
   │ eyJhbGciOiJIUzUxMi...   │                           │
   ├────────────────────────>│                           │
   │                         │                           │
   │                  ┌──────▼────────┐                  │
   │                  │ JWT Filter    │                  │
   │                  │               │                  │
   │                  │ 1. Extract JWT│                  │
   │                  │    from header│                  │
   │                  │               │                  │
   │                  │ 2. Validate:  │                  │
   │                  │    - Signature│                  │
   │                  │    - Expiry   │                  │
   │                  │    - Format   │                  │
   │                  │               │                  │
   │                  │ 3. Extract    │                  │
   │                  │    username   │                  │
   │                  └──────┬────────┘                  │
   │                         │                           │
   │                         │ Load UserDetails          │
   │                         ├──────────────────────────>│
   │                         │                           │
   │                         │ User + Authorities        │
   │                         │<──────────────────────────┤
   │                         │                           │
   │                  ┌──────▼────────┐                  │
   │                  │ Set Security  │                  │
   │                  │ Context       │                  │
   │                  │               │                  │
   │                  │ Authentication│                  │
   │                  │ = User +      │                  │
   │                  │   Authorities │                  │
   │                  └──────┬────────┘                  │
   │                         │                           │
   │                  ┌──────▼────────┐                  │
   │                  │ Authorization │                  │
   │                  │ Check         │                  │
   │                  │               │                  │
   │                  │ Check if user │                  │
   │                  │ has required  │                  │
   │                  │ permission    │                  │
   │                  └──────┬────────┘                  │
   │                         │                           │
   │                         │ Execute business logic    │
   │                         ├──────────────────────────>│
   │                         │                           │
   │                         │ Query results             │
   │                         │<──────────────────────────┤
   │                         │                           │
   │ 200 OK                  │                           │
   │ [List of products]      │                           │
   │<────────────────────────┤                           │
   │                         │                           │
```

**Key Points:**
- Client includes token in **Authorization header**: `Bearer <token>`
- **JwtAuthenticationFilter** intercepts request
- Token is validated (signature, expiration, format)
- Username extracted from token's **subject** claim
- User details loaded from database
- Authentication set in **SecurityContext**
- Spring Security checks permissions
- If authorized, request proceeds to controller

---

### 4. Token Refresh Flow

```
┌──────┐                ┌──────────┐                ┌──────────┐
│Client│                │  Server  │                │ Database │
└──┬───┘                └────┬─────┘                └────┬─────┘
   │                         │                           │
   │ Access Token Expired    │                           │
   │ (After 24 hours)        │                           │
   │                         │                           │
   │ POST /api/auth/refresh  │                           │
   │ {refreshToken: "eyJ..."} │                           │
   ├────────────────────────>│                           │
   │                         │                           │
   │                         │ Validate Refresh Token    │
   │                         │ - Check signature         │
   │                         │ - Check expiration        │
   │                         │                           │
   │                         │ Extract username          │
   │                         │                           │
   │                         │ Load user                 │
   │                         ├──────────────────────────>│
   │                         │                           │
   │                         │ User data                 │
   │                         │<──────────────────────────┤
   │                         │                           │
   │                         │ Generate new Access Token │
   │                         │ (Fresh 24 hours)          │
   │                         │                           │
   │                         │ Generate new Refresh Token│
   │                         │ (Fresh 7 days)            │
   │                         │                           │
   │ 200 OK                  │                           │
   │ {                       │                           │
   │   accessToken: "new...",│                           │
   │   refreshToken: "new..",│                           │
   │   expiresIn: 86400000   │                           │
   │ }                       │                           │
   │<────────────────────────┤                           │
   │                         │                           │
   │ Update tokens in storage│                           │
   │                         │                           │
```

**Key Points:**
- When access token expires, client uses refresh token
- Refresh token validated (signature, expiration)
- New access token generated (fresh 24 hours)
- New refresh token also generated (token rotation)
- Client updates both tokens in storage
- No need for user to re-login

---

## 🧩 Components

### 1. JwtUtils.java

**Purpose**: Core utility class for JWT token operations

**Methods**:

| Method | Description | Returns |
|--------|-------------|---------|
| `getSigningKey()` | Decodes Base64 secret and creates HMAC key | SecretKey |
| `generateAccessToken(username)` | Creates access token with 24h expiration | String (JWT) |
| `generateRefreshToken(username)` | Creates refresh token with 7d expiration | String (JWT) |
| `getUsernameFromToken(token)` | Extracts username from token's subject claim | String |
| `validateToken(token)` | Validates token signature and expiration | boolean |

**Token Structure**:
```json
{
  "header": {
    "alg": "HS512",
    "typ": "JWT"
  },
  "payload": {
    "sub": "username",
    "iat": 1703433600,
    "exp": 1703520000
  },
  "signature": "HMACSHA512(base64UrlEncode(header) + '.' + base64UrlEncode(payload), secret)"
}
```

**Token Claims**:
- **sub** (subject): Username of the authenticated user
- **iat** (issued at): Timestamp when token was created
- **exp** (expiration): Timestamp when token expires

---

### 2. JwtAuthenticationFilter.java

**Purpose**: Intercepts HTTP requests and validates JWT tokens

**Extends**: `OncePerRequestFilter` - Ensures filter runs once per request

**Process**:
1. **Extract Token**: Parses `Authorization: Bearer <token>` header
2. **Validate Token**: Checks signature and expiration
3. **Load User**: Retrieves user details from database
4. **Set Authentication**: Places user in SecurityContext
5. **Continue Chain**: Passes request to next filter

**Code Flow**:
```java
doFilterInternal(request, response, filterChain) {
    // 1. Parse JWT from header
    jwt = parseJwt(request)
    
    if (jwt != null && jwtUtils.validateToken(jwt)) {
        // 2. Extract username
        username = jwtUtils.getUsernameFromToken(jwt)
        
        // 3. Load user details
        userDetails = userDetailsService.loadUserByUsername(username)
        
        // 4. Create authentication object
        authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        )
        
        // 5. Set in security context
        SecurityContextHolder.getContext().setAuthentication(authentication)
    }
    
    // 6. Continue filter chain
    filterChain.doFilter(request, response)
}
```

---

### 3. JwtAuthenticationEntryPoint.java

**Purpose**: Handles unauthorized access attempts

**Implements**: `AuthenticationEntryPoint`

**Response Format**:
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/produits"
}
```

**When Triggered**:
- No JWT token provided
- Invalid JWT token
- Expired JWT token
- User tries to access protected resource without authentication

---

## 🔒 Security Flow

### Complete Authentication Flow

```
┌─────────────────────────────────────────────────────────────┐
│                     1. USER REGISTRATION                    │
│                                                             │
│  Client ─────> POST /api/auth/register                     │
│                {username, password, email, ...}             │
│                                                             │
│  Server ─────> Hash password (BCrypt)                      │
│         ─────> Save user (NO ROLE)                         │
│         ─────> Return success message                      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  2. ADMIN ASSIGNS ROLE                      │
│                                                             │
│  Admin  ─────> Assign role: MAGASINIER                     │
│         ─────> User gets default permissions for role      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                      3. USER LOGIN                          │
│                                                             │
│  Client ─────> POST /api/auth/login                        │
│                {username, password}                         │
│                                                             │
│  Server ─────> Verify credentials                          │
│         ─────> Generate Access Token (24h)                 │
│         ─────> Generate Refresh Token (7d)                 │
│         ─────> Return tokens + user info                   │
│                                                             │
│  Client ─────> Store tokens (localStorage/sessionStorage)  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  4. AUTHENTICATED REQUEST                   │
│                                                             │
│  Client ─────> GET /api/produits                           │
│                Authorization: Bearer <access_token>         │
│                                                             │
│  Filter ─────> Extract & validate token                    │
│         ─────> Load user details                           │
│         ─────> Set SecurityContext                         │
│                                                             │
│  Security ───> Check permissions                           │
│           ───> Allow/Deny access                           │
│                                                             │
│  Controller ─> Execute business logic                      │
│             ─> Return response                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    5. TOKEN REFRESH                         │
│                                                             │
│  Client ─────> Access token expired                        │
│         ─────> POST /api/auth/refresh                      │
│                {refreshToken}                               │
│                                                             │
│  Server ─────> Validate refresh token                      │
│         ─────> Generate new access token                   │
│         ─────> Generate new refresh token                  │
│         ─────> Return new tokens                           │
│                                                             │
│  Client ─────> Update stored tokens                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                       6. LOGOUT                             │
│                                                             │
│  Client ─────> POST /api/auth/logout                       │
│         ─────> Clear stored tokens                         │
│                                                             │
│  Server ─────> (Optional) Blacklist refresh token          │
│         ─────> Return success                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎫 Token Management

### Access Token vs Refresh Token

| Aspect | Access Token | Refresh Token |
|--------|-------------|---------------|
| **Purpose** | Access protected resources | Obtain new access tokens |
| **Lifetime** | 24 hours (86400000 ms) | 7 days (604800000 ms) |
| **Usage** | Every API request | Only for token refresh |
| **Storage** | Memory / SessionStorage | Secure storage (HttpOnly cookie recommended) |
| **Expiration Impact** | User must refresh token | User must re-login |
| **Security Risk** | Lower (short-lived) | Higher (long-lived) |

### Token Storage Best Practices

**Client-Side Storage Options**:

1. **Memory (Recommended for Access Token)**
   - Pros: Most secure, cleared on tab close
   - Cons: Lost on refresh
   ```javascript
   let accessToken = null; // In-memory variable
   ```

2. **SessionStorage (Access Token)**
   - Pros: Cleared on tab close, XSS protection with sanitization
   - Cons: Vulnerable to XSS attacks
   ```javascript
   sessionStorage.setItem('accessToken', token);
   ```

3. **HttpOnly Cookie (Refresh Token)**
   - Pros: Not accessible via JavaScript, CSRF protection with SameSite
   - Cons: Requires server-side management
   ```java
   Cookie cookie = new Cookie("refreshToken", token);
   cookie.setHttpOnly(true);
   cookie.setSecure(true);
   cookie.setPath("/api/auth/refresh");
   ```

4. **LocalStorage (NOT Recommended)**
   - Pros: Persists across sessions
   - Cons: Vulnerable to XSS, never use for sensitive tokens

---

## ❌ Error Handling

### Common Error Scenarios

#### 1. Missing Token
**Request**:
```http
GET /api/produits HTTP/1.1
Host: localhost:8080
```

**Response**:
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/produits"
}
```

#### 2. Invalid Token
**Request**:
```http
GET /api/produits HTTP/1.1
Authorization: Bearer invalid.token.here
```

**Log Output**:
```
ERROR JwtUtils - Invalid JWT token: JWT strings must contain exactly 2 period characters
```

**Response**: 401 Unauthorized (same as above)

#### 3. Expired Token
**Request**:
```http
GET /api/produits HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzUxMi... (expired)
```

**Log Output**:
```
ERROR JwtUtils - JWT token is expired: JWT expired at 2024-12-23T10:00:00Z
```

**Response**: 401 Unauthorized

**Solution**: Use refresh token to get new access token

#### 4. Malformed Token
**Request**:
```http
GET /api/produits HTTP/1.1
Authorization: Bearer notajwtoken
```

**Log Output**:
```
ERROR JwtUtils - Invalid JWT token: JWT strings must contain exactly 2 period characters
```

#### 5. Wrong Signature
**Request**:
```http
GET /api/produits HTTP/1.1
Authorization: Bearer eyJ... (signed with different secret)
```

**Log Output**:
```
ERROR JwtUtils - Invalid JWT token: JWT signature does not match
```

---

## 🔐 Security Best Practices

### Current Implementation

✅ **Strong Secret Key**: 512-bit key for HMAC-SHA-512
✅ **Short Access Token Lifetime**: 24 hours
✅ **Long Refresh Token**: 7 days for user convenience
✅ **Token Validation**: Comprehensive validation with error logging
✅ **Stateless Authentication**: No server-side session storage
✅ **Proper Error Handling**: Detailed logging without exposing sensitive info

### Recommendations for Production

1. **Environment Variables**
   ```properties
   # Don't hardcode in application.properties
   app.jwt.secret=${JWT_SECRET}
   app.jwt.expiration-ms=${JWT_EXPIRATION:86400000}
   ```

2. **HTTPS Only**
   ```properties
   server.ssl.enabled=true
   server.ssl.key-store=classpath:keystore.p12
   server.ssl.key-store-password=${KEYSTORE_PASSWORD}
   ```

3. **Token Blacklist** (for logout)
   - Store revoked tokens in Redis with TTL
   - Check blacklist in JWT filter

4. **Rate Limiting**
   - Limit login attempts per IP
   - Limit token refresh requests

5. **Audit Logging**
   - Log all authentication attempts
   - Log token refresh operations
   - Log authorization failures

---

## 📝 Usage Examples

### 1. Register New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john@example.com",
    "password": "securePass123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Response**:
```json
{
  "message": "User registered successfully! Waiting for admin approval."
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "password": "securePass123"
  }'
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZSIsImlhdCI6MTcwMzQzMzYwMCwiZXhwIjoxNzAzNTIwMDAwfQ.SflKxwRJSM...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZSIsImlhdCI6MTcwMzQzMzYwMCwiZXhwIjoxNzA0MDM4NDAwfQ.SflKxwRJSM...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "john.doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["MAGASINIER"],
    "permissions": ["CONSULTER_STOCK", "CREER_BON_SORTIE", "VALIDER_BON_SORTIE"]
  }
}
```

### 3. Access Protected Resource

```bash
curl -X GET http://localhost:8080/api/produits \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

### 4. Refresh Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZSIsImlhdCI6MTcwMzQzMzYwMCwiZXhwIjoxNzA0MDM4NDAwfQ..."
  }'
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9... (new token)",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9... (new refresh token)",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

---

## 🎓 Key Concepts

### What is JWT?

JWT (JSON Web Token) is a compact, URL-safe token format that contains:
- **Header**: Algorithm and token type
- **Payload**: Claims (user data)
- **Signature**: Verification hash

### Why JWT?

1. **Stateless**: Server doesn't store session data
2. **Scalable**: Works across multiple servers
3. **Secure**: Digitally signed, tamper-proof
4. **Efficient**: Compact size, fast validation
5. **Cross-Domain**: Works with CORS

### JWT vs Sessions

| Aspect | JWT | Sessions |
|--------|-----|----------|
| **Storage** | Client-side | Server-side |
| **Scalability** | Excellent | Requires sticky sessions |
| **Revocation** | Difficult (needs blacklist) | Easy (delete session) |
| **Size** | Larger (sent with every request) | Smaller (just session ID) |
| **Performance** | Fast validation | Database lookup |

---

## 🚀 Next Steps

After understanding this JWT implementation, proceed with:

1. ✅ Implement UserDetailsService
2. ✅ Create Authentication Service
3. ✅ Build Authentication Controller
4. ✅ Configure Spring Security
5. ✅ Add Role and Permission entities
6. ✅ Implement dynamic permission system
7. ✅ Add audit logging
8. ✅ Write unit tests

---

## 📚 References

- [JWT.io](https://jwt.io/) - JWT Debugger and Documentation
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [RFC 7519 - JWT Specification](https://tools.ietf.org/html/rfc7519)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

---

**Document Version**: 1.0  
**Last Updated**: December 24, 2024  
**Author**: Tricol Development Team

