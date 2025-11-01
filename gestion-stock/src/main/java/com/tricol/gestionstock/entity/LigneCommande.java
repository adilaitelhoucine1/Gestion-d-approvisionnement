package com.tricol.gestionstock.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_commande", indexes = {
        @Index(name = "idx_ligne_commande", columnList = "commande_id"),
        @Index(name = "idx_ligne_produit", columnList = "produit_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La commande est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ligne_commande"))
    private CommandeFournisseur commande;

    @NotNull(message = "Le produit est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ligne_produit"))
    private Produit produit;

    @NotNull(message = "La quantite est obligatoire")
    @Min(value = 1, message = "La quantite doit être au moins 1")
    @Column(nullable = false)
    private Integer quantite;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix unitaire doit être superieur a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @NotNull
    @DecimalMin(value = "0.0", message = "Le sous-total ne peut pas être negatif")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sousTotal;

    /**
     * Calcule automatiquement le sous-total avant persistence/update
     */
    @PrePersist
    @PreUpdate
    protected void calculerSousTotal() {
        if (quantite != null && prixUnitaire != null) {
            this.sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }
}