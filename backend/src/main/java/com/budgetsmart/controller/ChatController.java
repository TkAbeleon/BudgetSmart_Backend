package com.budgetsmart.controller;

import com.budgetsmart.dto.ChatDtos.*;
import com.budgetsmart.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour les interactions avec n8n et l'assistant IA
 * 
 * Gère les webhooks n8n pour le chatbot de budget
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Value("${n8n.api.key}")
    private String n8nApiKey;

    /**
     * Endpoint webhook pour n8n
     * Reçoit les requêtes de n8n et les traite avec l'assistant IA
     */
    @PostMapping("/webhook/n8n")
    public ResponseEntity<ChatResponse> handleN8nWebhook(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(value = "X-n8n-api-key", required = false) String apiKey) {
        
        log.info("Requête webhook n8n reçue pour userId: {}", request.getUserId());
        
        // Valider la clé API n8n si configurée
        if (n8nApiKey != null && !n8nApiKey.isEmpty() && !n8nApiKey.equals(apiKey)) {
            log.warn("Tentative d'accès au webhook n8n avec une clé API invalide");
            return ResponseEntity.status(401)
                    .build();
        }

        // Traiter la requête
        ChatResponse response = chatService.processChatRequest(request);
        
        log.info("Réponse générée pour userId: {}, temps de traitement: {}ms", 
                request.getUserId(), response.getProcessingTime());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de test pour le webhook n8n (sans authentification)
     */
    @PostMapping("/webhook/test")
    public ResponseEntity<ChatResponse> testWebhook(@Valid @RequestBody ChatRequest request) {
        log.info("Test webhook pour userId: {}", request.getUserId());
        
        ChatResponse response = chatService.processChatRequest(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour vérifier le statut du service de chat
     */
    @GetMapping("/status")
    public ResponseEntity<ChatStatusResponse> getChatStatus() {
        ChatStatusResponse status = chatService.getServiceStatus();
        
        return ResponseEntity.ok(status);
    }
}
