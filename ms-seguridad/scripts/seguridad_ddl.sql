-- =====================================================
-- DDL Script para Microservicio de Seguridad
-- Base de datos: MySQL 8.4.4
-- Generado desde: Diagrama Persistencia Seguridad.png
-- =====================================================

-- Crear base de datos si no existe
CREATE DATABASE IF NOT EXISTS seguridad 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE seguridad;

-- =====================================================
-- Tabla: Usuario
-- Descripción: Almacena la información de los usuarios del sistema
-- =====================================================
CREATE TABLE usuario (
    id INT NOT NULL AUTO_INCREMENT,
    usuario VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_usuario UNIQUE (usuario),
    
    INDEX idx_usuario_activo (activo),
    INDEX idx_usuario_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Tabla: Rol
-- Descripción: Catálogo de roles disponibles en el sistema
-- =====================================================
CREATE TABLE rol (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT pk_rol PRIMARY KEY (id),
    CONSTRAINT uk_rol_nombre UNIQUE (nombre),
    
    INDEX idx_rol_activo (activo),
    INDEX idx_rol_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Tabla: RolAsignado
-- Descripción: Tabla de relación muchos a muchos entre Usuario y Rol
-- =====================================================
CREATE TABLE rol_asignado (
    id_usuario INT NOT NULL,
    id_rol INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT pk_rol_asignado PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT fk_rol_asignado_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rol_asignado_rol FOREIGN KEY (id_rol) 
        REFERENCES rol(id) ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_rol_asignado_usuario (id_usuario),
    INDEX idx_rol_asignado_rol (id_rol),
    INDEX idx_rol_asignado_activo (activo),
    INDEX idx_rol_asignado_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Tabla: Acceso
-- Descripción: Gestión de sesiones únicas por usuario
-- =====================================================
CREATE TABLE acceso (
    id BIGINT NOT NULL AUTO_INCREMENT,
    id_sesion VARCHAR(255) NOT NULL,
    usuario_id INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin DATETIME NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT pk_acceso PRIMARY KEY (id),
    CONSTRAINT uk_acceso_id_sesion UNIQUE (id_sesion),
    CONSTRAINT fk_acceso_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_acceso_id_sesion (id_sesion),
    INDEX idx_acceso_usuario_id (usuario_id),
    INDEX idx_acceso_activo (activo),
    INDEX idx_acceso_usuario_activo (usuario_id, activo),
    INDEX idx_acceso_fecha_inicio (fecha_inicio),
    INDEX idx_acceso_fecha_fin (fecha_fin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Comentarios para las tablas
-- =====================================================
ALTER TABLE usuario COMMENT = 'Tabla que almacena los usuarios del sistema con sus credenciales';
ALTER TABLE rol COMMENT = 'Catálogo de roles disponibles para asignación a usuarios';
ALTER TABLE rol_asignado COMMENT = 'Tabla de relación que asigna roles específicos a usuarios';
ALTER TABLE acceso COMMENT = 'Tabla que gestiona sesiones únicas por usuario para el sistema de autenticación';

-- =====================================================
-- Comentarios para las columnas
-- =====================================================
ALTER TABLE usuario 
    MODIFY COLUMN id INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único del usuario',
    MODIFY COLUMN usuario VARCHAR(100) NOT NULL COMMENT 'Nombre de usuario único para login',
    MODIFY COLUMN password VARCHAR(255) NOT NULL COMMENT 'Contraseña encriptada del usuario',
    MODIFY COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Indica si el usuario está activo en el sistema',
    MODIFY COLUMN fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha y hora de creación del registro',
    MODIFY COLUMN fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha y hora de última modificación del registro';

ALTER TABLE rol 
    MODIFY COLUMN id INT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único del rol',
    MODIFY COLUMN nombre VARCHAR(100) NOT NULL COMMENT 'Nombre descriptivo del rol',
    MODIFY COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Indica si el rol está activo para asignación',
    MODIFY COLUMN fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha y hora de creación del registro',
    MODIFY COLUMN fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha y hora de última modificación del registro';

ALTER TABLE rol_asignado 
    MODIFY COLUMN id_usuario INT NOT NULL COMMENT 'Referencia al usuario (FK)',
    MODIFY COLUMN id_rol INT NOT NULL COMMENT 'Referencia al rol (FK)',
    MODIFY COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Indica si la asignación está activa',
    MODIFY COLUMN fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha y hora de creación del registro',
    MODIFY COLUMN fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha y hora de última modificación del registro';

ALTER TABLE acceso 
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Identificador único del acceso',
    MODIFY COLUMN id_sesion VARCHAR(255) NOT NULL COMMENT 'Identificador único de la sesión (UUID)',
    MODIFY COLUMN usuario_id INT NOT NULL COMMENT 'Referencia al usuario propietario de la sesión (FK)',
    MODIFY COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Indica si la sesión está activa',
    MODIFY COLUMN fecha_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha y hora de inicio de la sesión',
    MODIFY COLUMN fecha_fin DATETIME NULL COMMENT 'Fecha y hora de fin de la sesión (NULL si está activa)',
    MODIFY COLUMN fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha y hora de creación del registro',
    MODIFY COLUMN fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha y hora de última modificación del registro';

-- =====================================================
-- Datos iniciales (opcional)
-- =====================================================

-- Insertar roles básicos del sistema
INSERT INTO rol (nombre, activo) VALUES 
('ADMIN', TRUE),
('USER', TRUE),
('GUEST', TRUE),
('GERENTE', TRUE),
('VENDEDOR', TRUE),
('INVENTARIO', TRUE),
('CONSULTOR', TRUE);

-- Insertar usuario administrador por defecto
INSERT INTO usuario (usuario, password, activo) VALUES 
('admin', '$2a$12$mNCUbRx1w1hTLmB/07lNmuHqbblsqsn3wzTxMY34Uz2w4TGpXC.4u', TRUE); -- password: password

-- Asignar rol ADMIN al usuario admin
INSERT INTO rol_asignado (id_usuario, id_rol, activo) VALUES 
(1, 1, TRUE);

-- =====================================================
-- Script completado exitosamente
-- =====================================================