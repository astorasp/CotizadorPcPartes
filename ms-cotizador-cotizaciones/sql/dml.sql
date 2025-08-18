-- =======================================================================
-- DML MySQL ms-cotizador-cotizaciones v2.0 - Datos de Prueba
-- =======================================================================
-- Microservicio especializado en cotizaciones y promociones
-- =======================================================================

-- Configurar UTF-8 explícitamente
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET CHARACTER SET utf8mb4;

USE cotizador_cotizaciones_db;

-- =================================================================
-- DATOS DE DEPENDENCIAS (TIPOS Y COMPONENTES)
-- =================================================================

-- Insertar tipos de componente
INSERT INTO cotipo_componente (id_tipo_componente, nombre, descripcion) VALUES
(1, 'MONITOR', 'Monitores y pantallas de computadora'),
(2, 'GPU', 'Tarjetas gráficas y de video'),
(3, 'HDD', 'Discos duros y almacenamiento'),
(4, 'SSD', 'Unidades de estado sólido'),
(5, 'CPU', 'Procesadores de computadora'),
(6, 'RAM', 'Memoria RAM y módulos');

-- Insertar componentes de prueba
INSERT INTO cocomponente (id_componente, descripcion, precio, id_tipo_componente, id_promocion, activo) VALUES
-- Monitores
('MON001', 'Monitor LED 24 pulgadas Full HD', 300.00, 1, 2, TRUE),
('MON002', 'Monitor LED 27 pulgadas 4K', 450.00, 1, 2, TRUE),
('MON003', 'Monitor LED 21 pulgadas HD', 200.00, 1, 2, TRUE),

-- Tarjetas gráficas
('GPU001', 'Tarjeta Video NVIDIA GTX 1050 4GB', 180.00, 2, 3, TRUE),
('GPU002', 'Tarjeta Video NVIDIA RTX 3060 12GB', 350.00, 2, 3, TRUE),
('GPU003', 'Tarjeta Video NVIDIA GTX 1030 2GB', 150.00, 2, 3, TRUE),

-- Discos duros
('HDD001', 'Disco Duro SATA 1TB 7200RPM', 250.00, 3, 5, TRUE),
('HDD002', 'Disco SSD 500GB SATA III', 350.00, 4, 1, TRUE),
('HDD003', 'Disco Duro SATA 500GB 5400RPM', 150.00, 3, 5, TRUE),

-- CPUs
('CPU001', 'Procesador Intel Core i5-12400', 200.00, 5, 4, TRUE),
('CPU002', 'Procesador AMD Ryzen 5 5600X', 250.00, 5, 4, TRUE),

-- RAM
('RAM001', 'Memoria DDR4 8GB 3200MHz', 80.00, 6, 4, TRUE),
('RAM002', 'Memoria DDR4 16GB 3600MHz', 150.00, 6, 4, TRUE);

-- Insertar PCs de prueba con sus partes
INSERT INTO copc_parte (id_pc, id_componente, cantidad, posicion) VALUES
-- PC Gaming Básico
('PC001', 'CPU001', 1, 1),
('PC001', 'RAM001', 2, 2),
('PC001', 'GPU001', 1, 3),
('PC001', 'HDD001', 1, 4),
('PC001', 'MON001', 1, 5),

-- PC Gaming Avanzado
('PC002', 'CPU002', 1, 1),
('PC002', 'RAM002', 1, 2),
('PC002', 'GPU002', 1, 3),
('PC002', 'HDD002', 1, 4),
('PC002', 'MON002', 1, 5);

-- =================================================================
-- DATOS DE PROMOCIONES (PATRÓN DECORATOR)
-- =================================================================

-- Insertar promociones base
INSERT INTO copromocion (id_promocion, descripcion, nombre, vigencia_desde, vigencia_hasta) VALUES
(1, 'Sin promoción', 'Regular', '2025-01-01', '2030-12-31'),
(2, 'Descuento por cantidad en monitores', 'Monitores por Volumen', '2025-03-01', '2025-05-31'),
(3, 'Promoción compra 3 paga 2 en tarjetas de video', 'Tarjetas 3x2', '2025-04-01', '2025-04-30'),
(4, 'Descuento del 20% en componentes para PC', 'PC Componentes', '2025-05-01', '2025-07-31'),
(5, 'Promoción compra 3 paga 2 en discos duros', 'HDD 3x2', '2025-06-01', '2025-06-30');

-- Insertar detalles de promociones
INSERT INTO codetalle_promocion (id_detalle_promocion, es_base, llevent, nombre, paguen, porc_dcto_plano, tipo_prom_acumulable, tipo_prom_base, id_promocion) VALUES
-- Promoción 1: Sin promoción (base)
(1, TRUE, 0, 'Regular', 0, 0.0, 'NO_APLICABLE', 'SIN_PROMOCION', 1),

-- Promoción 2: Descuento por cantidad en monitores
(2, TRUE, 0, 'Monitor Base', 0, 5.0, 'ACUMULATIVA', 'DESCUENTO_PLANO', 2),
(3, FALSE, 0, 'Monitor Volumen', 0, 0.0, 'ACUMULATIVA', 'DESCUENTO_X_CANTIDAD', 2),

-- Promoción 3: Promoción 3x2 en tarjetas de video
(4, TRUE, 3, '3x2 Tarjetas', 2, 0.0, 'NO_ACUMULATIVA', 'N_X_M', 3),

-- Promoción 4: Descuento del 20% en componentes para PC
(5, TRUE, 0, 'PC 20% Off', 0, 20.0, 'ACUMULATIVA', 'DESCUENTO_PLANO', 4),

-- Promoción 5: Promoción 3x2 en discos duros
(6, TRUE, 3, '3x2 HDD', 2, 0.0, 'NO_ACUMULATIVA', 'N_X_M', 5);

-- Insertar escalas de descuento por cantidad para la promoción de monitores
INSERT INTO codetalle_prom_dscto_x_cant (num_dscto, cantidad, dscto, num_det_promocion, num_promocion) VALUES
(1, 2, 5.0, 3, 2),   -- 2 monitores = 5% descuento adicional
(2, 5, 10.0, 3, 2),  -- 5 monitores = 10% descuento adicional
(3, 10, 15.0, 3, 2); -- 10 monitores = 15% descuento adicional

-- =================================================================
-- DATOS DE PRUEBA PARA COTIZACIONES
-- =================================================================

-- Insertar cotizaciones de prueba para testing
INSERT INTO cocotizacion (folio, fecha, impuestos, subtotal, total, algoritmo_cotizacion, pais_impuestos) VALUES
(1, '2025-01-15', 160.00, 1000.00, 1160.00, 'COTIZADOR_A', 'MEXICO'),
(2, '2025-01-16', 240.00, 1500.00, 1740.00, 'COTIZADOR_B', 'MEXICO'),
(3, '2025-01-17', 80.00, 500.00, 580.00, 'COTIZADOR_A', 'USA');

-- Insertar detalles de cotización de prueba
INSERT INTO codetalle_cotizacion (folio, num_detalle, cantidad, descripcion, id_componente, precio_base, precio_con_promocion, descuento_aplicado, subtotal_detalle) VALUES
-- Cotización 1
(1, 1, 2, 'Monitor LED 24 pulgadas', 'MON001', 300.00, 285.00, 15.00, 570.00),
(1, 2, 1, 'Disco Duro SATA 1TB', 'HDD001', 250.00, 250.00, 0.00, 250.00),
(1, 3, 1, 'Tarjeta Video GTX 1050', 'GPU001', 180.00, 180.00, 0.00, 180.00),

-- Cotización 2
(2, 1, 1, 'Monitor LED 27 pulgadas', 'MON002', 450.00, 450.00, 0.00, 450.00),
(2, 2, 2, 'Disco Duro SSD 500GB', 'HDD002', 350.00, 350.00, 0.00, 700.00),
(2, 3, 1, 'Tarjeta Video RTX 3060', 'GPU002', 350.00, 350.00, 0.00, 350.00),

-- Cotización 3
(3, 1, 1, 'Monitor LED 21 pulgadas', 'MON003', 200.00, 200.00, 0.00, 200.00),
(3, 2, 1, 'Disco Duro SATA 500GB', 'HDD003', 150.00, 150.00, 0.00, 150.00),
(3, 3, 1, 'Tarjeta Video GTX 1030', 'GPU003', 150.00, 150.00, 0.00, 150.00);