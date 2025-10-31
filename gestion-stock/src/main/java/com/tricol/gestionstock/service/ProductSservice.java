package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.produit.CreateProduitDTO;
import com.tricol.gestionstock.dto.produit.ProduitResponseDTO;

public interface ProductSservice {
    public ProduitResponseDTO createProduit(CreateProduitDTO createDTO);
}
