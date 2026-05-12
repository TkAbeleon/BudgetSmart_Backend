package com.budgetsmart.service;

import com.budgetsmart.dto.BudgetDtos.SavingsAddRequest;
import com.budgetsmart.dto.BudgetDtos.SavingsRequest;
import com.budgetsmart.dto.BudgetDtos.SavingsResponse;
import com.budgetsmart.entity.Savings;
import com.budgetsmart.entity.User;
import com.budgetsmart.exception.ResourceNotFoundException;
import com.budgetsmart.repository.SavingsRepository;
import com.budgetsmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SavingsService {

    private final SavingsRepository savingsRepository;
    private final UserRepository userRepository;

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }

    public SavingsResponse create(SavingsRequest req) {
        User user = currentUser();
        Savings s = Savings.builder()
            .user(user)
            .name(req.getGoalName())
            .targetAmount(req.getTargetAmount())
            .targetDate(req.getTargetDate())
            .build();
        return toDto(savingsRepository.save(s));
    }

    @Transactional(readOnly = true)
    public List<SavingsResponse> findAll() {
        return savingsRepository.findByUserId(currentUser().getId())
            .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SavingsResponse findById(Integer id) {
        User user = currentUser();
        return toDto(savingsRepository.findById(id)
            .filter(s -> s.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Objectif non trouvé")));
    }

    public SavingsResponse update(Integer id, SavingsRequest req) {
        User user = currentUser();
        Savings s = savingsRepository.findById(id)
            .filter(sv -> sv.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Objectif non trouvé"));
        if (req.getGoalName()     != null) s.setName(req.getGoalName());
        if (req.getTargetAmount() != null) s.setTargetAmount(req.getTargetAmount());
        if (req.getTargetDate()   != null) s.setTargetDate(req.getTargetDate());
        return toDto(savingsRepository.save(s));
    }

    public SavingsResponse addAmount(Integer id, SavingsAddRequest req) {
        User user = currentUser();
        Savings s = savingsRepository.findById(id)
            .filter(sv -> sv.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Objectif non trouvé"));
        s.setCurrentAmount(s.getCurrentAmount().add(req.getAmount()));
        return toDto(savingsRepository.save(s));
    }

    public void delete(Integer id) {
        User user = currentUser();
        Savings s = savingsRepository.findById(id)
            .filter(sv -> sv.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Objectif non trouvé"));
        savingsRepository.delete(s);
    }

    private SavingsResponse toDto(Savings s) {
        BigDecimal remaining = s.getTargetAmount().subtract(s.getCurrentAmount());
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;
        double pct = 0;
        if (s.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            pct = s.getCurrentAmount()
                .divide(s.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }
        return SavingsResponse.builder()
            .id(s.getId())
            .goalName(s.getName())
            .targetAmount(s.getTargetAmount())
            .currentAmount(s.getCurrentAmount())
            .remaining(remaining)
            .progressPercent(Math.min(pct, 100.0))
            .targetDate(s.getTargetDate())
            .completed(s.isCompleted())
            .createdAt(s.getCreatedAt())
            .build();
    }
}
