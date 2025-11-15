package com.tricol.gestionstock.dto.stock;

import com.tricol.gestionstock.entity.Enums.TypeMouvement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MouvementStockSearchCriteria {

    private Long produitId;

    private String reference;

    private TypeMouvement type;

    private String numeroLot;

    private LocalDate dateDebut;

    private LocalDate dateFin;
}

