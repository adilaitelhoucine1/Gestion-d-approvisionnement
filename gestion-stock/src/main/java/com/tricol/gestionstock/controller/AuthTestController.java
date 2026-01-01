package com.tricol.gestionstock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class AuthTestController {

    @GetMapping("/auth-type")
    public ResponseEntity<?> getAuthType() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return ResponseEntity.ok(Map.of("authType", "NONE", "authenticated", false));
        }

        String authType;
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof OAuth2User) {
            authType = "OAUTH2";
        } else {
            authType = "LOCAL";
        }

        return ResponseEntity.ok(Map.of(
            "authType", authType,
            "authenticated", authentication.isAuthenticated(),
            "principal", principal.toString(),
            "authorities", authentication.getAuthorities()
        ));
    }

    @GetMapping("/whoami")
    public ResponseEntity<?> whoami() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        return ResponseEntity.ok(Map.of(
            "name", authentication.getName(),
            "authorities", authentication.getAuthorities(),
            "details", authentication.getDetails()
        ));
    }
}
