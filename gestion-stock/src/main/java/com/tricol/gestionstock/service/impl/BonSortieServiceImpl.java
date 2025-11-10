package com.tricol.gestionstock.service.impl;

import com.tricol.gestionstock.dto.bonsortie.BonSortieRequestDTO;
import com.tricol.gestionstock.dto.bonsortie.BonSortieResponseDTO;
import com.tricol.gestionstock.dto.bonsortie.LigneBonSortieDTO;
import com.tricol.gestionstock.entity.*;
import com.tricol.gestionstock.entity.Enums.StatutBonSortie;
import com.tricol.gestionstock.entity.Enums.TypeMouvement;
import com.tricol.gestionstock.exception.DuplicateResourceException;
import com.tricol.gestionstock.exception.ResourceNotFoundException;
import com.tricol.gestionstock.mapper.BonSortieMapper;
import com.tricol.gestionstock.repository.*;
import com.tricol.gestionstock.service.BonSortieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BonSortieServiceImpl implements BonSortieService {

    private final BonSortieRepository bonSortieRepository;
    private final ProduitRepository produitRepository;
    private final LotStockRepository lotStockRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final BonSortieMapper mapper;

    @Override
    public BonSortieResponseDTO createBonSortie(BonSortieRequestDTO requestDTO) {
        if (bonSortieRepository.existsByNumeroBon(requestDTO.getNumeroBon())) {
            throw new DuplicateResourceException("numeroBon", requestDTO.getNumeroBon());
        }

        BonSortie bonSortie = BonSortie.builder()
                .numeroBon(requestDTO.getNumeroBon())
                .dateSortie(requestDTO.getDateSortie())
                .atelierDestinataire(requestDTO.getAtelierDestinataire())
                .motif(requestDTO.getMotif())
                .statut(StatutBonSortie.BROUILLON)
                .observations(requestDTO.getObservations())
                .lignes(new ArrayList<>())
                .build();

        for (LigneBonSortieDTO ligneDTO : requestDTO.getLignes()) {
            Produit produit = produitRepository.findById(ligneDTO.getProduitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", ligneDTO.getProduitId()));

            LigneBonSortie ligne = LigneBonSortie.builder()
                    .produit(produit)
                    .quantiteDemandee(ligneDTO.getQuantiteDemandee())
                    .build();

            bonSortie.ajouterLigne(ligne);
        }

        BonSortie savedBonSortie = bonSortieRepository.save(bonSortie);
        return mapper.toResponseDTO(savedBonSortie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BonSortieResponseDTO> getAllBonsSortie() {
        List<BonSortie> bonsSortie = bonSortieRepository.findAllWithLignes();
        return mapper.toResponseDTOList(bonsSortie);
    }

    @Override
    @Transactional(readOnly = true)
    public BonSortieResponseDTO getBonSortieById(Long id) {
        BonSortie bonSortie = bonSortieRepository.findByIdWithLignes(id)
                .orElseThrow(() -> new ResourceNotFoundException("BonSortie", "id", id));
        return mapper.toResponseDTO(bonSortie);
    }

    @Override
    public BonSortieResponseDTO updateBonSortie(Long id, BonSortieRequestDTO requestDTO) {
        BonSortie bonSortie = bonSortieRepository.findByIdWithLignes(id)
                .orElseThrow(() -> new ResourceNotFoundException("BonSortie", "id", id));

        if (!bonSortie.estModifiable()) {
            throw new IllegalStateException("Le bon de sortie ne peut pas être modifie (statut: " + bonSortie.getStatut() + ")");
        }

        if (!bonSortie.getNumeroBon().equals(requestDTO.getNumeroBon()) &&
                bonSortieRepository.existsByNumeroBon(requestDTO.getNumeroBon())) {
            throw new DuplicateResourceException("numeroBon", requestDTO.getNumeroBon());
        }

        bonSortie.setNumeroBon(requestDTO.getNumeroBon());
        bonSortie.setDateSortie(requestDTO.getDateSortie());
        bonSortie.setAtelierDestinataire(requestDTO.getAtelierDestinataire());
        bonSortie.setMotif(requestDTO.getMotif());
        bonSortie.setObservations(requestDTO.getObservations());

        bonSortie.getLignes().clear();

        for (LigneBonSortieDTO ligneDTO : requestDTO.getLignes()) {
            Produit produit = produitRepository.findById(ligneDTO.getProduitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", ligneDTO.getProduitId()));

            LigneBonSortie ligne = LigneBonSortie.builder()
                    .produit(produit)
                    .quantiteDemandee(ligneDTO.getQuantiteDemandee())
                    .build();

            bonSortie.ajouterLigne(ligne);
        }

        BonSortie updatedBonSortie = bonSortieRepository.save(bonSortie);
        return mapper.toResponseDTO(updatedBonSortie);
    }

    @Override
    public BonSortieResponseDTO validerBonSortie(Long id) {
        BonSortie bonSortie = bonSortieRepository.findByIdWithLignes(id)
                .orElseThrow(() -> new ResourceNotFoundException("BonSortie", "id", id));

        if (!bonSortie.peutEtreValide()) {
            throw new IllegalStateException("Le bon de sortie ne peut pas être valide");
        }

        for (LigneBonSortie ligne : bonSortie.getLignes()) {
            traiterSortieFIFO(bonSortie, ligne);
        }

        bonSortie.setStatut(StatutBonSortie.VALIDE);
        bonSortie.setDateValidation(LocalDateTime.now());

        BonSortie validatedBonSortie = bonSortieRepository.save(bonSortie);
        return mapper.toResponseDTO(validatedBonSortie);
    }

    @Override
    public BonSortieResponseDTO annulerBonSortie(Long id) {
        BonSortie bonSortie = bonSortieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BonSortie", "id", id));

        if (!bonSortie.estModifiable()) {
            throw new IllegalStateException("Seuls les bons de sortie brouillon peuvent être annules");
        }

        bonSortie.setStatut(StatutBonSortie.ANNULE);
        BonSortie annuledBonSortie = bonSortieRepository.save(bonSortie);
        return mapper.toResponseDTO(annuledBonSortie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BonSortieResponseDTO> getBonsSortieByAtelier(String atelier) {
        List<BonSortie> bonsSortie = bonSortieRepository.findByAtelierDestinataireOrderByDateSortieDesc(atelier);
        return mapper.toResponseDTOList(bonsSortie);
    }

    private void traiterSortieFIFO(BonSortie bonSortie, LigneBonSortie ligne) {
        Produit produit = ligne.getProduit();
        int quantiteAPrelever = ligne.getQuantiteDemandee();

        if (produit.getStockActuel() < quantiteAPrelever) {
            throw new IllegalStateException(
                    String.format("Stock insuffisant pour le produit %s. Disponible: %d, Demande: %d",
                            produit.getNom(), produit.getStockActuel(), quantiteAPrelever)
            );
        }

        List<LotStock> lotsDisponibles = lotStockRepository.findLotsDisponiblesByProduitFIFO(produit.getId());

        if (lotsDisponibles.isEmpty()) {
            throw new IllegalStateException("Aucun lot disponible pour le produit: " + produit.getNom());
        }

        for (LotStock lot : lotsDisponibles) {
            if (quantiteAPrelever <= 0) {
                break;
            }

            int quantiteAPreleverDuLot = Math.min(quantiteAPrelever, lot.getQuantiteRestante());

            lot.consommer(quantiteAPreleverDuLot);
            lotStockRepository.save(lot);

            MouvementStock mouvement = MouvementStock.builder()
                    .produit(produit)
                    .typeMouvement(TypeMouvement.SORTIE)
                    .quantite(quantiteAPreleverDuLot)
                    .dateMouvement(LocalDateTime.now())
                    .lot(lot)
                    .bonSortie(bonSortie)
                    .prixUnitaire(lot.getPrixAchatUnitaire())
                    .referenceDocument(bonSortie.getNumeroBon())
                    .observation(String.format("Sortie vers atelier %s - Lot %s",
                            bonSortie.getAtelierDestinataire(), lot.getNumeroLot()))
                    .build();

            mouvementStockRepository.save(mouvement);

            quantiteAPrelever -= quantiteAPreleverDuLot;
        }

        produit.decrementerStock(ligne.getQuantiteDemandee());
        produitRepository.save(produit);
    }
}

