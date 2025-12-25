# Spring Security Configuration - Documentation

## 📋 Table of Contents
1. [Overview](#overview)
2. [Configuration Breakdown](#configuration-breakdown)
3. [Security Filter Chain](#security-filter-chain)
4. [Authentication Flow](#authentication-flow)
5. [Components](#components)
6. [Request Flow Diagrams](#request-flow-diagrams)
7. [Security Rules](#security-rules)
8. [Testing](#testing)

---

## 🎯 Overview

The **SecurityConfig.java** class is the heart of Spring Security configuration for the Tricol application. It defines:
- How users are authenticated
- Which endpoints are public vs protected
- How JWT tokens are validated
- Password encryption strategy
- Session management (stateless)

### Key Annotations

```java
@Configuration           // Marks this as a Spring configuration class
@EnableWebSecurity      // Enables Spring Security's web security support
@EnableMethodSecurity   // Enables method-level security (@PreAuthorize, @Secured)
```

---

## 🔧 Configuration Breakdown

### 1. Password Encoder Bean

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Purpose**: Encrypts passwords before storing in database and validates passwords during login.

**Why BCrypt?**
- **Adaptive**: Slow by design (prevents brute-force attacks)
- **Salted**: Each password gets a unique salt
- **Industry Standard**: Widely trusted and battle-tested
- **One-Way Hash**: Cannot be reversed

**Example**:
```java
// Registration
String rawPassword = "myPassword123";
String hashedPassword = passwordEncoder.encode(rawPassword);
// Result: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

// Login
boolean matches = passwordEncoder.matches(rawPassword, hashedPassword);
// Result: true
```

---

### 2. Authentication Provider Bean

```java
@Bean
public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
}
```

**Purpose**: Connects Spring Security to our custom UserDetailsService and password encoder.

**DaoAuthenticationProvider**:
- **DAO** = Data Access Object
- Retrieves user details from database
- Validates credentials
- Returns authenticated user

**Flow**:
```
1. User submits credentials (username, password)
2. DaoAuthenticationProvider calls UserDetailsService.loadUserByUsername(username)
3. UserDetailsService returns UserDetails (user + roles + permissions)
4. DaoAuthenticationProvider compares passwords using PasswordEncoder
5. If match: Authentication successful
6. If no match: Authentication failed
```

---

### 3. Authentication Manager Bean

```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) 
    throws Exception {
    return authConfig.getAuthenticationManager();
}
```

**Purpose**: Central interface for authentication. Used in login endpoint to authenticate users.

**Usage in AuthController**:
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    // AuthenticationManager validates credentials
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getUsername(),
            loginRequest.getPassword()
        )
    );
    
    // If successful, generate JWT tokens
    String jwt = jwtUtils.generateAccessToken(authentication);
    return ResponseEntity.ok(new AuthResponse(jwt, ...));
}
```

---

### 4. Security Filter Chain Bean

This is the **most important** configuration. It defines the entire security behavior.

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()
            .anyRequest().authenticated()
        );

    http.authenticationProvider(authenticationProvider());
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

Let's break this down piece by piece:

---

#### 4.1 CSRF Protection Disabled

```java
.csrf(AbstractHttpConfigurer::disable)
```

**What is CSRF?**
- **Cross-Site Request Forgery**: Attack where malicious site tricks user's browser into making unauthorized requests
- Example: Attacker tricks you into clicking a link that transfers money from your bank account

**Why Disable?**
- **Stateless JWT**: We're using JWT tokens, not cookies
- **No Sessions**: CSRF attacks target session cookies
- **Token in Header**: JWT is sent in Authorization header, not automatically by browser
- **Safe for REST APIs**: REST APIs with JWT don't need CSRF protection

**When to Enable CSRF?**
- Cookie-based authentication
- Server-side sessions
- Form-based login with session cookies

---

#### 4.2 Exception Handling

```java
.exceptionHandling(exception -> exception
    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
)
```

**Purpose**: Defines what happens when authentication fails.

**Without This**:
```
HTTP 403 Forbidden
Default Spring Security error page (HTML)
```

**With This**:
```json
HTTP 401 Unauthorized
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required",
  "path": "/api/produits"
}
```

**JwtAuthenticationEntryPoint** ensures REST API returns JSON errors, not HTML pages.

---

#### 4.3 Session Management

```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

**Purpose**: Tells Spring Security NOT to create or use HTTP sessions.

**Session Policies**:

| Policy | Description | Use Case |
|--------|-------------|----------|
| **STATELESS** | No sessions created or used | JWT/Token authentication |
| **ALWAYS** | Always create session | Traditional web apps |
| **IF_REQUIRED** | Create session if needed | Default Spring behavior |
| **NEVER** | Never create, but use if exists | Hybrid approach |

**Why STATELESS?**
- **Scalability**: No session storage across servers
- **Performance**: No session lookup on each request
- **JWT Design**: Tokens are self-contained
- **Microservices**: Easy to distribute across services

**Traditional vs JWT**:
```
TRADITIONAL (STATEFUL):
Client --> Login --> Server creates session --> Session ID in cookie
Client --> Request + Cookie --> Server looks up session --> Response

JWT (STATELESS):
Client --> Login --> Server generates JWT --> JWT returned to client
Client --> Request + JWT --> Server validates JWT --> Response
(No server-side storage!)
```

---

#### 4.4 Authorization Rules

```java
.authorizeHttpRequests(auth -> auth
    // Public endpoints - no authentication required
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
    .requestMatchers("/v3/api-docs/**").permitAll()
    
    // All other endpoints require authentication
    .anyRequest().authenticated()
)
```

**Request Matchers Explained**:

| Pattern | Matches | Example URLs |
|---------|---------|--------------|
| `/api/auth/**` | All auth endpoints | `/api/auth/login`, `/api/auth/register`, `/api/auth/refresh` |
| `/swagger-ui/**` | Swagger UI resources | `/swagger-ui/index.html`, `/swagger-ui/swagger-ui.css` |
| `/api-docs/**` | API documentation | `/api-docs`, `/api-docs/swagger-config` |
| `/v3/api-docs/**` | OpenAPI v3 docs | `/v3/api-docs`, `/v3/api-docs/swagger-config` |
| `anyRequest()` | Everything else | `/api/produits`, `/api/fournisseurs`, etc. |

**Access Levels**:
- **permitAll()**: Anyone can access (public)
- **authenticated()**: Must be logged in
- **hasRole("ADMIN")**: Must have ADMIN role
- **hasAuthority("PERMISSION")**: Must have specific permission

**Order Matters!**
Rules are evaluated **top to bottom**. First match wins.

```java
// ✅ CORRECT
.requestMatchers("/api/auth/**").permitAll()      // Specific
.anyRequest().authenticated()                      // General

// ❌ WRONG
.anyRequest().authenticated()                      // General (catches everything!)
.requestMatchers("/api/auth/**").permitAll()      // Never reached
```

---

#### 4.5 Add JWT Filter

```java
http.authenticationProvider(authenticationProvider());
http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

**Purpose**: Inserts JWT validation filter into Spring Security's filter chain.

**Filter Order**:
```
1. Spring Security's internal filters
2. JWT Authentication Filter (OUR CUSTOM FILTER) ← Added here
3. UsernamePasswordAuthenticationFilter (Spring's default)
4. Other filters...
5. Controller
```

**Why Before UsernamePasswordAuthenticationFilter?**
- We want JWT validation to happen **before** traditional form-based authentication
- Our filter sets authentication in SecurityContext
- Subsequent filters see the authenticated user

---

## 🔄 Security Filter Chain

### Complete Filter Chain Visualization

```
HTTP Request
    │
    ▼
┌─────────────────────────────────────────────────────────────┐
│  1. SecurityContextPersistenceFilter                        │
│     - Loads SecurityContext from session (not used in JWT)  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  2. LogoutFilter                                            │
│     - Handles logout requests                               │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  3. JwtAuthenticationFilter ← OUR CUSTOM FILTER             │
│     ┌──────────────────────────────────────────────────┐   │
│     │ a. Extract JWT from Authorization header         │   │
│     │ b. Validate JWT (signature, expiration)          │   │
│     │ c. Extract username from token                   │   │
│     │ d. Load UserDetails from database                │   │
│     │ e. Create Authentication object                  │   │
│     │ f. Set in SecurityContext                        │   │
│     └──────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  4. UsernamePasswordAuthenticationFilter                    │
│     - Handles form-based login (not used in our API)        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  5. ExceptionTranslationFilter                              │
│     - Catches security exceptions                           │
│     - Calls JwtAuthenticationEntryPoint on auth failure     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  6. FilterSecurityInterceptor                               │
│     - Makes final access control decision                   │
│     - Checks if authenticated                               │
│     - Checks roles and permissions                          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼ (If authorized)
                    Controller
```

---

## 🔐 Authentication Flow

### Scenario 1: Public Endpoint (No Authentication)

```
Request: GET /api/auth/login
Headers: (none)

┌──────────────────────────────────────────────────────────┐
│ 1. Request reaches JwtAuthenticationFilter               │
│    - No Authorization header                             │
│    - Filter does nothing, continues chain                │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────────────────┐
│ 2. FilterSecurityInterceptor checks authorization        │
│    - requestMatchers("/api/auth/**").permitAll()         │
│    - Match found! Access allowed                         │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▼
                  AuthController
                  (Handles login)
```

---

### Scenario 2: Protected Endpoint (Valid JWT)

```
Request: GET /api/produits
Headers: Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

┌──────────────────────────────────────────────────────────┐
│ 1. JwtAuthenticationFilter                               │
│    ┌────────────────────────────────────────────┐        │
│    │ jwt = "eyJhbGciOiJIUzUxMiJ9..."            │        │
│    │ if (jwtUtils.validateToken(jwt)) {         │        │
│    │   username = jwtUtils.getUsernameFromToken │        │
│    │   userDetails = loadUserByUsername(...)    │        │
│    │   // Set authentication                    │        │
│    │   SecurityContext.setAuthentication(...)   │        │
│    │ }                                           │        │
│    └────────────────────────────────────────────┘        │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────────────────┐
│ 2. SecurityContext now contains:                         │
│    - Authentication: UsernamePasswordAuthenticationToken │
│    - Principal: UserDetails (username, roles, perms)     │
│    - Authenticated: true                                 │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────────────────┐
│ 3. FilterSecurityInterceptor                             │
│    - Checks: anyRequest().authenticated()                │
│    - SecurityContext has authenticated user              │
│    - Access GRANTED                                      │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▼
                  ProduitController
                  (Returns products)
```

---

### Scenario 3: Protected Endpoint (No JWT)

```
Request: GET /api/produits
Headers: (none)

┌──────────────────────────────────────────────────────────┐
│ 1. JwtAuthenticationFilter                               │
│    - No Authorization header                             │
│    - SecurityContext remains empty (not authenticated)   │
│    - Filter continues chain                              │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────────────────┐
│ 2. FilterSecurityInterceptor                             │
│    - Checks: anyRequest().authenticated()                │
│    - SecurityContext is EMPTY (not authenticated)        │
│    - Access DENIED                                       │
│    - Throws AuthenticationException                      │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────────────────┐
│ 3. ExceptionTranslationFilter catches exception          │
│    - Calls JwtAuthenticationEntryPoint.commence()        │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▼
Response: 401 Unauthorized
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required",
  "path": "/api/produits"
}
```

---

### Scenario 4: Protected Endpoint (Invalid/Expired JWT)

```
Request: GET /api/produits
Headers: Authorization: Bearer expired_or_invalid_token

┌──────────────────────────────────────────────────────────┐
│ 1. JwtAuthenticationFilter                               │
│    ┌────────────────────────────────────────────┐        │
│    │ jwt = "expired_or_invalid_token"           │        │
│    │ if (jwtUtils.validateToken(jwt)) {         │        │
│    │   // Validation FAILS                      │        │
│    │   // Log error: "JWT token is expired"     │        │
│    │   // Returns false                         │        │
│    │ }                                           │        │
│    │ // SecurityContext remains empty           │        │
│    │ // Continue filter chain                   │        │
│    └────────────────────────────────────────────┘        │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────────────────┐
│ 2. FilterSecurityInterceptor                             │
│    - SecurityContext is EMPTY                            │
│    - Access DENIED                                       │
│    - Throws AuthenticationException                      │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────────────────┐
│ 3. ExceptionTranslationFilter                            │
│    - JwtAuthenticationEntryPoint returns 401             │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▼
Response: 401 Unauthorized
```

---

## 🧩 Components

### 1. UserDetailsService (Interface)

**Purpose**: Load user-specific data. Spring Security uses this to authenticate users.

**Your Implementation**:
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserAppRepository userAppRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) 
        throws UsernameNotFoundException {
        
        UserApp user = userAppRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return CustomUserDetails.build(user);
    }
}
```

**When Called**:
1. During login (by AuthenticationManager)
2. During JWT filter processing (to load user details)

---

### 2. UserDetails (Interface)

**Purpose**: Provides core user information to Spring Security.

**Required Methods**:
```java
public interface UserDetails {
    String getUsername();
    String getPassword();
    Collection<? extends GrantedAuthority> getAuthorities(); // Roles + Permissions
    boolean isAccountNonExpired();
    boolean isAccountNonLocked();
    boolean isCredentialsNonExpired();
    boolean isEnabled();
}
```

**Your Implementation (CustomUserDetails)**:
```java
public class CustomUserDetails implements UserDetails {
    private Long id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    
    // Implement all methods...
    
    public static CustomUserDetails build(UserApp user) {
        // Convert user roles and permissions to GrantedAuthority
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add roles
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
        }
        
        // Add permissions
        user.getPermissions().forEach(permission -> {
            authorities.add(new SimpleGrantedAuthority(permission.getName()));
        });
        
        return new CustomUserDetails(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            authorities
        );
    }
}
```

---

### 3. GrantedAuthority (Interface)

**Purpose**: Represents an authority (role or permission) granted to the user.

**Structure**:
```
User "john.doe"
├── ROLE_MAGASINIER (GrantedAuthority)
├── CONSULTER_STOCK (GrantedAuthority)
├── CREER_BON_SORTIE (GrantedAuthority)
└── VALIDER_BON_SORTIE (GrantedAuthority)
```

**Usage in Security**:
```java
// Check role
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) { ... }

// Check permission
@PreAuthorize("hasAuthority('CREER_BON_SORTIE')")
public BonSortie createBonSortie(...) { ... }

// Check multiple
@PreAuthorize("hasRole('ADMIN') or hasAuthority('CONSULTER_STOCK')")
public List<Stock> getStock() { ... }
```

---

## 📊 Request Flow Diagrams

### Diagram 1: Complete Request Flow with JWT

```
┌────────────┐
│   Client   │
│  (Browser) │
└──────┬─────┘
       │
       │ GET /api/produits
       │ Authorization: Bearer eyJhbGci...
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│               Spring DispatcherServlet                   │
└──────┬───────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│           Spring Security Filter Chain                   │
│  ┌────────────────────────────────────────────────────┐  │
│  │ 1. SecurityContextPersistenceFilter                │  │
│  │    - Load SecurityContext (empty for stateless)    │  │
│  └────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────┐  │
│  │ 2. JwtAuthenticationFilter ← CUSTOM                │  │
│  │    ┌──────────────────────────────────────────┐    │  │
│  │    │ parseJwt(request)                        │    │  │
│  │    │   ↓                                      │    │  │
│  │    │ Extract: "eyJhbGci..."                   │    │  │
│  │    │   ↓                                      │    │  │
│  │    │ jwtUtils.validateToken(jwt)              │    │  │
│  │    │   ↓                                      │    │  │
│  │    │ Valid? Yes                               │    │  │
│  │    │   ↓                                      │    │  │
│  │    │ jwtUtils.getUsernameFromToken(jwt)       │    │  │
│  │    │   ↓                                      │    │  │
│  │    │ Username: "john.doe"                     │    │  │
│  │    │   ↓                                      │    │  │
│  │    │ userDetailsService.loadUser("john.doe")  │    │  │
│  │    │   ↓                                      │    │  │
│  │    │ UserDetails (john + authorities)         │    │  │
│  │    │   ↓                                      │    │  │
│  │    │ Create Authentication object             │    │  │
│  │    │   ↓                                      │    │  │
│  │    │ SecurityContext.setAuthentication(...)   │    │  │
│  │    └──────────────────────────────────────────┘    │  │
│  └────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────┐  │
│  │ 3. FilterSecurityInterceptor                       │  │
│  │    - Check: anyRequest().authenticated()           │  │
│  │    - SecurityContext has user? YES                 │  │
│  │    - Access GRANTED                                │  │
│  └────────────────────────────────────────────────────┘  │
└──────┬───────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│              @PreAuthorize Check (Optional)              │
│  @PreAuthorize("hasAuthority('CONSULTER_PRODUITS')")    │
│  - Checks user's authorities                             │
│  - If match: Continue                                    │
│  - If no match: AccessDeniedException (403)              │
└──────┬───────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│                   ProduitController                      │
│  @GetMapping                                             │
│  public List<Produit> getAllProduits() {                 │
│      return produitService.findAll();                    │
│  }                                                       │
└──────┬───────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│                  Response to Client                      │
│  HTTP 200 OK                                             │
│  [                                                       │
│    {id: 1, nom: "Produit A", ...},                      │
│    {id: 2, nom: "Produit B", ...}                       │
│  ]                                                       │
└──────────────────────────────────────────────────────────┘
```

---

### Diagram 2: Login Flow

```
┌────────────┐
│   Client   │
└──────┬─────┘
       │
       │ POST /api/auth/login
       │ {username: "john.doe", password: "pass123"}
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│               Security Filter Chain                      │
│  - requestMatchers("/api/auth/**").permitAll()           │
│  - Request ALLOWED without authentication                │
└──────┬───────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│                  AuthController                          │
│  @PostMapping("/api/auth/login")                         │
│  public ResponseEntity<?> login(@RequestBody ...)        │
└──────┬───────────────────────────────────────────────────┘
       │
       │ 1. Create Authentication Request
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│         UsernamePasswordAuthenticationToken              │
│  - Username: "john.doe"                                  │
│  - Password: "pass123"                                   │
│  - Authenticated: false                                  │
└──────┬───────────────────────────────────────────────────┘
       │
       │ 2. Authenticate
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│              AuthenticationManager                       │
│  authenticationManager.authenticate(authRequest)         │
└──────┬───────────────────────────────────────────────────┘
       │
       │ 3. Delegate to Provider
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│           DaoAuthenticationProvider                      │
│  ┌────────────────────────────────────────────────────┐  │
│  │ a. Call UserDetailsService                         │  │
│  │    userDetails = loadUserByUsername("john.doe")    │  │
│  │      ↓                                             │  │
│  │    Database query...                               │  │
│  │      ↓                                             │  │
│  │    User found with hashed password                 │  │
│  │                                                    │  │
│  │ b. Verify Password                                 │  │
│  │    passwordEncoder.matches(                        │  │
│  │      "pass123",                                    │  │
│  │      "$2a$10$N9qo8uLOi..."                         │  │
│  │    )                                               │  │
│  │      ↓                                             │  │
│  │    Match? YES                                      │  │
│  │                                                    │  │
│  │ c. Create Authenticated Token                      │  │
│  │    - Username: "john.doe"                          │  │
│  │    - Authorities: [ROLE_MAGASINIER, ...]           │  │
│  │    - Authenticated: true                           │  │
│  └────────────────────────────────────────────────────┘  │
└──────┬───────────────────────────────────────────────────┘
       │
       │ 4. Return authenticated token
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│                  AuthController                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │ Authentication authentication = ...                │  │
│  │                                                    │  │
│  │ // Generate JWT tokens                            │  │
│  │ String accessToken =                               │  │
│  │   jwtUtils.generateAccessToken(authentication);    │  │
│  │                                                    │  │
│  │ String refreshToken =                              │  │
│  │   jwtUtils.generateRefreshToken(username);         │  │
│  │                                                    │  │
│  │ // Build response                                  │  │
│  │ return AuthResponse.builder()                      │  │
│  │   .accessToken(accessToken)                        │  │
│  │   .refreshToken(refreshToken)                      │  │
│  │   .user(userInfo)                                  │  │
│  │   .build();                                        │  │
│  └────────────────────────────────────────────────────┘  │
└──────┬───────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│                  Response to Client                      │
│  HTTP 200 OK                                             │
│  {                                                       │
│    "accessToken": "eyJhbGciOiJI...",                     │
│    "refreshToken": "eyJhbGciOiJI...",                    │
│    "tokenType": "Bearer",                                │
│    "expiresIn": 86400000,                                │
│    "user": {                                             │
│      "id": 1,                                            │
│      "username": "john.doe",                             │
│      "roles": ["MAGASINIER"],                            │
│      "permissions": [...]                                │
│    }                                                     │
│  }                                                       │
└──────────────────────────────────────────────────────────┘
```

---

## 🛡️ Security Rules

### Public Endpoints (No Authentication Required)

| Endpoint Pattern | Purpose | Example |
|------------------|---------|---------|
| `/api/auth/**` | Authentication endpoints | `/api/auth/login`, `/api/auth/register`, `/api/auth/refresh` |
| `/swagger-ui/**` | Swagger UI interface | `/swagger-ui/index.html` |
| `/api-docs/**` | API documentation | `/api-docs` |
| `/v3/api-docs/**` | OpenAPI v3 specification | `/v3/api-docs` |

### Protected Endpoints (Authentication Required)

| Endpoint Pattern | Required | Example |
|------------------|----------|---------|
| `/api/produits/**` | Authenticated user | `/api/produits`, `/api/produits/1` |
| `/api/fournisseurs/**` | Authenticated user | `/api/fournisseurs` |
| `/api/commandes/**` | Authenticated user | `/api/commandes` |
| `/api/stock/**` | Authenticated user | `/api/stock` |
| `/api/bons-sortie/**` | Authenticated user | `/api/bons-sortie` |

### Method-Level Security (Using @PreAuthorize)

```java
// Role-based
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) { ... }

// Permission-based
@PreAuthorize("hasAuthority('CREER_FOURNISSEUR')")
public Fournisseur createFournisseur(...) { ... }

// Multiple conditions (OR)
@PreAuthorize("hasRole('ADMIN') or hasRole('RESPONSABLE_ACHATS')")
public void validateCommande(Long id) { ... }

// Multiple conditions (AND)
@PreAuthorize("hasRole('MAGASINIER') and hasAuthority('VALIDER_BON_SORTIE')")
public void validateBonSortie(Long id) { ... }

// Complex expressions
@PreAuthorize("hasRole('ADMIN') or (hasRole('MAGASINIER') and hasAuthority('CONSULTER_STOCK'))")
public List<Stock> getStock() { ... }
```

---

## 🧪 Testing

### Test 1: Access Public Endpoint

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "test", "password": "test123"}'
```

**Expected**: 200 OK with tokens (no authentication required)

---

### Test 2: Access Protected Endpoint Without Token

```bash
curl -X GET http://localhost:8080/api/produits
```

**Expected**:
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/produits"
}
```

---

### Test 3: Access Protected Endpoint With Valid Token

```bash
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

curl -X GET http://localhost:8080/api/produits \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with list of products

---

### Test 4: Access Protected Endpoint With Expired Token

```bash
curl -X GET http://localhost:8080/api/produits \
  -H "Authorization: Bearer expired_token_here"
```

**Expected**: 401 Unauthorized

---

### Test 5: Access Swagger UI (Public)

```bash
curl -X GET http://localhost:8080/swagger-ui/index.html
```

**Expected**: 200 OK (HTML page)

---

## 🎯 Key Takeaways

### ✅ What This Configuration Does

1. **Disables CSRF**: Safe for stateless REST APIs with JWT
2. **Stateless Sessions**: No server-side session storage
3. **JWT Validation**: Custom filter validates tokens on each request
4. **Public Endpoints**: Auth and Swagger accessible without login
5. **Protected Endpoints**: Everything else requires authentication
6. **BCrypt Passwords**: Secure password hashing
7. **Custom Error Handling**: JSON responses for authentication failures
8. **Method Security**: Enables @PreAuthorize for fine-grained control

### ✅ Security Benefits

- **Scalable**: No session state to synchronize
- **Secure**: BCrypt hashing, JWT signing, HTTPS-ready
- **Flexible**: Easy to add role/permission checks
- **RESTful**: Proper HTTP status codes and JSON errors
- **Auditable**: All authentication events logged

### ✅ Best Practices Followed

- ✅ Passwords never stored in plain text
- ✅ Tokens are signed and validated
- ✅ Short-lived access tokens (24h)
- ✅ Longer refresh tokens (7d) for convenience
- ✅ Stateless design for scalability
- ✅ Clear separation of public/protected endpoints
- ✅ Proper error handling with meaningful messages

---

## 📚 Related Documentation

- [JWT_AUTHENTICATION_README.md](JWT_AUTHENTICATION_README.md) - JWT token generation and validation
- Spring Security Reference: https://docs.spring.io/spring-security/reference/
- JWT Specification (RFC 7519): https://tools.ietf.org/html/rfc7519

---

**Document Version**: 1.0  
**Last Updated**: December 24, 2024  
**Author**: Tricol Development Team

