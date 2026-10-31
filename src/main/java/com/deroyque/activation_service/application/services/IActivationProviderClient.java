package com.deroyque.activation_service.application.services;

import com.deroyque.activation_service.models.ActivationRegistry;

public interface IActivationProviderClient {

    void activate(ActivationRegistry activation);
}