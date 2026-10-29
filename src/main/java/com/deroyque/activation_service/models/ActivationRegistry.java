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
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public ActivationRegistry() {}

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
}
