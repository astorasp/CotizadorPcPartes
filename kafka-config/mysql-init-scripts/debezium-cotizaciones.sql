-- Script de inicialización Debezium para microservicio COTIZACIONES
-- Se ejecuta automáticamente al inicializar contenedor cotizaciones-mysql

-- Crear usuario debezium con permisos necesarios para CDC
-- Usar autenticación por defecto (caching_sha2_password) - compatible con Debezium 3.0+
CREATE USER IF NOT EXISTS 'debezium'@'%' IDENTIFIED BY 'dbz_password';

-- Otorgar permisos específicos para Debezium CDC (lectura)
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'debezium'@'%';

-- Otorgar permisos de escritura para JDBC Sink Connectors
-- Solo sobre la base de datos local que existe en este contenedor
GRANT INSERT, UPDATE, DELETE ON `cotizador_cotizaciones_db`.* TO 'debezium'@'%';

-- Aplicar cambios
FLUSH PRIVILEGES;

-- Mostrar confirmación
SELECT 'Usuario debezium creado exitosamente para COTIZACIONES' AS status;