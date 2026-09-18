package com.timcritt.tfg.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        return http
                /*
                 * Authentication is stateless and performed with bearer JWTs.
                 * No authentication cookies are used, so CSRF protection is
                 * unnecessary for this API.
                 */
                .csrf(AbstractHttpConfigurer::disable)

                /*
                 * Never create or use an HTTP session for authentication.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * This service exposes an API rather than Spring's
                 * form-login authentication.
                 */
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        // -----------------------------------------------------
                        // Public authentication endpoints
                        // -----------------------------------------------------

                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/api/auth/confirm-email",
                                "/api/auth/signup-with-invitation",
                                "/api/auth/request-password-reset",
                                "/api/auth/change-password",
                                "/api/auth/resend-verification-email"
                        ).permitAll()

                        // -----------------------------------------------------
                        // Administrator endpoints
                        // -----------------------------------------------------

                        .requestMatchers(
                                "/api/auth/send-platform-invitation",
                                "/api/users/teachers",
                                "/api/platform-invitations/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/users/*/roles/*"
                        ).hasRole("ADMIN")

                        // -----------------------------------------------------
                        // Actuator
                        // -----------------------------------------------------

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/prometheus"
                        ).permitAll()

                        // -----------------------------------------------------
                        // OpenAPI / Swagger
                        // -----------------------------------------------------

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        /*
                         * Needed so Spring can dispatch errors without
                         * replacing them with another authentication error.
                         */
                        .requestMatchers("/error").permitAll()

                        // -----------------------------------------------------
                        // Everything else under the API requires authentication
                        // -----------------------------------------------------

                        .requestMatchers("/api/**")
                        .authenticated()

                        /*
                         * Fail closed. If a new non-API endpoint is accidentally
                         * introduced, it isn't automatically exposed.
                         */
                        .anyRequest()
                        .denyAll()
                )

                /*
                 * REST clients should receive 401 rather than being redirected
                 * to a login form.
                 */
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                )
                        )
                )

                /*
                 * Populate the SecurityContext from the bearer JWT before
                 * Spring's username/password filter would execute.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter(
            CustomUserDetailsService userDetailsService,
            @Value("${app.jwt.secret}") String secret
    ) {
        return new JwtAuthenticationFilter(
                userDetailsService,
                secret
        );
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}