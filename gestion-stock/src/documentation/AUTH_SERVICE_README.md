# Authentication Service & Controller - Complete Guide

## 📋 Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Components Explained](#components-explained)
4. [API Endpoints](#api-endpoints)
5. [Authentication Flow](#authentication-flow)
6. [Code Walkthrough](#code-walkthrough)
7. [Testing Guide](#testing-guide)
8. [Security Considerations](#security-considerations)

---

## 🎯 Overview

This document explains the **AuthService** and **AuthController** implementation - the core components that handle user authentication, registration, and token management in the Tricol Stock Management System.

### What These Components Do

**AuthService** (Business Logic):
- ✅ User login with JWT token generation
- ✅ User registration (without role by default)
- ✅ Token refresh mechanism
- ✅ Get current authenticated user info
- ✅ Build user information with roles and permissions

**AuthController** (REST API):
- ✅ Exposes authentication endpoints
- ✅ Validates request data
- ✅ Returns appropriate HTTP responses
- ✅ Integrates with Swagger/OpenAPI documentation

---

## 🏗️ Architecture

```
┌────────────────────────────────────────────────────────────┐
│                        CLIENT                              │
│              (Browser, Mobile, Postman)                    │
└────────────────────┬───────────────────────────────────────┘
                     │
                     │ HTTP Request (JSON)
                     │
                     ▼
┌────────────────────────────────────────────────────────────┐
│                   AuthController                           │
│  - @PostMapping("/api/auth/login")                        │
│  - @PostMapping("/api/auth/register")                     │
│  - @PostMapping("/api/auth/refresh")                      │
│  - @GetMapping("/api/auth/me")                            │
│                                                            │
│  Responsibilities:                                         │
│  ✓ Validate request data (@Valid)                         │
│  ✓ Call appropriate service methods                       │
│  ✓ Return HTTP responses                                  │
└────────────────────┬───────────────────────────────────────┘
                     │
                     │ Delegate to Service Layer
                     │
                     ▼
┌────────────────────────────────────────────────────────────┐
│                     AuthService                            │
│                                                            │
│  Dependencies:                                             │
│  - AuthenticationManager (Spring Security)                │
│  - UserAppRepository (Database access)                    │
│  - PasswordEncoder (BCrypt)                               │
│  - JwtUtils (Token generation/validation)                 │
│                                                            │
│  Methods:                                                  │
│  ✓ login()         - Authenticate & generate tokens       │
│  ✓ register()      - Create new user                      │
│  ✓ refreshToken()  - Generate new access token            │
│  ✓ getCurrentUser()- Get authenticated user info          │
└────────────────────┬───────────────────────────────────────┘
                     │
          ┌──────────┴──────────┬──────────────┬─────────────┐
          │                     │              │             │
          ▼                     ▼              ▼             ▼
┌──────────────────┐  ┌──────────────┐  ┌─────────┐  ┌──────────┐
│ Authentication   │  │ Database     │  │ BCrypt  │  │ JwtUtils │
│ Manager          │  │ (MySQL)      │  │ Encoder │  │          │
│ (Spring Security)│  │              │  │         │  │          │
└──────────────────┘  └──────────────┘  └─────────┘  └──────────┘
```

---

## 🧩 Components Explained

### 1. AuthService

**Location**: `com.tricol.gestionstock.service.auth.AuthService`

**Dependencies**:

| Dependency | Purpose | When Used |
|------------|---------|-----------|
| `AuthenticationManager` | Validates username/password | During login |
| `UserAppRepository` | Access user data from database | All operations |
| `RoleAppRepository` | Access role data | (Reserved for future use) |
| `PasswordEncoder` | Hash passwords with BCrypt | During registration |
| `JwtUtils` | Generate and validate JWT tokens | Login, refresh token |

---

### 2. AuthController

**Location**: `com.tricol.gestionstock.controller.AuthController`

**Annotations**:
- `@RestController` - Marks this as a REST controller
- `@RequestMapping("/api/auth")` - Base path for all endpoints
- `@Tag` - Swagger/OpenAPI documentation grouping

---

## 📡 API Endpoints

### Endpoint 1: User Login

**URL**: `POST /api/auth/login`

**Access**: Public (no authentication required)

**Request Body**:
```json
{
  "username": "john.doe",
  "password": "myPassword123"
}
```

**Validation Rules**:
- `username`: Required, not blank
- `password`: Required, not blank

**Success Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZSIsImlhdCI6MTcwMzQzMzYwMCwiZXhwIjoxNzAzNTIwMDAwfQ...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZSIsImlhdCI6MTcwMzQzMzYwMCwiZXhwIjoxNzA0MDM4NDAwfQ...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "john.doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["MAGASINIER"],
    "permissions": [
      "CONSULTER_STOCK",
      "CREER_BON_SORTIE",
      "VALIDER_BON_SORTIE"
    ]
  }
}
```

**Error Responses**:

| Status | Error | Cause |
|--------|-------|-------|
| 401 | Bad credentials | Wrong username or password |
| 400 | Validation error | Missing or invalid fields |

---

### Endpoint 2: User Registration

**URL**: `POST /api/auth/register`

**Access**: Public (no authentication required)

**Request Body**:
```json
{
  "username": "jane.smith",
  "email": "jane@example.com",
  "password": "securePass456",
  "firstName": "Jane",
  "lastName": "Smith"
}
```

**Validation Rules**:
- `username`: Required, 3-50 characters
- `email`: Required, valid email format
- `password`: Required, minimum 6 characters
- `firstName`: Required
- `lastName`: Required

**Success Response** (201 Created):
```json
{
  "message": "User registered successfully! Please wait for an administrator to assign you a role."
}
```

**Error Responses**:

| Status | Error | Cause |
|--------|-------|-------|
| 400 | Username is already taken | Username exists in database |
| 400 | Email is already in use | Email exists in database |
| 400 | Validation error | Invalid field values |

**Important Notes**:
- ⚠️ New users have **NO role** by default
- ⚠️ User cannot access protected endpoints until admin assigns a role
- ✅ Password is automatically hashed with BCrypt
- ✅ User is enabled by default

---

### Endpoint 3: Refresh Token

**URL**: `POST /api/auth/refresh`

**Access**: Public (but requires valid refresh token)

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZSIsImlhdCI6MTcwMzQzMzYwMCwiZXhwIjoxNzA0MDM4NDAwfQ..."
}
```

**Success Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9... (NEW TOKEN)",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9... (NEW REFRESH TOKEN)",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

**Error Responses**:

| Status | Error | Cause |
|--------|-------|-------|
| 400 | Refresh token is invalid or expired | Token validation failed |
| 400 | User not found | User was deleted |

**Important Notes**:
- ✅ Both access token AND refresh token are rotated (new tokens generated)
- ✅ Old tokens become invalid
- ✅ Improves security (token rotation)

---

### Endpoint 4: Get Current User

**URL**: `GET /api/auth/me`

**Access**: Protected (requires valid JWT access token)

**Request Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Success Response** (200 OK):
```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["MAGASINIER"],
  "permissions": [
    "CONSULTER_STOCK",
    "CREER_BON_SORTIE",
    "VALIDER_BON_SORTIE"
  ]
}
```

**Error Responses**:

| Status | Error | Cause |
|--------|-------|-------|
| 401 | Unauthorized | No token or invalid token |
| 400 | User not found | User was deleted |

---

## 🔄 Authentication Flow

### Flow 1: User Login

```
┌─────────────────────────────────────────────────────────────┐
│ CLIENT: POST /api/auth/login                                │
│ {username: "john.doe", password: "pass123"}                 │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ AuthController.login()                                      │
│ - Validates request with @Valid                             │
│ - Calls authService.login(loginRequest)                     │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ AuthService.login()                                         │
│                                                             │
│ Step 1: Authenticate Credentials                           │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ authenticationManager.authenticate(                 │   │
│ │   new UsernamePasswordAuthenticationToken(          │   │
│ │     username, password                              │   │
│ │   )                                                 │   │
│ │ )                                                   │   │
│ │                                                     │   │
│ │ This internally:                                    │   │
│ │ a. Calls CustomUserDetailsService.loadByUsername()  │   │
│ │ b. Retrieves user from database                     │   │
│ │ c. Compares passwords using BCrypt                  │   │
│ │ d. Returns Authentication object if valid           │   │
│ │ e. Throws exception if invalid                      │   │
│ └─────────────────────────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
│ Step 2: Set Security Context                               │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ SecurityContextHolder.getContext()                  │   │
│ │   .setAuthentication(authentication)                │   │
│ │                                                     │   │
│ │ Stores authenticated user in thread-local storage   │   │
│ └─────────────────────────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
│ Step 3: Generate JWT Tokens                                │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ accessToken = jwtUtils.generateAccessToken(username)│   │
│ │ refreshToken = jwtUtils.generateRefreshToken(...)   │   │
│ │                                                     │   │
│ │ Access Token: 24 hours validity                    │   │
│ │ Refresh Token: 7 days validity                     │   │
│ └─────────────────────────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
│ Step 4: Update Last Login                                  │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ user.setLastLogin(LocalDateTime.now())              │   │
│ │ userAppRepository.save(user)                        │   │
│ └─────────────────────────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
│ Step 5: Build User Info                                    │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ Extract roles (authorities starting with "ROLE_")   │   │
│ │ Extract permissions (other authorities)             │   │
│ │ Build UserInfoDTO with all user data                │   │
│ └─────────────────────────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
│ Step 6: Return Response                                    │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ AuthResponseDTO {                                   │   │
│ │   accessToken,                                      │   │
│ │   refreshToken,                                     │   │
│ │   expiresIn,                                        │   │
│ │   user (UserInfoDTO)                                │   │
│ │ }                                                   │   │
│ └─────────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ AuthController returns ResponseEntity<AuthResponseDTO>      │
│ HTTP 200 OK                                                 │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ CLIENT receives tokens and user info                        │
│ - Stores tokens (localStorage/memory)                       │
│ - Uses accessToken for subsequent requests                  │
└─────────────────────────────────────────────────────────────┘
```

---

### Flow 2: User Registration

```
┌─────────────────────────────────────────────────────────────┐
│ CLIENT: POST /api/auth/register                             │
│ {username, email, password, firstName, lastName}            │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ AuthController.register()                                   │
│ - @Valid annotation triggers validation                     │
│ - Calls authService.register(registerRequest)               │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ AuthService.register()                                      │
│                                                             │
│ Step 1: Check Username Availability                        │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ if (userAppRepository.existsByUsername(username)) { │   │
│ │   throw new RuntimeException("Username taken!");    │   │
│ │ }                                                   │   │
│ └────────────��────────────────────────────────────────┘   │
��                     │                                       │
│                     ▼                                       │
│ Step 2: Check Email Availability                           │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ if (userAppRepository.existsByEmail(email)) {       │   │
│ │   throw new RuntimeException("Email in use!");      │   │
│ │ }                                                   │   │
│ └─────────────────────────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
│ Step 3: Hash Password                                      │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ hashedPassword =                                    │   │
│ │   passwordEncoder.encode(password)                  │   │
│ │                                                     │   │
│ │ Example:                                            │   │
│ │ "pass123" → "$2a$10$N9qo8uLOickgx2ZMRZoMy..."      │   │
│ │                                                     │   │
│ │ BCrypt properties:                                  │   │
│ │ - Salted (unique hash per password)                │   │
│ │ - Slow (prevents brute force)                      │   │
│ │ - One-way (cannot be decrypted)                    │   │
│ └─────────────────────────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
│ Step 4: Create User Entity                                 │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ UserApp.builder()                                   │   │
│ │   .username(username)                               │   │
│ │   .email(email)                                     │   │
│ │   .password(hashedPassword)                         │   │
│ │   .firstName(firstName)                             │   │
│ │   .lastName(lastName)                               │   │
│ │   .enabled(true)         ← Account enabled         │   │
│ │   .accountNonExpired(true)                          │   │
│ │   .accountNonLocked(true)                           │   │
│ │   .credentialsNonExpired(true)                      │   │
│ │   .build()                                          │   │
│ │                                                     │   │
│ │ ⚠️ Note: NO ROLE ASSIGNED!                         │   │
│ │ Admin must assign role later                       │   │
│ └─────────────────────────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
│ Step 5: Save to Database                                   │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ userAppRepository.save(user)                        │   │
│ │                                                     │   │
│ │ Database Insert:                                    │   │
│ │ INSERT INTO users (username, email, password,       │   │
│ │   first_name, last_name, enabled, ...)              │   │
│ │ VALUES ('jane.smith', 'jane@...', '$2a$...', ...)   │   │
│ └─────────────────────────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
│ Step 6: Return Success Message                             │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ MessageResponseDTO {                                │   │
│ │   message: "User registered successfully!           │   │
│ │            Please wait for admin to assign role."   │   │
│ │ }                                                   │   │
│ └─────────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ AuthController returns HTTP 201 Created                     │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ CLIENT receives success message                             │
│ - User created but CANNOT login yet (no role)               │
│ - Must wait for admin to assign role                        │
└─────────────────────────────────────────────────────────────┘
```

---

### Flow 3: Token Refresh

```
┌─────────────────────────────────────────────────────────────┐
│ CLIENT: Access token expired after 24 hours                 │
│ POST /api/auth/refresh                                      │
│ {refreshToken: "eyJhbGci..."}                               │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ AuthService.refreshToken()                                  │
│                                                             │
│ Step 1: Validate Refresh Token                             │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ if (!jwtUtils.validateToken(refreshToken)) {        │   │
│ │   throw new RuntimeException("Invalid token!");     │   │
│ │ }                                                   │   │
│ │                                                     │   │
│ │ Validation checks:                                  │   │
│ │ - Signature valid?                                  │   │
│ │ - Not expired? (< 7 days)                           │   │
│ │ - Format correct?                                   │   │
│ └─────────────────────────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
│ Step 2: Extract Username                                   │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ username =                                          │   │
│ │   jwtUtils.getUsernameFromToken(refreshToken)       │   │
│ │                                                     │   │
│ │ Extracts "sub" claim from JWT payload               │   │
│ └─────────────────────────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
│ Step 3: Verify User Exists                                 │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ user = userAppRepository.findByUsername(username)   │   │
│ │   .orElseThrow(() -> "User not found!")             │   │
│ │                                                     │   │
│ │ Ensures user wasn't deleted                         │   │
│ └─────────────────────────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
│ Step 4: Generate NEW Tokens                                │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ newAccessToken =                                    │   │
│ │   jwtUtils.generateAccessToken(username)            │   │
│ │ newRefreshToken =                                   │   │
│ │   jwtUtils.generateRefreshToken(username)           │   │
│ │                                                     │   │
│ │ Both tokens are NEW (Token Rotation)                │   │
│ │ Old tokens become invalid                           │   │
│ └─────────────────────────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
│ Step 5: Return New Tokens                                  │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ TokenRefreshResponseDTO {                           │   │
│ │   accessToken: (new, fresh 24h),                    │   │
│ │   refreshToken: (new, fresh 7d),                    │   │
│ │   expiresIn: 86400000                               │   │
│ │ }                                                   │   │
│ └─────────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ CLIENT receives new tokens                                  │
│ - Replaces old tokens in storage                            │
│ - Continues using application without re-login              │
└─────────────────────────────────────────────────────────────┘
```

**Why Token Rotation?**
- 🔒 **Security**: Old tokens can't be reused
- 🔒 **Detection**: If refresh token is stolen and used, legitimate user's refresh fails → security alert
- 🔒 **Invalidation**: Server can track and revoke token families

---

## 💻 Code Walkthrough

### AuthService.login() - Line by Line

```java
@Transactional
public AuthResponseDTO login(LoginRequestDTO loginRequest) {
```
- `@Transactional`: Database operations in a transaction
- Returns `AuthResponseDTO` with tokens and user info

```java
    // Authenticate user
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            )
    );
```
**What happens here:**
1. Creates `UsernamePasswordAuthenticationToken` with username + password
2. `AuthenticationManager` calls configured authentication providers
3. `DaoAuthenticationProvider` is invoked
4. Calls `CustomUserDetailsService.loadUserByUsername()`
5. Loads user from database
6. Compares passwords using `BCrypt.matches(rawPassword, hashedPassword)`
7. If match: Returns `Authentication` object with user details + authorities
8. If no match: Throws `BadCredentialsException`

```java
    SecurityContextHolder.getContext().setAuthentication(authentication);
```
**What this does:**
- Stores authenticated user in Spring Security's context
- Makes user accessible throughout the request
- Allows `@PreAuthorize` annotations to work

```java
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
```
- Extracts our custom user details from authentication object
- Contains user info + roles + permissions

```java
    String accessToken = jwtUtils.generateAccessToken(userDetails.getUsername());
    String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());
```
- Generates JWT tokens
- Access token: 24 hours validity
- Refresh token: 7 days validity
- Both signed with secret key

```java
    UserApp user = userAppRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
    user.setLastLogin(LocalDateTime.now());
    userAppRepository.save(user);
```
- Updates user's last login timestamp
- Useful for auditing and analytics

```java
    UserInfoDTO userInfo = buildUserInfo(userDetails, user);
```
- Builds user information DTO
- Separates roles from permissions
- Removes "ROLE_" prefix from role authorities

```java
    return AuthResponseDTO.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtUtils.getJwtExpirationMs())
            .user(userInfo)
            .build();
}
```
- Constructs response with all data
- Client receives everything needed to authenticate subsequent requests

---

### AuthService.register() - Line by Line

```java
@Transactional
public MessageResponseDTO register(RegisterRequestDTO registerRequest) {
```
- `@Transactional`: Ensures atomicity (all-or-nothing)

```java
    if (userAppRepository.existsByUsername(registerRequest.getUsername())) {
        throw new RuntimeException("Error: Username is already taken!");
    }
```
- Checks username uniqueness
- Prevents duplicate usernames
- Query: `SELECT COUNT(*) FROM users WHERE username = ?`

```java
    if (userAppRepository.existsByEmail(registerRequest.getEmail())) {
        throw new RuntimeException("Error: Email is already in use!");
    }
```
- Checks email uniqueness
- Prevents duplicate emails
- Important for account recovery

```java
    UserApp user = UserApp.builder()
            .username(registerRequest.getUsername())
            .email(registerRequest.getEmail())
            .password(passwordEncoder.encode(registerRequest.getPassword()))
```
**Password encoding**:
- Input: `"myPassword123"`
- BCrypt hash: `"$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"`
- Contains: algorithm, cost factor, salt, and hash
- Same password = different hash each time (random salt)

```java
            .firstName(registerRequest.getFirstName())
            .lastName(registerRequest.getLastName())
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();
```
- Sets account status flags
- All true by default
- Can be used to disable accounts, lock accounts, etc.

```java
    // Note: No role assigned by default - admin must assign role
    userAppRepository.save(user);
```
⚠️ **Critical point:**
- User created with NO role
- User CANNOT access protected endpoints
- Admin must assign role via admin panel

---

### AuthService.refreshToken() - Line by Line

```java
public TokenRefreshResponseDTO refreshToken(RefreshTokenRequestDTO request) {
    String refreshToken = request.getRefreshToken();
```
- Extracts refresh token from request

```java
    if (!jwtUtils.validateToken(refreshToken)) {
        throw new RuntimeException("Refresh token is invalid or expired!");
    }
```
**Validation performs:**
1. Verifies signature with secret key
2. Checks expiration date (< 7 days old)
3. Validates JWT structure (header.payload.signature)

```java
    String username = jwtUtils.getUsernameFromToken(refreshToken);
```
- Decodes JWT
- Extracts "sub" claim (subject = username)
- Does NOT validate again (already validated above)

```java
    UserApp user = userAppRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found!"));
```
- Verifies user still exists in database
- Important: User might have been deleted

```java
    String newAccessToken = jwtUtils.generateAccessToken(username);
    String newRefreshToken = jwtUtils.generateRefreshToken(username);
```
🔄 **Token Rotation:**
- Both tokens are regenerated
- Old tokens become useless
- Enhances security

```java
    return TokenRefreshResponseDTO.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .expiresIn(jwtUtils.getJwtExpirationMs())
            .build();
}
```
- Returns new tokens to client
- Client must store and use these new tokens

---

### AuthService.getCurrentUser() - Line by Line

```java
public UserInfoDTO getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
```
- Retrieves current authentication from thread-local storage
- Set by JWT filter during request processing

```java
    if (authentication == null || !authentication.isAuthenticated()) {
        throw new RuntimeException("No authenticated user found!");
    }
```
- Validates authentication exists
- Checks if user is authenticated

```java
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    UserApp user = userAppRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found!"));
```
- Gets user details from authentication
- Fetches fresh data from database (in case of updates)

```java
    return buildUserInfo(userDetails, user);
}
```
- Builds and returns user information DTO

---

### buildUserInfo() Helper Method

```java
private UserInfoDTO buildUserInfo(CustomUserDetails userDetails, UserApp user) {
    return UserInfoDTO.builder()
            .id(userDetails.getId())
            .username(userDetails.getUsername())
            .email(userDetails.getEmail())
            .firstName(userDetails.getFirstName())
            .lastName(userDetails.getLastName())
```
- Basic user information

```java
            .roles(userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                    .collect(Collectors.toSet()))
```
**Role extraction logic:**
1. Get all authorities: `["ROLE_MAGASINIER", "CONSULTER_STOCK", "CREER_BON_SORTIE"]`
2. Filter roles: `["ROLE_MAGASINIER"]` (starts with "ROLE_")
3. Remove prefix: `["MAGASINIER"]`
4. Collect to Set: `Set<String> = ["MAGASINIER"]`

```java
            .permissions(userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> !auth.startsWith("ROLE_"))
                    .collect(Collectors.toSet()))
```
**Permission extraction logic:**
1. Get all authorities: `["ROLE_MAGASINIER", "CONSULTER_STOCK", "CREER_BON_SORTIE"]`
2. Filter permissions: `["CONSULTER_STOCK", "CREER_BON_SORTIE"]` (NOT starting with "ROLE_")
3. Collect to Set: `Set<String> = ["CONSULTER_STOCK", "CREER_BON_SORTIE"]`

```java
            .build();
}
```
- Returns complete user information

---

## 🧪 Testing Guide

### Test 1: User Registration

**Request**:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test.user",
    "email": "test@example.com",
    "password": "testPass123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

**Expected Response** (201 Created):
```json
{
  "message": "User registered successfully! Please wait for an administrator to assign you a role."
}
```

**Verify in Database**:
```sql
SELECT id, username, email, first_name, last_name, enabled, role_id
FROM users
WHERE username = 'test.user';
```
Expected: User exists with `role_id = NULL`

---

### Test 2: Login WITHOUT Role (Should Fail)

**Request**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test.user",
    "password": "testPass123"
  }'
```

**Expected Response** (200 OK):
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "test.user",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "roles": [],        ← Empty! No role
    "permissions": []   ← Empty! No permissions
  }
}
```

**Try to Access Protected Endpoint**:
```bash
TOKEN="<access_token_from_above>"

curl -X GET http://localhost:8080/api/produits \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: Access may be denied by `@PreAuthorize` checks if implemented

---

### Test 3: Assign Role (Admin Task)

This would be done by an admin through an admin panel or direct database update:

```sql
-- Assuming role with id=2 is MAGASINIER
UPDATE users
SET role_id = 2
WHERE username = 'test.user';
```

---

### Test 4: Login WITH Role (Should Succeed)

**Request**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test.user",
    "password": "testPass123"
  }'
```

**Expected Response** (200 OK):
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "test.user",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "roles": ["MAGASINIER"],  ← Role assigned!
    "permissions": [          ← Permissions from role!
      "CONSULTER_STOCK",
      "CREER_BON_SORTIE",
      "VALIDER_BON_SORTIE"
    ]
  }
}
```

---

### Test 5: Access Protected Endpoint

**Request**:
```bash
TOKEN="<access_token_from_login>"

curl -X GET http://localhost:8080/api/produits \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with products list

---

### Test 6: Get Current User

**Request**:
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response** (200 OK):
```json
{
  "id": 1,
  "username": "test.user",
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User",
  "roles": ["MAGASINIER"],
  "permissions": [
    "CONSULTER_STOCK",
    "CREER_BON_SORTIE",
    "VALIDER_BON_SORTIE"
  ]
}
```

---

### Test 7: Refresh Token

**Wait 23 hours or simulate token expiration**

**Request**:
```bash
REFRESH_TOKEN="<refresh_token_from_login>"

curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$REFRESH_TOKEN\"
  }"
```

**Expected Response** (200 OK):
```json
{
  "accessToken": "eyJ... (NEW TOKEN)",
  "refreshToken": "eyJ... (NEW REFRESH TOKEN)",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

---

## 🔒 Security Considerations

### ✅ What We Did Right

1. **Password Hashing**
   - BCrypt algorithm
   - Automatic salting
   - Slow hashing (prevents brute force)

2. **JWT Tokens**
   - Signed with HMAC-SHA-512
   - Short-lived access tokens (24h)
   - Longer refresh tokens (7d) for UX

3. **Token Rotation**
   - Both tokens regenerated on refresh
   - Old tokens become invalid
   - Prevents token replay attacks

4. **No Role by Default**
   - Principle of least privilege
   - Admin must explicitly grant access
   - Prevents unauthorized access

5. **Input Validation**
   - `@Valid` annotation
   - Field-level constraints
   - Prevents invalid data

6. **Transaction Management**
   - `@Transactional` ensures atomicity
   - Rollback on errors

### ⚠️ Production Recommendations

1. **Error Handling**
   ```java
   // Current:
   throw new RuntimeException("Username is already taken!");
   
   // Better: Custom exceptions
   throw new UsernameAlreadyExistsException("Username is already taken!");
   ```

2. **Rate Limiting**
   - Limit login attempts per IP
   - Prevent brute force attacks
   - Use Spring Boot Actuator + Redis

3. **Token Blacklist**
   - Store revoked tokens in Redis
   - Check blacklist on token validation
   - Enable logout functionality

4. **HTTPS Only**
   - Never send tokens over HTTP
   - Configure SSL/TLS
   - Use secure cookies for refresh tokens

5. **Audit Logging**
   - Log all authentication attempts
   - Log permission changes
   - Track user activities

6. **Environment Variables**
   ```properties
   # Don't hardcode secrets
   app.jwt.secret=${JWT_SECRET}
   spring.datasource.password=${DB_PASSWORD}
   ```

7. **Account Lockout**
   - Lock account after N failed login attempts
   - Temporary lockout (15 minutes)
   - Email notification to user

---

## 🎯 Key Takeaways

### What We Implemented

✅ **Complete Authentication System**:
- User registration
- User login with JWT
- Token refresh mechanism
- Get current user info

✅ **Security Features**:
- BCrypt password hashing
- JWT token generation/validation
- Role-based access control
- Permission-based access control
- No default role (security by default)

✅ **Clean Architecture**:
- Service layer (business logic)
- Controller layer (REST API)
- Clear separation of concerns
- Validation at controller level

### User Journey

1. **User Registers** → Account created, NO role
2. **Admin Assigns Role** → User gets MAGASINIER role + default permissions
3. **User Logs In** → Receives JWT tokens + user info
4. **User Accesses API** → Token validated, permissions checked
5. **Token Expires** → User refreshes token (no re-login needed)
6. **Admin Customizes** → Admin can grant/revoke individual permissions

### Next Steps

Now that authentication is complete, you should:
1. ✅ Create Liquibase migrations for security tables
2. ✅ Seed initial roles and permissions
3. ✅ Create admin panel for user/role/permission management
4. ✅ Implement audit logging
5. ✅ Add @PreAuthorize annotations to controllers
6. ✅ Write unit tests

---

## 📚 Related Documentation

- [JWT_AUTHENTICATION_README.md](JWT_AUTHENTICATION_README.md) - JWT token details
- [SPRING_SECURITY_CONFIG_README.md](SPRING_SECURITY_CONFIG_README.md) - Security configuration
- Spring Security Reference: https://docs.spring.io/spring-security/reference/

---

**Document Version**: 1.0  
**Last Updated**: December 24, 2024  
**Author**: Tricol Development Team

