package com.tricol.gestionstock.repository;

import com.tricol.gestionstock.entity.LotStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LotStockRepository extends JpaRepository<LotStock, Long> {

    
    Optional<LotStock> findByNumeroLot(String numeroLot);

   
    @Query("SELECT l FROM LotStock l WHERE l.produit.id = :produitId " +
            "AND l.quantiteRestante > 0 ORDER BY l.dateEntree ASC")
    List<LotStock> findLotsDisponiblesByProduitFIFO(@Param("produitId") Long produitId);

   
    @Query("SELECT l FROM LotStock l WHERE l.produit.id = :produitId ORDER BY l.dateEntree DESC")
    List<LotStock> findByProduitId(@Param("produitId") Long produitId);

    @Query("SELECT l FROM LotStock l WHERE l.commande.id = :commandeId")
    List<LotStock> findByCommandeId(@Param("commandeId") Long commandeId);
}
