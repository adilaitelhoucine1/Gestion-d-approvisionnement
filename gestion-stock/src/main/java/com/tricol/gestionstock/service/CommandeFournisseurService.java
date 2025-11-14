package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.commande.CommandeFournisseurRequestDTO;
import com.tricol.gestionstock.dto.commande.CommandeFournisseurResponseDTO;
import com.tricol.gestionstock.dto.commande.ReceptionCommandeDTO;
import com.tricol.gestionstock.entity.Enums.StatutCommande;

import java.time.LocalDate;
import java.util.List;

public interface CommandeFournisseurService {

   
    CommandeFournisseurResponseDTO createCommande(CommandeFournisseurRequestDTO requestDTO);

    List<CommandeFournisseurResponseDTO> getAllCommandes();
    
    CommandeFournisseurResponseDTO getCommandeById(Long id);

    CommandeFournisseurResponseDTO updateCommande(Long id, CommandeFournisseurRequestDTO requestDTO);

    void deleteCommande(Long id);

    List<CommandeFournisseurResponseDTO> getCommandesByFournisseur(Long fournisseurId);


 
    CommandeFournisseurResponseDTO receptionnerCommande(Long id, ReceptionCommandeDTO receptionDTO);



    CommandeFournisseurResponseDTO annulerCommande(Long id);

    CommandeFournisseurResponseDTO validerCommande(Long id);



}
