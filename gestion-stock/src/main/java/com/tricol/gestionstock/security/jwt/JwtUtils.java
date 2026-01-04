package com.tricol.gestionstock.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public boolean isKeycloakToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
                return headerJson.contains("RS256") || headerJson.contains("RS384") || headerJson.contains("RS512");
            }
        } catch (Exception e) {
            logger.debug("Error checking token type: {}", e.getMessage());
        }
        return false;
    }



    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + refreshTokenExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            if (isKeycloakToken(token)) {
                // Extract payload without verifying signature - only for extracting username/email for syncing
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                    Map<String, Object> claims = objectMapper.readValue(payloadJson, Map.class);
                    String username = null;
                    if (claims.containsKey("preferred_username")) {
                        username = (String) claims.get("preferred_username");
                    } else if (claims.containsKey("email")) {
                        String email = (String) claims.get("email");
                        if (email != null && email.contains("@")) {
                            username = email.split("@")[0];
                        }
                    }
                    if (username == null) {
                        username = (String) claims.get("sub");
                    }
                    return username;
                }
            }

            // Fallback: parse with local HMAC signing key
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return claimsJws.getBody().getSubject();
        } catch (Exception e) {
            logger.error("Failed to extract username from token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token");
        }
    }

    public boolean validateToken(String authToken) {
        try {
            if (isKeycloakToken(authToken)) {
                logger.debug("RS256 token detected, delegating to Keycloak JwtDecoder");
                return false;
            }

            Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}
