package com.tricol.gestionstock.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerteStockDTO {
    private Long produitId;
    private String reference;
    private String nom;
    private String categorie;
    private Integer stockActuel;
    private Integer pointDeCommande;
    private Integer manquant;
}

