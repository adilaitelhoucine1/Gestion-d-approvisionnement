package com.tricol.gestionstock.repository;

import com.tricol.gestionstock.entity.CommandeFournisseur;
import com.tricol.gestionstock.entity.Enums.StatutCommande;
import com.tricol.gestionstock.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.xml.crypto.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeFournisseurRepository extends JpaRepository<CommandeFournisseur, Long> {

    
    boolean existsByNumeroCommande(String numeroCommande);


    @Query("SELECT c FROM CommandeFournisseur c WHERE c.fournisseur.id = :fournisseurId ORDER BY c.dateCommande DESC")
    List<CommandeFournisseur> findByFournisseurId(@Param("fournisseurId") Long fournisseurId);

    

     @Query("SELECT c FROM CommandeFournisseur c WHERE c.dateCommande BETWEEN :dateDebut AND :dateFin ORDER BY c.dateCommande DESC")
    List<CommandeFournisseur> findByDateCommandeBetween(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin
    );

     @Query("SELECT c FROM CommandeFournisseur c WHERE c.fournisseur.id = :fournisseurId AND c.statut = :statut ORDER BY c.dateCommande DESC")
    List<CommandeFournisseur> findByFournisseurIdAndStatut(
            @Param("fournisseurId") Long fournisseurId,
            @Param("statut") StatutCommande statut
    );

     @Query("SELECT c FROM CommandeFournisseur c WHERE c.dateCommande BETWEEN :dateDebut AND :dateFin AND c.statut = :statut ORDER BY c.dateCommande DESC")
    List<CommandeFournisseur> findByPeriodeAndStatut(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("statut") StatutCommande statut
    );

   
    List<CommandeFournisseur> findAllByOrderByDateCommandeDesc();

//    List<CommandeFournisseur>  findAllByMontantTotalGreaterThanOrderByMontantTotalDesc(BigDecimal Mon);
//    Integer findAllByStatutContaining(StatutCommande statutCommande);
//
//    List<CommandeFournisseur> findAllByMontantTotalLessThan(BigDecimal monatnt);

//    @Query("select  c from  CommandeFournisseur  c  where  c.montantTotal > :montant order by c.montantTotal  desc")
//    List<CommandeFournisseur> getall(@Param("montant") BigDecimal montant);

}
