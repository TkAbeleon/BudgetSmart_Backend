package com.budgetsmart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de Sécurité Spring Security
 * 
 * Gère :
 * - JWT Authentication Filter
 * - CORS Policy
 * - Endpoints publics vs privés
 * - CSRF Disable (JWT-based)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfig corsConfig;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                         CorsConfig corsConfig) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfig = corsConfig;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS Configuration
            .cors().configurationSource(corsConfig.corsConfigurationSource())
            .and()

            // CSRF Disabled (JWT-based, pas de sessions)
            .csrf().disable()

            // Session Management - STATELESS (JWT)
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()

            // Authorization Rules
            .authorizeRequests()
                // Public endpoints
                .antMatchers("/api/auth/register", "/api/auth/login").permitAll()
                .antMatchers("/api/chat/webhook/n8n").permitAll()
                .antMatchers("/api/health").permitAll()
                .antMatchers("/api/swagger-ui.html", "/api/v3/api-docs", "/api/v3/api-docs/**").permitAll()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Protected endpoints
                .anyRequest().authenticated()
            .and()

            // Exception Handling
            .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(401, "Unauthorized");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendError(403, "Access Denied");
                });

        // Add JWT Filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
