-- Remove any unique constraint/index on password_reset_token.user_id so multiple tokens per user are allowed
-- This migration is idempotent and safe to run multiple times.
DO $$
DECLARE
  idxname text;
BEGIN
  -- Drop named constraint if present
  IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ux_password_reset_token_user_id') THEN
    ALTER TABLE password_reset_token DROP CONSTRAINT ux_password_reset_token_user_id;
  END IF;

  -- Drop index if present with that name
  IF EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relkind = 'i' AND c.relname = 'ux_password_reset_token_user_id') THEN
    DROP INDEX IF EXISTS ux_password_reset_token_user_id;
  END IF;

  -- Drop any other UNIQUE indexes that reference only user_id
  FOR idxname IN
    SELECT indexname FROM pg_indexes
    WHERE tablename = 'password_reset_token'
      AND indexdef ILIKE '%UNIQUE%'
      AND (indexdef ILIKE '%(user_id)%' OR indexdef ILIKE '%user_id%')
  LOOP
    EXECUTE format('DROP INDEX IF EXISTS %I', idxname);
  END LOOP;
END$$;

-- Ensure a non-unique index exists for lookups (idempotent)
CREATE INDEX IF NOT EXISTS idx_password_reset_token_user_id ON password_reset_token (user_id);

