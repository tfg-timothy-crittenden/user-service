package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.exception.AlreadyHasRoleException;
import com.timcritt.tfg.application.exception.InvitationExpiredException;
import com.timcritt.tfg.application.exception.InvitationNotFoundException;
import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.PasswordEncoderPort;
import com.timcritt.tfg.application.port.outbound.PlatformInvitationRepositoryPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.aggregate.platformInvitation.PlatformInvitation;
import com.timcritt.tfg.domain.model.aggregate.platformInvitation.PlatformInvitationStatus;
import com.timcritt.tfg.domain.model.aggregate.user.PasswordHash;
import com.timcritt.tfg.domain.model.aggregate.user.User;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlatformInvitationService {

    private final PasswordEncoderPort passwordEncoder;
    private final PlatformInvitationRepositoryPort platformInvitationRepository;
    private final EmailSenderPort emailSender;
    private final UserRepositoryPort userRepository;
    private final String invitationUrlTemplate;

    public PlatformInvitationService(
            PasswordEncoderPort passwordEncoder,
            PlatformInvitationRepositoryPort platformInvitationRepository,
            EmailSenderPort emailSender,
            UserRepositoryPort userRepository,
            String invitationUrlTemplate
    ) {
        this.passwordEncoder = passwordEncoder;
        this.platformInvitationRepository = platformInvitationRepository;
        this.emailSender = emailSender;
        this.userRepository = userRepository;
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

        /*
         * If the user already exists, an invitation isn't required.
         * Grant the requested role directly unless they already have it.
         */
        Optional<User> userOpt =
                userRepository.findByEmail(normalizedEmail);

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
        String token = UUID.randomUUID().toString();

        Optional<PlatformInvitation> existingInvitationOpt =
                platformInvitationRepository
                        .findByInviteeEmail(normalizedEmail);

        if (existingInvitationOpt.isPresent()) {
            PlatformInvitation existing =
                    existingInvitationOpt.get();

            /*
             * There is already an active pending invitation.
             * Reissue it with a fresh token and validity period.
             */
            if (existing.getPlatformInvitationStatus()
                    == PlatformInvitationStatus.PENDING
                    && !existing.isExpiredAt(now)) {

                String newToken =
                        UUID.randomUUID().toString();

                existing.reissueAt(
                        createdByUserId,
                        newToken,
                        role,
                        now
                );

                platformInvitationRepository.save(existing);

                sendInvitationEmailAsync(
                        normalizedEmail,
                        newToken
                );

                return;
            }

            /*
             * Reuse an expired, cancelled or previously accepted
             * invitation row by reissuing it.
             */
            existing.reissueAt(
                    createdByUserId,
                    token,
                    role,
                    now
            );

            platformInvitationRepository.save(existing);

            sendInvitationEmailAsync(
                    normalizedEmail,
                    token
            );

            return;
        }

        /*
         * No previous invitation exists.
         */
        PlatformInvitation invitation =
                PlatformInvitation.create(
                        createdByUserId,
                        normalizedEmail,
                        token,
                        role,
                        now
                );

        platformInvitationRepository.save(invitation);

        sendInvitationEmailAsync(
                normalizedEmail,
                token
        );
    }

    public void resendInvitation(Long invitationId) {
        PlatformInvitation invitation =
                platformInvitationRepository
                        .findByInvitationId(invitationId)
                        .orElseThrow(
                                () -> new InvitationNotFoundException(
                                        String.valueOf(invitationId)
                                )
                        );

        Instant now = Instant.now();
        String newToken = UUID.randomUUID().toString();

        invitation.reissueAt(
                invitation.getCreatedByUserId(),
                newToken,
                invitation.getRole(),
                now
        );

        platformInvitationRepository.save(invitation);

        sendInvitationEmailAsync(
                invitation.getInviteeEmail(),
                newToken
        );
    }

    public void signUpWithInvitationToken(
            String token,
            String username,
            String name,
            String surname,
            String password
    ) {
        PlatformInvitation invitation =
                platformInvitationRepository
                        .findByToken(token)
                        .orElseThrow(
                                () -> new InvitationNotFoundException(
                                        token
                                )
                        );

        Instant now = Instant.now();

        /*
         * Keep the application-specific exception exposed by
         * the use case, while the actual expiry calculation
         * belongs to the domain object.
         */
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

        /*
         * confirmAt() owns the PENDING -> ACCEPTED
         * state transition and sets confirmedAt.
         */
        invitation.confirmAt(now);

        platformInvitationRepository.save(invitation);
    }

    private void sendInvitationEmailAsync(
            String email,
            String token
    ) {
        String link =
                invitationUrlTemplate.replace(
                        "{token}",
                        token
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

