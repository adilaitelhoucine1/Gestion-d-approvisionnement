package com.tricol.gestionstock.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotStockDTO {
    private Long id;
    private String numeroLot;
    private LocalDate dateEntree;
    private Integer quantiteInitiale;
    private Integer quantiteRestante;
    private BigDecimal prixAchatUnitaire;
    private BigDecimal valorisation;
}

