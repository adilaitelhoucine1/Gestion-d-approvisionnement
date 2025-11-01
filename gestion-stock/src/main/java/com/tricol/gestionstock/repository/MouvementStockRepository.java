package com.tricol.gestionstock.repository;

import com.tricol.gestionstock.entity.MouvementStock;
import com.tricol.gestionstock.entity.Enums.TypeMouvement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    
    @Query("SELECT m FROM MouvementStock m WHERE m.produit.id = :produitId ORDER BY m.dateMouvement DESC")
    List<MouvementStock> findByProduitId(@Param("produitId") Long produitId);

    
    @Query("SELECT m FROM MouvementStock m WHERE m.commande.id = :commandeId ORDER BY m.dateMouvement DESC")
    List<MouvementStock> findByCommandeId(@Param("commandeId") Long commandeId);

    
    List<MouvementStock> findByTypeMouvementOrderByDateMouvementDesc(TypeMouvement typeMouvement);
}
