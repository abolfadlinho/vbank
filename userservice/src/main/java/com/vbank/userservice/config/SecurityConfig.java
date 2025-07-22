package com.vbank.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security configuration for the User Service.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Defines the password encoder bean that will be used for hashing passwords.
     * @return A BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain.
     * - Disables CSRF as we are using a stateless API.
     * - Makes registration and login endpoints public.
     * - Secures all other endpoints.
     * - Enables the H2 console for development.
     * - Configures stateless session management.
     * @param http The HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection for stateless REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/users/register")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/users/login")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/users/{userId}/profile")).permitAll()
                        .anyRequest().authenticated()
                )

                // Configure stateless session management
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Allow rendering of H2 console frames
                .headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }
}