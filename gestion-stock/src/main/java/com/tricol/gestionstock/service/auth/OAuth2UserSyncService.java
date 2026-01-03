package com.tricol.gestionstock.service.auth;

import com.tricol.gestionstock.entity.security.RoleApp;
import com.tricol.gestionstock.entity.security.UserApp;
import com.tricol.gestionstock.repository.security.RoleAppRepository;
import com.tricol.gestionstock.repository.security.UserAppRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class OAuth2UserSyncService {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2UserSyncService.class);

    private final UserAppRepository userAppRepository;
    private final RoleAppRepository roleAppRepository;

    public OAuth2UserSyncService(UserAppRepository userAppRepository, RoleAppRepository roleAppRepository) {
        this.userAppRepository = userAppRepository;
        this.roleAppRepository = roleAppRepository;
    }

    @Transactional
    public UserApp syncOAuth2User(OAuth2User oauth2User, String provider) {
        String email = oauth2User.getAttribute("email");
        String username = extractUsername(oauth2User);

        logger.info("Syncing OAuth2 user: {} from provider: {}", username, provider);

        return userAppRepository.findByEmail(email)
                .map(this::updateExistingUser)
                .orElseGet(() -> createNewUser(oauth2User, username, email));
    }

    private UserApp updateExistingUser(UserApp user) {
        user.setLastLogin(LocalDateTime.now());
        logger.info("Updated existing user: {}", user.getUsername());
        return userAppRepository.save(user);
    }

    private UserApp createNewUser(OAuth2User oauth2User, String username, String email) {
        RoleApp defaultRole = roleAppRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));

        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");


        if (firstName == null) firstName = username;
        if (lastName == null) lastName = "";

        UserApp newUser = UserApp.builder()
                .username(username)
                .email(email)
                .password(null)
                .firstName(firstName)
                .lastName(lastName)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .role(defaultRole)
                .lastLogin(LocalDateTime.now())
                .build();

        UserApp savedUser = userAppRepository.save(newUser);
        logger.info("Created new Keycloak user: {}", username);
        return savedUser;
    }

    private String extractUsername(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        if (attributes.containsKey("preferred_username")) {
            return oauth2User.getAttribute("preferred_username");
        } else if (attributes.containsKey("login")) {
            return oauth2User.getAttribute("login"); // GitHub
        } else if (attributes.containsKey("email")) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                return email.split("@")[0];
            }
        }
        
        return "user_" + System.currentTimeMillis();
    }

    @Transactional
    public UserApp syncKeycloakJwtUser(Map<String, Object> claims) {
        String email = (String) claims.get("email");
        String username = extractUsernameFromClaims(claims);

        logger.info("Syncing Keycloak JWT user: {} (sub: {})", username, claims.get("sub"));

        return userAppRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.setLastLogin(LocalDateTime.now());
                    logger.info("Updated existing user from JWT: {}", existingUser.getUsername());
                    return userAppRepository.save(existingUser);
                })
                .orElseGet(() -> createNewUserFromJwt(claims, username, email));
    }

    private UserApp createNewUserFromJwt(Map<String, Object> claims, String username, String email) {
        RoleApp defaultRole = roleAppRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));

        String firstName = (String) claims.getOrDefault("given_name", username);
        String lastName = (String) claims.getOrDefault("family_name", "");

        UserApp newUser = UserApp.builder()
                .username(username)
                .email(email)
                .password(null)
                .firstName(firstName)
                .lastName(lastName)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .role(defaultRole)
                .lastLogin(LocalDateTime.now())
                .build();

        UserApp savedUser = userAppRepository.save(newUser);
        logger.info("Created new user from JWT: {}", username);
        return savedUser;
    }

    private String extractUsernameFromClaims(Map<String, Object> claims) {
        if (claims.containsKey("preferred_username")) {
            return (String) claims.get("preferred_username");
        } else if (claims.containsKey("email")) {
            String email = (String) claims.get("email");
            return email.split("@")[0];
        }
        return "user_" + System.currentTimeMillis();
    }

}
