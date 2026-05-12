package com.budgetsmart;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * BudgetSmart Application Main Class
 * 
 * Système de Gestion de Budget Intelligent
 * - Suivi des revenus et dépenses
 * - Module d'épargne avec objectifs
 * - Alertes automatiques (triggers PostgreSQL)
 * - Assistant IA conversationnel (via n8n)
 */
@SpringBootApplication
@EnableScheduling
public class BudgetSmartApplication {

    public static void main(String[] args) {
        // Load environment variables from .env file
        Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
        
        // Set system properties for Spring Boot
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        SpringApplication.run(BudgetSmartApplication.class, args);
    }
}
