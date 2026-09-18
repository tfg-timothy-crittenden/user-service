package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.exception.EmailSendFailedException;
import com.timcritt.tfg.application.exception.NewPasswordNotValidException;
import com.timcritt.tfg.application.exception.PasswordResetTokenNotValidException;
import com.timcritt.tfg.application.exception.UserNotFoundException;
import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.PasswordEncoderPort;
import com.timcritt.tfg.application.port.outbound.PasswordResetTokenRepositoryPort;
import com.timcritt.tfg.application.port.outbound.TokenHasherPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.aggregate.passwordReset.PasswordResetToken;
import com.timcritt.tfg.domain.model.aggregate.user.PasswordHash;
import com.timcritt.tfg.domain.model.aggregate.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private EmailSenderPort emailSender;

    @Mock
    private TokenHasherPort tokenHasher;

    @Mock
    private PasswordResetTokenRepositoryPort passwordResetTokenRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                userRepository,
                emailSender,
                tokenHasher,
                passwordResetTokenRepository,
                passwordEncoder
        );
    }

    @Test
    void shouldDoNothingWhenEmailDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        service.requestPasswordReset("missing@example.com");

        verify(userRepository).findByEmail("missing@example.com");

        verifyNoInteractions(
                passwordResetTokenRepository,
                tokenHasher,
                emailSender,
                passwordEncoder
        );
    }

    @Test
    void shouldDoNothingWhenUserIsNotVerified() {
        User user = createUser(false);

        when(userRepository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(user));

        service.requestPasswordReset("tim@example.com");

        verify(userRepository).findByEmail("tim@example.com");

        verifyNoInteractions(
                passwordResetTokenRepository,
                tokenHasher,
                emailSender,
                passwordEncoder
        );
    }

    @Test
    void shouldRevokeExistingValidTokensBeforeCreatingNewOne() {
        User user = createUser(true);

        PasswordResetToken existingToken =
                PasswordResetToken.create(
                        1L,
                        "old-hash",
                        Instant.now()
                );

        when(userRepository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetTokenRepository.findAllByUserId(1L))
                .thenReturn(List.of(existingToken));

        when(tokenHasher.hash(any()))
                .thenReturn("new-hash");

        service.requestPasswordReset("tim@example.com");

        assertFalse(existingToken.isValid());

        verify(passwordResetTokenRepository).save(existingToken);
    }

    @Test
    void shouldNotSaveAlreadyInvalidExistingTokenAgain() {
        User user = createUser(true);

        PasswordResetToken existingToken =
                PasswordResetToken.create(
                        1L,
                        "old-hash",
                        Instant.now()
                );

        existingToken.revoke();

        when(userRepository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetTokenRepository.findAllByUserId(1L))
                .thenReturn(List.of(existingToken));

        when(tokenHasher.hash(any()))
                .thenReturn("new-hash");

        service.requestPasswordReset("tim@example.com");

        verify(passwordResetTokenRepository, times(1))
                .save(any(PasswordResetToken.class));
    }

    @Test
    void shouldCreateAndPersistHashedResetToken() {
        User user = createUser(true);

        when(userRepository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetTokenRepository.findAllByUserId(1L))
                .thenReturn(List.of());

        when(tokenHasher.hash(any()))
                .thenReturn("hashed-reset-token");

        service.requestPasswordReset("tim@example.com");

        ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        verify(passwordResetTokenRepository).save(captor.capture());

        PasswordResetToken savedToken = captor.getValue();

        assertEquals(1L, savedToken.getUserId());

        verify(tokenHasher).hash(any());
    }

    @Test
    void shouldSendPasswordResetEmail() {
        User user = createUser(true);

        when(userRepository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetTokenRepository.findAllByUserId(1L))
                .thenReturn(List.of());

        when(tokenHasher.hash(any()))
                .thenReturn("hashed-token");

        service.requestPasswordReset("tim@example.com");

        verify(emailSender).sendPasswordResetEmail(
                eq("tim@example.com"),
                contains("reset-password?token=")
        );
    }

    @Test
    void shouldThrowWhenPasswordResetEmailCannotBeSent() {
        User user = createUser(true);

        when(userRepository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetTokenRepository.findAllByUserId(1L))
                .thenReturn(List.of());

        when(tokenHasher.hash(any()))
                .thenReturn("hashed-token");

        doThrow(new RuntimeException("SMTP down"))
                .when(emailSender)
                .sendPasswordResetEmail(
                        eq("tim@example.com"),
                        any()
                );

        assertThrows(
                EmailSendFailedException.class,
                () -> service.requestPasswordReset("tim@example.com")
        );
    }

    @Test
    void shouldThrowWhenResetTokenDoesNotExist() {
        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");

        when(passwordResetTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.empty());

        assertThrows(
                PasswordResetTokenNotValidException.class,
                () -> service.setNewPassword(
                        "raw-token",
                        "new-password"
                )
        );

        verify(userRepository, never()).findById(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldThrowWhenResetTokenIsNotUsable() {
        PasswordResetToken token =
                PasswordResetToken.create(
                        1L,
                        "hashed-token",
                        Instant.now()
                );

        token.revoke();

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");

        when(passwordResetTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(token));

        assertThrows(
                PasswordResetTokenNotValidException.class,
                () -> service.setNewPassword(
                        "raw-token",
                        "new-password"
                )
        );

        verify(userRepository, never()).findById(any());
    }

    @Test
    void shouldThrowWhenUserForResetTokenDoesNotExist() {
        PasswordResetToken token =
                PasswordResetToken.create(
                        1L,
                        "hashed-token",
                        Instant.now()
                );

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");

        when(passwordResetTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(token));

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.setNewPassword(
                        "raw-token",
                        "new-password"
                )
        );
    }

    @Test
    void shouldRejectNewPasswordWhenSameAsCurrentPassword() {
        User user = createUser(true);

        PasswordResetToken token =
                PasswordResetToken.create(
                        1L,
                        "hashed-token",
                        Instant.now()
                );

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");

        when(passwordResetTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(token));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "same-password",
                user.getPasswordHash().value()
        )).thenReturn(true);

        assertThrows(
                NewPasswordNotValidException.class,
                () -> service.setNewPassword(
                        "raw-token",
                        "same-password"
                )
        );

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(passwordResetTokenRepository, never()).save(token);
    }

    @Test
    void shouldChangePasswordAndConsumeToken() {
        User user = createUser(true);

        PasswordResetToken token =
                PasswordResetToken.create(
                        1L,
                        "hashed-token",
                        Instant.now()
                );

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");

        when(passwordResetTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(token));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "new-password",
                user.getPasswordHash().value()
        )).thenReturn(false);

        when(passwordEncoder.encode("new-password"))
                .thenReturn("encoded-new-password");

        service.setNewPassword(
                "raw-token",
                "new-password"
        );

        assertEquals(
                PasswordHash.of("encoded-new-password"),
                user.getPasswordHash()
        );

        assertFalse(token.isUsableAt(Instant.now()));

        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).save(token);
    }

    @Test
    void shouldHashRawResetTokenBeforeLookup() {
        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");

        when(passwordResetTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.empty());

        assertThrows(
                PasswordResetTokenNotValidException.class,
                () -> service.setNewPassword(
                        "raw-token",
                        "new-password"
                )
        );

        verify(tokenHasher).hash("raw-token");
        verify(passwordResetTokenRepository)
                .findByTokenHash("hashed-token");
    }

    private User createUser(boolean verified) {
        return User.rehydrate(
                1L,
                "timcritt",
                "Tim",
                "Crittenden",
                "tim@example.com",
                Set.of(Role.STUDENT),
                PasswordHash.of("current-password-hash"),
                verified
        );
    }
}