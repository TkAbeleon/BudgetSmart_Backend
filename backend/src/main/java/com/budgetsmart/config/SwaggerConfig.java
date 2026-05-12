package com.budgetsmart.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration Swagger / OpenAPI 3
 * Accessible sur : http://localhost:8080/api/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String port;

    @Bean
    public OpenAPI budgetSmartOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("BudgetSmart API")
                .description("""
                    API REST du système de gestion de budget intelligent.
                    
                    ## Fonctionnalités
                    - 🔐 **Authentification JWT** (register, login, refresh, logout)
                    - 💰 **Dépenses & Revenus** CRUD complet avec pagination
                    - 📂 **Catégories** personnalisées (EXPENSE / REVENUE)
                    - 🎯 **Épargne** — objectifs avec suivi de progression
                    - 🔔 **Alertes** — dépassements de budget détectés par triggers PostgreSQL
                    - 🤖 **Chat IA** — assistant budgétaire via n8n + Claude API
                    
                    ## Authentification
                    Utilisez le bouton **Authorize** 🔒 et saisissez votre token JWT sous la forme :
                    `Bearer <votre_token>`
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("BudgetSmart Team")
                    .email("contact@budgetsmart.mg"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + port + "/api")
                    .description("Serveur local de développement")
            ))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Saisissez votre token JWT (sans le préfixe 'Bearer')")));
    }
}
