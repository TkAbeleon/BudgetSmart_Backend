package com.budgetsmart.repository;

import com.budgetsmart.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdAndType(Long userId, String type);

    List<Category> findByUserIdAndActive(Long userId, boolean active);
}
