package com.tricol.gestionstock.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "lignes_bon_sortie", indexes = {
        @Index(name = "idx_ligne_bon", columnList = "bon_sortie_id"),
        @Index(name = "idx_ligne_bon_produit", columnList = "produit_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneBonSortie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le bon de sortie est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_sortie_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ligne_bon_sortie"))
    private BonSortie bonSortie;

    @NotNull(message = "Le produit est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ligne_bon_produit"))
    private Produit produit;

    @NotNull(message = "La quantite demandee est obligatoire")
    @Min(value = 1, message = "La quantite demandee doit être au moins 1")
    @Column(nullable = false)
    private Integer quantiteDemandee;
}