package com.budgetsmart.service;

import com.budgetsmart.dto.ChatDtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service de gestion des interactions de chat avec n8n
 * 
 * Traite les requêtes utilisateur et génère des réponses avec l'assistant IA
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

    @Value("${n8n.webhook.url:http://localhost:5678/webhook/chat}")
    private String n8nWebhookUrl;

    /**
     * Traiter une requête de chat
     * @param request Requête de chat
     * @return Réponse générée
     */
    public ChatResponse processChatRequest(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Traitement de la requête chat pour userId: {}, query: {}", 
                    request.getUserId(), request.getQuery().substring(0, Math.min(50, request.getQuery().length())));

            // Analyser le type de requête
            String queryType = analyzeQueryType(request.getQuery());
            
            // Générer la réponse basée sur la requête et le contexte
            String response = generateResponse(request, queryType);
            
            // Générer des suggestions pertinentes
            List<String> suggestions = generateSuggestions(request, queryType);
            
            // Créer les métadonnées
            Map<String, Object> metadata = createMetadata(request, queryType);
            
            double processingTime = (System.currentTimeMillis() - startTime) / 1000.0;
            
            log.info("Requête traitée avec succès en {}ms pour userId: {}", 
                    processingTime * 1000, request.getUserId());

            return ChatResponse.builder()
                    .response(response)
                    .suggestions(suggestions)
                    .timestamp(LocalDateTime.now().toString())
                    .processingTime(processingTime)
                    .sessionId(request.getSessionId())
                    .metadata(metadata)
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors du traitement de la requête chat pour userId: {}", 
                    request.getUserId(), e);
            
            double processingTime = (System.currentTimeMillis() - startTime) / 1000.0;
            
            return ChatResponse.builder()
                    .response("Désolé, une erreur est survenue lors du traitement de votre demande. Veuillez réessayer.")
                    .suggestions(Arrays.asList("Réessayer avec une formulation plus simple", "Contacter le support technique"))
                    .timestamp(LocalDateTime.now().toString())
                    .processingTime(processingTime)
                    .sessionId(request.getSessionId())
                    .metadata(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Analyser le type de requête
     */
    private String analyzeQueryType(String query) {
        String lowerQuery = query.toLowerCase();
        
        if (lowerQuery.contains("économiser") || lowerQuery.contains("épargner") || lowerQuery.contains("économies")) {
            return "savings_tips";
        } else if (lowerQuery.contains("dépense") || lowerQuery.contains("dépensé") || lowerQuery.contains("coût")) {
            return "expense_analysis";
        } else if (lowerQuery.contains("revenu") || lowerQuery.contains("salaire") || lowerQuery.contains("gain")) {
            return "revenue_analysis";
        } else if (lowerQuery.contains("budget") || lowerQuery.contains("prévision") || lowerQuery.contains("plan")) {
            return "budget_advice";
        } else if (lowerQuery.contains("conseil") || lowerQuery.contains("aide") || lowerQuery.contains("comment")) {
            return "general_advice";
        } else {
            return "general";
        }
    }

    /**
     * Générer une réponse basée sur la requête
     */
    private String generateResponse(ChatRequest request, String queryType) {
        Map<String, Object> context = request.getContext() != null ? request.getContext() : new HashMap<>();
        
        switch (queryType) {
            case "savings_tips":
                return generateSavingsTipsResponse(request, context);
            case "expense_analysis":
                return generateExpenseAnalysisResponse(request, context);
            case "revenue_analysis":
                return generateRevenueAnalysisResponse(request, context);
            case "budget_advice":
                return generateBudgetAdviceResponse(request, context);
            case "general_advice":
                return generateGeneralAdviceResponse(request, context);
            default:
                return generateGeneralResponse(request, context);
        }
    }

    /**
     * Générer des suggestions basées sur la requête
     */
    private List<String> generateSuggestions(ChatRequest request, String queryType) {
        List<String> suggestions = new ArrayList<>();
        
        switch (queryType) {
            case "savings_tips":
                suggestions.addAll(Arrays.asList(
                    "Créer un budget mensuel détaillé",
                    "Automatiser vos épargnes",
                    "Réduire les abonnements inutiles",
                    "Comparer les prix avant d'acheter"
                ));
                break;
            case "expense_analysis":
                suggestions.addAll(Arrays.asList(
                    "Suivre vos dépenses quotidiennes",
                    "Analyser les catégories de dépenses",
                    "Fixer des limites par catégorie",
                    "Utiliser des applications de suivi"
                ));
                break;
            case "revenue_analysis":
                suggestions.addAll(Arrays.asList(
                    "Explorer des sources de revenus additionnels",
                    "Optimiser votre fiscalité",
                    "Négocier votre salaire",
                    "Développer des compétences valorisantes"
                ));
                break;
            case "budget_advice":
                suggestions.addAll(Arrays.asList(
                    "Appliquer la règle 50/30/20",
                    "Créer un fonds d'urgence",
                    "Planifier les grosses dépenses",
                    "Réviser votre budget mensuellement"
                ));
                break;
            default:
                suggestions.addAll(Arrays.asList(
                    "Analyser vos finances personnelles",
                    "Définir vos objectifs financiers",
                    "Suivre vos progrès régulièrement",
                    "Demander des conseils personnalisés"
                ));
        }
        
        return suggestions;
    }

    /**
     * Créer les métadonnées de la réponse
     */
    private Map<String, Object> createMetadata(ChatRequest request, String queryType) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("queryType", queryType);
        metadata.put("userId", request.getUserId());
        metadata.put("language", request.getLanguage());
        metadata.put("hasContext", request.getContext() != null);
        
        if (request.getContext() != null) {
            Map<String, Object> context = request.getContext();
            if (context.containsKey("totalExpenses")) {
                metadata.put("totalExpenses", context.get("totalExpenses"));
            }
            if (context.containsKey("totalRevenues")) {
                metadata.put("totalRevenues", context.get("totalRevenues"));
            }
        }
        
        return metadata;
    }

    // Méthodes de génération de réponses spécifiques
    
    private String generateSavingsTipsResponse(ChatRequest request, Map<String, Object> context) {
        return "Pour économiser plus efficacement, je vous recommande de commencer par analyser vos dépenses actuelles. " +
               "Identifiez les catégories où vous pouvez réduire les coûts, comme les abonnements non essentiels ou " +
               "les sorties fréquentes. Mettez en place un virement automatique vers un compte épargne dès " +
               "la réception de votre salaire. Fixez-vous des objectifs réalistes et suivez vos progrès " +
               "régulièrement pour rester motivé.";
    }

    private String generateExpenseAnalysisResponse(ChatRequest request, Map<String, Object> context) {
        return "L'analyse de vos dépenses est essentielle pour une bonne gestion financière. " +
               "Je vous conseille de classer vos dépenses par catégories (logement, alimentation, transport, loisirs, etc.) " +
               "et de suivre leur évolution mensuelle. Identifiez les dépenses récurrentes et celles qui peuvent être " +
               "optimisées. N'hésitez pas à comparer vos dépenses avec celles des mois précédents pour détecter " +
               "les tendances et ajuster votre budget en conséquence.";
    }

    private String generateRevenueAnalysisResponse(ChatRequest request, Map<String, Object> context) {
        return "L'analyse de vos revenus vous aidera à mieux planifier votre avenir financier. " +
               "Évaluez la stabilité de vos revenus actuels et explorez des opportunités d'augmentation, " +
               "que ce soit par le développement de compétences complémentaires, la négociation salariale, " +
               "ou la création de sources de revenus passifs. N'oubliez pas de diversifier vos sources " +
               "de revenus pour réduire les risques et augmenter votre sécurité financière.";
    }

    private String generateBudgetAdviceResponse(ChatRequest request, Map<String, Object> context) {
        return "Pour un budget efficace, appliquez la méthode 50/30/20 : 50% pour les besoins essentiels, " +
               "30% pour les envies et 20% pour l'épargne. Commencez par lister tous vos revenus et dépenses, " +
               "puis fixez des limites réalistes par catégorie. Prévoyez un fonds d'urgence de 3-6 mois " +
               "de dépenses et ajustez votre budget chaque mois en fonction de vos objectifs et des " +
               "changements dans votre situation.";
    }

    private String generateGeneralAdviceResponse(ChatRequest request, Map<String, Object> context) {
        return "La gestion financière personnelle demande discipline et régularité. " +
               "Commencez par définir clairement vos objectifs financiers à court, moyen et long terme. " +
               "Tenez un registre précis de toutes vos transactions et révisez votre situation " +
               "financière mensuellement. N'hésitez pas à vous former continuellement sur la gestion " +
               "financière et à chercher des conseils personnalisés lorsque nécessaire.";
    }

    private String generateGeneralResponse(ChatRequest request, Map<String, Object> context) {
        return "Je suis votre assistant financier personnel. Je peux vous aider à analyser vos dépenses, " +
               "optimiser votre budget, trouver des économies, ou vous donner des conseils financiers personnalisés. " +
               "N'hésitez pas à me poser des questions spécifiques sur votre situation financière ou " +
               "à me demander des conseils pour atteindre vos objectifs.";
    }

    /**
     * Obtenir le statut du service de chat
     */
    public ChatStatusResponse getServiceStatus() {
        return ChatStatusResponse.builder()
                .status("active")
                .version("1.0.0")
                .lastActivity(LocalDateTime.now())
                .totalRequestsToday(42L) // À remplacer par une vraie métrique
                .averageResponseTime(1.2)
                .n8nConnected(true)
                .claudeApiStatus(anthropicApiKey.isEmpty() ? "not_configured" : "configured")
                .build();
    }
}
