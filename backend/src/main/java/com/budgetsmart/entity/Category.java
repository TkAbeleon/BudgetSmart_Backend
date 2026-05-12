package com.budgetsmart.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité Category — alignée avec la table SQL locale `categories`
 * Colonnes : id (int), user_id, name, type, color
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
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String type; // "EXPENSE" ou "REVENUE"

    @Builder.Default
    @Column(length = 7)
    private String color = "#6366f1";
}
