package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.stock.MouvementStockDTO;
import com.tricol.gestionstock.dto.stock.MouvementStockSearchCriteria;
import com.tricol.gestionstock.entity.Enums.TypeMouvement;
import com.tricol.gestionstock.entity.MouvementStock;
import com.tricol.gestionstock.entity.Produit;
import com.tricol.gestionstock.entity.LotStock;
import com.tricol.gestionstock.repository.MouvementStockRepository;
import com.tricol.gestionstock.repository.ProduitRepository;
import com.tricol.gestionstock.service.impl.MouvementServiceImpl;
import com.tricol.gestionstock.specification.MouvementStockSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for advanced search functionality on MouvementStock
 */
@ExtendWith(MockitoExtension.class)
class MouvementServiceAdvancedSearchTest {

    @Mock
    private MouvementStockRepository mouvementStockRepository;

    @Mock
    private ProduitRepository produitRepository;

    @InjectMocks
    private MouvementServiceImpl mouvementService;

    private Produit produit;
    private LotStock lot;
    private MouvementStock mouvement1;
    private MouvementStock mouvement2;

    @BeforeEach
    void setUp() {
        // Setup test data
        produit = Produit.builder()
                .id(1L)
                .reference("PROD001")
                .nom("Produit Test")
                .prixUnitaire(new BigDecimal("100.00"))
                .build();

        lot = LotStock.builder()
                .id(1L)
                .numeroLot("LOT-2025-001")
                .produit(produit)
                .dateEntree(LocalDate.of(2025, 1, 15))
                .quantiteInitiale(100)
                .quantiteRestante(50)
                .build();

        mouvement1 = MouvementStock.builder()
                .id(1L)
                .produit(produit)
                .typeMouvement(TypeMouvement.ENTREE)
                .quantite(100)
                .dateMouvement(LocalDateTime.of(2025, 1, 15, 10, 0))
                .lot(lot)
                .prixUnitaire(new BigDecimal("100.00"))
                .build();

        mouvement2 = MouvementStock.builder()
                .id(2L)
                .produit(produit)
                .typeMouvement(TypeMouvement.SORTIE)
                .quantite(50)
                .dateMouvement(LocalDateTime.of(2025, 2, 10, 14, 30))
                .lot(lot)
                .build();
    }

    @Test
    void testSearchMouvements_WithPagination() {
        // Given
        MouvementStockSearchCriteria criteria = MouvementStockSearchCriteria.builder().build();
        Pageable pageable = PageRequest.of(0, 10);
        List<MouvementStock> mouvements = Arrays.asList(mouvement1, mouvement2);
        Page<MouvementStock> mouvementPage = new PageImpl<>(mouvements, pageable, mouvements.size());

        when(mouvementStockRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mouvementPage);

        // When
        Page<MouvementStockDTO> result = mouvementService.searchMouvements(criteria, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(mouvementStockRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testSearchMouvements_ByProduitId() {
        // Given
        MouvementStockSearchCriteria criteria = MouvementStockSearchCriteria.builder()
                .produitId(1L)
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        List<MouvementStock> mouvements = Arrays.asList(mouvement1, mouvement2);
        Page<MouvementStock> mouvementPage = new PageImpl<>(mouvements, pageable, mouvements.size());

        when(mouvementStockRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mouvementPage);

        // When
        Page<MouvementStockDTO> result = mouvementService.searchMouvements(criteria, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getProduitId()).isEqualTo(1L);
    }

    @Test
    void testSearchMouvements_ByTypeEntree() {
        // Given
        MouvementStockSearchCriteria criteria = MouvementStockSearchCriteria.builder()
                .type(TypeMouvement.ENTREE)
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        List<MouvementStock> mouvements = Arrays.asList(mouvement1);
        Page<MouvementStock> mouvementPage = new PageImpl<>(mouvements, pageable, mouvements.size());

        when(mouvementStockRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mouvementPage);

        // When
        Page<MouvementStockDTO> result = mouvementService.searchMouvements(criteria, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTypeMouvement()).isEqualTo("ENTREE");
    }

    @Test
    void testSearchMouvements_ByDateRange() {
        // Given
        MouvementStockSearchCriteria criteria = MouvementStockSearchCriteria.builder()
                .dateDebut(LocalDate.of(2025, 1, 1))
                .dateFin(LocalDate.of(2025, 1, 31))
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        List<MouvementStock> mouvements = Arrays.asList(mouvement1);
        Page<MouvementStock> mouvementPage = new PageImpl<>(mouvements, pageable, mouvements.size());

        when(mouvementStockRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mouvementPage);

        // When
        Page<MouvementStockDTO> result = mouvementService.searchMouvements(criteria, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void testSearchMouvements_MultiCriteria() {
        // Given
        MouvementStockSearchCriteria criteria = MouvementStockSearchCriteria.builder()
                .produitId(1L)
                .reference("PROD001")
                .type(TypeMouvement.ENTREE)
                .dateDebut(LocalDate.of(2025, 1, 1))
                .build();
        Pageable pageable = PageRequest.of(0, 20);
        List<MouvementStock> mouvements = Arrays.asList(mouvement1);
        Page<MouvementStock> mouvementPage = new PageImpl<>(mouvements, pageable, mouvements.size());

        when(mouvementStockRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mouvementPage);

        // When
        Page<MouvementStockDTO> result = mouvementService.searchMouvements(criteria, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProduitReference()).isEqualTo("PROD001");
        assertThat(result.getContent().get(0).getTypeMouvement()).isEqualTo("ENTREE");
    }
}

