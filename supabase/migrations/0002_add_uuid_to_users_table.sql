ALTER TABLE users
ADD COLUMN IF NOT EXISTS uuid UUID DEFAULT gen_random_uuid() NOT NULL;

ALTER TABLE users
ADD CONSTRAINT users_uuid_unique UNIQUE (uuid);