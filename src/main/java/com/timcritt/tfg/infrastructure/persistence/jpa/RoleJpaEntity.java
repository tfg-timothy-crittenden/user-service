package com.timcritt.tfg.infrastructure.persistence.jpa;

import com.timcritt.tfg.domain.model.RoleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "role")
public class RoleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, unique = true)
    @Getter @Setter
    private RoleType roleType;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Getter @Setter
    private Set<UserJpaEntity> users = new HashSet<>();

    public RoleJpaEntity() {
    }

    public RoleJpaEntity(Long id, RoleType roleType) {
        this.id = id;
        this.roleType = roleType;
    }
}