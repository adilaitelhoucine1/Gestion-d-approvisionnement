package com.tricol.gestionstock.dto.commande;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceptionCommandeDTO {

    @NotNull(message = "La date de reception est obligatoire")
    private LocalDate dateReception;

    private String observations;
}
