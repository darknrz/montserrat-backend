-- ============================================================
--  I.E.P. Nuestra Señora de Monserrat - Script SQL (Limpio)
--  PostgreSQL 14+
--  Ejecutar en la BD: monserrat_db
-- ============================================================

-- Crear base de datos (ejecutar conectado a postgres)
-- CREATE DATABASE monserrat_db;
-- \c monserrat_db

-- ============================================================
-- TABLAS
-- ============================================================

-- Admins
CREATE TABLE IF NOT EXISTS admins (
    id       BIGSERIAL    PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre   VARCHAR(100) NOT NULL,
    rol      VARCHAR(50)  NOT NULL DEFAULT 'ADMIN',
    activo   BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Institution
CREATE TABLE IF NOT EXISTS institution (
    id               BIGSERIAL    PRIMARY KEY,
    nombre           VARCHAR(200) NOT NULL,
    direccion        VARCHAR(300) NOT NULL,
    ciudad           VARCHAR(100) NOT NULL,
    distrito         VARCHAR(100),
    anio_fundacion   VARCHAR(4)   NOT NULL,
    telefono         VARCHAR(100),
    email            VARCHAR(150),
    niveles          VARCHAR(100),
    tipo             VARCHAR(50),
    mision           TEXT         NOT NULL,
    vision           TEXT         NOT NULL,
    descripcion      TEXT,
    logo_url         VARCHAR(300),
    horario_atencion VARCHAR(100)
);

-- Ingresantes
CREATE TABLE IF NOT EXISTS ingresantes (
    id                  BIGSERIAL    PRIMARY KEY,
    nombre              VARCHAR(200) NOT NULL,
    universidad         VARCHAR(100) NOT NULL,
    universidad_siglas  VARCHAR(20)  NOT NULL,
    carrera             VARCHAR(150) NOT NULL,
    anio                VARCHAR(4)   NOT NULL,
    tipo_seleccion      VARCHAR(50)  NOT NULL,
    foto_url            VARCHAR(300),
    activo              BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Videos
CREATE TABLE IF NOT EXISTS videos (
    id         BIGSERIAL    PRIMARY KEY,
    titulo     VARCHAR(200) NOT NULL,
    descripcion TEXT,
    tag        VARCHAR(50)  NOT NULL,
    tag_color  VARCHAR(30)  NOT NULL,
    activo     BOOLEAN      NOT NULL DEFAULT TRUE,
    orden      INTEGER      NOT NULL DEFAULT 0,
    creado_en  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Redes Sociales
CREATE TABLE IF NOT EXISTS redes_sociales (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(50)  NOT NULL,
    icono  VARCHAR(30)  NOT NULL,
    url    VARCHAR(300) NOT NULL,
    activo BOOLEAN      NOT NULL DEFAULT TRUE,
    orden  INTEGER      NOT NULL DEFAULT 0
);

-- Usuarios academicos
CREATE TABLE IF NOT EXISTS usuarios_academicos (
    id                       BIGSERIAL    PRIMARY KEY,
    dni                      VARCHAR(20)  NOT NULL UNIQUE,
    codigo                   VARCHAR(30) UNIQUE,
    codigo_chatbot           VARCHAR(12) UNIQUE,
    password                 VARCHAR(255) NOT NULL,
    nombre                   VARCHAR(150) NOT NULL,
    nombres                  VARCHAR(100),
    apellidos                VARCHAR(120),
    correo                   VARCHAR(120),
    direccion                VARCHAR(250),
    fecha_nacimiento         DATE,
    rol                      VARCHAR(20)  NOT NULL,
    activo                   BOOLEAN      NOT NULL DEFAULT TRUE,
    estado                   VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    debe_cambiar_contrasena  BOOLEAN      NOT NULL DEFAULT TRUE,
    telefono                 VARCHAR(30),
    foto_url                 VARCHAR(300),
    nivel_educativo          VARCHAR(20),
    grado                    VARCHAR(50),
    seccion                  VARCHAR(50),
    materia                  VARCHAR(120),
    especialidad             VARCHAR(120),
    estado_matricula         VARCHAR(20)  DEFAULT 'MATRICULADO',
    pension_pagada           BOOLEAN      NOT NULL DEFAULT FALSE,
    pension_observacion      VARCHAR(200)
);

-- Asignaciones academicas
CREATE TABLE IF NOT EXISTS asignaciones_academicas (
    id         BIGSERIAL    PRIMARY KEY,
    docente_id BIGINT       NOT NULL REFERENCES usuarios_academicos (id),
    alumno_id  BIGINT       NOT NULL REFERENCES usuarios_academicos (id),
    curso      VARCHAR(50)  NOT NULL,
    nivel_educativo VARCHAR(20) NOT NULL,
    grado      VARCHAR(50)  NOT NULL,
    seccion    VARCHAR(10)  NOT NULL,
    activo     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_asig_docente ON asignaciones_academicas (docente_id);
CREATE INDEX IF NOT EXISTS idx_asig_alumno ON asignaciones_academicas (alumno_id);
CREATE INDEX IF NOT EXISTS idx_asig_curso ON asignaciones_academicas (curso);

-- Asistencias academicas
CREATE TABLE IF NOT EXISTS asistencias_academicas (
    id          BIGSERIAL    PRIMARY KEY,
    alumno_id   BIGINT       NOT NULL REFERENCES usuarios_academicos (id),
    docente_id  BIGINT       NOT NULL REFERENCES usuarios_academicos (id),
    fecha       DATE         NOT NULL,
    estado      VARCHAR(20)  NOT NULL,
    observacion VARCHAR(300),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Notas academicas
CREATE TABLE IF NOT EXISTS notas_academicas (
    id          BIGSERIAL    PRIMARY KEY,
    alumno_id   BIGINT       NOT NULL REFERENCES usuarios_academicos (id),
    docente_id  BIGINT       NOT NULL REFERENCES usuarios_academicos (id),
    curso       VARCHAR(50) NOT NULL,
    periodo     VARCHAR(60)  NOT NULL,
    tipo_evaluacion VARCHAR(30) NOT NULL DEFAULT 'EXAMEN',
    valor       DOUBLE PRECISION NOT NULL,
    observacion VARCHAR(300),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- DATOS INICIALES
-- ============================================================

-- Admin por defecto
-- usuario:   adminmontserrat
-- password:  adminmontserrat  (BCrypt $2b$10 cost=10)
INSERT INTO admins (username, password, nombre, rol, activo)
VALUES (
    'adminmontserrat',
    '$2b$10$dwLfNWlSx2CDXuxKDZ8wUOf3P/xzl4tAxeLvkFaASu/pIUFWZRtZa',
    'Administrador Monserrat',
    'ADMIN',
    TRUE
) ON CONFLICT (username) DO NOTHING;

-- ============================================================
-- VERIFICAR
-- ============================================================
SELECT 'admins'         AS tabla, COUNT(*) AS total FROM admins
UNION ALL
SELECT 'institution',    COUNT(*) FROM institution
UNION ALL
SELECT 'ingresantes',    COUNT(*) FROM ingresantes
UNION ALL
SELECT 'videos',         COUNT(*) FROM videos
UNION ALL
SELECT 'redes_sociales', COUNT(*) FROM redes_sociales;
