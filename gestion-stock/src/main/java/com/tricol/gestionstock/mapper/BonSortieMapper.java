package com.tricol.gestionstock.mapper;

import com.tricol.gestionstock.dto.bonsortie.BonSortieResponseDTO;
import com.tricol.gestionstock.dto.bonsortie.LigneBonSortieDTO;
import com.tricol.gestionstock.entity.BonSortie;
import com.tricol.gestionstock.entity.LigneBonSortie;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BonSortieMapper {

    BonSortieResponseDTO toResponseDTO(BonSortie bonSortie);

    List<BonSortieResponseDTO> toResponseDTOList(List<BonSortie> bonsSortie);

    @Mapping(target = "produitId", source = "produit.id")
    @Mapping(target = "produitNom", source = "produit.nom")
    @Mapping(target = "produitReference", source = "produit.reference")
    LigneBonSortieDTO toLigneBonSortieDTO(LigneBonSortie ligne);

    List<LigneBonSortieDTO> toLigneBonSortieDTOList(List<LigneBonSortie> lignes);
}

