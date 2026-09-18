package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.EmailVerificationTokenRepositoryPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.aggregate.user.EmailVerificationToken;
import com.timcritt.tfg.domain.model.aggregate.user.PasswordHash;
import com.timcritt.tfg.domain.model.aggregate.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationTokenRepositoryPort tokenRepository;

    @Mock
    private EmailSenderPort emailSender;

    @Mock
    private UserRepositoryPort userRepository;

    private EmailVerificationService service;

    private static final String VERIFY_URL =
            "https://frontend.example.com/verify?token={token}";

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(
                tokenRepository,
                emailSender,
                userRepository,
                VERIFY_URL
        );
    }

    @Test
    void shouldCreateSaveAndSendVerificationToken() {
        service.createAndSendToken(
                1L,
                "tim@example.com"
        );

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);

        verify(tokenRepository).save(tokenCaptor.capture());

        EmailVerificationToken savedToken = tokenCaptor.getValue();

        assertEquals(1L, savedToken.getUserId());
        assertEquals("tim@example.com", savedToken.getUserEmail());
        assertNotNull(savedToken.getToken());

        verify(emailSender).sendVerificationEmail(
                eq("tim@example.com"),
                startsWith("https://frontend.example.com/verify?token=")
        );
    }

    @Test
    void shouldInsertGeneratedTokenIntoConfiguredUrl() {
        service.createAndSendToken(
                1L,
                "tim@example.com"
        );

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);

        ArgumentCaptor<String> linkCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(tokenRepository).save(tokenCaptor.capture());

        verify(emailSender).sendVerificationEmail(
                eq("tim@example.com"),
                linkCaptor.capture()
        );

        assertTrue(
                linkCaptor.getValue()
                        .contains(tokenCaptor.getValue().getToken())
        );
    }

    @Test
    void shouldSupportPercentSPlaceholderInVerificationUrl() {
        EmailVerificationService serviceWithPercentPlaceholder =
                new EmailVerificationService(
                        tokenRepository,
                        emailSender,
                        userRepository,
                        "https://frontend.example.com/verify?token=%s"
                );

        serviceWithPercentPlaceholder.createAndSendToken(
                1L,
                "tim@example.com"
        );

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);

        ArgumentCaptor<String> linkCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(tokenRepository).save(tokenCaptor.capture());

        verify(emailSender).sendVerificationEmail(
                eq("tim@example.com"),
                linkCaptor.capture()
        );

        assertTrue(
                linkCaptor.getValue()
                        .contains(tokenCaptor.getValue().getToken())
        );
    }

    @Test
    void shouldUseFallbackUrlWhenConfiguredUrlHasNoPlaceholder() {
        EmailVerificationService fallbackService =
                new EmailVerificationService(
                        tokenRepository,
                        emailSender,
                        userRepository,
                        "https://frontend.example.com/verify"
                );

        fallbackService.createAndSendToken(
                1L,
                "tim@example.com"
        );

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);

        ArgumentCaptor<String> linkCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(tokenRepository).save(tokenCaptor.capture());

        verify(emailSender).sendVerificationEmail(
                eq("tim@example.com"),
                linkCaptor.capture()
        );

        assertEquals(
                "http://localhost:8082/api/auth/confirm-email?token="
                        + tokenCaptor.getValue().getToken(),
                linkCaptor.getValue()
        );
    }

    @Test
    void shouldUseFallbackUrlWhenConfiguredUrlIsNull() {
        EmailVerificationService fallbackService =
                new EmailVerificationService(
                        tokenRepository,
                        emailSender,
                        userRepository,
                        null
                );

        fallbackService.createAndSendToken(
                1L,
                "tim@example.com"
        );

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);

        ArgumentCaptor<String> linkCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(tokenRepository).save(tokenCaptor.capture());

        verify(emailSender).sendVerificationEmail(
                eq("tim@example.com"),
                linkCaptor.capture()
        );

        assertEquals(
                "http://localhost:8082/api/auth/confirm-email?token="
                        + tokenCaptor.getValue().getToken(),
                linkCaptor.getValue()
        );
    }

    @Test
    void shouldDoNothingWhenResendingForUnknownUser() {
        when(userRepository.findByEmail("tim@example.com"))
                .thenReturn(Optional.empty());

        service.resendVerificationEmail("  TIM@EXAMPLE.COM  ");

        verify(userRepository)
                .findByEmail("tim@example.com");

        verifyNoInteractions(
                tokenRepository,
                emailSender
        );
    }

    @Test
    void shouldDoNothingWhenResendingForAlreadyVerifiedUser() {
        User user = createUser(true);

        when(userRepository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(user));

        service.resendVerificationEmail("tim@example.com");

        verifyNoInteractions(
                tokenRepository,
                emailSender
        );
    }

    @Test
    void shouldDeleteExistingTokenBeforeResendingVerificationEmail() {
        User user = createUser(false);

        EmailVerificationToken existing =
                EmailVerificationToken.create(
                        1L,
                        "tim@example.com",
                        "old-token",
                        Instant.now()
                );

        when(userRepository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(user));

        when(tokenRepository.findByUserEmail("tim@example.com"))
                .thenReturn(Optional.of(existing));

        service.resendVerificationEmail("tim@example.com");

        verify(tokenRepository).delete(existing);

        verify(tokenRepository)
                .save(any(EmailVerificationToken.class));

        verify(emailSender).sendVerificationEmail(
                eq("tim@example.com"),
                any()
        );
    }

    @Test
    void shouldCreateNewTokenWhenNoExistingTokenExists() {
        User user = createUser(false);

        when(userRepository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(user));

        when(tokenRepository.findByUserEmail("tim@example.com"))
                .thenReturn(Optional.empty());

        service.resendVerificationEmail("tim@example.com");

        verify(tokenRepository, never())
                .delete(any());

        verify(tokenRepository)
                .save(any(EmailVerificationToken.class));

        verify(emailSender).sendVerificationEmail(
                eq("tim@example.com"),
                any()
        );
    }

    @Test
    void shouldThrowWhenConfirmingUnknownToken() {
        when(tokenRepository.findByToken("bad-token"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirmToken("bad-token")
        );

        verify(tokenRepository, never())
                .save(any());

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowWhenConfirmingExpiredToken() {
        EmailVerificationToken token =
                EmailVerificationToken.create(
                        1L,
                        "tim@example.com",
                        "expired-token",
                        Instant.now().minusSeconds(60 * 60 * 24 * 2)
                );

        when(tokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(token));

        assertThrows(
                IllegalStateException.class,
                () -> service.confirmToken("expired-token")
        );

        verify(tokenRepository, never())
                .save(any());

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldConfirmTokenAndVerifyUser() {
        User user = createUser(false);

        EmailVerificationToken token =
                EmailVerificationToken.create(
                        1L,
                        "tim@example.com",
                        "valid-token",
                        Instant.now()
                );

        when(tokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(token));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        service.confirmToken("valid-token");

        assertTrue(user.isVerified());
        assertNotNull(token.getConfirmedAt());

        verify(tokenRepository).save(token);
        verify(userRepository).save(user);
    }

    @Test
    void shouldNotSaveUserAgainWhenAlreadyVerified() {
        User user = createUser(true);

        EmailVerificationToken token =
                EmailVerificationToken.create(
                        1L,
                        "tim@example.com",
                        "valid-token",
                        Instant.now()
                );

        when(tokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(token));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        service.confirmToken("valid-token");

        assertNotNull(token.getConfirmedAt());

        verify(tokenRepository).save(token);

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void shouldStillConfirmTokenWhenUserNoLongerExists() {
        EmailVerificationToken token =
                EmailVerificationToken.create(
                        1L,
                        "tim@example.com",
                        "valid-token",
                        Instant.now()
                );

        when(tokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(token));

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        service.confirmToken("valid-token");

        assertNotNull(token.getConfirmedAt());

        verify(tokenRepository).save(token);

        verify(userRepository, never())
                .save(any());
    }

    private User createUser(boolean verified) {
        return User.rehydrate(
                1L,
                "timcritt",
                "Tim",
                "Crittenden",
                "tim@example.com",
                Set.of(Role.STUDENT),
                PasswordHash.of("hashed-password"),
                verified
        );
    }
}