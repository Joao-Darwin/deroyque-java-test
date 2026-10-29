package com.deroyque.activation_service.application.usecases.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateActivationRequestDTO(

        @NotBlank(message = "External ID is required")
        @Size(max = 100, message = "External ID must have at most 100 characters")
        String externalId,

        @NotBlank(message = "Customer ID is required")
        @Size(max = 100, message = "Customer ID must have at most 100 characters")
        String customerId,

        @NotBlank(message = "Product code is required")
        @Size(max = 100, message = "Product code must have at most 100 characters")
        String productCode
) {}
