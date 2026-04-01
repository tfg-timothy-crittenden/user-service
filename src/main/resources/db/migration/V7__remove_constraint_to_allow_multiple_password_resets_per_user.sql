-- Drop the old unique constraint on user_id (if it exists) so multiple tokens per user are allowed
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ux_password_reset_token_user_id') THEN
ALTER TABLE password_reset_token DROP CONSTRAINT ux_password_reset_token_user_id;
END IF;
END$$;
