package com.tricol.gestionstock.entity;

import com.tricol.gestionstock.entity.Enums.TypeMouvement;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements_stock", indexes = {
        @Index(name = "idx_mouvement_produit", columnList = "produit_id"),
        @Index(name = "idx_mouvement_type", columnList = "typeMouvement"),
        @Index(name = "idx_mouvement_date", columnList = "dateMouvement")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le produit est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_mouvement_produit"))
    private Produit produit;

    @NotNull(message = "Le type de mouvement est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeMouvement typeMouvement;

    @NotNull(message = "La quantite est obligatoire")
    @Min(value = 1, message = "La quantite doit être au moins 1")
    @Column(nullable = false)
    private Integer quantite;

    @NotNull(message = "La date du mouvement est obligatoire")
    @Column(nullable = false)
    private LocalDateTime dateMouvement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", foreignKey = @ForeignKey(name = "fk_mouvement_lot"))
    private LotStock lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", foreignKey = @ForeignKey(name = "fk_mouvement_commande"))
    private CommandeFournisseur commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_sortie_id", foreignKey = @ForeignKey(name = "fk_mouvement_bon"))
    private BonSortie bonSortie;

    @DecimalMin(value = "0.0", message = "Le prix unitaire ne peut pas être negatif")
    @Column(precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Size(max = 100, message = "La reference ne doit pas depasser 100 caracteres")
    @Column(length = 100)
    private String referenceDocument;

    @Column(columnDefinition = "TEXT")
    private String observation;


    @PrePersist
    protected void onCreate() {
        if (dateMouvement == null) {
            dateMouvement = LocalDateTime.now();
        }
    }
}