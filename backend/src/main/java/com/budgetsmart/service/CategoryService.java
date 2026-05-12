package com.budgetsmart.service;

import com.budgetsmart.dto.BudgetDtos.CategoryRequest;
import com.budgetsmart.dto.BudgetDtos.CategoryResponse;
import com.budgetsmart.entity.Category;
import com.budgetsmart.entity.User;
import com.budgetsmart.exception.ResourceNotFoundException;
import com.budgetsmart.repository.CategoryRepository;
import com.budgetsmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }

    public CategoryResponse create(CategoryRequest req) {
        User user = currentUser();
        Category cat = Category.builder()
            .user(user)
            .name(req.getName())
            .type(req.getType())
            .color(req.getColor() != null ? req.getColor() : "#6366f1")
            .build();
        return toDto(categoryRepository.save(cat));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll(String type) {
        User user = currentUser();
        List<Category> list = (type != null)
            ? categoryRepository.findByUserIdAndType(user.getId(), type)
            : categoryRepository.findByUserId(user.getId());
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public CategoryResponse update(Integer id, CategoryRequest req) {
        User user = currentUser();
        Category cat = categoryRepository.findById(id)
            .filter(c -> c.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));
        if (req.getName()  != null) cat.setName(req.getName());
        if (req.getColor() != null) cat.setColor(req.getColor());
        return toDto(categoryRepository.save(cat));
    }

    public void delete(Integer id) {
        User user = currentUser();
        Category cat = categoryRepository.findById(id)
            .filter(c -> c.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));
        categoryRepository.delete(cat);
    }

    CategoryResponse toDto(Category c) {
        return CategoryResponse.builder()
            .id(c.getId())
            .name(c.getName())
            .color(c.getColor())
            .type(c.getType())
            .build();
    }
}
