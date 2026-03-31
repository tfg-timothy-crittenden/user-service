-- sql
DO $$
BEGIN
  -- rename if legacy column exists
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'platform_invitation' AND column_name = 'platform_invitation_status'
  ) THEN
ALTER TABLE platform_invitation RENAME COLUMN platform_invitation_status TO invitation_status;
END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'platform_invitation' AND column_name = 'role_type'
  ) THEN
ALTER TABLE platform_invitation RENAME COLUMN role_type TO role;
END IF;

  -- ensure expected columns exist (add if missing)
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'platform_invitation' AND column_name = 'invitation_status'
  ) THEN
ALTER TABLE platform_invitation ADD COLUMN invitation_status VARCHAR(50);
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'platform_invitation' AND column_name = 'role'
  ) THEN
ALTER TABLE platform_invitation ADD COLUMN role VARCHAR(50);
END IF;
END
$$;
