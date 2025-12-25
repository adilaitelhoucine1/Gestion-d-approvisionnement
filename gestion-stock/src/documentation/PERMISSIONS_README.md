# 🔐 Permission-Based Authorization - Documentation

## Vue d'ensemble

Ce document décrit l'implémentation de l'autorisation basée sur les permissions pour l'application Tricol - Gestion des Approvisionnements.

---

## 🎓 Deep Dive: Comment fonctionne `@PreAuthorize("hasAuthority('...')")`

### Qu'est-ce que `@PreAuthorize` ?

`@PreAuthorize` est une annotation Spring Security qui effectue des **vérifications d'autorisation AVANT l'exécution de la méthode**. Si la vérification échoue, la méthode n'est jamais appelée et une erreur `403 Forbidden` est retournée.

```
Requête HTTP → JWT Filter → Authentification → Vérification @PreAuthorize → Exécution Méthode
                                                        ↓
                                                  Si échec: 403 Forbidden
```

### Flux Complet de Vérification

#### Étape 1: L'utilisateur s'authentifie (Login)
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "amine",
  "password": "password123"
}
```

#### Étape 2: CustomUserDetails charge les Authorities
Quand l'utilisateur est authentifié, `CustomUserDetails.build()` crée une liste d'**authorities** (permissions):

```java
public static CustomUserDetails build(UserApp user) {
    List<GrantedAuthority> authorities = new ArrayList<>();

    // Si l'utilisateur a le rôle MAGASINIER avec les permissions:
    // [CONSULTER_FOURNISSEUR, RECEPTIONNER_COMMANDE, CONSULTER_STOCK, CREER_BON_SORTIE, ...]
    
    if (user.getRole() != null) {
        // Ajoute le rôle: "ROLE_MAGASINIER"
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
        
        // Ajoute chaque permission du rôle
        user.getRole().getPermissions().forEach(permission ->
            authorities.add(new SimpleGrantedAuthority(permission.getName()))
            // Ajoute: "CONSULTER_FOURNISSEUR", "RECEPTIONNER_COMMANDE", etc.
        );
    }
    
    // Applique les permissions personnalisées (overrides)
    user.getUserPermissions().forEach(userPermission -> {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
            userPermission.getPermission().getName()
        );

        if (userPermission.getGranted()) {
            // Accorde la permission (ajoute si n'existe pas)
            if (!authorities.contains(authority)) {
                authorities.add(authority);
            }
        } else {
            // Révoque la permission (retire si existe)
            authorities.remove(authority);
        }
    });
    
    return new CustomUserDetails(..., authorities);
}
```

**Résultat pour l'utilisateur "amine" (MAGASINIER):**
```java
authorities = [
    "ROLE_MAGASINIER",
    "CONSULTER_FOURNISSEUR",
    "RECEPTIONNER_COMMANDE", 
    "CONSULTER_STOCK",
    "VOIR_VALORISATION",
    "CONSULTER_MOUVEMENTS",
    "CREER_BON_SORTIE",
    "VALIDER_BON_SORTIE",
    "ANNULER_BON_SORTIE",
    "CONSULTER_BON_SORTIE",
    "CONSULTER_COMMANDE"
]
```

#### Étape 3: Le Token JWT encode les informations
Le token JWT contient le username. À chaque requête, `JwtAuthenticationFilter` extrait l'utilisateur et charge ses authorities.

#### Étape 4: @PreAuthorize vérifie l'Authority

```java
@PostMapping
@PreAuthorize("hasAuthority('CREER_FOURNISSEUR')")  // ← LA VÉRIFICATION
public ResponseEntity<FournisseurResponseDTO> createFournisseur(...) {
    // Exécuté seulement si l'utilisateur a 'CREER_FOURNISSEUR' dans ses authorities
}
```

**Ce qui se passe:**
```
Spring Security vérifie: 
  Est-ce que authorities.contains("CREER_FOURNISSEUR") ?
  
Pour l'utilisateur "amine" (MAGASINIER):
  authorities = [..., "CONSULTER_FOURNISSEUR", ...]  ← A CONSULTER, pas CREER!
  
  "CREER_FOURNISSEUR" ∈ authorities? → FALSE → 403 Forbidden
```

### Diagramme de Flux Complet

```
┌─────────────────────────────────────────────────────────────────┐
│                        Requête HTTP                              │
│         POST /api/v1/fournisseurs                               │
│         Authorization: Bearer eyJhbGc...                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   JwtAuthenticationFilter                        │
│  1. Extrait le token du header                                  │
│  2. Valide le token                                             │
│  3. Obtient le username du token                                │
│  4. Charge CustomUserDetails (avec authorities)                 │
│  5. Configure le SecurityContext                                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   SecurityContext                                │
│  Authentication {                                                │
│    principal: CustomUserDetails {                               │
│      username: "amine",                                         │
│      authorities: [                                             │
│        "ROLE_MAGASINIER",                                       │
│        "CONSULTER_FOURNISSEUR",                                 │
│        "CREER_BON_SORTIE",                                      │
│        ...                                                      │
│      ]                                                          │
│    }                                                            │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              @PreAuthorize("hasAuthority('CREER_FOURNISSEUR')") │
│                                                                  │
│  Vérifie: authorities.contains("CREER_FOURNISSEUR")             │
│                                                                  │
│  Pour MAGASINIER: FALSE ❌                                       │
│  Pour ADMIN: TRUE ✅                                             │
│  Pour RESPONSABLE_ACHATS: TRUE ✅                                │
└─────────────────────────────────────────────────────────────────┘
                    │                    │
                    │ FALSE              │ TRUE
                    ▼                    ▼
        ┌──────────────────┐   ┌──────────────────┐
        │   403 Forbidden  │   │  Méthode Exécutée│
        │   Access Denied  │   │  Créer Fournisseur│
        └──────────────────┘   └──────────────────┘
```

### L'Expression SpEL (Spring Expression Language)

`hasAuthority('CREER_FOURNISSEUR')` est une expression **SpEL**.

| Expression | Description |
|------------|-------------|
| `hasAuthority('X')` | L'utilisateur a **exactement** l'authority "X" |
| `hasRole('ADMIN')` | L'utilisateur a le rôle "ROLE_ADMIN" (préfixe ajouté auto) |
| `hasAnyAuthority('X', 'Y')` | L'utilisateur a "X" **OU** "Y" |
| `hasAnyRole('ADMIN', 'MANAGER')` | L'utilisateur a ROLE_ADMIN ou ROLE_MANAGER |
| `isAuthenticated()` | L'utilisateur est connecté |
| `permitAll()` | Tout le monde peut accéder |
| `denyAll()` | Personne ne peut accéder |

#### Exemples Avancés:

```java
// Permission unique
@PreAuthorize("hasAuthority('CREER_FOURNISSEUR')")

// Plusieurs permissions (OU)
@PreAuthorize("hasAnyAuthority('CREER_FOURNISSEUR', 'MODIFIER_FOURNISSEUR')")

// Basé sur le rôle
@PreAuthorize("hasRole('ADMIN')")  // Vérifie ROLE_ADMIN

// Conditions complexes (ET)
@PreAuthorize("hasAuthority('CONSULTER_STOCK') and hasAuthority('VOIR_VALORISATION')")

// Utiliser les paramètres de méthode
@PreAuthorize("hasAuthority('MODIFIER_FOURNISSEUR') or #id == principal.id")
public void updateFournisseur(@PathVariable Long id, ...) { }

// Vérifier le principal (utilisateur connecté)
@PreAuthorize("#username == authentication.principal.username")
public void getProfile(@PathVariable String username) { }
```

### Authority vs Role

| Concept | Préfixe | Exemple | Vérification |
|---------|---------|---------|--------------|
| **Authority** | Aucun | `CREER_FOURNISSEUR` | `hasAuthority('CREER_FOURNISSEUR')` |
| **Role** | `ROLE_` | `ROLE_ADMIN` | `hasRole('ADMIN')` |

Dans notre code:
```java
// Les rôles sont stockés avec le préfixe ROLE_
authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
// → "ROLE_MAGASINIER"

// Les permissions sont stockées sans préfixe
authorities.add(new SimpleGrantedAuthority(permission.getName()));
// → "CREER_FOURNISSEUR"
```

### Configuration Requise

Pour que `@PreAuthorize` fonctionne:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // ← OBLIGATOIRE!
public class SecurityConfig {
    // ...
}
```

⚠️ **Sans `@EnableMethodSecurity`, toutes les annotations `@PreAuthorize` sont IGNORÉES!**

### Tableau de Test par Scénario

| Utilisateur | Rôle | A CREER_FOURNISSEUR? | Résultat |
|-------------|------|----------------------|----------|
| admin | ADMIN | ✅ Oui | 200 OK |
| jean | RESPONSABLE_ACHATS | ✅ Oui | 200 OK |
| amine | MAGASINIER | ❌ Non | 403 Forbidden |
| karim | CHEF_ATELIER | ❌ Non | 403 Forbidden |
| (pas de token) | - | - | 401 Unauthorized |

### Résumé

```java
@PreAuthorize("hasAuthority('CREER_FOURNISSEUR')")
```

**Signifie:**
1. Avant d'exécuter cette méthode...
2. Vérifier si l'utilisateur authentifié actuel...
3. A `"CREER_FOURNISSEUR"` dans sa liste d'authorities...
4. Si OUI → Exécuter la méthode
5. Si NON → Retourner 403 Forbidden

**L'authority provient de:**
- Permissions par défaut du rôle (table `role_permissions`)
- Plus/Moins les permissions personnalisées de l'utilisateur (table `user_permissions`)

---

## Architecture des Permissions

### Entités

```
┌─────────────┐     ┌─────────────┐     ┌─────────────────┐
│   UserApp   │────>│   RoleApp   │────>│   Permission    │
└─────────────┘     └─────────────┘     └─────────────────┘
       │                                        │
       │                                        │
       └────────────>┌─────────────────┐<───────┘
                     │ UserPermission  │
                     └─────────────────┘
```

- **UserApp**: L'utilisateur avec ses informations de connexion
- **RoleApp**: Le rôle attribué (ADMIN, RESPONSABLE_ACHATS, MAGASINIER, CHEF_ATELIER)
- **Permission**: Une permission spécifique (ex: CREER_FOURNISSEUR)
- **UserPermission**: Personnalisation des permissions par utilisateur

### Flux des Permissions

1. **Permissions par défaut du rôle**: Chaque rôle a des permissions par défaut
2. **Personnalisation**: L'admin peut ajouter (`granted=true`) ou retirer (`granted=false`) des permissions spécifiques
3. **Calcul effectif**: `Permissions du rôle + Ajouts - Retraits = Permissions effectives`

## Matrice des Permissions

| Fonctionnalité | ADMIN | RESP_ACHATS | MAGASINIER | CHEF_ATELIER |
|----------------|-------|-------------|------------|--------------|
| **FOURNISSEURS** |
| Créer/Modifier/Supprimer | ✓ | ✓ | ✗ | ✗ |
| Consulter | ✓ | ✓ | ✓ | ✗ |
| **PRODUITS** |
| Créer/Modifier/Supprimer | ✓ | ✓ | ✗ | ✗ |
| Consulter | ✓ | ✓ | ✓ | ✓ |
| Configurer seuils | ✓ | ✓ | ✗ | ✗ |
| **COMMANDES FOURNISSEURS** |
| Créer/Modifier | ✓ | ✓ | ✗ | ✗ |
| Valider | ✓ | ✓ | ✗ | ✗ |
| Annuler | ✓ | ✓ | ✗ | ✗ |
| Réceptionner | ✓ | ✗ | ✓ | ✗ |
| Consulter | ✓ | ✓ | ✓ | ✗ |
| **STOCK & LOTS** |
| Consulter stock/lots | ✓ | ✓ | ✓ | ✓ |
| Voir valorisation FIFO | ✓ | ✓ | ✓ | ✗ |
| Consulter mouvements | ✓ | ✓ | ✓ | ✓ |
| **BONS DE SORTIE** |
| Créer (brouillon) | ✓ | ✗ | ✓ | ✓ |
| Valider | ✓ | ✗ | ✓ | ✗ |
| Annuler | ✓ | ✗ | ✓ | ✗ |
| Consulter | ✓ | ✓ | ✓ | ✓ |
| **ADMINISTRATION** |
| Gérer utilisateurs | ✓ | ✗ | ✗ | ✗ |
| Voir logs d'audit | ✓ | ✗ | ✗ | ✗ |

## Liste des Permissions

### FOURNISSEURS
- `CREER_FOURNISSEUR` - Créer de nouveaux fournisseurs
- `MODIFIER_FOURNISSEUR` - Modifier les fournisseurs existants
- `SUPPRIMER_FOURNISSEUR` - Supprimer des fournisseurs
- `CONSULTER_FOURNISSEUR` - Voir les fournisseurs

### PRODUITS
- `CREER_PRODUIT` - Créer de nouveaux produits
- `MODIFIER_PRODUIT` - Modifier les produits existants
- `SUPPRIMER_PRODUIT` - Supprimer des produits
- `CONSULTER_PRODUIT` - Voir les produits
- `CONFIGURER_SEUILS` - Configurer les seuils d'alerte

### COMMANDES
- `CREER_COMMANDE` - Créer des commandes fournisseurs
- `MODIFIER_COMMANDE` - Modifier des commandes
- `VALIDER_COMMANDE` - Valider des commandes
- `ANNULER_COMMANDE` - Annuler des commandes
- `RECEPTIONNER_COMMANDE` - Réceptionner des commandes
- `CONSULTER_COMMANDE` - Voir les commandes

### STOCK
- `CONSULTER_STOCK` - Voir le stock et les lots
- `VOIR_VALORISATION` - Voir la valorisation FIFO
- `CONSULTER_MOUVEMENTS` - Voir l'historique des mouvements

### BONS_SORTIE
- `CREER_BON_SORTIE` - Créer des bons de sortie (brouillon)
- `VALIDER_BON_SORTIE` - Valider des bons de sortie
- `ANNULER_BON_SORTIE` - Annuler des bons de sortie
- `CONSULTER_BON_SORTIE` - Voir les bons de sortie

### ADMINISTRATION
- `GERER_UTILISATEURS` - Gérer les utilisateurs
- `VOIR_LOGS_AUDIT` - Voir les logs d'audit

## Endpoints Sécurisés

### Fournisseurs (`/api/v1/fournisseurs`)

| Méthode | Endpoint | Permission |
|---------|----------|------------|
| POST | `/` | `CREER_FOURNISSEUR` |
| GET | `/` | `CONSULTER_FOURNISSEUR` |
| GET | `/{id}` | `CONSULTER_FOURNISSEUR` |
| PUT | `/{id}` | `MODIFIER_FOURNISSEUR` |
| DELETE | `/{id}` | `SUPPRIMER_FOURNISSEUR` |
| GET | `/search` | `CONSULTER_FOURNISSEUR` |
| GET | `/email/{email}` | `CONSULTER_FOURNISSEUR` |
| GET | `/ice/{ice}` | `CONSULTER_FOURNISSEUR` |

### Produits (`/api/produits`)

| Méthode | Endpoint | Permission |
|---------|----------|------------|
| POST | `/` | `CREER_PRODUIT` |
| GET | `/` | `CONSULTER_PRODUIT` |
| GET | `/{id}` | `CONSULTER_PRODUIT` |
| PUT | `/{id}` | `MODIFIER_PRODUIT` |
| DELETE | `/{id}` | `SUPPRIMER_PRODUIT` |
| GET | `/{id}/stock` | `CONSULTER_PRODUIT` |

### Commandes Fournisseurs (`/api/v1/commandes`)

| Méthode | Endpoint | Permission |
|---------|----------|------------|
| POST | `/` | `CREER_COMMANDE` |
| GET | `/` | `CONSULTER_COMMANDE` |
| GET | `/{id}` | `CONSULTER_COMMANDE` |
| PUT | `/{id}` | `MODIFIER_COMMANDE` |
| DELETE | `/{id}` | `ANNULER_COMMANDE` |
| GET | `/fournisseur/{id}` | `CONSULTER_COMMANDE` |
| PUT | `/{id}/valider` | `VALIDER_COMMANDE` |
| PUT | `/{id}/annuler` | `ANNULER_COMMANDE` |
| PUT | `/{id}/reception` | `RECEPTIONNER_COMMANDE` |

### Bons de Sortie (`/api/v1/bons-sortie`)

| Méthode | Endpoint | Permission |
|---------|----------|------------|
| POST | `/` | `CREER_BON_SORTIE` |
| GET | `/` | `CONSULTER_BON_SORTIE` |
| GET | `/{id}` | `CONSULTER_BON_SORTIE` |
| PUT | `/{id}` | `CREER_BON_SORTIE` |
| PUT | `/{id}/valider` | `VALIDER_BON_SORTIE` |
| PUT | `/{id}/annuler` | `ANNULER_BON_SORTIE` |
| GET | `/atelier/{atelier}` | `CONSULTER_BON_SORTIE` |

### Stock & Lots (`/api/v1/stock`)

| Méthode | Endpoint | Permission |
|---------|----------|------------|
| GET | `/` | `CONSULTER_STOCK` |
| GET | `/produit/{id}` | `CONSULTER_STOCK` |
| GET | `/alertes` | `CONSULTER_STOCK` |
| GET | `/valorisation` | `VOIR_VALORISATION` |
| GET | `/mouvements` | `CONSULTER_MOUVEMENTS` |
| GET | `/mouvements/produit/{id}` | `CONSULTER_MOUVEMENTS` |

### Administration - Gestion Utilisateurs (`/api/v1/admin/users`)

| Méthode | Endpoint | Permission | Description |
|---------|----------|------------|-------------|
| GET | `/` | `GERER_UTILISATEURS` | Lister tous les utilisateurs |
| GET | `/{userId}` | `GERER_UTILISATEURS` | Obtenir un utilisateur |
| GET | `/username/{username}` | `GERER_UTILISATEURS` | Chercher par username |
| DELETE | `/{userId}` | `GERER_UTILISATEURS` | Supprimer un utilisateur |
| PUT | `/{userId}/enable` | `GERER_UTILISATEURS` | Activer un utilisateur |
| PUT | `/{userId}/disable` | `GERER_UTILISATEURS` | Désactiver un utilisateur |
| GET | `/roles` | `GERER_UTILISATEURS` | Lister les rôles |
| PUT | `/{userId}/role` | `GERER_UTILISATEURS` | Assigner un rôle |
| DELETE | `/{userId}/role` | `GERER_UTILISATEURS` | Retirer le rôle |
| GET | `/permissions` | `GERER_UTILISATEURS` | Lister les permissions |
| GET | `/permissions/category/{cat}` | `GERER_UTILISATEURS` | Permissions par catégorie |
| PUT | `/{userId}/permissions` | `GERER_UTILISATEURS` | Modifier permission user |
| DELETE | `/{userId}/permissions/{name}` | `GERER_UTILISATEURS` | Supprimer permission custom |
| GET | `/{userId}/permissions/custom` | `GERER_UTILISATEURS` | Permissions personnalisées |
| GET | `/{userId}/permissions/effective` | `GERER_UTILISATEURS` | Permissions effectives |

## Exemple d'Utilisation - Gestion Dynamique des Permissions

### Cas: Retirer une permission à un utilisateur

**Scénario**: L'utilisateur "amine" a le rôle MAGASINIER. L'admin veut lui retirer la permission de créer des bons de sortie.

```bash
# 1. L'admin se connecte
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}

# 2. Modifier la permission de l'utilisateur
PUT /api/v1/admin/users/5/permissions
Authorization: Bearer <admin_token>
{
  "permissionName": "CREER_BON_SORTIE",
  "granted": false
}
```

**Résultat**: Amine conserve son rôle MAGASINIER mais ne peut plus créer de bons de sortie.

### Cas: Accorder une permission supplémentaire

**Scénario**: L'utilisateur "karim" est CHEF_ATELIER mais doit pouvoir voir les fournisseurs.

```bash
PUT /api/v1/admin/users/7/permissions
Authorization: Bearer <admin_token>
{
  "permissionName": "CONSULTER_FOURNISSEUR",
  "granted": true
}
```

### Cas: Voir les permissions effectives

```bash
GET /api/v1/admin/users/5/permissions/effective
Authorization: Bearer <admin_token>
```

**Réponse**:
```json
[
  {
    "id": 14,
    "name": "RECEPTIONNER_COMMANDE",
    "description": "Receive purchase orders",
    "category": "COMMANDES"
  },
  {
    "id": 16,
    "name": "CONSULTER_STOCK",
    "description": "View stock and lots",
    "category": "STOCK"
  }
  // ... autres permissions effectives
]
```

## Comment ça Fonctionne Techniquement

### 1. Chargement des Permissions (CustomUserDetails)

```java
public static CustomUserDetails build(UserApp user) {
    List<GrantedAuthority> authorities = new ArrayList<>();

    // 1. Ajouter le rôle
    if (user.getRole() != null) {
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
        
        // 2. Ajouter les permissions par défaut du rôle
        user.getRole().getPermissions().forEach(permission ->
            authorities.add(new SimpleGrantedAuthority(permission.getName()))
        );
    }

    // 3. Appliquer les personnalisations
    user.getUserPermissions().forEach(userPermission -> {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
            userPermission.getPermission().getName()
        );

        if (userPermission.getGranted()) {
            // Accorder la permission
            if (!authorities.contains(authority)) {
                authorities.add(authority);
            }
        } else {
            // Révoquer la permission
            authorities.remove(authority);
        }
    });

    return new CustomUserDetails(..., authorities);
}
```

### 2. Vérification des Permissions (@PreAuthorize)

```java
@PostMapping
@PreAuthorize("hasAuthority('CREER_FOURNISSEUR')")
public ResponseEntity<FournisseurResponseDTO> createFournisseur(...) {
    // Seuls les utilisateurs avec la permission CREER_FOURNISSEUR peuvent accéder
}
```

### 3. Configuration Spring Security

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Active @PreAuthorize
public class SecurityConfig {
    // ...
}
```

## Réponses d'Erreur

### 401 Unauthorized
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

### 403 Forbidden (Permission insuffisante)
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

## Tests avec Postman

1. **Se connecter** avec un utilisateur ayant le rôle approprié
2. **Copier le token** JWT de la réponse
3. **Ajouter le header**: `Authorization: Bearer <token>`
4. **Tester les endpoints** - vérifier les réponses 200/403

### Collection de Tests

```
# Utilisateur ADMIN - devrait avoir accès à tout
# Utilisateur RESPONSABLE_ACHATS - pas d'accès aux bons de sortie
# Utilisateur MAGASINIER - pas d'accès aux créations fournisseurs/produits
# Utilisateur CHEF_ATELIER - accès limité (consultation produits, stock, bons de sortie)
```

## Prochaines Étapes

- [ ] Implémenter le système d'audit (VOIR_LOGS_AUDIT)
- [ ] Ajouter la permission CONFIGURER_SEUILS aux endpoints appropriés
- [ ] Tests unitaires pour les permissions
- [ ] Tests d'intégration avec différents rôles

