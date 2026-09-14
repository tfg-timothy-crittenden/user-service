package com.timcritt.tfg.infrastructure.web.dto;

import com.timcritt.tfg.domain.model.aggregate.platformInvitation.PlatformInvitationStatus;
import com.timcritt.tfg.domain.model.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@RequiredArgsConstructor
public class PlatformInvitationDto {

    private Long id;
    private Long createdByUserId;
    private String inviteeEmail;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant confirmedAt;
    private PlatformInvitationStatus invitationStatus;
    private Role role;


}
