-- =================================================================
-- DDL para Base de Datos: cotizador_componentes_db
-- Microservicio: ms-cotizador-componentes
-- =================================================================

-- Configurar UTF-8 explícitamente al inicio
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET CHARACTER SET utf8mb4;

-- Crear base de datos
CREATE DATABASE IF NOT EXISTS cotizador_componentes_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE cotizador_componentes_db;

-- =================================================================
-- TABLAS PRINCIPALES DEL DOMINIO COMPONENTES
-- =================================================================

-- Tabla para tipos de componentes
CREATE TABLE cotipo_componente (
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- Tabla de promociones
CREATE TABLE copromocion (
    id_promocion INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    vigencia_desde DATE NOT NULL,
    vigencia_hasta DATE NOT NULL
) ENGINE=InnoDB;

-- Tabla de detalles de promoción
CREATE TABLE codetalle_promocion (
    id_detalle_promocion INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    es_base BOOLEAN NOT NULL DEFAULT FALSE,
    llevent INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    paguen INT NOT NULL,
    porc_dcto_plano DOUBLE NOT NULL,
    tipo_prom_acumulable VARCHAR(50),
    tipo_prom_base VARCHAR(50),
    id_promocion INT UNSIGNED NOT NULL,
    FOREIGN KEY (id_promocion) REFERENCES copromocion(id_promocion) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabla de detalles de promoción por documento y cantidad
CREATE TABLE codetalle_prom_dscto_x_cant (
    num_dscto INT UNSIGNED NOT NULL,
    cantidad INT NOT NULL,
    dscto DOUBLE NOT NULL,
    num_det_promocion INT UNSIGNED NOT NULL,
    num_promocion INT UNSIGNED NOT NULL,
    PRIMARY KEY (num_dscto,num_det_promocion, num_promocion),
    FOREIGN KEY (num_det_promocion) REFERENCES codetalle_promocion(id_detalle_promocion) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabla principal de componentes
CREATE TABLE cocomponente (
    id_componente VARCHAR(50) PRIMARY KEY,
    capacidad_alm VARCHAR(50),
    costo DECIMAL(20,2) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    marca VARCHAR(100) NOT NULL,
    memoria VARCHAR(50),
    modelo VARCHAR(100) NOT NULL,
    precio_base DECIMAL(20,2) NOT NULL,
    id_tipo_componente SMALLINT UNSIGNED NOT NULL,
    id_promocion INT UNSIGNED NOT NULL,
    FOREIGN KEY (id_tipo_componente) REFERENCES cotipo_componente(id),
    FOREIGN KEY (id_promocion) REFERENCES copromocion(id_promocion)
) ENGINE=InnoDB;

-- Tabla para la relación composite (PC -> componentes)
CREATE TABLE copc_parte (
    id_pc VARCHAR(50) NOT NULL,
    id_componente VARCHAR(50) NOT NULL,
    PRIMARY KEY (id_pc, id_componente),
    FOREIGN KEY (id_pc) REFERENCES cocomponente(id_componente),
    FOREIGN KEY (id_componente) REFERENCES cocomponente(id_componente) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =================================================================
-- TABLAS DE SINCRONIZACIÓN CON OTROS MICROSERVICIOS
-- =================================================================

-- Tabla de sincronización de proveedores (replicada desde ms-cotizador-pedidos)
CREATE TABLE coproveedor_sync (
    cve VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    razon_social VARCHAR(255) NOT NULL,
    -- Campos de control de sincronización
    sync_version BIGINT DEFAULT 1,
    sync_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    sync_status ENUM('ACTIVE', 'DELETED') DEFAULT 'ACTIVE'
) ENGINE=InnoDB;

-- =================================================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- =================================================================

-- Crear índices para mejorar el rendimiento
CREATE INDEX idx_componente_tipo ON cocomponente (id_tipo_componente);
CREATE INDEX idx_promocion ON cocomponente (id_promocion);
CREATE INDEX idx_pcpartes_pc ON copc_parte (id_pc);
CREATE INDEX idx_detalle_promocion_promocion ON codetalle_promocion (id_promocion);

-- Índices para tablas de sincronización
CREATE INDEX idx_proveedor_sync_timestamp ON coproveedor_sync (sync_timestamp);
CREATE INDEX idx_proveedor_sync_status ON coproveedor_sync (sync_status);

-- =================================================================
-- CONFIGURACIÓN DE DEBEZIUM CDC
-- =================================================================

-- Habilitar el binlog para replicación
-- (Esta configuración se maneja en el nivel de instancia MySQL)

-- =================================================================
-- DATOS INICIALES BÁSICOS
-- =================================================================

-- Insertar tipos de componentes básicos
INSERT INTO cotipo_componente (nombre) VALUES 
('Procesador'),
('Tarjeta de Video'),
('Memoria RAM'),
('Disco Duro'),
('Monitor'),
('Motherboard'),
('Fuente de Poder'),
('Gabinete'),
('Teclado'),
('Mouse');

-- Insertar promoción por defecto (sin descuento)
INSERT INTO copromocion (descripcion, nombre, vigencia_desde, vigencia_hasta) 
VALUES ('Sin promoción aplicable', 'Sin Descuento', '2020-01-01', '2030-12-31');

-- Insertar detalle de promoción sin descuento
INSERT INTO codetalle_promocion (es_base, llevent, nombre, paguen, porc_dcto_plano, tipo_prom_base, id_promocion)
VALUES (TRUE, 1, 'Sin Descuento', 1, 0.0, 'SIN_DESCUENTO', 1);