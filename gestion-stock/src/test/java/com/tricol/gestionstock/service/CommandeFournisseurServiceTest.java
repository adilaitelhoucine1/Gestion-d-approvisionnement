package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.commande.CommandeFournisseurResponseDTO;
import com.tricol.gestionstock.dto.commande.ReceptionCommandeDTO;
import com.tricol.gestionstock.entity.*;
import com.tricol.gestionstock.entity.Enums.StatutCommande;
import com.tricol.gestionstock.mapper.CommandeFournisseurMapper;
import com.tricol.gestionstock.repository.*;
import com.tricol.gestionstock.service.impl.CommandeFournisseurServiceImpl;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Création Automatique de Lots - Commande Fournisseur Service")
class CommandeFournisseurServiceTest {

    @Mock
    private CommandeFournisseurRepository commandeRepository;
    @Mock
    private ProduitRepository produitRepository;
    @Mock
    private LotStockRepository lotStockRepository;
    @Mock
    private MouvementStockRepository mouvementStockRepository;
    @Mock
    private CommandeFournisseurMapper mapper;

    @InjectMocks
    private CommandeFournisseurServiceImpl commandeService;

    private CommandeFournisseur commande;
    private Produit produit;
    private LotStock lotCapture;

    @BeforeEach
    void setUp() {
        produit = Produit.builder()
                .id(1L)
                .reference("PROD-001")
                .nom("Produit Test")
                .stockActuel(0)
                .build();

        commande = CommandeFournisseur.builder()
                .id(1L)
                .numeroCommande("CMD-2024-001")
                .fournisseur(Fournisseur.builder().id(1L).raisonSociale("Test SA").build())
                .statut(StatutCommande.VALIDEE)
                .lignesCommande(new ArrayList<>())
                .build();

        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(commandeRepository.save(any())).thenReturn(commande);
        when(produitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mouvementStockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponseDTO(any())).thenReturn(new CommandeFournisseurResponseDTO());

        when(lotStockRepository.save(any(LotStock.class))).thenAnswer(inv -> {
            lotCapture = inv.getArgument(0);
            return lotCapture;
        });
    }

    @Test
    @DisplayName("B.1 - Réception de commande validée crée automatiquement un lot de stock traçable")
    void testReceptionCreeLotAutomatiquement() {

        commande.getLignesCommande().add(LigneCommande.builder()
                .id(1L)
                .produit(produit)
                .quantite(100)
                .prixUnitaire(new BigDecimal("10.00"))
                .commande(commande)
                .build());


        commandeService.receptionnerCommande(1L,
                ReceptionCommandeDTO.builder().dateReception(LocalDate.now()).build());

        assertNotNull(lotCapture, "Un lot doit être créé");
        assertEquals(produit, lotCapture.getProduit());
        assertEquals(100, lotCapture.getQuantiteInitiale());
        assertEquals(100, lotCapture.getQuantiteRestante());
    }

    @Test
    @DisplayName("B.2 - Génération du numéro de lot, date d'entrée et prix d'achat unitaire")
    void testGenerationNumeroLotDatePrix() {
        LocalDate dateReception = LocalDate.of(2024, 11, 15);

        commande.getLignesCommande().add(LigneCommande.builder()
                .id(1L)
                .produit(produit)
                .quantite(50)
                .prixUnitaire(new BigDecimal("15.50"))
                .commande(commande)
                .build());

        commandeService.receptionnerCommande(1L,
                ReceptionCommandeDTO.builder().dateReception(dateReception).build());

        assertNotNull(lotCapture.getNumeroLot(), "Le numéro de lot doit être généré");
        assertTrue(lotCapture.getNumeroLot().startsWith("LOT-"), "Le numéro doit commencer par LOT-");
        assertEquals(dateReception, lotCapture.getDateEntree());
        assertEquals(new BigDecimal("15.50"), lotCapture.getPrixAchatUnitaire());
    }

    @Test
    @DisplayName("B.3 - Lien entre le lot créé et la réception fournisseur")
    void testLienLotReceptionFournisseur() {
        commande.getLignesCommande().add(LigneCommande.builder()
                .id(1L)
                .produit(produit)
                .quantite(75)
                .prixUnitaire(new BigDecimal("12.00"))
                .commande(commande)
                .build());

        commandeService.receptionnerCommande(1L,
                ReceptionCommandeDTO.builder().dateReception(LocalDate.now()).build());

        assertEquals(commande, lotCapture.getCommande(), "Le lot doit être lié à la commande");
        assertEquals("CMD-2024-001", lotCapture.getCommande().getNumeroCommande());
    }
}
