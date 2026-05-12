package com.budgetsmart.service;

import com.budgetsmart.dto.BudgetDtos.AlertResponse;
import com.budgetsmart.dto.ChatDtos.*;
import com.budgetsmart.entity.User;
import com.budgetsmart.exception.ResourceNotFoundException;
import com.budgetsmart.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Chat — Phase 6 : Intégration n8n
 *
 * Flux :
 *   1. Frontend → POST /api/chat/message  (JWT requis)
 *   2. ChatService construit le contexte financier réel de l'utilisateur
 *   3. Appelle le webhook n8n avec le contexte + la question
 *   4. n8n appelle Claude API et renvoie la réponse IA
 *   5. Si n8n indisponible → fallback réponse locale
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository       userRepository;
    private final ExpenseRepository    expenseRepository;
    private final RevenueRepository    revenueRepository;
    private final SavingsRepository    savingsRepository;
    private final AlertRepository      alertRepository;
    private final RestTemplate         restTemplate;

    @Value("${n8n.webhook.url:http://localhost:5678/webhook/chat}")
    private String n8nWebhookUrl;

    @Value("${n8n.api.key:}")
    private String n8nApiKey;

    // ── Endpoint principal : Frontend → Backend → n8n → Claude ───────────────

    /**
     * Traiter un message du frontend.
     * Construit le contexte financier et appelle n8n.
     */
    public ChatResponse processMessage(String question) {
        long t0 = System.currentTimeMillis();

        User user = currentUser();
        log.info("Chat message de userId={} : {}", user.getId(),
                question.substring(0, Math.min(80, question.length())));

        // 1. Construire le contexte financier enrichi
        Map<String, Object> context = buildFinancialContext(user);

        // 2. Construire le payload pour n8n
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId",   user.getId());
        payload.put("userName", user.getFullName());
        payload.put("query",    question);
        payload.put("context",  context);
        payload.put("language", "fr");
        payload.put("timestamp", LocalDateTime.now().toString());

        // 3. Appeler n8n (ou fallback local si n8n indisponible)
        String aiResponse = callN8n(payload);

        double elapsed = (System.currentTimeMillis() - t0) / 1000.0;
        log.info("Chat traité en {}s pour userId={}", elapsed, user.getId());

        return ChatResponse.builder()
                .response(aiResponse)
                .suggestions(buildSuggestions(context))
                .timestamp(LocalDateTime.now().toString())
                .processingTime(elapsed)
                .sessionId(UUID.randomUUID().toString())
                .metadata(Map.of(
                    "userId",          user.getId(),
                    "n8nCalled",       !n8nWebhookUrl.isEmpty(),
                    "contextBuilt",    true,
                    "totalExpenses",   context.getOrDefault("totalExpensesMonth", 0),
                    "unreadAlerts",    context.getOrDefault("unreadAlerts", 0)
                ))
                .build();
    }

    /**
     * Endpoint webhook : n8n → Backend (sans JWT).
     * Utilisé pour que n8n puisse interroger les données utilisateur.
     */
    public ChatResponse processChatRequest(ChatRequest request) {
        long t0 = System.currentTimeMillis();

        // Construire une réponse locale si pas de contexte n8n
        String response = generateFallbackResponse(request.getQuery(), request.getContext());
        double elapsed  = (System.currentTimeMillis() - t0) / 1000.0;

        return ChatResponse.builder()
                .response(response)
                .suggestions(List.of(
                    "Analyser vos dépenses du mois",
                    "Consulter vos alertes budgétaires",
                    "Vérifier vos objectifs d'épargne"
                ))
                .timestamp(LocalDateTime.now().toString())
                .processingTime(elapsed)
                .sessionId(request.getSessionId())
                .metadata(Map.of("source", "local_fallback"))
                .build();
    }

    // ── Construction du contexte financier ────────────────────────────────────

    Map<String, Object> buildFinancialContext(User user) {
        Integer userId = user.getId();
        LocalDate today    = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        LocalDate lastDay  = today.withDayOfMonth(today.lengthOfMonth());

        Map<String, Object> ctx = new LinkedHashMap<>();

        // Dépenses du mois
        BigDecimal totalExp = expenseRepository.sumByUserIdAndDateBetween(userId, firstDay, lastDay);
        ctx.put("totalExpensesMonth", totalExp != null ? totalExp : BigDecimal.ZERO);

        // Dépenses par catégorie
        List<Object[]> byCat = expenseRepository.sumByCategoryAndDateBetween(userId, firstDay, lastDay);
        Map<String, BigDecimal> catMap = new LinkedHashMap<>();
        byCat.forEach(row -> catMap.put((String) row[0], (BigDecimal) row[1]));
        ctx.put("expensesByCategory", catMap);

        // Revenus du mois
        BigDecimal totalRev = revenueRepository.sumByUserIdAndDateBetween(userId, firstDay, lastDay);
        ctx.put("totalRevenuesMonth", totalRev != null ? totalRev : BigDecimal.ZERO);

        // Solde du mois
        BigDecimal exp = (BigDecimal) ctx.get("totalExpensesMonth");
        BigDecimal rev = (BigDecimal) ctx.get("totalRevenuesMonth");
        ctx.put("monthlyBalance", rev.subtract(exp));

        // Objectifs d'épargne en cours
        var savings = savingsRepository.findByUserId(userId);
        ctx.put("savingsGoalsCount",    savings.size());
        ctx.put("savingsGoalsInProgress",
            savings.stream().filter(s -> !s.isCompleted()).count());

        // Alertes non lues (dépassements budget générés par triggers)
        long unread = alertRepository.countByUserIdAndIsReadFalse(userId);
        ctx.put("unreadAlerts", unread);
        if (unread > 0) {
            List<String> alertMessages = alertRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream().limit(3)
                .map(a -> "[" + a.getLevel() + "] " + a.getMessage())
                .collect(Collectors.toList());
            ctx.put("recentAlerts", alertMessages);
        }

        // Budget mensuel de l'utilisateur
        if (user.getMonthlyBudget() != null) {
            ctx.put("monthlyBudget", user.getMonthlyBudget());
            BigDecimal remaining = user.getMonthlyBudget().subtract(exp);
            ctx.put("remainingBudget", remaining);
            ctx.put("budgetUsedPercent",
                exp.doubleValue() / user.getMonthlyBudget().doubleValue() * 100);
        }

        ctx.put("currentMonth", today.getMonth().getDisplayName(
            java.time.format.TextStyle.FULL, java.util.Locale.FRENCH));
        ctx.put("currentYear", today.getYear());

        return ctx;
    }

    // ── Appel n8n ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String callN8n(Map<String, Object> payload) {
        if (n8nWebhookUrl == null || n8nWebhookUrl.isBlank()) {
            log.warn("n8n webhook URL non configurée — fallback local");
            return generateFallbackResponse((String) payload.get("query"),
                    (Map<String, Object>) payload.get("context"));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!n8nApiKey.isBlank()) {
                headers.set("X-n8n-api-key", n8nApiKey);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(n8nWebhookUrl, request, Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Object body = resp.getBody();
                // n8n peut répondre sous différentes formes
                if (body instanceof Map<?,?> map) {
                    if (map.containsKey("response"))  return map.get("response").toString();
                    if (map.containsKey("text"))      return map.get("text").toString();
                    if (map.containsKey("message"))   return map.get("message").toString();
                    if (map.containsKey("output"))    return map.get("output").toString();
                }
                return resp.getBody().toString();
            }
            log.warn("n8n a répondu avec le code {}", resp.getStatusCode());

        } catch (RestClientException e) {
            log.warn("n8n indisponible ({}), utilisation du fallback local", e.getMessage());
        }

        return generateFallbackResponse((String) payload.get("query"),
                (Map<String, Object>) payload.get("context"));
    }

    // ── Fallback réponse locale (sans IA) ─────────────────────────────────────

    private String generateFallbackResponse(String query, Map<String, Object> ctx) {
        if (query == null) return "Bonjour ! Comment puis-je vous aider ?";
        String q = query.toLowerCase();

        if (ctx != null) {
            BigDecimal exp = toBigDecimal(ctx.get("totalExpensesMonth"));
            BigDecimal rev = toBigDecimal(ctx.get("totalRevenuesMonth"));
            Object alerts  = ctx.get("unreadAlerts");

            if (q.contains("dépense") || q.contains("combien")) {
                return String.format(
                    "Ce mois-ci vous avez dépensé %s MGA. Vos revenus s'élèvent à %s MGA, " +
                    "soit un solde de %s MGA.",
                    exp, rev, rev.subtract(exp));
            }
            if (q.contains("alerte") || q.contains("budget")) {
                long n = alerts instanceof Number ? ((Number) alerts).longValue() : 0L;
                return n > 0
                    ? String.format("Vous avez %d alerte(s) de dépassement de budget non lue(s). " +
                        "Consultez /api/alerts/unread pour les détails.", n)
                    : "Aucune alerte budgétaire active. Votre budget est sous contrôle !";
            }
            if (q.contains("épargne") || q.contains("objectif")) {
                Object goals = ctx.get("savingsGoalsInProgress");
                return String.format("Vous avez %s objectif(s) d'épargne en cours. " +
                    "Continuez vos efforts !", goals);
            }
        }

        return "Je suis votre assistant budgétaire BudgetSmart. " +
               "Posez-moi des questions sur vos dépenses, revenus, alertes ou objectifs d'épargne. " +
               "Pour une analyse IA complète, connectez n8n via la variable N8N_WEBHOOK_URL.";
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n)     return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }

    // ── Suggestions contextuelles ─────────────────────────────────────────────

    private List<String> buildSuggestions(Map<String, Object> ctx) {
        List<String> suggestions = new ArrayList<>();
        long unread = ctx.containsKey("unreadAlerts")
            ? ((Number) ctx.get("unreadAlerts")).longValue() : 0L;

        if (unread > 0)
            suggestions.add("Consulter mes " + unread + " alerte(s) non lue(s)");

        Object remaining = ctx.get("remainingBudget");
        if (remaining instanceof BigDecimal bd && bd.compareTo(BigDecimal.ZERO) < 0)
            suggestions.add("Budget dépassé — réduire les dépenses non essentielles");

        suggestions.add("Voir le résumé mensuel complet");
        suggestions.add("Ajouter un objectif d'épargne");
        suggestions.add("Analyser les dépenses par catégorie");

        return suggestions;
    }

    // ── Statut du service ─────────────────────────────────────────────────────

    public ChatStatusResponse getServiceStatus() {
        boolean n8nOk = false;
        if (!n8nWebhookUrl.isBlank()) {
            try {
                restTemplate.headForHeaders(n8nWebhookUrl);
                n8nOk = true;
            } catch (Exception ignored) {}
        }
        return ChatStatusResponse.builder()
                .status("active")
                .version("2.0.0")
                .lastActivity(LocalDateTime.now())
                .totalRequestsToday(0L)
                .averageResponseTime(0.5)
                .n8nConnected(n8nOk)
                .claudeApiStatus(n8nOk ? "via_n8n" : "not_connected")
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }
}
