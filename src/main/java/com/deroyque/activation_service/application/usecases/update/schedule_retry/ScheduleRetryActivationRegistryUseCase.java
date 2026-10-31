package com.deroyque.activation_service.application.usecases.update.schedule_retry;

import com.deroyque.activation_service.exception.exceptions.ActivationNotFoundException;
import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.repositories.ActivationRegistryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ScheduleRetryActivationRegistryUseCase {

    private final ActivationRegistryRepository activationRegistryRepository;

    @Autowired
    public ScheduleRetryActivationRegistryUseCase(ActivationRegistryRepository activationRegistryRepository) {
        this.activationRegistryRepository = activationRegistryRepository;
    }

    @Transactional
    public void scheduleRetry(
            UUID id,
            String reason,
            LocalDateTime nextAttemptAt
    ) {
        ActivationRegistry activation = activationRegistryRepository.findById(id)
                .orElseThrow(() -> new ActivationNotFoundException("registro de ativação não encontrado"));

        activation.scheduleRetry(reason, nextAttemptAt);
    }
}
