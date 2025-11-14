package com.tricol.gestionstock.service.impl;

import com.tricol.gestionstock.dto.produit.CreateProduitDTO;
import com.tricol.gestionstock.dto.produit.ProduitResponseDTO;
import com.tricol.gestionstock.dto.produit.UpdateProduitDTO;
import com.tricol.gestionstock.entity.Produit;
import com.tricol.gestionstock.mapper.ProduitMapper;
import com.tricol.gestionstock.repository.ProduitRepository;
import com.tricol.gestionstock.service.ProductSservice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


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
            throw new IllegalArgumentException(
                    "Un produit existe deja aec ce ref"
            );
        }
        Produit produit = produitMapper.toEntity(createDTO);
        produitRepository.save(produit);
        ProduitResponseDTO produitResponseDTO = produitMapper.toResponseDTO(produit);
        return produitResponseDTO;
    }
    @Override
    public List<ProduitResponseDTO> getAllProducts(){
        List<Produit> produits = produitRepository.findAll();
        List<ProduitResponseDTO> responseDTOs = produits.stream()
                .map(produitMapper::toResponseDTO)
                .collect(Collectors.toList());
        return responseDTOs;
    }
    public void deleteProduct(Long id){
        if (!produitRepository.existsById(id)) {
            throw new IllegalArgumentException("Produit non trouvé avec l'ID: " + id);
        }
        produitRepository.deleteById(id);
    }
    public ProduitResponseDTO updateProduit(UpdateProduitDTO updateDTO , Long id){

        Produit existingProduit = produitRepository.findById(id)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("produit non trouve");
                });
        if (updateDTO.getReference() != null &&
                !updateDTO.getReference().equals(existingProduit.getReference())) {
            if (produitRepository.existsByReference(updateDTO.getReference())) {
                log.error(" La référence deja kayn", updateDTO.getReference());
                throw new IllegalArgumentException(
                        "La référence deja kayn "
                );
            }
        }
        produitMapper.updateEntityFromDTO(updateDTO, existingProduit);
        Produit updatedProduit = produitRepository.save(existingProduit);
        return produitMapper.toResponseDTO(updatedProduit);

    }
    public ProduitResponseDTO getProduitById(Long id){

        Produit produit = produitRepository.findById(id).
                orElseThrow(()->new IllegalArgumentException("le produit avec cet id n existe pas"));
        return produitMapper.toResponseDTO(produit);
    }
    public ProduitResponseDTO getProductStock(Long id){
        Produit produit = produitRepository.findById(id).
                orElseThrow(()->new IllegalArgumentException("le produit avec cet id n existe pas"));
        return produitMapper.toResponseDTO(produit);
    }




}