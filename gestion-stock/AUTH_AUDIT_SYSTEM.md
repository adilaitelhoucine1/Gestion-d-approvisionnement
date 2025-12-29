# 🔐 Système d'Audit pour l'Authentification

## 📋 Vue d'ensemble

Le système d'audit d'authentification trace automatiquement toutes les actions sensibles liées à la sécurité :
- ✅ Connexions (succès/échec)
- ✅ Déconnexions
- ✅ Inscriptions d'utilisateurs
- ✅ Rafraîchissement de tokens
- ✅ Modifications de permissions
- ✅ Changements de rôles

## 🏗️ Architecture

### Composants créés

1. **AuthAuditLog** (Entity) - Table de stockage des logs
2. **AuthAuditLogRepository** - Repository avec requêtes avancées
3. **AuthAuditService** - Service de logging asynchrone
4. **AuthAuditController** - API REST pour consulter les logs
5. **AuthAuditLogDTO** - DTO pour les réponses API

## 📊 Structure de la table `auth_audit_logs`

| Colonne | Type | Description |
|---------|------|-------------|
| id | BIGINT | Identifiant unique |
| username | VARCHAR(100) | Nom d'utilisateur concerné |
| action | ENUM | Type d'action (LOGIN_SUCCESS, LOGIN_FAILURE, etc.) |
| status | ENUM | Statut (SUCCESS, FAILURE, PENDING) |
| ip_address | VARCHAR(45) | Adresse IP du client |
| user_agent | VARCHAR(255) | User-Agent du navigateur |
| details | VARCHAR(1000) | Détails supplémentaires |
| error_message | VARCHAR(500) | Message d'erreur (si échec) |
| action_date | TIMESTAMP | Date et heure de l'action |
| affected_resource | VARCHAR(100) | Ressource affectée |
| old_value | VARCHAR(500) | Ancienne valeur (pour les modifications) |
| new_value | VARCHAR(500) | Nouvelle valeur (pour les modifications) |

### Index pour la performance
- `idx_auth_audit_user` - Sur username
- `idx_auth_audit_action` - Sur action
- `idx_auth_audit_date` - Sur action_date
- `idx_auth_audit_status` - Sur status

## 🎯 Types d'actions trackées

```java
public enum AuthAction {
    LOGIN_SUCCESS,              // Connexion réussie
    LOGIN_FAILURE,              // Tentative de connexion échouée
    LOGOUT,                     // Déconnexion
    REGISTER,                   // Inscription d'un nouvel utilisateur
    TOKEN_REFRESH,              // Rafraîchissement du token JWT
    PASSWORD_CHANGE,            // Changement de mot de passe
    PERMISSION_GRANTED,         // Permission accordée
    PERMISSION_REVOKED,         // Permission révoquée
    ROLE_ASSIGNED,              // Rôle attribué
    ROLE_REMOVED,               // Rôle supprimé
    ACCOUNT_LOCKED,             // Compte verrouillé
    ACCOUNT_UNLOCKED,           // Compte déverrouillé
    PASSWORD_RESET_REQUEST,     // Demande de réinitialisation de mot de passe
    PASSWORD_RESET_COMPLETE,    // Réinitialisation de mot de passe terminée
    TWO_FACTOR_ENABLED,         // 2FA activé
    TWO_FACTOR_DISABLED         // 2FA désactivé
}
```

## 🔍 API Endpoints

### 1. Obtenir les logs par utilisateur
```http
GET /api/audit/auth/user/{username}?page=0&size=20
Authorization: Bearer {token}
Permission Required: GERER_UTILISATEURS ou VIEW_AUDIT_LOGS
```

**Exemple de réponse:**
```json
{
  "content": [
    {
      "id": 1,
      "username": "admin",
      "action": "LOGIN_SUCCESS",
      "status": "SUCCESS",
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0...",
      "details": "User logged in successfully",
      "actionDate": "2025-12-26T10:30:00"
    }
  ],
  "totalElements": 50,
  "totalPages": 3
}
```

### 2. Obtenir les logs par type d'action
```http
GET /api/audit/auth/action/LOGIN_FAILURE?page=0&size=20
Authorization: Bearer {token}
Permission Required: GERER_UTILISATEURS
```

### 3. Obtenir les logs par période
```http
GET /api/audit/auth/date-range?startDate=2025-12-01T00:00:00&endDate=2025-12-26T23:59:59
Authorization: Bearer {token}
Permission Required: GERER_UTILISATEURS
```

### 4. Obtenir les changements de permissions
```http
GET /api/audit/auth/permissions/changes?page=0&size=20
Authorization: Bearer {token}
Permission Required: GERER_UTILISATEURS
```

### 5. Rechercher dans les logs
```http
GET /api/audit/auth/search?username=john&action=LOGIN_FAILURE&startDate=2025-12-01T00:00:00
Authorization: Bearer {token}
Permission Required: GERER_UTILISATEURS
```

**Paramètres de recherche:**
- `username` - Nom d'utilisateur (partiel)
- `action` - Type d'action
- `status` - Statut (SUCCESS/FAILURE)
- `ipAddress` - Adresse IP
- `startDate` - Date de début
- `endDate` - Date de fin
- `page` - Numéro de page
- `size` - Taille de la page

### 6. Résumé d'activité utilisateur
```http
GET /api/audit/auth/user/{username}/summary?days=30
Authorization: Bearer {token}
Permission Required: GERER_UTILISATEURS ou VIEW_AUDIT_LOGS
```

**Exemple de réponse:**
```json
{
  "LOGIN_SUCCESS": 45,
  "LOGIN_FAILURE": 3,
  "LOGOUT": 42,
  "TOKEN_REFRESH": 120
}
```

### 7. Tentatives de connexion échouées
```http
GET /api/audit/auth/user/{username}/failed-logins?minutes=60
Authorization: Bearer {token}
Permission Required: GERER_UTILISATEURS
```

### 8. Détecter une activité suspecte
```http
GET /api/audit/auth/user/{username}/suspicious-activity?failureThreshold=5&minutesWindow=30
Authorization: Bearer {token}
Permission Required: GERER_UTILISATEURS
```

**Exemple de réponse:**
```json
{
  "username": "john",
  "hasSuspiciousActivity": true,
  "threshold": 5,
  "windowMinutes": 30
}
```

## 💻 Utilisation dans le code

### Logging automatique

Le système est déjà intégré dans `AuthService`. Toutes les actions d'authentification sont automatiquement loggées :

```java
@Autowired
private AuthAuditService auditService;

// Login success est automatiquement loggé
auditService.logLoginSuccess(username);

// Login failure est automatiquement loggé
auditService.logLoginFailure(username, "Invalid credentials");
```

### Logging manuel pour les permissions

Pour logger les changements de permissions/rôles :

```java
@Autowired
private AuthAuditService auditService;

// Logger l'attribution d'une permission
auditService.logPermissionChange(
    "john_doe",                          // Utilisateur affecté
    AuthAction.PERMISSION_GRANTED,       // Action
    "CREER_PRODUIT",                     // Ressource affectée
    null,                                // Ancienne valeur
    "CREER_PRODUIT"                      // Nouvelle valeur
);

// Logger l'attribution d'un rôle
auditService.logPermissionChange(
    "john_doe",
    AuthAction.ROLE_ASSIGNED,
    "User john_doe",
    "ROLE_USER",
    "ROLE_USER, ROLE_MANAGER"
);
```

### Logging asynchrone

Toutes les méthodes de logging sont **asynchrones** (`@Async`) pour ne pas impacter les performances :

```java
@Async
public void logAuthAction(String username, AuthAction action, 
                         AuditStatus status, String details) {
    // Le logging se fait en arrière-plan
}
```

## 🔒 Sécurité et Permissions

### Permissions requises

Pour accéder aux logs d'audit, l'utilisateur doit avoir :
- `GERER_UTILISATEURS` - Accès complet aux logs
- `VIEW_AUDIT_LOGS` - Accès en lecture seule (ses propres logs)

### Exemples de vérification

```java
@PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
public Page<AuthAuditLog> getAllAuditLogs() { ... }

@PreAuthorize("hasAnyAuthority('GERER_UTILISATEURS', 'VIEW_AUDIT_LOGS')")
public Page<AuthAuditLog> getMyAuditLogs() { ... }
```

## 📈 Cas d'usage

### 1. Détecter les tentatives de piratage

```java
// Vérifier si un utilisateur a plus de 5 tentatives échouées en 30 minutes
boolean suspicious = auditService.hasSuspiciousActivity("john_doe", 5, 30);

if (suspicious) {
    // Verrouiller le compte ou envoyer une alerte
    alertService.sendSecurityAlert(username);
}
```

### 2. Audit de conformité

```java
// Obtenir tous les changements de permissions des 90 derniers jours
LocalDateTime startDate = LocalDateTime.now().minusDays(90);
LocalDateTime endDate = LocalDateTime.now();
Page<AuthAuditLog> changes = auditService.searchAuditLogs(
    null, null, null, null, startDate, endDate, pageable
);
```

### 3. Rapport d'activité utilisateur

```java
// Résumé d'activité des 30 derniers jours
Map<AuthAction, Long> summary = auditService.getUserActivitySummary(
    "john_doe", 
    LocalDateTime.now().minusDays(30)
);
```

## 🎨 Exemple d'utilisation dans AdminController

Pour tracker les changements de permissions dans `AdminController` :

```java
@PostMapping("/users/{userId}/permissions/{permissionId}")
@PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
public ResponseEntity<?> grantPermission(
        @PathVariable Long userId, 
        @PathVariable Long permissionId) {
    
    UserApp user = userService.findById(userId);
    Permission permission = permissionService.findById(permissionId);
    
    // Sauvegarder l'ancienne liste de permissions
    String oldPermissions = user.getPermissions().stream()
        .map(Permission::getName)
        .collect(Collectors.joining(", "));
    
    // Ajouter la nouvelle permission
    user.getPermissions().add(permission);
    userService.save(user);
    
    // Logger le changement
    auditService.logPermissionChange(
        user.getUsername(),
        AuthAction.PERMISSION_GRANTED,
        permission.getName(),
        oldPermissions,
        oldPermissions + ", " + permission.getName()
    );
    
    return ResponseEntity.ok("Permission granted");
}
```

## 🧪 Tests

### Test de logging

```java
@Test
public void testLoginAudit() {
    // Login réussi
    authService.login(new LoginRequestDTO("admin", "password"));
    
    // Vérifier que le log est créé
    Page<AuthAuditLog> logs = auditService.getAuditLogsByUsername("admin", pageable);
    assertEquals(1, logs.getTotalElements());
    assertEquals(AuthAction.LOGIN_SUCCESS, logs.getContent().get(0).getAction());
}
```

## 📊 Indicateurs de performance

- **Logging asynchrone** : Pas d'impact sur le temps de réponse
- **Index optimisés** : Recherches rapides même avec millions de logs
- **Pagination** : Gestion efficace de grandes quantités de données

## 🔄 Maintenance

### Nettoyage des anciens logs

Recommandation : Créer un job programmé pour nettoyer les logs de plus de 1 an :

```java
@Scheduled(cron = "0 0 2 * * *") // Tous les jours à 2h du matin
public void cleanOldAuditLogs() {
    LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
    auditLogRepository.deleteByActionDateBefore(oneYearAgo);
}
```

## ✅ Résumé

### Ce qui est tracké automatiquement
- ✅ Connexions (succès/échec) avec IP et User-Agent
- ✅ Déconnexions
- ✅ Inscriptions
- ✅ Rafraîchissements de tokens

### Ce qui nécessite un ajout manuel
- ⚠️ Changements de permissions (à ajouter dans AdminController)
- ⚠️ Changements de rôles (à ajouter dans AdminController)
- ⚠️ Verrouillage/déverrouillage de comptes

### Avantages
- 🚀 Asynchrone - Aucun impact sur les performances
- 🔍 Recherche avancée avec filtres multiples
- 📊 Statistiques et résumés d'activité
- 🛡️ Détection d'activités suspectes
- 📝 Conformité et audit de sécurité

---

**Version**: 1.0.0  
**Date**: 26 Décembre 2025  
**Status**: ✅ Production Ready

