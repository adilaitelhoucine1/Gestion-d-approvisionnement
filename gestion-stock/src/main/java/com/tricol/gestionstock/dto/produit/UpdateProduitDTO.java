package com.tricol.gestionstock.dto.produit;



import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UpdateProduitDTO {

    @Size(min = 3, max = 50, message = "La référence doit contenir entre 3 et 50 caractères")
    private String reference;

    @Size(min = 3, max = 200, message = "Le nom doit contenir entre 3 et 200 caractères")
    private String nom;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @DecimalMin(value = "0.01", message = "Le prix unitaire doit être supérieur à 0")
    private BigDecimal prixUnitaire;

    @Size(max = 100, message = "La catégorie ne peut pas dépasser 100 caractères")
    private String categorie;

    @Min(value = 1, message = "Le point de commande doit être au moins 1")
    private Integer pointDeCommande;

    @Size(max = 20, message = "L'unité de mesure ne peut pas dépasser 20 caractères")
    private String uniteMesure;
}
