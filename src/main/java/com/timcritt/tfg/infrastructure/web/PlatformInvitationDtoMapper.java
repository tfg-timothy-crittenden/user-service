package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.domain.model.PlatformInvitation;
import com.timcritt.tfg.infrastructure.web.dto.PlatformInvitationDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PlatformInvitationDtoMapper {

    public static PlatformInvitationDto toDto(PlatformInvitation d) {

        if(d == null) {
            return null;
        }

        PlatformInvitationDto dto = new PlatformInvitationDto();
        dto.setId(d.getId());
        dto.setCreatedByUserId(d.getCreatedByUserId());
        dto.setInviteeEmail(d.getInviteeEmail());
        dto.setCreatedAt(d.getCreatedAt());
        dto.setExpiresAt(d.getExpiresAt());
        dto.setConfirmedAt(d.getConfirmedAt());
        dto.setInvitationStatus(d.getPlatformInvitationStatus());
        dto.setRoleType(d.getRoleType());
        return dto;
    }

}
