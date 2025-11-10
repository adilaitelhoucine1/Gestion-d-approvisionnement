package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.bonsortie.BonSortieRequestDTO;
import com.tricol.gestionstock.dto.bonsortie.BonSortieResponseDTO;

import java.util.List;

public interface BonSortieService {

    BonSortieResponseDTO createBonSortie(BonSortieRequestDTO requestDTO);

    List<BonSortieResponseDTO> getAllBonsSortie();

    BonSortieResponseDTO getBonSortieById(Long id);

    BonSortieResponseDTO updateBonSortie(Long id, BonSortieRequestDTO requestDTO);

    BonSortieResponseDTO validerBonSortie(Long id);

    BonSortieResponseDTO annulerBonSortie(Long id);

    List<BonSortieResponseDTO> getBonsSortieByAtelier(String atelier);
}

