package com.tricol.gestionstock.service;

import com.tricol.gestionstock.entity.LotStock;
import com.tricol.gestionstock.entity.Produit;
import com.tricol.gestionstock.repository.LotStockRepository;
import com.tricol.gestionstock.repository.ProduitRepository;
import com.tricol.gestionstock.mapper.StockMapper;
import com.tricol.gestionstock.service.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la valorisation du stock selon la méthode FIFO
 * Tâche 1.1.C : Calcul de Valorisation du Stock
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Valorisation FIFO - Stock Service")
class StockServiceTest {

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private LotStockRepository lotStockRepository;

    @Mock
    private StockMapper stockMapper;

    @InjectMocks
    private StockServiceImpl stockService;

    private Produit produit;

    @BeforeEach
    void setUp() {
        produit = Produit.builder()
                .id(1L)
                .reference("PROD-001")
                .nom("Produit Test")
                .stockActuel(100)
                .pointDeCommande(10)
                .build();
    }

    /**
     * TEST 1 : Calcul de valorisation avec un seul lot
     * Vérifie que la valorisation = quantité restante × prix unitaire
     */
    @Test
    @DisplayName("Test 1: Valorisation d'un seul lot")
    void testValorisationUnSeulLot() {
        // ARRANGE
        int quantiteRestante = 50;
        BigDecimal prixUnitaire = new BigDecimal("10.00");
        BigDecimal valorisationAttendue = new BigDecimal("500.00"); // 50 × 10.00

        LotStock lot = creerLot(1L, "LOT-001", LocalDate.now(), 50, quantiteRestante, prixUnitaire);

        when(lotStockRepository.findAll()).thenReturn(List.of(lot));

        // ACT
        BigDecimal valorisation = stockService.getValorisationFifo();

        // ASSERT
        assertNotNull(valorisation, "La valorisation ne doit pas être null");
        assertEquals(0, valorisationAttendue.compareTo(valorisation),
                "La valorisation doit être de 500.00 (50 × 10.00)");

        verify(lotStockRepository, times(1)).findAll();
    }

    /**
     * TEST 2 : Calcul de valorisation avec plusieurs lots à prix différents
     * Vérifie que la valorisation totale est la somme des valorisations de chaque lot
     */
    @Test
    @DisplayName("Test 2: Valorisation avec plusieurs lots à prix différents")
    void testValorisationPlusieursLotsPrixDifferents() {
        // ARRANGE
        // Lot 1: 30 unités × 10.00 = 300.00
        LotStock lot1 = creerLot(1L, "LOT-001", LocalDate.now().minusDays(30), 30, 30, new BigDecimal("10.00"));

        // Lot 2: 40 unités × 11.50 = 460.00
        LotStock lot2 = creerLot(2L, "LOT-002", LocalDate.now().minusDays(20), 40, 40, new BigDecimal("11.50"));

        // Lot 3: 50 unités × 12.00 = 600.00
        LotStock lot3 = creerLot(3L, "LOT-003", LocalDate.now().minusDays(10), 50, 50, new BigDecimal("12.00"));

        // Valorisation totale attendue = 300.00 + 460.00 + 600.00 = 1360.00
        BigDecimal valorisationAttendue = new BigDecimal("1360.00");

        when(lotStockRepository.findAll()).thenReturn(Arrays.asList(lot1, lot2, lot3));

        // ACT
        BigDecimal valorisation = stockService.getValorisationFifo();

        // ASSERT
        assertNotNull(valorisation);
        assertEquals(0, valorisationAttendue.compareTo(valorisation),
                "La valorisation totale doit être de 1360.00 (300 + 460 + 600)");

        verify(lotStockRepository, times(1)).findAll();
    }

    /**
     * TEST 3 : Calcul de valorisation avec lots partiellement consommés
     * Vérifie que seule la quantité restante est valorisée (FIFO)
     */
    @Test
    @DisplayName("Test 3: Valorisation selon FIFO avec lots partiellement consommés")
    void testValorisationFifoLotsPartiellementConsommes() {
        // ARRANGE
        // Lot 1 (le plus ancien): quantité initiale 100, restante 20 → 20 × 10.00 = 200.00
        LotStock lot1 = creerLot(1L, "LOT-001", LocalDate.now().minusDays(30), 100, 20, new BigDecimal("10.00"));

        // Lot 2 (moyen): quantité initiale 80, restante 50 → 50 × 11.00 = 550.00
        LotStock lot2 = creerLot(2L, "LOT-002", LocalDate.now().minusDays(20), 80, 50, new BigDecimal("11.00"));

        // Lot 3 (le plus récent): quantité initiale 60, restante 60 (non consommé) → 60 × 12.00 = 720.00
        LotStock lot3 = creerLot(3L, "LOT-003", LocalDate.now().minusDays(10), 60, 60, new BigDecimal("12.00"));

        // Valorisation attendue = 200.00 + 550.00 + 720.00 = 1470.00
        BigDecimal valorisationAttendue = new BigDecimal("1470.00");

        when(lotStockRepository.findAll()).thenReturn(Arrays.asList(lot1, lot2, lot3));

        // ACT
        BigDecimal valorisation = stockService.getValorisationFifo();

        // ASSERT
        assertNotNull(valorisation);
        assertEquals(0, valorisationAttendue.compareTo(valorisation),
                "La valorisation doit être basée sur les quantités restantes (1470.00)");

        verify(lotStockRepository, times(1)).findAll();
    }

    /**
     * TEST 4 : Calcul de valorisation avec des lots complètement épuisés
     * Vérifie que les lots épuisés ne sont pas comptabilisés
     */
    @Test
    @DisplayName("Test 4: Lots épuisés ne sont pas valorisés")
    void testValorisationLotsEpuises() {
        // ARRANGE
        // Lot 1: complètement épuisé (quantité restante = 0)
        LotStock lot1 = creerLot(1L, "LOT-001", LocalDate.now().minusDays(30), 100, 0, new BigDecimal("10.00"));

        // Lot 2: partiellement consommé (quantité restante = 25)
        LotStock lot2 = creerLot(2L, "LOT-002", LocalDate.now().minusDays(20), 50, 25, new BigDecimal("12.00"));

        // Lot 3: complètement épuisé (quantité restante = 0)
        LotStock lot3 = creerLot(3L, "LOT-003", LocalDate.now().minusDays(10), 75, 0, new BigDecimal("11.00"));

        // Valorisation attendue = 0 + (25 × 12.00) + 0 = 300.00
        BigDecimal valorisationAttendue = new BigDecimal("300.00");

        when(lotStockRepository.findAll()).thenReturn(Arrays.asList(lot1, lot2, lot3));

        // ACT
        BigDecimal valorisation = stockService.getValorisationFifo();

        // ASSERT
        assertNotNull(valorisation);
        assertEquals(0, valorisationAttendue.compareTo(valorisation),
                "Seuls les lots avec quantité restante > 0 doivent être valorisés (300.00)");

        verify(lotStockRepository, times(1)).findAll();
    }

    /**
     * TEST 5 : Calcul de valorisation avec stock vide
     * Vérifie que la valorisation est zéro quand il n'y a pas de lots
     */
    @Test
    @DisplayName("Test 5: Valorisation avec stock vide")
    void testValorisationStockVide() {
        // ARRANGE
        when(lotStockRepository.findAll()).thenReturn(Collections.emptyList());

        // ACT
        BigDecimal valorisation = stockService.getValorisationFifo();

        // ASSERT
        assertNotNull(valorisation, "La valorisation ne doit pas être null");
        assertEquals(0, BigDecimal.ZERO.compareTo(valorisation),
                "La valorisation doit être zéro quand il n'y a pas de lots");

        verify(lotStockRepository, times(1)).findAll();
    }

    /**
     * TEST 6 : Vérification du calcul de la méthode getValorisation() de LotStock
     * Test unitaire de la méthode d'entité
     */
    @Test
    @DisplayName("Test 6: Méthode getValorisation() de LotStock")
    void testGetValorisationLotStock() {
        // ARRANGE
        LotStock lot = creerLot(1L, "LOT-001", LocalDate.now(), 100, 75, new BigDecimal("15.50"));

        // ACT
        BigDecimal valorisation = lot.getValorisation();

        // ASSERT
        BigDecimal valorisationAttendue = new BigDecimal("1162.50"); // 75 × 15.50
        assertNotNull(valorisation);
        assertEquals(0, valorisationAttendue.compareTo(valorisation),
                "La valorisation du lot doit être 1162.50 (75 × 15.50)");
    }

    /**
     * TEST 7 : Calcul de valorisation avec prix unitaires décimaux
     * Vérifie la précision des calculs avec des prix à virgule
     */
    @Test
    @DisplayName("Test 7: Valorisation avec prix décimaux")
    void testValorisationPrixDecimaux() {
        // ARRANGE
        // Lot 1: 33 unités × 10.33 = 340.89
        LotStock lot1 = creerLot(1L, "LOT-001", LocalDate.now().minusDays(20), 33, 33, new BigDecimal("10.33"));

        // Lot 2: 47 unités × 15.67 = 736.49
        LotStock lot2 = creerLot(2L, "LOT-002", LocalDate.now().minusDays(10), 47, 47, new BigDecimal("15.67"));

        // Valorisation attendue = 340.89 + 736.49 = 1077.38
        BigDecimal valorisationAttendue = new BigDecimal("1077.38");

        when(lotStockRepository.findAll()).thenReturn(Arrays.asList(lot1, lot2));

        // ACT
        BigDecimal valorisation = stockService.getValorisationFifo();

        // ASSERT
        assertNotNull(valorisation);
        assertEquals(0, valorisationAttendue.compareTo(valorisation),
                "La valorisation doit gérer correctement les prix décimaux (1077.38)");

        verify(lotStockRepository, times(1)).findAll();
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Crée un lot de stock pour les tests
     */
    private LotStock creerLot(Long id, String numeroLot, LocalDate dateEntree,
                              int quantiteInitiale, int quantiteRestante, BigDecimal prixUnitaire) {
        return LotStock.builder()
                .id(id)
                .numeroLot(numeroLot)
                .produit(produit)
                .dateEntree(dateEntree)
                .quantiteInitiale(quantiteInitiale)
                .quantiteRestante(quantiteRestante)
                .prixAchatUnitaire(prixUnitaire)
                .build();
    }
}

