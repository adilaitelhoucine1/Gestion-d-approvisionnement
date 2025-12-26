package com.tricol.gestionstock.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuration CORS pour limiter les origines approuvées
 * Cette configuration centralisée remplace les annotations @CrossOrigin dispersées
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String[] allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String[] allowedHeaders;

    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age}")
    private long maxAge;

    /**
     * Configure la source de configuration CORS
     * @return CorsConfigurationSource configuré avec les origines approuvées
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origines approuvées (configurables via application.properties)
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));

        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));

        // En-têtes autorisés
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));

        // En-têtes exposés au client
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Total-Count"
        ));

        // Autoriser l'envoi de credentials (cookies, authorization headers)
        configuration.setAllowCredentials(allowCredentials);

        // Durée de mise en cache de la réponse preflight (en secondes)
        configuration.setMaxAge(maxAge);

        // Appliquer cette configuration à tous les chemins
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

