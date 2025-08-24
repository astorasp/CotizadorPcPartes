-- =================================================================
-- DML para Base de Datos: cotizador_pedidos_db  
-- Microservicio: ms-cotizador-pedidos
-- =================================================================

-- Configurar UTF-8 explícitamente
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET CHARACTER SET utf8mb4;

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
('TECH001', 'TechSupply SA', 'TechSupply Sociedad Anónima de Capital Variable'),
('HARD002', 'Hardware Express', 'Hardware Express S.A. de C.V.'),
('COMP003', 'ComponentesMX', 'Componentes Mexicanos S.A.'),
('GLOB004', 'Global PC Parts', 'Global PC Parts México S.A. de C.V.'),
('MICR005', 'MicroTech Solutions', 'MicroTech Solutions Internacional S.A.'),
('ELEC006', 'Electrónicos Del Valle', 'Electrónicos del Valle S.A. de C.V.'),
('PCWA007', 'PC Warehouse', 'PC Warehouse Internacional S.A. de C.V.'),
('DIGI008', 'Digital Components', 'Digital Components S. de R.L.'),
('TECH009', 'Tech Imports', 'Tech Imports México S.A. de C.V.'),
('HARD010', 'Hardware Depot', 'Hardware Depot Internacional S.A.');

-- Insertar promociones
INSERT INTO copromocion (id_promocion, descripcion, nombre, vigencia_desde, vigencia_hasta) VALUES
(1, 'Sin promoción', 'Regular', '2025-01-01', '2030-12-31'),
(2, 'Descuento por cantidad en monitores', 'Monitores por Volumen', '2025-03-01', '2025-05-31'),
(3, 'Promoción compra 3 paga 2 en tarjetas de video', 'Tarjetas 3x2', '2025-04-01', '2025-04-30'),
(4, 'Descuento del 20% en componentes para PC', 'PC Componentes', '2025-05-01', '2025-07-31'),
(5, 'Promoción compra 3 paga 2 en discos duros', 'HDD 3x2', '2025-06-01', '2025-06-30');

-- Insertar componentes - Monitores
INSERT INTO cocomponente (id_componente, descripcion, marca, modelo, costo, precio_base, id_tipo_componente, id_promocion) VALUES
('MON001', 'Monitor 24 pulgadas FullHD', 'LG', 'MN24F', 2500.00, 3500.00, 3, 2),
('MON002', 'Monitor 27 pulgadas 4K', 'Samsung', 'S27K', 4200.00, 5900.00, 3, 2),
('MON003', 'Monitor 32 pulgadas Curvo', 'Dell', 'D32C', 5100.00, 6800.00, 3, 1),
('MON004', 'Monitor Gamer 24" 144Hz', 'ASUS', 'VG24', 3800.00, 5200.00, 3, 1),
('MON005', 'Monitor UltraWide 29"', 'LG', 'UW29', 4500.00, 6300.00, 3, 2);

-- Insertar componentes - Discos Duros
INSERT INTO cocomponente (id_componente, descripcion, marca, modelo, costo, precio_base, id_tipo_componente, capacidad_alm, id_promocion) VALUES
('HDD001', 'Disco Duro 1TB SATA', 'Western Digital', 'WD10EZEX', 850.00, 1200.00, (SELECT id FROM cotipo_componente WHERE nombre = 'DISCO_DURO'), '1TB', 5),
('HDD002', 'SSD 500GB SATA', 'Samsung', 'EVO860', 1200.00, 1800.00, (SELECT id FROM cotipo_componente WHERE nombre = 'DISCO_DURO'), '500GB', 1),
('HDD003', 'NVMe SSD 1TB', 'Kingston', 'KC2500', 2300.00, 3100.00, (SELECT id FROM cotipo_componente WHERE nombre = 'DISCO_DURO'), '1TB', 5),
('HDD004', 'Disco Duro 2TB SATA', 'Seagate', 'Barracuda', 1100.00, 1600.00, (SELECT id FROM cotipo_componente WHERE nombre = 'DISCO_DURO'), '2TB', 1),
('HDD005', 'SSD 1TB SATA', 'Crucial', 'MX500', 1800.00, 2400.00, (SELECT id FROM cotipo_componente WHERE nombre = 'DISCO_DURO'), '1TB', 5);

-- Insertar componentes - Tarjetas de Video
INSERT INTO cocomponente (id_componente, descripcion, marca, modelo, costo, precio_base, id_tipo_componente, memoria, id_promocion) VALUES
('GPU001', 'Tarjeta de Video Gaming', 'NVIDIA', 'GeForce RTX 3060', 6000.00, 8500.00, (SELECT id FROM cotipo_componente WHERE nombre = 'TARJETA_VIDEO'), '8GB', 3),
('GPU002', 'Tarjeta de Video Profesional', 'AMD', 'Radeon RX 6700 XT', 7500.00, 10200.00, (SELECT id FROM cotipo_componente WHERE nombre = 'TARJETA_VIDEO'), '12GB', 1),
('GPU003', 'Tarjeta de Video Básica', 'NVIDIA', 'GeForce GTX 1650', 3200.00, 4500.00, (SELECT id FROM cotipo_componente WHERE nombre = 'TARJETA_VIDEO'), '4GB', 3),
('GPU004', 'Tarjeta de Video Alto Rendimiento', 'NVIDIA', 'GeForce RTX 3080', 12000.00, 16500.00, (SELECT id FROM cotipo_componente WHERE nombre = 'TARJETA_VIDEO'), '10GB', 1),
('GPU005', 'Tarjeta de Video Mid-Range', 'AMD', 'Radeon RX 6600', 5800.00, 7900.00, (SELECT id FROM cotipo_componente WHERE nombre = 'TARJETA_VIDEO'), '8GB', 3);

-- Insertar componentes - PCs (ensamblados)
INSERT INTO cocomponente (id_componente, descripcion, marca, modelo, costo, precio_base, id_tipo_componente, id_promocion) VALUES
('PC001', 'PC Gaming Alto Rendimiento', 'Custom Build', 'Gamer Pro X', 25000.00, 32000.00, (SELECT id FROM cotipo_componente WHERE nombre = 'PC'), 4),
('PC002', 'PC Oficina Estándar', 'Custom Build', 'Office Elite', 12000.00, 15000.00, (SELECT id FROM cotipo_componente WHERE nombre = 'PC'), 1),
('PC003', 'PC Diseño Profesional', 'Custom Build', 'Design Master', 28000.00, 35000.00, (SELECT id FROM cotipo_componente WHERE nombre = 'PC'), 4),
('PC004', 'PC Workstation', 'Custom Build', 'Power Station', 32000.00, 42000.00, (SELECT id FROM cotipo_componente WHERE nombre = 'PC'), 1),
('PC005', 'PC Gaming Económica', 'Custom Build', 'Game Start', 18000.00, 24000.00, (SELECT id FROM cotipo_componente WHERE nombre = 'PC'), 4);

-- =================================================================
-- DATOS CON DEPENDENCIAS DE SEGUNDO NIVEL
-- =================================================================

-- Insertar relaciones PC-Componentes
INSERT INTO copc_parte (id_pc, id_componente) VALUES
-- PC001 - Gaming Alto Rendimiento
('PC001', 'HDD003'), -- NVMe SSD 1TB
('PC001', 'GPU004'), -- GeForce RTX 3080
('PC001', 'MON004'), -- Monitor Gamer 24" 144Hz
-- PC002 - Oficina Estándar
('PC002', 'HDD002'), -- SSD 500GB SATA
('PC002', 'GPU003'), -- GeForce GTX 1650
('PC002', 'MON001'), -- Monitor 24 pulgadas FullHD
-- PC003 - Diseño Profesional
('PC003', 'HDD005'), -- SSD 1TB SATA
('PC003', 'GPU002'), -- Radeon RX 6700 XT
('PC003', 'MON002'), -- Monitor 27 pulgadas 4K
-- PC004 - Workstation
('PC004', 'HDD003'), -- NVMe SSD 1TB
('PC004', 'HDD004'), -- Disco Duro 2TB SATA
('PC004', 'GPU004'), -- GeForce RTX 3080
('PC004', 'MON003'), -- Monitor 32 pulgadas Curvo
-- PC005 - Gaming Económica
('PC005', 'HDD001'), -- Disco Duro 1TB SATA
('PC005', 'GPU005'), -- Radeon RX 6600
('PC005', 'MON001'); -- Monitor 24 pulgadas FullHD

-- Insertar cotizaciones
INSERT INTO cocotizacion (fecha, impuestos, subtotal, total) VALUES
('2025-04-15', 4800.00, 30000.00, 34800.00),
('2025-04-18', 2400.00, 15000.00, 17400.00),
('2025-04-20', 5600.00, 35000.00, 40600.00),
('2025-04-25', 6720.00, 42000.00, 48720.00),
('2025-04-28', 3840.00, 24000.00, 27840.00),
('2025-05-02', 1920.00, 12000.00, 13920.00),
('2025-05-05', 3360.00, 21000.00, 24360.00),
('2025-05-08', 1440.00, 9000.00, 10440.00),
('2025-05-10', 2720.00, 17000.00, 19720.00),
('2025-05-12', 2320.00, 14500.00, 16820.00);

-- Insertar detalles de cotización
INSERT INTO codetalle_cotizacion (cantidad, descripcion, folio, id_componente, num_detalle, precio_base) VALUES
-- Cotización 1 (usar el folio que se asignó automáticamente)
(1, 'PC Gaming Alto Rendimiento', 1, 'PC001', 1, 32000.00),
-- Cotización 2
(1, 'PC Oficina Estándar', 2, 'PC002', 1, 15000.00),
-- Cotización 3
(1, 'PC Diseño Profesional', 3, 'PC003', 1, 35000.00),
-- Cotización 4
(1, 'PC Workstation', 4, 'PC004', 1, 42000.00),
-- Cotización 5
(1, 'PC Gaming Económica', 5, 'PC005', 1, 24000.00),
-- Cotización 6
(2, 'Monitor 24 pulgadas FullHD', 6, 'MON001', 1, 3500.00),
(1, 'Disco Duro 1TB SATA', 6, 'HDD001', 2, 1200.00),
(2, 'Tarjeta de Video Básica', 6, 'GPU003', 3, 4500.00),
-- Cotización 7
(3, 'SSD 500GB SATA', 7, 'HDD002', 1, 1800.00),
(2, 'Monitor Gamer 24" 144Hz', 7, 'MON004', 2, 5200.00),
(1, 'Tarjeta de Video Mid-Range', 7, 'GPU005', 3, 7900.00),
-- Cotización 8
(2, 'Monitor 24 pulgadas FullHD', 8, 'MON001', 1, 3500.00),
(1, 'Disco Duro 2TB SATA', 8, 'HDD004', 2, 1600.00),
-- Cotización 9
(1, 'Tarjeta de Video Alto Rendimiento', 9, 'GPU004', 1, 16500.00),
(1, 'SSD 1TB SATA', 9, 'HDD005', 2, 2400.00),
-- Cotización 10
(1, 'Monitor UltraWide 29"', 10, 'MON005', 1, 6300.00),
(1, 'Tarjeta de Video Profesional', 10, 'GPU002', 2, 10200.00);

-- Insertar pedidos
INSERT INTO copedido (fecha_emision, fecha_entrega, nivel_surtido, cve_proveedor, total) VALUES
('2025-04-16', '2025-04-30', 1, 'TECH001', 34800.00),
('2025-04-19', '2025-05-03', 2, 'COMP003', 17400.00),
('2025-04-21', '2025-05-05', 0, 'HARD002', 40600.00),
('2025-04-26', '2025-05-10', 1, 'MICR005', 48720.00),
('2025-04-29', '2025-05-13', 2, 'GLOB004', 27840.00),
('2025-05-03', '2025-05-17', 1, 'ELEC006', 13920.00),
('2025-05-06', '2025-05-20', 0, 'PCWA007', 24360.00),
('2025-05-09', '2025-05-23', 2, 'DIGI008', 10440.00),
('2025-05-11', '2025-05-25', 1, 'TECH009', 19720.00),
('2025-05-13', '2025-05-27', 0, 'HARD010', 16820.00);

-- Insertar detalles de pedido
INSERT INTO codetalle_pedido (cantidad, id_componente, num_detalle, num_pedido, precio_unitario, total_cotizado) VALUES
-- Pedido 1
(1, 'PC001', 1, 1, 32000.00, 34800.00),
-- Pedido 2
(1, 'PC002', 1, 2, 15000.00, 17400.00),
-- Pedido 3
(1, 'PC003', 1, 3, 35000.00, 40600.00),
-- Pedido 4
(1, 'PC004', 1, 4, 42000.00, 48720.00),
-- Pedido 5
(1, 'PC005', 1, 5, 24000.00, 27840.00),
-- Pedido 6
(2, 'MON001', 1, 6, 3500.00, 7000.00),
(1, 'HDD001', 2, 6, 1200.00, 1200.00),
(2, 'GPU003', 3, 6, 4500.00, 9000.00),
-- Pedido 7
(3, 'HDD002', 1, 7, 1800.00, 5400.00),
(2, 'MON004', 2, 7, 5200.00, 10400.00),
(1, 'GPU005', 3, 7, 7900.00, 7900.00),
-- Pedido 8
(2, 'MON001', 1, 8, 3500.00, 7000.00),
(1, 'HDD004', 2, 8, 1600.00, 1600.00),
-- Pedido 9
(1, 'GPU004', 1, 9, 16500.00, 16500.00),
(1, 'HDD005', 2, 9, 2400.00, 2400.00),
-- Pedido 10
(1, 'MON005', 1, 10, 6300.00, 6300.00),
(1, 'GPU002', 2, 10, 10200.00, 10200.00);