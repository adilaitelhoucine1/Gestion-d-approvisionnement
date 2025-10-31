package com.tricol.gestionstock.controller;


import com.tricol.gestionstock.dto.produit.CreateProduitDTO;
import com.tricol.gestionstock.dto.produit.ProduitResponseDTO;
import com.tricol.gestionstock.service.ProductSservice;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProduitController {

    private final ProductSservice produitService;
    @PostMapping
    public ResponseEntity<ProduitResponseDTO> createProduit(
            @Valid @RequestBody CreateProduitDTO createDTO) {
            ProduitResponseDTO createdProduit = produitService.createProduit(createDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdProduit);
    }
    
}