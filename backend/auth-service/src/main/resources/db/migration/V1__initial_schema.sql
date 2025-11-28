
-- Create permissions table
CREATE TABLE permissions (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE,
                             description VARCHAR(255),
                             resource VARCHAR(100) NOT NULL,
                             action VARCHAR(50) NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create roles table
CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(255),
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       phone_number VARCHAR(20),
                       is_enabled BOOLEAN NOT NULL DEFAULT true,
                       is_locked BOOLEAN NOT NULL DEFAULT false,
                       is_email_verified BOOLEAN NOT NULL DEFAULT false,
                       failed_login_attempts INTEGER NOT NULL DEFAULT 0,
                       last_login_at TIMESTAMP,
                       password_changed_at TIMESTAMP,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       version INTEGER NOT NULL DEFAULT 0
);

-- Create role_permissions junction table
CREATE TABLE role_permissions (
                                  role_id BIGINT NOT NULL,
                                  permission_id BIGINT NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (role_id, permission_id),
                                  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
                                  FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Create user_roles junction table
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            assigned_by BIGINT,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
                            FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Create refresh_tokens table for JWT refresh tokens
CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                token VARCHAR(500) NOT NULL UNIQUE,
                                expires_at TIMESTAMP NOT NULL,
                                is_revoked BOOLEAN NOT NULL DEFAULT false,
                                revoked_at TIMESTAMP,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create audit_logs table for security events
CREATE TABLE audit_logs (
                            id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT,
                            username VARCHAR(50),
                            event_type VARCHAR(50) NOT NULL,
                            event_details TEXT,
                            ip_address VARCHAR(45),
                            user_agent TEXT,
                            correlation_id VARCHAR(100),
                            status VARCHAR(20) NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_enabled ON users(is_enabled);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_event_type ON audit_logs(event_type);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_correlation_id ON audit_logs(correlation_id);

-- Insert default permissions
INSERT INTO permissions (name, description, resource, action) VALUES
                                                                  ('USER_READ', 'View user information', 'USER', 'READ'),
                                                                  ('USER_WRITE', 'Create and update users', 'USER', 'WRITE'),
                                                                  ('USER_DELETE', 'Delete users', 'USER', 'DELETE'),
                                                                  ('ROLE_READ', 'View roles', 'ROLE', 'READ'),
                                                                  ('ROLE_WRITE', 'Create and update roles', 'ROLE', 'WRITE'),
                                                                  ('ROLE_DELETE', 'Delete roles', 'ROLE', 'DELETE'),
                                                                  ('WALLET_READ', 'View wallet information', 'WALLET', 'READ'),
                                                                  ('WALLET_WRITE', 'Perform wallet operations', 'WALLET', 'WRITE'),
                                                                  ('TRANSACTION_READ', 'View transactions', 'TRANSACTION', 'READ'),
                                                                  ('TRANSACTION_WRITE', 'Create transactions', 'TRANSACTION', 'WRITE'),
                                                                  ('ADMIN_ACCESS', 'Full administrative access', 'ADMIN', 'ALL');

-- Insert default roles
INSERT INTO roles (name, description) VALUES
                                          ('ROLE_USER', 'Standard user with basic permissions'),
                                          ('ROLE_ADMIN', 'Administrator with full access'),
                                          ('ROLE_SUPPORT', 'Customer support with limited access');

-- Assign permissions to roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_USER' AND p.name IN ('USER_READ', 'WALLET_READ', 'WALLET_WRITE', 'TRANSACTION_READ', 'TRANSACTION_WRITE');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_SUPPORT' AND p.name IN ('USER_READ', 'WALLET_READ', 'TRANSACTION_READ');
