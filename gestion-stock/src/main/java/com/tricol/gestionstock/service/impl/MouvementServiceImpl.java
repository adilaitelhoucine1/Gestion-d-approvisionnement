package com.tricol.gestionstock.service.impl;

import com.tricol.gestionstock.dto.stock.MouvementStockDTO;
import com.tricol.gestionstock.entity.MouvementStock;
import com.tricol.gestionstock.entity.Produit;
import com.tricol.gestionstock.repository.MouvementStockRepository;
import com.tricol.gestionstock.service.MouvementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tricol.gestionstock.repository.ProduitRepository;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)


public class MouvementServiceImpl implements MouvementService {

    private final MouvementStockRepository mouvementStockRepository;
    private final ProduitRepository produitRepository;


    @Override
    public List<MouvementStockDTO> findAll() {
        return mouvementStockRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MouvementStockDTO> findByProduitId(Long produitId) {
        Produit existingProduit = produitRepository.findById(produitId)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("produit non trouve");
                });

        return mouvementStockRepository.findByProduitId(produitId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }




   public List<MouvementStockDTO> filterbycritiere(String critire , String value , String condition){
        List<MouvementStockDTO> list= findAll();
        List<MouvementStockDTO> listFilter=new ArrayList<>();

        Predicate<MouvementStockDTO> predicate = mouvementStockDTO -> mouvementStockDTO.getObservation().equals("--");
        switch (critire){
            case "type":
                predicate= mouvementStockDTO ->  mouvementStockDTO.getTypeMouvement().equals("ENTREE");
                break;

            case "referenceDocument":
                predicate= new Predicate<MouvementStockDTO>() {
                    @Override
                    public boolean test(MouvementStockDTO mouvementStockDTO) {
                        return mouvementStockDTO.getReferenceDocument().equals("referenceDocument");
                    }
                };
                break;

        }


         for (MouvementStockDTO mv : list){
             if (predicate.test(mv)){
                 listFilter.add(mv);
             }
         }

         return listFilter;
    }


    private MouvementStockDTO mapToDTO(MouvementStock mouvement) {
        return MouvementStockDTO.builder()
                .id(mouvement.getId())
                .produitId(mouvement.getProduit().getId())
                .produitReference(mouvement.getProduit().getReference())
                .produitNom(mouvement.getProduit().getNom())
                .typeMouvement(mouvement.getTypeMouvement().name())
                .quantite(mouvement.getQuantite())
                .dateMouvement(mouvement.getDateMouvement())
                .numeroLot(mouvement.getLot() != null ? mouvement.getLot().getNumeroLot() : null)
                .prixUnitaire(mouvement.getPrixUnitaire())
                .referenceDocument(mouvement.getReferenceDocument())
                .observation(mouvement.getObservation())
                .build();
    }
}

