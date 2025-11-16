package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.bonsortie.BonSortieResponseDTO;
import com.tricol.gestionstock.entity.*;
import com.tricol.gestionstock.entity.Enums.MotifSortie;
import com.tricol.gestionstock.entity.Enums.StatutBonSortie;
import com.tricol.gestionstock.mapper.BonSortieMapper;
import com.tricol.gestionstock.repository.*;
import com.tricol.gestionstock.service.impl.BonSortieServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

@ExtendWith(MockitoExtension.class)
class BonSortieServiceFifoTest {

    @org.mockito.Mock BonSortieRepository bonSortieRepository;
    @org.mockito.Mock ProduitRepository produitRepository;
    @org.mockito.Mock LotStockRepository lotStockRepository;
    @org.mockito.Mock MouvementStockRepository mouvementStockRepository;
    @org.mockito.Mock BonSortieMapper mapper;

    @org.mockito.InjectMocks BonSortieServiceImpl service;

    @Test
    void sortie_partielle_un_seul_lot_fifo_simplifie() {

        Produit produit = Produit.builder()
                .id(1L).reference("PROD-001").nom("Produit Test")
                .stockActuel(100).pointDeCommande(10).build();

        LotStock lot = LotStock.builder()
                .id(10L).numeroLot("LOT-001").produit(produit)
                .dateEntree(LocalDate.now().minusDays(10))
                .quantiteInitiale(50).quantiteRestante(50)
                .prixAchatUnitaire(new BigDecimal("10.00")).build();

        // Bon de sortie en BROUILLON avec 1 ligne demandant 30
        LigneBonSortie ligne = LigneBonSortie.builder()
                .id(100L).produit(produit).quantiteDemandee(30).build();

        BonSortie bs = BonSortie.builder()
                .id(1L).numeroBon("BS-001")
                .dateSortie(LocalDate.now())
                .atelierDestinataire("Atelier A")
                .motif(MotifSortie.PRODUCTION)
                .statut(StatutBonSortie.BROUILLON)
                .lignes(new java.util.ArrayList<>(List.of(ligne)))
                .build();


        when(bonSortieRepository.findByIdWithLignes(1L)).thenReturn(Optional.of(bs));
        when(lotStockRepository.findLotsDisponiblesByProduitFIFO(produit.getId()))
                .thenReturn(List.of(lot));
        when(lotStockRepository.save(any(LotStock.class))).then(returnsFirstArg());
        when(mouvementStockRepository.save(any(MouvementStock.class))).then(returnsFirstArg());
        when(produitRepository.save(any(Produit.class))).then(returnsFirstArg());
        when(bonSortieRepository.save(any(BonSortie.class))).thenReturn(bs);
        when(mapper.toResponseDTO(any(BonSortie.class))).thenReturn(new BonSortieResponseDTO());


        BonSortieResponseDTO res = service.validerBonSortie(1L);


        assertNotNull(res, "Response should not be null");

         assertEquals(20, lot.getQuantiteRestante(), "Lot should have 20 remaining");
        assertFalse(isEpuise(lot), "Lot should not be marked exhausted");

         verify(mouvementStockRepository, times(1)).save(argThat(m ->
                m.getQuantite() == 30 &&
                        m.getProduit() == produit &&
                        m.getLot() == lot
        ));

         assertEquals(70, produit.getStockActuel(), "Product stock should be decreased to 70");
        verify(produitRepository, times(1)).save(same(produit));

         assertEquals(StatutBonSortie.VALIDE, bs.getStatut(), "Status should be VALIDE");
        assertNotNull(bs.getDateValidation(), "Validation date should be set");

         verify(lotStockRepository, times(1))
                .findLotsDisponiblesByProduitFIFO(eq(produit.getId()));

        verifyNoMoreInteractions(mouvementStockRepository, lotStockRepository, produitRepository);
    }

     private boolean isEpuise(LotStock lot) {
        try {
            return lot.isEpuise();
        } catch (Throwable ignored) {
            return lot.getQuantiteRestante() != null && lot.getQuantiteRestante() == 0;
        }
    }
}
