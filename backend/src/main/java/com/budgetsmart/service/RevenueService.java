package com.budgetsmart.service;

import com.budgetsmart.dto.BudgetDtos.*;
import com.budgetsmart.entity.*;
import com.budgetsmart.exception.ResourceNotFoundException;
import com.budgetsmart.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RevenueService {

    private final RevenueRepository revenueRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }

    public RevenueResponse create(RevenueRequest req) {
        User user = currentUser();
        Revenue revenue = Revenue.builder()
            .user(user)
            .amount(req.getAmount())
            .description(req.getDescription())
            .date(req.getDate() != null ? req.getDate() : LocalDate.now())
            .category(resolveCategory(req.getCategoryId(), user))
            .build();
        return toDto(revenueRepository.save(revenue));
    }

    @Transactional(readOnly = true)
    public Page<RevenueResponse> findAll(Pageable pageable) {
        User user = currentUser();
        return revenueRepository.findByUserId(user.getId(), pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public RevenueResponse findById(Long id) {
        User user = currentUser();
        Revenue r = revenueRepository.findById(id)
            .filter(rv -> rv.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Revenu non trouvé"));
        return toDto(r);
    }

    public RevenueResponse update(Long id, RevenueRequest req) {
        User user = currentUser();
        Revenue revenue = revenueRepository.findById(id)
            .filter(r -> r.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Revenu non trouvé"));

        if (req.getAmount()      != null) revenue.setAmount(req.getAmount());
        if (req.getDescription() != null) revenue.setDescription(req.getDescription());
        if (req.getDate()        != null) revenue.setDate(req.getDate());
        if (req.getCategoryId()  != null) revenue.setCategory(resolveCategory(req.getCategoryId(), user));

        return toDto(revenueRepository.save(revenue));
    }

    public void delete(Long id) {
        User user = currentUser();
        Revenue revenue = revenueRepository.findById(id)
            .filter(r -> r.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Revenu non trouvé"));
        revenueRepository.delete(revenue);
    }

    @Transactional(readOnly = true)
    public BigDecimal totalForPeriod(Long userId, LocalDate start, LocalDate end) {
        return revenueRepository.sumByUserIdAndDateBetween(userId, start, end);
    }

    private Category resolveCategory(Long catId, User user) {
        if (catId == null) return null;
        return categoryRepository.findById(catId)
            .filter(c -> c.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée : " + catId));
    }

    public RevenueResponse toDto(Revenue r) {
        CategoryResponse cat = null;
        if (r.getCategory() != null) {
            cat = CategoryResponse.builder()
                .id(r.getCategory().getId())
                .name(r.getCategory().getName())
                .color(r.getCategory().getColor())
                .icon(r.getCategory().getIcon())
                .type(r.getCategory().getType())
                .build();
        }
        return RevenueResponse.builder()
            .id(r.getId())
            .amount(r.getAmount())
            .description(r.getDescription())
            .date(r.getDate())
            .category(cat)
            .createdAt(r.getCreatedAt())
            .build();
    }
}
