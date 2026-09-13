-- V5__replace_role_entity_with_enum.sql

-- 1. Add the new role value column.
ALTER TABLE user_roles
    ADD COLUMN role VARCHAR(50);

-- 2. Copy the role value from the old role table.
UPDATE user_roles ur
SET role = r.role_type
FROM role r
WHERE r.id = ur.role_id;

-- 3. All existing assignments must now have a role.
ALTER TABLE user_roles
    ALTER COLUMN role SET NOT NULL;

-- 4. Remove the old role_id column and anything depending on it
--    (old FK / old composite PK).
ALTER TABLE user_roles
    DROP COLUMN role_id CASCADE;

-- 5. Define the new identity of a user-role assignment.
ALTER TABLE user_roles
    ADD CONSTRAINT pk_user_roles
        PRIMARY KEY (user_id, role);

-- 6. Only allow domain roles.
ALTER TABLE user_roles
    ADD CONSTRAINT ck_user_roles_role
        CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN'));

-- 7. Role is no longer an entity/table.
DROP TABLE role;