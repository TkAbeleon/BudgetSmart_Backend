package com.budgetsmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTOs pour les interactions de chat avec n8n
 * 
 * Contient les classes de transfert de données pour les requêtes de chat
 */
public class ChatDtos {

    /**
     * DTO pour la requête de chat
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        
        @NotBlank(message = "La requête est obligatoire")
        @Size(max = 1000, message = "La requête ne doit pas dépasser 1000 caractères")
        private String query;
        
        @NotBlank(message = "L'ID utilisateur est obligatoire")
        private String userId;
        
        private Map<String, Object> context;
        
        private String sessionId;
        
        @Builder.Default
        private String language = "fr";
    }

    /**
     * DTO pour la réponse de chat
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResponse {
        
        private String response;
        
        private java.util.List<String> suggestions;
        
        private String timestamp;
        
        private double processingTime;
        
        private String sessionId;
        
        private Map<String, Object> metadata;
    }

    /**
     * DTO pour le contexte utilisateur
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserContext {
        
        private String currentMonth;
        
        private double totalExpenses;
        
        private double totalRevenues;
        
        private double savingsRate;
        
        private java.util.List<String> recentCategories;
        
        private Map<String, Double> categoryExpenses;
    }

    /**
     * DTO pour les suggestions de budget
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetSuggestion {
        
        private String type; // "expense_reduction", "revenue_increase", "savings_optimization"
        
        private String title;
        
        private String description;
        
        private double potentialSavings;
        
        private String category;
        
        private int priority; // 1-5, 1 étant le plus prioritaire
    }

    /**
     * DTO pour la réponse de statut du service de chat
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatStatusResponse {
        
        private String status; // "active", "inactive", "maintenance"
        
        private String version;
        
        private LocalDateTime lastActivity;
        
        private long totalRequestsToday;
        
        private double averageResponseTime;
        
        private boolean n8nConnected;
        
        private String claudeApiStatus;
    }

    /**
     * DTO pour les métriques de chat
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMetrics {
        
        private String userId;
        
        private LocalDateTime timestamp;
        
        private String queryType; // "budget_advice", "expense_analysis", "savings_tips", "general"
        
        private double responseTime;
        
        private boolean satisfied;
        
        private String feedback;
    }

    /**
     * DTO pour l'historique de chat
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatHistory {
        
        private String sessionId;
        
        private String userId;
        
        private java.util.List<ChatMessage> messages;
        
        private LocalDateTime createdAt;
        
        private LocalDateTime lastActivity;
        
        private boolean active;
    }

    /**
     * DTO pour un message de chat
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        
        private String id;
        
        private String role; // "user", "assistant"
        
        private String content;
        
        private LocalDateTime timestamp;
        
        private Map<String, Object> metadata;
    }
}
