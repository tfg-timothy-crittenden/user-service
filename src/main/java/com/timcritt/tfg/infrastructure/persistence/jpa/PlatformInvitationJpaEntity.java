package com.timcritt.tfg.infrastructure.persistence.jpa;


import com.timcritt.tfg.domain.model.PlatformInvitationStatus;
import com.timcritt.tfg.domain.model.RoleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table (name="platform_invitation")
public class PlatformInvitationJpaEntity {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Column (nullable = false)
    @Getter @Setter
    private Long createdByUserId;

    @Column (nullable = false, unique = true)
    @Getter @Setter
    private String inviteeEmail;

    @Column (nullable = false)
    @Getter @Setter
    private Instant createdAt;

    @Column (nullable = false)
    @Getter @Setter
    private Instant expiresAt;
    @Getter @Setter
    private Instant confirmedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status", nullable = false)
    @Getter @Setter
    private PlatformInvitationStatus platformInvitationStatus;

    @Column (nullable = false)
    @Getter @Setter
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name ="role", nullable = false)
    @Getter @Setter
    private RoleType roleType;


    public PlatformInvitationJpaEntity() {

    }

    public PlatformInvitationJpaEntity(Long Id, Long createdByUserId, String inviteeEmail, Instant createdAt, Instant expiresAt, Instant confirmedAt, PlatformInvitationStatus platformInvitationStatus, String token, RoleType roleType) {
        this.id = id;
        this.createdByUserId = createdByUserId;
        this.inviteeEmail = inviteeEmail;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.confirmedAt = confirmedAt;
        this.platformInvitationStatus = platformInvitationStatus;
        this.token = token;
        this.roleType = roleType;

    }


}
