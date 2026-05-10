-- Drop all unique constraints on platform_invitation except the intentional one on invitee_email.
-- The 'role' and 'invitation_status' columns were incorrectly declared unique=true in the JPA entity,
-- which caused Hibernate to create DB-level unique constraints that prevent multiple invitations
-- sharing the same role type or status.
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        WHERE t.relname = 'platform_invitation'
          AND c.contype = 'u'
          AND c.conname NOT IN ('ux_platform_invitation_invitee_email')
    LOOP
        EXECUTE format('ALTER TABLE platform_invitation DROP CONSTRAINT %I', r.conname);
    END LOOP;
END$$;

