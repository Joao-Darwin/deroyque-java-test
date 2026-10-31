package com.deroyque.activation_service.models.repositories;

import com.deroyque.activation_service.AbstractIntegrationTest;
import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.enums.RegistryStatus;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ActivationRegistryRepositoryIT extends AbstractIntegrationTest {

    private static final int MAX_ATTEMPTS = 3;

    @Autowired
    private ActivationRegistryRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldSaveActivationRegistrySuccessfully() {
        ActivationRegistry registry = new ActivationRegistry(
                "external-123",
                "customer-123",
                "product-123"
        );

        ActivationRegistry saved = repository.save(registry);

        assertNotNull(saved.getId());
        assertEquals(RegistryStatus.PENDING, saved.getStatus());
        assertEquals(0, saved.getAttempts());
    }

    @Test
    void shouldFindActivationRegistryByExternalId() {
        ActivationRegistry registry = new ActivationRegistry(
                "external-123",
                "customer-123",
                "product-123"
        );

        repository.save(registry);

        Optional<ActivationRegistry> result =
                repository.findByExternalId("external-123");

        assertTrue(result.isPresent());
        assertEquals("external-123", result.get().getExternalId());
        assertEquals("customer-123", result.get().getCustomerId());
    }

    @Test
    void shouldReturnEmptyWhenExternalIdDoesNotExist() {
        Optional<ActivationRegistry> result =
                repository.findByExternalId("non-existent");

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void shouldClaimActivationRegistryForProcessing() {
        ActivationRegistry registry = new ActivationRegistry(
                "external-123",
                "customer-123",
                "product-123"
        );

        ActivationRegistry saved = repository.saveAndFlush(registry);

        LocalDateTime now = LocalDateTime.now();

        int updatedRows = repository.claimForProcessing(
                saved.getId(),
                RegistryStatus.PENDING,
                RegistryStatus.PROCESSING,
                MAX_ATTEMPTS,
                now
        );

        assertEquals(1, updatedRows);

        ActivationRegistry updated = repository.findById(saved.getId())
                .orElseThrow();

        assertEquals(RegistryStatus.PROCESSING, updated.getStatus());
        assertEquals(1, updated.getAttempts());
        assertNotNull(updated.getLastAttemptAt());
        assertNotNull(updated.getProcessingStartedAt());
        assertNull(updated.getNextAttemptAt());
        assertNull(updated.getLastError());
    }

    @Test
    @Transactional
    void shouldNotClaimActivationRegistryWhenStatusIsNotPending() {
        ActivationRegistry registry = new ActivationRegistry(
                "external-123",
                "customer-123",
                "product-123"
        );

        registry.startProcessing();

        ActivationRegistry saved = repository.saveAndFlush(registry);

        int updatedRows = repository.claimForProcessing(
                saved.getId(),
                RegistryStatus.PENDING,
                RegistryStatus.PROCESSING,
                MAX_ATTEMPTS,
                LocalDateTime.now()
        );

        assertEquals(0, updatedRows);
    }

    @Test
    @Transactional
    void shouldNotClaimActivationRegistryWhenMaximumAttemptsReached() {
        ActivationRegistry registry = new ActivationRegistry(
                "external-123",
                "customer-123",
                "product-123"
        );

        // Simula um registro que já atingiu o limite de tentativas.
        // O uso de reflexão evita adicionar um setter apenas para testes.
        setAttempts(registry, MAX_ATTEMPTS);

        ActivationRegistry saved = repository.saveAndFlush(registry);

        int updatedRows = repository.claimForProcessing(
                saved.getId(),
                RegistryStatus.PENDING,
                RegistryStatus.PROCESSING,
                MAX_ATTEMPTS,
                LocalDateTime.now()
        );

        assertEquals(0, updatedRows);
    }

    @Test
    @Transactional
    void shouldNotClaimActivationRegistryBeforeNextAttemptAt() {
        ActivationRegistry registry = new ActivationRegistry(
                "external-123",
                "customer-123",
                "product-123"
        );

        LocalDateTime future = LocalDateTime.now().plusMinutes(5);

        // Configura nextAttemptAt por reflexão.
        setNextAttemptAt(registry, future);

        ActivationRegistry saved = repository.saveAndFlush(registry);

        int updatedRows = repository.claimForProcessing(
                saved.getId(),
                RegistryStatus.PENDING,
                RegistryStatus.PROCESSING,
                MAX_ATTEMPTS,
                LocalDateTime.now()
        );

        assertEquals(0, updatedRows);
    }

    @Test
    void shouldFindEligibleActivationRegistriesForProcessing() {
        LocalDateTime now = LocalDateTime.now();

        ActivationRegistry first = new ActivationRegistry(
                "external-001",
                "customer-001",
                "product-001"
        );

        ActivationRegistry second = new ActivationRegistry(
                "external-002",
                "customer-002",
                "product-002"
        );

        ActivationRegistry processing = new ActivationRegistry(
                "external-003",
                "customer-003",
                "product-003"
        );

        processing.startProcessing();

        repository.saveAllAndFlush(List.of(first, second, processing));

        List<ActivationRegistry> result =
                repository.findEligibleForProcessing(
                        RegistryStatus.PENDING,
                        now
                );

        assertEquals(2, result.size());

        assertTrue(result.stream().allMatch(
                activation -> activation.getStatus() == RegistryStatus.PENDING
        ));
    }

    @Test
    void shouldNotFindActivationRegistriesWithFutureNextAttemptAt() {
        ActivationRegistry registry = new ActivationRegistry(
                "external-123",
                "customer-123",
                "product-123"
        );

        setNextAttemptAt(registry, LocalDateTime.now().plusMinutes(5));

        repository.saveAndFlush(registry);

        List<ActivationRegistry> result =
                repository.findEligibleForProcessing(
                        RegistryStatus.PENDING,
                        LocalDateTime.now()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEligibleRegistriesOrderedByCreationDate() throws InterruptedException {
        ActivationRegistry first = new ActivationRegistry(
                "external-001",
                "customer-001",
                "product-001"
        );

        repository.saveAndFlush(first);

        Thread.sleep(20);

        ActivationRegistry second = new ActivationRegistry(
                "external-002",
                "customer-002",
                "product-002"
        );

        repository.saveAndFlush(second);

        List<ActivationRegistry> result =
                repository.findEligibleForProcessing(
                        RegistryStatus.PENDING,
                        LocalDateTime.now()
                );

        assertEquals(2, result.size());
        assertEquals("external-001", result.get(0).getExternalId());
        assertEquals("external-002", result.get(1).getExternalId());
    }

    private void setAttempts(ActivationRegistry registry, int attempts) {
        try {
            var field = ActivationRegistry.class.getDeclaredField("attempts");
            field.setAccessible(true);
            field.set(registry, attempts);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void setNextAttemptAt(
            ActivationRegistry registry,
            LocalDateTime nextAttemptAt
    ) {
        try {
            var field = ActivationRegistry.class.getDeclaredField("nextAttemptAt");
            field.setAccessible(true);
            field.set(registry, nextAttemptAt);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}