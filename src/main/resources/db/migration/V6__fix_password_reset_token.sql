-- Make idempotent fixes to password_reset_token so we don't edit V5
-- This migration is safe to run even if V5 already created the table.

-- Ensure sequence exists
CREATE SEQUENCE IF NOT EXISTS password_reset_token_seq START 1;

-- Create table if missing (columns defined defensively)
CREATE TABLE IF NOT EXISTS password_reset_token (
    id BIGINT PRIMARY KEY DEFAULT nextval('password_reset_token_seq'),
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    expires_at TIMESTAMP WITH TIME ZONE,
    used BOOLEAN NOT NULL DEFAULT false,
    valid BOOLEAN NOT NULL DEFAULT true
);

-- Ensure required columns exist (safe, uses IF NOT EXISTS)
ALTER TABLE password_reset_token ADD COLUMN IF NOT EXISTS token_hash VARCHAR(128);
ALTER TABLE password_reset_token ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE password_reset_token ADD COLUMN IF NOT EXISTS used BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE password_reset_token ADD COLUMN IF NOT EXISTS valid BOOLEAN NOT NULL DEFAULT true;

-- Add foreign key to users if not present
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
    WHERE tc.table_name = 'password_reset_token' AND tc.constraint_type = 'FOREIGN KEY' AND kcu.column_name = 'user_id'
  ) THEN
    ALTER TABLE password_reset_token
      ADD CONSTRAINT fk_password_reset_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
  END IF;
END$$;

-- Add unique constraint on token_hash if not present
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ux_password_reset_token_token_hash') THEN
    ALTER TABLE password_reset_token ADD CONSTRAINT ux_password_reset_token_token_hash UNIQUE (token_hash);
  END IF;
END$$;

-- Create indexes if not exist
CREATE INDEX IF NOT EXISTS idx_password_reset_token_expires_at ON password_reset_token (expires_at);
CREATE INDEX IF NOT EXISTS idx_password_reset_token_user_id ON password_reset_token (user_id);

