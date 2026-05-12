package com.budgetsmart.dto;

import jakarta.validation.constraints.*;
import lombok.*;


import java.time.LocalDateTime;

public class AuthDtos {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        private String email;

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, max = 100)
        private String password;

        @Size(max = 100)
        private String firstName;

        @Size(max = 100)
        private String lastName;

        @Size(max = 20)
        private String phone;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "L'email est obligatoire")
        @Email
        private String email;

        @NotBlank(message = "Le mot de passe est obligatoire")
        private String password;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthResponse {
        private String message;
        private String token;
        private String refreshToken;
        private Long expiresIn;
        private UserInfo user;
        private String status;
        private Long timestamp;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserInfo {
        private Integer id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private java.math.BigDecimal monthlyBudget;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RefreshTokenRequest {
        @NotBlank(message = "Le refresh token est obligatoire")
        private String refreshToken;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateProfileRequest {
        @Size(max = 100)
        private String firstName;

        @Size(max = 100)
        private String lastName;

        @Size(max = 20)
        private String phone;

        @Email(message = "Format d'email invalide")
        private String email;

        private java.math.BigDecimal monthlyBudget;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank(message = "L'ancien mot de passe est obligatoire")
        private String oldPassword;

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 6, max = 100)
        private String newPassword;
    }
}
