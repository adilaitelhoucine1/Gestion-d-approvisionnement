package com.tricol.gestionstock.entity.security;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean accountNonLocked = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean accountNonExpired = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean credentialsNonExpired = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<RoleApp> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserPermission> customPermissions = new HashSet<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastLoginAt;

    // Helper methods for managing custom permissions
    public void addCustomPermission(UserPermission userPermission) {
        this.customPermissions.add(userPermission);
        userPermission.setUser(this);
    }

    public void removeCustomPermission(UserPermission userPermission) {
        this.customPermissions.remove(userPermission);
        userPermission.setUser(null);
    }
}

