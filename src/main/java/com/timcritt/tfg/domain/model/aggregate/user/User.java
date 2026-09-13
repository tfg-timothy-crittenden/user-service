package com.timcritt.tfg.domain.model.aggregate.user;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.RoleType;
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

    public User(Long id, String username, String name, String surname, String email, Set<Role> roles, PasswordHash passwordHash) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.roles = (roles == null) ? new HashSet<>() : new HashSet<>(roles);
        this.passwordHash = passwordHash;
    }
    //################################################### FACTORY METHODS #############################################################################
    public static User createStudent(String username, String name, String surname, String email, PasswordHash passwordHash) {

        Set<Role> roles = new HashSet<>();
        Role defaultRole = new Role();
        defaultRole.setRoleType(RoleType.STUDENT);
        roles.add(defaultRole);
        return new User(null, username, name, surname, email, roles, passwordHash);
    }

    public static User createFromInvitation(String username, String name, String surname, String email, PasswordHash passwordHash, PlatformInvitation invitation) {
        Set<Role> roles = new HashSet<>();
        Role defaultRole = new Role();
        defaultRole.setRoleType(invitation.getRoleType());
        roles.add(defaultRole);
        return new User(null, username, name, surname, email, roles, passwordHash);
    }

    public static User rehydrate(Long id, String username, String name, String surname, String email, Set<Role> roles, PasswordHash passwordHash) {
        return new User(id, username, name, surname, email, roles, passwordHash);
    }

    //#################################################################################################################################################

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

    public boolean checkRole(Role role) {
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

    public @Nullable PasswordHash getPasswordHash() { return passwordHash; }

    public Long getId() { return id; }
    public User setId(Long id) { this.id = id; return this; }
    public String getUsername() { return username; }
    public User setUsername(String username) { this.username = username; return this; }
    public String getName() { return name; }
    public User setName(String name) { this.name = name; return this; }
    public String getSurname() { return surname; }
    public User setSurname(String surname) { this.surname = surname; return this; }
    public String getEmail() { return email; }
    public User setEmail(String email) { this.email = email; return this; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = (roles == null) ? new HashSet<>() : new HashSet<>(roles); }

    public User addRoleType(RoleType roleType) {
        if (roleType != null) {
            this.roles.add(new Role(null, roleType));
        }
        return this;
    }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

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
