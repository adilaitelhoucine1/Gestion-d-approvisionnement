package com.tricol.gestionstock.controller;

import com.tricol.gestionstock.dto.commande.CommandeFournisseurRequestDTO;
import com.tricol.gestionstock.dto.commande.CommandeFournisseurResponseDTO;
import com.tricol.gestionstock.dto.commande.ReceptionCommandeDTO;
import com.tricol.gestionstock.entity.Enums.StatutCommande;
import com.tricol.gestionstock.service.CommandeFournisseurService;
import io.swagger.v3.oas.annotations.Parameter;
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
public class CommandeFournisseurController {

    private final CommandeFournisseurService commandeService;

    @PostMapping

    public ResponseEntity<CommandeFournisseurResponseDTO> createCommande(
            @Valid @RequestBody CommandeFournisseurRequestDTO requestDTO) {
        CommandeFournisseurResponseDTO response = commandeService.createCommande(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping

    public ResponseEntity<List<CommandeFournisseurResponseDTO>> getAllCommandes() {
        List<CommandeFournisseurResponseDTO> commandes = commandeService.getAllCommandes();
        return ResponseEntity.ok(commandes);
    }

    @GetMapping("/{id}")

    public ResponseEntity<CommandeFournisseurResponseDTO> getCommandeById(
            @Parameter(description = "ID de la commande") @PathVariable Long id) {
        CommandeFournisseurResponseDTO commande = commandeService.getCommandeById(id);
        return ResponseEntity.ok(commande);
    }

    @PutMapping("/{id}")

    public ResponseEntity<CommandeFournisseurResponseDTO> updateCommande(
            @PathVariable Long id,
            @Valid @RequestBody CommandeFournisseurRequestDTO requestDTO) {
        CommandeFournisseurResponseDTO response = commandeService.updateCommande(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deleteCommande(
            @PathVariable Long id) {
        commandeService.deleteCommande(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fournisseur/{id}")

    public ResponseEntity<List<CommandeFournisseurResponseDTO>> getCommandesByFournisseur(
            @PathVariable Long id) {
        List<CommandeFournisseurResponseDTO> commandes = commandeService.getCommandesByFournisseur(id);
        return ResponseEntity.ok(commandes);
    }

    @PutMapping("/{id}/reception")

    public ResponseEntity<CommandeFournisseurResponseDTO> receptionnerCommande(
            @PathVariable Long id,
            @Valid @RequestBody ReceptionCommandeDTO receptionDTO) {
        CommandeFournisseurResponseDTO response = commandeService.receptionnerCommande(id, receptionDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")

    public ResponseEntity<List<CommandeFournisseurResponseDTO>> searchCommandes(

            @RequestParam(required = false) Long fournisseurId,

            @RequestParam(required = false) StatutCommande statut,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {


        List<CommandeFournisseurResponseDTO> results = commandeService.searchCommandes(
                fournisseurId, statut, dateDebut, dateFin
        );

        return ResponseEntity.ok(results);
    }

    @GetMapping("/statut/{statut}")

    public ResponseEntity<List<CommandeFournisseurResponseDTO>> getCommandesByStatut(
            @PathVariable StatutCommande statut) {
        List<CommandeFournisseurResponseDTO> commandes = commandeService.getCommandesByStatut(statut);
        return ResponseEntity.ok(commandes);
    }

    @PutMapping("/{id}/annuler")

    public ResponseEntity<CommandeFournisseurResponseDTO> annulerCommande(
            @PathVariable Long id) {
        CommandeFournisseurResponseDTO response = commandeService.annulerCommande(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/valider")

    public ResponseEntity<CommandeFournisseurResponseDTO> validerCommande(
            @PathVariable Long id) {
        CommandeFournisseurResponseDTO response = commandeService.validerCommande(id);
        return ResponseEntity.ok(response);
    }
}
