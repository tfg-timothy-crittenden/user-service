package com.timcritt.tfg.domain.model.aggregate.user;

import com.timcritt.tfg.domain.exception.MustNotRemoveLastUserRoleException;
import com.timcritt.tfg.domain.model.Role;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserTest {

    @Test
    void createStudentShouldThrowWhenRequiredFieldsAreNull() {
        PasswordHash passwordHash = PasswordHash.of("hashed-password");

        assertThrows(
                IllegalArgumentException.class,
                () -> User.createStudent(
                        null, "Tim", "Crittenden", "tim@example.com", passwordHash
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> User.createStudent(
                        "tim", null, "Crittenden", "tim@example.com", passwordHash
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> User.createStudent(
                        "tim", "Tim", null, "tim@example.com", passwordHash
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> User.createStudent(
                        "tim", "Tim", "Crittenden", null, passwordHash
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> User.createStudent(
                        "tim", "Tim", "Crittenden", "tim@example.com", null
                )
        );
    }

    @Test
    void createFromInvitationShouldThrowWhenRequiredFieldsAreNull() {
        PasswordHash passwordHash = PasswordHash.of("hashed-password");

        assertThrows(
                IllegalArgumentException.class,
                () -> User.createFromInvitation(
                        null,
                        "Tim",
                        "Crittenden",
                        "tim@example.com",
                        passwordHash,
                        Role.TEACHER
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> User.createFromInvitation(
                        "tim",
                        null,
                        "Crittenden",
                        "tim@example.com",
                        passwordHash,
                        Role.TEACHER
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> User.createFromInvitation(
                        "tim",
                        "Tim",
                        null,
                        "tim@example.com",
                        passwordHash,
                        Role.TEACHER
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> User.createFromInvitation(
                        "tim",
                        "Tim",
                        "Crittenden",
                        null,
                        passwordHash,
                        Role.TEACHER
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> User.createFromInvitation(
                        "tim",
                        "Tim",
                        "Crittenden",
                        "tim@example.com",
                        null,
                        Role.TEACHER
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> User.createFromInvitation(
                        "tim",
                        "Tim",
                        "Crittenden",
                        "tim@example.com",
                        passwordHash,
                        null
                )
        );
    }

    @Test
    void rehydrateShouldThrowWhenRequiredFieldsAreNull() {
        PasswordHash passwordHash = PasswordHash.of("hashed-password");
        Set<Role> roles = Set.of(Role.STUDENT);

        assertThrows(
                IllegalArgumentException.class,
                () -> User.rehydrate(
                        1L,
                        null,
                        "Tim",
                        "Crittenden",
                        "tim@example.com",
                        roles,
                        passwordHash,
                        true
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> User.rehydrate(
                        1L,
                        "tim",
                        null,
                        "Crittenden",
                        "tim@example.com",
                        roles,
                        passwordHash,
                        true
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> User.rehydrate(
                        1L,
                        "tim",
                        "Tim",
                        null,
                        "tim@example.com",
                        roles,
                        passwordHash,
                        true
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> User.rehydrate(
                        1L,
                        "tim",
                        "Tim",
                        "Crittenden",
                        null,
                        roles,
                        passwordHash,
                        true
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> User.rehydrate(
                        1L,
                        "tim",
                        "Tim",
                        "Crittenden",
                        "tim@example.com",
                        null,
                        passwordHash,
                        true
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> User.rehydrate(
                        1L,
                        "tim",
                        "Tim",
                        "Crittenden",
                        "tim@example.com",
                        roles,
                        null,
                        true
                )
        );
    }

    @Test
    void shouldNotAllowRemovingLastRole() {
        User user = User.createStudent(
                "tim",
                "Tim",
                "Crittenden",
                "tim@example.com",
                PasswordHash.of("hashed-password")
        );

        assertThrows(
                MustNotRemoveLastUserRoleException.class,
                () -> user.revokeRole(Role.STUDENT)
        );
    }

    @Test
    void updateProfileShouldNotAllowNullOrBlankParameters() {
        User user = User.createStudent(
                "tim",
                "Tim",
                "Crittenden",
                "tim@example.com",
                PasswordHash.of("hashed-password")
        );
        assertThrows(IllegalArgumentException.class, () -> user.updateProfile(null, "Bob"));
        assertThrows(IllegalArgumentException.class, () -> user.updateProfile("", "Bob"));
        assertThrows(IllegalArgumentException.class, () -> user.updateProfile(" ", "Bob"));
        assertThrows(IllegalArgumentException.class, () -> user.updateProfile("Fluffy", null));
        assertThrows(IllegalArgumentException.class, () -> user.updateProfile("Fluffy", ""));
        assertThrows(IllegalArgumentException.class, () -> user.updateProfile("Fluffy", " "));

    }
}
