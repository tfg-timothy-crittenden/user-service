package com.timcritt.tfg.infrastructure.web.controller;

import com.timcritt.tfg.infrastructure.ratelimiting.ResendVerificationRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import com.timcritt.tfg.application.exception.UserNotFoundException;
import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.infrastructure.security.CustomUserPrincipal;
import com.timcritt.tfg.infrastructure.security.JwtTokenService;
import com.timcritt.tfg.infrastructure.service.PasswordResetAdapter;
import com.timcritt.tfg.infrastructure.service.PlatformInvitationAdapter;
import com.timcritt.tfg.infrastructure.web.UserDtoMapper;
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

    //TODO Move the business logic out of the infrastructure layer and into the application layer

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final org.springframework.security.authentication.AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserUseCase userUseCase;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationAdapter emailVerificationFacade;
    private final PlatformInvitationAdapter platformInvitationAdapter;
    private final PasswordResetAdapter passwordResetAdapter;
    private final ResendVerificationRateLimiter resendRateLimiter;

    public AuthController(
            org.springframework.security.authentication.AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            UserUseCase userUseCase,
            PasswordEncoder passwordEncoder,
            EmailVerificationAdapter emailVerificationFacade,
            PlatformInvitationAdapter platformInvitationAdapter,
            PasswordResetAdapter passwordResetAdapter,
            ResendVerificationRateLimiter resendRateLimiter) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userUseCase = userUseCase;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationFacade = emailVerificationFacade;
        this.platformInvitationAdapter = platformInvitationAdapter;
        this.passwordResetAdapter = passwordResetAdapter;
        this.resendRateLimiter = resendRateLimiter;
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

            // Build the same user payload as /me (we need id, name and surname to include in the token)
            UserDto userDto = UserDtoMapper.toDto(userUseCase.getUserByUsername(username));
            String token = jwtTokenService.generateToken(userDto.getId(), username, userDto.getName(), userDto.getSurname(), userDto.getRoles());

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(java.util.Map.of("user", userDto, "token", token));

        } catch (DisabledException e) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", "Please confirm your email"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Invalid credentials"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        log.info("GET /api/auth/me");

        String username = authentication.getPrincipal() instanceof UserDetails userDetails
                ? userDetails.getUsername()
                : authentication.getName();

        // Return the user wrapped under a top-level 'user' property to match the login response shape
        UserDto userDto = UserDtoMapper.toDto(userUseCase.getUserByUsername(username));
        return ResponseEntity.ok(java.util.Map.of("user", userDto));
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
    public ResponseEntity<?> sendPlatformInvitation(@Valid @RequestBody SendInvitationRequest invitationBody) {
        log.info("POST /api/auth/send-platform-invitation");

        platformInvitationAdapter.createAndSendPlatformInvitation(invitationBody.email(), invitationBody.roleType());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup-with-invitation")
    public ResponseEntity<?> signupWithInvitation(@Valid @RequestBody SignupWithInvitationRequest request) {
        log.info("POST /api/auth/signup-with-invitation");
        platformInvitationAdapter.signupWithInvitationToken(request.invitationToken(), request.username(), request.name(), request.surname(), request.password());
        return ResponseEntity.ok().build();
    }

    // Only for unauthenticated users: require an email in the request body
    @PostMapping("/resend-verification-email")
    public ResponseEntity<?> resendVerificationEmail(@Valid @RequestBody ResendVerificationEmailRequest body, HttpServletRequest request) {
        log.info("POST /api/auth/resend-verification-email");
        String ip = request.getRemoteAddr();
        if (!resendRateLimiter.tryConsume(body.email().trim().toLowerCase(), ip)) {
            return ResponseEntity.status(429).body(java.util.Map.of("error", "Too many requests. Please try again later."));
        }
        // Always returns 200 — even if the email is unknown or already verified (prevents user enumeration)
        emailVerificationFacade.resendVerificationEmail(body.email());
        return ResponseEntity.ok().build();
    }

    // Only for unauthenticated users: require an email in the request body
    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody RequestPasswordResetRequest body) {
        log.info("POST /api/auth/request-password-reset");
        String email = body.email();

        passwordResetAdapter.requestPasswordReset(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("change-password")
    public ResponseEntity<?> changePassword(@RequestParam("token") String token, @Valid @RequestBody SetNewPasswordRequest body) {
        log.info("POST /api/auth/change-password");
        passwordResetAdapter.setNewPassword(token, body.newPassword());
        return ResponseEntity.ok().build();
    }


    public record LoginRequest(@NotBlank String username, @NotBlank String password) { }
    public record LoginResponse(@NotBlank String username, @NotBlank String token, String message) { }

    public record SignupRequest(@NotBlank String username, @NotBlank String name, @NotBlank String surname, @NotBlank String email, @NotBlank String password) { }
    public record SignupResponse(@NotBlank String username, String message) { }

    public record SendInvitationRequest(@NotBlank String email, @NotNull RoleType roleType) { }
    public record SignupWithInvitationRequest(@NotBlank String username, @NotBlank String name, @NotBlank String surname, @NotBlank String invitationToken, @NotBlank String password) { }


    public record RequestPasswordResetRequest(@NotBlank String email) { }
    public record SetNewPasswordRequest(@NotBlank String newPassword) { }
    public record ResendVerificationEmailRequest(@NotBlank String email) { }

}
