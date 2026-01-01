package com.tricol.gestionstock.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tricol.gestionstock.entity.security.UserApp;
import com.tricol.gestionstock.security.jwt.JwtUtils;
import com.tricol.gestionstock.service.auth.OAuth2UserSyncService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final OAuth2UserSyncService oauth2UserSyncService;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(OAuth2UserSyncService oauth2UserSyncService, 
                                     JwtUtils jwtUtils,
                                     ObjectMapper objectMapper) {
        this.oauth2UserSyncService = oauth2UserSyncService;
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        //logger.info("Keycloak login success for user: {}", oauth2User.getAttribute("preferred_username"));

        try {
            // Sync Keycloak user to local database
            UserApp user = oauth2UserSyncService.syncOAuth2User(oauth2User, "keycloak");

            // Generate JWT tokens
            String accessToken = jwtUtils.generateAccessToken(user.getUsername());
            String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

            // Prepare response
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("accessToken", accessToken);
            responseBody.put("refreshToken", refreshToken);
            responseBody.put("expiresIn", jwtUtils.getJwtExpirationMs());
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
            userInfo.put("lastName", user.getLastName() != null ? user.getLastName() : "");
            
            responseBody.put("user", userInfo);

            // Send JSON response
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(responseBody));
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            logger.error("Error during Keycloak login", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication failed\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
