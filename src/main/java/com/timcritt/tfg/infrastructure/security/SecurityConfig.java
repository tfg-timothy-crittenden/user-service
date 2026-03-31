package com.timcritt.tfg.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.timcritt.tfg.application.port.outbound.PasswordEncoderPort;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) {
        var apiEntryPoint = new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED);
        var jsonMatcher = new MediaTypeRequestMatcher(new HeaderContentNegotiationStrategy(), MediaType.APPLICATION_JSON);
        jsonMatcher.setIgnoredMediaTypes(java.util.Set.of(MediaType.ALL));

        return http
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        // ignore CSRF for all API endpoints (JWT clients)
                        request -> request.getRequestURI().startsWith("/api/")
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/send-platform-invitation").hasRole("ADMIN")
                        .requestMatchers("/login", "/error").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/signup", "/api/auth/confirm-email", "/api/auth/signup-with-invitation").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(apiEntryPoint, jsonMatcher))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.defaultSuccessUrl("/", true))
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                .build();
    }

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter(
            CustomUserDetailsService userDetailsService,
            @Value("${app.jwt.secret:change-me-change-me-change-me-change-me}") String secret
    ) {
        return new JwtAuthenticationFilter(userDetailsService, secret);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }
}
