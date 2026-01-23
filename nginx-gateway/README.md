# 🌐 API Gateway con Nginx

> **Gateway de red seguro** para el sistema CotizadorPcPartes, proporcionando un punto único de acceso con microservicios en red privada.

[![Nginx](https://img.shields.io/badge/Nginx-1.25-green.svg)](https://nginx.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

## 📌 Navegación del Proyecto

- **📖 [README Principal](../README.md)** - Documentación completa del sistema
- **🚀 [Portal Web](../portal-cotizador/README.md)** - Frontend Vue.js 3
- **🖥️ [Backend API](../ms-cotizador-componentes/README.md)** - Microservicio Spring Boot
- **🔒 [Seguridad](../ms-seguridad/)** - Microservicio de autenticación
- **📚 [Documentación](../documentacion/)** - Diagramas y arquitectura

---

## 🎯 Arquitectura de Red Segura

Este directorio contiene la configuración de **nginx como API Gateway** para el sistema CotizadorPcPartes, proporcionando un punto único de acceso que mantiene los microservicios en una red privada.

## 🏗️ Arquitectura

```
Internet
    ↓
[Puerto 80] ← ÚNICO PUERTO EXPUESTO
    ↓
Nginx Gateway (cotizador-gateway)
    ↓
├── Frontend (Vue.js) ← Proxy interno
├── /api/componentes/* → Componentes (ms-cotizador-componentes:8082) ← Red privada
├── /api/cotizaciones/* → Cotizaciones (ms-cotizador-cotizaciones:8083) ← Red privada
├── /api/pedidos/* → Pedidos (ms-cotizador-pedidos:8084) ← Red privada
└── /api/seguridad/* → Seguridad (ms-seguridad:8081) ← Red privada
    ↓
Bases de Datos (MySQL) ← Red privada
```

## 🔒 Ventajas de Seguridad

1. **Exposición Mínima**: Solo el puerto 80 es accesible desde el exterior
2. **Red Privada**: Microservicios y bases de datos son inaccesibles directamente
3. **Rate Limiting**: Protección contra abuso de APIs
4. **Headers de Seguridad**: Configurados automáticamente
5. **Logs Centralizados**: Monitoreo de todas las requests

## 📁 Estructura de Archivos

```
nginx-gateway/
├── Dockerfile              # Construcción del contenedor gateway
├── nginx.conf              # Configuración principal de nginx
├── conf/
│   └── gateway.conf         # Configuración específica del gateway
└── README.md               # Este archivo
```

## 🚀 Uso

### Levantar el sistema con Gateway

```bash
# Usar el docker-compose con gateway
docker-compose -f docker-compose.gateway.yml up -d

# Verificar estado
docker-compose -f docker-compose.gateway.yml ps
```

### Acceso desde Otros Equipos

1. **Desde la misma red local**:
   ```
   http://IP_DEL_SERVIDOR
   ```

2. **Configurar firewall** (si es necesario):
   ```bash
   # Permitir puerto 80
   sudo ufw allow 80
   ```

3. **No exponer puertos de microservicios** - Solo el gateway debe estar expuesto

## 🛣️ Rutas Disponibles

### Frontend (Vue.js 3 SPA)
- `GET /` → Portal de cotización Vue.js 3
- `GET /login` → Página de autenticación
- `GET /componentes` → Gestión de componentes (Vue Router)
- `GET /cotizaciones` → Sistema de cotizaciones (Vue Router)
- `GET /pcs` → Armado de PCs (Vue Router)
- `GET /proveedores` → Gestión de proveedores (Vue Router)
- `GET /pedidos` → Gestión de pedidos (Vue Router)
- `GET /promociones` → Sistema de promociones (Vue Router)

### APIs (Proxy automático)
- `POST /api/seguridad/auth/login` → Login JWT
- `GET /api/componentes` → CRUD componentes
- `GET /api/pcs` → CRUD PCs y armado
- `GET /api/promociones` → CRUD promociones
- `GET /api/cotizaciones` → CRUD cotizaciones
- `GET /api/proveedores` → CRUD proveedores
- `GET /api/pedidos` → CRUD pedidos
- *Todas las rutas de los microservicios están disponibles*

### Monitoreo
- `GET /health` → Status del gateway
- `GET /gateway/status` → Status completo
- `GET /gateway/info` → Información del gateway

## ⚙️ Configuración

### Rate Limiting
- **APIs generales**: 100 requests/minuto por IP
- **Autenticación**: 20 requests/minuto por IP

### Timeouts
- **Conexión**: 5 segundos
- **APIs generales**: 30 segundos  
- **Autenticación**: 15 segundos

### Logs
- **Access Log**: `/var/log/nginx/gateway/access.log`
- **Error Log**: `/var/log/nginx/gateway/error.log`
- **Gateway Log**: `/var/log/nginx/gateway/gateway_access.log`

## 🔧 Personalización

### Agregar Nuevo Microservicio

1. **Agregar upstream** en `nginx.conf`:
   ```nginx
   upstream backend-nuevo {
       server nuevo-service:8082;
       keepalive 32;
   }
   ```

2. **Agregar location** en `gateway.conf`:
   ```nginx
   location /api/nuevo/ {
       rewrite ^/api/nuevo/(.*) /nuevo/v1/api/$1 break;
       proxy_pass http://backend-nuevo;
       # ... configuración de headers
   }
   ```

### Modificar Rate Limiting

Editar en `nginx.conf`:
```nginx
limit_req_zone $binary_remote_addr zone=api:10m rate=200r/m;  # Más requests
```

## 🌐 Acceso Remoto

### Configuración para Producción

1. **Usar dominio real**:
   ```nginx
   server_name tu-dominio.com www.tu-dominio.com;
   ```

2. **Configurar HTTPS**:
   ```nginx
   listen 443 ssl http2;
   ssl_certificate /path/to/cert.pem;
   ssl_certificate_key /path/to/key.pem;
   ```

3. **Actualizar variables de entorno**:
   ```bash
   VITE_API_BASE_URL=https://tu-dominio.com/api/cotizador
   VITE_SEGURIDAD_API_BASE_URL=https://tu-dominio.com/api/seguridad
   ```

## 📊 Monitoreo

### Health Checks Disponibles

```bash
# Gateway status
curl http://localhost/health

# Información del gateway  
curl http://localhost/gateway/info

# Status de upstreams
curl http://localhost/gateway/status
```

### Ver Logs en Tiempo Real

```bash
# Logs del gateway
docker logs -f cotizador-gateway

# Logs específicos (si tienes acceso al contenedor)
docker exec cotizador-gateway tail -f /var/log/nginx/gateway/access.log
```

## 🚨 Troubleshooting

### Problemas Comunes

1. **502 Bad Gateway**: Microservicio no disponible
   - Verificar que los contenedores backend estén ejecutándose
   - Revisar logs: `docker logs cotizador-backend`

2. **404 en APIs**: Ruta mal configurada
   - Verificar rewrite rules en `gateway.conf`
   - Confirmar context path de microservicios

3. **CORS Errors**: Headers mal configurados
   - Revisar headers de proxy en configuración
   - Verificar configuración CORS en microservicios

### Debug Mode

Para debugging temporal, modificar `nginx.conf`:
```nginx
error_log /var/log/nginx/error.log debug;
```

## 🔄 Comandos Útiles

```bash
# Recargar configuración sin downtime
docker exec cotizador-gateway nginx -s reload

# Verificar configuración
docker exec cotizador-gateway nginx -t

# Ver configuración activa
docker exec cotizador-gateway nginx -T
```

---

## 📞 Soporte

### 🔗 **Enlaces Útiles**

- **📖 [Documentación Principal](../README.md)** - Guía completa del sistema
- **🚀 [Portal Web](../portal-cotizador/README.md)** - Frontend Vue.js 3
- **🖥️ [Backend API](../ms-cotizador/README.md)** - Microservicio Spring Boot
- **🔒 [Seguridad](../ms-seguridad/)** - Microservicio de autenticación
- **📚 [Nginx Docs](https://nginx.org/en/docs/)** - Documentación oficial

### 🆘 **Soporte Técnico**

- **Issues**: Crear issue en GitHub
- **Health Check**: http://localhost/health
- **Gateway Status**: http://localhost/gateway/status
- **Logs**: `docker logs cotizador-gateway`

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [LICENSE](../LICENSE) para más detalles.

---

<div align="center">

**🌐 API Gateway con Nginx**

*Gateway de red seguro para microservicios*

[![Nginx](https://img.shields.io/badge/Nginx-1.25-green.svg)](https://nginx.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

**[⬆️ Volver al README Principal](../README.md)**

</div>