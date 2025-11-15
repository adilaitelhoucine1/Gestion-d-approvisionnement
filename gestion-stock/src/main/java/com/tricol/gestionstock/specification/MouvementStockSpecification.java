package com.tricol.gestionstock.specification;

import com.tricol.gestionstock.dto.stock.MouvementStockSearchCriteria;
import com.tricol.gestionstock.entity.MouvementStock;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class MouvementStockSpecification {


    public static Specification<MouvementStock> withCriteria(MouvementStockSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by product ID
            if (criteria.getProduitId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("produit").get("id"), criteria.getProduitId()));
            }

            // Filter by product reference
            if (criteria.getReference() != null && !criteria.getReference().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("produit").get("reference"), criteria.getReference()));
            }

            // Filter by movement type (ENTREE or SORTIE)
            if (criteria.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("typeMouvement"), criteria.getType()));
            }

            // Filter by lot number
            if (criteria.getNumeroLot() != null && !criteria.getNumeroLot().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("lot").get("numeroLot"), criteria.getNumeroLot()));
            }

            // Filter by date range - start date
            if (criteria.getDateDebut() != null) {
                LocalDateTime startOfDay = criteria.getDateDebut().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateMouvement"), startOfDay));
            }

            // Filter by date range - end date
            if (criteria.getDateFin() != null) {
                LocalDateTime endOfDay = criteria.getDateFin().atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateMouvement"), endOfDay));
            }

            // Sort by date in descending order
            query.orderBy(criteriaBuilder.desc(root.get("dateMouvement")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

