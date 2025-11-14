package com.tricol.gestionstock.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtatStockDTO {
    private Long produitId;
    private String reference;
    private String nom;
    private String categorie;
    private Integer quantiteDisponible;
    private BigDecimal valorisation;
    private Integer pointDeCommande;
    private Boolean enAlerte;
}

