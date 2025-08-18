-- =================================================================
-- DML para Base de Datos: cotizador_pedidos_db  
-- Microservicio: ms-cotizador-pedidos
-- =================================================================

-- TestContainers ya configuró la base de datos, no necesitamos USE
-- Solo insertar los datos directamente

-- =================================================================
-- DATOS INICIALES PARA PRUEBAS
-- =================================================================

-- Insertar proveedores de prueba
INSERT INTO coproveedor (cve, nombre, razon_social, telefono, email, direccion, activo) VALUES
('PROV001', 'TechCorp Distribution', 'TechCorp Distribution S.A. de C.V.', '+52-55-1234-5678', 'ventas@techcorp.mx', 'Av. Tecnología 123, Col. Innovación, CDMX', TRUE),
('PROV002', 'Hardware Solutions', 'Hardware Solutions México S.A.', '+52-55-9876-5432', 'contacto@hwsolutions.mx', 'Calle Circuitos 456, Col. Electrónica, Guadalajara', TRUE),
('PROV003', 'Component Masters', 'Component Masters Internacional', '+52-33-5555-7777', 'info@componentmasters.com', 'Blvd. Componentes 789, Col. Digital, Monterrey', TRUE);

-- =================================================================
-- DATOS DE REFERENCIA EXTERNA
-- =================================================================

-- Los datos de componentes y cotizaciones se obtienen dinámicamente
-- desde los microservicios correspondientes vía REST o Kafka
-- No se almacenan localmente para evitar inconsistencias

-- =================================================================
-- DATOS DE PRUEBA PARA DESARROLLO
-- =================================================================

-- Pedidos de ejemplo
INSERT INTO copedido (cve_proveedor, fecha_emision, fecha_entrega, nivel_surtido, total, estado_pedido, folio_cotizacion, observaciones) VALUES
('PROV001', '2024-08-15', '2024-08-22', 85, 25500.00, 'ENVIADO', 1, 'Pedido urgente para cliente empresarial'),
('PROV002', '2024-08-16', '2024-08-25', 100, 18700.00, 'COMPLETO', 2, 'Entrega realizada sin problemas'),
('PROV003', '2024-08-17', '2024-08-30', 75, 32100.00, 'PARCIAL', 3, 'Pendiente entrega de tarjeta de video');

-- Detalles de pedidos de ejemplo
INSERT INTO codetalle_pedido (num_pedido, num_detalle, id_componente, cantidad, precio_unitario, total_cotizado) VALUES
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
-- DATOS DE PRODUCCIÓN CARGADOS EXITOSAMENTE
-- =================================================================

-- Los datos iniciales han sido insertados correctamente
-- Para verificación manual, ejecutar consultas por separado