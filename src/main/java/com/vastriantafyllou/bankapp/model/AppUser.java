package com.vastriantafyllou.bankapp.model;

import com.vastriantafyllou.bankapp.core.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 64)
    private String firstName;

    @Column(length = 64)
    private String lastName;

    @Column(length = 128)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private Boolean blocked = false;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (blocked == null) blocked = false;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 16)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppUser appUser)) return false;
        return id != null && id.equals(appUser.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
