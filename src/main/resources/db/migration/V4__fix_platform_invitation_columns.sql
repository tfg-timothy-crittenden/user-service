ALTER TABLE platform_invitation
    RENAME COLUMN platform_invitation_status TO invitation_status;

ALTER TABLE platform_invitation
    RENAME COLUMN role_type TO role;