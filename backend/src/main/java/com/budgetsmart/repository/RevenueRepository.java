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
public interface RevenueRepository extends JpaRepository<Revenue, Integer> {
    List<Revenue> findByUserIdAndDateBetween(Integer userId, LocalDate start, LocalDate end);
    List<Revenue> findByUserIdAndCategoryId(Integer userId, Integer categoryId);
    Page<Revenue> findByUserId(Integer userId, Pageable pageable);
    Optional<Revenue> findFirstByUserIdOrderByDateDesc(Integer userId);
    long countByUserIdAndDateBetween(Integer userId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r WHERE r.user.id = :userId AND r.date BETWEEN :start AND :end")
    BigDecimal sumByUserIdAndDateBetween(@Param("userId") Integer userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(AVG(r.amount), 0) FROM Revenue r WHERE r.user.id = :userId AND r.date BETWEEN :start AND :end")
    BigDecimal averageByUserIdAndDateBetween(@Param("userId") Integer userId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
