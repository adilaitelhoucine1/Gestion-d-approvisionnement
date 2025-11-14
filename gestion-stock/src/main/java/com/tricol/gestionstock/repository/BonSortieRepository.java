package com.tricol.gestionstock.repository;

import com.tricol.gestionstock.entity.BonSortie;
import com.tricol.gestionstock.entity.Enums.StatutBonSortie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BonSortieRepository extends JpaRepository<BonSortie, Long> {

    boolean existsByNumeroBon(String numeroBon);

    Optional<BonSortie> findByNumeroBon(String numeroBon);

    List<BonSortie> findByAtelierDestinataireOrderByDateSortieDesc(String atelierDestinataire);

    List<BonSortie> findByStatutOrderByDateSortieDesc(StatutBonSortie statut);

    @Query("SELECT DISTINCT b FROM BonSortie b LEFT JOIN FETCH b.lignes WHERE b.id = :id")
    Optional<BonSortie> findByIdWithLignes(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT b FROM BonSortie b LEFT JOIN FETCH b.lignes ORDER BY b.dateSortie DESC")
    List<BonSortie> findAllWithLignes();


}

