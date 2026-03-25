-- Seed roles
-- With auto-increment IDs, we seed by role_type (unique) and let the DB generate IDs.

-- Cross-database compatible inserts (Postgres + H2)
INSERT INTO role (role_type)
SELECT 'TEACHER'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_type = 'TEACHER');

INSERT INTO role (role_type)
SELECT 'STUDENT'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_type = 'STUDENT');

INSERT INTO role (role_type)
SELECT 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_type = 'ADMIN');
