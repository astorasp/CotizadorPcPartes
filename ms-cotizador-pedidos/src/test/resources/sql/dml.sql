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

-- Insertar tipos de componente
INSERT INTO cotipo_componente (tipo, descripcion) VALUES
('PROCESADOR', 'Unidad central de procesamiento'),
('MEMORIA', 'Memoria RAM del sistema'),
('TARJETA_GRAFICA', 'Tarjeta gráfica/GPU'),
('ALMACENAMIENTO', 'Dispositivos de almacenamiento'),
('MOTHERBOARD', 'Placa base/Motherboard'),
('FUENTE_PODER', 'Fuente de alimentación'),
('GABINETE', 'Carcasa/Gabinete del sistema');

-- Insertar componentes de prueba
INSERT INTO cocomponente (marca, modelo, descripcion, precio, descuento, id_tipo_componente) VALUES
('Intel', 'i7-13700K', 'Procesador Intel Core i7 13va gen', 12500.00, 0.00, 1),
('AMD', 'Ryzen 5 5600', 'Procesador AMD Ryzen 5 serie 5000', 6200.00, 0.00, 1),
('Corsair', 'Vengeance DDR4 16GB', 'Memoria RAM DDR4 3200MHz', 8800.00, 0.00, 2),
('Kingston', 'ValueRAM DDR4 8GB', 'Memoria RAM DDR4 básica', 3800.00, 0.00, 2),
('NVIDIA', 'RTX 4060', 'Tarjeta gráfica gaming media', 15100.00, 0.00, 3),
('Samsung', '980 NVMe SSD 500GB', 'Almacenamiento SSD rápido', 4250.00, 0.00, 4),
('NVIDIA', 'RTX 4080', 'Tarjeta gráfica profesional', 22800.00, 0.00, 3),
('ASUS', 'Z790-E Gaming', 'Motherboard para Intel 13va gen', 10800.00, 0.00, 5),
('Seasonic', 'Focus GX-850', 'Fuente de poder modular', 8750.00, 0.00, 6),
('Fractal Design', 'Define 7', 'Gabinete gaming con RGB', 5400.00, 0.00, 7);

-- =================================================================
-- DATOS DE CACHE PARA SINCRONIZACIÓN
-- =================================================================

-- Los datos de cache se omiten en tests de integración
-- para simplificar la configuración de TestContainers
-- En producción estos datos vienen de otros microservicios

-- =================================================================
-- DATOS DE PRUEBA PARA DESARROLLO
-- =================================================================

-- Cotizaciones de ejemplo para pruebas
INSERT INTO cocotizacion (fecha_creacion, subtotal, impuestos, total, pais) VALUES
('2024-08-01', 38440.00, 6144.00, 44584.00, 'MX'),
('2024-08-02', 15730.00, 2517.00, 18247.00, 'MX'),
('2024-08-03', 44880.00, 7181.00, 52061.00, 'MX'),
('2024-08-04', 27285.00, 4366.00, 31651.00, 'MX'),
('2024-08-05', 19915.00, 3186.00, 23101.00, 'MX');

-- Detalles de cotizaciones de ejemplo
INSERT INTO codetalle_cotizacion (cantidad, precio_unitario, subtotal, cotizacion_id, componente_id) VALUES
-- Cotización 1 - GAMING_HIGH
(1, 12500.00, 12500.00, 1, 1),
(2, 8800.00, 17600.00, 1, 3),
(1, 15100.00, 15100.00, 1, 5),
-- Cotización 2 - OFFICE_BASIC  
(1, 6200.00, 6200.00, 2, 2),
(1, 3800.00, 3800.00, 2, 4),
(2, 4250.00, 8500.00, 2, 6),
-- Cotización 3 - DESIGN_PRO
(1, 12500.00, 12500.00, 3, 1),
(1, 22800.00, 22800.00, 3, 7),
(2, 8750.00, 17500.00, 3, 9),
-- Cotización 4 - GAMING_MID
(1, 6200.00, 6200.00, 4, 2),
(1, 15100.00, 15100.00, 4, 5),
(1, 10800.00, 10800.00, 4, 8),
-- Cotización 5 - GAMING_BUDGET
(1, 8800.00, 8800.00, 5, 3),
(1, 3800.00, 3800.00, 5, 4),
(2, 5400.00, 10800.00, 5, 10);

-- Pedidos de ejemplo
INSERT INTO copedido (cve_proveedor, fecha_emision, fecha_entrega, nivel_surtido, total, estado_pedido, folio_cotizacion, observaciones) VALUES
('PROV001', '2024-08-15', '2024-08-22', 85, 25500.00, 'ENVIADO', 1, 'Pedido urgente para cliente empresarial'),
('PROV002', '2024-08-16', '2024-08-25', 100, 18700.00, 'COMPLETO', 2, 'Entrega realizada sin problemas'),
('PROV003', '2024-08-17', '2024-08-30', 75, 32100.00, 'PARCIAL', 3, 'Pendiente entrega de tarjeta de video');

-- Detalles de pedidos de ejemplo
INSERT INTO codetalle_pedido (num_pedido, num_detalle, id_componente, cantidad, precio_unitario, total_cotizado) VALUES
-- Pedido 1
(1, 1, 1, 2, 6500.00, 13000.00),
(1, 2, 3, 4, 1200.00, 4800.00),
(1, 3, 5, 2, 2800.00, 5600.00),
(1, 4, 9, 1, 8500.00, 8500.00),
-- Pedido 2  
(2, 1, 2, 1, 5800.00, 5800.00),
(2, 2, 4, 2, 2300.00, 4600.00),
(2, 3, 6, 3, 1500.00, 4500.00),
(2, 4, 10, 2, 4200.00, 8400.00),
-- Pedido 3
(3, 1, 1, 3, 6500.00, 19500.00),
(3, 2, 7, 1, 12500.00, 12500.00);

-- =================================================================
-- DATOS DE PRUEBA CARGADOS EXITOSAMENTE  
-- =================================================================

-- Los datos de prueba han sido insertados correctamente
-- Para verificación manual, ejecutar consultas por separado