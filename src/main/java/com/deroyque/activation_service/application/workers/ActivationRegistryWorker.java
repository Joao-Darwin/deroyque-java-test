package com.deroyque.activation_service.application.workers;

import com.deroyque.activation_service.application.services.IActivationProviderClient;
import com.deroyque.activation_service.application.usecases.update.claim.ClaimActivationRegistryUseCase;
import com.deroyque.activation_service.application.usecases.update.complete.CompleteActivationRegistryUseCase;
import com.deroyque.activation_service.application.usecases.update.fail.FailActivationRegistryUseCase;
import com.deroyque.activation_service.application.usecases.update.schedule_retry.ScheduleRetryActivationRegistryUseCase;
import com.deroyque.activation_service.exception.exceptions.ProviderPermanentException;
import com.deroyque.activation_service.exception.exceptions.ProviderTemporaryException;
import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.enums.RegistryStatus;
import com.deroyque.activation_service.models.repositories.ActivationRegistryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ActivationRegistryWorker {

    private static final Logger log =
            LoggerFactory.getLogger(ActivationRegistryWorker.class);

    private final ScheduleRetryActivationRegistryUseCase scheduleRetry;
    private final FailActivationRegistryUseCase failActivationRegistryUseCase;
    private final ClaimActivationRegistryUseCase claimActivationRegistryUseCase;
    private final CompleteActivationRegistryUseCase completeActivationRegistryUseCase;
    private final ActivationRegistryRepository repository;
    private final IActivationProviderClient providerClient;
    private final RetryPolicy retryPolicy;

    public ActivationRegistryWorker(
            ScheduleRetryActivationRegistryUseCase scheduleRetry,
            FailActivationRegistryUseCase failActivationRegistryUseCase,
            ClaimActivationRegistryUseCase claimActivationRegistryUseCase,
            CompleteActivationRegistryUseCase completeActivationRegistryUseCase,
            ActivationRegistryRepository repository,
            IActivationProviderClient providerClient,
            RetryPolicy retryPolicy
    ) {
        this.scheduleRetry = scheduleRetry;
        this.failActivationRegistryUseCase = failActivationRegistryUseCase;
        this.claimActivationRegistryUseCase = claimActivationRegistryUseCase;
        this.completeActivationRegistryUseCase = completeActivationRegistryUseCase;
        this.repository = repository;
        this.providerClient = providerClient;
        this.retryPolicy = retryPolicy;
    }

    @Scheduled(fixedDelayString = "${activation.worker.fixed-delay:5000}")
    public void processPendingRequests() {
        List<ActivationRegistry> pendingRequests =
                repository.findEligibleForProcessing(
                        RegistryStatus.PENDING,
                        LocalDateTime.now()
                );

        for (ActivationRegistry request : pendingRequests) {
            process(request);
        }
    }

    private void process(ActivationRegistry request) {
        var claimed = claimActivationRegistryUseCase.claim(request.getId());

        if (claimed.isEmpty()) {
            return;
        }

        ActivationRegistry activation = claimed.get();

        try {
            providerClient.activate(activation);

            completeActivationRegistryUseCase.complete(activation.getId());

            log.info(
                    "Activation request {} completed successfully.",
                    activation.getId()
            );

        } catch (ProviderPermanentException exception) {
            failActivationRegistryUseCase.failPermanently(
                    activation.getId(),
                    exception.getMessage()
            );

            log.warn(
                    "Activation request {} failed permanently.",
                    activation.getId()
            );

        } catch (ProviderTemporaryException exception) {
            LocalDateTime nextAttemptAt =
                    retryPolicy.calculateNextAttempt(activation.getAttempts());

            scheduleRetry.scheduleRetry(
                    activation.getId(),
                    exception.getMessage(),
                    nextAttemptAt
            );

            log.warn(
                    "Temporary failure processing activation request {}.",
                    activation.getId()
            );

        } catch (Exception exception) {
            log.error(
                    "Unexpected error processing activation request {}.",
                    activation.getId(),
                    exception
            );

            LocalDateTime nextAttemptAt =
                    retryPolicy.calculateNextAttempt(activation.getAttempts());

            scheduleRetry.scheduleRetry(
                    activation.getId(),
                    "Unexpected error communicating with provider.",
                    nextAttemptAt
            );
        }
    }
}