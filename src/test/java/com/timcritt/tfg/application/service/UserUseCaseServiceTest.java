package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.exception.RoleNotFoundException;
import com.timcritt.tfg.application.exception.UserAlreadyExistsException;
import com.timcritt.tfg.application.exception.UserNotFoundException;
import com.timcritt.tfg.application.port.inbound.EmailVerificationUseCase;
import com.timcritt.tfg.application.port.outbound.UserEventPublisherPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.aggregate.user.PasswordHash;
import com.timcritt.tfg.domain.model.aggregate.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCaseServiceTest {

    @Mock
    private UserRepositoryPort repository;

    @Mock
    private EmailVerificationUseCase emailVerificationService;

    @Mock
    private UserEventPublisherPort userEventPublisher;

    private UserUseCaseService service;

    private final PasswordHash passwordHash =
            PasswordHash.of("hashed-password");

    @BeforeEach
    void setUp() {
        service = new UserUseCaseService(
                repository,
                emailVerificationService,
                userEventPublisher
        );
    }

    @Test
    void shouldGetUserById() {
        User user = createStudent(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = service.getUserById(1L);

        assertEquals(user, result);

        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowWhenGettingUserByIdAndUserDoesNotExist() {
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.getUserById(1L)
        );
    }

    @Test
    void shouldGetUserByUsername() {
        User user = createStudent(1L);

        when(repository.findByUsername("timcritt"))
                .thenReturn(Optional.of(user));

        User result = service.getUserByUsername("timcritt");

        assertEquals(user, result);

        verify(repository).findByUsername("timcritt");
        verify(repository, never()).findByEmail(any());
    }

    @Test
    void shouldFallBackToEmailWhenUsernameNotFound() {
        User user = createStudent(1L);

        when(repository.findByUsername("tim@example.com"))
                .thenReturn(Optional.empty());

        when(repository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(user));

        User result =
                service.getUserByUsername("tim@example.com");

        assertEquals(user, result);

        verify(repository).findByUsername("tim@example.com");
        verify(repository).findByEmail("tim@example.com");
    }

    @Test
    void shouldThrowWhenUsernameAndEmailDoNotExist() {
        when(repository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        when(repository.findByEmail("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.getUserByUsername("missing")
        );
    }

    @Test
    void shouldFindUserByIdentifierUsingUsername() {
        User user = createStudent(1L);

        when(repository.findByUsername("timcritt"))
                .thenReturn(Optional.of(user));

        Optional<User> result =
                service.findByIdentifier("timcritt");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());

        verify(repository, never()).findByEmail(any());
    }

    @Test
    void shouldFindUserByIdentifierUsingEmail() {
        User user = createStudent(1L);

        when(repository.findByUsername("tim@example.com"))
                .thenReturn(Optional.empty());

        when(repository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(user));

        Optional<User> result =
                service.findByIdentifier("tim@example.com");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void shouldReturnEmptyWhenIdentifierDoesNotExist() {
        when(repository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        when(repository.findByEmail("missing"))
                .thenReturn(Optional.empty());

        Optional<User> result =
                service.findByIdentifier("missing");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCreateStudentUser() {
        when(repository.findByUsername("timcritt"))
                .thenReturn(Optional.empty());

        when(repository.findByEmail("tim@example.com"))
                .thenReturn(Optional.empty());

        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.createUser(
                "timcritt",
                "Tim",
                "Crittenden",
                "tim@example.com",
                "hashed-password"
        );

        assertEquals("timcritt", result.getUsername());
        assertEquals("Tim", result.getName());
        assertEquals("Crittenden", result.getSurname());
        assertEquals("tim@example.com", result.getEmail());
        assertEquals(passwordHash, result.getPasswordHash());

        assertTrue(result.hasRole(Role.STUDENT));
        assertFalse(result.isVerified());

        verify(repository).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateUsernameWhenCreatingUser() {
        User existing = createStudent(1L);

        when(repository.findByUsername("timcritt"))
                .thenReturn(Optional.of(existing));

        assertThrows(
                UserAlreadyExistsException.class,
                () -> service.createUser(
                        "timcritt",
                        "Tim",
                        "Crittenden",
                        "other@example.com",
                        "hashed-password"
                )
        );

        verify(repository, never()).findByEmail(any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateEmailWhenCreatingUser() {
        User existing = createStudent(1L);

        when(repository.findByUsername("newusername"))
                .thenReturn(Optional.empty());

        when(repository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(existing));

        assertThrows(
                UserAlreadyExistsException.class,
                () -> service.createUser(
                        "newusername",
                        "Tim",
                        "Crittenden",
                        "tim@example.com",
                        "hashed-password"
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateUserAndSendVerificationWhenEmailChanges() {
        User user = createVerifiedStudent(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));

        when(repository.findByUsername("newusername"))
                .thenReturn(Optional.empty());

        when(repository.findByEmail("new@example.com"))
                .thenReturn(Optional.empty());

        when(repository.save(user))
                .thenReturn(user);

        User result = service.updateUser(
                1L,
                "newusername",
                "New",
                "Name",
                "new@example.com"
        );

        assertEquals("newusername", result.getUsername());
        assertEquals("New", result.getName());
        assertEquals("Name", result.getSurname());
        assertEquals("new@example.com", result.getEmail());

        // Changing the email means the address must be verified again.
        assertFalse(result.isVerified());

        verify(repository).save(user);

        // Because the email changed, a fresh verification token/email
        // should be created for the new address.
        verify(emailVerificationService)
                .createAndSendToken(
                        1L,
                        "new@example.com"
                );
    }

    @Test
    void shouldNotSendVerificationWhenEmailDoesNotChange() {
        User user = createVerifiedStudent(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));

        when(repository.findByUsername("newusername"))
                .thenReturn(Optional.empty());

        when(repository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(user));

        when(repository.save(user))
                .thenReturn(user);

        User result = service.updateUser(
                1L,
                "newusername",
                "New",
                "Name",
                "tim@example.com"
        );

        assertEquals("tim@example.com", result.getEmail());
        assertTrue(result.isVerified());

        verify(repository).save(user);

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void shouldThrowWhenUpdatingUnknownUser() {
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.updateUser(
                        1L,
                        "username",
                        "Name",
                        "Surname",
                        "email@example.com"
                )
        );

        verify(repository, never()).save(any());
        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void shouldDeleteExistingUser() {
        User user = createStudent(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));

        when(repository.delete(1L))
                .thenReturn(true);

        Boolean result =
                service.deleteUser(1L);

        assertTrue(result);

        verify(repository).delete(1L);
    }

    @Test
    void shouldThrowWhenDeletingUnknownUser() {
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.deleteUser(1L)
        );

        verify(repository, never()).delete(any());
    }

    @Test
    void shouldReturnUsersByRole() {
        User teacher = User.createFromInvitation(
                "teacher",
                "Bob",
                "Smith",
                "teacher@example.com",
                passwordHash,
                Role.TEACHER
        );

        when(repository.findAllUsersByRole(Role.TEACHER))
                .thenReturn(List.of(teacher));

        List<User> result =
                service.getAllUsersByRole(Role.TEACHER);

        assertEquals(1, result.size());
        assertTrue(
                result.getFirst()
                        .hasRole(Role.TEACHER)
        );
    }

    @Test
    void shouldRemoveRoleFromUser() {
        User user = User.rehydrate(
                1L,
                0L,
                "timcritt",
                "Tim",
                "Crittenden",
                "tim@example.com",
                Set.of(
                        Role.STUDENT,
                        Role.TEACHER
                ),
                passwordHash,
                true
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));

        when(repository.save(user))
                .thenReturn(user);

        User result =
                service.removeRole(
                        1L,
                        Role.TEACHER
                );

        assertTrue(result.hasRole(Role.STUDENT));
        assertFalse(result.hasRole(Role.TEACHER));

        verify(repository).save(user);
    }

    @Test
    void shouldThrowWhenRemovingRoleUserDoesNotHave() {
        User user = createStudent(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                RoleNotFoundException.class,
                () -> service.removeRole(
                        1L,
                        Role.TEACHER
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowWhenRemovingRoleFromUnknownUser() {
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.removeRole(
                        1L,
                        Role.TEACHER
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectUsernameAlreadyUsedByAnotherUser() {
        User existingUser =
                createStudent(1L);

        User otherUser = User.rehydrate(
                2L,
                0L,
                "taken",
                "Other",
                "User",
                "other@example.com",
                Set.of(Role.STUDENT),
                PasswordHash.of("hash"),
                true
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        when(repository.findByUsername("taken"))
                .thenReturn(Optional.of(otherUser));

        assertThrows(
                UserAlreadyExistsException.class,
                () -> service.updateUser(
                        1L,
                        "taken",
                        "Tim",
                        "Crittenden",
                        "tim@example.com"
                )
        );

        verify(repository, never()).save(any());
        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void shouldRejectEmailAlreadyUsedByAnotherUser() {
        User existingUser =
                createStudent(1L);

        User otherUser = User.rehydrate(
                2L,
                0L,
                "other",
                "Other",
                "User",
                "taken@example.com",
                Set.of(Role.STUDENT),
                PasswordHash.of("hash"),
                true
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        when(repository.findByUsername("timcritt"))
                .thenReturn(Optional.of(existingUser));

        when(repository.findByEmail("taken@example.com"))
                .thenReturn(Optional.of(otherUser));

        assertThrows(
                UserAlreadyExistsException.class,
                () -> service.updateUser(
                        1L,
                        "timcritt",
                        "Tim",
                        "Crittenden",
                        "taken@example.com"
                )
        );

        verify(repository, never()).save(any());
        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void shouldAllowKeepingOwnUsernameAndEmail() {
        User existingUser =
                createStudent(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        when(repository.findByUsername("timcritt"))
                .thenReturn(Optional.of(existingUser));

        when(repository.findByEmail("tim@example.com"))
                .thenReturn(Optional.of(existingUser));

        when(repository.save(existingUser))
                .thenReturn(existingUser);

        User result = service.updateUser(
                1L,
                "timcritt",
                "Tim",
                "Crittenden",
                "tim@example.com"
        );

        assertEquals(existingUser, result);

        verify(repository).findById(1L);
        verify(repository).findByUsername("timcritt");
        verify(repository).findByEmail("tim@example.com");
        verify(repository).save(existingUser);

        // Same email, so no new verification should be sent.
        verifyNoInteractions(emailVerificationService);
    }

    private User createStudent(Long id) {
        return User.rehydrate(
                id,
                0L,
                "timcritt",
                "Tim",
                "Crittenden",
                "tim@example.com",
                Set.of(Role.STUDENT),
                passwordHash,
                false
        );
    }

    private User createVerifiedStudent(Long id) {
        return User.rehydrate(
                id,
                0L,
                "timcritt",
                "Tim",
                "Crittenden",
                "tim@example.com",
                Set.of(Role.STUDENT),
                passwordHash,
                true
        );
    }
}