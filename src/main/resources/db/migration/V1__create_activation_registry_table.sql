CREATE TABLE activation_registry (
    id UUID PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL,
    customer_id VARCHAR(255) NOT NULL,
    product_code VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT,

    CONSTRAINT uk_activation_registry_external_id
     UNIQUE (external_id)
);

CREATE INDEX idx_activation_registry_customer_id
    ON activation_registry (customer_id);

CREATE INDEX idx_activation_registry_status
    ON activation_registry (status);