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
        String username = extractUsername(oauth2User, provider);
        
        logger.info("Syncing OAuth2 user: {} from provider: {}", username, provider);

        return userAppRepository.findByEmail(email)
                .map(existingUser -> updateExistingUser(existingUser, oauth2User))
                .orElseGet(() -> createNewUser(oauth2User, username, email, provider));
    }

    private UserApp updateExistingUser(UserApp user, OAuth2User oauth2User) {
        user.setLastLogin(LocalDateTime.now());
        logger.info("Updated existing user: {}", user.getUsername());
        return userAppRepository.save(user);
    }

    private UserApp createNewUser(OAuth2User oauth2User, String username, String email, String provider) {
        RoleApp defaultRole = roleAppRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));

        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");
        
        // Fallback if names not provided
        if (firstName == null) firstName = username;
        if (lastName == null) lastName = "";

        UserApp newUser = UserApp.builder()
                .username(username)
                .email(email)
                .password(null) // No password for Keycloak users
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

    private String extractUsername(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        if (attributes.containsKey("preferred_username")) {
            return oauth2User.getAttribute("preferred_username");
        } else if (attributes.containsKey("login")) {
            return oauth2User.getAttribute("login"); // GitHub
        } else if (attributes.containsKey("email")) {
            String email = oauth2User.getAttribute("email");
            return email.split("@")[0];
        }
        
        return "user_" + System.currentTimeMillis();
    }
}
