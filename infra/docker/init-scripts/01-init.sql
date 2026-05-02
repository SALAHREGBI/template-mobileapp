-- =============================================================================
-- daba.ma — Database Initialization Script
-- Runs once on first container start via docker-entrypoint-initdb.d
-- =============================================================================

-- Enable PostGIS extension (provides spatial types + functions)
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- ENUM TYPES
-- =============================================================================

CREATE TYPE user_role AS ENUM ('CLIENT', 'ARTISAN', 'ADMIN');

CREATE TYPE booking_status AS ENUM (
    'PENDING',
    'ACCEPTED',
    'EN_ROUTE',
    'ARRIVED',
    'IN_PROGRESS',
    'COMPLETED',
    'CANCELLED'
);

-- =============================================================================
-- IDENTITY MODULE — Users table
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone_number    VARCHAR(20)  NOT NULL UNIQUE,
    email           VARCHAR(255) UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            user_role    NOT NULL DEFAULT 'CLIENT',
    full_name       VARCHAR(100),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    is_verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    -- CNDP Law 09-08 compliance fields
    consent_given_at    TIMESTAMP WITH TIME ZONE,
    consent_ip_address  INET,
    deletion_requested_at TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast phone-based login lookup
CREATE INDEX IF NOT EXISTS idx_users_phone_number ON users (phone_number);
CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);

-- =============================================================================
-- CATALOG MODULE — Service Categories table
-- =============================================================================

CREATE TABLE IF NOT EXISTS service_categories (
    id          SERIAL PRIMARY KEY,
    name_ar     VARCHAR(100) NOT NULL, -- Arabic display name
    name_fr     VARCHAR(100) NOT NULL, -- French display name
    slug        VARCHAR(50)  NOT NULL UNIQUE,
    icon_url    VARCHAR(500),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- CATALOG MODULE — Artisan Profiles table
-- =============================================================================

CREATE TABLE IF NOT EXISTS artisan_profiles (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    bio             TEXT,
    trust_score     DECIMAL(4, 2) NOT NULL DEFAULT 5.00, -- 0.00 to 10.00
    review_count    INTEGER NOT NULL DEFAULT 0,
    is_available    BOOLEAN NOT NULL DEFAULT FALSE,
    is_verified     BOOLEAN NOT NULL DEFAULT FALSE,
    id_card_url     VARCHAR(500), -- for CNDP-compliant KYC
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id)
);

CREATE INDEX IF NOT EXISTS idx_artisan_profiles_user_id ON artisan_profiles (user_id);
CREATE INDEX IF NOT EXISTS idx_artisan_profiles_trust_score ON artisan_profiles (trust_score DESC);

-- Many-to-many: artisan ↔ service category
CREATE TABLE IF NOT EXISTS artisan_service_categories (
    artisan_id  UUID    NOT NULL REFERENCES artisan_profiles(id) ON DELETE CASCADE,
    category_id INTEGER NOT NULL REFERENCES service_categories(id) ON DELETE CASCADE,
    PRIMARY KEY (artisan_id, category_id)
);

-- =============================================================================
-- MATCHING MODULE — Live Spatial State (PostGIS)
-- =============================================================================

CREATE TABLE IF NOT EXISTS artisan_geodata (
    artisan_id  UUID PRIMARY KEY REFERENCES artisan_profiles(id) ON DELETE CASCADE,
    -- GEOMETRY(Point, 4326): longitude/latitude in WGS 84 (GPS standard)
    location    GEOMETRY(Point, 4326) NOT NULL,
    last_ping   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- GIST index for spatial pruning — enables KNN <-> operator and ST_DWithin fast path
CREATE INDEX IF NOT EXISTS idx_artisan_spatial_location
    ON artisan_geodata USING GIST (location);

-- =============================================================================
-- BOOKING MODULE — Bookings table (state machine)
-- =============================================================================

CREATE TABLE IF NOT EXISTS bookings (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id       UUID NOT NULL REFERENCES users(id),
    artisan_id      UUID REFERENCES artisan_profiles(id),
    category_id     INTEGER NOT NULL REFERENCES service_categories(id),
    status          booking_status NOT NULL DEFAULT 'PENDING',
    price_estimate  DECIMAL(10, 2),
    price_final     DECIMAL(10, 2),
    notes           TEXT,
    -- Spatial: client's location at time of booking
    client_location GEOMETRY(Point, 4326),
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    accepted_at     TIMESTAMP WITH TIME ZONE,
    completed_at    TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_bookings_client_id ON bookings (client_id);
CREATE INDEX IF NOT EXISTS idx_bookings_artisan_id ON bookings (artisan_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings (status);

-- =============================================================================
-- FEEDBACK MODULE — Reviews
-- =============================================================================

CREATE TABLE IF NOT EXISTS reviews (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id      UUID NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    rating          INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment         TEXT,
    sentiment_score DECIMAL(3, 2), -- -1.00 to 1.00 (populated by AI)
    ai_issues       JSONB,          -- JSON array of detected issues
    ai_flagged      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- SEED DATA — Service Categories
-- =============================================================================

INSERT INTO service_categories (name_ar, name_fr, slug, icon_url) VALUES
    ('سباكة', 'Plomberie', 'plomberie', 'https://cdn.daba.ma/icons/plomberie.svg'),
    ('كهرباء', 'Électricité', 'electricite', 'https://cdn.daba.ma/icons/electricite.svg'),
    ('تكييف وتدفئة', 'Climatisation & Chauffage', 'climatisation', 'https://cdn.daba.ma/icons/clima.svg'),
    ('نجارة', 'Menuiserie', 'menuiserie', 'https://cdn.daba.ma/icons/menuiserie.svg'),
    ('دهان', 'Peinture', 'peinture', 'https://cdn.daba.ma/icons/peinture.svg'),
    ('تنظيف', 'Nettoyage', 'nettoyage', 'https://cdn.daba.ma/icons/nettoyage.svg'),
    ('حدادة', 'Serrurerie', 'serrurerie', 'https://cdn.daba.ma/icons/serrurerie.svg'),
    ('إصلاح أجهزة', 'Réparation Électroménager', 'electromenager', 'https://cdn.daba.ma/icons/electro.svg')
ON CONFLICT (slug) DO NOTHING;

-- =============================================================================
-- UTILITY — Auto-update updated_at timestamps
-- =============================================================================

CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at_users
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_artisan_profiles
    BEFORE UPDATE ON artisan_profiles
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_bookings
    BEFORE UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
