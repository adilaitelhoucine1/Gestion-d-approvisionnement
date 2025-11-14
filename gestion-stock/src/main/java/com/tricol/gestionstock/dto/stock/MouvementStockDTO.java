package com.tricol.gestionstock.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MouvementStockDTO {
    private Long id;
    private Long produitId;
    private String produitReference;
    private String produitNom;
    private String typeMouvement;
    private Integer quantite;
    private LocalDateTime dateMouvement;
    private String numeroLot;
    private BigDecimal prixUnitaire;
    private String referenceDocument;
    private String observation;
}

