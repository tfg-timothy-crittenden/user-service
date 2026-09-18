package com.timcritt.tfg.domain.model.aggregate.user;

import com.timcritt.tfg.domain.exception.MustNotRemoveLastUserRoleException;
import com.timcritt.tfg.domain.exception.UserDoesNotHaveRoleException;
import com.timcritt.tfg.domain.model.Role;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
                        0L,
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
                        0L,
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
                        0L,
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
                        0L,
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
                        0L,
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
                        0L,
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
    void rehydrateShouldThrowWhenRolesContainNull() {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.STUDENT);
        roles.add(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> User.rehydrate(
                        1L,
                        0L,
                        "tim",
                        "Tim",
                        "Crittenden",
                        "tim@example.com",
                        roles,
                        PasswordHash.of("hash"),
                        true
                )
        );
    }
    @Test
    void rehydrateShouldThrowWhenRolesAreEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.rehydrate(
                        1L,
                        0L,
                        "tim",
                        "Tim",
                        "Crittenden",
                        "tim@example.com",
                        Set.of(),
                        PasswordHash.of("hash"),
                        true
                )
        );
    }



    @Test
    void updateProfileShouldNotAllowNullOrBlankParameters() {
        User user = createValidStudent();

        // username
        assertThrows(IllegalArgumentException.class,
                () -> user.updateProfile(null, "Bob", "Smith"));
        assertThrows(IllegalArgumentException.class,
                () -> user.updateProfile("", "Bob", "Smith"));
        assertThrows(IllegalArgumentException.class,
                () -> user.updateProfile(" ", "Bob", "Smith"));

        // name
        assertThrows(IllegalArgumentException.class,
                () -> user.updateProfile("bob", null, "Smith"));
        assertThrows(IllegalArgumentException.class,
                () -> user.updateProfile("bob", "", "Smith"));
        assertThrows(IllegalArgumentException.class,
                () -> user.updateProfile("bob", " ", "Smith"));

        // surname
        assertThrows(IllegalArgumentException.class,
                () -> user.updateProfile("bob", "Bob", null));
        assertThrows(IllegalArgumentException.class,
                () -> user.updateProfile("bob", "Bob", ""));
        assertThrows(IllegalArgumentException.class,
                () -> user.updateProfile("bob", "Bob", " "));
    }

    @Test
    void changeEmailShouldNotAllowNullOrBlankParameters() {
        User user = createValidStudent();
        assertThrows(IllegalArgumentException.class, () -> user.changeEmail(null));
        assertThrows(IllegalArgumentException.class, () -> user.changeEmail(""));
        assertThrows(IllegalArgumentException.class, () -> user.changeEmail(" "));
    }

    @Test
    void updateEmailShouldUpdateEmail() {
        User user = createValidStudent();
        String newEmail = "fluffy@bob.com";
        user.changeEmail(newEmail);
        assertEquals(newEmail, user.getEmail());
    }

    @Test
    void changeEmailShouldDoNothingWhenEmailIsUnchanged() {
        User user = createValidTeacherFromInvitation();
        user.confirmEmail();
        assertTrue(user.isVerified());

        String newEmail = user.getEmail();

        user.changeEmail(newEmail);

        assertEquals(newEmail, user.getEmail());
        assertTrue(user.isVerified());
    }

    @Test
    void changeEmailShouldUpdateEmailAndResetVerification() {
        User user = createValidTeacherFromInvitation();

        assertTrue(user.isVerified());

        String newEmail = "fluffy@bob.com";
        user.changeEmail(newEmail);

        assertEquals(newEmail, user.getEmail());
        assertFalse(user.isVerified());
    }

    @Test
    void updateProfileShouldUpdateUsernameNameAndSurname() {
        User user = createValidStudent();

        user.updateProfile(
                "fluffy",
                "Fluffy",
                "Crittenden"
        );

        assertEquals("fluffy", user.getUsername());
        assertEquals("Fluffy", user.getName());
        assertEquals("Crittenden", user.getSurname());
    }

    @Test
    void changePasswordShouldNotAllowNullOrBlankParameters() {
        User user = createValidStudent();
        assertThrows(NullPointerException.class, () -> user.changePassword(null));
    }
    @Test
    void changePasswordShouldUpdatePassword() {
        User user = createValidStudent();
        PasswordHash newPassword = PasswordHash.of("fluffy");
        assertNotEquals(newPassword, user.getPasswordHash());
        user.changePassword(newPassword);
        assertEquals(newPassword, user.getPasswordHash());
    }

    @Test
    void grantRoleShouldNotAllowNullOrBlankParameters() {
        User user = createValidStudent();
        assertThrows(NullPointerException.class, () -> user.grantRole(null));
    }

    @Test
    void grantRoleShouldUpdateRole() {
        User user = createValidStudent();
        Role role = Role.TEACHER;
        user.grantRole(role);
        assertTrue(user.hasRole(role));
    }

    @Test
    void grantRoleShouldThrowWhenRoleAlreadyGranted() {
        User user = createValidStudent();
        Role role = Role.STUDENT;

        //Check that the student already has the role first, which it should by default
        assertTrue(user.hasRole(role));
        assertThrows(IllegalArgumentException.class, () -> user.grantRole(role));
    }

    @Test
    void revokeRoleShouldNotAllowNullOrBlankParameters() {
        User user = createValidStudent();
        assertThrows(NullPointerException.class, () -> user.revokeRole(null));
    }

    @Test
    void revokeRoleShouldRemoveRole() {
        User user = createValidStudent();
        Role role = Role.TEACHER;
        // Add a second role first because the final remaining role cannot be revoked as per domain rules
        user.grantRole(role);
        assertTrue(user.hasRole(role));
        user.revokeRole(role);
        assertFalse(user.hasRole(role));
    }

    @Test
    void revokeRoleShouldThrowWhenRoleIsNotAlreadyGranted() {
        User user = createValidStudent();
        Role secondRole = Role.TEACHER;

        // Add a second role first because the final remaining role cannot be revoked
        user.grantRole(secondRole);
        assertTrue(user.hasRole(secondRole));

        Role roleToRevoke = Role.ADMIN;

        assertThrows(UserDoesNotHaveRoleException.class, () -> user.revokeRole(roleToRevoke));

    }
    @Test
    void shouldNotAllowRemovingLastRole() {
        User user = createValidStudent();
        assertThrows(MustNotRemoveLastUserRoleException.class, () -> user.revokeRole(Role.STUDENT)
        );
    }

    @Test
    void hasRoleShouldThrowWhenRoleParameterIsNull() {
        User user = createValidStudent();
        assertThrows(NullPointerException.class, () -> user.hasRole(null));
    }

    @Test
    void hasRoleShouldReturnFalseWhenRoleIsNotAlreadyGranted() {
        User user = createValidStudent();
        Role roleToCheck = Role.TEACHER;
        assertFalse(user.hasRole(roleToCheck));
    }

    @Test
    void hasRoleShouldReturnTrueWhenRoleIsAlreadyGranted() {
        User user = createValidStudent();
        Role role = Role.TEACHER;
        user.grantRole(role);
        assertTrue(user.hasRole(role));
    }

    @Test
    void usersWithSameIdShouldBeEqual(){
        User user1 = User.rehydrate(
                1L,
                0L,
                "tim",
                "Tim",
                "Crittenden",
                "tim@example.com",
                Set.of(Role.STUDENT),
                PasswordHash.of("hashed-password"),
                true
        );

        User user2 = User.rehydrate(
                1L,
                0L,
                "differentUsername",
                "Bob",
                "Smith",
                "other@example.com",
                Set.of(Role.TEACHER),
                PasswordHash.of("different-hash"),
                false
        );
        assertEquals(user1, user2);

    }
    @Test
    void usersWithDifferentIdsShouldNotBeEqual() {
        User user1 = User.rehydrate(
                1L,
                0L,
                "tim",
                "Tim",
                "Crittenden",
                "tim@example.com",
                Set.of(Role.STUDENT),
                PasswordHash.of("hashed-password"),
                true
        );

        User user2 = User.rehydrate(
                2L,
                0L,
                "tim",
                "Tim",
                "Crittenden",
                "tim@example.com",
                Set.of(Role.STUDENT),
                PasswordHash.of("hashed-password"),
                true
        );

        assertNotEquals(user1, user2);
    }

    @Test
    void newUsersWithoutIdsShouldNotBeEqual() {
        User user1 = createValidStudent();
        User user2 = createValidStudent();
        assertNotEquals(user1, user2);
    }

    @Test
    void userShouldEqualItself() {
        User user = createValidStudent();
        User user2 = user;
        assertEquals(user, user2);
    }

    @Test
    void userShouldNotEqualNull() {
        User user = createValidStudent();
        assertNotEquals(user, null);
    }

    @Test
    void userShouldNotEqualDifferentClass() {
        User user = createValidStudent();

        assertNotEquals(user, "not a user");
    }

    @Test
    void usersShouldHaveSameHashCode() {
        User user1 = createValidStudent();
        User user2 = createValidStudent();

        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void equalUsersShouldHaveSameHashCode() {
        User user1 = User.rehydrate(
                1L,
                0L,
                "tim",
                "Tim",
                "Crittenden",
                "tim@example.com",
                Set.of(Role.STUDENT),
                PasswordHash.of("hash"),
                true
        );

        User user2 = User.rehydrate(
                1L,
                0L,
                "bob",
                "Bob",
                "Smith",
                "bob@example.com",
                Set.of(Role.TEACHER),
                PasswordHash.of("other-hash"),
                false
        );

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void newUserShouldStartAtVersionZero() {
        User user = createValidStudent();

        assertEquals(0L, user.getVersion());
    }

    @Test
    void updateProfileShouldIncrementVersionWhenProfileChanges() {
        User user = createValidStudent();

        long initialVersion = user.getVersion();

        user.updateProfile(
                "fluffy",
                "Fluffy",
                "Crittenden"
        );

        assertEquals(initialVersion + 1, user.getVersion());
    }

    @Test
    void updateProfileShouldNotIncrementVersionWhenNothingChanges() {
        User user = createValidStudent();

        long initialVersion = user.getVersion();

        user.updateProfile(
                user.getUsername(),
                user.getName(),
                user.getSurname()
        );

        assertEquals(initialVersion, user.getVersion());
    }

    @Test
    void grantRoleShouldIncrementVersion() {
        User user = createValidStudent();

        long version = user.getVersion();

        user.grantRole(Role.TEACHER);

        assertEquals(version + 1, user.getVersion());
    }

    @Test
    void revokeRoleShouldIncrementVersion() {
        User user = createValidStudent();
        user.grantRole(Role.TEACHER);

        long version = user.getVersion();

        user.revokeRole(Role.TEACHER);

        assertEquals(version + 1, user.getVersion());
    }

    @Test
    void confirmEmailShouldIncrementVersionOnlyOnce() {
        User user = createValidStudent();

        long version = user.getVersion();

        user.confirmEmail();

        assertEquals(version + 1, user.getVersion());

        user.confirmEmail();

        assertEquals(version + 1, user.getVersion());
    }

    @Test
    void changePasswordShouldIncrementVersion() {
        User user = createValidStudent();

        long version = user.getVersion();

        user.changePassword(PasswordHash.of("different-hash"));

        assertEquals(version + 1, user.getVersion());
    }


    //################################################ Helpers #########################################
    private User createValidStudent() {
        return User.createStudent(
                "tim",
                "Tim",
                "Crittenden",
                "tim@example.com",
                PasswordHash.of("hashed-password")
        );
    }

    private User createValidTeacherFromInvitation() {
        return User.createFromInvitation(
                "tim",
                "Tim",
                "Crittenden",
                "tim@example.com",
                PasswordHash.of("hashed-password"),
                Role.TEACHER
        );
    }

}
