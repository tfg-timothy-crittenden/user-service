package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.aggregate.user.PasswordHash;
import com.timcritt.tfg.domain.model.aggregate.user.User;
import config.PostgresTestContainerConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// @Transactional Spring tests are rolled back after each test method.
@DataJpaTest
@ActiveProfiles("test")
@Import({
        PostgresTestContainerConfiguration.class,
        UserRepositoryAdapter.class
})
class UserRepositoryAdapterIT {

    @Autowired
    private UserRepositoryAdapter repository;

    @Autowired
    private EntityManager entityManager;

    private final String name = "Tim";
    private final String surname = "Crittenden";
    private final String username = "timcritt";
    private final String email = "tim@example.com";
    private final PasswordHash password = PasswordHash.of("123456");

    @Test
    void shouldSaveAndReloadNewStudent() {
        User user = createValidStudent();

        User saved = repository.save(user);

        flushAndClear();

        User reloaded = repository.findById(saved.getId())
                .orElseThrow();

        assertNotNull(saved.getId());
        assertEquals(username, reloaded.getUsername());
        assertEquals(name, reloaded.getName());
        assertEquals(surname, reloaded.getSurname());
        assertEquals(email, reloaded.getEmail());
        assertEquals(password, reloaded.getPasswordHash());
        assertTrue(reloaded.hasRole(Role.STUDENT));
        assertFalse(reloaded.isVerified());
    }

    @Test
    void shouldPersistProfileChangesToExistingUser() {
        User saved = repository.save(createValidStudent());

        saved.updateProfile("fluff", "Fluffy", "Bob");

        repository.save(saved);

        flushAndClear();

        User reloaded = repository.findById(saved.getId())
                .orElseThrow();

        assertEquals("Fluffy", reloaded.getName());
        assertEquals("Bob", reloaded.getSurname());
    }

    @Test
    void shouldPersistEmailChangeAndResetVerification() {
        User user = User.rehydrate(
                null,
                0L,
                username,
                name,
                surname,
                email,
                java.util.Set.of(Role.STUDENT),
                password,
                true
        );

        User saved = repository.save(user);

        saved.changeEmail("new@example.com");

        repository.save(saved);

        flushAndClear();

        User reloaded = repository.findById(saved.getId())
                .orElseThrow();

        assertEquals("new@example.com", reloaded.getEmail());
        assertFalse(reloaded.isVerified());
    }

    @Test
    void shouldPersistPasswordChange() {
        User saved = repository.save(createValidStudent());

        PasswordHash newPassword = PasswordHash.of("new-password-hash");

        saved.changePassword(newPassword);

        repository.save(saved);

        flushAndClear();

        User reloaded = repository.findById(saved.getId())
                .orElseThrow();

        assertEquals(newPassword, reloaded.getPasswordHash());
    }

    @Test
    void shouldPersistGrantedRole() {
        User saved = repository.save(createValidStudent());

        saved.grantRole(Role.TEACHER);

        repository.save(saved);

        flushAndClear();

        User reloaded = repository.findById(saved.getId())
                .orElseThrow();

        assertTrue(reloaded.hasRole(Role.STUDENT));
        assertTrue(reloaded.hasRole(Role.TEACHER));
    }

    @Test
    void shouldPersistRevokedRole() {
        User user = createValidStudent();
        user.grantRole(Role.TEACHER);

        User saved = repository.save(user);

        saved.revokeRole(Role.TEACHER);

        repository.save(saved);

        flushAndClear();

        User reloaded = repository.findById(saved.getId())
                .orElseThrow();

        assertTrue(reloaded.hasRole(Role.STUDENT));
        assertFalse(reloaded.hasRole(Role.TEACHER));
    }

    @Test
    void shouldFindUserByUsername() {
        User saved = repository.save(createValidStudent());

        flushAndClear();

        User found = repository.findByUsername(username)
                .orElseThrow();

        assertEquals(saved.getId(), found.getId());
        assertEquals(username, found.getUsername());
    }

    @Test
    void shouldFindUserByEmail() {
        User saved = repository.save(createValidStudent());

        flushAndClear();

        User found = repository.findByEmail(email)
                .orElseThrow();

        assertEquals(saved.getId(), found.getId());
        assertEquals(email, found.getEmail());
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        assertTrue(repository.findById(Long.MAX_VALUE).isEmpty());
        assertTrue(repository.findByUsername("does-not-exist").isEmpty());
        assertTrue(repository.findByEmail("does-not-exist@example.com").isEmpty());
    }

    @Test
    void shouldFindAllUsersWithGivenRole() {
        User student = createValidStudent();

        User teacher = User.createFromInvitation(
                "teacher",
                "Bob",
                "Smith",
                "teacher@example.com",
                PasswordHash.of("teacher-password"),
                Role.TEACHER
        );

        repository.save(student);
        User savedTeacher = repository.save(teacher);

        flushAndClear();

        List<User> teachers = repository.findAllUsersByRole(Role.TEACHER);

        assertEquals(1, teachers.size());
        assertEquals(savedTeacher.getId(), teachers.getFirst().getId());
        assertTrue(teachers.getFirst().hasRole(Role.TEACHER));
    }

    @Test
    void shouldFindUserWithMultipleRolesByEitherRole() {
        User user = createValidStudent();
        user.grantRole(Role.TEACHER);

        User saved = repository.save(user);

        flushAndClear();

        List<User> students = repository.findAllUsersByRole(Role.STUDENT);
        List<User> teachers = repository.findAllUsersByRole(Role.TEACHER);

        assertTrue(
                students.stream()
                        .anyMatch(found -> found.getId().equals(saved.getId()))
        );

        assertTrue(
                teachers.stream()
                        .anyMatch(found -> found.getId().equals(saved.getId()))
        );
    }

    @Test
    void shouldDeleteExistingUser() {
        User saved = repository.save(createValidStudent());

        flushAndClear();

        Boolean deleted = repository.delete(saved.getId());

        flushAndClear();

        assertTrue(deleted);
        assertTrue(repository.findById(saved.getId()).isEmpty());
    }

    @Test
    void shouldNotCreateUserWhenSavingUnknownExistingId() {
        User user = User.rehydrate(
                999999L,
                0L,
                username,
                name,
                surname,
                email,
                Set.of(Role.STUDENT),
                password,
                false
        );

        assertThrows(
                IllegalStateException.class,
                () -> repository.save(user)
        );
    }

    private User createValidStudent() {
        return User.createStudent(
                username,
                name,
                surname,
                email,
                password
        );
    }

    // Ensures subsequent reads come from the database rather than the current persistence context.
    private void flushAndClear() {
        // Flush pending changes to the database without committing the transaction.
        entityManager.flush();

        // Clear the persistence context, detaching all managed entities.
        entityManager.clear();
    }
}