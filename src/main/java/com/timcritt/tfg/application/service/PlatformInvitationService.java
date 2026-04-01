package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.PasswordEncoderPort;
import com.timcritt.tfg.application.port.outbound.PlatformInvitationRepositoryPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.*;
import com.timcritt.tfg.application.exception.AlreadyHasRoleException;
import com.timcritt.tfg.application.exception.ActiveInvitationExistsException;
import com.timcritt.tfg.application.exception.InvitationNotFoundException;

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
        if (inviteeEmail == null) throw new IllegalArgumentException("inviteeEmail must not be null");

        String normalizedEmail = inviteeEmail.trim().toLowerCase();

        // 1) If user exists: if they already have the role -> exit. Otherwise assign and exit.
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isPresent()) {
            User existingUser = userOpt.get();
            boolean hasRole = existingUser.getRoles().stream()
                    .anyMatch(r -> r.getRoleType() == roleType);
            if (hasRole) {
                throw new AlreadyHasRoleException(normalizedEmail, roleType.name());

            }

            Role newRole = new Role();
            newRole.setRoleType(roleType);
            var roles = existingUser.getRoles();
            roles.add(newRole);
            existingUser.setRoles(roles);
            userRepository.save(existingUser);
            return; // role assigned, do not create invitation
        }

        // 2) No user found -> handle invitation
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(60 * 60 * 24); // 24h
        String token = UUID.randomUUID().toString();

        Optional<PlatformInvitation> existingInvOpt = platformInvitationRepository.findByInviteeEmail(normalizedEmail);
        if (existingInvOpt.isPresent()) {
            PlatformInvitation existing = existingInvOpt.get();

            // If pending and still active: resend (update token/expiry) and return
            if (existing.getPlatformInvitationStatus() == PlatformInvitationStatus.PENDING
                    && now.isBefore(existing.getExpiresAt())) {

                String newToken = UUID.randomUUID().toString();
                existing.setToken(newToken);
                existing.setCreatedAt(now);
                existing.setExpiresAt(expiresAt);

                try {
                    platformInvitationRepository.save(existing);
                } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                    throw new ActiveInvitationExistsException(normalizedEmail);
                }

                String link = "http://localhost:8082/api/auth/confirm-email?token=" + newToken;
                CompletableFuture.runAsync(() -> {
                    try { emailSender.sendInvitationEmail(normalizedEmail, link); }
                    catch (Exception e) { throw new RuntimeException(e); }
                });
                return;
            }

            // Otherwise reuse/update the existing row (make it pending with new token)
            existing.setCreatedByUserId(createdByUserId);
            existing.setCreatedAt(now);
            existing.setExpiresAt(expiresAt);
            existing.setPlatformInvitationStatus(PlatformInvitationStatus.PENDING);
            existing.setToken(token);
            existing.setRoleType(roleType);
            existing.setConfirmedAt(null);

            try {
                platformInvitationRepository.save(existing);
            } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                throw new ActiveInvitationExistsException(normalizedEmail);
            }

            String link = "http://localhost:8082/api/auth/confirm-email?token=" + token;
            CompletableFuture.runAsync(() -> {
                try { emailSender.sendInvitationEmail(normalizedEmail, link); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
            return;
        }

        // 3) No existing invitation -> create new and send
        PlatformInvitation platformInvitation = new PlatformInvitation();
        platformInvitation.setCreatedByUserId(createdByUserId);
        platformInvitation.setEmailInvitee(normalizedEmail);
        platformInvitation.setCreatedAt(now);
        platformInvitation.setExpiresAt(expiresAt);
        platformInvitation.setPlatformInvitationStatus(PlatformInvitationStatus.PENDING);
        platformInvitation.setToken(token);
        platformInvitation.setRoleType(roleType);

        try {
            platformInvitationRepository.save(platformInvitation);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // concurrent insert created the row between check and insert
            throw new ActiveInvitationExistsException(normalizedEmail);
        }

        String link = "http://localhost:8082/api/auth/confirm-email?token=" + token;
        CompletableFuture.runAsync(() -> {
            try { emailSender.sendInvitationEmail(normalizedEmail, link); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
    }
    public void signUpWIthInvitationToken(String token, String username, String name, String surname, String password) {
        // Check that the invitation with that token exists
        Optional<PlatformInvitation> invitationOpt = platformInvitationRepository.findByToken(token);
        if (invitationOpt.isEmpty()) {
            throw new InvitationNotFoundException(token);
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
        Instant confirmedAt = Instant.now();
        invitation.setConfirmedAt(confirmedAt);
        invitation.setPlatformInvitationStatus(PlatformInvitationStatus.ACCEPTED);
        platformInvitationRepository.save(invitation);
    }
}

