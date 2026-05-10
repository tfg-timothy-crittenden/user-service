package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.PlatformInvitation;
import com.timcritt.tfg.domain.model.RoleType;

import java.util.List;
import java.util.Optional;

public interface PlatformInvitationRepositoryPort {

    Optional<PlatformInvitation> findByInviteeEmail(String inviteeEmail);

    Optional<PlatformInvitation> findByInvitationId(Long invitationId);

    List<PlatformInvitation> findPendingByRoleType(RoleType roleType);

    void save(PlatformInvitation platformInvitation);

    void deleteAllByIds(List<Long> ids);

    List<PlatformInvitation> findAllByIds(List<Long> ids);

    Optional<PlatformInvitation> findByToken(String token);
}
