package com.tricol.gestionstock.dto.bonsortie;

import com.tricol.gestionstock.entity.Enums.MotifSortie;
import com.tricol.gestionstock.entity.Enums.StatutBonSortie;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonSortieResponseDTO {

    private Long id;
    private String numeroBon;
    private LocalDate dateSortie;
    private String atelierDestinataire;
    private MotifSortie motif;
    private StatutBonSortie statut;
    private String observations;
    private LocalDateTime dateValidation;

    @Builder.Default
    private List<LigneBonSortieDTO> lignes = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

