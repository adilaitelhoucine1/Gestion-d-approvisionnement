package com.tricol.gestionstock.service;
import com.tricol.gestionstock.dto.stock.AlerteStockDTO;
import com.tricol.gestionstock.dto.stock.EtatStockDTO;
import com.tricol.gestionstock.dto.stock.StockProduitDTO;
import java.math.BigDecimal;
import java.util.List;
public interface StockService {
    List<EtatStockDTO> getEtatGlobal();
    StockProduitDTO getLotsDisponiblesFifo(Long produitId);
    List<AlerteStockDTO> getAlertes();
    BigDecimal getValorisationFifo();
}
