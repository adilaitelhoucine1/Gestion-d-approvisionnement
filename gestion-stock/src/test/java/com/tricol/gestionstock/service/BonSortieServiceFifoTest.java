package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.bonsortie.BonSortieResponseDTO;
import com.tricol.gestionstock.entity.*;
import com.tricol.gestionstock.entity.Enums.MotifSortie;
import com.tricol.gestionstock.entity.Enums.StatutBonSortie;
import com.tricol.gestionstock.mapper.BonSortieMapper;
import com.tricol.gestionstock.repository.*;
import com.tricol.gestionstock.service.impl.BonSortieServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Tests unitaires pour la logique FIFO de gestion de stock
 * Partie 1 : Tests Unitaires de la Gestion de Stock
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests FIFO - Bon de Sortie Service")
class BonSortieServiceFifoTest {

    @Mock
    private BonSortieRepository bonSortieRepository;

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private LotStockRepository lotStockRepository;

    @Mock
    private MouvementStockRepository mouvementStockRepository;

    @Mock
    private BonSortieMapper mapper;

    @InjectMocks
    private BonSortieServiceImpl bonSortieService;

    @Captor
    private ArgumentCaptor<LotStock> lotStockCaptor;

    @Captor
    private ArgumentCaptor<MouvementStock> mouvementStockCaptor;

    @Captor
    private ArgumentCaptor<Produit> produitCaptor;

    private Produit produit;
    private BonSortie bonSortie;

    @BeforeEach
    void setUp() {
        // Initialisation du produit de test
        produit = Produit.builder()
                .id(1L)
                .reference("PROD-001")
                .nom("Produit Test")
                .stockActuel(100)
                .pointDeCommande(10)
                .build();

        // Initialisation du bon de sortie
        bonSortie = BonSortie.builder()
                .id(1L)
                .numeroBon("BS-2024-001")
                .dateSortie(LocalDate.now())
                .atelierDestinataire("Atelier A")
                .motif(MotifSortie.PRODUCTION)
                .statut(StatutBonSortie.BROUILLON)
                .lignes(new ArrayList<>())
                .build();
    }

    /**
     * SCENARIO 1 : Sortie simple consommant partiellement un seul lot
     * - Un seul lot avec 50 unités
     * - Demande de sortie de 30 unités
     * - Le lot doit avoir 20 unités restantes après consommation
     */
    @Test
    @DisplayName("Scenario 1: Sortie partielle d'un seul lot")
    void testSortiePartielleUnSeulLot() {
        // ARRANGE
        int quantiteLot = 50;
        int quantiteDemandee = 30;
        int quantiteAttendue = quantiteLot - quantiteDemandee; // 20

        // Création du lot le plus ancien
        LotStock lot1 = creerLot(1L, "LOT-001", LocalDate.now().minusDays(10), quantiteLot, new BigDecimal("10.00"));

        // Configuration des mocks
        configurerMocksValidation(produit, bonSortie, List.of(lot1), quantiteDemandee);

        // ACT
        BonSortieResponseDTO result = bonSortieService.validerBonSortie(1L);

        // ASSERT
        assertNotNull(result, "Le résultat ne doit pas être null");

        // Vérifier que le lot a été consommé correctement
        verify(lotStockRepository, times(1)).save(lotStockCaptor.capture());
        LotStock lotSauvegarde = lotStockCaptor.getValue();
        assertEquals(quantiteAttendue, lotSauvegarde.getQuantiteRestante(),
                "Le lot doit avoir " + quantiteAttendue + " unités restantes");

        // Vérifier qu'un seul mouvement de stock a été créé
        verify(mouvementStockRepository, times(1)).save(any(MouvementStock.class));

        // Vérifier que le stock du produit a été décrémenté
        verify(produitRepository, times(1)).save(produitCaptor.capture());
        Produit produitSauvegarde = produitCaptor.getValue();
        assertEquals(100 - quantiteDemandee, produitSauvegarde.getStockActuel(),
                "Le stock du produit doit être réduit de " + quantiteDemandee);
    }

    /**
     * SCENARIO 2 : Sortie nécessitant la consommation de plusieurs lots successifs
     * - Lot 1 (le plus ancien) : 30 unités
     * - Lot 2 (moyen) : 40 unités
     * - Lot 3 (le plus récent) : 50 unités
     * - Demande de sortie de 60 unités
     * - Lot 1 doit être épuisé (0 unités)
     * - Lot 2 doit avoir 10 unités restantes
     * - Lot 3 ne doit pas être touché (50 unités)
     */
    @Test
    @DisplayName("Scenario 2: Sortie consommant plusieurs lots successifs (FIFO)")
    void testSortiePlusieursLotsFifo() {
        // ARRANGE
        int quantiteDemandee = 60;

        // Création de 3 lots (du plus ancien au plus récent)
        LotStock lot1 = creerLot(1L, "LOT-001", LocalDate.now().minusDays(30), 30, new BigDecimal("10.00"));
        LotStock lot2 = creerLot(2L, "LOT-002", LocalDate.now().minusDays(20), 40, new BigDecimal("11.00"));
        LotStock lot3 = creerLot(3L, "LOT-003", LocalDate.now().minusDays(10), 50, new BigDecimal("12.00"));

        produit.setStockActuel(120); // Total des 3 lots

        // Configuration des mocks
        configurerMocksValidation(produit, bonSortie, Arrays.asList(lot1, lot2, lot3), quantiteDemandee);

        // ACT
        BonSortieResponseDTO result = bonSortieService.validerBonSortie(1L);

        // ASSERT
        assertNotNull(result);

        // Vérifier que 2 lots ont été sauvegardés (lot1 et lot2)
        verify(lotStockRepository, times(2)).save(lotStockCaptor.capture());
        List<LotStock> lotsSauvegardes = lotStockCaptor.getAllValues();

        // Vérifier lot 1 : complètement épuisé
        LotStock lot1Sauvegarde = lotsSauvegardes.get(0);
        assertEquals(0, lot1Sauvegarde.getQuantiteRestante(),
                "Le lot 1 (le plus ancien) doit être complètement épuisé");

        // Vérifier lot 2 : partiellement consommé (40 - 30 = 10)
        LotStock lot2Sauvegarde = lotsSauvegardes.get(1);
        assertEquals(10, lot2Sauvegarde.getQuantiteRestante(),
                "Le lot 2 doit avoir 10 unités restantes");

        // Vérifier que 2 mouvements de stock ont été créés (un par lot consommé)
        verify(mouvementStockRepository, times(2)).save(mouvementStockCaptor.capture());
        List<MouvementStock> mouvements = mouvementStockCaptor.getAllValues();

        // Vérifier les quantités des mouvements
        assertEquals(30, mouvements.get(0).getQuantite(), "Le premier mouvement doit être de 30 unités");
        assertEquals(30, mouvements.get(1).getQuantite(), "Le deuxième mouvement doit être de 30 unités");

        // Vérifier que le stock du produit a été décrémenté
        verify(produitRepository, times(1)).save(produitCaptor.capture());
        assertEquals(60, produitCaptor.getValue().getStockActuel(),
                "Le stock du produit doit être réduit de 60");
    }

    /**
     * SCENARIO 3 : Sortie avec stock insuffisant (gestion d'erreur)
     * - Stock disponible : 40 unités
     * - Demande de sortie : 50 unités
     * - Doit lever une exception IllegalStateException
     */
    @Test
    @DisplayName("Scenario 3: Stock insuffisant - Exception attendue")
    void testStockInsuffisant() {
        // ARRANGE
        int stockDisponible = 40;
        int quantiteDemandee = 50;

        produit.setStockActuel(stockDisponible);

        LotStock lot1 = creerLot(1L, "LOT-001", LocalDate.now(), stockDisponible, new BigDecimal("10.00"));

        // Configuration des mocks
        configurerMocksValidation(produit, bonSortie, List.of(lot1), quantiteDemandee);

        // ACT & ASSERT
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bonSortieService.validerBonSortie(1L),
                "Une exception doit être levée pour stock insuffisant");

        assertTrue(exception.getMessage().contains("Stock insuffisant"),
                "Le message d'erreur doit mentionner le stock insuffisant");

        // Vérifier qu'aucun lot n'a été sauvegardé
        verify(lotStockRepository, never()).save(any(LotStock.class));

        // Vérifier qu'aucun mouvement n'a été créé
        verify(mouvementStockRepository, never()).save(any(MouvementStock.class));

        // Vérifier que le produit n'a pas été modifié
        verify(produitRepository, never()).save(any(Produit.class));
    }

    /**
     * SCENARIO 4 : Sortie épuisant exactement le stock disponible
     * - Lot 1 : 30 unités
     * - Lot 2 : 20 unités
     * - Demande de sortie : 50 unités (exactement le total)
     * - Les deux lots doivent être épuisés (0 unités chacun)
     */
    @Test
    @DisplayName("Scenario 4: Sortie épuisant exactement le stock")
    void testSortieEpuisantExactementLeStock() {
        // ARRANGE
        int quantiteDemandee = 50;

        LotStock lot1 = creerLot(1L, "LOT-001", LocalDate.now().minusDays(20), 30, new BigDecimal("10.00"));
        LotStock lot2 = creerLot(2L, "LOT-002", LocalDate.now().minusDays(10), 20, new BigDecimal("11.00"));

        produit.setStockActuel(50);

        // Configuration des mocks
        configurerMocksValidation(produit, bonSortie, Arrays.asList(lot1, lot2), quantiteDemandee);

        // ACT
        BonSortieResponseDTO result = bonSortieService.validerBonSortie(1L);

        // ASSERT
        assertNotNull(result);

        // Vérifier que les 2 lots ont été sauvegardés
        verify(lotStockRepository, times(2)).save(lotStockCaptor.capture());
        List<LotStock> lotsSauvegardes = lotStockCaptor.getAllValues();

        // Vérifier que les deux lots sont épuisés
        assertEquals(0, lotsSauvegardes.get(0).getQuantiteRestante(),
                "Le lot 1 doit être complètement épuisé");
        assertEquals(0, lotsSauvegardes.get(1).getQuantiteRestante(),
                "Le lot 2 doit être complètement épuisé");

        // Vérifier que les deux lots sont marqués comme épuisés
        assertTrue(lotsSauvegardes.get(0).isEpuise(), "Le lot 1 doit être marqué comme épuisé");
        assertTrue(lotsSauvegardes.get(1).isEpuise(), "Le lot 2 doit être marqué comme épuisé");

        // Vérifier que 2 mouvements ont été créés
        verify(mouvementStockRepository, times(2)).save(mouvementStockCaptor.capture());
        List<MouvementStock> mouvements = mouvementStockCaptor.getAllValues();

        assertEquals(30, mouvements.get(0).getQuantite());
        assertEquals(20, mouvements.get(1).getQuantite());

        // Vérifier que le stock du produit est à 0
        verify(produitRepository, times(1)).save(produitCaptor.capture());
        assertEquals(0, produitCaptor.getValue().getStockActuel(),
                "Le stock du produit doit être à 0");
    }

    /**
     * SCENARIO 5 : Tests des Transitions de Statut
     * Tâche 1.2 : Vérifier que la validation d'un bon de sortie déclenche les bonnes actions
     * - Changement de statut de BROUILLON à VALIDÉ
     * - Création des mouvements de stock
     * - Mise à jour des quantités dans les lots
     */
    @Test
    @DisplayName("Scenario 5: Validation change le statut de BROUILLON à VALIDÉ")
    void testTransitionStatutBrouillonVersValide() {
        // ARRANGE
        int quantiteDemandee = 30;

        // Vérifier que le bon de sortie est en BROUILLON au départ
        assertEquals(StatutBonSortie.BROUILLON, bonSortie.getStatut(),
                "Le bon de sortie doit être en statut BROUILLON avant validation");

        LotStock lot1 = creerLot(1L, "LOT-001", LocalDate.now().minusDays(10), 50, new BigDecimal("10.00"));

        // Configuration des mocks
        configurerMocksValidation(produit, bonSortie, List.of(lot1), quantiteDemandee);

        // ACT
        BonSortieResponseDTO result = bonSortieService.validerBonSortie(1L);

        // ASSERT
        assertNotNull(result, "Le résultat ne doit pas être null");

        // Vérifier que le statut a changé
        verify(bonSortieRepository, times(1)).save(any(BonSortie.class));
        assertEquals(StatutBonSortie.VALIDE, bonSortie.getStatut(),
                "Le statut doit passer à VALIDE après validation");

        // Vérifier que la date de validation a été enregistrée
        assertNotNull(bonSortie.getDateValidation(),
                "La date de validation doit être enregistrée");

        // Vérifier que les mouvements ont été créés
        verify(mouvementStockRepository, times(1)).save(any(MouvementStock.class));

        // Vérifier que les lots ont été mis à jour
        verify(lotStockRepository, times(1)).save(any(LotStock.class));
    }

    /**
     * SCENARIO 6 : Vérifier que les mouvements de stock créés sont correctement liés
     */
    @Test
    @DisplayName("Scenario 6: Mouvements de stock sont correctement créés et liés")
    void testMouvementsStockCorrectementLies() {
        // ARRANGE
        int quantiteDemandee = 40;

        LotStock lot1 = creerLot(1L, "LOT-001", LocalDate.now().minusDays(20), 30, new BigDecimal("10.00"));
        LotStock lot2 = creerLot(2L, "LOT-002", LocalDate.now().minusDays(10), 50, new BigDecimal("11.00"));

        produit.setStockActuel(80);

        // Configuration des mocks
        configurerMocksValidation(produit, bonSortie, Arrays.asList(lot1, lot2), quantiteDemandee);

        // ACT
        bonSortieService.validerBonSortie(1L);

        // ASSERT
        verify(mouvementStockRepository, times(2)).save(mouvementStockCaptor.capture());
        List<MouvementStock> mouvements = mouvementStockCaptor.getAllValues();

        // Vérifier le premier mouvement (lot 1 - consommé complètement)
        MouvementStock mouvement1 = mouvements.get(0);
        assertEquals(30, mouvement1.getQuantite(), "Le premier mouvement doit être de 30 unités");
        assertEquals(produit, mouvement1.getProduit(), "Le mouvement doit être lié au bon produit");
        assertNotNull(mouvement1.getDateMouvement(), "La date du mouvement doit être enregistrée");

        // Vérifier le deuxième mouvement (lot 2 - partiellement consommé)
        MouvementStock mouvement2 = mouvements.get(1);
        assertEquals(10, mouvement2.getQuantite(), "Le deuxième mouvement doit être de 10 unités");
        assertEquals(produit, mouvement2.getProduit(), "Le mouvement doit être lié au bon produit");
    }

    /**
     * SCENARIO 7 : Vérifier que toutes les lignes du bon de sortie sont traitées
     */
    @Test
    @DisplayName("Scenario 7: Toutes les lignes du bon de sortie sont traitées")
    void testToutesLignesTraitees() {
        // ARRANGE
        int quantiteDemandee = 25;

        LotStock lot1 = creerLot(1L, "LOT-001", LocalDate.now().minusDays(10), 100, new BigDecimal("10.00"));

        // Configuration des mocks
        configurerMocksValidation(produit, bonSortie, List.of(lot1), quantiteDemandee);

        // ACT
        BonSortieResponseDTO result = bonSortieService.validerBonSortie(1L);

        // ASSERT
        assertNotNull(result);

        // Vérifier que la ligne a été traitée
        assertEquals(1, bonSortie.getLignes().size(), "Le bon de sortie doit avoir une ligne");
        LigneBonSortie ligne = bonSortie.getLignes().get(0);
        assertEquals(quantiteDemandee, ligne.getQuantiteDemandee(),
                "La quantité demandée doit être celle de la ligne");

        // Vérifier que le stock a été décrémenté du bon montant
        verify(produitRepository, times(1)).save(produitCaptor.capture());
        Produit produitSauvegarde = produitCaptor.getValue();
        assertEquals(100 - quantiteDemandee, produitSauvegarde.getStockActuel(),
                "Le stock doit être réduit de la quantité demandée");
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Crée un lot de stock pour les tests
     */
    private LotStock creerLot(Long id, String numeroLot, LocalDate dateEntree,
                              int quantite, BigDecimal prixUnitaire) {
        return LotStock.builder()
                .id(id)
                .numeroLot(numeroLot)
                .produit(produit)
                .dateEntree(dateEntree)
                .quantiteInitiale(quantite)
                .quantiteRestante(quantite)
                .prixAchatUnitaire(prixUnitaire)
                .build();
    }

    /**
     * Configure les mocks pour la validation d'un bon de sortie
     */
    private void configurerMocksValidation(Produit produit, BonSortie bonSortie,
                                           List<LotStock> lots, int quantiteDemandee) {
        // Créer une ligne de bon de sortie
        LigneBonSortie ligne = LigneBonSortie.builder()
                .id(1L)
                .produit(produit)
                .quantiteDemandee(quantiteDemandee)
                .build();
        bonSortie.getLignes().add(ligne);

        // Configuration des mocks (use lenient for cases where exception might prevent some mocks from being called)
        lenient().when(bonSortieRepository.findByIdWithLignes(anyLong())).thenReturn(Optional.of(bonSortie));
        lenient().when(lotStockRepository.findLotsDisponiblesByProduitFIFO(produit.getId())).thenReturn(lots);
        lenient().when(lotStockRepository.save(any(LotStock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(mouvementStockRepository.save(any(MouvementStock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(produitRepository.save(any(Produit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(bonSortieRepository.save(any(BonSortie.class))).thenReturn(bonSortie);
        lenient().when(mapper.toResponseDTO(any(BonSortie.class))).thenReturn(new BonSortieResponseDTO());
    }
}

