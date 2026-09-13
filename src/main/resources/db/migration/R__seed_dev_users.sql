-- User roles

-- jsmith -> STUDENT
INSERT INTO user_roles (user_id, role)
SELECT u.id, 'STUDENT'
FROM users u
WHERE u.email = 'john.smith@example.com'
  AND NOT EXISTS (
    SELECT 1
    FROM user_roles ur
    WHERE ur.user_id = u.id
      AND ur.role = 'STUDENT'
);

-- mdoe -> TEACHER
INSERT INTO user_roles (user_id, role)
SELECT u.id, 'TEACHER'
FROM users u
WHERE u.email = 'mary.doe@example.com'
  AND NOT EXISTS (
    SELECT 1
    FROM user_roles ur
    WHERE ur.user_id = u.id
      AND ur.role = 'TEACHER'
);

-- rjohnson -> ADMIN
INSERT INTO user_roles (user_id, role)
SELECT u.id, 'ADMIN'
FROM users u
WHERE u.email = 'robert.johnson@example.com'
  AND NOT EXISTS (
    SELECT 1
    FROM user_roles ur
    WHERE ur.user_id = u.id
      AND ur.role = 'ADMIN'
);

-- rjohnson -> TEACHER
INSERT INTO user_roles (user_id, role)
SELECT u.id, 'TEACHER'
FROM users u
WHERE u.email = 'robert.johnson@example.com'
  AND NOT EXISTS (
    SELECT 1
    FROM user_roles ur
    WHERE ur.user_id = u.id
      AND ur.role = 'TEACHER'
);