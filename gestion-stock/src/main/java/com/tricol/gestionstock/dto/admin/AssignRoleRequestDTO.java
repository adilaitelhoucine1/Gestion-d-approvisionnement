package com.tricol.gestionstock.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignRoleRequestDTO {

    @NotBlank(message = "Le nom du rôle est obligatoire")
    private String roleName;
}

