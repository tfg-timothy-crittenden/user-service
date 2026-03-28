package com.timcritt.tfg.domain.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.Nullable;

public class User {

    private Long id;
    private String username;
    private String name;
    private String surname;
    private String email;
    private String passwordHash;
    private Set<Role> roles = new HashSet<>();

    public User() {}

    public User(Long id, String username, String name, String surname, String email, Set<Role> roles, String passwordHash) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.roles = (roles == null) ? new HashSet<>() : new HashSet<>(roles);
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public User setId(Long id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public User setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = (roles == null) ? new HashSet<>() : new HashSet<>(roles);
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
    public int hashCode() {
        return Objects.hash(id, username, name, surname, email);
    }

    public @Nullable String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
