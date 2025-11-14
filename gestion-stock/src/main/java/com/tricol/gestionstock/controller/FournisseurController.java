package com.tricol.gestionstock.controller;

import com.tricol.gestionstock.dto.fournisseur.FournisseurRequestDTO;
import com.tricol.gestionstock.dto.fournisseur.FournisseurResponseDTO;
import com.tricol.gestionstock.service.FournisseurService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fournisseurs")
@RequiredArgsConstructor
@Slf4j
public class FournisseurController {

    private final FournisseurService fournisseurService;

    @PostMapping
    public ResponseEntity<FournisseurResponseDTO> createFournisseur(@Valid @RequestBody FournisseurRequestDTO requestDTO) {

        FournisseurResponseDTO response = fournisseurService.createFournisseur(requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping

    public ResponseEntity<List<FournisseurResponseDTO>> getAllFournisseurs() {

        List<FournisseurResponseDTO> fournisseurs = fournisseurService.getAllFournisseurs();
        return ResponseEntity.ok(fournisseurs);
    }

    @GetMapping("/{id}")

    public ResponseEntity<FournisseurResponseDTO> getFournisseurById(@PathVariable Long id) {

        FournisseurResponseDTO fournisseur = fournisseurService.getFournisseurById(id);
        return ResponseEntity.ok(fournisseur);
    }

    @PutMapping("/{id}")

    public ResponseEntity<FournisseurResponseDTO> updateFournisseur(@PathVariable Long id, @Valid @RequestBody FournisseurRequestDTO requestDTO) {

        FournisseurResponseDTO response = fournisseurService.updateFournisseur(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFournisseur(@PathVariable Long id) {

        fournisseurService.deleteFournisseur(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")

    public ResponseEntity<List<FournisseurResponseDTO>> searchFournisseurs(@RequestParam(required = false) String ville, @RequestParam(required = false) String raisonSociale) {

        List<FournisseurResponseDTO> results;

        if (ville != null && !ville.trim().isEmpty()) {
            results = fournisseurService.searchByVille(ville);
        } else if (raisonSociale != null && !raisonSociale.trim().isEmpty()) {
            results = fournisseurService.searchByRaisonSociale(raisonSociale);
        } else {
            results = fournisseurService.getAllFournisseurs();
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<FournisseurResponseDTO> getFournisseurByEmail(@PathVariable String email) {
        FournisseurResponseDTO fournisseur = fournisseurService.getFournisseurByEmail(email);
        return ResponseEntity.ok(fournisseur);
    }

    @GetMapping("/ice/{ice}")
    public ResponseEntity<FournisseurResponseDTO> getFournisseurByIce(@PathVariable String ice) {
        FournisseurResponseDTO fournisseur = fournisseurService.getFournisseurByIce(ice);
        return ResponseEntity.ok(fournisseur);
    }

    @GetMapping("/test")
    public ResponseEntity<List<FournisseurResponseDTO> >test(){
        return ResponseEntity.ok(fournisseurService.filtered());
    }
}
