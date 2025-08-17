-- TestContainers ya creó la base de datos, no necesitamos crear ni usar
-- Solo crear las tablas directamente

-- Crear tabla de roles
CREATE TABLE IF NOT EXISTS rol (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Crear tabla de usuarios
CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Crear tabla de roles asignados (relación many-to-many)
CREATE TABLE IF NOT EXISTS rol_asignado (
    id_usuario BIGINT NOT NULL,
    id_rol BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_rol),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (id_rol) REFERENCES rol(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Crear tabla de acceso para gestión de sesiones únicas
CREATE TABLE IF NOT EXISTS acceso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_sesion VARCHAR(255) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Crear índices para mejorar performance
CREATE INDEX idx_usuario_activo ON usuario(activo);
CREATE INDEX idx_rol_activo ON rol(activo);
CREATE INDEX idx_rol_asignado_activo ON rol_asignado(activo);
CREATE INDEX idx_rol_asignado_usuario ON rol_asignado(id_usuario);
CREATE INDEX idx_rol_asignado_rol ON rol_asignado(id_rol);
-- Índices para tabla acceso
CREATE INDEX idx_acceso_id_sesion ON acceso(id_sesion);
CREATE INDEX idx_acceso_usuario_id ON acceso(usuario_id);
CREATE INDEX idx_acceso_activo ON acceso(activo);
CREATE INDEX idx_acceso_usuario_activo ON acceso(usuario_id, activo);
CREATE INDEX idx_acceso_fecha_inicio ON acceso(fecha_inicio);
CREATE INDEX idx_acceso_fecha_fin ON acceso(fecha_fin);

-- =======================================================
-- DATOS DE PRUEBA
-- =======================================================

-- Insertar roles básicos
INSERT INTO rol (id, nombre, activo, fecha_creacion, fecha_modificacion) VALUES
(1, 'ADMIN', true, NOW(), NOW()),
(2, 'USER', true, NOW(), NOW()),
(3, 'SUPERVISOR', true, NOW(), NOW()),
(4, 'GERENTE', true, NOW(), NOW()),
(5, 'VENDEDOR', true, NOW(), NOW()),
(6, 'INVENTARIO', true, NOW(), NOW()),
(7, 'CONSULTOR', true, NOW(), NOW());

-- Insertar usuario administrador
-- Contraseña: admin123 (BCrypt strength 12)
INSERT INTO usuario (id, usuario, password, activo, fecha_creacion, fecha_modificacion) VALUES
(1, 'admin', '$2a$12$jdrYIKBJmaImZO9zj1xZkOaIyIDRNVr4Fq2pEZesSF4IvwkJ39uLi', true, NOW(), NOW());

-- Insertar usuario normal
-- Contraseña: user123 (BCrypt strength 12)  
INSERT INTO usuario (id, usuario, password, activo, fecha_creacion, fecha_modificacion) VALUES
(2, 'testuser', '$2a$12$EWK2AXaS89uv1lWLWcNvUureXbRH/cWVq1a3v5cZeRdyydF0MiI7m', true, NOW(), NOW());

-- Usuario inactivo para tests
INSERT INTO usuario (id, usuario, password, activo, fecha_creacion, fecha_modificacion) VALUES
(3, 'inactive', '$2a$12$EWK2AXaS89uv1lWLWcNvUureXbRH/cWVq1a3v5cZeRdyydF0MiI7m', false, NOW(), NOW());

-- Usuario sin roles para tests
INSERT INTO usuario (id, usuario, password, activo, fecha_creacion, fecha_modificacion) VALUES
(4, 'noroles', '$2a$12$EWK2AXaS89uv1lWLWcNvUureXbRH/cWVq1a3v5cZeRdyydF0MiI7m', true, NOW(), NOW());

-- Asignar rol ADMIN al usuario admin
INSERT INTO rol_asignado (id_usuario, id_rol, activo, fecha_creacion, fecha_modificacion) VALUES
(1, 1, true, NOW(), NOW());

-- Asignar rol USER al usuario testuser
INSERT INTO rol_asignado (id_usuario, id_rol, activo, fecha_creacion, fecha_modificacion) VALUES
(2, 2, true, NOW(), NOW());

-- Asignar rol ADMIN también al testuser para algunos tests
INSERT INTO rol_asignado (id_usuario, id_rol, activo, fecha_creacion, fecha_modificacion) VALUES
(2, 1, true, NOW(), NOW());