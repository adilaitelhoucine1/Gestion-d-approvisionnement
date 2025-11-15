package com.tricol.gestionstock.service.impl;

import com.tricol.gestionstock.dto.stock.MouvementStockDTO;
import com.tricol.gestionstock.dto.stock.MouvementStockSearchCriteria;
import com.tricol.gestionstock.entity.MouvementStock;
import com.tricol.gestionstock.entity.Produit;
import com.tricol.gestionstock.repository.MouvementStockRepository;
import com.tricol.gestionstock.repository.ProduitRepository;
import com.tricol.gestionstock.service.MouvementService;
import com.tricol.gestionstock.specification.MouvementStockSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        // Verify product exists
        produitRepository.findById(produitId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé"));

        return mouvementStockRepository.findByProduitId(produitId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<MouvementStockDTO> searchMouvements(MouvementStockSearchCriteria criteria, Pageable pageable) {

        Page<MouvementStock> mouvementsPage = mouvementStockRepository.findAll(
                MouvementStockSpecification.withCriteria(criteria),
                pageable
        );


        return mouvementsPage.map(this::mapToDTO);
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

