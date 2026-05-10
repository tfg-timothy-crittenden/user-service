package com.timcritt.tfg.application.port.outbound;
import com.timcritt.tfg.domain.event.TeacherRoleRevokedEvent;
public interface RoleEventPublisherPort {
    void publishTeacherRoleRevoked(TeacherRoleRevokedEvent event);
}
