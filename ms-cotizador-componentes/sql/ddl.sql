-- Configurar UTF-8 explícitamente al inicio
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET CHARACTER SET utf8mb4;

-- Usar base de datos creada por docker-compose
USE cotizador_componentes_db;

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
    llevent INT,
    nombre VARCHAR(100) NOT NULL,
    paguen INT,
    porc_dcto_plano DOUBLE,
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

-- ===================================================================
-- MICROSERVICIO: ms-cotizador-componentes
-- RESPONSABILIDAD: Gestión de componentes, tipos, promociones y PCs
-- ===================================================================

-- Crear índices para mejorar el rendimiento
CREATE INDEX idx_componente_tipo ON cocomponente (id_tipo_componente);
CREATE INDEX idx_promocion ON cocomponente (id_promocion);
CREATE INDEX idx_pcpartes_pc ON copc_parte (id_pc);
CREATE INDEX idx_detalle_promocion_promocion ON codetalle_promocion (id_promocion);