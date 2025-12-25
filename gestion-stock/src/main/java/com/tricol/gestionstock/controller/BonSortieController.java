package com.tricol.gestionstock.controller;

import com.tricol.gestionstock.dto.bonsortie.BonSortieRequestDTO;
import com.tricol.gestionstock.dto.bonsortie.BonSortieResponseDTO;
import com.tricol.gestionstock.service.BonSortieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bons-sortie")
@RequiredArgsConstructor
@Tag(name = "Bons de Sortie", description = "Gestion des bons de sortie")
public class BonSortieController {

    private final BonSortieService bonSortieService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREER_BON_SORTIE')")
    @Operation(summary = "Créer un bon de sortie (brouillon)", description = "Requiert la permission CREER_BON_SORTIE")
    public ResponseEntity<BonSortieResponseDTO> createBonSortie(@Valid @RequestBody BonSortieRequestDTO requestDTO) {
        BonSortieResponseDTO response = bonSortieService.createBonSortie(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONSULTER_BON_SORTIE')")
    @Operation(summary = "Lister tous les bons de sortie", description = "Requiert la permission CONSULTER_BON_SORTIE")
    public ResponseEntity<List<BonSortieResponseDTO>> getAllBonsSortie() {
        List<BonSortieResponseDTO> bonsSortie = bonSortieService.getAllBonsSortie();
        return ResponseEntity.ok(bonsSortie);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONSULTER_BON_SORTIE')")
    @Operation(summary = "Obtenir un bon de sortie par ID", description = "Requiert la permission CONSULTER_BON_SORTIE")
    public ResponseEntity<BonSortieResponseDTO> getBonSortieById(@PathVariable Long id) {
        BonSortieResponseDTO bonSortie = bonSortieService.getBonSortieById(id);
        return ResponseEntity.ok(bonSortie);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CREER_BON_SORTIE')")
    @Operation(summary = "Modifier un bon de sortie", description = "Requiert la permission CREER_BON_SORTIE")
    public ResponseEntity<BonSortieResponseDTO> updateBonSortie(@PathVariable Long id, @Valid @RequestBody BonSortieRequestDTO requestDTO) {
        BonSortieResponseDTO response = bonSortieService.updateBonSortie(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/valider")
    @PreAuthorize("hasAuthority('VALIDER_BON_SORTIE')")
    @Operation(summary = "Valider un bon de sortie", description = "Requiert la permission VALIDER_BON_SORTIE")
    public ResponseEntity<BonSortieResponseDTO> validerBonSortie(@PathVariable Long id) {
        BonSortieResponseDTO response = bonSortieService.validerBonSortie(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasAuthority('ANNULER_BON_SORTIE')")
    @Operation(summary = "Annuler un bon de sortie", description = "Requiert la permission ANNULER_BON_SORTIE")
    public ResponseEntity<BonSortieResponseDTO> annulerBonSortie(@PathVariable Long id) {
        BonSortieResponseDTO response = bonSortieService.annulerBonSortie(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/atelier/{atelier}")
    @PreAuthorize("hasAuthority('CONSULTER_BON_SORTIE')")
    @Operation(summary = "Obtenir les bons de sortie par atelier", description = "Requiert la permission CONSULTER_BON_SORTIE")
    public ResponseEntity<List<BonSortieResponseDTO>> getBonsSortieByAtelier(@PathVariable String atelier) {
        List<BonSortieResponseDTO> bonsSortie = bonSortieService.getBonsSortieByAtelier(atelier);
        return ResponseEntity.ok(bonsSortie);
    }
}

