package com.tricol.gestionstock.dto.bonsortie;

import com.tricol.gestionstock.entity.Enums.MotifSortie;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonSortieRequestDTO {

    @NotBlank(message = "Le numero de bon est obligatoire")
    @Size(max = 50, message = "Le numero de bon ne doit pas depasser 50 caracteres")
    private String numeroBon;

    @NotNull(message = "La date de sortie est obligatoire")
    private LocalDate dateSortie;

    @NotBlank(message = "L'atelier destinataire est obligatoire")
    @Size(max = 100, message = "L'atelier destinataire ne doit pas depasser 100 caracteres")
    private String atelierDestinataire;

    @NotNull(message = "Le motif est obligatoire")
    private MotifSortie motif;

    @NotEmpty(message = "Au moins une ligne de bon de sortie est obligatoire")
    @Valid
    @Builder.Default
    private List<LigneBonSortieDTO> lignes = new ArrayList<>();

    @Size(max = 1000, message = "Les observations ne doivent pas depasser 1000 caracteres")
    private String observations;
}

