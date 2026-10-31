package com.deroyque.activation_service.application.services.impl;

import com.deroyque.activation_service.application.services.IActivationProviderClient;
import com.deroyque.activation_service.exception.exceptions.ProviderPermanentException;
import com.deroyque.activation_service.exception.exceptions.ProviderTemporaryException;
import com.deroyque.activation_service.models.ActivationRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ActivationProviderClient implements IActivationProviderClient {

    private final RestClient restClient;

    public ActivationProviderClient(RestClient activationProviderRestClient) {
        this.restClient = activationProviderRestClient;
    }

    @Override
    @Retry(name = "activationProvider")
    public void activate(ActivationRegistry activation) {
        try {
            restClient.post()
                    .uri("/provider/v1/{id}/activate", activation.getId())
                    .retrieve()
                    .toBodilessEntity();

        } catch (ProviderPermanentException | ProviderTemporaryException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new ProviderTemporaryException(
                    "Error communicating with activation provider."
            );
        }
    }
}
