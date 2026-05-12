package com.budgetsmart.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class BudgetDtos {

    // ── Expense ──────────────────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExpenseRequest {
        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être positif")
        private BigDecimal amount;

        @Size(max = 500)
        private String description;

        private Long categoryId;

        private LocalDate date;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExpenseResponse {
        private Long id;
        private BigDecimal amount;
        private String description;
        private LocalDate date;
        private CategoryResponse category;
        private LocalDateTime createdAt;
    }

    // ── Revenue ──────────────────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RevenueRequest {
        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être positif")
        private BigDecimal amount;

        @Size(max = 500)
        private String description;

        private Long categoryId;

        private LocalDate date;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RevenueResponse {
        private Long id;
        private BigDecimal amount;
        private String description;
        private LocalDate date;
        private CategoryResponse category;
        private LocalDateTime createdAt;
    }

    // ── Category ─────────────────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategoryRequest {
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        private String color;
        private String icon;

        @NotBlank(message = "Le type est obligatoire (EXPENSE ou REVENUE)")
        @Pattern(regexp = "^(EXPENSE|REVENUE)$", message = "Type invalide")
        private String type;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String description;
        private String color;
        private String icon;
        private String type;
    }

    // ── Budget ────────────────────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BudgetRequest {
        @NotNull(message = "Le montant limite est obligatoire")
        @Positive
        private BigDecimal limitAmount;

        private Long categoryId;

        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BudgetResponse {
        private Long id;
        private BigDecimal limitAmount;
        private BigDecimal spent;
        private BigDecimal remaining;
        private double percentUsed;
        private CategoryResponse category;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean active;
    }

    // ── Savings ───────────────────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SavingsRequest {
        @NotBlank(message = "Le nom de l'objectif est obligatoire")
        @Size(max = 200)
        private String goalName;

        @NotNull(message = "Le montant cible est obligatoire")
        @Positive
        private BigDecimal targetAmount;

        private LocalDate targetDate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SavingsResponse {
        private Long id;
        private String goalName;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private double progressPercent;
        private BigDecimal remaining;
        private LocalDate targetDate;
        private boolean completed;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SavingsAddRequest {
        @NotNull(message = "Le montant est obligatoire")
        @Positive
        private BigDecimal amount;
    }

    // ── Summary ───────────────────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MonthlySummary {
        private int year;
        private int month;
        private BigDecimal totalRevenues;
        private BigDecimal totalExpenses;
        private BigDecimal balance;
        private Map<String, BigDecimal> expensesByCategory;
    }
}
