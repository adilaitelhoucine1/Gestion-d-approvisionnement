package com.tricol.gestionstock.dto.commande;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneCommandeDTO {

    private Long id;

    @NotNull(message = "L'ID du produit est obligatoire")
    private Long produitId;

    private String produitNom;
    private String produitReference;

    @NotNull(message = "La quantite est obligatoire")
    @Min(value = 1, message = "La quantite doit être au moins 1")
    private Integer quantite;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix unitaire doit être superieur a 0")
    private BigDecimal prixUnitaire;

    private BigDecimal sousTotal;
}
