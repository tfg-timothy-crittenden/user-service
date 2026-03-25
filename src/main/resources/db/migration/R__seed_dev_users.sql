-- Repeatable migration: DEV seed data
-- Idempotent inserts so local startup is safe to run multiple times.

INSERT INTO users (username, name, surname, email)
SELECT 'jsmith', 'John', 'Smith', 'john.smith@example.com'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'john.smith@example.com');

INSERT INTO users (username, name, surname, email)
SELECT 'mdoe', 'Mary', 'Doe', 'mary.doe@example.com'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'mary.doe@example.com');

INSERT INTO users (username, name, surname, email)
SELECT 'rjohnson', 'Robert', 'Johnson', 'robert.johnson@example.com'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'robert.johnson@example.com');

