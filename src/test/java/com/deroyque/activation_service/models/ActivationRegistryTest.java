package com.deroyque.activation_service.models;

import com.deroyque.activation_service.models.enums.RegistryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActivationRegistryTest {

    private ActivationRegistry activationRegistry;

    @BeforeEach
    void setUp() {
        activationRegistry = new ActivationRegistry(
                "external-123",
                "customer-123",
                "product-123"
        );
    }

    @Test
    void shouldCreateActivationRegistryWithPendingStatus() {
        assertEquals("external-123", activationRegistry.getExternalId());
        assertEquals("customer-123", activationRegistry.getCustomerId());
        assertEquals("product-123", activationRegistry.getProductCode());
        assertEquals(RegistryStatus.PENDING, activationRegistry.getStatus());
        assertEquals(0, activationRegistry.getAttempts());
    }

    @Test
    void shouldStartProcessingSuccessfully() {
        activationRegistry.startProcessing();

        assertEquals(RegistryStatus.PROCESSING, activationRegistry.getStatus());
        assertEquals(1, activationRegistry.getAttempts());
        assertNotNull(activationRegistry.getLastAttemptAt());
        assertNotNull(activationRegistry.getProcessingStartedAt());
        assertNull(activationRegistry.getNextAttemptAt());
        assertNull(activationRegistry.getLastError());
    }

    @Test
    void shouldCompleteProcessingSuccessfully() {
        activationRegistry.startProcessing();

        activationRegistry.complete();

        assertEquals(RegistryStatus.COMPLETED, activationRegistry.getStatus());
        assertNull(activationRegistry.getProcessingStartedAt());
        assertNull(activationRegistry.getNextAttemptAt());
        assertNull(activationRegistry.getLastError());
    }

    @Test
    void shouldFailPermanentlySuccessfully() {
        activationRegistry.startProcessing();

        String reason = "Invalid product.";

        activationRegistry.failPermanently(reason);

        assertEquals(RegistryStatus.FAILED, activationRegistry.getStatus());
        assertEquals(reason, activationRegistry.getLastError());
        assertNull(activationRegistry.getProcessingStartedAt());
        assertNull(activationRegistry.getNextAttemptAt());
    }

    @Test
    void shouldScheduleRetrySuccessfully() {
        activationRegistry.startProcessing();

        String reason = "Provider temporarily unavailable.";
        LocalDateTime nextAttemptAt = LocalDateTime.now().plusSeconds(30);

        activationRegistry.scheduleRetry(reason, nextAttemptAt);

        assertEquals(RegistryStatus.PENDING, activationRegistry.getStatus());
        assertEquals(1, activationRegistry.getAttempts());
        assertEquals(reason, activationRegistry.getLastError());
        assertEquals(nextAttemptAt, activationRegistry.getNextAttemptAt());
        assertNull(activationRegistry.getProcessingStartedAt());
    }

    @Test
    void shouldFailPermanentlyWhenMaximumAttemptsAreReached() {
        activationRegistry.startProcessing();
        activationRegistry.scheduleRetry(
                "First temporary failure.",
                LocalDateTime.now().plusSeconds(30)
        );

        activationRegistry.startProcessing();
        activationRegistry.scheduleRetry(
                "Second temporary failure.",
                LocalDateTime.now().plusSeconds(60)
        );

        activationRegistry.startProcessing();

        String reason = "Maximum attempts reached.";

        activationRegistry.scheduleRetry(
                reason,
                LocalDateTime.now().plusSeconds(120)
        );

        assertEquals(RegistryStatus.FAILED, activationRegistry.getStatus());
        assertEquals(3, activationRegistry.getAttempts());
        assertEquals(reason, activationRegistry.getLastError());
        assertNull(activationRegistry.getProcessingStartedAt());
        assertNull(activationRegistry.getNextAttemptAt());
    }

    @Test
    void shouldNotStartProcessingWhenStatusIsNotPending() {
        activationRegistry.startProcessing();

        assertThrows(
                IllegalStateException.class,
                () -> activationRegistry.startProcessing()
        );

        assertEquals(1, activationRegistry.getAttempts());
    }

    @Test
    void shouldNotCompleteWhenStatusIsNotProcessing() {
        assertThrows(
                IllegalStateException.class,
                () -> activationRegistry.complete()
        );
    }

    @Test
    void shouldNotFailPermanentlyWhenStatusIsNotProcessing() {
        assertThrows(
                IllegalStateException.class,
                () -> activationRegistry.failPermanently("Some error.")
        );
    }

    @Test
    void shouldNotScheduleRetryWhenStatusIsNotProcessing() {
        assertThrows(
                IllegalStateException.class,
                () -> activationRegistry.scheduleRetry(
                        "Temporary error.",
                        LocalDateTime.now().plusSeconds(30)
                )
        );
    }
}