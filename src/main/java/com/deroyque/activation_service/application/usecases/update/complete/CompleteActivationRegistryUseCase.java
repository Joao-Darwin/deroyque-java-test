package com.deroyque.activation_service.application.usecases.update.complete;

import com.deroyque.activation_service.exception.exceptions.ActivationNotFoundException;
import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.repositories.ActivationRegistryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CompleteActivationRegistryUseCase {

    private final ActivationRegistryRepository repository;

    @Autowired
    public CompleteActivationRegistryUseCase(ActivationRegistryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void complete(UUID id) {
        ActivationRegistry activation = repository.findById(id)
                .orElseThrow(() -> new ActivationNotFoundException("registro de ativação não encontrado."));

        activation.complete();
    }
}
