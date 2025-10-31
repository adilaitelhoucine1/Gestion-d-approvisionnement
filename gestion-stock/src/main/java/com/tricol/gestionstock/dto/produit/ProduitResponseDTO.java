package com.tricol.gestionstock.dto.produit;

import java.math.BigDecimal;

public class ProduitResponseDTO {
    private Long id;
    private String reference;
    private String nom;
    private BigDecimal prixUnitaire;
    private Integer stockActuel;
}
