package com.tricol.gestionstock.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean enabled;
    private RoleDTO role;
    private List<UserPermissionDTO> customPermissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

