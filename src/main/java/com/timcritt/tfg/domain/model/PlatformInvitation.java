package com.timcritt.tfg.domain.model;

import java.time.Instant;
import com.timcritt.tfg.domain.model.PlatformInvitationStatus;

public class PlatformInvitation {

    private Long id;
    private Long createdByUserId; //Who sent the invite
    private String inviteeEmail;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant confirmedAt;
    private PlatformInvitationStatus invitationStatus;
    private String token;
    private RoleType roleType;

    public PlatformInvitation() {

    }

    public PlatformInvitation(Long id, String email, String inviteeEmail, String token, RoleType roleType, Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.inviteeEmail = inviteeEmail;
        this.token = token;
        this.roleType = roleType;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.invitationStatus = PlatformInvitationStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public void setEmailInvitee(String inviteeEmail) {
        this.inviteeEmail = inviteeEmail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public PlatformInvitationStatus getInvitationStatus() {
        return invitationStatus;
    }

    public void setInvitationStatus(PlatformInvitationStatus invitationStatus) {
        this.invitationStatus = invitationStatus;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public void confirm() {
        if (invitationStatus != PlatformInvitationStatus.PENDING) {
            throw new IllegalStateException("Only pending invitations can be confirmed");
        }
        if (isExpired()) {
            throw new IllegalStateException("Cannot confirm an expired invitation");
        }
        this.confirmedAt = Instant.now();
        this.invitationStatus = PlatformInvitationStatus.ACCEPTED;
    }

    public void cancel() {
        if (invitationStatus != PlatformInvitationStatus.PENDING) {
            throw new IllegalStateException("Only pending invitations can be cancelled");
        }
        this.invitationStatus = PlatformInvitationStatus.CANCELLED;
    }
}
