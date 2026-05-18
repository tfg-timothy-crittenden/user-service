-- Baseline schema for user-service.
-- This file replaces the historical V1..V5 migration chain.
-- New dev databases should be created by applying ONLY this baseline
-- plus any repeatable migrations (R__*.sql).

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    surname VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    verified BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username)
);

-- Role table
CREATE TABLE IF NOT EXISTS role (
    id BIGSERIAL PRIMARY KEY,
    role_type VARCHAR(255) NOT NULL,
    CONSTRAINT uq_role_role_type UNIQUE (role_type)
);

-- Join table used by the JPA ManyToMany mapping
CREATE TABLE IF NOT EXISTS user_roles (
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (role_id, user_id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE RESTRICT,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

