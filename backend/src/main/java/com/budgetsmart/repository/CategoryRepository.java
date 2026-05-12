package com.budgetsmart.repository;

import com.budgetsmart.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByUserId(Integer userId);
    List<Category> findByUserIdAndType(Integer userId, String type);
}
