package com.tricol.gestionstock.dto.produit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProduitResponseDTO {
    private Long id;
    private String reference;
    private String nom;
    private BigDecimal prixUnitaire;
    private Integer stockActuel;
}
