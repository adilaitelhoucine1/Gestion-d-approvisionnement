package com.tricol.gestionstock.mapper;

import com.tricol.gestionstock.dto.stock.AlerteStockDTO;
import com.tricol.gestionstock.dto.stock.EtatStockDTO;
import com.tricol.gestionstock.dto.stock.LotStockDTO;
import com.tricol.gestionstock.entity.LotStock;
import com.tricol.gestionstock.entity.Produit;
import com.tricol.gestionstock.repository.LotStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class StockMapper {

    @Autowired
    private LotStockRepository lotStockRepository;

    public EtatStockDTO toEtatStockDTO(Produit produit) {
        List<LotStock> lots = lotStockRepository.findLotsDisponiblesByProduitFIFO(produit.getId());

        return EtatStockDTO.builder()
                .produitId(produit.getId())
                .reference(produit.getReference())
                .nom(produit.getNom())
                .categorie(produit.getCategorie())
                .quantiteDisponible(produit.getStockActuel())
                .valorisation(sumValorisations(lots))
                .pointDeCommande(produit.getPointDeCommande())
                .enAlerte(produit.isEnAlerte())
                .build();
    }

    public LotStockDTO toLotStockDTO(LotStock lot) {
        return LotStockDTO.builder()
                .id(lot.getId())
                .numeroLot(lot.getNumeroLot())
                .dateEntree(lot.getDateEntree())
                .quantiteInitiale(lot.getQuantiteInitiale())
                .quantiteRestante(lot.getQuantiteRestante())
                .prixAchatUnitaire(lot.getPrixAchatUnitaire())
                .valorisation(lot.getValorisation())
                .build();
    }

    public AlerteStockDTO toAlerteStockDTO(Produit produit) {
        return AlerteStockDTO.builder()
                .produitId(produit.getId())
                .reference(produit.getReference())
                .nom(produit.getNom())
                .categorie(produit.getCategorie())
                .stockActuel(produit.getStockActuel())
                .pointDeCommande(produit.getPointDeCommande())
                .manquant(Math.max(produit.getPointDeCommande() - produit.getStockActuel(), 0))
                .build();
    }

    private BigDecimal sumValorisations(List<LotStock> lots) {
        return lots.stream()
                .map(LotStock::getValorisation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

