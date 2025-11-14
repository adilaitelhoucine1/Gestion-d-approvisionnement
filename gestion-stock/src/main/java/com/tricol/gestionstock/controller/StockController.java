package com.tricol.gestionstock.controller;

import com.tricol.gestionstock.dto.stock.AlerteStockDTO;
import com.tricol.gestionstock.dto.stock.EtatStockDTO;
import com.tricol.gestionstock.dto.stock.MouvementStockDTO;
import com.tricol.gestionstock.dto.stock.StockProduitDTO;
import com.tricol.gestionstock.service.MouvementService;
import com.tricol.gestionstock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final MouvementService mouvementService;

    @GetMapping
    public ResponseEntity<List<EtatStockDTO>> getEtatGlobal() {
        List<EtatStockDTO> etatGlobal = stockService.getEtatGlobal();
        return ResponseEntity.ok(etatGlobal);
    }

    @GetMapping("/produit/{id}")
    public ResponseEntity<StockProduitDTO> getStockByProduit(@PathVariable Long id) {
        StockProduitDTO stockProduit = stockService.getLotsDisponiblesFifo(id);
        return ResponseEntity.ok(stockProduit);
    }

    @GetMapping("/mouvements")
    public ResponseEntity<List<MouvementStockDTO>> getMouvements() {
        List<MouvementStockDTO> mouvements = mouvementService.findAll();
        return ResponseEntity.ok(mouvements);
    }

    @GetMapping("/mouvements/produit/{id}")
    public ResponseEntity<List<MouvementStockDTO>> getMouvementsByProduit(@PathVariable Long id) {

        List<MouvementStockDTO> mouvements = mouvementService.findByProduitId(id);

        return ResponseEntity.ok(mouvements);
    }

    @GetMapping("/alertes")
    public ResponseEntity<List<AlerteStockDTO>> getAlertes() {
        List<AlerteStockDTO> alertes = stockService.getAlertes();
        return ResponseEntity.ok(alertes);
    }

    @GetMapping("/valorisation")
    public ResponseEntity<String> getValorisation() {
        BigDecimal valorisation = stockService.getValorisationFifo();
        return ResponseEntity.ok().body("Valo de stock restant est "+valorisation);
    }

    @GetMapping("/search")

    public List<MouvementStockDTO> search( @RequestParam("critere") String critire,
                            @RequestParam("value") String value ,
    @RequestParam(value = "condition",required = false)  String condition  ){
        return mouvementService.filterbycritiere(critire ,value,condition);
    }
}
