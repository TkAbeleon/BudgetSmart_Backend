package com.budgetsmart.repository;

import com.budgetsmart.entity.Savings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsRepository extends JpaRepository<Savings, Integer> {
    List<Savings> findByUserId(Integer userId);
}
