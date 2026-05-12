package com.budgetsmart.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité Catégorie - alignée avec la table SQL `categories`
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(length = 7)
    private String color = "#6366f1";

    @Column(length = 50)
    private String icon;

    @Column(nullable = false, length = 20)
    private String type; // "EXPENSE" ou "REVENUE"

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
