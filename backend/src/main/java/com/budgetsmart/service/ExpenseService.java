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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }

    public ExpenseResponse create(ExpenseRequest req) {
        User user = currentUser();
        Expense expense = Expense.builder()
            .user(user)
            .amount(req.getAmount())
            .description(req.getDescription())
            .date(req.getDate() != null ? req.getDate() : LocalDate.now())
            .category(resolveCategory(req.getCategoryId(), user))
            .build();
        return toDto(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> findAll(Pageable pageable) {
        User user = currentUser();
        return expenseRepository.findByUserId(user.getId(), pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long id) {
        User user = currentUser();
        Expense e = expenseRepository.findById(id)
            .filter(ex -> ex.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Dépense non trouvée"));
        return toDto(e);
    }

    public ExpenseResponse update(Long id, ExpenseRequest req) {
        User user = currentUser();
        Expense expense = expenseRepository.findById(id)
            .filter(e -> e.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Dépense non trouvée"));

        if (req.getAmount()      != null) expense.setAmount(req.getAmount());
        if (req.getDescription() != null) expense.setDescription(req.getDescription());
        if (req.getDate()        != null) expense.setDate(req.getDate());
        if (req.getCategoryId()  != null) expense.setCategory(resolveCategory(req.getCategoryId(), user));

        return toDto(expenseRepository.save(expense));
    }

    public void delete(Long id) {
        User user = currentUser();
        Expense expense = expenseRepository.findById(id)
            .filter(e -> e.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Dépense non trouvée"));
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public MonthlySummary monthlySummary(int year, int month) {
        User user = currentUser();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());

        BigDecimal total = expenseRepository.sumByUserIdAndDateBetween(user.getId(), start, end);
        List<Object[]> byCat = expenseRepository.sumByCategoryAndDateBetween(user.getId(), start, end);

        Map<String, BigDecimal> catMap = new LinkedHashMap<>();
        for (Object[] row : byCat) {
            catMap.put((String) row[0], (BigDecimal) row[1]);
        }

        return MonthlySummary.builder()
            .year(year).month(month)
            .totalExpenses(total)
            .totalRevenues(BigDecimal.ZERO) // rempli par le controller si besoin
            .balance(BigDecimal.ZERO)
            .expensesByCategory(catMap)
            .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Category resolveCategory(Long catId, User user) {
        if (catId == null) return null;
        return categoryRepository.findById(catId)
            .filter(c -> c.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée : " + catId));
    }

    public ExpenseResponse toDto(Expense e) {
        CategoryResponse cat = null;
        if (e.getCategory() != null) {
            cat = CategoryResponse.builder()
                .id(e.getCategory().getId())
                .name(e.getCategory().getName())
                .color(e.getCategory().getColor())
                .icon(e.getCategory().getIcon())
                .type(e.getCategory().getType())
                .build();
        }
        return ExpenseResponse.builder()
            .id(e.getId())
            .amount(e.getAmount())
            .description(e.getDescription())
            .date(e.getDate())
            .category(cat)
            .createdAt(e.getCreatedAt())
            .build();
    }
}
