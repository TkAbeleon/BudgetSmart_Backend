package com.budgetsmart.repository;

import com.budgetsmart.entity.Savings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsRepository extends JpaRepository<Savings, Long> {

    List<Savings> findByUserId(Long userId);

    List<Savings> findByUserIdAndCompleted(Long userId, boolean completed);
}
