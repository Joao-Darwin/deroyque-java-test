package com.deroyque.activation_service.application.usecases.retrive.paged;

import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.enums.RegistryStatus;
import com.deroyque.activation_service.models.repositories.ActivationRegistryRepository;
import com.deroyque.activation_service.models.specifications.ActivationRegistrySpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PagedActivationUseCase {

    private final ActivationRegistryRepository activationRegistryRepository;

    @Autowired
    public PagedActivationUseCase(ActivationRegistryRepository activationRegistryRepository) {
        this.activationRegistryRepository = activationRegistryRepository;
    }

    public Page<PagedActivationResponseDTO> findAll(Pageable pageable, RegistryStatus status, String customerId) {
        Specification<ActivationRegistry> specification = ActivationRegistrySpecification.filterBy(status, customerId);

        return activationRegistryRepository.findAll(specification, pageable)
                .map(this::toResponse);
    }

    private PagedActivationResponseDTO toResponse(ActivationRegistry activationRegistry) {
        return new PagedActivationResponseDTO(
                activationRegistry.getId(),
                activationRegistry.getExternalId(),
                activationRegistry.getCustomerId(),
                activationRegistry.getProductCode(),
                activationRegistry.getStatus(),
                activationRegistry.getCreatedAt(),
                activationRegistry.getUpdatedAt()
        );
    }
}
