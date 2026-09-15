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
import com.timcritt.tfg.domain.model.aggregate.user.PasswordHash;
import com.timcritt.tfg.domain.model.aggregate.user.User;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PlatformInvitationService {

    private final PasswordEncoderPort passwordEncoder;
    private final PlatformInvitationRepositoryPort platformInvitationRepository;
    private final EmailSenderPort emailSender;
    private final UserRepositoryPort userRepository;
    private final TokenGeneratorPort tokenGenerator;
    private final TokenHasherPort tokenHasher;
    private final String invitationUrlTemplate;

    public PlatformInvitationService(
            PasswordEncoderPort passwordEncoder,
            PlatformInvitationRepositoryPort platformInvitationRepository,
            EmailSenderPort emailSender,
            UserRepositoryPort userRepository,
            TokenGeneratorPort tokenGenerator,
            TokenHasherPort tokenHasher,
            String invitationUrlTemplate
    ) {
        this.passwordEncoder = passwordEncoder;
        this.platformInvitationRepository = platformInvitationRepository;
        this.emailSender = emailSender;
        this.userRepository = userRepository;
        this.tokenGenerator = tokenGenerator;
        this.tokenHasher = tokenHasher;
        this.invitationUrlTemplate = invitationUrlTemplate;
    }

    public List<PlatformInvitation> findPendingByRole(Role role) {
        return platformInvitationRepository.findPendingByRole(role);
    }

    public BatchDeleteResult deleteAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new BatchDeleteResult(
                    List.of(),
                    List.of()
            );
        }

        List<Long> existingIds =
                platformInvitationRepository
                        .findAllByIds(ids)
                        .stream()
                        .map(PlatformInvitation::getId)
                        .toList();

        List<Long> notFound =
                ids.stream()
                        .filter(id -> !existingIds.contains(id))
                        .toList();

        if (!existingIds.isEmpty()) {
            platformInvitationRepository.deleteAllByIds(
                    existingIds
            );
        }

        return new BatchDeleteResult(
                existingIds,
                notFound
        );
    }

    public void createAndSendPlatformInvitation(
            Long createdByUserId,
            String inviteeEmail,
            Role role
    ) {
        if (inviteeEmail == null || inviteeEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "inviteeEmail must not be blank"
            );
        }

        String normalizedEmail =
                inviteeEmail
                        .trim()
                        .toLowerCase(Locale.ROOT);

        Optional<User> userOpt =
                userRepository.findByEmail(normalizedEmail);

        /*
         * Existing users don't need an invitation.
         * Grant the role directly.
         */
        if (userOpt.isPresent()) {
            User existingUser = userOpt.get();

            if (existingUser.hasRole(role)) {
                throw new AlreadyHasRoleException(
                        normalizedEmail,
                        role.name()
                );
            }

            existingUser.grantRole(role);
            userRepository.save(existingUser);

            return;
        }

        Instant now = Instant.now();

        Optional<PlatformInvitation> existingInvitationOpt =
                platformInvitationRepository
                        .findByInviteeEmail(normalizedEmail);

        if (existingInvitationOpt.isPresent()) {
            PlatformInvitation existing =
                    existingInvitationOpt.get();

            /*
             * Whether currently active, expired, cancelled or accepted,
             * reissuing requires a brand-new raw token.
             */
            String rawToken = tokenGenerator.generate();
            String tokenHash = tokenHasher.hash(rawToken);

            existing.reissueAt(
                    createdByUserId,
                    tokenHash,
                    role,
                    now
            );

            platformInvitationRepository.save(existing);

            sendInvitationEmailAsync(
                    normalizedEmail,
                    rawToken
            );

            return;
        }

        /*
         * Brand-new invitation.
         */
        String rawToken = tokenGenerator.generate();
        String tokenHash = tokenHasher.hash(rawToken);

        PlatformInvitation invitation =
                PlatformInvitation.create(
                        createdByUserId,
                        normalizedEmail,
                        tokenHash,
                        role,
                        now
                );

        platformInvitationRepository.save(invitation);

        sendInvitationEmailAsync(
                normalizedEmail,
                rawToken
        );
    }

    public void resendInvitation(Long invitationId) {
        PlatformInvitation invitation =
                platformInvitationRepository
                        .findByInvitationId(invitationId)
                        .orElseThrow(
                                () -> new InvitationNotFoundException()
                        );

        Instant now = Instant.now();

        String rawToken = tokenGenerator.generate();
        String tokenHash = tokenHasher.hash(rawToken);

        invitation.reissueAt(
                invitation.getCreatedByUserId(),
                tokenHash,
                invitation.getRole(),
                now
        );

        platformInvitationRepository.save(invitation);

        sendInvitationEmailAsync(
                invitation.getInviteeEmail(),
                rawToken
        );
    }

    public void signUpWithInvitationToken(
            String rawToken,
            String username,
            String name,
            String surname,
            String password
    ) {
        String tokenHash = tokenHasher.hash(rawToken);

        PlatformInvitation invitation =
                platformInvitationRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(
                                () -> new InvitationNotFoundException()
                        );

        Instant now = Instant.now();

        if (invitation.isExpiredAt(now)) {
            throw new InvitationExpiredException();
        }

        String inviteeEmail =
                invitation.getInviteeEmail();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalStateException(
                    "Username already exists: " + username
            );
        }

        if (userRepository.findByEmail(inviteeEmail).isPresent()) {
            throw new IllegalStateException(
                    "Email already registered: " + inviteeEmail
            );
        }

        PasswordHash passwordHash =
                PasswordHash.of(
                        passwordEncoder.encode(password)
                );

        User user =
                User.createFromInvitation(
                        username,
                        name,
                        surname,
                        inviteeEmail,
                        passwordHash,
                        invitation.getRole()
                );

        userRepository.save(user);

        invitation.confirmAt(now);

        platformInvitationRepository.save(invitation);
    }

    private void sendInvitationEmailAsync(
            String email,
            String rawToken
    ) {
        String link =
                invitationUrlTemplate.replace(
                        "{token}",
                        rawToken
                );

        CompletableFuture.runAsync(() -> {
            try {
                emailSender.sendInvitationEmail(
                        email,
                        link
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}