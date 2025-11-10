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
public class CreateProduitDTO {

    @NotBlank(message = "La référence du produit est obligatoire")
    @Size(min = 3, max = 50, message = "La référence doit contenir entre 3 et 50 caractères")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "La référence ne peut contenir que des lettres majuscules, chiffres et tirets")
    private String reference;

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(min = 3, max = 200, message = "Le nom doit contenir entre 3 et 200 caractères")
    private String nom;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix unitaire doit être supérieur à 0")
    @DecimalMax(value = "999999.99", message = "Le prix unitaire ne peut pas dépasser 999 999,99")
    @Digits(integer = 6, fraction = 2, message = "Le prix unitaire doit avoir au maximum 6 chiffres avant la virgule et 2 après")
    private BigDecimal prixUnitaire;

    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 100, message = "La catégorie ne peut pas dépasser 100 caractères")
    private String categorie;


    @Min(value = 0, message = "Le stock initial ne peut pas être négatif")
    private Integer stockInitial;

    @NotNull(message = "Le point de commande est obligatoire")
    @Min(value = 1, message = "Le point de commande doit être au moins 1")
    @Max(value = 10000, message = "Le point de commande ne peut pas dépasser 10 000")
    private Integer pointDeCommande;

    @NotBlank(message = "L'unité de mesure est obligatoire")
    @Size(max = 20, message = "L'unité de mesure ne peut pas dépasser 20 caractères")
    @Pattern(regexp = "^(unité|kg|g|litre|ml|mètre|cm|pièce|paquet|boîte|bobine)$",
            message = "Unité de mesure non valide")
    private String uniteMesure;

    
}