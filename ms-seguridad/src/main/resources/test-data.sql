-- =====================================================
-- Datos de prueba para tests del Microservicio de Seguridad
-- =====================================================

-- Insertar roles básicos del sistema
INSERT INTO rol (nombre, activo) VALUES 
('ADMIN', TRUE),
('USER', TRUE),
('GUEST', TRUE);

-- Insertar usuarios de prueba
INSERT INTO usuario (usuario, password, activo) VALUES 
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', TRUE), -- password: password
('testuser', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', TRUE), -- password: password
('guestuser', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', FALSE); -- password: password

-- Asignar roles a usuarios
INSERT INTO rol_asignado (id_usuario, id_rol, activo) VALUES 
(1, 1, TRUE), -- admin tiene rol ADMIN
(2, 2, TRUE), -- testuser tiene rol USER
(3, 3, TRUE); -- guestuser tiene rol GUEST