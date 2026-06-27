CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(uuid) ON DELETE CASCADE,
    role user_role NOT NULL,
    PRIMARY KEY (user_id, role)
);