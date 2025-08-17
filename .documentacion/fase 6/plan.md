 # 🎯 Plan de Migración: Separación de ms-cotizador en 3 Microservicios

## 📋 Resumen Ejecutivo

Dividir el monolito `ms-cotizador` en 3 microservicios especializados, implementando Apache Kafka para sincronización de datos y manteniendo la compatibilidad con el frontend actual.

## 🏗️ Arquitectura Target

### 1. ms-cotizador-componentes (Puerto 8082)

- **Responsabilidades**: Componentes, PCs, Promociones
- **Base de datos**: `cotizador_componentes_db`
- **Tablas propias**: `cocomponente`, `cotipo_componente`, `copromocion`, `codetalle_promocion`, `copc_parte`
- **Tablas replicadas**: ninguna (es la fuente de verdad para componentes)

### 2. ms-cotizador-cotizaciones (Puerto 8083)

- **Responsabilidades**: Cotizaciones, cálculo de impuestos
- **Base de datos**: `cotizador_cotizaciones_db`
- **Tablas propias**: `cocotizacion`, `codetalle_cotizacion`
- **Tablas replicadas**: `cocomponente` (read-only), `copromocion` (read-only)

### 3. ms-cotizador-pedidos (Puerto 8084)

- **Responsabilidades**: Pedidos, Proveedores
- **Base de datos**: `cotizador_pedidos_db`
- **Tablas propias**: `copedido`, `codetalle_pedido`, `coproveedor`
- **Tablas replicadas**: `cocomponente` (read-only), `cocotizacion` (read-only)

## 🚀 Fases de Implementación

### FASE 1: Infraestructura Kafka y Bases de Datos

1. Agregar Apache Kafka 4.0.0 en modo KRaft al `docker-compose.yml`
2. Configurar Debezium MySQL Connector para CDC
3. Crear 3 nuevas bases de datos MySQL
4. Configurar topics de Kafka para sincronización

### FASE 2: ms-cotizador-componentes

1. Copiar estructura base de `ms-cotizador`
2. Migrar clases: `ComponenteController`, `PcController`, `PromocionControlador`
3. Configurar productor Kafka para eventos de cambio
4. Actualizar seguridad JWT

### FASE 3: ms-cotizador-cotizaciones

1. Copiar estructura base
2. Migrar `CotizacionController` y lógica de dominio
3. Configurar consumidor Kafka para componentes/promociones
4. Implementar cache local para datos replicados

### FASE 4: ms-cotizador-pedidos

1. Copiar estructura base
2. Migrar `PedidoController`, `ProveedorController`
3. Configurar consumidor para componentes/cotizaciones
4. Implementar patrón SAGA para operaciones distribuidas

### FASE 5: Integración y Portal

1. Actualizar API Gateway con nuevas rutas
2. Modificar `portal-cotizador` para nuevos endpoints
3. Pruebas de integración end-to-end
4. Desactivar `ms-cotizador` original

## 📦 Estructura de Directorios

```
CotizadorPcPartes/
├── ms-cotizador/              (mantener como referencia)
├── ms-cotizador-componentes/  (nuevo)
├── ms-cotizador-cotizaciones/ (nuevo)
├── ms-cotizador-pedidos/      (nuevo)
├── kafka-config/              (nuevo)
│   ├── debezium-connectors/
│   └── topic-definitions/
├── portal-cotizador/          (actualizar)
├── nginx-gateway/             (actualizar)
└── .documentacion/fase 6/
    └── changelist.md          (registro de cambios)
```

## 👥 División del Trabajo (Subagentes)

### Subagente 1: Infraestructura Kafka

- Configurar Kafka en modo KRaft
- Implementar Debezium CDC
- Crear topics y schemas

### Subagente 2: ms-cotizador-componentes

- Migrar módulo de componentes
- Implementar productor Kafka
- Pruebas unitarias

### Subagente 3: ms-cotizador-cotizaciones

- Migrar módulo de cotizaciones
- Implementar consumidor Kafka
- Cache de datos replicados

### Subagente 4: ms-cotizador-pedidos

- Migrar módulo de pedidos
- Implementar SAGA pattern
- Integración con cotizaciones

### Subagente 5: Portal y Gateway

- Actualizar servicios en frontend
- Modificar rutas del gateway
- Pruebas de integración

## 🔄 Sincronización con Kafka

### Topics a Crear:

- `componentes.changes` - Cambios en componentes
- `promociones.changes` - Cambios en promociones
- `cotizaciones.changes` - Cambios en cotizaciones
- `proveedores.changes` - Cambios en proveedores

### Flujo de Datos:

1. Componentes → Kafka → Cotizaciones y Pedidos (consumidores)
2. Promociones → Kafka → Cotizaciones (consumidor)
3. Cotizaciones → Kafka → Pedidos (consumidor)

## ✅ Criterios de Éxito

- Funcionalidad idéntica al sistema actual
- Sin cambios visibles en el frontend
- Sincronización automática entre bases
- Validación JWT funcionando
- RBAC aplicado en cada microservicio
- Zero-downtime durante migración

## 📝 Registro de Cambios

Todo el progreso se documentará en `.documentacion/fase 6/changelist.md` con formato:
`[YYYY-MM-DD HH:MM:SS MST] - Agente - Descripción del cambio`

## ⚠️ Consideraciones Importantes

- Mantener `ms-cotizador` como referencia (no eliminar)
- Migración gradual microservicio por microservicio
- Frontend usa una sola URL (gateway hace routing)
- Cada microservicio valida con `ms-seguridad`
- Eventual consistency aceptable