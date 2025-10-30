package com.tricol.gestionstock.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lots_stock", indexes = {
        @Index(name = "idx_lot_numero", columnList = "numeroLot"),
        @Index(name = "idx_lot_produit", columnList = "produit_id"),
        @Index(name = "idx_lot_date", columnList = "dateEntree")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le numéro de lot est obligatoire")
    @Size(max = 50, message = "Le numéro de lot ne doit pas dépasser 50 caractères")
    @Column(nullable = false, unique = true, length = 50)
    private String numeroLot;

    @NotNull(message = "Le produit est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lot_produit"))
    private Produit produit;

    @NotNull(message = "La date d'entrée est obligatoire")
    @Column(nullable = false)
    private LocalDate dateEntree;

    @NotNull(message = "La quantité initiale est obligatoire")
    @Min(value = 1, message = "La quantité initiale doit être au moins 1")
    @Column(nullable = false)
    private Integer quantiteInitiale;

    @NotNull
    @Min(value = 0, message = "La quantité restante ne peut pas être négative")
    @Column(nullable = false)
    private Integer quantiteRestante;

    @NotNull(message = "Le prix d'achat unitaire est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix d'achat doit être supérieur à 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixAchatUnitaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", foreignKey = @ForeignKey(name = "fk_lot_commande"))
    private CommandeFournisseur commande;

    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MouvementStock> mouvements = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Consomme une quantité du lot (FIFO)
     */
    public void consommer(Integer quantite) {
        if (quantite > quantiteRestante) {
            throw new IllegalArgumentException(
                    "Quantité insuffisante dans le lot " + numeroLot +
                            ". Disponible: " + quantiteRestante + ", Demandé: " + quantite
            );
        }
        this.quantiteRestante -= quantite;
    }

    /**
     * Vérifie si le lot est épuisé
     */
    public boolean isEpuise() {
        return quantiteRestante == 0;
    }

    /**
     * Calcule la valorisation actuelle du lot
     */
    public BigDecimal getValorisation() {
        return prixAchatUnitaire.multiply(BigDecimal.valueOf(quantiteRestante));
    }

    /**
     * Initialise la quantité restante à la création
     */
    @PrePersist
    protected void onCreate() {
        if (quantiteRestante == null) {
            quantiteRestante = quantiteInitiale;
        }
    }
}