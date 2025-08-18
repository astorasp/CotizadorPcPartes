-- =================================================================
-- DDL para Base de Datos: cotizador_pedidos_db
-- Microservicio: ms-cotizador-pedidos
-- =================================================================

-- Configurar UTF-8 explícitamente al inicio
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET CHARACTER SET utf8mb4;

-- Crear base de datos
CREATE DATABASE IF NOT EXISTS cotizador_pedidos_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE cotizador_pedidos_db;

-- =================================================================
-- TABLAS PRINCIPALES DEL DOMINIO PEDIDOS
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
    -- Campos de auditoria
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_tipo_componente) REFERENCES cotipo_componente(id),
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

-- Tabla de cotizaciones (necesaria para entidades JPA)
CREATE TABLE cocotizacion (
    folio INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    fecha VARCHAR(20) NOT NULL,
    impuestos DECIMAL(20,2) NOT NULL,
    subtotal DECIMAL(20,2) NOT NULL,
    total DECIMAL(20,2) NOT NULL
) ENGINE=InnoDB;

-- Tabla de detalles de cotización
CREATE TABLE codetalle_cotizacion (
    folio INT UNSIGNED NOT NULL,
    num_detalle INT UNSIGNED NOT NULL,
    cantidad INT UNSIGNED NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    id_componente VARCHAR(50) NOT NULL,
    precio_base DECIMAL(20,2) NOT NULL,
    PRIMARY KEY (folio, num_detalle),
    FOREIGN KEY (folio) REFERENCES cocotizacion(folio) ON DELETE CASCADE,
    FOREIGN KEY (id_componente) REFERENCES cocomponente(id_componente)
) ENGINE=InnoDB;

-- Tabla de proveedores
CREATE TABLE coproveedor (
    cve VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    razon_social VARCHAR(255) NOT NULL,
    -- Campos adicionales para gestión de proveedores
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    -- Campos de auditoria
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tabla de pedidos
CREATE TABLE copedido (
    num_pedido INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cve_proveedor VARCHAR(50) NOT NULL,
    fecha_emision DATE NOT NULL,
    fecha_entrega DATE NOT NULL,
    nivel_surtido INT NOT NULL,
    total DECIMAL(20,2) NOT NULL,
    -- Campos adicionales para gestión de pedidos
    estado_pedido ENUM('CREADO', 'ENVIADO', 'PARCIAL', 'COMPLETO', 'CANCELADO') DEFAULT 'CREADO',
    observaciones TEXT,
    folio_cotizacion INT UNSIGNED, -- Referencia a la cotización origen
    -- Campos de auditoria
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cve_proveedor) REFERENCES coproveedor(cve)
) ENGINE=InnoDB;

-- Tabla de detalles de pedido
CREATE TABLE codetalle_pedido (
    num_pedido INT UNSIGNED NOT NULL,
    num_detalle INT UNSIGNED NOT NULL,
    cantidad INT UNSIGNED NOT NULL,
    precio_unitario DECIMAL(20,2) NOT NULL,
    total_cotizado DECIMAL(20,2) NOT NULL,
    id_componente VARCHAR(50) NOT NULL,
    -- Campos adicionales para control de surtido
    cantidad_surtida INT UNSIGNED DEFAULT 0,
    cantidad_pendiente INT UNSIGNED GENERATED ALWAYS AS (cantidad - cantidad_surtida) STORED,
    fecha_surtido DATE NULL,
    PRIMARY KEY (num_pedido, num_detalle),
    FOREIGN KEY (num_pedido) REFERENCES copedido(num_pedido) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =================================================================
-- TABLAS DE CACHE LOCAL (DATOS REPLICADOS)
-- =================================================================

-- Cache de componentes (replicado desde ms-cotizador-componentes)
CREATE TABLE cocomponente_cache (
    id_componente VARCHAR(50) PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    precio_base DECIMAL(20,2) NOT NULL,
    costo DECIMAL(20,2) NOT NULL,
    id_tipo_componente SMALLINT UNSIGNED NOT NULL,
    -- Campos de control de cache
    cache_version BIGINT DEFAULT 1,
    cache_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    cache_status ENUM('ACTIVE', 'STALE', 'DELETED') DEFAULT 'ACTIVE'
) ENGINE=InnoDB;

-- Cache de cotizaciones (replicado desde ms-cotizador-cotizaciones)
CREATE TABLE cocotizacion_cache (
    folio INT UNSIGNED PRIMARY KEY,
    fecha VARCHAR(20) NOT NULL,
    subtotal DECIMAL(20,2) NOT NULL,
    impuestos DECIMAL(20,2) NOT NULL,
    total DECIMAL(20,2) NOT NULL,
    algoritmo_cotizacion ENUM('COTIZADOR_A', 'COTIZADOR_B') DEFAULT 'COTIZADOR_A',
    -- Campos de control de cache
    cache_version BIGINT DEFAULT 1,
    cache_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    cache_status ENUM('ACTIVE', 'STALE', 'DELETED') DEFAULT 'ACTIVE'
) ENGINE=InnoDB;

-- Cache de detalles de cotización
CREATE TABLE codetalle_cotizacion_cache (
    folio INT UNSIGNED NOT NULL,
    num_detalle INT UNSIGNED NOT NULL,
    cantidad INT UNSIGNED NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    id_componente VARCHAR(50) NOT NULL,
    precio_base DECIMAL(20,2) NOT NULL,
    precio_con_promocion DECIMAL(20,2) NOT NULL DEFAULT 0.00,
    subtotal_detalle DECIMAL(20,2) NOT NULL DEFAULT 0.00,
    -- Campos de control de cache
    cache_version BIGINT DEFAULT 1,
    cache_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    cache_status ENUM('ACTIVE', 'STALE', 'DELETED') DEFAULT 'ACTIVE',
    PRIMARY KEY (folio, num_detalle),
    FOREIGN KEY (folio) REFERENCES cocotizacion_cache(folio) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =================================================================
-- TABLAS PARA PATRÓN SAGA
-- =================================================================

-- Tabla para tracking de transacciones distribuidas (SAGA Pattern)
CREATE TABLE saga_transaction (
    saga_id VARCHAR(100) PRIMARY KEY,
    saga_type ENUM('CREATE_PEDIDO', 'UPDATE_PEDIDO', 'CANCEL_PEDIDO') NOT NULL,
    saga_status ENUM('STARTED', 'COMPLETED', 'COMPENSATING', 'FAILED') DEFAULT 'STARTED',
    current_step VARCHAR(50) NOT NULL,
    total_steps INT NOT NULL,
    payload JSON NOT NULL,
    -- Tracking de tiempo
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tabla para pasos individuales de SAGA
CREATE TABLE saga_step (
    saga_id VARCHAR(100) NOT NULL,
    step_number INT NOT NULL,
    step_name VARCHAR(50) NOT NULL,
    step_status ENUM('PENDING', 'COMPLETED', 'COMPENSATED', 'FAILED') DEFAULT 'PENDING',
    service_name VARCHAR(50) NOT NULL,
    action_type ENUM('COMMAND', 'COMPENSATION') NOT NULL,
    request_payload JSON,
    response_payload JSON,
    error_message TEXT,
    -- Tracking de tiempo
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    PRIMARY KEY (saga_id, step_number),
    FOREIGN KEY (saga_id) REFERENCES saga_transaction(saga_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =================================================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- =================================================================

-- Índices para tablas principales
CREATE INDEX idx_componente_tipo ON cocomponente (id_tipo_componente);
CREATE INDEX idx_promocion ON cocomponente (id_promocion);
CREATE INDEX idx_pcpartes_pc ON copc_parte (id_pc);
CREATE INDEX idx_detalle_cotizacion_cotizacion ON codetalle_cotizacion (folio, num_detalle);
CREATE INDEX idx_detalle_pedido_pedido ON codetalle_pedido (num_pedido);
CREATE INDEX idx_detalle_promocion_promocion ON codetalle_promocion (id_promocion);
CREATE INDEX idx_proveedor_nombre ON coproveedor (nombre);
CREATE INDEX idx_proveedor_activo ON coproveedor (activo);
CREATE INDEX idx_pedido_proveedor ON copedido (cve_proveedor);
CREATE INDEX idx_pedido_fecha_emision ON copedido (fecha_emision);
CREATE INDEX idx_pedido_fecha_entrega ON copedido (fecha_entrega);
CREATE INDEX idx_pedido_estado ON copedido (estado_pedido);
CREATE INDEX idx_pedido_cotizacion ON copedido (folio_cotizacion);
CREATE INDEX idx_detalle_pedido_componente ON codetalle_pedido (id_componente);
CREATE INDEX idx_detalle_pedido_fecha_surtido ON codetalle_pedido (fecha_surtido);

-- Índices para tablas de cache
CREATE INDEX idx_componente_cache_timestamp ON cocomponente_cache (cache_timestamp);
CREATE INDEX idx_componente_cache_status ON cocomponente_cache (cache_status);
CREATE INDEX idx_cotizacion_cache_timestamp ON cocotizacion_cache (cache_timestamp);
CREATE INDEX idx_cotizacion_cache_status ON cocotizacion_cache (cache_status);

-- Índices para SAGA
CREATE INDEX idx_saga_status ON saga_transaction (saga_status);
CREATE INDEX idx_saga_type ON saga_transaction (saga_type);
CREATE INDEX idx_saga_started_at ON saga_transaction (started_at);
CREATE INDEX idx_saga_step_status ON saga_step (step_status);
CREATE INDEX idx_saga_step_service ON saga_step (service_name);

-- =================================================================
-- TRIGGERS PARA MANTENIMIENTO
-- =================================================================

DELIMITER $$

-- Trigger para actualizar estado del pedido basado en surtido
CREATE TRIGGER update_pedido_estado
    AFTER UPDATE ON codetalle_pedido
    FOR EACH ROW
BEGIN
    DECLARE total_cantidad INT DEFAULT 0;
    DECLARE total_surtido INT DEFAULT 0;
    DECLARE nuevo_estado ENUM('CREADO', 'ENVIADO', 'PARCIAL', 'COMPLETO', 'CANCELADO');
    
    -- Calcular totales del pedido
    SELECT SUM(cantidad), SUM(cantidad_surtida)
    INTO total_cantidad, total_surtido
    FROM codetalle_pedido
    WHERE num_pedido = NEW.num_pedido;
    
    -- Determinar nuevo estado
    IF total_surtido = 0 THEN
        SET nuevo_estado = 'ENVIADO';
    ELSEIF total_surtido = total_cantidad THEN
        SET nuevo_estado = 'COMPLETO';
    ELSE
        SET nuevo_estado = 'PARCIAL';
    END IF;
    
    -- Actualizar estado del pedido
    UPDATE copedido 
    SET estado_pedido = nuevo_estado,
        updated_at = CURRENT_TIMESTAMP
    WHERE num_pedido = NEW.num_pedido;
END$$

-- Trigger para limpiar cache obsoleto
CREATE TRIGGER cleanup_stale_cache_pedidos
    AFTER INSERT ON cocomponente_cache
    FOR EACH ROW
BEGIN
    -- Limpiar registros marcados como DELETED y más antiguos de 24 horas
    DELETE FROM cocomponente_cache 
    WHERE cache_status = 'DELETED' 
    AND cache_timestamp < DATE_SUB(NOW(), INTERVAL 24 HOUR);
    
    DELETE FROM cocotizacion_cache 
    WHERE cache_status = 'DELETED' 
    AND cache_timestamp < DATE_SUB(NOW(), INTERVAL 24 HOUR);
END$$

DELIMITER ;

-- =================================================================
-- CONFIGURACIÓN INICIAL
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

-- Insertar proveedor por defecto
INSERT INTO coproveedor (cve, nombre, razon_social, telefono, email, activo) 
VALUES ('PROV001', 'Proveedor General', 'Proveedor General S.A. de C.V.', '555-0123', 'contacto@proveedor.com', TRUE);