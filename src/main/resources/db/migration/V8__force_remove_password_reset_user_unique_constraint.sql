-- Force-remove any unique constraint / index on password_reset_token.user_id so multiple tokens per user are allowed
-- Safe to run idempotently
DO $$
DECLARE
  idx RECORD;
BEGIN
  -- Drop constraint if present
  IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ux_password_reset_token_user_id') THEN
    ALTER TABLE password_reset_token DROP CONSTRAINT ux_password_reset_token_user_id;
  END IF;

  -- Some DBs may have created a UNIQUE INDEX instead of named constraint - drop it if exists
  IF EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relkind = 'i' AND c.relname = 'ux_password_reset_token_user_id') THEN
    DROP INDEX IF EXISTS ux_password_reset_token_user_id;
  END IF;

  -- Also drop any unnamed unique indexes that cover only user_id
  FOR idx IN
    SELECT indexname FROM pg_indexes WHERE tablename = 'password_reset_token' AND indexdef ILIKE '%UNIQUE%' AND indexdef ILIKE '%user_id%'
  LOOP
    EXECUTE format('DROP INDEX IF EXISTS %I', idx.indexname);
  END LOOP;
END$$;

-- Ensure an index exists for user_id for lookups (non-unique)
CREATE INDEX IF NOT EXISTS idx_password_reset_token_user_id ON password_reset_token (user_id);
