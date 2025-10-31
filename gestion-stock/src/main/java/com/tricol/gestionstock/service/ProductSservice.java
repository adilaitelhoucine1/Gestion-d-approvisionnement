package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.produit.CreateProduitDTO;
import com.tricol.gestionstock.dto.produit.ProduitResponseDTO;

import java.util.List;

public interface ProductSservice {
    public ProduitResponseDTO createProduit(CreateProduitDTO createDTO);
    public List<ProduitResponseDTO> getAllProducts();
}
