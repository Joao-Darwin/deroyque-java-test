package com.deroyque.activation_service.models;

import com.deroyque.activation_service.models.enums.RegistryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "activation_registry",
        indexes = {
                @Index(
                        name = "idx_activation_registry_customer_id",
                        columnList = "customer_id"
                ),
                @Index(
                        name = "idx_activation_registry_status",
                        columnList = "status"
                )
        }
)
public class ActivationRegistry {

    private static final int MAX_ATTEMPTS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private RegistryStatus status = RegistryStatus.PENDING;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public ActivationRegistry() {}

    public ActivationRegistry(
            String externalId,
            String customerId,
            String productCode
    ) {
        this.externalId = externalId;
        this.customerId = customerId;
        this.productCode = productCode;
        this.attempts = 0;
    }

    public void startProcessing() {
        validateStatus(RegistryStatus.PENDING);

        if (attempts >= MAX_ATTEMPTS) {
            throw new IllegalStateException(
                    "Maximum number of attempts exceeded."
            );
        }

        this.status = RegistryStatus.PROCESSING;
        this.attempts++;
        this.lastAttemptAt = LocalDateTime.now();
        this.processingStartedAt = LocalDateTime.now();
        this.nextAttemptAt = null;
        this.lastError = null;
    }

    public void complete() {
        validateStatus(RegistryStatus.PROCESSING);

        this.status = RegistryStatus.COMPLETED;
        this.processingStartedAt = null;
        this.nextAttemptAt = null;
        this.lastError = null;
    }

    public void failPermanently(String reason) {
        validateStatus(RegistryStatus.PROCESSING);

        this.status = RegistryStatus.FAILED;
        this.lastError = reason;
        this.processingStartedAt = null;
        this.nextAttemptAt = null;
    }

    public void scheduleRetry(String reason, LocalDateTime nextAttemptAt) {
        validateStatus(RegistryStatus.PROCESSING);

        if (attempts >= MAX_ATTEMPTS) {
            failPermanently(reason);
            return;
        }

        this.status = RegistryStatus.PENDING;
        this.lastError = reason;
        this.nextAttemptAt = nextAttemptAt;
        this.processingStartedAt = null;
    }

    private void validateStatus(RegistryStatus expectedStatus) {
        if (this.status != expectedStatus) {
            throw new IllegalStateException(
                    "Invalid transition from " + this.status +
                            ". Expected status: " + expectedStatus
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public RegistryStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLastError() {
        return lastError;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public LocalDateTime getProcessingStartedAt() {
        return processingStartedAt;
    }
}
