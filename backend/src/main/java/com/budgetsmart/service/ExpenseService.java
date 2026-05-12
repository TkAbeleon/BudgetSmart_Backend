package com.budgetsmart.service;

import com.budgetsmart.dto.BudgetDtos.CategoryResponse;
import com.budgetsmart.dto.BudgetDtos.ExpenseRequest;
import com.budgetsmart.dto.BudgetDtos.ExpenseResponse;
import com.budgetsmart.dto.BudgetDtos.MonthlySummary;
import com.budgetsmart.entity.Category;
import com.budgetsmart.entity.Expense;
import com.budgetsmart.entity.User;
import com.budgetsmart.exception.ResourceNotFoundException;
import com.budgetsmart.repository.CategoryRepository;
import com.budgetsmart.repository.ExpenseRepository;
import com.budgetsmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Expense e = Expense.builder()
            .user(user)
            .amount(req.getAmount())
            .description(req.getDescription())
            .date(req.getDate() != null ? req.getDate() : LocalDate.now())
            .category(resolveCategory(req.getCategoryId(), user))
            .build();
        return toDto(expenseRepository.save(e));
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> findAll(Pageable pageable) {
        return expenseRepository.findByUserId(currentUser().getId(), pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse findById(Integer id) {
        User user = currentUser();
        return toDto(expenseRepository.findById(id)
            .filter(e -> e.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Dépense non trouvée")));
    }

    public ExpenseResponse update(Integer id, ExpenseRequest req) {
        User user = currentUser();
        Expense e = expenseRepository.findById(id)
            .filter(ex -> ex.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Dépense non trouvée"));
        if (req.getAmount()      != null) e.setAmount(req.getAmount());
        if (req.getDescription() != null) e.setDescription(req.getDescription());
        if (req.getDate()        != null) e.setDate(req.getDate());
        if (req.getCategoryId()  != null) e.setCategory(resolveCategory(req.getCategoryId(), user));
        return toDto(expenseRepository.save(e));
    }

    public void delete(Integer id) {
        User user = currentUser();
        Expense e = expenseRepository.findById(id)
            .filter(ex -> ex.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Dépense non trouvée"));
        expenseRepository.delete(e);
    }

    @Transactional(readOnly = true)
    public MonthlySummary monthlySummary(int year, int month) {
        User user = currentUser();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());

        BigDecimal total = expenseRepository.sumByUserIdAndDateBetween(user.getId(), start, end);
        List<Object[]> byCat = expenseRepository.sumByCategoryAndDateBetween(user.getId(), start, end);
        Map<String, BigDecimal> catMap = new LinkedHashMap<>();
        for (Object[] row : byCat) catMap.put((String) row[0], (BigDecimal) row[1]);

        return MonthlySummary.builder()
            .year(year).month(month)
            .totalExpenses(total != null ? total : BigDecimal.ZERO)
            .totalRevenues(BigDecimal.ZERO)
            .balance(BigDecimal.ZERO)
            .expensesByCategory(catMap)
            .build();
    }

    private Category resolveCategory(Integer catId, User user) {
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
