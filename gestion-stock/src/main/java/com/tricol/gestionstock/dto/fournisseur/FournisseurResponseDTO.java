package com.tricol.gestionstock.dto.fournisseur;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FournisseurResponseDTO {

    private Long id;
    private String raisonSociale;
    private String adresse;
    private String ville;
    private String personneContact;
    private String email;
    private String telephone;
    private String ice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
