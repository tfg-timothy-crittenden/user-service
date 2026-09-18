package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.exception.AlreadyHasRoleException;
import com.timcritt.tfg.application.exception.InvitationExpiredException;
import com.timcritt.tfg.application.exception.InvitationNotFoundException;
import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.PasswordEncoderPort;
import com.timcritt.tfg.application.port.outbound.PlatformInvitationRepositoryPort;
import com.timcritt.tfg.application.port.outbound.TokenGeneratorPort;
import com.timcritt.tfg.application.port.outbound.TokenHasherPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.aggregate.platformInvitation.PlatformInvitation;
import com.timcritt.tfg.domain.model.aggregate.platformInvitation.PlatformInvitationStatus;
import com.timcritt.tfg.domain.model.aggregate.user.PasswordHash;
import com.timcritt.tfg.domain.model.aggregate.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformInvitationServiceTest {

    private static final String INVITATION_URL_TEMPLATE =
            "http://localhost:5173/signup-with-invitation?token={token}";

    private static final String RAW_TOKEN = "raw-token-123";
    private static final String TOKEN_HASH = "hashed-token-123";

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private PlatformInvitationRepositoryPort platformInvitationRepository;

    @Mock
    private EmailSenderPort emailSender;

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private TokenGeneratorPort tokenGenerator;

    @Mock
    private TokenHasherPort tokenHasher;

    private PlatformInvitationService service;

    @BeforeEach
    void setUp() {
        service = new PlatformInvitationService(
                passwordEncoder,
                platformInvitationRepository,
                emailSender,
                userRepository,
                tokenGenerator,
                tokenHasher,
                INVITATION_URL_TEMPLATE
        );
    }

    // ========================================================================
    // findPendingByRole
    // ========================================================================

    @Test
    void findPendingByRoleShouldDelegateToRepository() {
        PlatformInvitation invitation =
                createPendingInvitation();

        when(platformInvitationRepository.findPendingByRole(Role.TEACHER))
                .thenReturn(List.of(invitation));

        List<PlatformInvitation> result =
                service.findPendingByRole(Role.TEACHER);

        assertEquals(List.of(invitation), result);

        verify(platformInvitationRepository)
                .findPendingByRole(Role.TEACHER);
    }

    // ========================================================================
    // deleteAllByIds
    // ========================================================================

    @Test
    void deleteAllByIdsShouldReturnEmptyResultWhenIdsAreNull() {
        BatchDeleteResult result =
                service.deleteAllByIds(null);

        assertTrue(result.deleted().isEmpty());
        assertTrue(result.notFound().isEmpty());

        verifyNoInteractions(platformInvitationRepository);
    }

    @Test
    void deleteAllByIdsShouldReturnEmptyResultWhenIdsAreEmpty() {
        BatchDeleteResult result =
                service.deleteAllByIds(List.of());

        assertTrue(result.deleted().isEmpty());
        assertTrue(result.notFound().isEmpty());

        verifyNoInteractions(platformInvitationRepository);
    }

    @Test
    void deleteAllByIdsShouldDeleteExistingIdsAndReturnMissingIds() {
        PlatformInvitation first =
                rehydrateInvitation(
                        1L,
                        PlatformInvitationStatus.PENDING,
                        false
                );

        PlatformInvitation third =
                rehydrateInvitation(
                        3L,
                        PlatformInvitationStatus.PENDING,
                        false
                );

        when(platformInvitationRepository.findAllByIds(
                List.of(1L, 2L, 3L)
        )).thenReturn(
                List.of(first, third)
        );

        BatchDeleteResult result =
                service.deleteAllByIds(
                        List.of(1L, 2L, 3L)
                );

        assertEquals(
                List.of(1L, 3L),
                result.deleted()
        );

        assertEquals(
                List.of(2L),
                result.notFound()
        );

        verify(platformInvitationRepository)
                .deleteAllByIds(
                        List.of(1L, 3L)
                );
    }

    @Test
    void deleteAllByIdsShouldNotCallDeleteWhenNoIdsExist() {
        when(platformInvitationRepository.findAllByIds(
                List.of(1L, 2L)
        )).thenReturn(List.of());

        BatchDeleteResult result =
                service.deleteAllByIds(
                        List.of(1L, 2L)
                );

        assertTrue(result.deleted().isEmpty());
        assertEquals(
                List.of(1L, 2L),
                result.notFound()
        );

        verify(platformInvitationRepository, never())
                .deleteAllByIds(any());
    }

    // ========================================================================
    // createAndSendPlatformInvitation
    // ========================================================================

    @Test
    void createInvitationShouldRejectNullEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createAndSendPlatformInvitation(
                        10L,
                        null,
                        Role.TEACHER
                )
        );

        verifyNoInteractions(
                userRepository,
                platformInvitationRepository,
                tokenGenerator,
                tokenHasher,
                emailSender
        );
    }

    @Test
    void createInvitationShouldRejectBlankEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createAndSendPlatformInvitation(
                        10L,
                        "   ",
                        Role.TEACHER
                )
        );

        verifyNoInteractions(
                userRepository,
                platformInvitationRepository,
                tokenGenerator,
                tokenHasher,
                emailSender
        );
    }

    @Test
    void createInvitationShouldNormalizeEmailBeforeLookup() {
        when(userRepository.findByEmail(
                "teacher@example.com"
        )).thenReturn(Optional.empty());

        when(platformInvitationRepository.findByInviteeEmail(
                "teacher@example.com"
        )).thenReturn(Optional.empty());

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        service.createAndSendPlatformInvitation(
                10L,
                "  Teacher@Example.COM  ",
                Role.TEACHER
        );

        verify(userRepository)
                .findByEmail("teacher@example.com");

        verify(platformInvitationRepository)
                .findByInviteeEmail("teacher@example.com");
    }

    @Test
    void createInvitationShouldThrowWhenExistingUserAlreadyHasRole() {
        User existingUser =
                createExistingUser(
                        Set.of(
                                Role.STUDENT,
                                Role.TEACHER
                        )
                );

        when(userRepository.findByEmail(
                "teacher@example.com"
        )).thenReturn(
                Optional.of(existingUser)
        );

        assertThrows(
                AlreadyHasRoleException.class,
                () -> service.createAndSendPlatformInvitation(
                        10L,
                        "teacher@example.com",
                        Role.TEACHER
                )
        );

        verify(userRepository, never())
                .save(any());

        verifyNoInteractions(
                platformInvitationRepository,
                tokenGenerator,
                tokenHasher,
                emailSender
        );
    }

    @Test
    void createInvitationShouldGrantRoleDirectlyWhenUserAlreadyExists() {
        User existingUser =
                createExistingUser(
                        Set.of(Role.STUDENT)
                );

        when(userRepository.findByEmail(
                "teacher@example.com"
        )).thenReturn(
                Optional.of(existingUser)
        );

        service.createAndSendPlatformInvitation(
                10L,
                "teacher@example.com",
                Role.TEACHER
        );

        assertTrue(
                existingUser.hasRole(Role.TEACHER)
        );

        verify(userRepository)
                .save(existingUser);

        verifyNoInteractions(
                platformInvitationRepository,
                tokenGenerator,
                tokenHasher,
                emailSender
        );
    }

    @Test
    void createInvitationShouldStoreHashAndEmailRawToken() {
        when(userRepository.findByEmail(
                "teacher@example.com"
        )).thenReturn(Optional.empty());

        when(platformInvitationRepository.findByInviteeEmail(
                "teacher@example.com"
        )).thenReturn(Optional.empty());

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        service.createAndSendPlatformInvitation(
                10L,
                "teacher@example.com",
                Role.TEACHER
        );

        ArgumentCaptor<PlatformInvitation> captor =
                ArgumentCaptor.forClass(
                        PlatformInvitation.class
                );

        verify(platformInvitationRepository)
                .save(captor.capture());

        PlatformInvitation saved =
                captor.getValue();

        assertEquals(
                TOKEN_HASH,
                saved.getTokenHash()
        );

        assertNotEquals(
                RAW_TOKEN,
                saved.getTokenHash()
        );

        assertEquals(
                "teacher@example.com",
                saved.getInviteeEmail()
        );

        assertEquals(
                10L,
                saved.getCreatedByUserId()
        );

        assertEquals(
                Role.TEACHER,
                saved.getRole()
        );

        assertEquals(
                PlatformInvitationStatus.PENDING,
                saved.getPlatformInvitationStatus()
        );

        verify(tokenGenerator)
                .generate();

        verify(tokenHasher)
                .hash(RAW_TOKEN);

        verify(
                emailSender,
                timeout(1000)
        ).sendInvitationEmail(
                "teacher@example.com",
                "http://localhost:5173/"
                        + "signup-with-invitation"
                        + "?token="
                        + RAW_TOKEN
        );
    }

    @Test
    void createInvitationShouldNeverEmailTokenHash() {
        when(userRepository.findByEmail(
                "teacher@example.com"
        )).thenReturn(Optional.empty());

        when(platformInvitationRepository.findByInviteeEmail(
                "teacher@example.com"
        )).thenReturn(Optional.empty());

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        service.createAndSendPlatformInvitation(
                10L,
                "teacher@example.com",
                Role.TEACHER
        );

        verify(
                emailSender,
                timeout(1000)
        ).sendInvitationEmail(
                "teacher@example.com",
                INVITATION_URL_TEMPLATE.replace(
                        "{token}",
                        RAW_TOKEN
                )
        );

        verify(
                emailSender,
                never()
        ).sendInvitationEmail(
                "teacher@example.com",
                INVITATION_URL_TEMPLATE.replace(
                        "{token}",
                        TOKEN_HASH
                )
        );
    }

    @Test
    void createInvitationShouldReissueExistingInvitationWithNewHash() {
        PlatformInvitation existing =
                rehydrateInvitation(
                        25L,
                        PlatformInvitationStatus.PENDING,
                        false
                );

        assertEquals(
                "old-token-hash",
                existing.getTokenHash()
        );

        when(userRepository.findByEmail(
                "teacher@example.com"
        )).thenReturn(Optional.empty());

        when(platformInvitationRepository.findByInviteeEmail(
                "teacher@example.com"
        )).thenReturn(
                Optional.of(existing)
        );

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        service.createAndSendPlatformInvitation(
                99L,
                "teacher@example.com",
                Role.ADMIN
        );

        assertEquals(
                TOKEN_HASH,
                existing.getTokenHash()
        );

        assertEquals(
                99L,
                existing.getCreatedByUserId()
        );

        assertEquals(
                Role.ADMIN,
                existing.getRole()
        );

        assertEquals(
                PlatformInvitationStatus.PENDING,
                existing.getPlatformInvitationStatus()
        );

        assertNull(
                existing.getConfirmedAt()
        );

        verify(platformInvitationRepository)
                .save(existing);

        verify(
                emailSender,
                timeout(1000)
        ).sendInvitationEmail(
                "teacher@example.com",
                INVITATION_URL_TEMPLATE.replace(
                        "{token}",
                        RAW_TOKEN
                )
        );
    }

    @Test
    void createInvitationShouldReissueExpiredInvitation() {
        PlatformInvitation expired =
                rehydrateInvitation(
                        25L,
                        PlatformInvitationStatus.PENDING,
                        true
                );

        assertTrue(
                expired.isExpiredAt(Instant.now())
        );

        when(userRepository.findByEmail(
                "teacher@example.com"
        )).thenReturn(Optional.empty());

        when(platformInvitationRepository.findByInviteeEmail(
                "teacher@example.com"
        )).thenReturn(
                Optional.of(expired)
        );

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        service.createAndSendPlatformInvitation(
                99L,
                "teacher@example.com",
                Role.TEACHER
        );

        assertEquals(
                TOKEN_HASH,
                expired.getTokenHash()
        );

        assertEquals(
                PlatformInvitationStatus.PENDING,
                expired.getPlatformInvitationStatus()
        );

        assertFalse(
                expired.isExpiredAt(Instant.now())
        );

        verify(platformInvitationRepository)
                .save(expired);
    }

    @Test
    void createInvitationShouldReissueAcceptedInvitationAsPending() {
        PlatformInvitation accepted =
                rehydrateInvitation(
                        25L,
                        PlatformInvitationStatus.ACCEPTED,
                        false
                );

        assertNotNull(
                accepted.getConfirmedAt()
        );

        when(userRepository.findByEmail(
                "teacher@example.com"
        )).thenReturn(Optional.empty());

        when(platformInvitationRepository.findByInviteeEmail(
                "teacher@example.com"
        )).thenReturn(
                Optional.of(accepted)
        );

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        service.createAndSendPlatformInvitation(
                99L,
                "teacher@example.com",
                Role.TEACHER
        );

        assertEquals(
                PlatformInvitationStatus.PENDING,
                accepted.getPlatformInvitationStatus()
        );

        assertNull(
                accepted.getConfirmedAt()
        );

        assertEquals(
                TOKEN_HASH,
                accepted.getTokenHash()
        );
    }

    @Test
    void createInvitationShouldReissueCancelledInvitationAsPending() {
        PlatformInvitation cancelled =
                rehydrateInvitation(
                        25L,
                        PlatformInvitationStatus.CANCELLED,
                        false
                );

        when(userRepository.findByEmail(
                "teacher@example.com"
        )).thenReturn(Optional.empty());

        when(platformInvitationRepository.findByInviteeEmail(
                "teacher@example.com"
        )).thenReturn(
                Optional.of(cancelled)
        );

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        service.createAndSendPlatformInvitation(
                99L,
                "teacher@example.com",
                Role.TEACHER
        );

        assertEquals(
                PlatformInvitationStatus.PENDING,
                cancelled.getPlatformInvitationStatus()
        );

        assertEquals(
                TOKEN_HASH,
                cancelled.getTokenHash()
        );
    }

    // ========================================================================
    // resendInvitation
    // ========================================================================

    @Test
    void resendInvitationShouldThrowWhenInvitationDoesNotExist() {
        when(platformInvitationRepository.findByInvitationId(50L))
                .thenReturn(Optional.empty());

        assertThrows(
                InvitationNotFoundException.class,
                () -> service.resendInvitation(50L)
        );

        verifyNoInteractions(
                tokenGenerator,
                tokenHasher,
                emailSender
        );

        verify(platformInvitationRepository, never())
                .save(any());
    }

    @Test
    void resendInvitationShouldStoreNewHashAndEmailNewRawToken() {
        PlatformInvitation invitation =
                rehydrateInvitation(
                        50L,
                        PlatformInvitationStatus.PENDING,
                        false
                );

        when(platformInvitationRepository.findByInvitationId(50L))
                .thenReturn(
                        Optional.of(invitation)
                );

        when(tokenGenerator.generate())
                .thenReturn(RAW_TOKEN);

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        service.resendInvitation(50L);

        assertEquals(
                TOKEN_HASH,
                invitation.getTokenHash()
        );

        assertNotEquals(
                RAW_TOKEN,
                invitation.getTokenHash()
        );

        assertEquals(
                PlatformInvitationStatus.PENDING,
                invitation.getPlatformInvitationStatus()
        );

        verify(platformInvitationRepository)
                .save(invitation);

        verify(tokenHasher)
                .hash(RAW_TOKEN);

        verify(
                emailSender,
                timeout(1000)
        ).sendInvitationEmail(
                invitation.getInviteeEmail(),
                INVITATION_URL_TEMPLATE.replace(
                        "{token}",
                        RAW_TOKEN
                )
        );
    }

    // ========================================================================
    // signUpWithInvitationToken
    // ========================================================================

    @Test
    void signupShouldHashRawTokenBeforeRepositoryLookup() {
        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(platformInvitationRepository.findByTokenHash(
                TOKEN_HASH
        )).thenReturn(Optional.empty());

        assertThrows(
                InvitationNotFoundException.class,
                () -> service.signUpWithInvitationToken(
                        RAW_TOKEN,
                        "new-user",
                        "Tim",
                        "Smith",
                        "password"
                )
        );

        verify(tokenHasher)
                .hash(RAW_TOKEN);

        verify(platformInvitationRepository)
                .findByTokenHash(TOKEN_HASH);

        verify(platformInvitationRepository, never())
                .findByTokenHash(RAW_TOKEN);
    }

    @Test
    void signupShouldThrowWhenInvitationDoesNotExist() {
        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(platformInvitationRepository.findByTokenHash(
                TOKEN_HASH
        )).thenReturn(Optional.empty());

        assertThrows(
                InvitationNotFoundException.class,
                () -> service.signUpWithInvitationToken(
                        RAW_TOKEN,
                        "new-user",
                        "Tim",
                        "Smith",
                        "password"
                )
        );

        verifyNoInteractions(
                passwordEncoder
        );

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void signupShouldThrowWhenInvitationIsExpired() {
        PlatformInvitation expired =
                rehydrateInvitation(
                        50L,
                        PlatformInvitationStatus.PENDING,
                        true
                );

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(platformInvitationRepository.findByTokenHash(
                TOKEN_HASH
        )).thenReturn(
                Optional.of(expired)
        );

        assertThrows(
                InvitationExpiredException.class,
                () -> service.signUpWithInvitationToken(
                        RAW_TOKEN,
                        "new-user",
                        "Tim",
                        "Smith",
                        "password"
                )
        );

        verify(userRepository, never())
                .findByUsername(anyString());

        verify(userRepository, never())
                .save(any());

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void signupShouldRejectExistingUsername() {
        PlatformInvitation invitation =
                rehydrateInvitation(
                        50L,
                        PlatformInvitationStatus.PENDING,
                        false
                );

        User existingUser =
                createExistingUser(
                        Set.of(Role.STUDENT)
                );

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(platformInvitationRepository.findByTokenHash(
                TOKEN_HASH
        )).thenReturn(
                Optional.of(invitation)
        );

        when(userRepository.findByUsername(
                "existing-user"
        )).thenReturn(
                Optional.of(existingUser)
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.signUpWithInvitationToken(
                        RAW_TOKEN,
                        "existing-user",
                        "Tim",
                        "Smith",
                        "password"
                )
        );

        verify(userRepository, never())
                .save(any());

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void signupShouldRejectAlreadyRegisteredEmail() {
        PlatformInvitation invitation =
                rehydrateInvitation(
                        50L,
                        PlatformInvitationStatus.PENDING,
                        false
                );

        User existingUser =
                createExistingUser(
                        Set.of(Role.STUDENT)
                );

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(platformInvitationRepository.findByTokenHash(
                TOKEN_HASH
        )).thenReturn(
                Optional.of(invitation)
        );

        when(userRepository.findByUsername(
                "new-user"
        )).thenReturn(Optional.empty());

        when(userRepository.findByEmail(
                invitation.getInviteeEmail()
        )).thenReturn(
                Optional.of(existingUser)
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.signUpWithInvitationToken(
                        RAW_TOKEN,
                        "new-user",
                        "Tim",
                        "Smith",
                        "password"
                )
        );

        verify(userRepository, never())
                .save(any());

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void signupShouldCreateUserAndAcceptInvitation() {
        PlatformInvitation invitation =
                rehydrateInvitation(
                        50L,
                        PlatformInvitationStatus.PENDING,
                        false
                );

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(platformInvitationRepository.findByTokenHash(
                TOKEN_HASH
        )).thenReturn(
                Optional.of(invitation)
        );

        when(userRepository.findByUsername(
                "new-user"
        )).thenReturn(Optional.empty());

        when(userRepository.findByEmail(
                invitation.getInviteeEmail()
        )).thenReturn(Optional.empty());

        when(passwordEncoder.encode(
                "raw-password"
        )).thenReturn(
                "encoded-password"
        );

        service.signUpWithInvitationToken(
                RAW_TOKEN,
                "new-user",
                "Tim",
                "Smith",
                "raw-password"
        );

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(userCaptor.capture());

        User savedUser =
                userCaptor.getValue();

        assertNull(
                savedUser.getId()
        );

        assertEquals(
                "new-user",
                savedUser.getUsername()
        );

        assertEquals(
                "Tim",
                savedUser.getName()
        );

        assertEquals(
                "Smith",
                savedUser.getSurname()
        );

        assertEquals(
                invitation.getInviteeEmail(),
                savedUser.getEmail()
        );

        assertEquals(
                Set.of(Role.TEACHER),
                savedUser.getRoles()
        );

        assertEquals(
                PasswordHash.of("encoded-password"),
                savedUser.getPasswordHash()
        );

        assertTrue(
                savedUser.isVerified()
        );

        assertEquals(
                PlatformInvitationStatus.ACCEPTED,
                invitation.getPlatformInvitationStatus()
        );

        assertNotNull(
                invitation.getConfirmedAt()
        );

        verify(platformInvitationRepository)
                .save(invitation);

        verify(passwordEncoder)
                .encode("raw-password");
    }

    @Test
    void signupShouldNotGenerateANewToken() {
        PlatformInvitation invitation =
                rehydrateInvitation(
                        50L,
                        PlatformInvitationStatus.PENDING,
                        false
                );

        when(tokenHasher.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(platformInvitationRepository.findByTokenHash(
                TOKEN_HASH
        )).thenReturn(
                Optional.of(invitation)
        );

        when(userRepository.findByUsername(
                "new-user"
        )).thenReturn(Optional.empty());

        when(userRepository.findByEmail(
                invitation.getInviteeEmail()
        )).thenReturn(Optional.empty());

        when(passwordEncoder.encode(
                "password"
        )).thenReturn(
                "encoded-password"
        );

        service.signUpWithInvitationToken(
                RAW_TOKEN,
                "new-user",
                "Tim",
                "Smith",
                "password"
        );

        verifyNoInteractions(tokenGenerator);
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private PlatformInvitation createPendingInvitation() {
        Instant now =
                Instant.now();

        return PlatformInvitation.rehydrate(
                1L,
                10L,
                "teacher@example.com",
                "old-token-hash",
                Role.TEACHER,
                now.minus(1, ChronoUnit.HOURS),
                now.plus(23, ChronoUnit.HOURS),
                null,
                PlatformInvitationStatus.PENDING
        );
    }

    private PlatformInvitation rehydrateInvitation(
            Long id,
            PlatformInvitationStatus status,
            boolean expired
    ) {
        Instant now =
                Instant.now();

        Instant createdAt;
        Instant expiresAt;

        if (expired) {
            createdAt =
                    now.minus(2, ChronoUnit.DAYS);

            expiresAt =
                    now.minus(1, ChronoUnit.DAYS);
        } else {
            createdAt =
                    now.minus(1, ChronoUnit.HOURS);

            expiresAt =
                    now.plus(23, ChronoUnit.HOURS);
        }

        Instant confirmedAt =
                status == PlatformInvitationStatus.ACCEPTED
                        ? now.minus(30, ChronoUnit.MINUTES)
                        : null;

        return PlatformInvitation.rehydrate(
                id,
                10L,
                "teacher@example.com",
                "old-token-hash",
                Role.TEACHER,
                createdAt,
                expiresAt,
                confirmedAt,
                status
        );
    }

    private User createExistingUser(
            Set<Role> roles
    ) {
        return User.rehydrate(
                100L,
                0L,
                "existing-user",
                "Existing",
                "User",
                "teacher@example.com",
                roles,
                PasswordHash.of("existing-password-hash"),
                true
        );
    }
}