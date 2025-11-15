package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.commande.CommandeFournisseurResponseDTO;
import com.tricol.gestionstock.dto.commande.ReceptionCommandeDTO;
import com.tricol.gestionstock.entity.*;
import com.tricol.gestionstock.entity.Enums.StatutCommande;
import com.tricol.gestionstock.entity.Enums.TypeMouvement;
import com.tricol.gestionstock.mapper.CommandeFournisseurMapper;
import com.tricol.gestionstock.repository.*;
import com.tricol.gestionstock.service.impl.CommandeFournisseurServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la création automatique de lots lors de la réception de commande
 * Tâche 1.1.B : Création Automatique de Lot
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Création Automatique de Lots - Commande Fournisseur Service")
class CommandeFournisseurServiceTest {

    @Mock
    private CommandeFournisseurRepository commandeRepository;

    @Mock
    private FournisseurRepository fournisseurRepository;

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private LigneCommandeRepository ligneCommandeRepository;

    @Mock
    private LotStockRepository lotStockRepository;

    @Mock
    private MouvementStockRepository mouvementStockRepository;

    @Mock
    private CommandeFournisseurMapper mapper;

    @InjectMocks
    private CommandeFournisseurServiceImpl commandeService;

    @Captor
    private ArgumentCaptor<LotStock> lotStockCaptor;

    @Captor
    private ArgumentCaptor<MouvementStock> mouvementStockCaptor;

    @Captor
    private ArgumentCaptor<Produit> produitCaptor;

    private CommandeFournisseur commande;
    private Fournisseur fournisseur;
    private Produit produit1;
    private Produit produit2;

    @BeforeEach
    void setUp() {
        // Initialisation du fournisseur
        fournisseur = Fournisseur.builder()
                .id(1L)
                .raisonSociale("Fournisseur Test SA")
                .adresse("123 Rue Test")
                .ville("Ville Test")
                .personneContact("Contact Test")
                .email("test@fournisseur.com")
                .build();

        // Initialisation des produits
        produit1 = Produit.builder()
                .id(1L)
                .reference("PROD-001")
                .nom("Produit Test 1")
                .stockActuel(0)
                .pointDeCommande(10)
                .build();

        produit2 = Produit.builder()
                .id(2L)
                .reference("PROD-002")
                .nom("Produit Test 2")
                .stockActuel(0)
                .pointDeCommande(20)
                .build();

        // Initialisation de la commande
        commande = CommandeFournisseur.builder()
                .id(1L)
                .numeroCommande("CMD-2024-001")
                .dateCommande(LocalDate.now().minusDays(10))
                .fournisseur(fournisseur)
                .statut(StatutCommande.VALIDEE)
                .montantTotal(new BigDecimal("1000.00"))
                .lignesCommande(new ArrayList<>())
                .build();
    }

    /**
     * TEST 1 : Vérifier qu'une réception de commande crée automatiquement un lot de stock
     * Une commande avec une ligne doit créer exactement un lot
     */
    @Test
    @DisplayName("Test 1: Réception de commande crée automatiquement un lot")
    void testReceptionCreeLotAutomatiquement() {
        // ARRANGE
        int quantiteCommandee = 100;
        BigDecimal prixUnitaire = new BigDecimal("10.00");

        LigneCommande ligne = creerLigneCommande(1L, produit1, quantiteCommandee, prixUnitaire);
        commande.getLignesCommande().add(ligne);

        ReceptionCommandeDTO receptionDTO = ReceptionCommandeDTO.builder()
                .dateReception(LocalDate.now())
                .observations("Livraison conforme")
                .build();

        configurerMocksReception(commande);

        // ACT
        CommandeFournisseurResponseDTO result = commandeService.receptionnerCommande(1L, receptionDTO);

        // ASSERT
        assertNotNull(result, "Le résultat ne doit pas être null");

        // Vérifier qu'un lot a été créé
        verify(lotStockRepository, times(1)).save(lotStockCaptor.capture());
        LotStock lotCree = lotStockCaptor.getValue();

        assertNotNull(lotCree, "Un lot doit être créé");
        assertEquals(produit1, lotCree.getProduit(), "Le lot doit être lié au bon produit");
        assertEquals(quantiteCommandee, lotCree.getQuantiteInitiale(),
                "La quantité initiale du lot doit correspondre à la quantité commandée");
        assertEquals(quantiteCommandee, lotCree.getQuantiteRestante(),
                "La quantité restante du lot doit être égale à la quantité initiale");
        assertEquals(prixUnitaire, lotCree.getPrixAchatUnitaire(),
                "Le prix d'achat unitaire doit être celui de la ligne de commande");
    }

    /**
     * TEST 2 : Vérifier la génération du numéro de lot, date d'entrée et prix d'achat
     */
    @Test
    @DisplayName("Test 2: Génération correcte du numéro de lot, date et prix")
    void testGenerationNumeroLotDatePrix() {
        // ARRANGE
        int quantiteCommandee = 50;
        BigDecimal prixUnitaire = new BigDecimal("15.50");
        LocalDate dateReception = LocalDate.of(2024, 11, 15);

        LigneCommande ligne = creerLigneCommande(1L, produit1, quantiteCommandee, prixUnitaire);
        commande.getLignesCommande().add(ligne);

        ReceptionCommandeDTO receptionDTO = ReceptionCommandeDTO.builder()
                .dateReception(dateReception)
                .observations("Réception test")
                .build();

        configurerMocksReception(commande);

        // ACT
        commandeService.receptionnerCommande(1L, receptionDTO);

        // ASSERT
        verify(lotStockRepository, times(1)).save(lotStockCaptor.capture());
        LotStock lotCree = lotStockCaptor.getValue();

        // Vérifier le numéro de lot
        assertNotNull(lotCree.getNumeroLot(), "Le numéro de lot doit être généré");
        assertTrue(lotCree.getNumeroLot().startsWith("LOT-"),
                "Le numéro de lot doit commencer par 'LOT-'");
        assertTrue(lotCree.getNumeroLot().length() > 4,
                "Le numéro de lot doit avoir une longueur significative");

        // Vérifier la date d'entrée
        assertEquals(dateReception, lotCree.getDateEntree(),
                "La date d'entrée du lot doit correspondre à la date de réception");

        // Vérifier le prix d'achat unitaire
        assertEquals(0, prixUnitaire.compareTo(lotCree.getPrixAchatUnitaire()),
                "Le prix d'achat unitaire doit être enregistré correctement");
    }

    /**
     * TEST 3 : Vérifier le lien entre le lot créé et la commande fournisseur
     */
    @Test
    @DisplayName("Test 3: Lot est lié à la commande fournisseur")
    void testLienLotCommandeFournisseur() {
        // ARRANGE
        int quantiteCommandee = 75;
        BigDecimal prixUnitaire = new BigDecimal("12.00");

        LigneCommande ligne = creerLigneCommande(1L, produit1, quantiteCommandee, prixUnitaire);
        commande.getLignesCommande().add(ligne);

        ReceptionCommandeDTO receptionDTO = ReceptionCommandeDTO.builder()
                .dateReception(LocalDate.now())
                .build();

        configurerMocksReception(commande);

        // ACT
        commandeService.receptionnerCommande(1L, receptionDTO);

        // ASSERT
        verify(lotStockRepository, times(1)).save(lotStockCaptor.capture());
        LotStock lotCree = lotStockCaptor.getValue();

        // Vérifier que le lot est lié à la commande
        assertEquals(commande, lotCree.getCommande(),
                "Le lot doit être lié à la commande fournisseur");
        assertEquals(commande.getId(), lotCree.getCommande().getId(),
                "L'ID de la commande doit correspondre");
        assertEquals(commande.getNumeroCommande(), lotCree.getCommande().getNumeroCommande(),
                "Le numéro de commande doit correspondre");
    }

    /**
     * TEST 4 : Vérifier qu'une commande avec plusieurs lignes crée plusieurs lots
     */
    @Test
    @DisplayName("Test 4: Commande multi-lignes crée plusieurs lots")
    void testCommandeMultiLignesCreePlusieursLots() {
        // ARRANGE
        LigneCommande ligne1 = creerLigneCommande(1L, produit1, 100, new BigDecimal("10.00"));
        LigneCommande ligne2 = creerLigneCommande(2L, produit2, 50, new BigDecimal("20.00"));

        commande.getLignesCommande().add(ligne1);
        commande.getLignesCommande().add(ligne2);

        ReceptionCommandeDTO receptionDTO = ReceptionCommandeDTO.builder()
                .dateReception(LocalDate.now())
                .build();

        configurerMocksReception(commande);

        // ACT
        commandeService.receptionnerCommande(1L, receptionDTO);

        // ASSERT
        // Vérifier que 2 lots ont été créés (un par ligne)
        verify(lotStockRepository, times(2)).save(lotStockCaptor.capture());
        List<LotStock> lotsCreees = lotStockCaptor.getAllValues();

        assertEquals(2, lotsCreees.size(), "Deux lots doivent être créés");

        // Vérifier le premier lot
        LotStock lot1 = lotsCreees.get(0);
        assertEquals(produit1, lot1.getProduit());
        assertEquals(100, lot1.getQuantiteInitiale());
        assertEquals(new BigDecimal("10.00"), lot1.getPrixAchatUnitaire());

        // Vérifier le deuxième lot
        LotStock lot2 = lotsCreees.get(1);
        assertEquals(produit2, lot2.getProduit());
        assertEquals(50, lot2.getQuantiteInitiale());
        assertEquals(new BigDecimal("20.00"), lot2.getPrixAchatUnitaire());
    }

    /**
     * TEST 5 : Vérifier la création d'un mouvement de stock lors de la réception
     */
    @Test
    @DisplayName("Test 5: Réception crée un mouvement de stock ENTREE")
    void testReceptionCreeMouvementStock() {
        // ARRANGE
        int quantiteCommandee = 80;
        BigDecimal prixUnitaire = new BigDecimal("11.00");

        LigneCommande ligne = creerLigneCommande(1L, produit1, quantiteCommandee, prixUnitaire);
        commande.getLignesCommande().add(ligne);

        ReceptionCommandeDTO receptionDTO = ReceptionCommandeDTO.builder()
                .dateReception(LocalDate.now())
                .build();

        configurerMocksReception(commande);

        // ACT
        commandeService.receptionnerCommande(1L, receptionDTO);

        // ASSERT
        verify(mouvementStockRepository, times(1)).save(mouvementStockCaptor.capture());
        MouvementStock mouvementCree = mouvementStockCaptor.getValue();

        assertNotNull(mouvementCree, "Un mouvement de stock doit être créé");
        assertEquals(TypeMouvement.ENTREE, mouvementCree.getTypeMouvement(),
                "Le type de mouvement doit être ENTREE");
        assertEquals(produit1, mouvementCree.getProduit(),
                "Le mouvement doit être lié au bon produit");
        assertEquals(quantiteCommandee, mouvementCree.getQuantite(),
                "La quantité du mouvement doit correspondre à la quantité reçue");
        assertEquals(commande, mouvementCree.getCommande(),
                "Le mouvement doit être lié à la commande");
        assertEquals(commande.getNumeroCommande(), mouvementCree.getReferenceDocument(),
                "La référence du document doit être le numéro de commande");
    }

    /**
     * TEST 6 : Vérifier l'incrémentation du stock du produit
     */
    @Test
    @DisplayName("Test 6: Réception incrémente le stock du produit")
    void testReceptionIncrementeStockProduit() {
        // ARRANGE
        int stockInitial = 25;
        int quantiteCommandee = 100;
        int stockFinalAttendu = stockInitial + quantiteCommandee; // 125

        produit1.setStockActuel(stockInitial);

        LigneCommande ligne = creerLigneCommande(1L, produit1, quantiteCommandee, new BigDecimal("10.00"));
        commande.getLignesCommande().add(ligne);

        ReceptionCommandeDTO receptionDTO = ReceptionCommandeDTO.builder()
                .dateReception(LocalDate.now())
                .build();

        configurerMocksReception(commande);

        // ACT
        commandeService.receptionnerCommande(1L, receptionDTO);

        // ASSERT
        verify(produitRepository, times(1)).save(produitCaptor.capture());
        Produit produitSauvegarde = produitCaptor.getValue();

        assertEquals(stockFinalAttendu, produitSauvegarde.getStockActuel(),
                "Le stock du produit doit être incrémenté de la quantité reçue");
    }

    /**
     * TEST 7 : Vérifier le changement de statut de la commande à LIVREE
     */
    @Test
    @DisplayName("Test 7: Réception change le statut de la commande à LIVREE")
    void testReceptionChangeStatutLivree() {
        // ARRANGE
        assertEquals(StatutCommande.VALIDEE, commande.getStatut(),
                "La commande doit être VALIDEE avant réception");

        LigneCommande ligne = creerLigneCommande(1L, produit1, 50, new BigDecimal("10.00"));
        commande.getLignesCommande().add(ligne);

        ReceptionCommandeDTO receptionDTO = ReceptionCommandeDTO.builder()
                .dateReception(LocalDate.now())
                .build();

        configurerMocksReception(commande);

        // ACT
        commandeService.receptionnerCommande(1L, receptionDTO);

        // ASSERT
        verify(commandeRepository, times(1)).save(any(CommandeFournisseur.class));
        assertEquals(StatutCommande.LIVREE, commande.getStatut(),
                "Le statut de la commande doit passer à LIVREE après réception");
    }

    /**
     * TEST 8 : Vérifier l'enregistrement de la date de réception
     */
    @Test
    @DisplayName("Test 8: Date de réception est enregistrée")
    void testDateReceptionEnregistree() {
        // ARRANGE
        LocalDate dateReception = LocalDate.of(2024, 11, 10);

        LigneCommande ligne = creerLigneCommande(1L, produit1, 50, new BigDecimal("10.00"));
        commande.getLignesCommande().add(ligne);

        ReceptionCommandeDTO receptionDTO = ReceptionCommandeDTO.builder()
                .dateReception(dateReception)
                .observations("Test date")
                .build();

        configurerMocksReception(commande);

        // ACT
        commandeService.receptionnerCommande(1L, receptionDTO);

        // ASSERT
        assertEquals(dateReception, commande.getDateReception(),
                "La date de réception doit être enregistrée dans la commande");
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Crée une ligne de commande pour les tests
     */
    private LigneCommande creerLigneCommande(Long id, Produit produit, int quantite, BigDecimal prixUnitaire) {
        return LigneCommande.builder()
                .id(id)
                .produit(produit)
                .quantite(quantite)
                .prixUnitaire(prixUnitaire)
                .commande(commande)
                .build();
    }

    /**
     * Configure les mocks pour la réception d'une commande
     */
    private void configurerMocksReception(CommandeFournisseur commande) {
        when(commandeRepository.findById(anyLong())).thenReturn(Optional.of(commande));
        when(lotStockRepository.save(any(LotStock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mouvementStockRepository.save(any(MouvementStock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(produitRepository.save(any(Produit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commandeRepository.save(any(CommandeFournisseur.class))).thenReturn(commande);
        when(mapper.toResponseDTO(any(CommandeFournisseur.class))).thenReturn(new CommandeFournisseurResponseDTO());
    }
}

