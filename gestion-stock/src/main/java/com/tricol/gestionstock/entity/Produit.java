package com.tricol.gestionstock.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produits", indexes = {
        @Index(name = "idx_produit_reference", columnList = "reference"),
        @Index(name = "idx_produit_categorie", columnList = "categorie")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La référence produit est obligatoire")
    @Size(max = 50, message = "La référence ne doit pas dépasser 50 caractères")
    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
    @Column(nullable = false, length = 200)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 100, message = "La catégorie ne doit pas dépasser 100 caractères")
    @Column(nullable = false, length = 100)
    private String categorie;

    @NotNull(message = "Le stock actuel est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    @Column(nullable = false)
    @Builder.Default
    private Integer stockActuel = 0;

    @NotNull(message = "Le point de commande est obligatoire")
    @Min(value = 0, message = "Le point de commande ne peut pas être négatif")
    @Column(nullable = false)
    private Integer pointDeCommande;

    @NotBlank(message = "L'unité de mesure est obligatoire")
    @Size(max = 20, message = "L'unité de mesure ne doit pas dépasser 20 caractères")
    @Column(nullable = false, length = 20)
    private String uniteMesure;

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LigneCommande> lignesCommande = new ArrayList<>();

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LotStock> lots = new ArrayList<>();

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MouvementStock> mouvements = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;

    @UpdateTimestamp
        @Column(nullable = false)
        private LocalDateTime updatedAt;

   /*
    public boolean isEnAlerte() {
        return stockActuel < pointDeCommande;
    }


    public void incrementerStock(Integer quantite) {
        this.stockActuel += quantite;
    }


    public void decrementerStock(Integer quantite) {
        if (this.stockActuel < quantite) {
            throw new IllegalArgumentException(
                    "Stock insuffisant pour le produit " + this.nom +
                            ". Disponible: " + this.stockActuel + ", Demandé: " + quantite
            );
        }
        this.stockActuel -= quantite;
    } */
}