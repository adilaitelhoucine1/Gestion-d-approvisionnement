package com.tricol.gestionstock.controller;

import com.tricol.gestionstock.dto.stock.AlerteStockDTO;
import com.tricol.gestionstock.dto.stock.EtatStockDTO;
import com.tricol.gestionstock.dto.stock.MouvementStockDTO;
import com.tricol.gestionstock.dto.stock.MouvementStockSearchCriteria;
import com.tricol.gestionstock.dto.stock.StockProduitDTO;
import com.tricol.gestionstock.entity.Enums.TypeMouvement;
import com.tricol.gestionstock.service.MouvementService;
import com.tricol.gestionstock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "Stock & Lots", description = "Gestion du stock et des lots")
public class StockController {

    private final StockService stockService;
    private final MouvementService mouvementService;

    @GetMapping
    @PreAuthorize("hasAuthority('CONSULTER_STOCK')")
    @Operation(summary = "Obtenir l'état global du stock", description = "Requiert la permission CONSULTER_STOCK")
    public ResponseEntity<List<EtatStockDTO>> getEtatGlobal() {
        List<EtatStockDTO> etatGlobal = stockService.getEtatGlobal();
        return ResponseEntity.ok(etatGlobal);
    }

    @GetMapping("/produit/{id}")
    @PreAuthorize("hasAuthority('CONSULTER_STOCK')")
    @Operation(summary = "Obtenir le stock d'un produit (FIFO)", description = "Requiert la permission CONSULTER_STOCK")
    public ResponseEntity<StockProduitDTO> getStockByProduit(@PathVariable Long id) {
        StockProduitDTO stockProduit = stockService.getLotsDisponiblesFifo(id);
        return ResponseEntity.ok(stockProduit);
    }


    @GetMapping("/mouvements")
    @PreAuthorize("hasAuthority('CONSULTER_MOUVEMENTS')")
    @Operation(summary = "Rechercher les mouvements de stock", description = "Requiert la permission CONSULTER_MOUVEMENTS")
    public ResponseEntity<Page<MouvementStockDTO>> searchMouvements(
            @RequestParam(required = false) Long produitId,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) TypeMouvement type,
            @RequestParam(required = false) String numeroLot,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {


        MouvementStockSearchCriteria criteria = MouvementStockSearchCriteria.builder()
                .produitId(produitId)
                .reference(reference)
                .type(type)
                .numeroLot(numeroLot)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .build();


        Pageable pageable = PageRequest.of(page, size);


        Page<MouvementStockDTO> mouvements = mouvementService.searchMouvements(criteria, pageable);

        return ResponseEntity.ok(mouvements);
    }

    @GetMapping("/mouvements/produit/{id}")
    @PreAuthorize("hasAuthority('CONSULTER_MOUVEMENTS')")
    @Operation(summary = "Obtenir les mouvements d'un produit", description = "Requiert la permission CONSULTER_MOUVEMENTS")
    public ResponseEntity<List<MouvementStockDTO>> getMouvementsByProduit(@PathVariable Long id) {
        List<MouvementStockDTO> mouvements = mouvementService.findByProduitId(id);
        return ResponseEntity.ok(mouvements);
    }

    @GetMapping("/alertes")
    @PreAuthorize("hasAuthority('CONSULTER_STOCK')")
    @Operation(summary = "Obtenir les alertes de stock", description = "Requiert la permission CONSULTER_STOCK")
    public ResponseEntity<List<AlerteStockDTO>> getAlertes() {
        List<AlerteStockDTO> alertes = stockService.getAlertes();
        return ResponseEntity.ok(alertes);
    }

    @GetMapping("/valorisation")
    @PreAuthorize("hasAuthority('VOIR_VALORISATION')")
    @Operation(summary = "Obtenir la valorisation FIFO du stock", description = "Requiert la permission VOIR_VALORISATION")
    public ResponseEntity<String> getValorisation() {
        BigDecimal valorisation = stockService.getValorisationFifo();
        return ResponseEntity.ok().body("Valo de stock restant est " + valorisation);
    }
}
