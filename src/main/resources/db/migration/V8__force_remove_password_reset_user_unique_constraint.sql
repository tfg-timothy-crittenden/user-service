
CREATE INDEX IF NOT EXISTS idx_password_reset_token_user_id
    ON password_reset_token (user_id);