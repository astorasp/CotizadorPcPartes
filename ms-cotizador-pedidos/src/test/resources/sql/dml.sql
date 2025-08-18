-- =================================================================
-- DML para Base de Datos: cotizador_pedidos_db (TEST)
-- Microservicio: ms-cotizador-pedidos
-- =================================================================

-- TestContainers ya configuró la base de datos, no necesitamos USE
-- Solo insertar los datos directamente

-- =================================================================
-- DATOS BÁSICOS (SIN DEPENDENCIAS)
-- =================================================================

-- Insertar tipos de componentes
INSERT INTO cotipo_componente (nombre) VALUES 
('PC'), 
('DISCO_DURO'), 
('MONITOR'), 
('TARJETA_VIDEO');

-- Insertar proveedores
INSERT INTO coproveedor (cve, nombre, razon_social) VALUES
('PROV001', 'TechCorp Distribution', 'TechCorp Distribution S.A. de C.V.'),
('PROV002', 'Hardware Solutions', 'Hardware Solutions México S.A.'),
('PROV003', 'Component Masters', 'Component Masters Internacional'),
('PROV004', 'Global PC Parts', 'Global PC Parts México S.A. de C.V.'),
('PROV005', 'MicroTech Solutions', 'MicroTech Solutions Internacional S.A.');

-- Insertar promociones
INSERT INTO copromocion (id_promocion, descripcion, nombre, vigencia_desde, vigencia_hasta) VALUES
(1, 'Sin promoción', 'Regular', '2025-01-01', '2030-12-31'),
(2, 'Descuento por cantidad en monitores', 'Monitores por Volumen', '2025-03-01', '2025-05-31'),
(3, 'Promoción compra 3 paga 2 en tarjetas de video', 'Tarjetas 3x2', '2025-04-01', '2025-04-30'),
(4, 'Descuento del 20% en componentes para PC', 'PC Componentes', '2025-05-01', '2025-07-31'),
(5, 'Promoción compra 3 paga 2 en discos duros', 'HDD 3x2', '2025-06-01', '2025-06-30');

-- =================================================================
-- DATOS CON DEPENDENCIAS DE PRIMER NIVEL  
-- =================================================================

-- Insertar detalles de promoción
INSERT INTO codetalle_promocion (id_detalle_promocion, es_base, llevent, nombre, paguen, porc_dcto_plano, tipo_prom_acumulable, tipo_prom_base, id_promocion) VALUES
-- Promoción Regular (sin promoción)
(1, TRUE, 1, 'Precio Regular', 1, 0.00, NULL, 'BASE', 1),
-- Promoción de Monitores por Volumen (descuento por cantidad)
(2, TRUE, 1, 'Descuento por Volumen Monitores', 1, 0.00, NULL, 'BASE', 2),
-- Promoción Tarjetas 3x2 (compra N lleva M)
(3, TRUE, 3, 'Compra 3 Paga 2 - Tarjetas', 2, 33.33, NULL, 'BASE', 3),
-- Promoción PC Componentes (descuento general)
(4, TRUE, 1, 'Descuento en PC Componentes', 1, 20.00, NULL, 'BASE', 4),
-- Promoción HDD 3x2 (compra N lleva M)
(5, TRUE, 3, 'Compra 3 Paga 2 - Discos', 2, 33.33, NULL, 'BASE', 5);

-- Insertar detalles de promoción por documento y cantidad
INSERT INTO codetalle_prom_dscto_x_cant (num_dscto, cantidad, dscto, num_det_promocion, num_promocion) VALUES
-- Detalle Promoción Monitores por Volumen - escalado por cantidad
(1, 3, 5.00, 2, 2),  -- 5% para 3-5 monitores
(2, 6, 10.00, 2, 2); -- 10% para 6+ monitores

-- Insertar componentes - Monitores (datos reducidos para tests)
INSERT INTO cocomponente (id_componente, descripcion, marca, modelo, costo, precio_base, id_tipo_componente, id_promocion) VALUES
('MON001', 'Monitor 24 pulgadas FullHD', 'LG', 'MN24F', 2500.00, 3500.00, 3, 2),
('MON002', 'Monitor 27 pulgadas 4K', 'Samsung', 'S27K', 4200.00, 5900.00, 3, 2);

-- Insertar componentes - Discos Duros (datos reducidos para tests)
INSERT INTO cocomponente (id_componente, descripcion, marca, modelo, costo, precio_base, id_tipo_componente, capacidad_alm, id_promocion) VALUES
('HDD001', 'Disco Duro 1TB SATA', 'Western Digital', 'WD10EZEX', 850.00, 1200.00, (SELECT id FROM cotipo_componente WHERE nombre = 'DISCO_DURO'), '1TB', 5),
('HDD002', 'SSD 500GB SATA', 'Samsung', 'EVO860', 1200.00, 1800.00, (SELECT id FROM cotipo_componente WHERE nombre = 'DISCO_DURO'), '500GB', 1);

-- Insertar componentes - Tarjetas de Video (datos reducidos para tests)
INSERT INTO cocomponente (id_componente, descripcion, marca, modelo, costo, precio_base, id_tipo_componente, memoria, id_promocion) VALUES
('GPU001', 'Tarjeta de Video Gaming', 'NVIDIA', 'GeForce RTX 3060', 6000.00, 8500.00, (SELECT id FROM cotipo_componente WHERE nombre = 'TARJETA_VIDEO'), '8GB', 3),
('GPU002', 'Tarjeta de Video Profesional', 'AMD', 'Radeon RX 6700 XT', 7500.00, 10200.00, (SELECT id FROM cotipo_componente WHERE nombre = 'TARJETA_VIDEO'), '12GB', 1);

-- Insertar componentes - PCs (datos reducidos para tests)
INSERT INTO cocomponente (id_componente, descripcion, marca, modelo, costo, precio_base, id_tipo_componente, id_promocion) VALUES
('PC001', 'PC Gaming Alto Rendimiento', 'Custom Build', 'Gamer Pro X', 25000.00, 32000.00, (SELECT id FROM cotipo_componente WHERE nombre = 'PC'), 4),
('PC002', 'PC Oficina Estándar', 'Custom Build', 'Office Elite', 12000.00, 15000.00, (SELECT id FROM cotipo_componente WHERE nombre = 'PC'), 1);

-- =================================================================
-- DATOS CON DEPENDENCIAS DE SEGUNDO NIVEL
-- =================================================================

-- Insertar relaciones PC-Componentes (datos reducidos para tests)
INSERT INTO copc_parte (id_pc, id_componente) VALUES
-- PC001 - Gaming Alto Rendimiento
('PC001', 'HDD001'), -- Disco Duro 1TB SATA
('PC001', 'GPU001'), -- GeForce RTX 3060
('PC001', 'MON001'), -- Monitor 24 pulgadas FullHD
-- PC002 - Oficina Estándar
('PC002', 'HDD002'), -- SSD 500GB SATA
('PC002', 'GPU002'), -- Radeon RX 6700 XT
('PC002', 'MON002'); -- Monitor 27 pulgadas 4K

-- Insertar cotizaciones (datos reducidos para tests)
INSERT INTO cocotizacion (fecha, impuestos, subtotal, total) VALUES
('2025-04-15', 4800.00, 30000.00, 34800.00),
('2025-04-18', 2400.00, 15000.00, 17400.00),
('2025-04-20', 5600.00, 35000.00, 40600.00);

-- Insertar detalles de cotización (datos reducidos para tests)
INSERT INTO codetalle_cotizacion (cantidad, descripcion, folio, id_componente, num_detalle, precio_base) VALUES
-- Cotización 1
(1, 'PC Gaming Alto Rendimiento', 1, 'PC001', 1, 32000.00),
-- Cotización 2
(1, 'PC Oficina Estándar', 2, 'PC002', 1, 15000.00),
-- Cotización 3
(2, 'Monitor 24 pulgadas FullHD', 3, 'MON001', 1, 3500.00),
(1, 'Disco Duro 1TB SATA', 3, 'HDD001', 2, 1200.00);

-- Insertar pedidos (datos reducidos para tests)
INSERT INTO copedido (fecha_emision, fecha_entrega, nivel_surtido, cve_proveedor, total) VALUES
('2025-04-16', '2025-04-30', 1, 'PROV001', 34800.00),
('2025-04-19', '2025-05-03', 2, 'PROV003', 17400.00),
('2025-04-21', '2025-05-05', 0, 'PROV002', 40600.00);

-- Insertar detalles de pedido (datos reducidos para tests)
INSERT INTO codetalle_pedido (cantidad, id_componente, num_detalle, num_pedido, precio_unitario, total_cotizado) VALUES
-- Pedido 1
(1, 'PC001', 1, 1, 32000.00, 34800.00),
-- Pedido 2
(1, 'PC002', 1, 2, 15000.00, 17400.00),
-- Pedido 3
(2, 'MON001', 1, 3, 3500.00, 7000.00),
(1, 'HDD001', 2, 3, 1200.00, 1200.00);