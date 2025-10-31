package com.tricol.gestionstock.mapper;

import com.tricol.gestionstock.dto.produit.CreateProduitDTO;
import com.tricol.gestionstock.dto.produit.ProduitResponseDTO;
import com.tricol.gestionstock.entity.Produit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ProduitMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stockActuel", source = "stockInitial")
    @Mapping(target = "createdAt" ,  ignore = true)
    @Mapping(target = "updatedAt" ,  ignore = true)
    Produit toEntity(CreateProduitDTO createDTO);


    ProduitResponseDTO toResponseDTO(Produit produit);
}