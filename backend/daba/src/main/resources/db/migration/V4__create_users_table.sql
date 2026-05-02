-- =============================================================================
-- V4__create_users_table.sql
-- Identity module: users (must match ma.daba.identity.User JPA mapping).
-- =============================================================================

CREATE TABLE users (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number            VARCHAR(20)  NOT NULL,
    email                   VARCHAR(255),
    password_hash           VARCHAR(255) NOT NULL,
    role                    VARCHAR(255) NOT NULL DEFAULT 'CLIENT',
    full_name               VARCHAR(100),
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    is_verified             BOOLEAN      NOT NULL DEFAULT FALSE,
    consent_given_at        TIMESTAMP WITH TIME ZONE,
    consent_ip_address      INET,
    deletion_requested_at   TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE,
    updated_at              TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_users_phone_number UNIQUE (phone_number),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE INDEX idx_users_phone_number ON users (phone_number);
CREATE INDEX idx_users_role ON users (role);

CREATE TRIGGER set_updated_at_users
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
