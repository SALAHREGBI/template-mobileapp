-- =============================================================================
-- V1__baseline_schema.sql
-- Flyway baseline migration.
-- The actual schema is created by infra/docker/init-scripts/01-init.sql
-- on first container boot. This file marks Flyway's starting point so it
-- won't try to re-run the init script.
-- =============================================================================

-- This migration intentionally left as a baseline marker.
-- Future migrations (V2, V3, ...) will add columns/tables incrementally.
-- Keep this baseline migration side-effect free and environment-agnostic.
SELECT 1;
