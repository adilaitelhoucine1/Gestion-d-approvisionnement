package com.tricol.gestionstock.repository;

import com.tricol.gestionstock.entity.LigneBonSortie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneBonSortieRepository extends JpaRepository<LigneBonSortie, Long> {
    List<LigneBonSortie> findByBonSortieId(Long bonSortieId);
    void deleteByBonSortieId(Long bonSortieId);
}

