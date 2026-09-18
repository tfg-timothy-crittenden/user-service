package com.timcritt.tfg.application.port.outbound;
import com.timcritt.tfg.domain.event.TeacherRoleRevokedEvent;
import com.timcritt.tfg.domain.event.UserProfileUpdatedEvent;

public interface UserEventPublisherPort {
    void publishUserProfileUpdated(UserProfileUpdatedEvent event);

    void publishTeacherRoleRevoked(TeacherRoleRevokedEvent event);
}
