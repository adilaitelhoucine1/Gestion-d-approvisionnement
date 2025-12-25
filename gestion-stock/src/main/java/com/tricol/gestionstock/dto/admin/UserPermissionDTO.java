package com.tricol.gestionstock.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermissionDTO {

    private Long permissionId;
    private String permissionName;
    private String permissionCategory;
    private Boolean granted;
    private LocalDateTime assignedAt;
    private String assignedBy;
}

