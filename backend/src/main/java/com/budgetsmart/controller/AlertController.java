package com.budgetsmart.controller;

import com.budgetsmart.dto.BudgetDtos.AlertResponse;
import com.budgetsmart.dto.BudgetDtos.AlertStatsResponse;
import com.budgetsmart.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestion des alertes budgétaires générées automatiquement
 * par les triggers PostgreSQL lors d'un dépassement de budget.
 *
 * Routes :
 *   GET    /api/alerts              → toutes les alertes
 *   GET    /api/alerts/unread       → alertes non lues
 *   GET    /api/alerts/stats        → compteurs total/unread
 *   PUT    /api/alerts/{id}/read    → marquer une alerte comme lue
 *   PUT    /api/alerts/read-all     → marquer toutes comme lues
 *   DELETE /api/alerts/{id}         → supprimer
 */
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<AlertResponse>> list() {
        return ResponseEntity.ok(alertService.findAll());
    }

    @GetMapping("/unread")
    public ResponseEntity<List<AlertResponse>> unread() {
        return ResponseEntity.ok(alertService.findUnread());
    }

    @GetMapping("/stats")
    public ResponseEntity<AlertStatsResponse> stats() {
        return ResponseEntity.ok(alertService.stats());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<AlertResponse> markRead(@PathVariable Integer id) {
        return ResponseEntity.ok(alertService.markAsRead(id));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        alertService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        alertService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
