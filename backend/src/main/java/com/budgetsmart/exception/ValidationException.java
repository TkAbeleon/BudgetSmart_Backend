package com.budgetsmart.exception;

/**
 * Exception levée quand une validation échoue
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
