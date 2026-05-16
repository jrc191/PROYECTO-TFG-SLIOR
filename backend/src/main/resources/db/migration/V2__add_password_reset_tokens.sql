-- V2: Creación de la tabla para tokens de restablecimiento de contraseña
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(6) NOT NULL,
    expiry_date TIMESTAMP NOT NULL
);

-- Índice para búsquedas rápidas por email y token
CREATE INDEX idx_reset_token_email_token ON password_reset_tokens(email, token);
