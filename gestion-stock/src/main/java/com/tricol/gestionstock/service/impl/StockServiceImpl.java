package com.tricol.gestionstock.service.impl;

import com.tricol.gestionstock.dto.stock.AlerteStockDTO;
import com.tricol.gestionstock.dto.stock.EtatStockDTO;
import com.tricol.gestionstock.dto.stock.LotStockDTO;
import com.tricol.gestionstock.dto.stock.StockProduitDTO;
import com.tricol.gestionstock.entity.LotStock;
import com.tricol.gestionstock.entity.Produit;
import com.tricol.gestionstock.mapper.StockMapper;
import com.tricol.gestionstock.repository.LotStockRepository;
import com.tricol.gestionstock.repository.ProduitRepository;
import com.tricol.gestionstock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockServiceImpl implements StockService {

    private final ProduitRepository produitRepository;
    private final LotStockRepository lotStockRepository;
    private final StockMapper stockMapper;

    @Override
    public List<EtatStockDTO> getEtatGlobal() {
        return produitRepository.findAll()
                .stream()
                .map(stockMapper::toEtatStockDTO)
                .toList();
    }

    @Override
    public StockProduitDTO getLotsDisponiblesFifo(Long produitId) {
        Produit produit = findProduitById(produitId);
        List<LotStock> lots = lotStockRepository.findLotsDisponiblesByProduitFIFO(produitId);

        return StockProduitDTO.builder()
                .produitId(produit.getId())
                .reference(produit.getReference())
                .nom(produit.getNom())
                .quantiteTotale(sumQuantites(lots))
                .valorisationTotale(sumValorisations(lots))
                .lots(lots.stream().map(stockMapper::toLotStockDTO).toList())
                .build();
    }

    @Override
    public List<AlerteStockDTO> getAlertes() {
        return produitRepository.findAll()
                .stream()
                .filter(Produit::isEnAlerte)
                .map(stockMapper::toAlerteStockDTO)
                .toList();
    }

    @Override
    public BigDecimal getValorisationFifo() {
        return lotStockRepository.findAll()
                .stream()
                .filter(lot -> lot.getQuantiteRestante() > 0)
                .map(LotStock::getValorisation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private Produit findProduitById(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + id));
    }

    private Integer sumQuantites(List<LotStock> lots) {
        return lots.stream().mapToInt(LotStock::getQuantiteRestante).sum();
    }

    private BigDecimal sumValorisations(List<LotStock> lots) {
        return lots.stream()
                .map(LotStock::getValorisation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }




}

