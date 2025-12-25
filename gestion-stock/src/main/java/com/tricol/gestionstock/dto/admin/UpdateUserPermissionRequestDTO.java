package com.tricol.gestionstock.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserPermissionRequestDTO {

    @NotBlank(message = "Le nom de la permission est obligatoire")
    private String permissionName;

    @NotNull(message = "Le statut granted est obligatoire")
    private Boolean granted; // true = accorder, false = révoquer
}

