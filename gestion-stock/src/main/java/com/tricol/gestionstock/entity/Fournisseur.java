package com.tricol.gestionstock.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fournisseurs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La raison sociale est obligatoire")
    @Size(max = 200, message = "La raison sociale ne doit pas dépasser 200 caractères")
    @Column(nullable = false, length = 200)
    private String raisonSociale;

    @NotBlank(message = "L'adresse est obligatoire")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String adresse;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
    @Column(nullable = false, length = 100)
    private String ville;

    @NotBlank(message = "Le nom de la personne de contact est obligatoire")
    @Size(max = 150, message = "Le nom de la personne de contact ne doit pas dépasser 150 caractères")
    @Column(nullable = false, length = 150)
    private String personneContact;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Format de téléphone invalide")
    @Column(nullable = false, length = 20)
    private String telephone;

    @NotBlank(message = "L'ICE est obligatoire")
    @Size(min = 15, max = 15, message = "L'ICE doit contenir exactement 15 caractères")
    @Column(nullable = false, unique = true, length = 15)
    private String ice;

    @OneToMany(mappedBy = "fournisseur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CommandeFournisseur> commandes = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}