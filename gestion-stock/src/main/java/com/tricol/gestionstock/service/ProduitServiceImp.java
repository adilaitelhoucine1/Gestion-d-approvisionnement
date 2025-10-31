package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.produit.CreateProduitDTO;
import com.tricol.gestionstock.dto.produit.ProduitResponseDTO;
import com.tricol.gestionstock.entity.Produit;
import com.tricol.gestionstock.mapper.ProduitMapper;
import com.tricol.gestionstock.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProduitServiceImp implements ProductSservice {

    private final ProduitRepository produitRepository;
    private final ProduitMapper produitMapper;

    @Override
    public ProduitResponseDTO createProduit(CreateProduitDTO createDTO) {


        if (produitRepository.existsByReference(createDTO.getReference())) {
            //log.error("La reference  existe deja");
            throw new IllegalArgumentException(
                    "Un produit existe deja aec ce ref"
            );
        }

        Produit produit = produitMapper.toEntity(createDTO);
        produitRepository.save(produit);
        ProduitResponseDTO produitResponseDTO = produitMapper.toResponseDTO(produit);
        log.info("Produit deja cree avecc suceees");
        return produitResponseDTO;
    }
}