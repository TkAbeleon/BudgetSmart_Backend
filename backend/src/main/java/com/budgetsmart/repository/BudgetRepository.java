package com.budgetsmart.repository;

import com.budgetsmart.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdAndActive(Long userId, boolean active);

    List<Budget> findByUserId(Long userId);

    List<Budget> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<Budget> findByUserIdAndActiveAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        Long userId, boolean active, LocalDate start, LocalDate end);
}
