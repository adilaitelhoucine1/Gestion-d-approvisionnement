package com.tricol.gestionstock.service;

import com.tricol.gestionstock.entity.*;
import com.tricol.gestionstock.entity.Enums.MotifSortie;
import com.tricol.gestionstock.entity.Enums.StatutBonSortie;
import com.tricol.gestionstock.entity.Enums.TypeMouvement;
import com.tricol.gestionstock.mapper.BonSortieMapper;
import com.tricol.gestionstock.repository.*;
import com.tricol.gestionstock.service.impl.BonSortieServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;


@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Validation de Bon de Sortie")
class StockServiceTest {

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

    private Produit produit;
    private BonSortie bonSortie;
    private LigneBonSortie ligne;

    @BeforeEach
    void setUp() {
        produit = Produit.builder()
                .id(1L)
                .reference("PROD-001")
                .nom("Produit Test")
                .stockActuel(100)
                .pointDeCommande(10)
                .build();

        ligne = LigneBonSortie.builder()
                .id(1L)
                .produit(produit)
                .quantiteDemandee(30)
                .build();

        bonSortie = BonSortie.builder()
                .id(1L)
                .numeroBon("BS-001")
                .dateSortie(LocalDate.now())
                .atelierDestinataire("Atelier A")
                .motif(MotifSortie.PRODUCTION)
                .statut(StatutBonSortie.BROUILLON)
                .lignes(new ArrayList<>(List.of(ligne)))
                .build();

        ligne.setBonSortie(bonSortie);


        lenient().when(mapper.toResponseDTO(any(BonSortie.class))).thenReturn(null);
    }


    @Test
    @DisplayName("Test 1: Validation simple avec un seul lot")
    void testValidationBonSortieAvecUnSeulLot() {

        LotStock lot = LotStock.builder()
                .id(1L)
                .numeroLot("LOT-001")
                .produit(produit)
                .dateEntree(LocalDate.now().minusDays(10))
                .quantiteInitiale(100)
                .quantiteRestante(100)
                .prixAchatUnitaire(new BigDecimal("10.00"))
                .build();

        when(bonSortieRepository.findByIdWithLignes(1L)).thenReturn(Optional.of(bonSortie));
        when(lotStockRepository.findLotsDisponiblesByProduitFIFO(produit.getId()))
                .thenReturn(List.of(lot));
        when(lotStockRepository.save(any(LotStock.class))).thenReturn(lot);
        when(mouvementStockRepository.save(any(MouvementStock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(produitRepository.save(any(Produit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bonSortieRepository.save(any(BonSortie.class))).thenReturn(bonSortie);

        bonSortieService.validerBonSortie(1L);


        assertEquals(StatutBonSortie.VALIDE, bonSortie.getStatut(),
                "Le statut doit passer à VALIDE");
        assertNotNull(bonSortie.getDateValidation(),
                "La date de validation doit être enregistrée");

        assertEquals(70, lot.getQuantiteRestante(),
                "La quantité restante doit être mise à jour (100 - 30 = 70)");

        verify(mouvementStockRepository, times(1)).save(argThat(mouvement ->
                mouvement.getTypeMouvement() == TypeMouvement.SORTIE &&
                        mouvement.getQuantite() == 30 &&
                        mouvement.getProduit().equals(produit) &&
                        mouvement.getLot().equals(lot) &&
                        mouvement.getBonSortie().equals(bonSortie)
        ));
    }


    @Test
    @DisplayName("Test 2: Validation FIFO avec plusieurs lots")
    void testValidationBonSortieAvecPlusieursLotsFIFO() {

        ligne.setQuantiteDemandee(80);


        LotStock lot1 = LotStock.builder()
                .id(1L)
                .numeroLot("LOT-001")
                .produit(produit)
                .dateEntree(LocalDate.now().minusDays(30))
                .quantiteInitiale(50)
                .quantiteRestante(50)
                .prixAchatUnitaire(new BigDecimal("10.00"))
                .build();


        LotStock lot2 = LotStock.builder()
                .id(2L)
                .numeroLot("LOT-002")
                .produit(produit)
                .dateEntree(LocalDate.now().minusDays(10))
                .quantiteInitiale(60)
                .quantiteRestante(60)
                .prixAchatUnitaire(new BigDecimal("12.00"))
                .build();

        when(bonSortieRepository.findByIdWithLignes(1L)).thenReturn(Optional.of(bonSortie));
        when(lotStockRepository.findLotsDisponiblesByProduitFIFO(produit.getId()))
                .thenReturn(List.of(lot1, lot2));
        when(lotStockRepository.save(any(LotStock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mouvementStockRepository.save(any(MouvementStock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(produitRepository.save(any(Produit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bonSortieRepository.save(any(BonSortie.class))).thenReturn(bonSortie);


        bonSortieService.validerBonSortie(1L);

         assertEquals(0, lot1.getQuantiteRestante(),
                "Le lot le plus ancien doit être complètement consommé (50 unités)");

         assertEquals(30, lot2.getQuantiteRestante(),
                "Le lot récent doit avoir 30 unités restantes (60 - 30 = 30)");

         verify(mouvementStockRepository, times(2)).save(any(MouvementStock.class));

         verify(mouvementStockRepository).save(argThat(mouvement ->
                mouvement.getLot().equals(lot1) && mouvement.getQuantite() == 50
        ));

         verify(mouvementStockRepository).save(argThat(mouvement ->
                mouvement.getLot().equals(lot2) && mouvement.getQuantite() == 30
        ));
    }


    @Test
    @DisplayName("Test 3: Vérification des informations complètes du mouvement")
    void testValidationAvecInformationsCompletesMouvement() {
        LotStock lot = LotStock.builder()
                .id(1L)
                .numeroLot("LOT-001")
                .produit(produit)
                .dateEntree(LocalDate.now().minusDays(10))
                .quantiteInitiale(100)
                .quantiteRestante(100)
                .prixAchatUnitaire(new BigDecimal("15.50"))
                .build();

        when(bonSortieRepository.findByIdWithLignes(1L)).thenReturn(Optional.of(bonSortie));
        when(lotStockRepository.findLotsDisponiblesByProduitFIFO(produit.getId()))
                .thenReturn(List.of(lot));
        when(lotStockRepository.save(any(LotStock.class))).thenReturn(lot);
        when(mouvementStockRepository.save(any(MouvementStock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(produitRepository.save(any(Produit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bonSortieRepository.save(any(BonSortie.class))).thenReturn(bonSortie);

         bonSortieService.validerBonSortie(1L);

         verify(mouvementStockRepository).save(argThat(mouvement ->
                mouvement.getProduit().equals(produit)
                && mouvement.getTypeMouvement() == TypeMouvement.SORTIE
                && mouvement.getQuantite() == 30
                && mouvement.getLot().equals(lot)
                && mouvement.getBonSortie().equals(bonSortie)
                && mouvement.getPrixUnitaire().compareTo(new BigDecimal("15.50")) == 0
                && mouvement.getReferenceDocument().equals("BS-001")
                && mouvement.getObservation().contains("Atelier A")
                && mouvement.getObservation().contains("LOT-001")
        ));
    }


    @Test
    @DisplayName("Test 4: Impossible de valider un bon déjà validé")
    void testImpossibleDeValiderBonDejaValide() {

        bonSortie.setStatut(StatutBonSortie.VALIDE);

        when(bonSortieRepository.findByIdWithLignes(1L)).thenReturn(Optional.of(bonSortie));


        assertThrows(IllegalStateException.class, () -> bonSortieService.validerBonSortie(1L),
                "La validation d'un bon déjà validé doit lever une exception");


        verify(mouvementStockRepository, never()).save(any(MouvementStock.class));
    }


    @Test
    @DisplayName("Test 5: Validation avec plusieurs lignes de produits")
    void testValidationAvecPlusieursLignes() {
        Produit produit2 = Produit.builder()
                .id(2L)
                .reference("PROD-002")
                .nom("Produit Test 2")
                .stockActuel(50)
                .pointDeCommande(5)
                .build();

        LigneBonSortie ligne2 = LigneBonSortie.builder()
                .id(2L)
                .produit(produit2)
                .quantiteDemandee(20)
                .bonSortie(bonSortie)
                .build();

        bonSortie.getLignes().add(ligne2);

        LotStock lot1 = LotStock.builder()
                .id(1L)
                .numeroLot("LOT-001")
                .produit(produit)
                .dateEntree(LocalDate.now().minusDays(10))
                .quantiteInitiale(100)
                .quantiteRestante(100)
                .prixAchatUnitaire(new BigDecimal("10.00"))
                .build();

        LotStock lot2 = LotStock.builder()
                .id(2L)
                .numeroLot("LOT-002")
                .produit(produit2)
                .dateEntree(LocalDate.now().minusDays(5))
                .quantiteInitiale(50)
                .quantiteRestante(50)
                .prixAchatUnitaire(new BigDecimal("8.00"))
                .build();

        when(bonSortieRepository.findByIdWithLignes(1L)).thenReturn(Optional.of(bonSortie));
        when(lotStockRepository.findLotsDisponiblesByProduitFIFO(produit.getId()))
                .thenReturn(List.of(lot1));
        when(lotStockRepository.findLotsDisponiblesByProduitFIFO(produit2.getId()))
                .thenReturn(List.of(lot2));
        when(lotStockRepository.save(any(LotStock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mouvementStockRepository.save(any(MouvementStock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(produitRepository.save(any(Produit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bonSortieRepository.save(any(BonSortie.class))).thenReturn(bonSortie);


        bonSortieService.validerBonSortie(1L);

         verify(mouvementStockRepository, times(2)).save(any(MouvementStock.class));

         assertEquals(70, lot1.getQuantiteRestante(),
                "Le lot du produit 1 doit avoir 70 unités restantes");
        assertEquals(30, lot2.getQuantiteRestante(),
                "Le lot du produit 2 doit avoir 30 unités restantes");
    }
}

