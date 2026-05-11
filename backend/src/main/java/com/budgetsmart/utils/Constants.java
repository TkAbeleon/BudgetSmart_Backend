package com.budgetsmart.utils;

/**
 * Constantes globales de l'application BudgetSmart
 */
public class Constants {

    // ==========================================
    // Application Constants
    // ==========================================
    public static final String APP_NAME = "BudgetSmart";
    public static final String API_VERSION = "v1";
    public static final String API_BASE_PATH = "/api";

    // ==========================================
    // Error Messages
    // ==========================================
    public static final String ERROR_USER_NOT_FOUND = "Utilisateur non trouvé";
    public static final String ERROR_UNAUTHORIZED = "Non autorisé";
    public static final String ERROR_INVALID_TOKEN = "Token invalide ou expiré";
    public static final String ERROR_EMAIL_ALREADY_EXISTS = "Cet email est déjà utilisé";
    public static final String ERROR_INVALID_CREDENTIALS = "Email ou mot de passe incorrect";
    public static final String ERROR_BUDGET_EXCEEDED = "Budget dépassé";
    public static final String ERROR_INVALID_AMOUNT = "Le montant doit être positif";
    public static final String ERROR_DATABASE = "Erreur de base de données";

    // ==========================================
    // Success Messages
    // ==========================================
    public static final String SUCCESS_REGISTRATION = "Inscription réussie";
    public static final String SUCCESS_LOGIN = "Connexion réussie";
    public static final String SUCCESS_CREATED = "Ressource créée avec succès";
    public static final String SUCCESS_UPDATED = "Ressource mise à jour avec succès";
    public static final String SUCCESS_DELETED = "Ressource supprimée avec succès";

    // ==========================================
    // JWT Constants
    // ==========================================
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final long DEFAULT_TOKEN_EXPIRATION = 86400000L; // 24 heures

    // ==========================================
    // HTTP Status Codes
    // ==========================================
    public static final int STATUS_OK = 200;
    public static final int STATUS_CREATED = 201;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_CONFLICT = 409;
    public static final int STATUS_INTERNAL_ERROR = 500;

    // ==========================================
    // Validation Constants
    // ==========================================
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    // ==========================================
    // Regex Patterns
    // ==========================================
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PHONE_PATTERN = "^[+]?[0-9]{7,15}$";

    // ==========================================
    // Date Format Constants
    // ==========================================
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String TIMEZONE = "UTC";

    // ==========================================
    // Category Types
    // ==========================================
    public static final String CATEGORY_TYPE_REVENUE = "REVENUE";
    public static final String CATEGORY_TYPE_EXPENSE = "EXPENSE";

    // ==========================================
    // Alert Types
    // ==========================================
    public static final String ALERT_TYPE_INFO = "INFO";
    public static final String ALERT_TYPE_WARNING = "WARNING";
    public static final String ALERT_TYPE_CRITICAL = "CRITICAL";

    // ==========================================
    // Pagination Constants
    // ==========================================
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // ==========================================
    // N8N Constants
    // ==========================================
    public static final String N8N_API_KEY_HEADER = "X-N8N-API-KEY";
    public static final String N8N_WEBHOOK_HEADER = "X-Webhook-ID";

    // ==========================================
    // Cache Keys
    // ==========================================
    public static final String CACHE_USER_PREFIX = "user:";
    public static final String CACHE_BUDGET_PREFIX = "budget:";
    public static final long CACHE_DURATION_HOURS = 1;
}
