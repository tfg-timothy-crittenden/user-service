package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.infrastructure.security.JwtTokenService;
import com.timcritt.tfg.infrastructure.web.dto.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final org.springframework.security.authentication.AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserUseCase userUseCase;

    public AuthController(
            org.springframework.security.authentication.AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            UserUseCase userUseCase
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userUseCase = userUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String username = authentication.getPrincipal() instanceof UserDetails userDetails
                ? userDetails.getUsername()
                : request.username();
        String token = jwtTokenService.generateToken(username);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new LoginResponse(username, token, "Authenticated"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {
        log.info("GET /api/auth/me");

        String username = authentication.getPrincipal() instanceof UserDetails userDetails
                ? userDetails.getUsername()
                : authentication.getName();

        return ResponseEntity.ok(UserDtoMapper.toDto(userUseCase.getUserByUsername(username)));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) { }

    public record LoginResponse(String username, String token, String message) { }
}
