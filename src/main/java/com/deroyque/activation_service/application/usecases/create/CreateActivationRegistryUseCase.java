package com.deroyque.activation_service.application.usecases.create;

import com.deroyque.activation_service.exception.exceptions.ActivationConflictException;
import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.repositories.ActivationRegistryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class CreateActivationRegistryUseCase {

    private final Logger log = LoggerFactory.getLogger(CreateActivationRegistryUseCase.class.getName());

    private final ActivationRegistryRepository activationRegistryRepository;

    @Autowired
    public CreateActivationRegistryUseCase(ActivationRegistryRepository activationRegistryRepository) {
        this.activationRegistryRepository = activationRegistryRepository;
    }

    public CreateActivationResponseDTO create(CreateActivationRequestDTO request) {
        log.info("iniciando solicitação de ativação do produto {} para cliente {}", request.productCode(), request.customerId());

        Optional<ActivationRegistry> activationRegistryOptional = activationRegistryRepository.findByExternalId(request.externalId());

        if (activationRegistryOptional.isPresent()) {
            ActivationRegistry activationRegistry = activationRegistryOptional.get();

            if (isSameRequest(activationRegistry, request)) {
                log.info("solicitação já realizada, retornando solicitação");
                return toResponse(activationRegistry);
            }

            throw new ActivationConflictException(
                    "O ID externo já está associado com uma solicitação de ativação diferente."
            );
        }

        ActivationRegistry activationRegistry = new ActivationRegistry();

        activationRegistry.setExternalId(request.externalId());
        activationRegistry.setCustomerId(request.customerId());
        activationRegistry.setProductCode(request.productCode());

        ActivationRegistry savedRegistry = activationRegistryRepository.save(activationRegistry);

        log.info("solicitação criada com sucesso ID {}", savedRegistry.getId());
        return toResponse(savedRegistry);
    }

    private boolean isSameRequest(ActivationRegistry activationRegistry, CreateActivationRequestDTO request) {
        Objects.requireNonNull(activationRegistry);
        Objects.requireNonNull(request);

        return activationRegistry.getExternalId().equals(request.externalId()) &&
                activationRegistry.getCustomerId().equals(request.customerId()) &&
                activationRegistry.getProductCode().equals(request.productCode());
    }

    private CreateActivationResponseDTO toResponse(ActivationRegistry activationRegistry) {
        return new CreateActivationResponseDTO(
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
