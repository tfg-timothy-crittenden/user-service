ALTER TABLE user_roles
    DROP CONSTRAINT IF EXISTS fkhfh9dx7w3ubf1co1vdev94g3f;

ALTER TABLE user_roles
    DROP CONSTRAINT IF EXISTS fk_user_roles_user;

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE;