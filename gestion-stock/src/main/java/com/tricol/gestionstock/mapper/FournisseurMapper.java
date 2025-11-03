package com.tricol.gestionstock.mapper;

import com.tricol.gestionstock.dto.fournisseur.FournisseurRequestDTO;
import com.tricol.gestionstock.dto.fournisseur.FournisseurResponseDTO;
import com.tricol.gestionstock.entity.Fournisseur;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FournisseurMapper {

   
    FournisseurResponseDTO toResponseDTO(Fournisseur fournisseur);

   
    List<FournisseurResponseDTO> toResponseDTOList(List<Fournisseur> fournisseurs);

   
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commandes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Fournisseur toEntity(FournisseurRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commandes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(FournisseurRequestDTO requestDTO, @MappingTarget Fournisseur fournisseur);
}
