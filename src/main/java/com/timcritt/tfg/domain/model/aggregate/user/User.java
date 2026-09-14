package com.timcritt.tfg.domain.model.aggregate.user;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.timcritt.tfg.application.exception.RoleNotFoundException;
import com.timcritt.tfg.domain.exception.MustNotRemoveLastUserRoleException;
import com.timcritt.tfg.domain.exception.UserDoesNotHaveRoleException;
import com.timcritt.tfg.domain.model.Role;
import org.jspecify.annotations.Nullable;

public class User {

    private Long id;
    private String username;
    private String name;
    private String surname;
    private String email;
    private PasswordHash passwordHash;
    private Set<Role> roles = new HashSet<>();
    private boolean verified = false;

    private User(Long id, String username, String name, String surname, String email, Set<Role> roles, PasswordHash passwordHash, boolean verified) {

        requireNonBlank(username, "username");
        requireNonBlank(name, "name");
        requireNonBlank(surname, "surname");
        requireNonBlank(email, "email");

        if (passwordHash == null) {
            throw new NullPointerException("passwordHash cannot be null");
        }

        if (roles == null || roles.isEmpty() || roles.contains(null)) {
            throw new IllegalArgumentException(
                    "user must have at least one valid role");
        }

        this.id = id;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.roles = new HashSet<>(roles);
        this.passwordHash = passwordHash;
        this.verified = verified;
    }
    //################################################### FACTORY METHODS ##############################################
    public static User createStudent(String username, String name, String surname, String email, PasswordHash passwordHash) {

        Set<Role> roles = new HashSet<>();
        roles.add(Role.STUDENT);
        return new User(null, username, name, surname, email, roles, passwordHash, false);
    }

    public static User createFromInvitation(String username, String name, String surname, String email, PasswordHash passwordHash, Role roleToGrant) {
        Set<Role> roles = new HashSet<>();
        roles.add(roleToGrant);
        return new User(null, username, name, surname, email, roles, passwordHash, true);
    }

    public static User rehydrate(Long id, String username, String name, String surname, String email, Set<Role> roles, PasswordHash passwordHash, boolean verified) {
        return new User(id, username, name, surname, email, roles, passwordHash, verified);
    }

    //######################################################## BUSINESS LOGIC #########################################

    public void updateProfile(String name, String surname) {
        requireNonBlank(name, "name");
        requireNonBlank(surname, "surname");
        this.name = name;
        this.surname = surname;
    }

    public void updateUsername(String username) {
        requireNonBlank(username, "username");
        this.username = username;
    }

    public void changeEmail(String newEmail) {

        requireNonBlank(newEmail, "newEmail");

        if (Objects.equals(this.email, newEmail)) {
            return;
        }

        this.email = newEmail;
        this.verified = false;
    }

    public void confirmEmail() {
        this.verified = true;
    }

    public void grantRole(Role role) {
        if(role == null ) {
            throw new NullPointerException("role cannot be null");
        }

        if (roles.contains(role)) {
            throw new IllegalArgumentException("Role already assigned");
        }
        roles.add(role);
    }

    public void revokeRole(Role role) {
        if(role == null ) {
            throw new NullPointerException("role cannot be null");
        }
        if (!roles.contains(role)) {
            throw new UserDoesNotHaveRoleException(role);
        }

        if(roles.size() == 1) {
            throw new MustNotRemoveLastUserRoleException();
        }

        roles.remove(role);
    }

    public boolean hasRole(Role role) {
        if (role == null) {
            throw new NullPointerException("role cannot be null");
        }
        return roles.contains(role);
    }

    public void changePassword(PasswordHash passwordHash) {
        if(passwordHash == null) {
            throw new NullPointerException("passwordHash cannot be null");
        }
        this.passwordHash = passwordHash;
    }
    public boolean isVerified() { return verified; }

    //######################################## GETTERS #############################################################

    public PasswordHash getPasswordHash() { return passwordHash;}
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }

    //Return an immutable copy to prevent bypassing business logic by direct mutation of returned collection
    public Set<Role> getRoles() {
        return Set.copyOf(roles);
    }


    private static void requireNonBlank(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        //Only compare identity, as this is stable when not null
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        //Hash should be stable, so use class type, not ID, as ID can be null.
        return getClass().hashCode();
    }

}
