package com.timcritt.tfg.domain.model.aggregate.platformInvitation;

import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.ValidityPeriod;

import java.time.Duration;
import java.time.Instant;

public class PlatformInvitation {

    private static final Duration VALIDITY_DURATION = Duration.ofDays(1);

    private final Long id;
    private Long createdByUserId;
    private final String inviteeEmail;
    private String token;
    private Role role;
    private ValidityPeriod validityPeriod;

    private Instant confirmedAt;
    private PlatformInvitationStatus invitationStatus;

    private PlatformInvitation(
            Long id,
            Long createdByUserId,
            String inviteeEmail,
            String token,
            Role role,
            ValidityPeriod validityPeriod,
            Instant confirmedAt,
            PlatformInvitationStatus invitationStatus
    ) {
        requireNotNull(createdByUserId, "createdByUserId");
        requireNonBlank(inviteeEmail, "inviteeEmail");
        requireNonBlank(token, "token");
        requireNotNull(role, "role");
        requireNotNull(validityPeriod, "validityPeriod");
        requireNotNull(invitationStatus, "invitationStatus");

        this.id = id;
        this.createdByUserId = createdByUserId;
        this.inviteeEmail = inviteeEmail;
        this.token = token;
        this.role = role;
        this.validityPeriod = validityPeriod;
        this.confirmedAt = confirmedAt;
        this.invitationStatus = invitationStatus;
    }

    //############################################ Static Factory Methods #############################################

    public static PlatformInvitation create(
            Long createdByUserId,
            String inviteeEmail,
            String token,
            Role role,
            Instant createdAt
    ) {
        return new PlatformInvitation(
                null,
                createdByUserId,
                inviteeEmail,
                token,
                role,
                ValidityPeriod.startingAt(
                        createdAt,
                        VALIDITY_DURATION
                ),
                null,
                PlatformInvitationStatus.PENDING
        );
    }

    public static PlatformInvitation rehydrate(
            Long id,
            Long createdByUserId,
            String inviteeEmail,
            String token,
            Role role,
            Instant createdAt,
            Instant expiresAt,
            Instant confirmedAt,
            PlatformInvitationStatus invitationStatus
    ) {
        requireNotNull(id, "id");

        return new PlatformInvitation(
                id,
                createdByUserId,
                inviteeEmail,
                token,
                role,
                ValidityPeriod.between(
                        createdAt,
                        expiresAt
                ),
                confirmedAt,
                invitationStatus
        );
    }

    //############################################ Getters ##################################################

    public Long getId() {
        return id;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public String getToken() {
        return token;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return validityPeriod.createdAt();
    }

    public Instant getExpiresAt() {
        return validityPeriod.expiresAt();
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public PlatformInvitationStatus getPlatformInvitationStatus() {
        return invitationStatus;
    }

    //############################################ Business Logic ##################################################

    public boolean isExpiredAt(Instant now) {
        return validityPeriod.isExpiredAt(now);
    }

    public void confirmAt(Instant now) {
        if (invitationStatus != PlatformInvitationStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending invitations can be confirmed"
            );
        }

        if (isExpiredAt(now)) {
            throw new IllegalStateException(
                    "Cannot confirm an expired invitation"
            );
        }

        confirmedAt = now;
        invitationStatus = PlatformInvitationStatus.ACCEPTED;
    }

    public void cancelAt(Instant now) {
        if (invitationStatus != PlatformInvitationStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending invitations can be cancelled"
            );
        }

        if (isExpiredAt(now)) {
            throw new IllegalStateException(
                    "Cannot cancel an expired invitation"
            );
        }

        invitationStatus = PlatformInvitationStatus.CANCELLED;
    }

    public void reissueAt(
            Long createdByUserId,
            String newToken,
            Role role,
            Instant now
    ) {
        requireNotNull(createdByUserId, "createdByUserId");
        requireNonBlank(newToken, "newToken");
        requireNotNull(role, "role");

        this.createdByUserId = createdByUserId;
        this.token = newToken;
        this.role = role;
        this.validityPeriod =
                ValidityPeriod.startingAt(
                        now,
                        VALIDITY_DURATION
                );

        this.confirmedAt = null;
        this.invitationStatus =
                PlatformInvitationStatus.PENDING;
    }

    //############################################ Helpers ##################################################

    private static void requireNonBlank(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }
    }

    private static void requireNotNull(
            Object value,
            String fieldName
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null"
            );
        }
    }
}

