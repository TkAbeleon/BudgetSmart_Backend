package com.budgetsmart.service;

import com.budgetsmart.dto.BudgetDtos.CategoryResponse;
import com.budgetsmart.dto.BudgetDtos.RevenueRequest;
import com.budgetsmart.dto.BudgetDtos.RevenueResponse;
import com.budgetsmart.entity.Category;
import com.budgetsmart.entity.Revenue;
import com.budgetsmart.entity.User;
import com.budgetsmart.exception.ResourceNotFoundException;
import com.budgetsmart.repository.CategoryRepository;
import com.budgetsmart.repository.RevenueRepository;
import com.budgetsmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

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
        Revenue r = Revenue.builder()
            .user(user)
            .amount(req.getAmount())
            .description(req.getDescription())
            .date(req.getDate() != null ? req.getDate() : LocalDate.now())
            .category(resolveCategory(req.getCategoryId(), user))
            .build();
        return toDto(revenueRepository.save(r));
    }

    @Transactional(readOnly = true)
    public Page<RevenueResponse> findAll(Pageable pageable) {
        return revenueRepository.findByUserId(currentUser().getId(), pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public RevenueResponse findById(Integer id) {
        User user = currentUser();
        return toDto(revenueRepository.findById(id)
            .filter(r -> r.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Revenu non trouvé")));
    }

    public RevenueResponse update(Integer id, RevenueRequest req) {
        User user = currentUser();
        Revenue r = revenueRepository.findById(id)
            .filter(rv -> rv.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Revenu non trouvé"));
        if (req.getAmount()      != null) r.setAmount(req.getAmount());
        if (req.getDescription() != null) r.setDescription(req.getDescription());
        if (req.getDate()        != null) r.setDate(req.getDate());
        if (req.getCategoryId()  != null) r.setCategory(resolveCategory(req.getCategoryId(), user));
        return toDto(revenueRepository.save(r));
    }

    public void delete(Integer id) {
        User user = currentUser();
        Revenue r = revenueRepository.findById(id)
            .filter(rv -> rv.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Revenu non trouvé"));
        revenueRepository.delete(r);
    }

    public BigDecimal totalForPeriod(Integer userId, LocalDate start, LocalDate end) {
        return revenueRepository.sumByUserIdAndDateBetween(userId, start, end);
    }

    private Category resolveCategory(Integer catId, User user) {
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
