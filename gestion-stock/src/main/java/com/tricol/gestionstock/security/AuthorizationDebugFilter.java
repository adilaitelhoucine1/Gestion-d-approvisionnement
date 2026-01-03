package com.tricol.gestionstock.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class AuthorizationDebugFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationDebugFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        // ═══════════════════════════════════════════════════════════════
        // BREAKPOINT 1: Check if Authorization header is present
        // ═══════════════════════════════════════════════════════════════
        boolean hasAuthHeader = authHeader != null && authHeader.startsWith("Bearer ");
        logger.debug("══════════════════════════════════════════════════════════════");
        logger.debug("REQUEST: {} {}", method, requestURI);
        logger.debug("Authorization Header Present: {}", hasAuthHeader);

        // ═══════════════════════════════════════════════════════════════
        // BREAKPOINT 2: Check SecurityContext BEFORE authorization
        // ═══════════════════════════════════════════════════════════════
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.debug("SecurityContext: EMPTY (No authentication)");
            logger.debug("→ This will result in 401 Unauthorized for protected endpoints");
        } else {
            logger.debug("SecurityContext Authentication:");
            logger.debug("  → Principal: {}", authentication.getPrincipal());
            logger.debug("  → Is Authenticated: {}", authentication.isAuthenticated());
            logger.debug("  → Authentication Type: {}", authentication.getClass().getSimpleName());

            // ═══════════════════════════════════════════════════════════════
            // BREAKPOINT 3: Check user authorities (roles/permissions)
            // ═══════════════════════════════════════════════════════════════
            String authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", "));

            logger.debug("  → Authorities: [{}]", authorities);

            // Check for common roles
            boolean hasRoleUser = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
            boolean hasRoleAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            logger.debug("  → Has ROLE_USER: {}", hasRoleUser);
            logger.debug("  → Has ROLE_ADMIN: {}", hasRoleAdmin);
        }

        logger.debug("══════════════════════════════════════════════════════════════");

        // ═══════════════════════════════════════════════════════════════
        // BREAKPOINT 4: Continue filter chain - authorization happens next
        // If 401/403 occurs, it will be thrown by AuthorizationFilter
        // ═══════════════════════════════════════════════════════════════
        try {
            filterChain.doFilter(request, response);

            // ═══════════════════════════════════════════════════════════════
            // BREAKPOINT 5: Check response status after authorization
            // ═══════════════════════════════════════════════════════════════
            logger.debug("RESPONSE STATUS: {} for {} {}", response.getStatus(), method, requestURI);

        } catch (Exception e) {
            logger.debug("EXCEPTION during filter chain: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip filtering for static resources
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/favicon");
    }
}

