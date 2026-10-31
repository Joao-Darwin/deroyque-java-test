package com.deroyque.activation_service.application.usecases.update.fail;

import com.deroyque.activation_service.exception.exceptions.ActivationNotFoundException;
import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.repositories.ActivationRegistryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FailActivationRegistryUseCase {

    private final ActivationRegistryRepository activationRegistryRepository;

    @Autowired
    public FailActivationRegistryUseCase(ActivationRegistryRepository activationRegistryRepository) {
        this.activationRegistryRepository = activationRegistryRepository;
    }

    @Transactional
    public void failPermanently(UUID id, String reason) {
        ActivationRegistry activation = activationRegistryRepository.findById(id)
                .orElseThrow(() -> new ActivationNotFoundException("registro de ativação não encontrado."));

        activation.failPermanently(reason);
    }
}
