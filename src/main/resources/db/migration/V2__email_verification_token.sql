-- Email verification token table
CREATE TABLE IF NOT EXISTS email_verification_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL UNIQUE,
    token VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    confirmed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_email_verification_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_email_verification_token_user_email ON email_verification_token(user_email);
CREATE INDEX IF NOT EXISTS idx_email_verification_token_token ON email_verification_token(token);

