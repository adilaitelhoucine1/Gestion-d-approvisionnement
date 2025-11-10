package com.tricol.gestionstock.dto.bonsortie;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneBonSortieDTO {

    private Long id;

    @NotNull(message = "L'ID du produit est obligatoire")
    private Long produitId;

    private String produitNom;
    private String produitReference;

    @NotNull(message = "La quantite demandee est obligatoire")
    @Min(value = 1, message = "La quantite demandee doit être au moins 1")
    private Integer quantiteDemandee;
}

