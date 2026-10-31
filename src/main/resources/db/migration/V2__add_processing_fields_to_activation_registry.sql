ALTER TABLE activation_registry
    ADD COLUMN attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN last_error VARCHAR(1000),
    ADD COLUMN next_attempt_at TIMESTAMP,
    ADD COLUMN last_attempt_at TIMESTAMP,
    ADD COLUMN processing_started_at TIMESTAMP;

CREATE INDEX idx_activation_registry_next_attempt_at
    ON activation_registry (next_attempt_at);