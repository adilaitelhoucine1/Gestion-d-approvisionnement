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


            if (criteria.getProduitId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("produit").get("id"), criteria.getProduitId()));
            }


            if (criteria.getReference() != null && !criteria.getReference().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("produit").get("reference"), criteria.getReference()));
            }


            if (criteria.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("typeMouvement"), criteria.getType()));
            }


            if (criteria.getNumeroLot() != null && !criteria.getNumeroLot().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("lot").get("numeroLot"), criteria.getNumeroLot()));
            }


            if (criteria.getDateDebut() != null) {
                LocalDateTime startOfDay = criteria.getDateDebut().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateMouvement"), startOfDay));
            }


            if (criteria.getDateFin() != null) {
                LocalDateTime endOfDay = criteria.getDateFin().atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateMouvement"), endOfDay));
            }


            query.orderBy(criteriaBuilder.desc(root.get("dateMouvement")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

