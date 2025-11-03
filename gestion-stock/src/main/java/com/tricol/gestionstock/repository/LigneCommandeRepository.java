package com.tricol.gestionstock.repository;

import com.tricol.gestionstock.entity.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {

    
    @Query("SELECT l FROM LigneCommande l WHERE l.commande.id = :commandeId")
    List<LigneCommande> findByCommandeId(@Param("commandeId") Long commandeId);

    
    void deleteByCommandeId(Long commandeId);
}
