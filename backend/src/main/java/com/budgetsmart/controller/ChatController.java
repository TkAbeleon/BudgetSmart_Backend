package com.budgetsmart.controller;

import com.budgetsmart.dto.ChatDtos.*;
import com.budgetsmart.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Gestion du chat budgétaire IA via n8n + Claude.
 *
 * Routes :
 *   POST /api/chat/message          → Frontend (JWT requis) → n8n → Claude
 *   POST /api/chat/webhook/n8n      → Webhook entrant n8n (sans JWT)
 *   POST /api/chat/webhook/test     → Test local (sans JWT)
 *   GET  /api/chat/status           → Statut du service
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Value("${n8n.api.key:}")
    private String n8nApiKey;

    // ── Endpoint Frontend (JWT requis) ────────────────────────────────────────

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody MessageRequest req) {
        log.info("Message reçu du frontend : {}", req.getQuestion().substring(0, Math.min(60, req.getQuestion().length())));
        ChatResponse response = chatService.processMessage(req.getQuestion());
        return ResponseEntity.ok(response);
    }

    // ── Webhook n8n entrant (sans JWT) ────────────────────────────────────────

    @PostMapping("/webhook/n8n")
    public ResponseEntity<ChatResponse> handleN8nWebhook(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(value = "X-n8n-api-key", required = false) String apiKey) {

        log.info("Webhook n8n reçu pour userId={}", request.getUserId());

        // Valider la clé API n8n si configurée
        if (!n8nApiKey.isBlank() && !n8nApiKey.equals(apiKey)) {
            log.warn("Clé API n8n invalide — accès refusé");
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(chatService.processChatRequest(request));
    }

    // ── Endpoint de test (sans JWT) ───────────────────────────────────────────

    @PostMapping("/webhook/test")
    public ResponseEntity<ChatResponse> testWebhook(@Valid @RequestBody ChatRequest request) {
        log.info("Test webhook pour userId={}", request.getUserId());
        return ResponseEntity.ok(chatService.processChatRequest(request));
    }

    // ── Statut du service ─────────────────────────────────────────────────────

    @GetMapping("/status")
    public ResponseEntity<ChatStatusResponse> status() {
        return ResponseEntity.ok(chatService.getServiceStatus());
    }

    // ── DTO inline pour le endpoint /message ─────────────────────────────────

    @Data
    public static class MessageRequest {
        @NotBlank(message = "La question est obligatoire")
        @Size(max = 2000, message = "La question ne doit pas dépasser 2000 caractères")
        private String question;
    }
}
