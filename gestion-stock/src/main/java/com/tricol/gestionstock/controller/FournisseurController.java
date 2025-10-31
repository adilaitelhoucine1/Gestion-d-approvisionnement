package com.tricol.gestionstock.controller;

import com.tricol.gestionstock.dto.fournisseur.FournisseurRequestDTO;
import com.tricol.gestionstock.dto.fournisseur.FournisseurResponseDTO;
import com.tricol.gestionstock.service.FournisseurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Fournisseurs", description = "API de gestion des fournisseurs")
public class FournisseurController {

    private final FournisseurService fournisseurService;

    @PostMapping
    @Operation(summary = "Creer un nouveau fournisseur", 
               description = "Cree un nouveau fournisseur avec les informations fournies")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Fournisseur cree avec succes",
                     content = @Content(schema = @Schema(implementation = FournisseurResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Donnees invalides"),
        @ApiResponse(responseCode = "409", description = "Email ou ICE deja existant")
    })
    public ResponseEntity<FournisseurResponseDTO> createFournisseur(
            @Valid @RequestBody FournisseurRequestDTO requestDTO) {
        log.info("POST /api/v1/fournisseurs - creation nev fournisseur");
        FournisseurResponseDTO response = fournisseurService.createFournisseur(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Recuper tous les fournisseurs",
               description = "Retourne la liste complete de tous les fournisseurs")
    @ApiResponse(responseCode = "200", description = "Liste des fournisseurs récupérée avec succès")
    public ResponseEntity<List<FournisseurResponseDTO>> getAllFournisseurs() {
        log.info("GET /api/v1/fournisseurs - Récupération de tous les fournisseurs");
        List<FournisseurResponseDTO> fournisseurs = fournisseurService.getAllFournisseurs();
        return ResponseEntity.ok(fournisseurs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer un fournisseur par ID",
               description = "Retourne les détails d'un fournisseur spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fournisseur trouvé",
                     content = @Content(schema = @Schema(implementation = FournisseurResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Fournisseur non trouvé")
    })
    public ResponseEntity<FournisseurResponseDTO> getFournisseurById(
            @Parameter(description = "ID du fournisseur") @PathVariable Long id) {
        log.info("GET /api/v1/fournisseurs/{} - Récupération du fournisseur", id);
        FournisseurResponseDTO fournisseur = fournisseurService.getFournisseurById(id);
        return ResponseEntity.ok(fournisseur);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un fournisseur",
               description = "Met à jour les informations d'un fournisseur existant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fournisseur mis à jour avec succès",
                     content = @Content(schema = @Schema(implementation = FournisseurResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "404", description = "Fournisseur non trouvé"),
        @ApiResponse(responseCode = "409", description = "Email ou ICE déjà existant")
    })
    public ResponseEntity<FournisseurResponseDTO> updateFournisseur(
            @Parameter(description = "ID du fournisseur") @PathVariable Long id,
            @Valid @RequestBody FournisseurRequestDTO requestDTO) {
        log.info("PUT /api/v1/fournisseurs/{} - Mise à jour du fournisseur", id);
        FournisseurResponseDTO response = fournisseurService.updateFournisseur(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un fournisseur",
               description = "Supprime un fournisseur de la base de données")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Fournisseur supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Fournisseur non trouvé")
    })
    public ResponseEntity<Void> deleteFournisseur(
            @Parameter(description = "ID du fournisseur") @PathVariable Long id) {
        log.info("DELETE /api/v1/fournisseurs/{} - Suppression du fournisseur", id);
        fournisseurService.deleteFournisseur(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des fournisseurs",
               description = "Recherche des fournisseurs par mot-clé (raison sociale, ville, email, ICE)")
    @ApiResponse(responseCode = "200", description = "Résultats de recherche récupérés avec succès")
    public ResponseEntity<List<FournisseurResponseDTO>> searchFournisseurs(
            @Parameter(description = "Mot-clé de recherche") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Recherche par ville") 
            @RequestParam(required = false) String ville,
            @Parameter(description = "Recherche par raison sociale") 
            @RequestParam(required = false) String raisonSociale) {
        
        log.info("GET /api/v1/fournisseurs/search - Recherche avec paramètres: keyword={}, ville={}, raisonSociale={}", 
                 keyword, ville, raisonSociale);
        
        List<FournisseurResponseDTO> results;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            results = fournisseurService.searchFournisseurs(keyword);
        } else if (ville != null && !ville.trim().isEmpty()) {
            results = fournisseurService.searchByVille(ville);
        } else if (raisonSociale != null && !raisonSociale.trim().isEmpty()) {
            results = fournisseurService.searchByRaisonSociale(raisonSociale);
        } else {
            results = fournisseurService.getAllFournisseurs();
        }
        
        return ResponseEntity.ok(results);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Récupérer un fournisseur par email",
               description = "Retourne les détails d'un fournisseur par son adresse email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fournisseur trouvé"),
        @ApiResponse(responseCode = "404", description = "Fournisseur non trouvé")
    })
    public ResponseEntity<FournisseurResponseDTO> getFournisseurByEmail(
            @Parameter(description = "Email du fournisseur") @PathVariable String email) {
        log.info("GET /api/v1/fournisseurs/email/{} - Récupération du fournisseur par email", email);
        FournisseurResponseDTO fournisseur = fournisseurService.getFournisseurByEmail(email);
        return ResponseEntity.ok(fournisseur);
    }

    @GetMapping("/ice/{ice}")
    @Operation(summary = "Récupérer un fournisseur par ICE",
               description = "Retourne les détails d'un fournisseur par son numéro ICE")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fournisseur trouvé"),
        @ApiResponse(responseCode = "404", description = "Fournisseur non trouvé")
    })
    public ResponseEntity<FournisseurResponseDTO> getFournisseurByIce(
            @Parameter(description = "Numéro ICE du fournisseur") @PathVariable String ice) {
        log.info("GET /api/v1/fournisseurs/ice/{} - Récupération du fournisseur par ICE", ice);
        FournisseurResponseDTO fournisseur = fournisseurService.getFournisseurByIce(ice);
        return ResponseEntity.ok(fournisseur);
    }
}
