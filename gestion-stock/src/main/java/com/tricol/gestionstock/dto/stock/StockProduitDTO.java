package com.tricol.gestionstock.dto.stock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockProduitDTO {
    private Long produitId;
    private String reference;
    private String nom;
    private Integer quantiteTotale;
    private BigDecimal valorisationTotale;
    private List<LotStockDTO> lots;
}
