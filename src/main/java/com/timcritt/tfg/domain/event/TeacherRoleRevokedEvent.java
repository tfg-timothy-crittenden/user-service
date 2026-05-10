package com.timcritt.tfg.domain.event;

/**
 * Domain event published when the TEACHER role is revoked from a user.
 * The classroom service can consume this to remove the teacher from all classrooms.
 */
public record TeacherRoleRevokedEvent(Long userId) {
}

