package com.tricol.gestionstock.repository;

import com.tricol.gestionstock.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    
    boolean existsByEmail(String email);

    boolean existsByIce(String ice);

   
    boolean existsByEmailAndIdNot(String email, Long id);

    
    boolean existsByIceAndIdNot(String ice, Long id);

    Optional<Fournisseur> findByEmail(String email);

    Optional<Fournisseur> findByIce(String ice);

    List<Fournisseur> findByVilleContainingIgnoreCase(String ville);

   
    List<Fournisseur> findByRaisonSocialeContainingIgnoreCase(String raisonSociale);

    

}
