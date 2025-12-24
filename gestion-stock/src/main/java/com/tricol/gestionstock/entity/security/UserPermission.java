package com.tricol.gestionstock.entity.security;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_permissions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermission {

    @EmbeddedId
    private UserPermissionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserApp user;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private Permission permission;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true; // true = granted, false = denied

    @Column(length = 255)
    private String reason; // Why this permission was customized

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String modifiedBy; // Username of admin who modified this permission
}

