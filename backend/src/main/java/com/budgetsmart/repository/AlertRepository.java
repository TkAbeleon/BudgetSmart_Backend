package com.budgetsmart.repository;

import com.budgetsmart.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Integer> {
    List<Alert> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<Alert> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Integer userId);
    long countByUserIdAndIsReadFalse(Integer userId);
}
