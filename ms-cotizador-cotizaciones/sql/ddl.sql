-- TestContainers ya creó la base de datos, no necesitamos crear ni usar
-- Solo crear las tablas directamente

-- Tabla para tipos de componentes
CREATE TABLE IF NOT EXISTS cotipo_componente (
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- Tabla de promociones
CREATE TABLE IF NOT EXISTS copromocion (
    id_promocion INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    vigencia_desde DATE NOT NULL,
    vigencia_hasta DATE NOT NULL
) ENGINE=InnoDB;

-- Tabla de detalles de promoción
CREATE TABLE IF NOT EXISTS codetalle_promocion (
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
CREATE TABLE IF NOT EXISTS codetalle_prom_dscto_x_cant (
    num_dscto INT UNSIGNED NOT NULL,
    cantidad INT NOT NULL,
    dscto DOUBLE NOT NULL,
    num_det_promocion INT UNSIGNED NOT NULL,
    num_promocion INT UNSIGNED NOT NULL,
    PRIMARY KEY (num_dscto,num_det_promocion, num_promocion),
    FOREIGN KEY (num_det_promocion) REFERENCES codetalle_promocion(id_detalle_promocion) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabla principal de componentes
CREATE TABLE IF NOT EXISTS cocomponente (
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
CREATE TABLE IF NOT EXISTS copc_parte (
    id_pc VARCHAR(50) NOT NULL,
    id_componente VARCHAR(50) NOT NULL,
    PRIMARY KEY (id_pc, id_componente),
    FOREIGN KEY (id_pc) REFERENCES cocomponente(id_componente),
    FOREIGN KEY (id_componente) REFERENCES cocomponente(id_componente) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabla de cotizaciones
CREATE TABLE IF NOT EXISTS cocotizacion (
    folio INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    fecha VARCHAR(20) NOT NULL,    
    impuestos DECIMAL(20,2) NOT NULL,
    subtotal DECIMAL(20,2) NOT NULL,
    total DECIMAL(20,2) NOT NULL
) ENGINE=InnoDB;

-- Tabla de detalles de cotización
CREATE TABLE IF NOT EXISTS codetalle_cotizacion (
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

-- Crear índices para mejorar el rendimiento
CREATE INDEX idx_componente_tipo ON cocomponente (id_tipo_componente);
CREATE INDEX idx_promocion ON cocomponente (id_promocion);
CREATE INDEX idx_pcpartes_pc ON copc_parte (id_pc);
CREATE INDEX idx_detalle_cotizacion_cotizacion ON codetalle_cotizacion (folio, num_detalle);
CREATE INDEX idx_detalle_promocion_promocion ON codetalle_promocion (id_promocion);