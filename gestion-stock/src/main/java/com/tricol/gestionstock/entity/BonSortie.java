package com.tricol.gestionstock.entity;

import com.tricol.gestionstock.entity.Enums.MotifSortie;
import com.tricol.gestionstock.entity.Enums.StatutBonSortie;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bons_sortie", indexes = {
        @Index(name = "idx_bon_numero", columnList = "numeroBon"),
        @Index(name = "idx_bon_statut", columnList = "statut"),
        @Index(name = "idx_bon_atelier", columnList = "atelierDestinataire")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonSortie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le numero de bon est obligatoire")
    @Size(max = 50, message = "Le numero de bon ne doit pas depasser 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String numeroBon;

    @NotNull(message = "La date de sortie est obligatoire")
    @Column(nullable = false)
    private LocalDate dateSortie;

    @NotBlank(message = "L'atelier destinataire est obligatoire")
    @Size(max = 100, message = "L'atelier destinataire ne doit pas depasser 100 caracteres")
    @Column(nullable = false, length = 100)
    private String atelierDestinataire;

    @NotNull(message = "Le motif est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MotifSortie motif;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutBonSortie statut = StatutBonSortie.BROUILLON;

    @OneToMany(mappedBy = "bonSortie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LigneBonSortie> lignes = new ArrayList<>();

    @OneToMany(mappedBy = "bonSortie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MouvementStock> mouvements = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column
    private LocalDateTime dateValidation;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Ajoute une ligne au bon de sortie
     */
    public void ajouterLigne(LigneBonSortie ligne) {
        lignes.add(ligne);
        ligne.setBonSortie(this);
    }

    /**
     * Retire une ligne du bon de sortie
     */
    public void retirerLigne(LigneBonSortie ligne) {
        lignes.remove(ligne);
        ligne.setBonSortie(null);
    }

    /**
     * Verifie si le bon peut être modifie
     */
    public boolean estModifiable() {
        return statut == StatutBonSortie.BROUILLON;
    }

    /**
     * Verifie si le bon peut être valide
     */
    public boolean peutEtreValide() {
        return statut == StatutBonSortie.BROUILLON && !lignes.isEmpty();
    }

    /**
     * Initialise les valeurs par defaut
     */
    @PrePersist
    protected void onCreate() {
        if (statut == null) {
            statut = StatutBonSortie.BROUILLON;
        }
        if (dateSortie == null) {
            dateSortie = LocalDate.now();
        }
    }
}