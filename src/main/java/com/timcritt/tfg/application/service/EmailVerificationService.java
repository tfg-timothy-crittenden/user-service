package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.EmailVerificationTokenRepositoryPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.EmailVerificationToken;
import com.timcritt.tfg.domain.model.User;

import java.time.Instant;
import java.util.UUID;

public class EmailVerificationService {

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

    public void createAndSendToken(Long userId, String userEmail) {
        // create token
        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(60 * 60 * 24); // 24 hours
        EmailVerificationToken ev = new EmailVerificationToken(null, userId, userEmail, token, now, expiresAt);
        tokenRepository.save(ev);

        // build link using configured frontend URL template. Template should contain "{token}" placeholder.
        String link;
        if (verifyUrlTemplate != null && verifyUrlTemplate.contains("{token}")) {
            link = verifyUrlTemplate.replace("{token}", token);
        } else if (verifyUrlTemplate != null && verifyUrlTemplate.contains("%s")) {
            link = String.format(verifyUrlTemplate, token);
        } else {
            // Fallback to previous server-side confirm endpoint
            link = "http://localhost:8082/api/auth/confirm-email?token=" + token;
        }
        emailSender.sendVerificationEmail(userEmail, link);
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
        EmailVerificationToken ev = tokenRepository.findByToken(token).orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (ev.isExpired()) {
            throw new IllegalStateException("Token expired");
        }

        // mark token confirmed
        ev.confirm();
        tokenRepository.save(ev);

        // mark user verified
        if (ev.getUserId() != null) {
            User user = userRepository.findById(ev.getUserId()).orElse(null);
            if (user != null && !user.isVerified()) {
                user.setVerified(true);
                userRepository.save(user);
            }
        } else if (ev.getUserEmail() != null) {
            userRepository.findByEmail(ev.getUserEmail()).ifPresent(u -> {
                if (!u.isVerified()) {
                    u.setVerified(true);
                    userRepository.save(u);
                }
            });
        }
    }
}
