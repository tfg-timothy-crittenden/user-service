package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.port.outbound.EmailVerificationTokenRepositoryPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.infrastructure.service.EmailSenderService;
import com.timcritt.tfg.domain.model.EmailVerificationToken;
import com.timcritt.tfg.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepositoryPort tokenRepository;
    private final EmailSenderService emailSenderService;
    private final UserRepositoryPort userRepository;

    public EmailVerificationService(EmailVerificationTokenRepositoryPort tokenRepository, EmailSenderService emailSenderService, UserRepositoryPort userRepository) {
        this.tokenRepository = tokenRepository;
        this.emailSenderService = emailSenderService;
        this.userRepository = userRepository;
    }

    public void createAndSendToken(Long userId, String userEmail) {
        // create token
        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(60 * 60 * 24); // 24 hours
        EmailVerificationToken ev = new EmailVerificationToken(null, userId, userEmail, token, now, expiresAt);
        tokenRepository.save(ev);

        // build link
        String link = "http://localhost:8082/api/auth/confirm-email?token=" + token;
        emailSenderService.sendVerificationEmail(userEmail, link);
    }

    @Transactional
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
