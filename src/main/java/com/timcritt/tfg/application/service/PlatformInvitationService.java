// java
package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.PasswordEncoderPort;
import com.timcritt.tfg.application.port.outbound.PlatformInvitationRepositoryPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.*;


import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class PlatformInvitationService {

    private final PasswordEncoderPort passwordEncoder;
    private final PlatformInvitationRepositoryPort platformInvitationRepository;
    private final EmailSenderPort emailSender;
    private final UserRepositoryPort userRepository;

    public PlatformInvitationService(PasswordEncoderPort passwordEncoder, PlatformInvitationRepositoryPort platformInvitationJpaRepository, EmailSenderPort emailSender, UserRepositoryPort userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.platformInvitationRepository = platformInvitationJpaRepository;
        this.emailSender = emailSender;
        this.userRepository = userRepository;
    }

    public void createAndSendPlatformInvitation(Long createdByUserId, String inviteeEmail, RoleType roleType) {

        // Create the new invitation
        PlatformInvitation platformInvitation = new PlatformInvitation();

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(60 * 60 * 24);
        String token = UUID.randomUUID().toString();

        platformInvitation.setCreatedByUserId(createdByUserId);
        platformInvitation.setEmailInvitee(inviteeEmail);
        platformInvitation.setCreatedAt(now);
        platformInvitation.setExpiresAt(expiresAt);
        platformInvitation.setPlatformInvitationStatus(PlatformInvitationStatus.PENDING);
        platformInvitation.setToken(token);
        platformInvitation.setRoleType(roleType);

        // Persist the new invitation
        platformInvitationRepository.save(platformInvitation);

        String link = "http://localhost:8082/api/auth/confirm-email?token=" + token;

        // Send email asynchronously so the HTTP request doesn't block on SMTP delays
        CompletableFuture.runAsync(() -> {

            try {
                emailSender.sendInvitationEmail(inviteeEmail, link);

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });


    }
    public void signUpWIthInvitationToken(String token, String username, String name, String surname, String password) {
        // Check that the invitation with that token exists
        Optional<PlatformInvitation> invitationOpt = platformInvitationRepository.findByToken(token);
        if (invitationOpt.isEmpty()) {
            throw new IllegalStateException("No platformInvitation found with token " + token);
        }

        PlatformInvitation invitation = invitationOpt.get();

        // Check if the token has expired
        Instant now = Instant.now();
        Instant expiresAt = invitation.getExpiresAt();
        if (now.isAfter(expiresAt)) {
            throw new IllegalStateException("PlatformInvitation token expired");
        }

        // check the token has state pending
        PlatformInvitationStatus platformInvitationStatus = invitation.getPlatformInvitationStatus();
        if (platformInvitationStatus != PlatformInvitationStatus.PENDING) {
            throw new IllegalStateException("PlatformInvitation status is: " + platformInvitationStatus + ". Cannot sign up");
        }

        // Retrieve the email from the invitation (use the same property name used by the setter)
        String inviteeEmail = invitation.getInviteeEmail();

        // Check for existing username/email to avoid DB constraint violations
        // (assumes userRepository has these lookup methods)
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalStateException("Username already exists: " + username);
        }
        if (userRepository.findByEmail(inviteeEmail).isPresent()) {
            throw new IllegalStateException("Email already registered: " + inviteeEmail);
        }

        // encode the password
        String passwordHash = passwordEncoder.encode(password);

        // Retrieve the roleType from the invitation and create role set
        RoleType roleType = invitation.getRoleType();
        Role role = new Role();
        role.setRoleType(roleType);
        Set<Role> roles = new java.util.HashSet<>(Set.of(role));

        // Create the user object and set the attributes
        User user = new User();
        user.setUsername(username);
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(inviteeEmail);
        user.setVerified(true);
        user.setRoles(roles);
        user.setPasswordHash(passwordHash);

        userRepository.save(user);

        // Mark invitation as accepted and persist to prevent reuse
        invitation.setPlatformInvitationStatus(PlatformInvitationStatus.ACCEPTED);
        platformInvitationRepository.save(invitation);
    }
}