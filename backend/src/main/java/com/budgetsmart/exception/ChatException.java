package com.budgetsmart.exception;

/**
 * Exception levée pour les erreurs d'intégration Chat/n8n
 */
public class ChatException extends RuntimeException {
    public ChatException(String message) {
        super(message);
    }

    public ChatException(String message, Throwable cause) {
        super(message, cause);
    }
}
