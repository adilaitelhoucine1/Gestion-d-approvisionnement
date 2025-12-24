package com.tricol.gestionstock.entity.security;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserPermissionId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "permission_id")
    private Long permissionId;
}

