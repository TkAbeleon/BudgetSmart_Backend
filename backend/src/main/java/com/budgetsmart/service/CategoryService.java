package com.budgetsmart.service;

import com.budgetsmart.dto.BudgetDtos.*;
import com.budgetsmart.entity.*;
import com.budgetsmart.exception.ResourceNotFoundException;
import com.budgetsmart.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
            .description(req.getDescription())
            .color(req.getColor() != null ? req.getColor() : "#6366f1")
            .icon(req.getIcon())
            .type(req.getType())
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

    public CategoryResponse update(Long id, CategoryRequest req) {
        User user = currentUser();
        Category cat = categoryRepository.findById(id)
            .filter(c -> c.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));

        if (req.getName()        != null) cat.setName(req.getName());
        if (req.getDescription() != null) cat.setDescription(req.getDescription());
        if (req.getColor()       != null) cat.setColor(req.getColor());
        if (req.getIcon()        != null) cat.setIcon(req.getIcon());

        return toDto(categoryRepository.save(cat));
    }

    public void delete(Long id) {
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
            .description(c.getDescription())
            .color(c.getColor())
            .icon(c.getIcon())
            .type(c.getType())
            .build();
    }
}
