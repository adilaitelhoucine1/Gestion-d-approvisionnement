package com.tricol.gestionstock.entity.security;

import com.tricol.gestionstock.entity.Enums.ActionType;
import com.tricol.gestionstock.entity.Enums.ResourceType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ResourceType resource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActionType action;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, unique = true, length = 100)
    private String permissionKey; // e.g., "FOURNISSEUR:CREATE"

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Permission(ResourceType resource, ActionType action, String description) {
        this.resource = resource;
        this.action = action;
        this.description = description;
        this.permissionKey = resource.name() + ":" + action.name();
    }
}

