package com.timcritt.tfg.infrastructure.persistence.jpa;

import com.timcritt.tfg.domain.model.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")

public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter @Setter
    private String username;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String surname;

    @Getter @Setter
    @Column(name = "password_hash")
    private String passwordHash;

    @Getter @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Getter @Setter
    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();


    public Set<Role> getUserRoles() {
        return roles;
    }

    public void setUserRoles(Set<Role> roles) {
        this.roles = (roles == null) ? new HashSet<>() : new HashSet<>(roles);
    }
}