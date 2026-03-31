package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.application.exception.UserNotFoundException;
import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.infrastructure.security.CustomUserPrincipal;
import com.timcritt.tfg.infrastructure.security.JwtTokenService;
import com.timcritt.tfg.infrastructure.service.PlatformInvitationAdapter;
import com.timcritt.tfg.infrastructure.web.dto.UserDto;
import com.timcritt.tfg.infrastructure.service.EmailVerificationAdapter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final org.springframework.security.authentication.AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserUseCase userUseCase;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationAdapter emailVerificationFacade;
    private final PlatformInvitationAdapter platformInvitationAdapter;

    public AuthController(
            org.springframework.security.authentication.AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            UserUseCase userUseCase,
            PasswordEncoder passwordEncoder,
            EmailVerificationAdapter emailVerificationFacade, PlatformInvitationAdapter platformInvitationAdapter) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userUseCase = userUseCase;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationFacade = emailVerificationFacade;
        this.platformInvitationAdapter = platformInvitationAdapter;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        // First check whether the user exists and is unverified — return 403 with a helpful message.
        try {
            var existing = userUseCase.getUserByUsername(request.username());
            if (existing != null && !existing.isVerified()) {
                return ResponseEntity.status(403).body(java.util.Map.of("error", "Please confirm your email"));
            }
        } catch (UserNotFoundException e) {
            // If user doesn't exist, fall through to authentication to return 401 for bad credentials.
        }

        try {
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

        } catch (DisabledException e) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", "Please confirm your email"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Invalid credentials"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {
        log.info("GET /api/auth/me");

        String username = authentication.getPrincipal() instanceof UserDetails userDetails
                ? userDetails.getUsername()
                : authentication.getName();

        return ResponseEntity.ok(UserDtoMapper.toDto(userUseCase.getUserByUsername(username)));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("POST /api/auth/signup");

        String passwordHash = passwordEncoder.encode(request.password());
        userUseCase.createUser(request.username(), request.name(), request.surname(), request.email(), passwordHash);

        return ResponseEntity.ok(new SignupResponse(request.username(), "User created"));
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail(@RequestParam("token") String token) {
        log.info("GET /api/auth/confirm-email token={}", token);

        try {
            emailVerificationFacade.confirmToken(token);
            return ResponseEntity.ok(java.util.Map.of("message", "Email confirmed"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(410).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/send-platform-invitation")
    public ResponseEntity<?> sendPlatformInvitation(@Valid @RequestBody SendInvitationRequest invitationBody, @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        log.info("POST /api/auth/send-platform-invitation");

        platformInvitationAdapter.createAndSendPlatformInvitation(userPrincipal.getId(), invitationBody.email(), invitationBody.roleType());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup-with-invitation")
    public ResponseEntity<?> signupWithInvitation(@Valid @RequestBody SignupWithInvitationRequest request) {
        log.info("POST /api/auth/signup-with-invitation");
        platformInvitationAdapter.signupWithInvitationToken(request.invitationToken(), request.username(), request.name(), request.surname(), request.password());
        return ResponseEntity.ok().build();
    }


    public record LoginRequest(@NotBlank String username, @NotBlank String password) { }
    public record LoginResponse(@NotBlank String username, @NotBlank String token, String message) { }
    public record SignupRequest(@NotBlank String username, @NotBlank String name, @NotBlank String surname, @NotBlank String email, @NotBlank String password) { }
    public record SignupResponse(@NotBlank String username, String message) { }

    public record SendInvitationRequest(@NotBlank String email, @NotNull RoleType roleType) { }

    public record SignupWithInvitationRequest(@NotBlank String username, @NotBlank String name, @NotBlank String surname, @NotBlank String invitationToken, @NotBlank String password) { }

}
