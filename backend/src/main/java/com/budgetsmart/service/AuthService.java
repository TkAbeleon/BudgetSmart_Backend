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
import org.springframework.security.core.Authentication;
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

    /** Inscription */
    public AuthResponse register(RegisterRequest request) {
        log.info("Inscription : {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Un compte existe déjà avec cet email");
        }

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phone(request.getPhone())
            .build();

        User saved = userRepository.save(user);
        log.info("Utilisateur créé : {}", saved.getEmail());
        return buildResponse("Inscription réussie", saved);
    }

    /** Connexion */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Connexion : {}", request.getEmail());

        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Identifiants incorrects");
        }

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        return buildResponse("Connexion réussie", user);
    }

    /** Rafraîchissement du token */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Refresh token invalide ou expiré");
        }
        String email = jwtProvider.extractEmail(request.getRefreshToken());
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return buildResponse("Token rafraîchi", user);
    }

    /** Déconnexion (stateless JWT) */
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    /** Profil de l'utilisateur connecté */
    @Transactional(readOnly = true)
    public UserInfo getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return toUserInfo(user);
    }

    /** Mise à jour du profil */
    public UserInfo updateProfile(UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName()  != null) user.setLastName(request.getLastName());
        if (request.getPhone()     != null) user.setPhone(request.getPhone());

        return toUserInfo(userRepository.save(user));
    }

    /** Changement de mot de passe */
    public void changePassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ValidationException("Ancien mot de passe incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthResponse buildResponse(String message, User user) {
        String token        = jwtProvider.generateToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());
        return AuthResponse.builder()
            .message(message)
            .token(token)
            .refreshToken(refreshToken)
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
            .phone(user.getPhone())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
