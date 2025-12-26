# UserDetails vs UserDetailsService - Spring Security Documentation

## 📚 Vue d'ensemble

Dans Spring Security, `UserDetails` et `UserDetailsService` sont deux interfaces fondamentales qui travaillent ensemble pour gérer l'authentification des utilisateurs. Comprendre la différence entre ces deux concepts est essentiel pour implémenter correctement la sécurité dans votre application.

---

## 🔑 UserDetails - Le Modèle Utilisateur

### Définition
`UserDetails` est une **interface** qui représente les informations essentielles d'un utilisateur authentifié. C'est le **contrat/modèle** que Spring Security utilise pour stocker les données de l'utilisateur dans le contexte de sécurité.

### Responsabilités
- Contenir les informations d'identification (username, password)
- Stocker les autorités/permissions de l'utilisateur
- Indiquer l'état du compte (actif, expiré, verrouillé, etc.)

### Interface UserDetails (méthodes à implémenter)

```java
public interface UserDetails extends Serializable {
    // Retourne les autorités/permissions accordées à l'utilisateur
    Collection<? extends GrantedAuthority> getAuthorities();
    
    // Retourne le mot de passe utilisé pour l'authentification
    String getPassword();
    
    // Retourne le nom d'utilisateur
    String getUsername();
    
    // Indique si le compte a expiré
    boolean isAccountNonExpired();
    
    // Indique si le compte est verrouillé
    boolean isAccountNonLocked();
    
    // Indique si les identifiants ont expiré
    boolean isCredentialsNonExpired();
    
    // Indique si l'utilisateur est activé
    boolean isEnabled();
}
```

### Notre Implémentation: CustomUserDetails

```java
@Getter
public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final String email;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.enabled = user.isEnabled();
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}
```

### Points Clés
- **C'est un objet de données** (Data Object/Model)
- **Stocké dans le SecurityContext** après authentification réussie
- **Accessible partout** dans l'application via `SecurityContextHolder`
- Peut être étendu pour ajouter des champs personnalisés (ex: `id`, `email`)

---

## 🔧 UserDetailsService - Le Service de Chargement

### Définition
`UserDetailsService` est une **interface de service** utilisée par Spring Security pour **charger les données utilisateur** à partir d'une source de données (base de données, LDAP, etc.).

### Responsabilités
- Charger un utilisateur par son nom d'utilisateur
- Transformer l'entité utilisateur en `UserDetails`
- Lever une exception si l'utilisateur n'existe pas

### Interface UserDetailsService

```java
public interface UserDetailsService {
    // Charge un utilisateur par son username
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
```

### Notre Implémentation: CustomUserDetailsService

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Charger l'utilisateur depuis la base de données
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur non trouvé: " + username));

        // 2. Calculer les permissions effectives
        Set<GrantedAuthority> authorities = calculateEffectivePermissions(user);

        // 3. Retourner un CustomUserDetails
        return new CustomUserDetails(user, authorities);
    }

    private Set<GrantedAuthority> calculateEffectivePermissions(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Ajouter les permissions du rôle
        if (user.getRole() != null) {
            user.getRole().getPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getName()))
            );
        }

        // Appliquer les permissions personnalisées
        user.getUserPermissions().forEach(up -> {
            SimpleGrantedAuthority authority = 
                new SimpleGrantedAuthority(up.getPermission().getName());
            if (up.isGranted()) {
                authorities.add(authority);
            } else {
                authorities.remove(authority);
            }
        });

        return authorities;
    }
}
```

### Points Clés
- **C'est un service** (Business Logic)
- **Appelé une seule fois** lors de l'authentification
- **Fait le lien** entre votre entité `User` et le `UserDetails` de Spring Security
- **Gère la logique métier** (calcul des permissions, vérifications, etc.)

---

## 🔄 Comparaison Directe

| Aspect | UserDetails | UserDetailsService |
|--------|-------------|-------------------|
| **Type** | Interface/Modèle | Interface/Service |
| **Rôle** | Représente un utilisateur | Charge un utilisateur |
| **Contient** | Données utilisateur | Logique de chargement |
| **Méthodes** | Getters (username, password, authorities...) | `loadUserByUsername()` |
| **Quand utilisé** | Stocké après authentification | Appelé pendant l'authentification |
| **Analogie** | Le "passeport" de l'utilisateur | L'agent qui délivre le passeport |
| **Durée de vie** | Existe pendant toute la session | Appelé ponctuellement |

---

## 🔁 Flux d'Authentification Complet

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        FLUX D'AUTHENTIFICATION                               │
└─────────────────────────────────────────────────────────────────────────────┘

1. Requête de Login
   ┌──────────────┐
   │   Client     │ ──POST /api/auth/login──▶ { username, password }
   └──────────────┘

2. AuthenticationManager entre en jeu
   ┌─────────────────────────┐
   │  AuthenticationManager  │
   │  (DaoAuthenticationProvider) │
   └───────────┬─────────────┘
               │
               │ Appelle loadUserByUsername(username)
               ▼
   ┌─────────────────────────┐
   │ CustomUserDetailsService│  ◄── SERVICE (logique métier)
   │                         │
   │ - Cherche user en DB    │
   │ - Calcule permissions   │
   │ - Crée CustomUserDetails│
   └───────────┬─────────────┘
               │
               │ Retourne UserDetails
               ▼
   ┌─────────────────────────┐
   │   CustomUserDetails     │  ◄── MODÈLE (données)
   │                         │
   │ - id, username, email   │
   │ - password (hashé)      │
   │ - authorities (perms)   │
   │ - enabled, etc.         │
   └───────────┬─────────────┘
               │
3. Vérification du mot de passe
   ┌─────────────────────────┐
   │   PasswordEncoder       │
   │   .matches(raw, hashed) │
   └───────────┬─────────────┘
               │
               │ Si OK
               ▼
4. Stockage dans SecurityContext
   ┌─────────────────────────┐
   │   SecurityContext       │
   │   .setAuthentication()  │
   │                         │
   │   Authentication {      │
   │     principal: UserDetails
   │     credentials: null   │
   │     authorities: [...]  │
   │   }                     │
   └───────────┬─────────────┘
               │
5. Génération du JWT
   ┌─────────────────────────┐
   │     JwtService          │
   │  .generateToken(user)   │
   └───────────┬─────────────┘
               │
               ▼
   ┌──────────────┐
   │   Client     │ ◄── Reçoit le JWT token
   └──────────────┘
```

---

## 🔐 Utilisation dans l'Application

### Récupérer l'utilisateur connecté

```java
// Dans un Controller ou Service
@GetMapping("/me")
public ResponseEntity<?> getCurrentUser() {
    // Récupérer l'Authentication du SecurityContext
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    // Le principal est notre CustomUserDetails
    CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
    
    // Accéder aux informations
    Long userId = userDetails.getId();
    String username = userDetails.getUsername();
    Collection<? extends GrantedAuthority> permissions = userDetails.getAuthorities();
    
    return ResponseEntity.ok(userDetails);
}
```

### Vérifier les permissions avec @PreAuthorize

```java
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    // Spring Security vérifie automatiquement les authorities du UserDetails
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        // Cette méthode n'est accessible que si 
        // userDetails.getAuthorities() contient 'GERER_UTILISATEURS'
        return ResponseEntity.ok(adminService.getAllUsers());
    }
}
```

---

## 📊 Diagramme de Classes

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Spring Security Interfaces                        │
└─────────────────────────────────────────────────────────────────────┘

    ┌─────────────────────┐         ┌─────────────────────────┐
    │   <<interface>>     │         │     <<interface>>       │
    │    UserDetails      │         │   UserDetailsService    │
    ├─────────────────────┤         ├─────────────────────────┤
    │ +getUsername()      │         │ +loadUserByUsername()   │
    │ +getPassword()      │◄────────│   : UserDetails         │
    │ +getAuthorities()   │ returns └───────────┬─────────────┘
    │ +isEnabled()        │                     │
    │ +isAccountNonExpired│                     │ implements
    │ +isAccountNonLocked │                     │
    │ +isCredentialsNon...│                     ▼
    └─────────┬───────────┘         ┌─────────────────────────┐
              │                     │ CustomUserDetailsService│
              │ implements          ├─────────────────────────┤
              │                     │ -userRepository         │
              ▼                     │ +loadUserByUsername()   │
    ┌─────────────────────┐         │ -calculatePermissions() │
    │  CustomUserDetails  │         └─────────────────────────┘
    ├─────────────────────┤                     │
    │ -id: Long           │                     │ uses
    │ -username: String   │                     │
    │ -password: String   │◄────────────────────┘
    │ -email: String      │
    │ -enabled: boolean   │
    │ -authorities: Set   │
    └─────────────────────┘
              │
              │ built from
              ▼
    ┌─────────────────────┐
    │    User (Entity)    │
    ├─────────────────────┤
    │ -id: Long           │
    │ -username: String   │
    │ -password: String   │
    │ -email: String      │
    │ -enabled: boolean   │
    │ -role: Role         │
    │ -userPermissions    │
    └─────────────────────┘
```

---

## 🎯 Résumé

| Composant | Analogie Simple | Rôle |
|-----------|----------------|------|
| **UserDetails** | 🪪 Carte d'identité | Contient les infos de l'utilisateur |
| **UserDetailsService** | 🏛️ Bureau d'état civil | Délivre la carte d'identité |
| **SecurityContext** | 👜 Portefeuille | Stocke la carte pendant la session |
| **@PreAuthorize** | 🚪 Vigile | Vérifie la carte avant d'autoriser l'accès |

### En une phrase:
> **UserDetailsService** charge et construit un **UserDetails** qui est ensuite stocké dans le **SecurityContext** et utilisé pour les vérifications d'autorisation.

---

## 📁 Fichiers dans notre projet

| Fichier | Chemin | Description |
|---------|--------|-------------|
| `CustomUserDetails.java` | `security/CustomUserDetails.java` | Notre implémentation de UserDetails |
| `CustomUserDetailsService.java` | `security/CustomUserDetailsService.java` | Notre implémentation de UserDetailsService |
| `SecurityConfig.java` | `config/SecurityConfig.java` | Configuration Spring Security |
| `JwtAuthenticationFilter.java` | `security/JwtAuthenticationFilter.java` | Filtre JWT qui utilise UserDetailsService |

---

## 🔗 Liens Utiles

- [Spring Security Reference - UserDetails](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details.html)
- [Spring Security Reference - UserDetailsService](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details-service.html)
- [Baeldung - Spring Security UserDetailsService](https://www.baeldung.com/spring-security-authentication-with-a-database)

