package com.tricol.gestionstock.controller;

import com.tricol.gestionstock.dto.commande.CommandeFournisseurRequestDTO;
import com.tricol.gestionstock.dto.commande.CommandeFournisseurResponseDTO;
import com.tricol.gestionstock.dto.commande.ReceptionCommandeDTO;
import com.tricol.gestionstock.entity.Enums.StatutCommande;
import com.tricol.gestionstock.service.CommandeFournisseurService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/commandes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Commandes Fournisseurs", description = "API de gestion des commandes fournisseurs")
public class CommandeFournisseurController {

    private final CommandeFournisseurService commandeService;

    @PostMapping
    @Operation(summary = "Creer une nouvelle commande fournisseur",
               description = "Cree une nouvelle commande avec ses lignes de commande associees")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Commande creee avec succes",
                     content = @Content(schema = @Schema(implementation = CommandeFournisseurResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Donnees invalides"),
        @ApiResponse(responseCode = "404", description = "Fournisseur ou produit non trouve"),
        @ApiResponse(responseCode = "409", description = "Numero de commande deja existant")
    })
    public ResponseEntity<CommandeFournisseurResponseDTO> createCommande(
            @Valid @RequestBody CommandeFournisseurRequestDTO requestDTO) {
        log.info("POST /api/v1/commandes - Creation d'une nouvelle commande");
        CommandeFournisseurResponseDTO response = commandeService.createCommande(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Recuperer toutes les commandes",
               description = "Retourne la liste complete de toutes les commandes fournisseurs triees par date decroissante")
    @ApiResponse(responseCode = "200", description = "Liste des commandes recuperee avec succes")
    public ResponseEntity<List<CommandeFournisseurResponseDTO>> getAllCommandes() {
        log.info("GET /api/v1/commandes - Recuperation de toutes les commandes");
        List<CommandeFournisseurResponseDTO> commandes = commandeService.getAllCommandes();
        return ResponseEntity.ok(commandes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une commande par ID",
               description = "Retourne les details complets d'une commande specifique avec ses lignes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Commande trouvee",
                     content = @Content(schema = @Schema(implementation = CommandeFournisseurResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Commande non trouvee")
    })
    public ResponseEntity<CommandeFournisseurResponseDTO> getCommandeById(
            @Parameter(description = "ID de la commande") @PathVariable Long id) {
        log.info("GET /api/v1/commandes/{} - Recuperation de la commande", id);
        CommandeFournisseurResponseDTO commande = commandeService.getCommandeById(id);
        return ResponseEntity.ok(commande);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre a jour une commande",
               description = "Met a jour les informations d'une commande existante (uniquement si EN_ATTENTE ou VALIDeE)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Commande mise a jour avec succes",
                     content = @Content(schema = @Schema(implementation = CommandeFournisseurResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Donnees invalides ou commande non modifiable"),
        @ApiResponse(responseCode = "404", description = "Commande non trouvee"),
        @ApiResponse(responseCode = "409", description = "Numero de commande deja existant")
    })
    public ResponseEntity<CommandeFournisseurResponseDTO> updateCommande(
            @Parameter(description = "ID de la commande") @PathVariable Long id,
            @Valid @RequestBody CommandeFournisseurRequestDTO requestDTO) {
        log.info("PUT /api/v1/commandes/{} - Mise a jour de la commande", id);
        CommandeFournisseurResponseDTO response = commandeService.updateCommande(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une commande",
               description = "Supprime une commande de la base de donnees (impossible si deja livree)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Commande supprimee avec succes"),
        @ApiResponse(responseCode = "400", description = "Commande non supprimable (deja livree)"),
        @ApiResponse(responseCode = "404", description = "Commande non trouvee")
    })
    public ResponseEntity<Void> deleteCommande(
            @Parameter(description = "ID de la commande") @PathVariable Long id) {
        log.info("DELETE /api/v1/commandes/{} - Suppression de la commande", id);
        commandeService.deleteCommande(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fournisseur/{id}")
    @Operation(summary = "Recuperer les commandes d'un fournisseur",
               description = "Retourne toutes les commandes d'un fournisseur specifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des commandes recuperee avec succes"),
        @ApiResponse(responseCode = "404", description = "Fournisseur non trouve")
    })
    public ResponseEntity<List<CommandeFournisseurResponseDTO>> getCommandesByFournisseur(
            @Parameter(description = "ID du fournisseur") @PathVariable Long id) {
        log.info("GET /api/v1/commandes/fournisseur/{} - Recuperation des commandes du fournisseur", id);
        List<CommandeFournisseurResponseDTO> commandes = commandeService.getCommandesByFournisseur(id);
        return ResponseEntity.ok(commandes);
    }

    @PutMapping("/{id}/reception")
    @Operation(summary = "Receptionner une commande",
               description = "Receptionne une commande validee et genere automatiquement les entrees de stock (lots FIFO) et mouvements")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Commande receptionnee avec succes, stock mis a jour",
                     content = @Content(schema = @Schema(implementation = CommandeFournisseurResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Commande non receptionnable (statut incorrect)"),
        @ApiResponse(responseCode = "404", description = "Commande non trouvee")
    })
    public ResponseEntity<CommandeFournisseurResponseDTO> receptionnerCommande(
            @Parameter(description = "ID de la commande") @PathVariable Long id,
            @Valid @RequestBody ReceptionCommandeDTO receptionDTO) {
        log.info("PUT /api/v1/commandes/{}/reception - Reception de la commande", id);
        CommandeFournisseurResponseDTO response = commandeService.receptionnerCommande(id, receptionDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des commandes avec filtres",
               description = "Recherche multicriteres: fournisseur, statut, periode. Tous les parametres sont optionnels.")
    @ApiResponse(responseCode = "200", description = "Resultats de recherche recuperes avec succes")
    public ResponseEntity<List<CommandeFournisseurResponseDTO>> searchCommandes(
            @Parameter(description = "ID du fournisseur (optionnel)")
            @RequestParam(required = false) Long fournisseurId,
            
            @Parameter(description = "Statut de la commande (optionnel)")
            @RequestParam(required = false) StatutCommande statut,
            
            @Parameter(description = "Date de debut de la periode (format: yyyy-MM-dd, optionnel)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            
            @Parameter(description = "Date de fin de la periode (format: yyyy-MM-dd, optionnel)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        
        log.info("GET /api/v1/commandes/search - Recherche avec filtres: fournisseur={}, statut={}, periode={} a {}",
                 fournisseurId, statut, dateDebut, dateFin);
        
        List<CommandeFournisseurResponseDTO> results = commandeService.searchCommandes(
                fournisseurId, statut, dateDebut, dateFin
        );
        
        return ResponseEntity.ok(results);
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Recuperer les commandes par statut",
               description = "Retourne toutes les commandes ayant un statut specifique")
    @ApiResponse(responseCode = "200", description = "Liste des commandes recuperee avec succes")
    public ResponseEntity<List<CommandeFournisseurResponseDTO>> getCommandesByStatut(
            @Parameter(description = "Statut de la commande") @PathVariable StatutCommande statut) {
        log.info("GET /api/v1/commandes/statut/{} - Recuperation des commandes par statut", statut);
        List<CommandeFournisseurResponseDTO> commandes = commandeService.getCommandesByStatut(statut);
        return ResponseEntity.ok(commandes);
    }

    @PutMapping("/{id}/annuler")
    @Operation(summary = "Annuler une commande",
               description = "Change le statut d'une commande a ANNULeE (impossible si deja livree)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Commande annulee avec succes"),
        @ApiResponse(responseCode = "400", description = "Commande non annulable"),
        @ApiResponse(responseCode = "404", description = "Commande non trouvee")
    })
    public ResponseEntity<CommandeFournisseurResponseDTO> annulerCommande(
            @Parameter(description = "ID de la commande") @PathVariable Long id) {
        log.info("PUT /api/v1/commandes/{}/annuler - Annulation de la commande", id);
        CommandeFournisseurResponseDTO response = commandeService.annulerCommande(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/valider")
    @Operation(summary = "Valider une commande",
               description = "Change le statut d'une commande de EN_ATTENTE a VALIDeE")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Commande validee avec succes"),
        @ApiResponse(responseCode = "400", description = "Commande non validable (statut incorrect)"),
        @ApiResponse(responseCode = "404", description = "Commande non trouvee")
    })
    public ResponseEntity<CommandeFournisseurResponseDTO> validerCommande(
            @Parameter(description = "ID de la commande") @PathVariable Long id) {
        log.info("PUT /api/v1/commandes/{}/valider - Validation de la commande", id);
        CommandeFournisseurResponseDTO response = commandeService.validerCommande(id);
        return ResponseEntity.ok(response);
    }
}
