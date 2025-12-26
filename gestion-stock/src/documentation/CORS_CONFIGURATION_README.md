# Configuration CORS - Gestion d'Approvisionnement

## Vue d'ensemble

La configuration CORS (Cross-Origin Resource Sharing) a été mise en place pour sécuriser l'application en limitant les origines approuvées qui peuvent accéder à l'API.

## Fichiers impliqués

### 1. `CorsConfig.java`
- **Emplacement** : `src/main/java/com/tricol/gestionstock/config/CorsConfig.java`
- **Rôle** : Configure la source de configuration CORS avec les origines, méthodes et en-têtes autorisés
- **Bean** : `corsConfigurationSource()` - Crée et configure un `CorsConfigurationSource`

### 2. `SecurityConfig.java`
- **Emplacement** : `src/main/java/com/tricol/gestionstock/security/SecurityConfig.java`
- **Modification** : Injection du `CorsConfigurationSource` et intégration dans la chaîne de filtres de sécurité
- **Ligne clé** : `.cors(cors -> cors.configurationSource(corsConfigurationSource))`

### 3. `application.properties`
- **Emplacement** : `src/main/resources/application.properties`
- **Nouvelles propriétés** :
  ```properties
  app.cors.allowed-origins=http://localhost:3000,http://localhost:4200,http://localhost:5173
  app.cors.allowed-methods=GET,POST,PUT,DELETE,PATCH,OPTIONS
  app.cors.allowed-headers=Authorization,Content-Type,Accept,X-Requested-With,Cache-Control
  app.cors.allow-credentials=true
  app.cors.max-age=3600
  ```

## Configuration CORS par défaut

### Origines approuvées (Development)
- `http://localhost:3000` - React (Create React App, Vite)
- `http://localhost:4200` - Angular
- `http://localhost:5173` - Vite (port par défaut)

### Méthodes HTTP autorisées
- GET, POST, PUT, DELETE, PATCH, OPTIONS

### En-têtes autorisés
- Authorization (pour les tokens JWT)
- Content-Type
- Accept
- X-Requested-With
- Cache-Control

### En-têtes exposés
- Authorization
- Content-Type
- X-Total-Count (utile pour la pagination)

### Configuration supplémentaire
- **Credentials** : Activés (permet l'envoi de cookies et headers d'authentification)
- **Max Age** : 3600 secondes (1 heure) - durée de mise en cache de la réponse preflight

## Configuration pour la Production

⚠️ **IMPORTANT** : Avant de déployer en production, vous DEVEZ :

1. **Modifier les origines approuvées** dans `application.properties` :
   ```properties
   app.cors.allowed-origins=https://votre-domaine-frontend.com,https://app.votre-domaine.com
   ```

2. **Utiliser des profils Spring** pour différencier dev et prod :
   
   **application-dev.properties** :
   ```properties
   app.cors.allowed-origins=http://localhost:3000,http://localhost:4200,http://localhost:5173
   ```
   
   **application-prod.properties** :
   ```properties
   app.cors.allowed-origins=https://votre-domaine-frontend.com
   ```

3. **Ne JAMAIS utiliser** `*` comme origine en production :
   ```properties
   # ❌ DANGEREUX en production
   app.cors.allowed-origins=*
   ```

## Utilisation avec profils Spring

Pour activer un profil spécifique :

```bash
# Développement
java -jar gestion-stock.war --spring.profiles.active=dev

# Production
java -jar gestion-stock.war --spring.profiles.active=prod
```

Ou dans `application.properties` :
```properties
spring.profiles.active=dev
```

## Avantages de cette approche

### 1. Centralisation
- Configuration CORS centralisée dans un seul endroit
- Plus besoin d'annotations `@CrossOrigin` dispersées dans les contrôleurs
- Facilite la maintenance

### 2. Sécurité
- Liste blanche d'origines approuvées (whitelist)
- Protection contre les attaques CSRF cross-origin
- Contrôle précis des méthodes et en-têtes autorisés

### 3. Flexibilité
- Configuration externalisée via `application.properties`
- Facilement modifiable sans recompilation
- Support des profils Spring (dev, prod, test)

### 4. Performance
- Mise en cache des réponses preflight (max-age)
- Réduction du nombre de requêtes OPTIONS

## Test de la configuration CORS

### Test avec curl

```bash
# Test d'une requête preflight
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Authorization" \
     -X OPTIONS \
     --verbose \
     http://localhost:8080/api/produits

# Test d'une requête GET simple
curl -H "Origin: http://localhost:3000" \
     --verbose \
     http://localhost:8080/api/produits
```

### Test avec JavaScript (Frontend)

```javascript
// Test depuis la console du navigateur
fetch('http://localhost:8080/api/produits', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer votre-token-jwt'
  },
  credentials: 'include'
})
.then(response => response.json())
.then(data => console.log('Success:', data))
.catch(error => console.error('Error:', error));
```

## Dépannage

### Erreur : "CORS policy: No 'Access-Control-Allow-Origin' header"

**Solution** : Vérifiez que l'origine du frontend est bien dans la liste `app.cors.allowed-origins`

### Erreur : "CORS policy: Request header field authorization is not allowed"

**Solution** : Ajoutez `Authorization` dans `app.cors.allowed-headers`

### Les requêtes OPTIONS échouent

**Solution** : Assurez-vous que `OPTIONS` est dans `app.cors.allowed-methods`

### Les cookies ne sont pas envoyés

**Solution** : Vérifiez que `app.cors.allow-credentials=true` et que le frontend utilise `credentials: 'include'`

## Intégration avec JWT

La configuration CORS fonctionne en synergie avec l'authentification JWT :

1. Le frontend envoie une requête avec l'en-tête `Authorization: Bearer <token>`
2. La configuration CORS valide l'origine
3. Le `JwtAuthenticationFilter` valide le token
4. Si tout est valide, la requête est traitée

## Exemples de configuration avancée

### Configuration avec patterns d'origines

Si vous avez besoin d'autoriser plusieurs sous-domaines :

```java
// Dans CorsConfig.java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:[*]",
    "https://*.votre-domaine.com"
));
```

### Configuration par endpoint

Pour des règles CORS différentes selon les endpoints :

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration publicConfig = new CorsConfiguration();
    publicConfig.setAllowedOrigins(Arrays.asList("*"));
    publicConfig.setAllowedMethods(Arrays.asList("GET"));
    
    CorsConfiguration privateConfig = new CorsConfiguration();
    privateConfig.setAllowedOrigins(Arrays.asList("https://app.votre-domaine.com"));
    privateConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/public/**", publicConfig);
    source.registerCorsConfiguration("/api/**", privateConfig);
    
    return source;
}
```

## Références

- [Spring Security CORS](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)
- [MDN CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [OWASP CORS](https://owasp.org/www-community/attacks/CORS_OriginHeaderScrutiny)

## Auteur

Configuration implémentée le 26 décembre 2025 pour le projet Gestion d'Approvisionnement Tricol.

