-- Add a unique constraint to usernames to prevent duplicate usernames at the database level.
ALTER TABLE users
    ADD CONSTRAINT uq_users_username UNIQUE (username);

