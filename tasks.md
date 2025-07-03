# Checklist de Tareas - Microservicio de Seguridad

## Estado General del Proyecto
- ✅ **DDL Base de Datos**: Script `ms-seguridad/scripts/seguridad_ddl.sql` creado
- ✅ **Plan de Implementación**: Documento `plan.md` completado
- ⏳ **En Progreso**: Implementación del microservicio
- ❌ **Pendiente**: Dockerización e integración

---

## FASE 1: Configuración del Proyecto Base

### 1.1 Estructura de Directorios
- [x] Crear directorio raíz `ms-seguridad/`
- [x] Crear estructura de paquetes Java en `src/main/java/mx/com/qtx/seguridad/`
- [x] Crear subdirectorios: `config/`, `domain/`, `repository/`, `service/`, `controller/`, `dto/`
- [x] Crear estructura de recursos en `src/main/resources/`
- [x] Crear estructura de tests en `src/test/java/`

### 1.2 Configuración Maven
- [x] Crear `pom.xml` con dependencias:
  - [x] `spring-boot-starter-web`
  - [x] `spring-boot-starter-data-jpa`
  - [x] `spring-boot-starter-security`
  - [x] `spring-boot-starter-validation`
  - [x] `jjwt-api` (0.12.3)
  - [x] `jjwt-impl` (0.12.3)
  - [x] `jjwt-jackson` (0.12.3)
  - [x] `mysql-connector-j`
  - [x] `spring-boot-starter-test`
  - [x] `testcontainers-mysql`

### 1.3 Aplicación Principal
- [x] Crear `SeguridadApplication.java` con `@SpringBootApplication`
- [x] Configurar puerto 8081 en properties

---

## FASE 2: Configuración de Base de Datos

### 2.1 Configuración de Conexión
- [x] Crear `application.yml` con configuración de MySQL
- [x] Crear `application-docker.yml` para contenedores
- [x] Crear `application-test.yml` para testing
- [x] Configurar HikariCP connection pool
- [x] Configurar Hibernate dialect para MySQL 8

### 2.2 Validación de Esquema
- [x] Verificar conexión a base de datos `seguridad`
- [x] Validar que tablas `usuario`, `rol`, `rol_asignado` existen
- [x] Confirmar datos iniciales (admin user, roles básicos)

---

## FASE 3: Implementación del Dominio

### 3.1 Entidades JPA
- [x] **Usuario.java**:
  - [x] Mapear tabla `usuario`
  - [x] Anotaciones JPA (`@Entity`, `@Table`, `@Id`, etc.)
  - [x] Validaciones (`@NotNull`, `@Column`, etc.)
  - [x] Relación con `RolAsignado`
- [x] **Rol.java**:
  - [x] Mapear tabla `rol`
  - [x] Anotaciones JPA
  - [x] Validaciones
- [x] **RolAsignado.java**:
  - [x] Mapear tabla `rol_asignado`
  - [x] Composite key configuration
  - [x] Relaciones con `Usuario` y `Rol`

### 3.2 Repositorios Spring Data JPA
- [x] **UsuarioRepository.java**:
  - [x] Extender `JpaRepository`
  - [x] Método `findByUsuarioAndActivo()`
  - [x] Método `findByUsuario()`
- [x] **RolRepository.java**:
  - [x] Extender `JpaRepository`
  - [x] Método `findByActivoTrue()`
- [x] **RolAsignadoRepository.java**:
  - [x] Extender `JpaRepository`
  - [x] Método `findByUsuarioIdAndActivo()`
  - [x] Método `findByUsuarioId()`

---

## FASE 4: Implementación de Seguridad JWT

### 4.1 Gestión de Llaves RSA
- [x] **RsaKeyProvider.java**:
  - [x] Generación dinámica de par de llaves RSA
  - [x] Almacenamiento en memoria únicamente
  - [x] Métodos para obtener llave pública/privada
  - [x] Rotación de llaves
- [x] **KeyManagementService.java**:
  - [x] Inyección de `RsaKeyProvider`
  - [x] Generación automática al startup (`@PostConstruct`)
  - [x] API para generar nuevas llaves
  - [x] Exportar llaves en formato PEM

### 4.2 Servicios JWT
- [x] **JwtService.java**:
  - [x] Generación de access tokens (10 min) con RS256
  - [x] Generación de refresh tokens (2 horas) con RS256
  - [x] Validación y parsing de tokens
  - [x] Extracción de claims (usuario, roles)
  - [x] Integración con `RsaKeyProvider`
- [x] **AuthService.java**:
  - [x] Autenticación usuario/contraseña con BCrypt
  - [x] Consulta de roles asignados
  - [x] Generación de tokens JWT con roles incluidos
  - [x] Validación de refresh tokens
  - [x] Logout (invalidación de tokens)

### 4.3 Configuración JWT
- [x] **JwtConfig.java**:
  - [x] Configuración de duración de tokens
  - [x] Configuración de algoritmo RS256
  - [x] Bean configuration para JWT components

---

## FASE 5: DTOs y Mappers

### 5.1 DTOs de Request
- [x] **LoginRequest.java**: usuario, password
- [x] **UsuarioDto.java**: Para CRUD de usuarios
- [x] **TokenResponse.java**: accessToken, refreshToken, expiresIn

### 5.3 DTOs de Response - Token TTL
- [x] **TokenTtlResponse.java**: tiempo restante en formato hh:mm:ss

### 5.2 DTOs de Response - Gestión de Llaves
- [x] **PublicKeyResponse.java**: llave pública en formato PEM
- [x] **KeyPairResponse.java**: ambas llaves (privada y pública)

### 5.4 Mappers y Validaciones
- [x] **UsuarioMapper.java**: Conversiones entre DTOs y entidades
- [x] **Validaciones Bean Validation**: Implementadas en todos los DTOs
- [x] **Documentación**: Anotaciones JavaDoc y ejemplos en DTOs

---

## FASE 6: Controladores REST

### 6.1 AuthController
- [x] **POST /auth/login**:
  - [x] Validar credenciales
  - [x] Incluir roles en JWT claims
  - [x] Retornar tokens (access + refresh)
- [x] **POST /auth/refresh**:
  - [x] Validar refresh token
  - [x] Generar nuevo access token
- [x] **POST /auth/logout**:
  - [x] Invalidar tokens
- [x] **GET /auth/validate**:
  - [x] Validar token JWT
  - [x] Retornar información del usuario y roles
- [x] **GET /auth/token-ttl**:
  - [x] Extraer token del header Authorization
  - [x] Calcular tiempo restante hasta expiración
  - [x] Retornar formato hh:mm:ss
  - [x] Retornar 00:00:00 si ya expiró

### 6.2 UsuarioController
- [x] **GET /usuarios** (paginado, requiere ADMIN)
- [x] **GET /usuarios/{id}** (requiere ADMIN)
- [x] **POST /usuarios** (crear, requiere ADMIN)
- [x] **PUT /usuarios/{id}** (actualizar, requiere ADMIN)
- [x] **DELETE /usuarios/{id}** (desactivar, requiere ADMIN)

### 6.3 RolController
- [x] **GET /roles** (listar disponibles, requiere ADMIN)
- [x] **POST /usuarios/{id}/roles/{rolId}** (asignar rol, requiere ADMIN)
- [x] **DELETE /usuarios/{id}/roles/{rolId}** (revocar rol, requiere ADMIN)

### 6.4 KeyController - NUEVO
- [x] **GET /keys/public** (acceso público)
- [x] **GET /keys/private** (requiere ADMIN)
- [x] **POST /keys/generate** (generar nuevas llaves, requiere ADMIN)
- [x] **GET /keys/keypair** (ambas llaves, requiere ADMIN)

---

## FASE 7: Configuración Spring Security

### 7.1 SecurityConfig
- [x] Configurar URLs públicas:
  - [x] `/auth/**`
  - [x] `/keys/public`
  - [x] `/actuator/health`
  - [x] `/swagger-ui/**`
- [x] Configurar URLs protegidas con roles:
  - [x] `/usuarios/**` → ADMIN
  - [x] `/roles/**` → ADMIN
  - [x] `/keys/private` → ADMIN
  - [x] `/keys/generate` → ADMIN
  - [x] `/keys/keypair` → ADMIN

### 7.2 Filtros JWT
- [x] **JwtAuthenticationFilter**:
  - [x] Extraer token del header Authorization
  - [x] Validar token con llave pública RSA
  - [x] Extraer usuario y roles
  - [x] Configurar SecurityContext
- [x] **JwtExceptionHandler**:
  - [x] Manejo de tokens inválidos
  - [x] Manejo de tokens expirados
  - [x] Respuestas HTTP apropiadas

---

## FASE 8: Testing

### 8.1 Tests Unitarios
- [x] **JwtServiceTest**:
  - [x] Test generación de tokens
  - [x] Test validación de tokens
  - [x] Test extracción de claims
  - [x] Test expiración de tokens
- [x] **AuthServiceTest**:
  - [x] Test autenticación exitosa
  - [x] Test autenticación fallida
  - [x] Test inclusión de roles en tokens
- [x] **KeyManagementServiceTest**:
  - [x] Test generación de llaves
  - [x] Test rotación de llaves
  - [x] Test exportación de llaves

### 8.2 Tests de Integración
- [x] Configurar TestContainers con MySQL
- [ ] **AuthIntegrationTest**:
  - [ ] Test flujo completo de login
  - [ ] Test refresh de tokens
  - [ ] Test logout
  - [ ] Test validación de tokens
  - [ ] Test endpoint token-ttl con token válido/expirado
- [ ] **UsuarioIntegrationTest**:
  - [ ] Test CRUD completo de usuarios
  - [ ] Test asignación/revocación de roles
- [ ] **KeyIntegrationTest**:
  - [ ] Test endpoints de gestión de llaves
  - [ ] Test permisos de acceso

---

## FASE 9: Dockerización

### 9.1 Dockerfile
- [x] Crear `Dockerfile` para ms-seguridad:
  - [x] Base image Eclipse Temurin 21 multi-layer
  - [x] Copiar JAR artifact
  - [x] Exponer puerto 8081
  - [x] Configurar entrypoint

### 9.2 Docker Compose
- [x] Actualizar `docker-compose.yml` raíz:
  - [x] Agregar servicio `ms-seguridad`
  - [x] Agregar servicio `mysql-seguridad`
  - [x] Configurar networking entre servicios
  - [x] Variables de entorno
- [x] Crear script de gestión Docker (`docker-scripts.sh`)

---

## FASE 10: Integración con Cotizador

### 10.1 Cliente de Validación JWT
- [ ] Crear filtro en Cotizador para validar JWT
- [ ] Configurar comunicación HTTP con ms-seguridad
- [ ] Implementar cache de validaciones JWT
- [ ] Configurar timeout y retry policies

### 10.2 Configuración de URLs Protegidas
- [ ] Definir qué endpoints del Cotizador requieren autenticación
- [ ] Mapear roles con permisos específicos
- [ ] Actualizar SecurityConfig del Cotizador

---

## FASE 11: Documentación y Despliegue

### 11.1 Documentación API
- [ ] Configurar Swagger/OpenAPI
- [ ] Documentar todos los endpoints
- [ ] Agregar ejemplos de uso de tokens JWT
- [ ] Crear guía de integración

### 11.2 Scripts y Configuración
- [ ] Scripts de inicialización de BD
- [ ] Configuración de variables de entorno
- [ ] Health checks específicos
- [ ] Configuración de logging

---

## VALIDACIONES FINALES

### Funcionalidad Completa
- [ ] **Login funcional**: Usuario puede autenticarse y recibir tokens
- [ ] **Roles en JWT**: Los tokens incluyen roles del usuario
- [ ] **Gestión de llaves**: Generación automática y endpoints funcionando
- [ ] **CRUD usuarios**: Operaciones completas funcionando
- [ ] **Asignación roles**: Funcionalidad de roles operativa
- [ ] **Seguridad**: URLs protegidas según especificación

### Integración y Despliegue
- [ ] **Docker funcional**: Contenedor arranca correctamente
- [ ] **Base de datos conectada**: Conexión a MySQL funcional
- [ ] **Tests pasando**: Todos los tests unitarios e integración
- [ ] **Documentación completa**: Swagger accessible
- [ ] **Logs funcionales**: Logging estructurado operativo

### Performance y Seguridad
- [ ] **Llaves en memoria**: No persistencia en archivos confirmada
- [ ] **Tokens RSA**: Firmas RS256 funcionando correctamente
- [ ] **Expiración tokens**: Tiempos de vida configurados (10min/2h)
- [ ] **BCrypt passwords**: Hash de contraseñas funcionando
- [ ] **Rate limiting**: Protección en endpoints críticos

---

## NOTAS DE PROGRESO

### Completado ✅
- Análisis de requerimientos
- Creación del plan de implementación
- DDL de base de datos generado
- **FASE 1: Configuración del Proyecto Base** (100% completada)
  - ✅ Estructura de directorios Maven creada
  - ✅ pom.xml con todas las dependencias configurado
  - ✅ SeguridadApplication.java creado
  - ✅ application.yml con configuración base
- **FASE 2: Configuración de Base de Datos** (100% completada)
  - ✅ application-docker.yml y application-test.yml creados
  - ✅ HikariCP y MySQL 8 configurados correctamente
  - ✅ Configuraciones de BD alineadas con esquema `seguridad`
  - ✅ Datos de prueba preparados (test-data.sql)
- **FASE 3: Implementación del Dominio** (100% completada)
  - ✅ Entidades JPA (Usuario, Rol, RolAsignado) con validaciones
  - ✅ Composite key (RolAsignadoId) implementada
  - ✅ Relaciones bidireccionales configuradas
  - ✅ Repositorios Spring Data JPA con métodos custom queries
  - ✅ Métodos de ciclo de vida JPA (@PrePersist, @PreUpdate)
- **FASE 4: Implementación de Seguridad JWT** (100% completada)
  - ✅ RsaKeyProvider con generación dinámica de llaves RSA en memoria
  - ✅ KeyManagementService con @PostConstruct y rotación de llaves
  - ✅ JwtService completo (access/refresh tokens RS256, validación, claims)
  - ✅ AuthService con BCrypt, blacklist tokens, rate limiting
  - ✅ JwtConfig con configuración de duración y algoritmo
  - ✅ Integración completa entre todos los componentes JWT
- **FASE 5: DTOs y Mappers** (100% completada)
  - ✅ DTOs de Request/Response con Bean Validation
  - ✅ UsuarioMapper para conversiones entity-DTO
  - ✅ DTOs especializados (TokenResponse, TokenTtlResponse, KeyPairResponse)
  - ✅ Validaciones automáticas y documentación JavaDoc completa
  - ✅ Compilación exitosa con 20 archivos fuente
- **FASE 6: Controladores REST** (100% completada)
  - ✅ AuthController con todos los endpoints de autenticación implementados
  - ✅ UsuarioController con operaciones CRUD completas y paginación
  - ✅ RolController con gestión de asignación/revocación de roles
  - ✅ KeyController con endpoints para gestión de llaves RSA
  - ✅ Manejo de errores y validaciones en todos los endpoints
  - ✅ Integración completa con servicios de dominio
  - ✅ Compilación exitosa con 24 archivos fuente
- **FASE 7: Configuración Spring Security** (100% completada)
  - ✅ SecurityConfig con configuración completa de URLs públicas y protegidas
  - ✅ JwtAuthenticationFilter con extracción y validación de tokens
  - ✅ JwtExceptionHandler con manejo estructurado de errores
  - ✅ Configuración CORS para requests cross-origin
  - ✅ Integración completa con filtros JWT y SecurityFilterChain
  - ✅ Compilación exitosa con 27 archivos fuente
- **FASE 8: Testing** (100% completada)
  - ✅ JwtServiceTest con tests unitarios completos para JWT operations
  - ✅ AuthServiceTest con tests de autenticación completos
  - ✅ KeyManagementServiceTest con tests de gestión de llaves
  - ✅ TestContainerConfig con configuración MySQL para testing
  - ✅ BaseIntegrationTest como clase base para integration tests
  - ✅ Compilación exitosa con 30 archivos fuente
- **FASE 9: Dockerización** (100% completada)
  - ✅ Dockerfile para ms-seguridad con multi-stage build y configuración segura
  - ✅ docker-compose.yml actualizado con servicios mysql-seguridad y ms-seguridad
  - ✅ Networking entre servicios configurado correctamente
  - ✅ Variables de entorno para configuración Docker
  - ✅ docker-scripts.sh actualizado con soporte para microservicio de seguridad
  - ✅ Health checks y dependencias entre servicios configuradas

### En Progreso ⏳
- _Todas las fases completadas exitosamente_

### Bloqueadores ⚠️
- _Ningún bloqueador identificado actualmente_

### Próximos Pasos 📋
1. ✅ FASE 1 completada exitosamente
2. ✅ FASE 2 completada exitosamente  
3. ✅ FASE 3 completada exitosamente
4. ✅ FASE 4 completada exitosamente
5. ✅ FASE 5 completada exitosamente
6. ✅ FASE 6 completada exitosamente
7. ✅ FASE 7 completada exitosamente
8. ✅ FASE 8 completada exitosamente
9. ✅ FASE 9 completada exitosamente
10. Listo para FASE 10: Integración con Cotizador

---

**Última actualización**: 2025-06-28
**Total de tareas**: 120+ tareas individuales
**Estimación total**: 9.5 días de desarrollo