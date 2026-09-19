package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.port.inbound.EmailVerificationUseCase;
import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.EmailVerificationTokenRepositoryPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.aggregate.user.EmailVerificationToken;
import com.timcritt.tfg.domain.model.aggregate.user.User;

import java.time.Instant;
import java.util.UUID;

public class EmailVerificationService implements EmailVerificationUseCase {

    private final EmailVerificationTokenRepositoryPort tokenRepository;
    private final EmailSenderPort emailSender;
    private final UserRepositoryPort userRepository;
    private final String verifyUrlTemplate;

    public EmailVerificationService(EmailVerificationTokenRepositoryPort tokenRepository, EmailSenderPort emailSender, UserRepositoryPort userRepository, String verifyUrlTemplate) {
        this.tokenRepository = tokenRepository;
        this.emailSender = emailSender;
        this.userRepository = userRepository;
        this.verifyUrlTemplate = verifyUrlTemplate;
    }

    public void createAndSendToken(
            Long userId,
            String userEmail
    ) {
        tokenRepository.findByUserId(userId)
                .ifPresent(tokenRepository::delete);

        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();

        EmailVerificationToken ev =
                EmailVerificationToken.create(
                        userId,
                        userEmail,
                        token,
                        now
                );

        tokenRepository.save(ev);

        String link;

        if (verifyUrlTemplate != null
                && verifyUrlTemplate.contains("{token}")) {
            link = verifyUrlTemplate.replace("{token}", token);

        } else if (verifyUrlTemplate != null
                && verifyUrlTemplate.contains("%s")) {
            link = String.format(verifyUrlTemplate, token);

        } else {
            link =
                    "http://localhost:8082/api/auth/confirm-email?token="
                            + token;
        }

        emailSender.sendVerificationEmail(
                userEmail,
                link
        );
    }

    public void resendVerificationEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        // Silently do nothing if user not found or already verified — prevents user enumeration
        java.util.Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isEmpty() || userOpt.get().isVerified()) {
            return;
        }

        // Cancel any existing pending token so there is never more than one active at a time
        tokenRepository.findByUserEmail(normalizedEmail).ifPresent(tokenRepository::delete);

        // Issue and send a fresh token
        createAndSendToken(userOpt.get().getId(), normalizedEmail);
    }

    public void confirmToken(String token) {
        EmailVerificationToken ev = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        Instant now = Instant.now();

        if (ev.isExpiredAt(now)) {
            throw new IllegalStateException("Token expired");
        }

        ev.confirmAt(now);
        tokenRepository.save(ev);

        userRepository.findById(ev.getUserId())
                .filter(user -> !user.isVerified())
                .ifPresent(user -> {
                    user.confirmEmail();
                    userRepository.save(user);
                });
    }
}
