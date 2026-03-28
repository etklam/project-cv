-- Add role column to users table
ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'USER' NOT NULL;
CREATE INDEX idx_users_role ON users(role);

-- Set all existing users to USER role
UPDATE users SET role = 'USER' WHERE role IS NULL;

-- Create initial admin user
-- Password: admin123 (BCrypt hash generated using BCrypt 12 rounds)
INSERT INTO users (email, password_hash, display_name, role, credit_balance, invite_code, onboarding_status, created_at, updated_at)
VALUES (
    'admin@project.cv',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj6Sf2iHGV1u',
    'System Admin',
    'ADMIN',
    1000,
    'ADMIN-INIT',
    'DONE',
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;
