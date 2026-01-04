package com.tricol.gestionstock.security;

import com.tricol.gestionstock.entity.security.UserApp;
import com.tricol.gestionstock.security.jwt.JwtUtils;
import com.tricol.gestionstock.service.auth.OAuth2UserSyncService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class HybridJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HybridJwtAuthenticationFilter.class);

    private final JwtUtils customJwtUtils;
    private final UserDetailsService userDetailsService;
    private JwtDecoder jwtDecoder;
    private JwtAuthenticationConverter jwtAuthenticationConverter;
    private OAuth2UserSyncService oAuth2UserSyncService;

    public HybridJwtAuthenticationFilter(
            JwtUtils customJwtUtils,
            UserDetailsService userDetailsService) {
        this.customJwtUtils = customJwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Autowired(required = false)
    public void setJwtDecoder(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Autowired(required = false)
    public void setJwtAuthenticationConverter(JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Autowired
    public void setOAuth2UserSyncService(OAuth2UserSyncService oAuth2UserSyncService) {
        this.oAuth2UserSyncService = oAuth2UserSyncService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String headerAuth = request.getHeader("Authorization");
            logger.info(">>> Processing request: {} {} | Auth header present: {}",
                    request.getMethod(), request.getRequestURI(), headerAuth != null);

            String jwt = parseJwt(request);

            if (jwt != null) {
                logger.info(">>> JWT token found, length: {}", jwt.length());
                if (customJwtUtils.validateToken(jwt)) {
                    logger.info(">>> Custom JWT validation successful");
                    authenticateWithCustomJwt(jwt, request);
                } else {
                    logger.info(">>> Custom JWT validation failed, trying Keycloak");
                    authenticateWithKeycloakJwt(jwt, request);
                }
            } else {
                logger.info(">>> No JWT token found in request (Authorization header: {})",
                        headerAuth != null ? headerAuth.substring(0, Math.min(20, headerAuth.length())) + "..." : "null");
            }
        } catch (Exception e) {
            logger.error(">>> Authentication failed: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateWithCustomJwt(String jwt, HttpServletRequest request) {
        try {
            String username = customJwtUtils.getUsernameFromToken(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("✅ Custom JWT authenticated: {}", username);
        } catch (Exception e) {
            logger.debug("Custom JWT failed", e);
        }
    }

    private void authenticateWithKeycloakJwt(String jwt, HttpServletRequest request) {

        if (jwtDecoder == null) {
            logger.warn(">>> ⚠️  Keycloak JWT decoder not available, skipping Keycloak authentication");
            return;
        }

        try {
            logger.info(">>> Attempting to decode Keycloak JWT...");
            Jwt decodedJwt = jwtDecoder.decode(jwt);
            Map<String, Object> claims = decodedJwt.getClaims();
            logger.info(">>> ✅ Keycloak JWT decoded successfully");
            logger.debug(">>> JWT claims: {}", claims);

            // Get username from Keycloak claims
            String username = (String) claims.get("preferred_username");
            if (username == null) {
                username = decodedJwt.getSubject();
            }
            logger.info(">>> Extracted username from JWT: {}", username);

            // Sync user from Keycloak to local database FIRST
            if (oAuth2UserSyncService != null) {
                try {
                    logger.info(">>> Syncing user to local database...");
                    logger.info(">>> Email: {}, Username: {}", claims.get("email"), username);
                    UserApp syncedUser = oAuth2UserSyncService.syncKeycloakJwtUser(claims);
                    logger.info(">>> ✅ User synced successfully: {} (id: {})", syncedUser.getUsername(), syncedUser.getId());
                } catch (Exception syncException) {
                    logger.error(">>> ❌ CRITICAL: Failed to sync user to database!");
                    logger.error(">>> Exception type: {}", syncException.getClass().getName());
                    logger.error(">>> Exception message: {}", syncException.getMessage());
                    logger.error(">>> Full stack trace:", syncException);
                    // Continue without sync - will try JWT converter as fallback
                }
            } else {
                logger.warn(">>> ⚠️  OAuth2UserSyncService not available");
            }

            // Load user from local database to get CustomUserDetails with permissions
            try {
                logger.info(">>> Loading user from local database: {}", username);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info(">>> ✅ Keycloak JWT authenticated with local user: {}", username);
                logger.debug(">>> Authorities: {}", userDetails.getAuthorities());
            } catch (Exception e) {
                // User not found locally, use JWT converter as fallback
                logger.warn(">>> ⚠️  User {} not found in local database: {}", username, e.getMessage());
                if (jwtAuthenticationConverter != null) {
                    logger.info(">>> Using JWT authentication converter as fallback...");
                    var authentication = jwtAuthenticationConverter.convert(decodedJwt);
                    if (authentication != null) {
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info(">>> ✅ Keycloak JWT authenticated with JWT converter: {}", decodedJwt.getSubject());
                        logger.debug(">>> Authorities from JWT: {}", authentication.getAuthorities());
                    } else {
                        logger.error(">>> ❌ JWT authentication converter returned null");
                    }
                } else {
                    logger.error(">>> ❌ JWT authentication converter not available");
                }
            }
        } catch (Exception e) {
            logger.error(">>> ❌ Keycloak JWT authentication failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            logger.debug(">>> Full exception:", e);
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
