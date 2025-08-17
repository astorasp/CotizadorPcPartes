# Tareas para Migración de ms-cotizador a 3 Microservicios

## Organización General

**Total de Fases**: 5  
**Total de Subagentes**: 5  
**Formato de Registro**: `[YYYY-MM-DD HH:MM:SS MST] - Agente - Descripción del cambio`  
**Archivo de Seguimiento**: `.documentación/fase 6/changelist.md`

---

## FASE 1: Infraestructura Kafka y Bases de Datos

### Subagente 1: Infraestructura Kafka

#### T1.1 - Configurar Apache Kafka
- **Descripción**: Agregar servicio Kafka en modo KRaft al docker-compose.yml
- **Dependencias**: Ninguna
- **Estimación**: 3-4 horas
- **Criterios de Aceptación**:
  - [ ] Kafka 4.0.0 funcionando en puerto 9092
  - [ ] Modo KRaft configurado (sin Zookeeper)
  - [ ] Health checks configurados
  - [ ] Volúmenes persistentes configurados
  - [ ] Red cotizador-network configurada

#### T1.2 - Configurar Debezium MySQL Connector
- **Descripción**: Implementar CDC para sincronización automática entre bases de datos
- **Dependencias**: T1.1
- **Estimación**: 6-8 horas
- **Criterios de Aceptación**:
  - [ ] Debezium Connector configurado para MySQL
  - [ ] Conectores específicos para cada base de datos
  - [ ] Configuracion de CDC para tablas críticas
  - [ ] Logs de cambios funcionando

#### T1.3 - Crear Topics de Kafka
- **Descripción**: Definir y crear topics para sincronización entre microservicios
- **Dependencias**: T1.1
- **Estimación**: 2-3 horas
- **Criterios de Aceptación**:
  - [ ] Topic `componentes.changes` creado
  - [ ] Topic `promociones.changes` creado
  - [ ] Topic `cotizaciones.changes` creado
  - [ ] Topic `proveedores.changes` creado
  - [ ] Configuracion de particiones y replicación

#### T1.4 - Crear Bases de Datos MySQL
- **Descripción**: Configurar 3 nuevas bases de datos MySQL para cada microservicio
- **Dependencias**: Ninguna
- **Estimación**: 3-4 horas
- **Criterios de Aceptación**:
  - [ ] Base `cotizador_componentes_db` creada
  - [ ] Base `cotizador_cotizaciones_db` creada
  - [ ] Base `cotizador_pedidos_db` creada
  - [ ] Usuarios y permisos configurados
  - [ ] Scripts DDL aplicados

#### T1.5 - Documentar Infraestructura
- **Descripción**: Crear documentación de la infraestructura Kafka
- **Dependencias**: T1.1, T1.2, T1.3, T1.4
- **Estimación**: 2-3 horas
- **Criterios de Aceptación**:
  - [ ] README.md en `kafka-config/` creado
  - [ ] Diagramas de flujo de datos documentados
  - [ ] Configuraciones de conectores documentadas
  - [ ] Troubleshooting guide incluido

---

## FASE 2: ms-cotizador-componentes

### Subagente 2: ms-cotizador-componentes

#### T2.1 - Crear Estructura Base del Microservicio
- **Descripción**: Copiar y adaptar estructura de ms-cotizador para componentes
- **Dependencias**: T1.4
- **Estimación**: 4-5 horas
- **Criterios de Aceptación**:
  - [ ] Carpeta `ms-cotizador-componentes/` creada
  - [ ] pom.xml adaptado con puerto 8082
  - [ ] Dockerfile copiado y adaptado
  - [ ] Estructura de paquetes Java organizada
  - [ ] application.yml configurado

#### T2.2 - Migrar Controladores de Componentes
- **Descripción**: Migrar ComponenteController, PcController, PromocionControlador
- **Dependencias**: T2.1
- **Estimación**: 6-8 horas
- **Criterios de Aceptación**:
  - [ ] ComponenteController migrado y funcional
  - [ ] PcController migrado y funcional
  - [ ] PromocionControlador migrado y funcional
  - [ ] Validaciones RBAC mantenidas
  - [ ] DTOs y mappers migrados

#### T2.3 - Migrar Servicios y Repositorios
- **Descripción**: Migrar lógica de negocio y acceso a datos
- **Dependencias**: T2.2
- **Estimación**: 8-10 horas
- **Criterios de Aceptación**:
  - [ ] ComponenteServicio migrado
  - [ ] PromocionServicio migrado
  - [ ] Repositorios migrados
  - [ ] Entidades JPA migradas
  - [ ] Wrapper classes migradas

#### T2.4 - Configurar Productor Kafka
- **Descripción**: Implementar eventos de cambio hacia Kafka
- **Dependencias**: T2.3, T1.3
- **Estimación**: 5-6 horas
- **Criterios de Aceptación**:
  - [ ] KafkaProducer configurado
  - [ ] Eventos de creacion/actualizacion/eliminacion
  - [ ] Serializacion JSON configurada
  - [ ] Retry logic implementado
  - [ ] Logging de eventos configurado

#### T2.5 - Configurar Seguridad JWT
- **Descripción**: Integrar validación JWT con ms-seguridad
- **Dependencias**: T2.1
- **Estimación**: 4-5 horas
- **Criterios de Aceptación**:
  - [ ] JwtAuthenticationFilter configurado
  - [ ] JWKS client configurado
  - [ ] SessionValidationClient configurado
  - [ ] RBAC annotations funcionando
  - [ ] Cache de tokens configurado

#### T2.6 - Escribir Tests Unitarios
- **Descripción**: Crear tests unitarios para el microservicio
- **Dependencias**: T2.3
- **Estimación**: 6-8 horas
- **Criterios de Aceptación**:
  - [ ] Tests de controladores (>80% cobertura)
  - [ ] Tests de servicios (>80% cobertura)
  - [ ] Tests de repositorios
  - [ ] Tests de integración con TestContainers
  - [ ] Mock de Kafka producer

#### T2.7 - Documentar Microservicio Componentes
- **Descripción**: Crear documentación completa del microservicio
- **Dependencias**: T2.6
- **Estimación**: 3-4 horas
- **Criterios de Aceptación**:
  - [ ] README.md en `ms-cotizador-componentes/` creado
  - [ ] Documentacion de APIs (Swagger)
  - [ ] Javadoc en clases principales
  - [ ] Diagramas de arquitectura interna
  - [ ] Guía de configuración

---

## FASE 3: ms-cotizador-cotizaciones

### Subagente 3: ms-cotizador-cotizaciones

#### T3.1 - Crear Estructura Base del Microservicio
- **Descripción**: Copiar y adaptar estructura para cotizaciones
- **Dependencias**: T1.4
- **Estimación**: 4-5 horas
- **Criterios de Aceptación**:
  - [ ] Carpeta `ms-cotizador-cotizaciones/` creada
  - [ ] pom.xml adaptado con puerto 8083
  - [ ] Dockerfile copiado y adaptado
  - [ ] Estructura de paquetes Java organizada
  - [ ] application.yml configurado

#### T3.2 - Migrar Controlador de Cotizaciones
- **Descripción**: Migrar CotizacionController y lógica asociada
- **Dependencias**: T3.1
- **Estimación**: 6-7 horas
- **Criterios de Aceptación**:
  - [ ] CotizacionController migrado
  - [ ] Validaciones RBAC mantenidas
  - [ ] DTOs de cotizacion migrados
  - [ ] Mappers migrados
  - [ ] Endpoints funcionando

#### T3.3 - Migrar Logica de Dominio
- **Descripción**: Migrar toda la lógica de dominio de cotizaciones
- **Dependencias**: T3.2
- **Estimación**: 10-12 horas
- **Criterios de Aceptación**:
  - [ ] Patron Strategy (ICotizador) migrado
  - [ ] CotizadorA y CotizadorB migrados
  - [ ] Logica de impuestos migrada
  - [ ] Bridge pattern para impuestos funcionando
  - [ ] CotizacionServicio migrado

#### T3.4 - Configurar Consumidor Kafka
- **Descripción**: Implementar sincronización de datos desde componentes/promociones
- **Dependencias**: T3.3, T2.4
- **Estimación**: 6-8 horas
- **Criterios de Aceptación**:
  - [ ] KafkaConsumer configurado
  - [ ] Listener para componentes.changes
  - [ ] Listener para promociones.changes
  - [ ] Deserializacion JSON configurada
  - [ ] Manejo de errores implementado

#### T3.5 - Implementar Cache Local
- **Descripción**: Cache de datos replicados para mejor performance
- **Dependencias**: T3.4
- **Estimación**: 4-5 horas
- **Criterios de Aceptación**:
  - [ ] Cache de componentes implementado
  - [ ] Cache de promociones implementado
  - [ ] TTL configurado
  - [ ] Invalidación automática
  - [ ] Fallback a base de datos

#### T3.6 - Configurar Seguridad JWT
- **Descripción**: Integrar validación JWT con ms-seguridad
- **Dependencias**: T3.1
- **Estimación**: 4-5 horas
- **Criterios de Aceptación**:
  - [ ] JwtAuthenticationFilter configurado
  - [ ] JWKS client configurado
  - [ ] SessionValidationClient configurado
  - [ ] RBAC annotations funcionando
  - [ ] Cache de tokens configurado

#### T3.7 - Escribir Tests
- **Descripción**: Tests unitarios e integración
- **Dependencias**: T3.5
- **Estimación**: 8-10 horas
- **Criterios de Aceptación**:
  - [ ] Tests de controladores (>80% cobertura)
  - [ ] Tests de lógica de dominio
  - [ ] Tests de consumidores Kafka
  - [ ] Tests de cache
  - [ ] Tests de integración con TestContainers

#### T3.8 - Documentar Microservicio Cotizaciones
- **Descripción**: Documentacion completa
- **Dependencias**: T3.7
- **Estimación**: 3-4 horas
- **Criterios de Aceptación**:
  - [ ] README.md creado
  - [ ] Documentacion de lógica de dominio
  - [ ] Javadoc en clases principales
  - [ ] Diagramas de flujo de cotizacion
  - [ ] Documentacion de cache

---

## FASE 4: ms-cotizador-pedidos

### Subagente 4: ms-cotizador-pedidos

#### T4.1 - Crear Estructura Base del Microservicio
- **Descripción**: Copiar y adaptar estructura para pedidos
- **Dependencias**: T1.4
- **Estimación**: 4-5 horas
- **Criterios de Aceptación**:
  - [ ] Carpeta `ms-cotizador-pedidos/` creada
  - [ ] pom.xml adaptado con puerto 8084
  - [ ] Dockerfile copiado y adaptado
  - [ ] Estructura de paquetes Java organizada
  - [ ] application.yml configurado

#### T4.2 - Migrar Controladores de Pedidos
- **Descripción**: Migrar PedidoController y ProveedorController
- **Dependencias**: T4.1
- **Estimación**: 6-7 horas
- **Criterios de Aceptación**:
  - [ ] PedidoController migrado
  - [ ] ProveedorController migrado
  - [ ] Validaciones RBAC mantenidas
  - [ ] DTOs migrados
  - [ ] Endpoints funcionando

#### T4.3 - Migrar Logica de Dominio de Pedidos
- **Descripción**: Migrar GestorPedidos y lógica asociada
- **Dependencias**: T4.2
- **Estimación**: 8-10 horas
- **Criterios de Aceptación**:
  - [ ] GestorPedidos migrado
  - [ ] CotizacionPresupuestoAdapter migrado
  - [ ] Patron Adapter funcionando
  - [ ] PedidoServicio migrado
  - [ ] ProveedorServicio migrado

#### T4.4 - Configurar Consumidores Kafka
- **Descripción**: Sincronizacion desde componentes y cotizaciones
- **Dependencias**: T4.3, T3.4
- **Estimación**: 6-8 horas
- **Criterios de Aceptación**:
  - [ ] Consumer para componentes.changes
  - [ ] Consumer para cotizaciones.changes
  - [ ] Consumer para proveedores.changes
  - [ ] Sincronizacion bidireccional
  - [ ] Manejo de conflictos

#### T4.5 - Implementar Patron SAGA
- **Descripción**: Transacciones distribuidas para operaciones complejas
- **Dependencias**: T4.4
- **Estimación**: 8-10 horas
- **Criterios de Aceptación**:
  - [ ] SAGA Orchestrator implementado
  - [ ] Compensating actions definidas
  - [ ] Estado de transacciones distribuidas
  - [ ] Rollback automatico en fallos
  - [ ] Logging de SAGA events

#### T4.6 - Configurar Seguridad JWT
- **Descripción**: Integrar validación JWT con ms-seguridad
- **Dependencias**: T4.1
- **Estimación**: 4-5 horas
- **Criterios de Aceptación**:
  - [ ] JwtAuthenticationFilter configurado
  - [ ] JWKS client configurado
  - [ ] SessionValidationClient configurado
  - [ ] RBAC annotations funcionando
  - [ ] Cache de tokens configurado

#### T4.7 - Escribir Tests
- **Descripción**: Tests unitarios e integración
- **Dependencias**: T4.5
- **Estimación**: 8-10 horas
- **Criterios de Aceptación**:
  - [ ] Tests de controladores (>80% cobertura)
  - [ ] Tests de SAGA pattern
  - [ ] Tests de consumidores Kafka
  - [ ] Tests de integración
  - [ ] Tests de compensaciones

#### T4.8 - Documentar Microservicio Pedidos
- **Descripción**: Documentacion completa
- **Dependencias**: T4.7
- **Estimación**: 3-4 horas
- **Criterios de Aceptación**:
  - [ ] README.md creado
  - [ ] Documentacion de SAGA pattern
  - [ ] Javadoc en clases principales
  - [ ] Diagramas de flujo de pedidos
  - [ ] Documentacion de transacciones distribuidas

---

## FASE 5: Integracion y Portal

### Subagente 5: Portal y Gateway

#### T5.1 - Actualizar API Gateway
- **Descripción**: Configurar rutas para los nuevos microservicios
- **Dependencias**: T2.7, T3.8, T4.8
- **Estimación**: 4-6 horas
- **Criterios de Aceptación**:
  - [ ] Ruta `/api/componentes/` a ms-cotizador-componentes:8082
  - [ ] Ruta `/api/cotizaciones/` a ms-cotizador-cotizaciones:8083
  - [ ] Ruta `/api/pedidos/` a ms-cotizador-pedidos:8084
  - [ ] Load balancing configurado
  - [ ] Health checks del gateway funcionando

#### T5.2 - Actualizar Servicios Frontend
- **Descripción**: Modificar servicios API del portal para nuevos endpoints
- **Dependencias**: T5.1
- **Estimación**: 6-8 horas
- **Criterios de Aceptación**:
  - [ ] componentesApi.js actualizado
  - [ ] cotizacionesApi.js actualizado
  - [ ] pedidosApi.js actualizado
  - [ ] proveedoresApi.js actualizado
  - [ ] promocionesApi.js actualizado

#### T5.3 - Actualizar Configuracion Frontend
- **Descripción**: Modificar constantes y configuración del portal
- **Dependencias**: T5.2
- **Estimación**: 2-3 horas
- **Criterios de Aceptación**:
  - [ ] constants.js actualizado con nuevas rutas
  - [ ] apiClient.js adaptado
  - [ ] Manejo de errores actualizado
  - [ ] Interceptors funcionando

#### T5.4 - Pruebas de Integracion End-to-End
- **Descripción**: Validar funcionamiento completo del sistema
- **Dependencias**: T5.3
- **Estimación**: 8-10 horas
- **Criterios de Aceptación**:
  - [ ] Tests de flujo completo de componentes
  - [ ] Tests de flujo completo de cotizaciones
  - [ ] Tests de flujo completo de pedidos
  - [ ] Tests de sincronización Kafka
  - [ ] Tests de autenticacion JWT

#### T5.5 - Configurar Docker Compose Final
- **Descripción**: Integrar todos los servicios en docker-compose.yml
- **Dependencias**: T5.4
- **Estimación**: 3-4 horas
- **Criterios de Aceptación**:
  - [ ] Todos los microservicios en docker-compose
  - [ ] Dependencias entre servicios configuradas
  - [ ] Health checks funcionando
  - [ ] Volumes y networks configurados
  - [ ] Variables de entorno organizadas

#### T5.6 - Desactivar ms-cotizador Original
- **Descripción**: Comentar/desactivar el microservicio original
- **Dependencias**: T5.5
- **Estimación**: 1-2 horas
- **Criterios de Aceptación**:
  - [ ] Servicio backend comentado en docker-compose
  - [ ] Rutas del gateway redirigidas
  - [ ] Documentacion de rollback creada
  - [ ] Backup de configuración original

#### T5.7 - Documentar Portal Actualizado
- **Descripción**: Actualizar documentación del frontend
- **Dependencias**: T5.6
- **Estimación**: 2-3 horas
- **Criterios de Aceptación**:
  - [ ] README.md de portal-cotizador actualizado
  - [ ] Documentacion de nuevas rutas API
  - [ ] Guía de configuración actualizada
  - [ ] Troubleshooting guide actualizado

---

## Tareas Transversales

### Registro y Documentacion

#### TX.1 - Mantener Changelist Actualizado
- **Descripción**: Registrar todos los cambios en changelist.md
- **Responsable**: Todos los subagentes
- **Frecuencia**: Cada commit/cambio importante
- **Criterios de Aceptacion**:
  - [ ] Formato: `[YYYY-MM-DD HH:MM:SS MST] - Agente - Descripción`
  - [ ] Registro de inicio/fin de cada tarea
  - [ ] Registro de issues/resoluciones
  - [ ] Registro de decisiones arquitectónicas

#### TX.2 - Crear Documentacion General
- **Descripción**: Documentacion general del proyecto migrado
- **Responsable**: Lider técnico / Todos
- **Dependencias**: T5.7
- **Estimación**: 4-6 horas
- **Criterios de Aceptación**:
  - [ ] README.md principal actualizado
  - [ ] Arquitectura general documentada
  - [ ] Guía de deployment actualizada
  - [ ] Guía de troubleshooting general
  - [ ] Matriz de responsabilidades de microservicios

#### TX.3 - Validar Criterios de Aceptacion PRD
- **Descripción**: Verificar que se cumplen todos los criterios del PRD
- **Responsable**: Todos los subagentes
- **Dependencias**: Todas las tareas principales
- **Criterios de Aceptacion**:
  - [ ] Separación en 3 microservicios funcional
  - [ ] Portal funcionando transparentemente
  - [ ] Cada microservicio con su propia BD
  - [ ] Seguridad JWT funcionando igual
  - [ ] Tablas redundantes sincronizadas
  - [ ] README.md y documentación actualizada

---

## Estimaciones Totales

**FASE 1**: ~17-24 horas  
**FASE 2**: ~35-46 horas  
**FASE 3**: ~45-60 horas  
**FASE 4**: ~43-59 horas  
**FASE 5**: ~26-36 horas  
**Transversales**: ~10-15 horas  

**TOTAL ESTIMADO**: ~176-240 horas (22-30 días laborales)

---

## Dependencias Críticas

1. **Kafka debe estar funcionando** antes de cualquier microservicio
2. **ms-cotizador-componentes** debe completarse antes que cotizaciones
3. **ms-cotizador-cotizaciones** debe completarse antes que pedidos  
4. **Todos los microservicios** deben estar listos antes de la integración final
5. **Tests de integración** son obligatorios antes de desactivar ms-cotizador original

---

## Riesgos y Mitigaciones

- **Riesgo**: Perdida de datos durante migración  
  **Mitigacion**: Backups antes de cada fase, rollback plan documentado

- **Riesgo**: Inconsistencia en sincronización Kafka  
  **Mitigacion**: Tests exhaustivos de CDC, monitoring de lag

- **Riesgo**: Performance degradation  
  **Mitigacion**: Load testing, optimizacion de cache, monitoring

- **Riesgo**: Security vulnerabilities  
  **Mitigacion**: Security testing en cada microservicio, RBAC validation