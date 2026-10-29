package com.deroyque.activation_service.models.specifications;

import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.enums.RegistryStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ActivationRegistrySpecification {

    private ActivationRegistrySpecification() {
    }

    public static Specification<ActivationRegistry> filterBy(RegistryStatus status, String customerId) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("status"), status)
                );
            }

            if (customerId != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("customerId"), customerId)
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
