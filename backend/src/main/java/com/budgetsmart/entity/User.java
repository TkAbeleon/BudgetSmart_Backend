package com.budgetsmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité User — Hibernate crée/gère la table `users` automatiquement via ddl-auto=update.
 */
@Entity
@Table(name = "users",
    uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    /** Colonne de compatibilité — computed ou stockée séparément */
    @Column(length = 200)
    private String name;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Builder.Default
    @Column(name = "monthly_budget", precision = 12, scale = 2)
    private BigDecimal monthlyBudget = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        // Synchroniser name depuis firstName+lastName
        this.name = buildFullName();
    }

    @PreUpdate
    protected void onUpdate() {
        this.name = buildFullName();
    }

    /** Retourne le nom complet (firstName + lastName ou email comme fallback) */
    public String getFullName() {
        String full = buildFullName();
        return full != null && !full.isBlank() ? full : email;
    }

    private String buildFullName() {
        if (firstName != null && lastName != null) return firstName + " " + lastName;
        if (firstName != null) return firstName;
        if (lastName  != null) return lastName;
        return name;
    }
}
