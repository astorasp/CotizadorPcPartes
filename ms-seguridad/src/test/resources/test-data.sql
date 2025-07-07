-- Datos de prueba para testing
-- Este archivo se ejecuta antes de cada test para asegurar un estado consistente

-- Limpiar datos existentes en orden correcto (foreign keys)
DELETE FROM rol_asignado;
DELETE FROM usuario;
DELETE FROM rol;

-- Reiniciar auto_increment (MySQL)
ALTER TABLE rol AUTO_INCREMENT = 1;
ALTER TABLE usuario AUTO_INCREMENT = 1;

-- Insertar roles básicos
INSERT INTO rol (id, nombre, activo, fecha_creacion, fecha_modificacion) VALUES
(1, 'ADMIN', true, NOW(), NOW()),
(2, 'USER', true, NOW(), NOW()),
(3, 'SUPERVISOR', true, NOW(), NOW()),
(4, 'GERENTE', true, NOW(), NOW()),
(5, 'VENDEDOR', true, NOW(), NOW()),
(6, 'INVENTARIO', true, NOW(), NOW()),
(7, 'CONSULTOR', true, NOW(), NOW());

-- Insertar usuario administrador
-- Contraseña: admin123 (BCrypt strength 12)
INSERT INTO usuario (id, usuario, password, activo, fecha_creacion, fecha_modificacion) VALUES
(1, 'admin', '$2a$12$rWx1.WL7wFJKj8qXV8yUxeKsQf5K6F7K8J9L2M3N4O5P6Q7R8S9T0U', true, NOW(), NOW());

-- Insertar usuario normal
-- Contraseña: user123 (BCrypt strength 12)  
INSERT INTO usuario (id, usuario, password, activo, fecha_creacion, fecha_modificacion) VALUES
(2, 'testuser', '$2a$12$sXy2.XM8xGKLk9rYW9zVyfLtRg6L7G8L9K0M3N4O5P6Q7R8S9T0U1V', true, NOW(), NOW());

-- Usuario inactivo para tests
INSERT INTO usuario (id, usuario, password, activo, fecha_creacion, fecha_modificacion) VALUES
(3, 'inactive', '$2a$12$tYz3.YN9yHLMl0sZX0aWzgMuSh7M8H9M0L1N4O5P6Q7R8S9T0U1V2W', false, NOW(), NOW());

-- Usuario sin roles para tests
INSERT INTO usuario (id, usuario, password, activo, fecha_creacion, fecha_modificacion) VALUES
(4, 'noroles', '$2a$12$uZa4.ZO0zIMNm1tAY1bXahNvTi8N9I0N1M2O5P6Q7R8S9T0U1V2W3X', true, NOW(), NOW());

-- Asignar rol ADMIN al usuario admin
INSERT INTO rol_asignado (id_usuario, id_rol, activo, fecha_creacion, fecha_modificacion) VALUES
(1, 1, true, NOW(), NOW());

-- Asignar rol USER al usuario testuser
INSERT INTO rol_asignado (id_usuario, id_rol, activo, fecha_creacion, fecha_modificacion) VALUES
(2, 2, true, NOW(), NOW());

-- Asignar rol ADMIN también al testuser para algunos tests
INSERT INTO rol_asignado (id_usuario, id_rol, activo, fecha_creacion, fecha_modificacion) VALUES
(2, 1, true, NOW(), NOW());