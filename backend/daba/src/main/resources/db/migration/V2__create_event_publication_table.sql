-- =============================================================================
-- V2__create_event_publication_table.sql
-- Spring Modulith event publication store table.
-- =============================================================================

CREATE TABLE IF NOT EXISTS event_publication (
    id                UUID PRIMARY KEY,
    publication_date  TIMESTAMP WITH TIME ZONE NOT NULL,
    listener_id       VARCHAR(512) NOT NULL,
    serialized_event  TEXT NOT NULL,
    event_type        VARCHAR(512) NOT NULL,
    completion_date   TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_event_publication_completion_date
    ON event_publication (completion_date);
