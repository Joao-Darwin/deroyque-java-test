package com.deroyque.activation_service.application.usecases.retrive.byid;

import com.deroyque.activation_service.exception.exceptions.ActivationNotFoundException;
import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.repositories.ActivationRegistryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetActivationByIdUseCaseTest {

    @Mock
    private ActivationRegistryRepository activationRegistryRepository;

    @InjectMocks
    private GetActivationByIdUseCase getActivationByIdUseCase;

    private UUID id;
    private ActivationRegistry activationRegistry;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();

        activationRegistry = new ActivationRegistry();
        activationRegistry.setId(id);
        activationRegistry.setExternalId("external-123");
        activationRegistry.setCustomerId("customer-123");
        activationRegistry.setProductCode("product-123");
        activationRegistry.setCreatedAt(LocalDateTime.now());
        activationRegistry.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void shouldReturnActivationRegistryWhenIdExists() {
        when(activationRegistryRepository.findById(id))
                .thenReturn(Optional.of(activationRegistry));

        GetActivationByIdResponseDTO response =
                getActivationByIdUseCase.findById(id);

        assertNotNull(response);
        assertEquals(activationRegistry.getId(), response.id());
        assertEquals(activationRegistry.getExternalId(), response.externalId());
        assertEquals(activationRegistry.getCustomerId(), response.customerId());
        assertEquals(activationRegistry.getProductCode(), response.productCode());
        assertEquals(activationRegistry.getStatus(), response.status());
        assertEquals(activationRegistry.getCreatedAt(), response.createdAt());
        assertEquals(activationRegistry.getUpdatedAt(), response.updatedAt());

        verify(activationRegistryRepository, times(1)).findById(id);
    }

    @Test
    void shouldThrowActivationNotFoundExceptionWhenIdDoesNotExist() {
        when(activationRegistryRepository.findById(id))
                .thenReturn(Optional.empty());

        ActivationNotFoundException exception = assertThrows(
                ActivationNotFoundException.class,
                () -> getActivationByIdUseCase.findById(id)
        );

        assertEquals(
                "Solicitação de ativação não encontrada.",
                exception.getMessage()
        );

        verify(activationRegistryRepository, times(1)).findById(id);
    }
}