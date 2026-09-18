package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.port.outbound.UserEventPublisherPort;
import com.timcritt.tfg.domain.event.TeacherRoleRevokedEvent;
import com.timcritt.tfg.domain.event.UserProfileUpdatedEvent;
import com.timcritt.tfg.infrastructure.persistence.jpa.OutboxEventJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.spring.OutboxEventJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class OutboxUserEventPublisherAdapter implements UserEventPublisherPort {

    private static final String AGGREGATE_TYPE = "USER";

    private static final String USER_PROFILE_UPDATED_EVENT =
            "user.profile-updated.v1";

    private static final String TEACHER_ROLE_REVOKED_EVENT =
            "user.teacher-role-revoked.v1";

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxUserEventPublisherAdapter(
            OutboxEventJpaRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishTeacherRoleRevoked(
            TeacherRoleRevokedEvent event
    ) {
        saveEvent(
                AGGREGATE_TYPE,
                event.userId().toString(),
                TEACHER_ROLE_REVOKED_EVENT,
                event
        );
    }

    @Override
    public void publishUserProfileUpdated(
            UserProfileUpdatedEvent event
    ) {
        saveEvent(
                AGGREGATE_TYPE,
                event.userId().toString(),
                USER_PROFILE_UPDATED_EVENT,
                event
        );
    }

    private void saveEvent(
            String aggregateType,
            String aggregateId,
            String eventType,
            Object payload
    ) {
        OutboxEventJpaEntity outboxEvent =
                new OutboxEventJpaEntity(
                        UUID.randomUUID(),
                        aggregateType,
                        aggregateId,
                        eventType,
                        serialize(payload),
                        Instant.now()
                );

        outboxRepository.save(outboxEvent);
    }

    private String serialize(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize outbox event",
                    e
            );
        }
    }
}