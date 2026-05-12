package com.budgetsmart.repository;

import com.budgetsmart.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    List<Expense> findByUserIdAndDateBetween(Integer userId, LocalDate start, LocalDate end);
    List<Expense> findByUserIdAndCategoryId(Integer userId, Integer categoryId);
    Page<Expense> findByUserId(Integer userId, Pageable pageable);
    Optional<Expense> findFirstByUserIdOrderByDateDesc(Integer userId);
    List<Expense> findTop10ByUserIdOrderByAmountDesc(Integer userId);
    long countByUserIdAndDateBetween(Integer userId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :start AND :end")
    BigDecimal sumByUserIdAndDateBetween(@Param("userId") Integer userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.category.id = :categoryId")
    BigDecimal sumByUserIdAndCategoryId(@Param("userId") Integer userId, @Param("categoryId") Integer categoryId);

    @Query("SELECT COALESCE(AVG(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :start AND :end")
    BigDecimal averageByUserIdAndDateBetween(@Param("userId") Integer userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT c.name, COALESCE(SUM(e.amount), 0) FROM Expense e JOIN e.category c WHERE e.user.id = :userId AND e.date BETWEEN :start AND :end GROUP BY c.name ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumByCategoryAndDateBetween(@Param("userId") Integer userId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
