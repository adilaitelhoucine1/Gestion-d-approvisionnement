package com.tricol.gestionstock.service.impl;

import com.tricol.gestionstock.dto.commande.CommandeFournisseurRequestDTO;
import com.tricol.gestionstock.dto.commande.CommandeFournisseurResponseDTO;
import com.tricol.gestionstock.dto.commande.LigneCommandeDTO;
import com.tricol.gestionstock.dto.commande.ReceptionCommandeDTO;
import com.tricol.gestionstock.entity.*;
import com.tricol.gestionstock.entity.Enums.StatutCommande;
import com.tricol.gestionstock.entity.Enums.TypeMouvement;
import com.tricol.gestionstock.exception.DuplicateResourceException;
import com.tricol.gestionstock.exception.ResourceNotFoundException;
import com.tricol.gestionstock.mapper.CommandeFournisseurMapper;
import com.tricol.gestionstock.repository.*;
import com.tricol.gestionstock.service.CommandeFournisseurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommandeFournisseurServiceImpl implements CommandeFournisseurService {

    private final CommandeFournisseurRepository commandeRepository;
    private final FournisseurRepository fournisseurRepository;
    private final ProduitRepository produitRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final LotStockRepository lotStockRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final CommandeFournisseurMapper mapper;

    @Override
    public CommandeFournisseurResponseDTO createCommande(CommandeFournisseurRequestDTO requestDTO) {
        log.info("Creation d'une nouvelle commande: {}", requestDTO.getNumeroCommande());

        if (commandeRepository.existsByNumeroCommande(requestDTO.getNumeroCommande())) {
            throw new DuplicateResourceException("numeroCommande", requestDTO.getNumeroCommande());
        }

        Fournisseur fournisseur = fournisseurRepository.findById(requestDTO.getFournisseurId())
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", "id", requestDTO.getFournisseurId()));

        CommandeFournisseur commande = CommandeFournisseur.builder() 
                .numeroCommande(requestDTO.getNumeroCommande())
                .dateCommande(requestDTO.getDateCommande())
                .fournisseur(fournisseur)
                .statut(requestDTO.getStatut() != null ? requestDTO.getStatut() : StatutCommande.EN_ATTENTE)
                .observations(requestDTO.getObservations())
                .montantTotal(BigDecimal.ZERO)
                .lignesCommande(new ArrayList<>())
                .build();

        for (LigneCommandeDTO ligneDTO : requestDTO.getLignesCommande()) {
            Produit produit = produitRepository.findById(ligneDTO.getProduitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", ligneDTO.getProduitId()));

            LigneCommande ligne = LigneCommande.builder()
                    .produit(produit)
                    .quantite(ligneDTO.getQuantite())
                    .prixUnitaire(ligneDTO.getPrixUnitaire())
                    .build();

            commande.ajouterLigneCommande(ligne);
        }

        commande.calculerMontantTotal();

        CommandeFournisseur savedCommande = commandeRepository.save(commande);
        log.info("Commande creee avec succes: ID={}", savedCommande.getId());

        return mapper.toResponseDTO(savedCommande);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandeFournisseurResponseDTO> getAllCommandes() {
        log.info("Recuperation de toutes les commandes");
        List<CommandeFournisseur> commandes = commandeRepository.findAllByOrderByDateCommandeDesc();
        return mapper.toResponseDTOList(commandes);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandeFournisseurResponseDTO getCommandeById(Long id) {
        log.info("Recuperation de la commande avec ID: {}", id);
        CommandeFournisseur commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommandeFournisseur", "id", id));
        return mapper.toResponseDTO(commande);
    }

    @Override
    public CommandeFournisseurResponseDTO updateCommande(Long id, CommandeFournisseurRequestDTO requestDTO) {
        log.info("Mise a jour de la commande avec ID: {}", id);

        CommandeFournisseur commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommandeFournisseur", "id", id));

        if (!commande.estModifiable()) {
            throw new IllegalStateException("La commande ne peut pas être modifiee (statut: " + commande.getStatut() + ")");
        }

        if (!commande.getNumeroCommande().equals(requestDTO.getNumeroCommande()) &&
                commandeRepository.existsByNumeroCommande(requestDTO.getNumeroCommande())) {
            throw new DuplicateResourceException("numeroCommande", requestDTO.getNumeroCommande());
        }

        Fournisseur fournisseur = fournisseurRepository.findById(requestDTO.getFournisseurId())
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", "id", requestDTO.getFournisseurId()));

        commande.setNumeroCommande(requestDTO.getNumeroCommande());
        commande.setDateCommande(requestDTO.getDateCommande());
        commande.setFournisseur(fournisseur);
        commande.setObservations(requestDTO.getObservations());

        commande.getLignesCommande().clear();

        for (LigneCommandeDTO ligneDTO : requestDTO.getLignesCommande()) {
            Produit produit = produitRepository.findById(ligneDTO.getProduitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit", "id", ligneDTO.getProduitId()));

            LigneCommande ligne = LigneCommande.builder()
                    .produit(produit)
                    .quantite(ligneDTO.getQuantite())
                    .prixUnitaire(ligneDTO.getPrixUnitaire())
                    .build();

            commande.ajouterLigneCommande(ligne);
        }

        commande.calculerMontantTotal();

        CommandeFournisseur updatedCommande = commandeRepository.save(commande);
        log.info("Commande mise a jour avec succes: ID={}", updatedCommande.getId());

        return mapper.toResponseDTO(updatedCommande);
    }

    @Override
    public void deleteCommande(Long id) {
        log.info("Suppression de la commande avec ID: {}", id);

        CommandeFournisseur commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommandeFournisseur", "id", id));

        if (commande.getStatut() == StatutCommande.LIVREE) {
            throw new IllegalStateException("Impossible de supprimer une commande deja livree");
        }

        commandeRepository.delete(commande);
        log.info("Commande supprimee avec succes: ID={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandeFournisseurResponseDTO> getCommandesByFournisseur(Long fournisseurId) {
        log.info("Recuperation des commandes du fournisseur: {}", fournisseurId);

        if (!fournisseurRepository.existsById(fournisseurId)) {
            throw new ResourceNotFoundException("Fournisseur", "id", fournisseurId);
        }

        List<CommandeFournisseur> commandes = commandeRepository.findByFournisseurId(fournisseurId);
        return mapper.toResponseDTOList(commandes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandeFournisseurResponseDTO> getCommandesByStatut(StatutCommande statut) {
        log.info("Recuperation des commandes avec statut: {}", statut);
        List<CommandeFournisseur> commandes = commandeRepository.findByStatutOrderByDateCommandeDesc(statut);
        return mapper.toResponseDTOList(commandes);
    }

    @Override
    public CommandeFournisseurResponseDTO receptionnerCommande(Long id, ReceptionCommandeDTO receptionDTO) {
        log.info("Reception de la commande avec ID: {}", id);

        CommandeFournisseur commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommandeFournisseur", "id", id));

        if (!commande.peutEtreReceptionnee()) {
            throw new IllegalStateException("La commande ne peut pas être receptionnee (statut: " + commande.getStatut() + ")");
        }

        commande.setStatut(StatutCommande.LIVREE);
        commande.setDateReception(receptionDTO.getDateReception());
        if (receptionDTO.getObservations() != null) {
            commande.setObservations(
                    (commande.getObservations() != null ? commande.getObservations() + "\n" : "") +
                            "Reception: " + receptionDTO.getObservations()
            );
        }

        for (LigneCommande ligne : commande.getLignesCommande()) {
            String numeroLot = genererNumeroLot(ligne.getProduit(), commande);
            LotStock lot = LotStock.builder()
                    .numeroLot(numeroLot)
                    .produit(ligne.getProduit())
                    .dateEntree(receptionDTO.getDateReception())
                    .quantiteInitiale(ligne.getQuantite())
                    .quantiteRestante(ligne.getQuantite())
                    .prixAchatUnitaire(ligne.getPrixUnitaire())
                    .commande(commande)
                    .build();

            lotStockRepository.save(lot);

            MouvementStock mouvement = MouvementStock.builder()
                    .produit(ligne.getProduit())
                    .typeMouvement(TypeMouvement.ENTREE)
                    .quantite(ligne.getQuantite())
                    .dateMouvement(LocalDateTime.now())
                    .lot(lot)
                    .commande(commande)
                    .prixUnitaire(ligne.getPrixUnitaire())
                    .referenceDocument(commande.getNumeroCommande())
                    .observation("Reception commande " + commande.getNumeroCommande())
                    .build();

            mouvementStockRepository.save(mouvement);

            ligne.getProduit().incrementerStock(ligne.getQuantite());
            produitRepository.save(ligne.getProduit());

            log.info("Lot cree: {} - Quantite: {} - Produit: {}",
                    numeroLot, ligne.getQuantite(), ligne.getProduit().getNom());
        }

        CommandeFournisseur updatedCommande = commandeRepository.save(commande);
        log.info("Commande receptionnee avec succes: ID={}", updatedCommande.getId());

        return mapper.toResponseDTO(updatedCommande);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandeFournisseurResponseDTO> searchCommandes(
            Long fournisseurId,
            StatutCommande statut,
            LocalDate dateDebut,
            LocalDate dateFin) {
        log.info("Recherche de commandes avec filtres: fournisseur={}, statut={}, periode={} a {}",
                fournisseurId, statut, dateDebut, dateFin);

        List<CommandeFournisseur> commandes = commandeRepository.findByFilters(
                fournisseurId, statut, dateDebut, dateFin
        );

        return mapper.toResponseDTOList(commandes);
    }

    @Override
    public CommandeFournisseurResponseDTO annulerCommande(Long id) {
        log.info("Annulation de la commande avec ID: {}", id);

        CommandeFournisseur commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommandeFournisseur", "id", id));

        if (commande.getStatut() == StatutCommande.LIVREE) {
            throw new IllegalStateException("Impossible d'annuler une commande deja livree");
        }

        if (commande.getStatut() == StatutCommande.ANNULEE) {
            throw new IllegalStateException("La commande est deja annulee");
        }

        commande.setStatut(StatutCommande.ANNULEE);
        CommandeFournisseur updatedCommande = commandeRepository.save(commande);
        log.info("Commande annulee avec succes: ID={}", updatedCommande.getId());

        return mapper.toResponseDTO(updatedCommande);
    }

    @Override
    public CommandeFournisseurResponseDTO validerCommande(Long id) {
        log.info("Validation de la commande avec ID: {}", id);

        CommandeFournisseur commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommandeFournisseur", "id", id));

        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new IllegalStateException("Seules les commandes en attente peuvent être validees");
        }

        commande.setStatut(StatutCommande.VALIDEE);
        CommandeFournisseur updatedCommande = commandeRepository.save(commande);
        log.info("Commande validee avec succes: ID={}", updatedCommande.getId());

        return mapper.toResponseDTO(updatedCommande);
    }

   
    private String genererNumeroLot(Produit produit, CommandeFournisseur commande) {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = date.format(formatter);
        String produitRef = produit.getReference().substring(0, Math.min(4, produit.getReference().length())).toUpperCase();
        String commandeNum = commande.getNumeroCommande().substring(0, Math.min(4, commande.getNumeroCommande().length())).toUpperCase();

        return String.format("LOT-%s-%s-%s-%d",
                dateStr,
                produitRef,
                commandeNum,
                System.currentTimeMillis() % 10000);
    }
}
