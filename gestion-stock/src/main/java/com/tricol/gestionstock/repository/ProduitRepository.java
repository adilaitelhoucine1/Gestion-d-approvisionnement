package com.tricol.gestionstock.repository;

import com.tricol.gestionstock.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

   
    Optional<Produit> findByReference(String reference);

    
    boolean existsByReference(String reference);

    
    boolean existsByReferenceAndIdNot(String reference, Long id);

  
    List<Produit> findByCategorieOrderByNomAsc(String categorie);

    
    @Query("SELECT p FROM Produit p WHERE p.stockActuel < p.pointDeCommande ORDER BY p.nom ASC")
    List<Produit> findProduitsEnAlerte();

    @Query("SELECT p FROM Produit p WHERE " +
           "LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.reference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.categorie) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Produit> searchProduits(@Param("keyword") String keyword);
}
