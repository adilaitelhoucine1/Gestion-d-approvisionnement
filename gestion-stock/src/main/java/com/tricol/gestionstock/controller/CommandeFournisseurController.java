package com.tricol.gestionstock.controller;

import com.tricol.gestionstock.dto.commande.CommandeFournisseurRequestDTO;
import com.tricol.gestionstock.dto.commande.CommandeFournisseurResponseDTO;
import com.tricol.gestionstock.dto.commande.ReceptionCommandeDTO;
import com.tricol.gestionstock.entity.Enums.StatutCommande;
import com.tricol.gestionstock.service.CommandeFournisseurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/commandes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Commandes Fournisseurs", description = "Gestion des commandes fournisseurs")
public class CommandeFournisseurController {

    private final CommandeFournisseurService commandeService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREER_COMMANDE')")
    @Operation(summary = "Créer une commande", description = "Requiert la permission CREER_COMMANDE")
    public ResponseEntity<CommandeFournisseurResponseDTO> createCommande(
            @Valid @RequestBody CommandeFournisseurRequestDTO requestDTO) {
        CommandeFournisseurResponseDTO response = commandeService.createCommande(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONSULTER_COMMANDE')")
    @Operation(summary = "Lister toutes les commandes", description = "Requiert la permission CONSULTER_COMMANDE")
    public ResponseEntity<List<CommandeFournisseurResponseDTO>> getAllCommandes() {
        List<CommandeFournisseurResponseDTO> commandes = commandeService.getAllCommandes();
        return ResponseEntity.ok(commandes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONSULTER_COMMANDE')")
    @Operation(summary = "Obtenir une commande par ID", description = "Requiert la permission CONSULTER_COMMANDE")
    public ResponseEntity<CommandeFournisseurResponseDTO> getCommandeById(
           @PathVariable Long id) {
        CommandeFournisseurResponseDTO commande = commandeService.getCommandeById(id);
        return ResponseEntity.ok(commande);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MODIFIER_COMMANDE')")
    @Operation(summary = "Modifier une commande", description = "Requiert la permission MODIFIER_COMMANDE")
    public ResponseEntity<CommandeFournisseurResponseDTO> updateCommande(
            @PathVariable Long id,
            @Valid @RequestBody CommandeFournisseurRequestDTO requestDTO) {
        CommandeFournisseurResponseDTO response = commandeService.updateCommande(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNULER_COMMANDE')")
    @Operation(summary = "Supprimer une commande", description = "Requiert la permission ANNULER_COMMANDE")
    public ResponseEntity<Void> deleteCommande(
            @PathVariable Long id) {
        commandeService.deleteCommande(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fournisseur/{id}")
    @PreAuthorize("hasAuthority('CONSULTER_COMMANDE')")
    @Operation(summary = "Obtenir les commandes par fournisseur", description = "Requiert la permission CONSULTER_COMMANDE")
    public ResponseEntity<List<CommandeFournisseurResponseDTO>> getCommandesByFournisseur(
            @PathVariable Long id) {
        List<CommandeFournisseurResponseDTO> commandes = commandeService.getCommandesByFournisseur(id);
        return ResponseEntity.ok(commandes);
    }

    @PutMapping("/{id}/reception")
    @PreAuthorize("hasAuthority('RECEPTIONNER_COMMANDE')")
    @Operation(summary = "Réceptionner une commande", description = "Requiert la permission RECEPTIONNER_COMMANDE")
    public ResponseEntity<CommandeFournisseurResponseDTO> receptionnerCommande(
            @PathVariable Long id,
            @Valid @RequestBody ReceptionCommandeDTO receptionDTO) {
        CommandeFournisseurResponseDTO response = commandeService.receptionnerCommande(id, receptionDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasAuthority('ANNULER_COMMANDE')")
    @Operation(summary = "Annuler une commande", description = "Requiert la permission ANNULER_COMMANDE")
    public ResponseEntity<CommandeFournisseurResponseDTO> annulerCommande(
            @PathVariable Long id) {
        CommandeFournisseurResponseDTO response = commandeService.annulerCommande(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/valider")
    @PreAuthorize("hasAuthority('VALIDER_COMMANDE')")
    @Operation(summary = "Valider une commande", description = "Requiert la permission VALIDER_COMMANDE")
    public ResponseEntity<CommandeFournisseurResponseDTO> validerCommande(
            @PathVariable Long id) {
        CommandeFournisseurResponseDTO response = commandeService.validerCommande(id);
        return ResponseEntity.ok(response);
    }
}
