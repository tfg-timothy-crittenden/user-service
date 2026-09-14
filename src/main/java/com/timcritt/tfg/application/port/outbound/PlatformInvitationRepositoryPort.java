package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.aggregate.platformInvitation.PlatformInvitation;
import com.timcritt.tfg.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface PlatformInvitationRepositoryPort {

    Optional<PlatformInvitation> findByInviteeEmail(String inviteeEmail);

    Optional<PlatformInvitation> findByInvitationId(Long invitationId);

    List<PlatformInvitation> findPendingByRole(Role role);

    void save(PlatformInvitation platformInvitation);

    void deleteAllByIds(List<Long> ids);

    List<PlatformInvitation> findAllByIds(List<Long> ids);

    Optional<PlatformInvitation> findByToken(String token);
}
