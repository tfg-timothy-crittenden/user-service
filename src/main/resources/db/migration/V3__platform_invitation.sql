-- sql
CREATE TABLE IF NOT EXISTS platform_invitation (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   invitee_email VARCHAR(254) NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    role_type VARCHAR(50) NOT NULL,
    token VARCHAR(255) NOT NULL,
    platform_invitation_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ,
    confirmed_at TIMESTAMPTZ
    );

-- unique constraint / quick lookup by email
CREATE UNIQUE INDEX IF NOT EXISTS ux_platform_invitation_invitee_email
    ON platform_invitation (invitee_email);

-- index for token lookups (e.g. confirmation)
CREATE INDEX IF NOT EXISTS ix_platform_invitation_token
    ON platform_invitation (token);

-- index on creator for joins / queries
CREATE INDEX IF NOT EXISTS ix_platform_invitation_created_by
    ON platform_invitation (created_by_user_id);
