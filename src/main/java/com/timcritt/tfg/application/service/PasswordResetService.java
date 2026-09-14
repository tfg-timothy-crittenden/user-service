package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.exception.EmailSendFailedException;
import com.timcritt.tfg.application.exception.NewPasswordNotValidException;
import com.timcritt.tfg.application.exception.PasswordResetTokenNotValidException;
import com.timcritt.tfg.application.exception.UserNotFoundException;
import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.PasswordEncoderPort;
import com.timcritt.tfg.application.port.outbound.PasswordResetTokenRepositoryPort;
import com.timcritt.tfg.application.port.outbound.TokenEncoderPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.aggregate.passwordReset.PasswordResetToken;
import com.timcritt.tfg.domain.model.aggregate.user.PasswordHash;
import com.timcritt.tfg.domain.model.aggregate.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PasswordResetService {

    private final UserRepositoryPort userRepository;
    private final EmailSenderPort emailSender;
    private final TokenEncoderPort tokenHasher;
    private final PasswordResetTokenRepositoryPort passwordResetTokenRepository;
    private final PasswordEncoderPort passwordEncoder;

    public PasswordResetService(
            UserRepositoryPort userRepository,
            EmailSenderPort emailSender,
            TokenEncoderPort tokenHasher,
            PasswordResetTokenRepositoryPort passwordResetTokenRepository,
            PasswordEncoderPort passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        this.tokenHasher = tokenHasher;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void requestPasswordReset(String email) {

        Optional<User> userOpt = userRepository.findByEmail(email);

        // Don't disclose whether the email is registered.
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        // Unverified users cannot reset their password.
        // Still return silently to avoid user enumeration.
        if (!user.isVerified()) {
            return;
        }

        // Invalidate existing password-reset tokens.
        List<PasswordResetToken> existingTokens =
                passwordResetTokenRepository.findAllByUserId(user.getId());

        existingTokens.forEach(token -> {
            if (token.isValid()) {
                token.invalidate();
                passwordResetTokenRepository.save(token);
            }
        });

        // Generate the externally-visible token.
        String token = UUID.randomUUID().toString();

        // Persist only its hash.
        String tokenHash = tokenHasher.encode(token);

        PasswordResetToken passwordResetToken = PasswordResetToken.create(user.getId(), tokenHash);

        passwordResetTokenRepository.save(passwordResetToken);

        // TODO point this at the frontend.
        String link =
                "http://localhost:8082/api/auth/reset-password?token=" + token;

        try {
            emailSender.sendPasswordResetEmail(email, link);
        } catch (Exception e) {
            throw new EmailSendFailedException(
                    "Failed to send password reset email",
                    e
            );
        }
    }

    public void setNewPassword(String token, String newPassword) {

        String encodedToken = tokenHasher.encode(token);

        PasswordResetToken passwordResetToken =
                passwordResetTokenRepository.findByTokenHash(encodedToken)
                        .orElseThrow(() ->
                                new PasswordResetTokenNotValidException(
                                        "Password reset token not valid"
                                )
                        );

        if (!passwordResetToken.isValid()
                || passwordResetToken.isExpired()) {
            throw new PasswordResetTokenNotValidException(
                    "Password reset token not valid"
            );
        }

        Long userId = passwordResetToken.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(userId, "")
                );

        PasswordHash currentPasswordHash = user.getPasswordHash();

        assert currentPasswordHash != null;
        if (passwordEncoder.matches(
                newPassword,
                currentPasswordHash.value()
        )) {
            throw new NewPasswordNotValidException(
                    "New password must not be the same as the current one"
            );
        }

        PasswordHash newPasswordHash =
                PasswordHash.of(
                        passwordEncoder.encode(newPassword)
                );

        user.changePassword(newPasswordHash);

        passwordResetToken.invalidate();

        userRepository.save(user);
        passwordResetTokenRepository.save(passwordResetToken);
    }
}