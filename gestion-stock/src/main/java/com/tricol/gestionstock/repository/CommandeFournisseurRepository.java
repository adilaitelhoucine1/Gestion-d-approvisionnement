package com.tricol.gestionstock.repository;

import com.tricol.gestionstock.entity.CommandeFournisseur;
import com.tricol.gestionstock.entity.Enums.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeFournisseurRepository extends JpaRepository<CommandeFournisseur, Long> {

    
    boolean existsByNumeroCommande(String numeroCommande);

    
    boolean existsByNumeroCommandeAndIdNot(String numeroCommande, Long id);

    
    Optional<CommandeFournisseur> findByNumeroCommande(String numeroCommande);

    //kan9lb 3la ga3 commend li kaynin dyal fournisseur
    @Query("SELECT c FROM CommandeFournisseur c WHERE c.fournisseur.id = :fournisseurId ORDER BY c.dateCommande DESC")
    List<CommandeFournisseur> findByFournisseurId(@Param("fournisseurId") Long fournisseurId);

    
    List<CommandeFournisseur> findByStatutOrderByDateCommandeDesc(StatutCommande statut);

    //kan9lb 3la commend bin 2 dyal dates
    @Query("SELECT c FROM CommandeFournisseur c WHERE c.dateCommande BETWEEN :dateDebut AND :dateFin ORDER BY c.dateCommande DESC")
    List<CommandeFournisseur> findByDateCommandeBetween(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin
    );

    // b fourni w statu
    @Query("SELECT c FROM CommandeFournisseur c WHERE c.fournisseur.id = :fournisseurId AND c.statut = :statut ORDER BY c.dateCommande DESC")
    List<CommandeFournisseur> findByFournisseurIdAndStatut(
            @Param("fournisseurId") Long fournisseurId,
            @Param("statut") StatutCommande statut
    );

    // prd w status
    @Query("SELECT c FROM CommandeFournisseur c WHERE c.dateCommande BETWEEN :dateDebut AND :dateFin AND c.statut = :statut ORDER BY c.dateCommande DESC")
    List<CommandeFournisseur> findByPeriodeAndStatut(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("statut") StatutCommande statut
    );

    
     // hada recherch avance b  multicriteres 
     
    @Query("SELECT c FROM CommandeFournisseur c WHERE " +
           "(:fournisseurId IS NULL OR c.fournisseur.id = :fournisseurId) AND " +
           "(:statut IS NULL OR c.statut = :statut) AND " +
           "(:dateDebut IS NULL OR c.dateCommande >= :dateDebut) AND " +
           "(:dateFin IS NULL OR c.dateCommande <= :dateFin) " +
           "ORDER BY c.dateCommande DESC")
    List<CommandeFournisseur> findByFilters(
            @Param("fournisseurId") Long fournisseurId,
            @Param("statut") StatutCommande statut,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin
    );

   
    List<CommandeFournisseur> findAllByOrderByDateCommandeDesc();
}
