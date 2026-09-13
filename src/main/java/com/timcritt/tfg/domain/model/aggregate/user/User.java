package com.timcritt.tfg.domain.model.aggregate.user;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.aggregate.platformInvitation.PlatformInvitation;
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

    public User() {}

    private User(Long id, String username, String name, String surname, String email, Set<Role> roles, PasswordHash passwordHash, boolean verified) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.roles = (roles == null) ? new HashSet<>() : new HashSet<>(roles);
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
        this.name = name;
        this.surname = surname;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateEmail(String email) {
        this.email = email;
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
            throw new IllegalArgumentException("Role not assigned");
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

    //############################################################## GETTERS ###########################################

    public @Nullable PasswordHash getPasswordHash() { return passwordHash; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
    public Set<Role> getRoles() {
        return Set.copyOf(roles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id)
                && Objects.equals(username, user.username)
                && Objects.equals(name, user.name)
                && Objects.equals(surname, user.surname)
                && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() { return Objects.hash(id, username, name, surname, email); }



}
