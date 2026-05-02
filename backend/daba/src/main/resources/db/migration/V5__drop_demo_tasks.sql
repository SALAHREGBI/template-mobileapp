-- =============================================================================
-- V5 — remove legacy demo table if present (from earlier boilerplate installs)
-- =============================================================================

DROP TRIGGER IF EXISTS set_updated_at_demo_tasks ON demo_tasks;
DROP TABLE IF EXISTS demo_tasks;
