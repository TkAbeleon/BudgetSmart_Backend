package com.budgetsmart.controller;

import com.budgetsmart.dto.AuthDtos.*;
import com.budgetsmart.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentification Controller
 * 
 * Endpoints pour l'inscription, la connexion et la gestion des comptes utilisateurs
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Inscrire un nouvel utilisateur
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Tentative d'inscription avec email: {}", request.getEmail());
        
        AuthResponse response = authService.register(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Connecter un utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Tentative de connexion avec email: {}", request.getEmail());
        
        AuthResponse response = authService.login(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Rafraîchir un token JWT
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Tentative de rafraîchissement de token");
        
        AuthResponse response = authService.refreshToken(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Déconnexion d'un utilisateur
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        log.info("Tentative de déconnexion");
        
        authService.logout();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Déconnexion réussie");
        response.put("status", "success");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir le profil de l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUserProfile() {
        log.debug("Récupération du profil utilisateur");
        
        UserInfo userInfo = authService.getCurrentUserProfile();
        
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Mettre à jour le profil utilisateur
     */
    @PutMapping("/profile")
    public ResponseEntity<UserInfo> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        log.info("Mise à jour du profil utilisateur");
        
        UserInfo updatedUserInfo = authService.updateProfile(request);
        
        return ResponseEntity.ok(updatedUserInfo);
    }

    /**
     * Changer le mot de passe utilisateur
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Changement de mot de passe utilisateur");
        
        authService.changePassword(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Mot de passe changé avec succès");
        response.put("status", "success");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
