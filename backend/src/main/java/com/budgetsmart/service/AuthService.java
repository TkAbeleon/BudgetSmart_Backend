package com.budgetsmart.service;

import com.budgetsmart.config.JwtProvider;
import com.budgetsmart.dto.AuthDtos.*;
import com.budgetsmart.entity.User;
import com.budgetsmart.exception.ResourceNotFoundException;
import com.budgetsmart.exception.UnauthorizedException;
import com.budgetsmart.exception.ValidationException;
import com.budgetsmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new ValidationException("Un compte existe déjà avec cet email");

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .build();

        return buildResponse("Inscription réussie", userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Identifiants incorrects");
        }
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return buildResponse("Connexion réussie", user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest req) {
        if (!jwtProvider.validateToken(req.getRefreshToken()))
            throw new UnauthorizedException("Refresh token invalide");
        String email = jwtProvider.extractEmail(req.getRefreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return buildResponse("Token rafraîchi", user);
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Transactional(readOnly = true)
    public UserInfo getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return toUserInfo(user);
    }

    public UserInfo updateProfile(UpdateProfileRequest req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        if (req.getFirstName() != null)
            user.setFirstName(req.getFirstName());
        if (req.getLastName() != null)
            user.setLastName(req.getLastName());
        if (req.getEmail() != null && !req.getEmail().isBlank())
            user.setEmail(req.getEmail());
        if (req.getMonthlyBudget() != null)
            user.setMonthlyBudget(req.getMonthlyBudget());
        return toUserInfo(userRepository.save(user));
    }

    public void changePassword(ChangePasswordRequest req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword()))
            throw new ValidationException("Ancien mot de passe incorrect");
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthResponse buildResponse(String message, User user) {
        return AuthResponse.builder()
                .message(message)
                .token(jwtProvider.generateToken(user.getEmail()))
                .refreshToken(jwtProvider.generateRefreshToken(user.getEmail()))
                .expiresIn(jwtProvider.getExpiration())
                .user(toUserInfo(user))
                .status("success")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public UserInfo toUserInfo(User user) {
        return UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .monthlyBudget(user.getMonthlyBudget())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
