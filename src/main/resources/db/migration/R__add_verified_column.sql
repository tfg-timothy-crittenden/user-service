-- Repeatable migration to add verified column to users
ALTER TABLE users
ADD COLUMN IF NOT EXISTS verified BOOLEAN DEFAULT FALSE NOT NULL;

