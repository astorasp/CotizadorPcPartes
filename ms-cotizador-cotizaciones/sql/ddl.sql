-- =======================================================================
-- DDL MySQL ms-cotizador-cotizaciones v2.0 - Microservicio de Cotizaciones
-- =======================================================================
-- Arquitectura: Spring Boot 3.5.3 + JPA + MySQL 8.4.4
-- Patrones implementados:
--   * Domain-Driven Design con lógica de negocio en entidades de dominio  
--   * Strategy Pattern (CotizadorA/CotizadorB)
--   * Bridge Pattern (Sistema de impuestos por país)
--   * Decorator Pattern (Promociones apilables)
-- =======================================================================

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
-- TABLAS DE DEPENDENCIAS (NECESARIAS PARA COTIZACIONES)
-- =================================================================

-- Tabla de tipos de componente
CREATE TABLE cotipo_componente (
    id_tipo_componente SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tabla de componentes (vista desde cotizaciones)
CREATE TABLE cocomponente (
    id_componente VARCHAR(50) PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL,
    precio DECIMAL(20,2) NOT NULL,
    id_tipo_componente SMALLINT UNSIGNED NOT NULL,
    id_promocion INT UNSIGNED NULL,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_tipo_componente) REFERENCES cotipo_componente(id_tipo_componente),
    FOREIGN KEY (id_promocion) REFERENCES copromocion(id_promocion) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Tabla de partes de PC (composición de componentes)
CREATE TABLE copc_parte (
    id_pc VARCHAR(50) NOT NULL,
    id_componente VARCHAR(50) NOT NULL,
    cantidad INT UNSIGNED NOT NULL DEFAULT 1,
    posicion SMALLINT UNSIGNED,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_pc, id_componente),
    FOREIGN KEY (id_componente) REFERENCES cocomponente(id_componente) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =================================================================
-- TABLAS DE PROMOCIONES (PATRÓN DECORATOR)
-- =================================================================

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

-- =================================================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- =================================================================

-- Índices para tablas principales
CREATE INDEX idx_cotizacion_fecha ON cocotizacion (fecha);
CREATE INDEX idx_cotizacion_algoritmo ON cocotizacion (algoritmo_cotizacion);
CREATE INDEX idx_cotizacion_pais ON cocotizacion (pais_impuestos);
CREATE INDEX idx_detalle_cotizacion_componente ON codetalle_cotizacion (id_componente);

-- Índices para promociones
CREATE INDEX idx_promocion_vigencia ON copromocion (vigencia_desde, vigencia_hasta);
CREATE INDEX idx_detalle_promocion_promocion ON codetalle_promocion (id_promocion);

-- Índices para dependencias
CREATE INDEX idx_componente_tipo ON cocomponente (id_tipo_componente);
CREATE INDEX idx_componente_promocion ON cocomponente (id_promocion);
CREATE INDEX idx_componente_activo ON cocomponente (activo);
CREATE INDEX idx_pc_parte_pc ON copc_parte (id_pc);
CREATE INDEX idx_pc_parte_componente ON copc_parte (id_componente);