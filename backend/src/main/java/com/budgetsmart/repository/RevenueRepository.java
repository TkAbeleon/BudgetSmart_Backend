package com.budgetsmart.repository;

import com.budgetsmart.entity.Revenue;
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
public interface RevenueRepository extends JpaRepository<Revenue, Long> {

    List<Revenue> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    List<Revenue> findByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r WHERE r.user.id = :userId AND r.date BETWEEN :start AND :end")
    BigDecimal sumByUserIdAndDateBetween(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r WHERE r.user.id = :userId AND r.category.id = :categoryId")
    BigDecimal sumByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    Page<Revenue> findByUserId(Long userId, Pageable pageable);

    List<Revenue> findByUserIdAndCategoryIdAndDateBetween(Long userId, Long categoryId, LocalDate start, LocalDate end);

    long countByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    Optional<Revenue> findFirstByUserIdOrderByDateDesc(Long userId);

    @Query("SELECT COALESCE(AVG(r.amount), 0) FROM Revenue r WHERE r.user.id = :userId AND r.date BETWEEN :start AND :end")
    BigDecimal averageByUserIdAndDateBetween(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
