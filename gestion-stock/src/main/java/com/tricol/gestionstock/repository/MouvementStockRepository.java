package com.tricol.gestionstock.repository;

import com.tricol.gestionstock.entity.MouvementStock;
import com.tricol.gestionstock.entity.Enums.TypeMouvement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long>, JpaSpecificationExecutor<MouvementStock> {

    
    @Query("SELECT m FROM MouvementStock m WHERE m.produit.id = :produitId ORDER BY m.dateMouvement DESC")
    List<MouvementStock> findByProduitId(@Param("produitId") Long produitId);

}
