package com.deroyque.activation_service.application.usecases.update.claim;

import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.enums.RegistryStatus;
import com.deroyque.activation_service.models.repositories.ActivationRegistryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClaimActivationRegistryUseCase {

    private static final int MAX_ATTEMPTS = 3;

    private final ActivationRegistryRepository registryRepository;

    @Autowired
    public ClaimActivationRegistryUseCase(ActivationRegistryRepository registryRepository) {
        this.registryRepository = registryRepository;
    }

    @Transactional
    public Optional<ActivationRegistry> claim(UUID id) {
        LocalDateTime now = LocalDateTime.now();

        int updatedRows = registryRepository.claimForProcessing(
                id,
                RegistryStatus.PENDING,
                RegistryStatus.PROCESSING,
                MAX_ATTEMPTS,
                now
        );

        if (updatedRows == 0) {
            return Optional.empty();
        }

        return registryRepository.findById(id);
    }
}
