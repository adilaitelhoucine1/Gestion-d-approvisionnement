package com.tricol.gestionstock.dto.fournisseur;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FournisseurRequestDTO {

    @NotBlank(message = "La raison sociale est obligatoire")
    @Size(max = 200, message = "La raison sociale ne doit pas dépasser 200 caracteres")
    private String raisonSociale;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
    private String ville;

    @NotBlank(message = "Le nom de la personne de contact est obligatoire")
    @Size(max = 150, message = "Le nom de la personne de contact ne doit pas dépasser 150 caractères")
    private String personneContact;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Format de téléphone invalide")
    private String telephone;

    @NotBlank(message = "L'ICE est obligatoire")
    @Size(min = 15, max = 15, message = "L'ICE doit contenir exactement 15 caractères")
    private String ice;
}
