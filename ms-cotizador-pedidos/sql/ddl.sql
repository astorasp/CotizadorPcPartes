-- =================================================================
-- DDL para Base de Datos: cotizador_pedidos_db
-- Microservicio: ms-cotizador-pedidos
-- =================================================================

-- TestContainers ya creó la base de datos, no necesitamos crear ni usar
-- Solo crear las tablas directamente

-- =================================================================
-- TABLAS PRINCIPALES DEL DOMINIO PEDIDOS
-- =================================================================

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
-- DATOS REPLICADOS DE OTROS MICROSERVICIOS
-- =================================================================

-- Los datos de componentes y cotizaciones se obtienen vía Kafka
-- o mediante llamadas REST a los microservicios correspondientes
-- No se mantienen tablas cache locales para simplificar la arquitectura


-- =================================================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- =================================================================

-- Índices para tablas principales
CREATE INDEX idx_proveedor_nombre ON coproveedor (nombre);
CREATE INDEX idx_proveedor_activo ON coproveedor (activo);
CREATE INDEX idx_pedido_proveedor ON copedido (cve_proveedor);
CREATE INDEX idx_pedido_fecha_emision ON copedido (fecha_emision);
CREATE INDEX idx_pedido_fecha_entrega ON copedido (fecha_entrega);
CREATE INDEX idx_pedido_estado ON copedido (estado_pedido);
CREATE INDEX idx_pedido_cotizacion ON copedido (folio_cotizacion);
CREATE INDEX idx_detalle_pedido_componente ON codetalle_pedido (id_componente);
CREATE INDEX idx_detalle_pedido_fecha_surtido ON codetalle_pedido (fecha_surtido);

-- Sin índices para cache ya que no hay tablas cache


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

-- Trigger para auditoria de cambios en pedidos (futuro)

DELIMITER ;

-- =================================================================
-- CONFIGURACIÓN INICIAL
-- =================================================================

-- Insertar proveedor por defecto
INSERT INTO coproveedor (cve, nombre, razon_social, telefono, email, activo) 
VALUES ('PROV001', 'Proveedor General', 'Proveedor General S.A. de C.V.', '555-0123', 'contacto@proveedor.com', TRUE);