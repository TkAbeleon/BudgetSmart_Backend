package com.budgetsmart.service;

import com.budgetsmart.dto.BudgetDtos.AlertResponse;
import com.budgetsmart.dto.BudgetDtos.AlertStatsResponse;
import com.budgetsmart.entity.Alert;
import com.budgetsmart.entity.User;
import com.budgetsmart.exception.ResourceNotFoundException;
import com.budgetsmart.repository.AlertRepository;
import com.budgetsmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }

    /** Toutes les alertes de l'utilisateur connecté (lues + non lues) */
    @Transactional(readOnly = true)
    public List<AlertResponse> findAll() {
        return alertRepository
            .findByUserIdOrderByCreatedAtDesc(currentUser().getId())
            .stream().map(this::toDto).collect(Collectors.toList());
    }

    /** Alertes non lues uniquement */
    @Transactional(readOnly = true)
    public List<AlertResponse> findUnread() {
        return alertRepository
            .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(currentUser().getId())
            .stream().map(this::toDto).collect(Collectors.toList());
    }

    /** Statistiques : total + non lues */
    @Transactional(readOnly = true)
    public AlertStatsResponse stats() {
        Integer userId = currentUser().getId();
        long total  = alertRepository.findByUserIdOrderByCreatedAtDesc(userId).size();
        long unread = alertRepository.countByUserIdAndIsReadFalse(userId);
        return AlertStatsResponse.builder().total(total).unread(unread).build();
    }

    /** Marquer une alerte comme lue */
    public AlertResponse markAsRead(Integer id) {
        Alert alert = getOwnedAlert(id);
        alert.setRead(true);
        return toDto(alertRepository.save(alert));
    }

    /** Marquer toutes les alertes comme lues */
    public void markAllAsRead() {
        List<Alert> unread = alertRepository
            .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(currentUser().getId());
        unread.forEach(a -> a.setRead(true));
        alertRepository.saveAll(unread);
        log.info("Marqué {} alertes comme lues pour userId={}", unread.size(), currentUser().getId());
    }

    /** Supprimer une alerte */
    public void delete(Integer id) {
        alertRepository.delete(getOwnedAlert(id));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Alert getOwnedAlert(Integer id) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alerte non trouvée : " + id));
        if (!alert.getUser().getId().equals(currentUser().getId())) {
            throw new ResourceNotFoundException("Alerte non trouvée : " + id);
        }
        return alert;
    }

    public AlertResponse toDto(Alert a) {
        return AlertResponse.builder()
            .id(a.getId())
            .level(a.getLevel())
            .message(a.getMessage())
            .read(a.isRead())
            .createdAt(a.getCreatedAt())
            .build();
    }
}
