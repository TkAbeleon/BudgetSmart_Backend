package com.budgetsmart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Beans d'infrastructure HTTP partagés
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate partagé pour les appels HTTP externes
     * (webhook n8n, API tierces, etc.)
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
