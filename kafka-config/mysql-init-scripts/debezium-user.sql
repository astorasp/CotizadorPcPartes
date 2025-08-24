-- Script de inicialización para crear usuario Debezium con permisos CDC
-- Se ejecuta automáticamente al inicializar contenedores MySQL

-- Crear usuario debezium con permisos necesarios para CDC
CREATE USER IF NOT EXISTS 'debezium'@'%' IDENTIFIED BY 'dbz_password';

-- Otorgar permisos específicos para Debezium CDC (lectura)
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'debezium'@'%';

-- Otorgar permisos de escritura para JDBC Sink Connectors
-- Estas líneas se ejecutan solo si las bases de datos existen
GRANT INSERT, UPDATE, DELETE ON cotizador_componentes_db.* TO 'debezium'@'%';
GRANT INSERT, UPDATE, DELETE ON cotizador_cotizaciones_db.* TO 'debezium'@'%';
GRANT INSERT, UPDATE, DELETE ON cotizador_pedidos_db.* TO 'debezium'@'%';

-- Aplicar cambios
FLUSH PRIVILEGES;

-- Mostrar confirmación
SELECT 'Usuario debezium creado exitosamente para CDC' AS status;