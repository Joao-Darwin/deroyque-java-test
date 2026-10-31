package com.deroyque.activation_service.application.services.impl;

import com.deroyque.activation_service.application.services.IActivationProviderClient;
import com.deroyque.activation_service.exception.exceptions.ProviderPermanentException;
import com.deroyque.activation_service.exception.exceptions.ProviderTemporaryException;
import com.deroyque.activation_service.models.ActivationRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class ActivationProviderClient implements IActivationProviderClient {

    private final RestClient restClient;

    public ActivationProviderClient(RestClient activationProviderRestClient) {
        this.restClient = activationProviderRestClient;
    }

    @Override
    public void activate(ActivationRegistry activation) {
        try {
            restClient.post()
                    .uri("/provider/v1/{id}/activate", activation.getId())
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException exception) {
            throw new ProviderPermanentException(
                    "Provider rejected activation: HTTP " +
                            exception.getStatusCode().value()
            );

        } catch (HttpServerErrorException | ResourceAccessException exception) {
            throw new ProviderTemporaryException(
                    "Provider temporarily unavailable."
            );
        }
    }
}
