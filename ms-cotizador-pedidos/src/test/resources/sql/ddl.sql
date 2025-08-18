-- =================================================================
-- DDL para Base de Datos: cotizador_pedidos_db
-- Microservicio: ms-cotizador-pedidos
-- =================================================================

-- TestContainers ya creó la base de datos, no necesitamos crear ni usar
-- Solo crear las tablas directamente

-- =================================================================
-- TABLAS BÁSICAS (SIN DEPENDENCIAS)
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

-- Tabla de tipos de componente
CREATE TABLE cotipo_componente (
    id_tipo_componente INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    -- Campos de auditoria
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =================================================================
-- TABLAS CON DEPENDENCIAS DE PRIMER NIVEL
-- =================================================================

-- Tabla de componentes locales
CREATE TABLE cocomponente (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(20,2) NOT NULL,
    descuento DECIMAL(20,2) DEFAULT 0.00,
    id_tipo_componente INT UNSIGNED NOT NULL,
    -- Campos de auditoria
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_tipo_componente) REFERENCES cotipo_componente(id_tipo_componente)
) ENGINE=InnoDB;

-- Tabla de cotizaciones locales
CREATE TABLE cocotizacion (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    fecha_creacion DATE NOT NULL,
    subtotal DECIMAL(20,2) NOT NULL,
    impuestos DECIMAL(20,2) NOT NULL,
    total DECIMAL(20,2) NOT NULL,
    pais VARCHAR(10) NOT NULL,
    -- Campos de auditoria
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tabla de detalles de cotización locales
CREATE TABLE codetalle_cotizacion (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cantidad INT UNSIGNED NOT NULL,
    precio_unitario DECIMAL(20,2) NOT NULL,
    subtotal DECIMAL(20,2) NOT NULL,
    cotizacion_id INT UNSIGNED NOT NULL,
    componente_id INT UNSIGNED NOT NULL,
    -- Campos de auditoria
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cotizacion_id) REFERENCES cocotizacion(id) ON DELETE CASCADE,
    FOREIGN KEY (componente_id) REFERENCES cocomponente(id)
) ENGINE=InnoDB;

-- =================================================================
-- TABLAS CON DEPENDENCIAS DE SEGUNDO NIVEL
-- =================================================================

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
    id_componente INT UNSIGNED NOT NULL,
    -- Campos adicionales para control de surtido
    cantidad_surtida INT UNSIGNED DEFAULT 0,
    cantidad_pendiente INT UNSIGNED GENERATED ALWAYS AS (cantidad - cantidad_surtida) STORED,
    fecha_surtido DATE NULL,
    PRIMARY KEY (num_pedido, num_detalle),
    FOREIGN KEY (num_pedido) REFERENCES copedido(num_pedido) ON DELETE CASCADE,
    FOREIGN KEY (id_componente) REFERENCES cocomponente(id)
) ENGINE=InnoDB;



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

-- Índices para tablas locales de datos
CREATE INDEX idx_tipo_componente_tipo ON cotipo_componente (tipo);
CREATE INDEX idx_componente_marca ON cocomponente (marca);
CREATE INDEX idx_componente_tipo ON cocomponente (id_tipo_componente);
CREATE INDEX idx_cotizacion_fecha ON cocotizacion (fecha_creacion);
CREATE INDEX idx_cotizacion_pais ON cocotizacion (pais);
CREATE INDEX idx_detalle_cotizacion_componente ON codetalle_cotizacion (componente_id);
CREATE INDEX idx_detalle_cotizacion_cotizacion ON codetalle_cotizacion (cotizacion_id);



-- =================================================================
-- TRIGGERS PARA MANTENIMIENTO
-- =================================================================

-- Los triggers no se incluyen en tests de integración
-- para simplificar la configuración de TestContainers
-- Los triggers se configuran en el DDL de producción

-- =================================================================
-- CONFIGURACIÓN INICIAL COMPLETADA
-- Los datos iniciales están en dml.sql
-- =================================================================