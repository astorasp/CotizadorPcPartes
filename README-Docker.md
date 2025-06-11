# 🐳 Cotizador - Deployment con Docker

Este documento explica cómo ejecutar el Sistema Cotizador usando Docker y Docker Compose.

## 📋 Prerrequisitos

- [Docker](https://docs.docker.com/get-docker/) (versión 20.10 o superior)
- [Docker Compose](https://docs.docker.com/compose/install/) (versión 1.28 o superior)
- Al menos 4GB de RAM disponible
- Puertos libres: 80, 8080, 8081, 3306

## 🏗️ Arquitectura

El sistema se compone de los siguientes servicios:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │    Backend      │    │     MySQL       │
│   (Nginx:80)    │◄──►│ (Spring:8080)   │◄──►│   (Port:3306)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                 ▲
                                 │
                       ┌─────────────────┐
                       │    Adminer      │
                       │   (Port:8081)   │
                       └─────────────────┘
```

## 🚀 Inicio Rápido

### 1. Clonar el repositorio
```bash
git clone <repository-url>
cd cotizador-project
```

### 2. Ejecutar con Docker Compose
```bash
# Construir y levantar todos los servicios
docker-compose up -d --build
```

### 3. Verificar que todo esté funcionando
```bash
# Ver estado de los servicios
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f
```

### 4. Acceder a las aplicaciones
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080/cotizador/v1/api
- **Adminer (DB Admin)**: http://localhost:8081
- **MySQL**: localhost:3306

## 🛠️ Scripts de Utilidad

Hemos incluido un script para facilitar las operaciones comunes:

```bash
# Hacer ejecutable el script
chmod +x docker-scripts.sh

# Ver ayuda
./docker-scripts.sh help

# Comandos principales
./docker-scripts.sh start      # Iniciar servicios
./docker-scripts.sh stop       # Detener servicios  
./docker-scripts.sh restart    # Reiniciar servicios
./docker-scripts.sh status     # Ver estado
./docker-scripts.sh logs       # Ver logs
./docker-scripts.sh health     # Verificar salud
```

## 📊 Configuración de Base de Datos

### Credenciales por defecto:
- **Host**: mysql (interno) / localhost (externo)
- **Puerto**: 3306
- **Base de datos**: cotizador
- **Usuario**: cotizador_user
- **Contraseña**: cotizador_pass
- **Root password**: root_password

### Scripts de inicialización:
Los scripts SQL se ejecutan automáticamente al crear el contenedor:
- `Cotizador/sql/ddl.sql` - Estructura de tablas
- `Cotizador/sql/dml.sql` - Datos iniciales

## 🔧 Configuración Avanzada

### Variables de Entorno

Puedes personalizar la configuración mediante variables de entorno:

```yaml
# En docker-compose.yml
environment:
  # Base de datos
  DB_HOST: mysql
  DB_PORT: 3306
  DB_USERNAME: cotizador_user
  DB_PASSWORD: cotizador_pass
  
  # Seguridad
  SECURITY_USERNAME: admin
  SECURITY_PASSWORD: admin123
  
  # Java optimizations
  JAVA_OPTS: "-Xmx1g -Xms512m"
```

### Volúmenes Persistentes

- `mysql_data`: Datos de MySQL
- `backend_logs`: Logs del backend

```bash
# Ver volúmenes
docker volume ls | grep cotizador

# Backup de base de datos
docker-compose exec mysql mysqldump -u cotizador_user -pcotizador_pass cotizador > backup.sql
```

## 🐛 Troubleshooting

### Problemas Comunes

#### 1. Puerto ya en uso
```bash
# Ver qué proceso usa el puerto
sudo lsof -i :8080

# Cambiar puerto en docker-compose.yml
ports:
  - "8081:8080"  # Mapear a puerto diferente
```

#### 2. Error de conexión a base de datos
```bash
# Verificar que MySQL esté listo
./docker-scripts.sh health

# Reiniciar solo la base de datos
docker-compose restart mysql
```

#### 3. Frontend no se conecta al backend
Verificar que la configuración en `portal-cotizador/js/config.js` apunte correctamente:
```javascript
const API_CONFIG = {
    BASE_URL: 'http://localhost:8080/cotizador/v1/api',
    // ...
};
```

#### 4. Permisos en Linux
```bash
# Dar permisos al script
chmod +x docker-scripts.sh

# Si hay problemas con volúmenes
sudo chown -R $USER:$USER ./logs
```

### Logs y Debugging

```bash
# Ver logs específicos
docker-compose logs backend
docker-compose logs mysql
docker-compose logs frontend

# Acceder al shell del contenedor
./docker-scripts.sh shell-backend
./docker-scripts.sh shell-mysql

# Verificar recursos
docker stats
```

## 🧹 Limpieza

### Limpieza básica
```bash
# Detener servicios
./docker-scripts.sh stop

# Limpiar contenedores e imágenes
./docker-scripts.sh clean
```

### Reset completo
```bash
# ⚠️ CUIDADO: Elimina todos los datos
./docker-scripts.sh reset
```

## 🔄 Desarrollo

### Rebuild después de cambios en código

```bash
# Solo rebuild del servicio modificado
docker-compose build backend
docker-compose up -d backend

# O usar el script
./docker-scripts.sh rebuild
```

### Hot reload (desarrollo)

Para desarrollo, puedes montar volúmenes para hot reload:

```yaml
# En docker-compose.override.yml
services:
  frontend:
    volumes:
      - ./portal-cotizador:/usr/share/nginx/html
```

## 📈 Monitoreo

### Health Checks

Todos los servicios incluyen health checks:
- **MySQL**: `mysqladmin ping`
- **Backend**: `/actuator/health`
- **Frontend**: HTTP GET `/`

```bash
# Verificar salud
./docker-scripts.sh health

# Ver detalles en Docker
docker-compose ps
```

### Métricas de Recursos

```bash
# Ver uso de recursos
docker stats

# Ver logs de performance
docker-compose logs backend | grep -i performance
```

## 🔒 Seguridad

### Configuración de Producción

Para producción, modifica:

1. **Contraseñas**: Cambia todas las contraseñas por defecto
2. **SSL/TLS**: Configura certificados SSL
3. **Red**: Usa redes internas para comunicación entre servicios
4. **Secrets**: Usa Docker secrets para información sensible

```yaml
# Ejemplo de configuración segura
services:
  mysql:
    environment:
      MYSQL_ROOT_PASSWORD_FILE: /run/secrets/mysql_root_password
    secrets:
      - mysql_root_password

secrets:
  mysql_root_password:
    file: ./secrets/mysql_root_password.txt
```

## 📚 Referencias

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
- [MySQL Docker Hub](https://hub.docker.com/_/mysql)
- [Nginx Docker Hub](https://hub.docker.com/_/nginx)

## ❓ Soporte

Si encuentras problemas:

1. Verifica los logs: `./docker-scripts.sh logs`
2. Verifica la salud: `./docker-scripts.sh health`
3. Reinicia los servicios: `./docker-scripts.sh restart`
4. Si persiste, haz un reset: `./docker-scripts.sh reset`

---

**¡Listo!** Tu sistema Cotizador debería estar funcionando en Docker. 🎉 