package com.budgetsmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité Savings — alignée avec la table SQL locale `savings_goals`
 * Colonnes : id (int), user_id, name, target_amount, current_amount, deadline, created_at
 */
@Entity
@Table(name = "savings_goals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Savings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Colonne réelle : name (pas goal_name) */
    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "target_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    @Builder.Default
    @Column(name = "current_amount", precision = 12, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    /** Colonne en base : target_date */
    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (currentAmount == null) currentAmount = BigDecimal.ZERO;
    }

    public boolean isCompleted() {
        return currentAmount != null && currentAmount.compareTo(targetAmount) >= 0;
    }
}
