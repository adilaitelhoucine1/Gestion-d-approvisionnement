package com.tricol.gestionstock.service;
import com.tricol.gestionstock.dto.stock.MouvementStockDTO;
import java.util.List;
public interface MouvementService {
    List<MouvementStockDTO> findAll();
    List<MouvementStockDTO> findByProduitId(Long produitId);

    List<MouvementStockDTO> filterbycritiere(String critire ,String value , String condition);
}
