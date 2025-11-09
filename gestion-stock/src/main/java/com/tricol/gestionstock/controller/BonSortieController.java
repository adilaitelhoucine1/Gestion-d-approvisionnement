package com.tricol.gestionstock.controller;

import com.tricol.gestionstock.dto.bonsortie.BonSortieRequestDTO;
import com.tricol.gestionstock.dto.bonsortie.BonSortieResponseDTO;
import com.tricol.gestionstock.service.BonSortieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bons-sortie")
@RequiredArgsConstructor
public class BonSortieController {

    private final BonSortieService bonSortieService;

    @PostMapping
    public ResponseEntity<BonSortieResponseDTO> createBonSortie(@Valid @RequestBody BonSortieRequestDTO requestDTO) {
        BonSortieResponseDTO response = bonSortieService.createBonSortie(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BonSortieResponseDTO>> getAllBonsSortie() {
        List<BonSortieResponseDTO> bonsSortie = bonSortieService.getAllBonsSortie();
        return ResponseEntity.ok(bonsSortie);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BonSortieResponseDTO> getBonSortieById(@PathVariable Long id) {
        BonSortieResponseDTO bonSortie = bonSortieService.getBonSortieById(id);
        return ResponseEntity.ok(bonSortie);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BonSortieResponseDTO> updateBonSortie(@PathVariable Long id, @Valid @RequestBody BonSortieRequestDTO requestDTO) {
        BonSortieResponseDTO response = bonSortieService.updateBonSortie(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<BonSortieResponseDTO> validerBonSortie(@PathVariable Long id) {
        BonSortieResponseDTO response = bonSortieService.validerBonSortie(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<BonSortieResponseDTO> annulerBonSortie(@PathVariable Long id) {
        BonSortieResponseDTO response = bonSortieService.annulerBonSortie(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/atelier/{atelier}")
    public ResponseEntity<List<BonSortieResponseDTO>> getBonsSortieByAtelier(@PathVariable String atelier) {
        List<BonSortieResponseDTO> bonsSortie = bonSortieService.getBonsSortieByAtelier(atelier);
        return ResponseEntity.ok(bonsSortie);
    }
}

