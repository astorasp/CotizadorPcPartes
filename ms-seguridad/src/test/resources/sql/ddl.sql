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

-- Crear índices para mejorar performance
CREATE INDEX idx_usuario_activo ON usuario(activo);
CREATE INDEX idx_rol_activo ON rol(activo);
CREATE INDEX idx_rol_asignado_activo ON rol_asignado(activo);
CREATE INDEX idx_rol_asignado_usuario ON rol_asignado(id_usuario);
CREATE INDEX idx_rol_asignado_rol ON rol_asignado(id_rol);

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
(1, 'admin', '$2a$12$IUgNrSprzR3Ocwhcowqb0.NUiKmxOeQVAyFDJYr5AtLVr8iRZiUwC', true, NOW(), NOW());

-- Insertar usuario normal
-- Contraseña: user123 (BCrypt strength 12)  
INSERT INTO usuario (id, usuario, password, activo, fecha_creacion, fecha_modificacion) VALUES
(2, 'testuser', '$2a$12$pp2wCuSdMHuu5HPk8Sc5Cuc7DYJalgXKGDPeNPn4CqgpKu7t54Ckq', true, NOW(), NOW());

-- Usuario inactivo para tests
INSERT INTO usuario (id, usuario, password, activo, fecha_creacion, fecha_modificacion) VALUES
(3, 'inactive', '$2a$12$pp2wCuSdMHuu5HPk8Sc5Cuc7DYJalgXKGDPeNPn4CqgpKu7t54Ckq', false, NOW(), NOW());

-- Usuario sin roles para tests
INSERT INTO usuario (id, usuario, password, activo, fecha_creacion, fecha_modificacion) VALUES
(4, 'noroles', '$2a$12$pp2wCuSdMHuu5HPk8Sc5Cuc7DYJalgXKGDPeNPn4CqgpKu7t54Ckq', true, NOW(), NOW());

-- Asignar rol ADMIN al usuario admin
INSERT INTO rol_asignado (id_usuario, id_rol, activo, fecha_creacion, fecha_modificacion) VALUES
(1, 1, true, NOW(), NOW());

-- Asignar rol USER al usuario testuser
INSERT INTO rol_asignado (id_usuario, id_rol, activo, fecha_creacion, fecha_modificacion) VALUES
(2, 2, true, NOW(), NOW());

-- Asignar rol ADMIN también al testuser para algunos tests
INSERT INTO rol_asignado (id_usuario, id_rol, activo, fecha_creacion, fecha_modificacion) VALUES
(2, 1, true, NOW(), NOW());