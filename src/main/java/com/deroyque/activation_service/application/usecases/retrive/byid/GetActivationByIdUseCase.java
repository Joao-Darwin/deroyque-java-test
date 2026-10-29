package com.deroyque.activation_service.application.usecases.retrive.byid;

import com.deroyque.activation_service.exception.exceptions.ActivationNotFoundException;
import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.repositories.ActivationRegistryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetActivationByIdUseCase {

    private final ActivationRegistryRepository activationRegistryRepository;

    @Autowired
    public GetActivationByIdUseCase(ActivationRegistryRepository activationRegistryRepository) {
        this.activationRegistryRepository = activationRegistryRepository;
    }

    public GetActivationByIdResponseDTO findById(UUID id) {
        ActivationRegistry activationRegistry = activationRegistryRepository.findById(id)
                .orElseThrow(() -> new ActivationNotFoundException(
                        "Solicitação de ativação não encontrada."
                ));

        return new GetActivationByIdResponseDTO(
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
