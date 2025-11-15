package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.stock.MouvementStockDTO;
import com.tricol.gestionstock.dto.stock.MouvementStockSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MouvementService {

    List<MouvementStockDTO> findAll();

    List<MouvementStockDTO> findByProduitId(Long produitId);

    /**
     * Advanced search with pagination
     *
     * @param criteria Search criteria
     * @param pageable Pagination parameters
     * @return Page of movement DTOs
     */
    Page<MouvementStockDTO> searchMouvements(MouvementStockSearchCriteria criteria, Pageable pageable);
}
