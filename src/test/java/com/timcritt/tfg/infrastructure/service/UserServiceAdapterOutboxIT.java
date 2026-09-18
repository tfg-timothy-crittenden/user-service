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

    /*
     * This is a spy rather than a normal mock.
     *
     * Normally it behaves like the real Spring Data repository and
     * persists outbox events to the Testcontainers PostgreSQL database.
     *
     * In the rollback test, we temporarily force save() to fail so we
     * can prove that the user-role change is rolled back as part of the
     * same transaction.
     */
    @MockitoSpyBean
    private OutboxEventJpaRepository outboxRepository;

    @Test
    void shouldWriteOutboxEventWhenTeacherRoleIsRevoked() throws Exception {
        // Arrange:
        // Create and persist a user with both STUDENT and TEACHER roles.
        User teacher = createTeacher();

        User saved = userRepository.save(teacher);

        // Act:
        // Revoke the TEACHER role through the real transactional service.
        userService.removeRole(
                saved.getId(),
                Role.TEACHER
        );

        // Assert:
        // Reload the user from the database.
        User reloaded = userRepository.findById(saved.getId())
                .orElseThrow();

        // The TEACHER role should have been removed.
        assertFalse(reloaded.hasRole(Role.TEACHER));

        // STUDENT should remain.
        assertTrue(reloaded.hasRole(Role.STUDENT));

        // Find the outbox event created for this user.
        List<OutboxEventJpaEntity> events =
                outboxRepository.findAll();

        OutboxEventJpaEntity event = events.stream()
                .filter(e ->
                        e.getAggregateId()
                                .equals(saved.getId().toString())
                )
                .findFirst()
                .orElseThrow();

        // Check the outbox metadata.
        assertEquals(
                "USER",
                event.getAggregateType()
        );

        assertEquals(
                saved.getId().toString(),
                event.getAggregateId()
        );

        assertEquals(
                "TEACHER_ROLE_REVOKED",
                event.getEventType()
        );

        assertNotNull(event.getId());
        assertNotNull(event.getCreatedAt());

        /*
         * Do not compare the JSON as a raw String.
         *
         * PostgreSQL JSONB is allowed to normalize formatting, for example:
         *
         * {"userId":7}
         *
         * might come back as:
         *
         * {"userId": 7}
         *
         * Parsing the payload lets us test the actual JSON data instead
         * of depending on whitespace/formatting.
         */
        JsonNode payload =
                objectMapper.readTree(event.getPayload());

        assertEquals(
                saved.getId().longValue(),
                payload.get("userId").asLong()
        );
    }

    @Test
    void shouldRollbackRoleRemovalWhenOutboxWriteFails() {
        // Arrange:
        // First persist a real user with STUDENT + TEACHER.
        User teacher = createTeacher();

        User saved = userRepository.save(teacher);

        /*
         * Force the outbox repository to fail when the application attempts
         * to persist the domain event.
         *
         * The important thing here is that the role update and outbox insert
         * happen inside the same transaction.
         */
        doThrow(
                new DataAccessResourceFailureException(
                        "Simulated outbox failure"
                )
        ).when(outboxRepository)
                .save(any(OutboxEventJpaEntity.class));

        // Act + assert:
        // The whole use case should fail.
        assertThrows(
                DataAccessResourceFailureException.class,
                () -> userService.removeRole(
                        saved.getId(),
                        Role.TEACHER
                )
        );

        /*
         * Now reload the user AFTER the failed transaction.
         *
         * If the transaction really rolled back, the TEACHER role should
         * still exist in the database.
         */
        User reloaded = userRepository.findById(saved.getId())
                .orElseThrow();

        assertTrue(reloaded.hasRole(Role.TEACHER));
        assertTrue(reloaded.hasRole(Role.STUDENT));
    }

    @Test
    void shouldWriteUserCreatedEventToOutbox() throws Exception {
        User saved = userService.createUser(
                "tim",
                "Tim",
                "Crittenden",
                "tim@example.com",
                "password"
        );

        OutboxEventJpaEntity event = outboxRepository.findAll()
                .stream()
                .filter(e -> "user.created.v1".equals(e.getEventType()))
                .filter(e -> saved.getId().toString().equals(e.getAggregateId()))
                .findFirst()
                .orElseThrow();

        assertEquals("USER", event.getAggregateType());

        JsonNode payload = objectMapper.readTree(event.getPayload());

        assertEquals(saved.getId().longValue(),
                payload.get("userId").asLong());

        assertEquals(saved.getVersion(),
                payload.get("version").asLong());

        assertEquals("Tim",
                payload.get("firstName").asText());

        assertEquals("Crittenden",
                payload.get("lastName").asText());
    }

    @Test
    void shouldRollbackUserCreationWhenOutboxWriteFails() {
        doThrow(new DataAccessResourceFailureException(
                "Simulated outbox failure"
        ))
                .when(outboxRepository)
                .save(any(OutboxEventJpaEntity.class));

        assertThrows(
                DataAccessResourceFailureException.class,
                () -> userService.createUser(
                        "tim",
                        "Tim",
                        "Crittenden",
                        "tim@example.com",
                        "password"
                )
        );

        assertTrue(
                userRepository.findByEmail("tim@example.com").isEmpty()
        );
    }

    private User createTeacher() {
        /*
         * Use unique username/email values so this integration test cannot
         * collide with users inserted by other tests.
         */
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