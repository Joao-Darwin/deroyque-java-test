package com.deroyque.activation_service.application.usecases.retrive.byid;

import com.deroyque.activation_service.models.enums.RegistryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetActivationByIdResponseDTO(
        UUID id,
        String externalId,
        String customerId,
        String productCode,
        RegistryStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
