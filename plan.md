# Plan de Implementación - Microservicio de Seguridad

## Resumen del Proyecto

Implementación de un microservicio de seguridad (`ms-seguridad`) para manejo de autenticación JWT, gestión de usuarios y control de acceso basado en roles para el sistema CotizadorPcPartes.

## Arquitectura del Sistema

```
CotizadorPcPartes (Proyecto Principal)
├── Cotizador/                    # Servicio principal existente
├── ms-seguridad/                 # Nuevo microservicio de seguridad
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── src/test/java/
│   ├── scripts/                  # Scripts de BD (ya creado)
│   └── pom.xml
└── docker-compose.yml            # Actualizar para incluir ms-seguridad
```

## Stack Tecnológico - ms-seguridad

- **Java**: 21
- **Spring Boot**: 3.5.3
- **Spring Data JPA**: Para persistencia
- **Spring Security**: Control de acceso
- **MySQL**: Base de datos `seguridad`
- **JWT Libraries**: 
  - `jjwt-api`
  - `jjwt-impl` 
  - `jjwt-jackson`
- **BCrypt**: Hash de contraseñas
- **Maven**: Gestión de dependencias

## Fase 1: Configuración del Proyecto Base

### 1.1 Estructura del Proyecto
```bash
ms-seguridad/
├── pom.xml
├── src/main/java/mx/com/qtx/seguridad/
│   ├── SeguridadApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtConfig.java
│   │   ├── RsaKeyProvider.java
│   │   └── DatabaseConfig.java
│   ├── domain/
│   │   ├── Usuario.java
│   │   ├── Rol.java
│   │   └── RolAsignado.java
│   ├── repository/
│   │   ├── UsuarioRepository.java
│   │   ├── RolRepository.java
│   │   └── RolAsignadoRepository.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UsuarioService.java
│   │   ├── JwtService.java
│   │   ├── RolService.java
│   │   └── KeyManagementService.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UsuarioController.java
│   │   └── KeyController.java
│   └── dto/
│       ├── LoginRequest.java
│       ├── TokenResponse.java
│       ├── UsuarioDto.java
│       ├── KeyPairResponse.java
│       └── PublicKeyResponse.java
└── src/main/resources/
    ├── application.yml
    ├── application-docker.yml
    └── application-test.yml
```

### 1.2 Dependencias Maven (pom.xml)
```xml
<!-- Spring Boot Starters -->
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-validation

<!-- JWT Dependencies -->
- jjwt-api (0.12.3)
- jjwt-impl (0.12.3)
- jjwt-jackson (0.12.3)

<!-- Database -->
- mysql-connector-j

<!-- Testing -->
- spring-boot-starter-test
- testcontainers-mysql
```

## Fase 2: Configuración de Base de Datos

### 2.1 Base de Datos
- **Nombre**: `seguridad`
- **Esquema**: Ya creado en `ms-seguridad/scripts/seguridad_ddl.sql`
- **Tablas principales**:
  - `usuario` (id, usuario, password, activo, fechas)
  - `rol` (id, nombre, activo, fechas)
  - `rol_asignado` (id_usuario, id_rol, activo, fechas)

### 2.2 Configuración de Conexión
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/seguridad
    username: ${DB_USERNAME:seguridad_user}
    password: ${DB_PASSWORD:seguridad_pass}
  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.MySQL8Dialect
```

## Fase 3: Implementación del Dominio

### 3.1 Entidades JPA
- **Usuario**: Mapeo de tabla `usuario` con validaciones
- **Rol**: Catálogo de roles del sistema
- **RolAsignado**: Tabla de relación many-to-many

### 3.2 Repositorios
- Usar Spring Data JPA con métodos personalizados
- Queries para autenticación y validación de roles

## Fase 4: Implementación de Seguridad JWT

### 4.1 Configuración JWT
- **Access Token**: Duración 10 minutos
- **Refresh Token**: Duración 2 horas
- **Algoritmo**: RS256 (clave pública/privada)
- **Claims**: usuario, **roles asignados**, timestamps
- **Llaves**: Par de llaves RSA **generadas dinámicamente en memoria**
- **Gestión de llaves**: Generación automática al inicio + endpoints para renovación

### 4.2 Servicios de Seguridad
```java
@Service
public class AuthService {
    // Autenticación usuario/contraseña
    // Generación de tokens JWT con RS256
    // Validación de refresh tokens
}

@Service
public class JwtService {
    // Generación de access/refresh tokens con RS256
    // Validación y parsing de tokens
    // Extracción de claims
    // Manejo de llaves RSA públicas/privadas
}

@Component
public class RsaKeyProvider {
    // Generación dinámica de par de llaves RSA en memoria
    // Proveer llave privada para firmar
    // Proveer llave pública para verificar
    // Rotación de llaves por demanda
}

@Service
public class KeyManagementService {
    // Gestión completa de llaves RSA
    // Generación automática al startup
    // API para generar nuevas llaves
    // Exportar llaves públicas/privadas
}
```

## Fase 5: Endpoints REST

### 5.1 Endpoints de Autenticación (`/auth`)
```
POST /auth/login          # Login usuario/contraseña (incluye roles en token)
POST /auth/refresh        # Renovar access token
POST /auth/logout         # Invalidar tokens
GET  /auth/validate       # Validar token JWT
GET  /auth/token-ttl      # Obtener vigencia restante del token (hh:mm:ss)
```

### 5.2 Endpoints CRUD Usuarios (`/usuarios`)
```
GET    /usuarios          # Listar usuarios (paginado)
GET    /usuarios/{id}     # Obtener usuario por ID
POST   /usuarios          # Crear nuevo usuario
PUT    /usuarios/{id}     # Actualizar usuario
DELETE /usuarios/{id}     # Desactivar usuario
```

### 5.3 Endpoints de Roles (`/roles`)
```
GET    /roles             # Listar roles disponibles
POST   /usuarios/{id}/roles/{rolId}    # Asignar rol
DELETE /usuarios/{id}/roles/{rolId}    # Revocar rol
```

### 5.4 Endpoints de Gestión de Llaves (`/keys`) - **NUEVO**
```
GET    /keys/public       # Obtener llave pública actual
GET    /keys/private      # Obtener llave privada actual (ADMIN only)
POST   /keys/generate     # Generar nuevo par de llaves (ADMIN only)
GET    /keys/keypair      # Obtener ambas llaves (ADMIN only)
```

## Fase 6: Configuración de Spring Security

### 6.1 URLs Públicas
- `/auth/**` - Endpoints de autenticación
- `/keys/public` - Llave pública (para validación externa)
- `/actuator/health` - Health check
- `/swagger-ui/**` - Documentación API

### 6.2 URLs Protegidas
- `/usuarios/**` - Requiere rol ADMIN
- `/roles/**` - Requiere rol ADMIN
- `/keys/private` - Requiere rol ADMIN
- `/keys/generate` - Requiere rol ADMIN
- `/keys/keypair` - Requiere rol ADMIN
- Otras URLs - Requiere autenticación válida

### 6.3 Filtros de Seguridad
- JWT Authentication Filter
- Exception handling para tokens inválidos/expirados

## Fase 7: Testing

### 7.1 Test de Integración
- Usar TestContainers con MySQL
- Tests end-to-end de flujos de autenticación
- Validación de roles y permisos

### 7.2 Test Unitarios
- Servicios de JWT
- Lógica de autenticación
- Validaciones de dominio

## Fase 8: Dockerización

### 8.1 Dockerfile para ms-seguridad
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/ms-seguridad-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 8.2 Actualizar docker-compose.yml
```yaml
services:
  ms-seguridad:
    build: ./ms-seguridad
    ports:
      - "8081:8081"
    depends_on:
      - mysql-seguridad
    environment:
      - DB_HOST=mysql-seguridad
      
  mysql-seguridad:
    image: mysql:8.4.4
    environment:
      - MYSQL_DATABASE=seguridad
      - MYSQL_USER=seguridad_user
      - MYSQL_PASSWORD=seguridad_pass
```

## Fase 9: Integración con Cotizador

### 9.1 Middleware de Autenticación
- Crear filtro en Cotizador para validar JWT
- Comunicación HTTP entre microservicios
- Cache de validaciones JWT

### 9.2 Configuración de URLs Protegidas
- Definir qué endpoints del Cotizador requieren autenticación
- Mapeo de roles con permisos específicos

## Fase 10: Documentación y Despliegue

### 10.1 Documentación API
- Swagger/OpenAPI para endpoints
- Ejemplos de uso de tokens JWT
- Guía de integración

### 10.2 Scripts de Despliegue
- Scripts de inicialización de BD
- Configuración de variables de entorno
- Health checks y monitoreo

## Cronograma Estimado

| Fase | Descripción | Duración | Dependencias |
|------|-------------|----------|--------------|
| 1 | Configuración proyecto base | 1 día | - |
| 2 | Configuración BD | 0.5 días | Fase 1 |
| 3 | Implementación dominio | 1 día | Fase 2 |
| 4 | JWT y seguridad | 2 días | Fase 3 |
| 5 | Endpoints REST | 1.5 días | Fase 4 |
| 6 | Spring Security | 1 día | Fase 5 |
| 7 | Testing | 1.5 días | Fases 4-6 |
| 8 | Dockerización | 0.5 días | Fase 7 |
| 9 | Integración | 1 día | Fase 8 |
| 10 | Documentación | 0.5 días | Fase 9 |

**Total estimado: 9.5 días de desarrollo**

## Consideraciones Técnicas

### Seguridad
- Passwords hasheados con BCrypt (strength 12)
- JWT tokens con tiempo de vida limitado
- Validación estricta de refresh tokens
- **Llaves RSA almacenadas únicamente en memoria** (no persistencia en archivos)
- **Rotación de llaves bajo demanda** para mayor seguridad
- **Roles incluidos en JWT claims** para autorización
- Rate limiting en endpoints de autenticación

### Performance
- Cache de validaciones JWT en memoria
- Índices optimizados en base de datos
- Connection pooling con HikariCP

### Escalabilidad
- Preparado para múltiples instancias
- Base de datos compartida para sesiones
- Configuración externa via environment variables

### Monitoreo
- Health checks específicos
- Métricas de autenticación fallida
- Logs estructurados para auditoría

## Entregables

1. **Código fuente** completo del microservicio ms-seguridad
2. **Scripts DDL/DML** para base de datos seguridad
3. **Documentación API** con Swagger
4. **Tests automatizados** (unitarios e integración)
5. **Configuración Docker** y docker-compose actualizado
6. **Guía de integración** con el servicio Cotizador
7. **Scripts de despliegue** y configuración

## Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Incompatibilidad JWT con frontend | Media | Alto | Usar estándares JWT y probar temprano |
| Performance de validación tokens | Baja | Medio | Implementar cache y optimizar queries |
| Complejidad integración microservicios | Media | Alto | Fase de integración dedicada con pruebas |
| Seguridad tokens comprometidos | Baja | Alto | Tiempos de vida cortos y refresh tokens |

---

*Este plan está diseñado para ser ejecutado de forma incremental, permitiendo validaciones tempranas y ajustes según sea necesario.*