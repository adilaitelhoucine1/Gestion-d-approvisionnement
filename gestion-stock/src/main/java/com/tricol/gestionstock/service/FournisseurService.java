package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.fournisseur.FournisseurRequestDTO;
import com.tricol.gestionstock.dto.fournisseur.FournisseurResponseDTO;

import java.util.List;

public interface FournisseurService {

   
    FournisseurResponseDTO createFournisseur(FournisseurRequestDTO requestDTO);

    
    List<FournisseurResponseDTO> getAllFournisseurs();

   
    FournisseurResponseDTO getFournisseurById(Long id);

    FournisseurResponseDTO updateFournisseur(Long id, FournisseurRequestDTO requestDTO);

   
    void deleteFournisseur(Long id);

   
    List<FournisseurResponseDTO> searchByVille(String ville);

  
    List<FournisseurResponseDTO> searchByRaisonSociale(String raisonSociale);

    

    
    FournisseurResponseDTO getFournisseurByEmail(String email);

   
    FournisseurResponseDTO getFournisseurByIce(String ice);


    List<FournisseurResponseDTO>filtered();
}
