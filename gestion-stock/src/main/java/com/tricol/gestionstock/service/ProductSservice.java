package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.produit.CreateProduitDTO;
import com.tricol.gestionstock.dto.produit.ProductSearchCriteria;
import com.tricol.gestionstock.dto.produit.ProduitResponseDTO;
import com.tricol.gestionstock.dto.produit.UpdateProduitDTO;
import org.hibernate.query.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProductSservice {
    public ProduitResponseDTO createProduit(CreateProduitDTO createDTO);
    public List<ProduitResponseDTO> getAllProducts();
    public void deleteProduct(Long id);
    public ProduitResponseDTO updateProduit(UpdateProduitDTO updateProduitDTO , Long id);

   public ProduitResponseDTO getProduitById(Long id);

   public ProduitResponseDTO getProductStock(Long id);


}
