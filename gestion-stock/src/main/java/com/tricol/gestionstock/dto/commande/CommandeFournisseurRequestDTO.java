package com.tricol.gestionstock.dto.commande;

import com.tricol.gestionstock.entity.Enums.StatutCommande;
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
public class CommandeFournisseurRequestDTO {

    @NotBlank(message = "Le numero de commande est obligatoire")
    @Size(max = 50, message = "Le numero de commande ne doit pas depasser 50 caracteres")
    private String numeroCommande;

    @NotNull(message = "La date de commande est obligatoire")
    private LocalDate dateCommande;

    @NotNull(message = "L'ID du fournisseur est obligatoire")
    private Long fournisseurId;

    private StatutCommande statut;

    @NotEmpty(message = "Au moins une ligne de commande est obligatoire")
    @Valid
    @Builder.Default
    private List<LigneCommandeDTO> lignesCommande = new ArrayList<>();

    @Size(max = 1000, message = "Les observations ne doivent pas depasser 1000 caracteres")
    private String observations;
}
