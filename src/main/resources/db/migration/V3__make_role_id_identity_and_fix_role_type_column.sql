-- V3: Safety migration.
--
-- On a fresh database, V1 already creates table `role` with `role_type`.
-- This migration exists primarily to help developers who created the DB with
-- earlier iterations of the schema.
--
-- Keep it Postgres-compatible.

-- Ensure role_type column exists (Postgres supports IF NOT EXISTS on ADD COLUMN)
ALTER TABLE role ADD COLUMN IF NOT EXISTS role_type VARCHAR(255);

-- Enforce NOT NULL (safe on fresh DB; on older DBs might fail if data exists)
ALTER TABLE role ALTER COLUMN role_type SET NOT NULL;

-- Ensure unique constraint exists.
-- Postgres does NOT support "ADD CONSTRAINT IF NOT EXISTS", so we use a unique index.
-- Creating a unique index will fail if duplicates exist, which is fine for dev.
CREATE UNIQUE INDEX IF NOT EXISTS uq_role_role_type ON role(role_type);

