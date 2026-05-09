-- V1__init_schema.sql
-- Esquema inicial de SLIOR con soporte para Auditoría, RGPD y Geocodificación Híbrida

-- Limpieza previa para asegurar recreación correcta (Solo en V1 de desarrollo)
DROP TABLE IF EXISTS stops;
DROP TABLE IF EXISTS routes;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS geocode_cache;
DROP TABLE IF EXISTS direcciones;
DROP TABLE IF EXISTS users;

-- Habilitar extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Tabla de Usuarios (con campos RGPD)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    vehicle_type VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deletion_requested_at TIMESTAMP,
    tratamiento_limitado BOOLEAN NOT NULL DEFAULT FALSE,
    consentimiento_notificaciones BOOLEAN NOT NULL DEFAULT FALSE,
    consentimiento_geolocalizacion BOOLEAN NOT NULL DEFAULT FALSE,
    anonymized_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Tabla de Rutas
CREATE TABLE IF NOT EXISTS routes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre VARCHAR(255) NOT NULL,
    fecha_planificada DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    distancia_total DOUBLE PRECISION,
    tiempo_estimado INTEGER,
    notas TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Tabla de Paradas
CREATE TABLE IF NOT EXISTS stops (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    route_id UUID NOT NULL REFERENCES routes(id),
    direccion VARCHAR(255) NOT NULL,
    destinatario VARCHAR(255) NOT NULL,
    telefono_destinatario VARCHAR(255) NOT NULL,
    latitud DOUBLE PRECISION NOT NULL,
    longitud DOUBLE PRECISION NOT NULL,
    orden_visita INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    notas TEXT,
    entregado_en TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Tabla de Auditoría (RGPD)
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    action VARCHAR(255) NOT NULL,
    entity VARCHAR(255),
    ip_hash VARCHAR(255),
    timestamp TIMESTAMP NOT NULL,
    request_id VARCHAR(255)
);

-- Tabla de Caché de Geocodificación (JSONB para flexibilidad)
CREATE TABLE IF NOT EXISTS geocode_cache (
    query_normalized VARCHAR(255) PRIMARY KEY,
    results JSONB,
    source VARCHAR(20),
    created_at TIMESTAMP DEFAULT NOW(),
    last_accessed_at TIMESTAMP DEFAULT NOW()
);

-- Tabla de Direcciones Locales (Carga de Catastro/OSM)
CREATE TABLE IF NOT EXISTS direcciones (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    numero VARCHAR(20),
    municipio VARCHAR(100),
    provincia VARCHAR(100),
    codigo_postal VARCHAR(10),
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION
);

-- Índice Trigram para búsqueda difusa (fuzzy search) eficiente
CREATE INDEX IF NOT EXISTS idx_direcciones_nombre_trgm ON direcciones USING gin (nombre gin_trgm_ops);
