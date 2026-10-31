package com.deroyque.activation_service.application.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ActivationProviderConfig {

    @Bean
    public RestClient activationProviderRestClient(
            @Value("${PROVIDER_BASE_URL}") String baseUrl
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}