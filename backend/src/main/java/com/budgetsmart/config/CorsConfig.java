package com.budgetsmart.config;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration pour BudgetSmart
 * 
 * Gère les origins autorisées pour les requêtes cross-origin
 * (Frontend React, n8n, etc.)
 */
@Component
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5678}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    /**
     * Configuration CORS pour l'application
     */
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Ajouter les origins autorisées
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // Ajouter les méthodes HTTP autorisées
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);

        // Autoriser tous les headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Headers exposés au client
        configuration.setExposedHeaders(Arrays.asList(
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CONTENT_TYPE,
            "X-Total-Count", // Pour la pagination
            "X-Page-Number"
        ));

        // Permettre les credentials (cookies, auth headers)
        configuration.setAllowCredentials(allowCredentials);

        // Cache préflight pendant 1 heure
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
