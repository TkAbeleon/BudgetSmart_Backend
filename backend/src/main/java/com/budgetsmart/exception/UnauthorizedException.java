package com.budgetsmart.exception;

/**
 * Exception levée quand un utilisateur n'est pas autorisé
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
