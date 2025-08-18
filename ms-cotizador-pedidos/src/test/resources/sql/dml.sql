-- =================================================================
-- DML para Base de Datos: cotizador_pedidos_db  
-- Microservicio: ms-cotizador-pedidos
-- =================================================================

-- Configurar UTF-8 explícitamente al inicio
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET CHARACTER SET utf8mb4;

USE cotizador_pedidos_db;

-- =================================================================
-- DATOS INICIALES PARA PRUEBAS
-- =================================================================

-- Insertar proveedores de prueba
INSERT INTO coproveedor (cve, nombre, razon_social, telefono, email, direccion, activo) VALUES
('PROV001', 'TechCorp Distribution', 'TechCorp Distribution S.A. de C.V.', '+52-55-1234-5678', 'ventas@techcorp.mx', 'Av. Tecnología 123, Col. Innovación, CDMX', TRUE),
('PROV002', 'Hardware Solutions', 'Hardware Solutions México S.A.', '+52-55-9876-5432', 'contacto@hwsolutions.mx', 'Calle Circuitos 456, Col. Electrónica, Guadalajara', TRUE),
('PROV003', 'Component Masters', 'Component Masters Internacional', '+52-33-5555-7777', 'info@componentmasters.com', 'Blvd. Componentes 789, Col. Digital, Monterrey', TRUE);

-- =================================================================
-- INSERTAR DATOS DE CACHE PARA SINCRONIZACIÓN
-- =================================================================

-- Cache de tipos de componente (sincronizado desde ms-cotizador-componentes)
INSERT INTO cache_tipo_componente (id_tipo, descripcion, activo) VALUES
(1, 'Procesador', TRUE),
(2, 'Memoria RAM', TRUE),
(3, 'Disco Duro', TRUE),
(4, 'Tarjeta de Video', TRUE),
(5, 'Monitor', TRUE),
(6, 'Teclado', TRUE),
(7, 'Mouse', TRUE),
(8, 'Parlantes', TRUE),
(9, 'Motherboard', TRUE),
(10, 'Fuente de Poder', TRUE);

-- Cache de componentes básicos (sincronizado desde ms-cotizador-componentes)
INSERT INTO cache_componente (id_componente, descripcion, precio, id_tipo, activo) VALUES
('COMP001', 'Intel Core i7-13700K 3.4GHz', 6500.00, 1, TRUE),
('COMP002', 'AMD Ryzen 7 7700X 4.5GHz', 5800.00, 1, TRUE),
('COMP003', 'Corsair Vengeance LPX 16GB DDR4-3200', 1200.00, 2, TRUE),
('COMP004', 'Kingston Fury Beast 32GB DDR4-3600', 2300.00, 2, TRUE),
('COMP005', 'WD Black SN850X 1TB NVMe SSD', 2800.00, 3, TRUE),
('COMP006', 'Seagate Barracuda 2TB HDD', 1500.00, 3, TRUE),
('COMP007', 'NVIDIA GeForce RTX 4070 12GB', 12500.00, 4, TRUE),
('COMP008', 'AMD Radeon RX 7700 XT 12GB', 10800.00, 4, TRUE),
('COMP009', 'ASUS ROG Swift 27" 144Hz Gaming', 8500.00, 5, TRUE),
('COMP010', 'LG UltraGear 24" 165Hz', 4200.00, 5, TRUE);

-- Cache de cotizaciones básicas para pruebas (sincronizado desde ms-cotizador-cotizaciones)
INSERT INTO cache_cotizacion (folio, fecha, total, activo) VALUES
(1, '2024-08-15', 25500.00, TRUE),
(2, '2024-08-16', 18700.00, TRUE),
(3, '2024-08-17', 32100.00, TRUE);

-- =================================================================
-- DATOS DE PRUEBA PARA DESARROLLO
-- =================================================================

-- Pedidos de ejemplo
INSERT INTO copedido (cve_proveedor, fecha_emision, fecha_entrega, nivel_surtido, total, estado_pedido, folio_cotizacion, observaciones) VALUES
('PROV001', '2024-08-15', '2024-08-22', 85, 25500.00, 'ENVIADO', 1, 'Pedido urgente para cliente empresarial'),
('PROV002', '2024-08-16', '2024-08-25', 100, 18700.00, 'COMPLETO', 2, 'Entrega realizada sin problemas'),
('PROV003', '2024-08-17', '2024-08-30', 75, 32100.00, 'PARCIAL', 3, 'Pendiente entrega de tarjeta de video');

-- Detalles de pedidos de ejemplo
INSERT INTO codetalle_pedido (num_pedido, num_detalle, id_componente, cantidad, precio_unitario, subtotal) VALUES
-- Pedido 1
(1, 1, 'COMP001', 2, 6500.00, 13000.00),
(1, 2, 'COMP003', 4, 1200.00, 4800.00),
(1, 3, 'COMP005', 2, 2800.00, 5600.00),
(1, 4, 'COMP009', 1, 8500.00, 8500.00),
-- Pedido 2  
(2, 1, 'COMP002', 1, 5800.00, 5800.00),
(2, 2, 'COMP004', 2, 2300.00, 4600.00),
(2, 3, 'COMP006', 3, 1500.00, 4500.00),
(2, 4, 'COMP010', 2, 4200.00, 8400.00),
-- Pedido 3
(3, 1, 'COMP001', 3, 6500.00, 19500.00),
(3, 2, 'COMP007', 1, 12500.00, 12500.00);

-- =================================================================
-- VERIFICACIÓN DE INTEGRIDAD
-- =================================================================

-- Verificar que los totales de pedidos coincidan con la suma de detalles
SELECT 
    p.num_pedido,
    p.total as total_pedido,
    SUM(dp.subtotal) as total_calculado,
    CASE 
        WHEN p.total = SUM(dp.subtotal) THEN 'OK'
        ELSE 'ERROR'
    END as verificacion
FROM copedido p
LEFT JOIN codetalle_pedido dp ON p.num_pedido = dp.num_pedido
GROUP BY p.num_pedido, p.total;

-- Mostrar resumen de datos insertados
SELECT 'Proveedores' as tabla, COUNT(*) as registros FROM coproveedor
UNION ALL
SELECT 'Pedidos' as tabla, COUNT(*) as registros FROM copedido  
UNION ALL
SELECT 'Detalles Pedido' as tabla, COUNT(*) as registros FROM codetalle_pedido
UNION ALL
SELECT 'Cache Componentes' as tabla, COUNT(*) as registros FROM cache_componente
UNION ALL
SELECT 'Cache Tipos' as tabla, COUNT(*) as registros FROM cache_tipo_componente
UNION ALL
SELECT 'Cache Cotizaciones' as tabla, COUNT(*) as registros FROM cache_cotizacion;