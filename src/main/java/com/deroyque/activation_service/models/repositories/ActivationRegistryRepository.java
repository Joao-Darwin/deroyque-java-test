package com.deroyque.activation_service.models.repositories;

import com.deroyque.activation_service.models.ActivationRegistry;
import com.deroyque.activation_service.models.enums.RegistryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivationRegistryRepository extends JpaRepository<ActivationRegistry, UUID>, JpaSpecificationExecutor<ActivationRegistry> {

    Optional<ActivationRegistry> findByExternalId(String externalId);

    @Modifying
    @Query("""
        UPDATE ActivationRegistry a
        SET a.status = :processing,
           a.attempts = a.attempts + 1,
           a.lastAttemptAt = :now,
           a.processingStartedAt = :now,
           a.nextAttemptAt = null,
           a.lastError = null,
           a.version = a.version + 1
        WHERE a.id = :id
        AND a.status = :pending
        AND a.attempts < :maxAttempts
        AND (a.nextAttemptAt IS NULL OR a.nextAttemptAt <= :now)
    """)
    int claimForProcessing(
            @Param("id") UUID id,
            @Param("pending") RegistryStatus pending,
            @Param("processing") RegistryStatus processing,
            @Param("maxAttempts") int maxAttempts,
            @Param("now") LocalDateTime now
    );

    @Query("""
        SELECT a
        FROM ActivationRegistry a
        WHERE a.status = :status
          AND (a.nextAttemptAt IS NULL OR a.nextAttemptAt <= :now)
        ORDER BY a.createdAt ASC
    """)
    List<ActivationRegistry> findEligibleForProcessing(
            @Param("status") RegistryStatus status,
            @Param("now") LocalDateTime now
    );
}
