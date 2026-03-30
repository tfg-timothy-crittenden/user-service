package com.timcritt.tfg.infrastructure.web.dto;

import java.util.Set;

public class UserDto {
    private Long id;
    private String username;
    private String name;
    private String surname;
    private String email;
    private Set<String> roles;
    private Boolean verified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Boolean isVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
}
