package com.tricol.gestionstock.entity;

import com.tricol.gestionstock.entity.Enums.StatutCommande;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commandes_fournisseurs", indexes = {
        @Index(name = "idx_commande_numero", columnList = "numeroCommande"),
        @Index(name = "idx_commande_statut", columnList = "statut"),
        @Index(name = "idx_commande_date", columnList = "dateCommande")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeFournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le numéro de commande est obligatoire")
    @Size(max = 50, message = "Le numéro de commande ne doit pas dépasser 50 caractères")
    @Column(nullable = false, unique = true, length = 50)
    private String numeroCommande;

    @NotNull(message = "La date de commande est obligatoire")
    @Column(nullable = false)
    private LocalDate dateCommande;

    @Column
    private LocalDate dateReception;

    @NotNull(message = "Le fournisseur est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_commande_fournisseur"))
    private Fournisseur fournisseur;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LigneCommande> lignesCommande = new ArrayList<>();

    @NotNull
    @DecimalMin(value = "0.0", message = "Le montant total ne peut pas être négatif")
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Calcule le montant total de la commande
     */
    public void calculerMontantTotal() {
        this.montantTotal = lignesCommande.stream()
                .map(LigneCommande::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Ajoute une ligne de commande
     */
    public void ajouterLigneCommande(LigneCommande ligne) {
        lignesCommande.add(ligne);
        ligne.setCommande(this);
        calculerMontantTotal();
    }

    /**
     * Retire une ligne de commande
     */
    public void retirerLigneCommande(LigneCommande ligne) {
        lignesCommande.remove(ligne);
        ligne.setCommande(null);
        calculerMontantTotal();
    }

    /**
     * Vérifie si la commande peut être modifiée
     */
    public boolean estModifiable() {
        return statut == StatutCommande.EN_ATTENTE || statut == StatutCommande.VALIDEE;
    }

    /**
     * Vérifie si la commande peut être réceptionnée
     */
    public boolean peutEtreReceptionnee() {
        return statut == StatutCommande.VALIDEE;
    }
}