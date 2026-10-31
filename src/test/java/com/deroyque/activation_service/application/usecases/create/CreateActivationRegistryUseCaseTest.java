package com.deroyque.activation_service.application.usecases.create;

import com.deroyque.activation_service.exception.exceptions.ActivationConflictException;
import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.enums.RegistryStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateActivationRegistryUseCaseTest {

    @Mock
    private ActivationRegistryRepository activationRegistryRepository;

    @InjectMocks
    private CreateActivationRegistryUseCase createActivationRegistryUseCase;

    private CreateActivationRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new CreateActivationRequestDTO(
                "external-123",
                "customer-123",
                "product-123"
        );
    }

    @Test
    void shouldCreateActivationRegistrySuccessfully() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        when(activationRegistryRepository.findByExternalId(request.externalId()))
                .thenReturn(Optional.empty());

        when(activationRegistryRepository.save(any(ActivationRegistry.class)))
                .thenAnswer(invocation -> {
                    ActivationRegistry registry = invocation.getArgument(0);

                    registry.setId(id);
                    registry.setCreatedAt(now);
                    registry.setUpdatedAt(now);

                    return registry;
                });

        CreateActivationResponseDTO response =
                createActivationRegistryUseCase.create(request);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals(request.externalId(), response.externalId());
        assertEquals(request.customerId(), response.customerId());
        assertEquals(request.productCode(), response.productCode());
        assertEquals(RegistryStatus.PENDING, response.status());
        assertEquals(now, response.createdAt());
        assertEquals(now, response.updatedAt());

        verify(activationRegistryRepository).findByExternalId(request.externalId());
        verify(activationRegistryRepository).save(any(ActivationRegistry.class));
    }

    @Test
    void shouldReturnExistingActivationWhenRequestIsIdentical() {
        ActivationRegistry existingRegistry = createExistingRegistry();

        when(activationRegistryRepository.findByExternalId(request.externalId()))
                .thenReturn(Optional.of(existingRegistry));

        CreateActivationResponseDTO response =
                createActivationRegistryUseCase.create(request);

        assertNotNull(response);
        assertEquals(existingRegistry.getId(), response.id());
        assertEquals(existingRegistry.getExternalId(), response.externalId());
        assertEquals(existingRegistry.getCustomerId(), response.customerId());
        assertEquals(existingRegistry.getProductCode(), response.productCode());
        assertEquals(existingRegistry.getStatus(), response.status());

        verify(activationRegistryRepository).findByExternalId(request.externalId());
        verify(activationRegistryRepository, never()).save(any(ActivationRegistry.class));
    }

    @Test
    void shouldThrowConflictExceptionWhenExternalIdHasDifferentRequestData() {
        ActivationRegistry existingRegistry = createExistingRegistry();
        existingRegistry.setProductCode("different-product");

        when(activationRegistryRepository.findByExternalId(request.externalId()))
                .thenReturn(Optional.of(existingRegistry));

        assertThrows(
                ActivationConflictException.class,
                () -> createActivationRegistryUseCase.create(request)
        );

        verify(activationRegistryRepository).findByExternalId(request.externalId());
        verify(activationRegistryRepository, never()).save(any(ActivationRegistry.class));
    }

    @Test
    void shouldNotSaveWhenExistingRequestHasSameExternalId() {

        ActivationRegistry existingRegistry = createExistingRegistry();

        when(activationRegistryRepository.findByExternalId(request.externalId()))
                .thenReturn(Optional.of(existingRegistry));

        createActivationRegistryUseCase.create(request);

        verify(activationRegistryRepository, never()).save(any(ActivationRegistry.class));
    }

    private ActivationRegistry createExistingRegistry() {
        ActivationRegistry registry = new ActivationRegistry();

        registry.setId(UUID.randomUUID());
        registry.setExternalId(request.externalId());
        registry.setCustomerId(request.customerId());
        registry.setProductCode(request.productCode());
        registry.setCreatedAt(LocalDateTime.now());
        registry.setUpdatedAt(LocalDateTime.now());

        return registry;
    }
}