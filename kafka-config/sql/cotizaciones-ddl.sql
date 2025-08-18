-- =================================================================
-- DDL para Base de Datos: cotizador_cotizaciones_db
-- Microservicio: ms-cotizador-cotizaciones
-- =================================================================

-- Configurar UTF-8 explícitamente al inicio
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET CHARACTER SET utf8mb4;

-- Crear base de datos
CREATE DATABASE IF NOT EXISTS cotizador_cotizaciones_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE cotizador_cotizaciones_db;

-- =================================================================
-- TABLAS PRINCIPALES DEL DOMINIO COTIZACIONES
-- =================================================================

-- Tabla para tipos de componentes (necesaria para entidades JPA)
CREATE TABLE cotipo_componente (
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- Tabla de promociones (necesaria para entidades JPA)
CREATE TABLE copromocion (
    id_promocion INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    vigencia_desde DATE NOT NULL,
    vigencia_hasta DATE NOT NULL
) ENGINE=InnoDB;

-- Tabla de detalles de promoción (necesaria para entidades JPA)
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

-- Tabla de detalles de promoción por documento y cantidad (necesaria para entidades JPA)
CREATE TABLE codetalle_prom_dscto_x_cant (
    num_dscto INT UNSIGNED NOT NULL,
    cantidad INT NOT NULL,
    dscto DOUBLE NOT NULL,
    num_det_promocion INT UNSIGNED NOT NULL,
    num_promocion INT UNSIGNED NOT NULL,
    PRIMARY KEY (num_dscto, num_det_promocion, num_promocion),
    FOREIGN KEY (num_det_promocion) REFERENCES codetalle_promocion(id_detalle_promocion) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabla principal de componentes (necesaria para entidades JPA)
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

-- Tabla de cotizaciones
CREATE TABLE cocotizacion (
    folio INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    fecha VARCHAR(20) NOT NULL,    
    impuestos DECIMAL(20,2) NOT NULL,
    subtotal DECIMAL(20,2) NOT NULL,
    total DECIMAL(20,2) NOT NULL,
    -- Campos adicionales para el dominio de cotizaciones
    algoritmo_cotizacion ENUM('COTIZADOR_A', 'COTIZADOR_B') DEFAULT 'COTIZADOR_A',
    pais_impuestos ENUM('MEXICO', 'USA', 'CANADA') DEFAULT 'MEXICO',
    -- Campos de auditoria
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tabla de detalles de cotización
CREATE TABLE codetalle_cotizacion (
    folio INT UNSIGNED NOT NULL,
    num_detalle INT UNSIGNED NOT NULL,
    cantidad INT UNSIGNED NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    id_componente VARCHAR(50) NOT NULL,
    precio_base DECIMAL(20,2) NOT NULL,
    -- Campos adicionales para cálculos
    precio_con_promocion DECIMAL(20,2) NOT NULL DEFAULT 0.00,
    descuento_aplicado DECIMAL(20,2) NOT NULL DEFAULT 0.00,
    subtotal_detalle DECIMAL(20,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (folio, num_detalle),
    FOREIGN KEY (folio) REFERENCES cocotizacion(folio) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =================================================================
-- TABLAS DE CACHE LOCAL (DATOS REPLICADOS)
-- =================================================================

-- Cache de componentes (replicado desde ms-cotizador-componentes)
CREATE TABLE cocomponente_cache (
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
    -- Campos de control de cache
    cache_version BIGINT DEFAULT 1,
    cache_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    cache_status ENUM('ACTIVE', 'STALE', 'DELETED') DEFAULT 'ACTIVE'
) ENGINE=InnoDB;

-- Cache de tipos de componentes
CREATE TABLE cotipo_componente_cache (
    id SMALLINT UNSIGNED PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    -- Campos de control de cache
    cache_version BIGINT DEFAULT 1,
    cache_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    cache_status ENUM('ACTIVE', 'STALE', 'DELETED') DEFAULT 'ACTIVE'
) ENGINE=InnoDB;

-- Cache de promociones (replicado desde ms-cotizador-componentes)
CREATE TABLE copromocion_cache (
    id_promocion INT UNSIGNED PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    vigencia_desde DATE NOT NULL,
    vigencia_hasta DATE NOT NULL,
    -- Campos de control de cache
    cache_version BIGINT DEFAULT 1,
    cache_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    cache_status ENUM('ACTIVE', 'STALE', 'DELETED') DEFAULT 'ACTIVE'
) ENGINE=InnoDB;

-- Cache de detalles de promoción
CREATE TABLE codetalle_promocion_cache (
    id_detalle_promocion INT UNSIGNED PRIMARY KEY,
    es_base BOOLEAN NOT NULL DEFAULT FALSE,
    llevent INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    paguen INT NOT NULL,
    porc_dcto_plano DOUBLE NOT NULL,
    tipo_prom_acumulable VARCHAR(50),
    tipo_prom_base VARCHAR(50),
    id_promocion INT UNSIGNED NOT NULL,
    -- Campos de control de cache
    cache_version BIGINT DEFAULT 1,
    cache_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    cache_status ENUM('ACTIVE', 'STALE', 'DELETED') DEFAULT 'ACTIVE',
    FOREIGN KEY (id_promocion) REFERENCES copromocion_cache(id_promocion) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Cache de escalas de descuento
CREATE TABLE codetalle_prom_dscto_x_cant_cache (
    num_dscto INT UNSIGNED NOT NULL,
    cantidad INT NOT NULL,
    dscto DOUBLE NOT NULL,
    num_det_promocion INT UNSIGNED NOT NULL,
    num_promocion INT UNSIGNED NOT NULL,
    -- Campos de control de cache
    cache_version BIGINT DEFAULT 1,
    cache_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    cache_status ENUM('ACTIVE', 'STALE', 'DELETED') DEFAULT 'ACTIVE',
    PRIMARY KEY (num_dscto, num_det_promocion, num_promocion),
    FOREIGN KEY (num_det_promocion) REFERENCES codetalle_promocion_cache(id_detalle_promocion) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =================================================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- =================================================================

-- Índices para tablas principales
CREATE INDEX idx_cotizacion_fecha ON cocotizacion (fecha);
CREATE INDEX idx_cotizacion_algoritmo ON cocotizacion (algoritmo_cotizacion);
CREATE INDEX idx_cotizacion_pais ON cocotizacion (pais_impuestos);
CREATE INDEX idx_detalle_cotizacion_componente ON codetalle_cotizacion (id_componente);

-- Índices para tablas de cache
CREATE INDEX idx_componente_cache_tipo ON cocomponente_cache (id_tipo_componente);
CREATE INDEX idx_componente_cache_promocion ON cocomponente_cache (id_promocion);
CREATE INDEX idx_componente_cache_timestamp ON cocomponente_cache (cache_timestamp);
CREATE INDEX idx_componente_cache_status ON cocomponente_cache (cache_status);

CREATE INDEX idx_promocion_cache_vigencia ON copromocion_cache (vigencia_desde, vigencia_hasta);
CREATE INDEX idx_promocion_cache_timestamp ON copromocion_cache (cache_timestamp);
CREATE INDEX idx_promocion_cache_status ON copromocion_cache (cache_status);

CREATE INDEX idx_detalle_promocion_cache_promocion ON codetalle_promocion_cache (id_promocion);
CREATE INDEX idx_detalle_promocion_cache_timestamp ON codetalle_promocion_cache (cache_timestamp);

-- =================================================================
-- TRIGGERS PARA MANTENIMIENTO DE CACHE
-- =================================================================

-- Trigger para limpiar cache obsoleto automáticamente
DELIMITER $$

CREATE TRIGGER cleanup_stale_cache_componentes
    AFTER INSERT ON cocomponente_cache
    FOR EACH ROW
BEGIN
    -- Limpiar registros marcados como DELETED y más antiguos de 24 horas
    DELETE FROM cocomponente_cache 
    WHERE cache_status = 'DELETED' 
    AND cache_timestamp < DATE_SUB(NOW(), INTERVAL 24 HOUR);
END$$

CREATE TRIGGER cleanup_stale_cache_promociones
    AFTER INSERT ON copromocion_cache
    FOR EACH ROW
BEGIN
    -- Limpiar registros marcados como DELETED y más antiguos de 24 horas
    DELETE FROM copromocion_cache 
    WHERE cache_status = 'DELETED' 
    AND cache_timestamp < DATE_SUB(NOW(), INTERVAL 24 HOUR);
END$$

DELIMITER ;

-- =================================================================
-- CONFIGURACIÓN INICIAL
-- =================================================================

-- Configuración inicial de cache vacío
-- Los datos se llenarán automáticamente via Kafka CDC