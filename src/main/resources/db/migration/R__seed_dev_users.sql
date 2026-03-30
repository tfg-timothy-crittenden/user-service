-- Repeatable migration: DEV seed data
-- Idempotent inserts so local startup is safe to run multiple times.

-- Ensure roles exist (repeatable safety; V2 also seeds these)
INSERT INTO role (role_type)
SELECT 'TEACHER'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_type = 'TEACHER');

INSERT INTO role (role_type)
SELECT 'STUDENT'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_type = 'STUDENT');

INSERT INTO role (role_type)
SELECT 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_type = 'ADMIN');

-- Users
-- Canonical column is password_hash (see V5__standardize_users_password_hash.sql)
-- Include verified column explicitly to be safe if the column exists with NOT NULL constraint in some environments.
INSERT INTO users (username, name, surname, email, password_hash, verified)
SELECT 'jsmith', 'John', 'Smith', 'john.smith@example.com', '$2a$12$FO7NUwkIDboYS53fl5yZzO8.3A6cHxdBmuqvlwJ56MdY97GI8IPhe', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'john.smith@example.com');

INSERT INTO users (username, name, surname, email, password_hash, verified)
SELECT 'mdoe', 'Mary', 'Doe', 'mary.doe@example.com', '$2a$12$FO7NUwkIDboYS53fl5yZzO8.3A6cHxdBmuqvlwJ56MdY97GI8IPhe', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'mary.doe@example.com');

INSERT INTO users (username, name, surname, email, password_hash, verified)
SELECT 'rjohnson', 'Robert', 'Johnson', 'robert.johnson@example.com', '$2a$12$FO7NUwkIDboYS53fl5yZzO8.3A6cHxdBmuqvlwJ56MdY97GI8IPhe', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'robert.johnson@example.com');

-- Ensure seeded users are marked verified for local dev convenience
UPDATE users SET verified = TRUE WHERE email IN (
  'john.smith@example.com',
  'mary.doe@example.com',
  'robert.johnson@example.com'
);

-- User roles (join table)
-- jsmith -> STUDENT
INSERT INTO user_roles (role_id, user_id)
SELECT r.id, u.id
FROM role r
JOIN users u ON u.email = 'john.smith@example.com'
WHERE r.role_type = 'STUDENT'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.role_id = r.id AND ur.user_id = u.id
  );

-- mdoe -> TEACHER
INSERT INTO user_roles (role_id, user_id)
SELECT r.id, u.id
FROM role r
JOIN users u ON u.email = 'mary.doe@example.com'
WHERE r.role_type = 'TEACHER'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.role_id = r.id AND ur.user_id = u.id
  );

-- rjohnson -> ADMIN
INSERT INTO user_roles (role_id, user_id)
SELECT r.id, u.id
FROM role r
JOIN users u ON u.email = 'robert.johnson@example.com'
WHERE r.role_type = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.role_id = r.id AND ur.user_id = u.id
  );
