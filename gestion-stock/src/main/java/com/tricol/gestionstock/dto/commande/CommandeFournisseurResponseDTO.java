package com.tricol.gestionstock.dto.commande;

import com.tricol.gestionstock.entity.Enums.StatutCommande;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeFournisseurResponseDTO {

    private Long id;
    private String numeroCommande;
    private LocalDate dateCommande;
    private LocalDate dateReception;
    private StatutCommande statut;
    private BigDecimal montantTotal;
    private String observations;
    
    private Long fournisseurId;
    private String fournisseurRaisonSociale;
    private String fournisseurEmail;
    private String fournisseurTelephone;
    
    // Lignes dyal commande

    //builder dertha bach npassiw param l construct b . bhal stram d blasst maykon ldakhel 
    @Builder.Default
    private List<LigneCommandeDTO> lignesCommande = new ArrayList<>();

  
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
