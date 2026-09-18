package com.timcritt.tfg.infrastructure.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.aggregate.user.PasswordHash;
import com.timcritt.tfg.domain.model.aggregate.user.User;
import com.timcritt.tfg.infrastructure.persistence.UserRepositoryAdapter;
import com.timcritt.tfg.infrastructure.persistence.jpa.OutboxEventJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.spring.OutboxEventJpaRepository;
import config.PostgresTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresTestContainerConfiguration.class)
class UserServiceAdapterOutboxIT {

    @Autowired
    private UserServiceAdapter userService;

    @Autowired
    private UserRepositoryAdapter userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private OutboxEventJpaRepository outboxRepository;

    @Test
    void shouldWriteOutboxEventWhenTeacherRoleIsRevoked() throws Exception {
        User teacher = createTeacher();
        User saved = userRepository.save(teacher);

        userService.removeRole(
                saved.getId(),
                Role.TEACHER
        );

        User reloaded = userRepository.findById(saved.getId())
                .orElseThrow();

        assertFalse(reloaded.hasRole(Role.TEACHER));
        assertTrue(reloaded.hasRole(Role.STUDENT));

        List<OutboxEventJpaEntity> events =
                outboxRepository.findAll();

        OutboxEventJpaEntity event = events.stream()
                .filter(e ->
                        saved.getId().toString()
                                .equals(e.getAggregateId())
                )
                .filter(e ->
                        "user.teacher-role-revoked.v1"
                                .equals(e.getEventType())
                )
                .findFirst()
                .orElseThrow();

        assertEquals(
                "USER",
                event.getAggregateType()
        );

        assertEquals(
                saved.getId().toString(),
                event.getAggregateId()
        );

        assertEquals(
                "user.teacher-role-revoked.v1",
                event.getEventType()
        );

        assertNotNull(event.getId());
        assertNotNull(event.getCreatedAt());

        JsonNode payload =
                objectMapper.readTree(event.getPayload());

        assertEquals(
                saved.getId().longValue(),
                payload.get("userId").asLong()
        );
    }

    @Test
    void shouldRollbackRoleRemovalWhenOutboxWriteFails() {
        User teacher = createTeacher();
        User saved = userRepository.save(teacher);

        doThrow(
                new DataAccessResourceFailureException(
                        "Simulated outbox failure"
                )
        )
                .when(outboxRepository)
                .save(any(OutboxEventJpaEntity.class));

        assertThrows(
                DataAccessResourceFailureException.class,
                () -> userService.removeRole(
                        saved.getId(),
                        Role.TEACHER
                )
        );

        User reloaded = userRepository.findById(saved.getId())
                .orElseThrow();

        assertTrue(reloaded.hasRole(Role.TEACHER));
        assertTrue(reloaded.hasRole(Role.STUDENT));
    }

    @Test
    void shouldWriteProfileUpdatedEventToOutbox() throws Exception {
        String suffix = UUID.randomUUID().toString();

        User user = User.createStudent(
                "tim-" + suffix,
                "Tim",
                "Crittenden",
                "tim-" + suffix + "@example.com",
                PasswordHash.of("hashed-password")
        );

        User saved = userRepository.save(user);

        User updated = userService.updateUser(
                saved.getId(),
                saved.getUsername(),
                "Timothy",
                saved.getSurname(),
                saved.getEmail()
        );

        OutboxEventJpaEntity event = outboxRepository.findAll()
                .stream()
                .filter(e ->
                        "user.profile-updated.v1"
                                .equals(e.getEventType())
                )
                .filter(e ->
                        saved.getId().toString()
                                .equals(e.getAggregateId())
                )
                .findFirst()
                .orElseThrow();

        assertEquals(
                "USER",
                event.getAggregateType()
        );

        assertEquals(
                saved.getId().toString(),
                event.getAggregateId()
        );

        assertNotNull(event.getId());
        assertNotNull(event.getCreatedAt());

        JsonNode payload =
                objectMapper.readTree(event.getPayload());

        assertEquals(
                saved.getId().longValue(),
                payload.get("userId").asLong()
        );

        assertEquals(
                updated.getVersion(),
                payload.get("version").asLong()
        );

        assertEquals(
                "Timothy",
                payload.get("firstName").asText()
        );

        assertEquals(
                saved.getSurname(),
                payload.get("lastName").asText()
        );
    }

    @Test
    void shouldRollbackProfileUpdateWhenOutboxWriteFails() {
        String suffix = UUID.randomUUID().toString();

        User user = User.createStudent(
                "tim-" + suffix,
                "Tim",
                "Crittenden",
                "tim-" + suffix + "@example.com",
                PasswordHash.of("hashed-password")
        );

        User saved = userRepository.save(user);

        long originalVersion = saved.getVersion();

        doThrow(
                new DataAccessResourceFailureException(
                        "Simulated outbox failure"
                )
        )
                .when(outboxRepository)
                .save(any(OutboxEventJpaEntity.class));

        assertThrows(
                DataAccessResourceFailureException.class,
                () -> userService.updateUser(
                        saved.getId(),
                        saved.getUsername(),
                        "Timothy",
                        saved.getSurname(),
                        saved.getEmail()
                )
        );

        User reloaded = userRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals("Tim", reloaded.getName());

        assertEquals(
                "Crittenden",
                reloaded.getSurname()
        );

        assertEquals(
                originalVersion,
                reloaded.getVersion()
        );
    }

    private User createTeacher() {
        String suffix = UUID.randomUUID().toString();

        User user = User.createStudent(
                "teacher-" + suffix,
                "Test",
                "Teacher",
                "teacher-" + suffix + "@example.com",
                PasswordHash.of("hashed-password")
        );

        user.grantRole(Role.TEACHER);

        return user;
    }
}