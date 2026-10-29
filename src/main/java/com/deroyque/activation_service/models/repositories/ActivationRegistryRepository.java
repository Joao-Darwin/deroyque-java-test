package com.deroyque.activation_service.models.repositories;

import com.deroyque.activation_service.models.ActivationRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivationRegistryRepository extends JpaRepository<ActivationRegistry, UUID>, JpaSpecificationExecutor<ActivationRegistry> {

    Optional<ActivationRegistry> findByExternalId(String externalId);
}
