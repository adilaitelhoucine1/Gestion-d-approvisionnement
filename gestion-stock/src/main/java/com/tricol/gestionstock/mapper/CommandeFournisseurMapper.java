package com.tricol.gestionstock.mapper;

import com.tricol.gestionstock.dto.commande.CommandeFournisseurRequestDTO;
import com.tricol.gestionstock.dto.commande.CommandeFournisseurResponseDTO;
import com.tricol.gestionstock.dto.commande.LigneCommandeDTO;
import com.tricol.gestionstock.entity.CommandeFournisseur;
import com.tricol.gestionstock.entity.LigneCommande;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommandeFournisseurMapper {


    @Mapping(target = "fournisseurId", source = "fournisseur.id")
    @Mapping(target = "fournisseurRaisonSociale", source = "fournisseur.raisonSociale")
    @Mapping(target = "fournisseurEmail", source = "fournisseur.email")
    @Mapping(target = "fournisseurTelephone", source = "fournisseur.telephone")
    CommandeFournisseurResponseDTO toResponseDTO(CommandeFournisseur commande);


    List<CommandeFournisseurResponseDTO> toResponseDTOList(List<CommandeFournisseur> commandes);


    @Mapping(target = "produitId", source = "produit.id")
    @Mapping(target = "produitNom", source = "produit.nom")
    @Mapping(target = "produitReference", source = "produit.reference")
    LigneCommandeDTO toLigneCommandeDTO(LigneCommande ligne);


    List<LigneCommandeDTO> toLigneCommandeDTOList(List<LigneCommande> lignes);
}
