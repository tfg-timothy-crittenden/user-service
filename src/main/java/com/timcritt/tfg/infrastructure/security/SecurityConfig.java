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
                        request -> "/api/auth/login".equals(request.getRequestURI())
                                || "/api/auth/signup".equals(request.getRequestURI())
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/signup", "/api/auth/confirm-email").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
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
