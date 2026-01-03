package com.tricol.gestionstock.security;

import com.tricol.gestionstock.security.jwt.JwtUtils;
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

@Component
public class HybridJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HybridJwtAuthenticationFilter.class);

    private final JwtUtils customJwtUtils;
    private final UserDetailsService userDetailsService;
    private JwtDecoder jwtDecoder;
    private JwtAuthenticationConverter jwtAuthenticationConverter;

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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                if (customJwtUtils.validateToken(jwt)) {
                    authenticateWithCustomJwt(jwt, request);
                } else {
                    authenticateWithKeycloakJwt(jwt);
                }
            }
        } catch (Exception e) {
            logger.debug("Authentication failed", e);
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

    private void authenticateWithKeycloakJwt(String jwt) {
        // Skip Keycloak authentication if JwtDecoder is not available
        if (jwtDecoder == null || jwtAuthenticationConverter == null) {
            logger.debug("Keycloak JWT decoder not available, skipping Keycloak authentication");
            return;
        }

        try {
            Jwt decodedJwt = jwtDecoder.decode(jwt);
            var authentication = jwtAuthenticationConverter.convert(decodedJwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("✅ Keycloak JWT authenticated: {}", decodedJwt.getSubject());
        } catch (Exception e) {
            logger.debug("Keycloak JWT failed", e);
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
