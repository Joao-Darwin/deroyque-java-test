package com.deroyque.activation_service.application.usecases.retrive.paged;

import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.enums.RegistryStatus;
import com.deroyque.activation_service.models.repositories.ActivationRegistryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagedActivationUseCaseTest {

    @Mock
    private ActivationRegistryRepository activationRegistryRepository;

    @InjectMocks
    private PagedActivationUseCase pagedActivationUseCase;

    private Pageable pageable;

    private ActivationRegistry firstRegistry;
    private ActivationRegistry secondRegistry;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        firstRegistry = createActivationRegistry(
                "external-123",
                "customer-123",
                "product-123"
        );

        secondRegistry = createActivationRegistry(
                "external-456",
                "customer-456",
                "product-456"
        );
    }

    @Test
    void shouldReturnPagedActivationsSuccessfully() {
        List<ActivationRegistry> registries = List.of(
                firstRegistry,
                secondRegistry
        );

        Page<ActivationRegistry> registryPage = new PageImpl<>(
                registries,
                pageable,
                registries.size()
        );

        when(activationRegistryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(registryPage);

        Page<PagedActivationResponseDTO> response =
                pagedActivationUseCase.findAll(pageable, null, null);

        assertNotNull(response);
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(2, response.getContent().size());

        PagedActivationResponseDTO firstResponse = response.getContent().getFirst();

        assertEquals(firstRegistry.getId(), firstResponse.id());
        assertEquals(firstRegistry.getExternalId(), firstResponse.externalId());
        assertEquals(firstRegistry.getCustomerId(), firstResponse.customerId());
        assertEquals(firstRegistry.getProductCode(), firstResponse.productCode());
        assertEquals(firstRegistry.getStatus(), firstResponse.status());

        verify(activationRegistryRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void shouldReturnEmptyPageWhenNoActivationsAreFound() {
        Page<ActivationRegistry> emptyPage = Page.empty(pageable);

        when(activationRegistryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(emptyPage);

        Page<PagedActivationResponseDTO> response =
                pagedActivationUseCase.findAll(pageable, null, null);

        assertNotNull(response);
        assertTrue(response.isEmpty());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());

        verify(activationRegistryRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void shouldApplyStatusAndCustomerIdFilters() {
        RegistryStatus status = RegistryStatus.PENDING;
        String customerId = "customer-123";

        Page<ActivationRegistry> registryPage = new PageImpl<>(
                List.of(firstRegistry),
                pageable,
                1
        );

        when(activationRegistryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(registryPage);

        Page<PagedActivationResponseDTO> response =
                pagedActivationUseCase.findAll(pageable, status, customerId);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(customerId, response.getContent().getFirst().customerId());
        assertEquals(status, response.getContent().getFirst().status());

        verify(activationRegistryRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void shouldPreservePaginationInformation() {
        pageable = PageRequest.of(1, 5);

        Page<ActivationRegistry> registryPage = new PageImpl<>(
                List.of(firstRegistry),
                pageable,
                6
        );

        when(activationRegistryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(registryPage);

        Page<PagedActivationResponseDTO> response =
                pagedActivationUseCase.findAll(pageable, null, null);

        assertEquals(1, response.getNumber());
        assertEquals(5, response.getSize());
        assertEquals(6, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertTrue(response.hasPrevious());
        assertFalse(response.hasNext());
    }

    private ActivationRegistry createActivationRegistry(
            String externalId,
            String customerId,
            String productCode
    ) {
        ActivationRegistry registry = new ActivationRegistry();

        registry.setId(UUID.randomUUID());
        registry.setExternalId(externalId);
        registry.setCustomerId(customerId);
        registry.setProductCode(productCode);
        registry.setCreatedAt(LocalDateTime.now());
        registry.setUpdatedAt(LocalDateTime.now());

        return registry;
    }
}