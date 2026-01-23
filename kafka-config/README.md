# Configuración Kafka + Debezium CDC

## Descripción

Este directorio contiene la configuración completa para **Change Data Capture (CDC)** usando Apache Kafka y Debezium para sincronización automática entre bases de datos del sistema CotizadorPcPartes.

## Arquitectura CDC

### Componentes Principales

1. **Apache Kafka 4.0.0** - Broker de mensajes para eventos CDC
2. **Debezium 2.5** - Conectores MySQL para captura de cambios
3. **MySQL 8.4.4** - Bases de datos con binlog habilitado
4. **Kafka Connect** - Plataforma para conectores

### Bases de Datos Monitoreadas

- **mysql** (cotizador) - Base de datos principal
- **mysql-componentes** - Microservicio de componentes
- **mysql-cotizaciones** - Microservicio de cotizaciones  
- **mysql-pedidos** - Microservicio de pedidos

## Estructura de Archivos

```
kafka-config/
├── README.md                          # Este archivo (documentación completa)
├── mysql-debezium.cnf                 # Configuración MySQL para CDC
├── setup-kafka-cdc.sh                 # Script maestro de configuración
├── create-kafka-topics.sh             # Script para crear topics
├── setup-debezium-connectors.sh       # Script para configurar conectores
├── health-check.sh                    # Script de verificación de salud ✨
├── monitor-cdc.sh                     # Monitor en tiempo real ✨
├── alert-system.sh                    # Sistema de alertas automáticas ✨
├── connectors/                        # Configuraciones JSON de conectores
│   ├── componentes-connector.json
│   ├── cotizaciones-connector.json
│   ├── pedidos-connector.json
│   └── cotizador-principal-connector.json
└── sql/                               # Scripts DDL para nuevas BD
    ├── componentes-ddl.sql
    ├── cotizaciones-ddl.sql
    └── pedidos-ddl.sql
```

## Topics de Kafka Creados

### Topics Principales (6 particiones)
- `componentes.changes` - Cambios en componentes
- `promociones.changes` - Cambios en promociones
- `cotizaciones.changes` - Cambios en cotizaciones
- `proveedores.changes` - Cambios en proveedores

### Topics por Tabla (3 particiones)
- `cocomponente.changes`
- `copromocion.changes` 
- `cocotizacion.changes`
- `coproveedor.changes`
- `codetalle_cotizacion.changes`
- `codetalle_pedido.changes`
- `copedido.changes`
- `codetalle_promocion.changes`
- `copc_parte.changes`

### Topics de Sistema (1 partición)
- `dbhistory.*` - Historial de esquemas
- `connect-*` - Configuración de Kafka Connect

## Conectores Debezium

### Configuración por Base de Datos

| Conector | Base de Datos | Server ID | Tablas Monitoreadas |
|----------|---------------|-----------|-------------------|
| componentes-mysql-connector | mysql-componentes | 184054 | cocomponente, copromocion, codetalle_promocion, copc_parte |
| cotizaciones-mysql-connector | mysql-cotizaciones | 184055 | cocotizacion, codetalle_cotizacion, copromocion |
| pedidos-mysql-connector | mysql-pedidos | 184056 | copedido, codetalle_pedido, coproveedor |
| cotizador-principal-mysql-connector | mysql | 184057 | Todas las tablas principales |

### Configuración CDC

- **Snapshot Mode**: `initial` - Captura estado inicial
- **Binlog Format**: `ROW` - Formato de fila completa
- **GTID**: Habilitado para consistencia
- **Transforms**: Redirige eventos a topics `.changes`

## Uso

### Configuración Inicial

```bash
# Ejecutar configuración completa
./kafka-config/setup-kafka-cdc.sh
```

### Scripts Individuales

```bash
# Solo crear topics
./kafka-config/create-kafka-topics.sh

# Solo configurar conectores
./kafka-config/setup-debezium-connectors.sh
```

### Scripts de Utilidad

#### 1. Verificación de Salud del Sistema
```bash
# Verificación completa del estado del sistema
./kafka-config/health-check.sh

# Verificar solo conectores
./kafka-config/health-check.sh --connectors-only

# Generar reporte detallado
./kafka-config/health-check.sh --detailed
```

#### 2. Monitor en Tiempo Real
```bash
# Monitoreo continuo de CDC
./kafka-config/monitor-cdc.sh

# Monitor con intervalo personalizado (cada 30 segundos)
REFRESH_INTERVAL=30 ./kafka-config/monitor-cdc.sh
```

#### 3. Sistema de Alertas
```bash
# Configurar alertas (primera vez)
export SLACK_WEBHOOK_URL="https://hooks.slack.com/services/..."
export ALERT_EMAIL="admin@example.com"
./kafka-config/alert-system.sh --setup

# Ejecutar verificación única
./kafka-config/alert-system.sh --check

# Ejecutar en modo daemon (recomendado para producción)
nohup ./kafka-config/alert-system.sh --daemon > /tmp/kafka-alerts-daemon.log 2>&1 &

# Probar sistema de alertas
./kafka-config/alert-system.sh --test-alert
```

### Monitoreo Manual

```bash
# Listar topics
kafka-topics.sh --bootstrap-server localhost:9092 --list

# Ver conectores activos
curl http://localhost:8083/connectors

# Estado de un conector
curl http://localhost:8083/connectors/componentes-mysql-connector/status

# Monitorear cambios en tiempo real
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic componentes.changes --from-beginning
```

### Verificación de Funcionamiento

```bash
# Verificar binlog habilitado
docker exec cotizador-mysql mysql -u root -p -e "SHOW VARIABLES LIKE 'log_bin';"

# Ver eventos en topic
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic cocomponente.changes
```

## Configuración MySQL

### Parámetros CDC Habilitados

```ini
# Binlog para CDC
log-bin=mysql-bin
binlog-format=ROW
binlog-row-image=FULL

# GTID para consistencia
gtid-mode=ON
enforce-gtid-consistency=ON

# Server IDs únicos por instancia
server-id=184054-184058
```

## Troubleshooting

### Problemas Comunes

1. **Conector FAILED**
   ```bash
   # Ver detalles del error
   curl http://localhost:8083/connectors/componentes-mysql-connector/status
   
   # Reiniciar conector
   curl -X POST http://localhost:8083/connectors/componentes-mysql-connector/restart
   ```

2. **Topics no se crean**
   ```bash
   # Verificar Kafka está ejecutándose
   kafka-topics.sh --bootstrap-server localhost:9092 --list
   
   # Verificar auto-create habilitado
   docker logs cotizador-kafka
   ```

3. **Binlog no habilitado**
   ```bash
   # Verificar configuración MySQL
   docker exec cotizador-mysql mysql -u root -p -e "SHOW VARIABLES LIKE '%log_bin%';"
   ```

### Logs de Troubleshooting

- **Kafka**: `docker logs cotizador-kafka`
- **Kafka Connect**: `docker logs cotizador-kafka-connect`
- **MySQL**: `docker logs cotizador-mysql`
- **Setup CDC**: `kafka-config/setup-cdc.log`

## Flujo de Datos entre Microservicios

### Sincronización de Datos Distribuidos

El sistema implementa un patrón de **Event Sourcing** con **Change Data Capture** para mantener consistencia eventual entre microservicios:

```
┌─────────────────────────────────────────────────────────────────────┐
│                    FLUJO DE SINCRONIZACIÓN                         │
└─────────────────────────────────────────────────────────────────────┘

1. EVENTO DE NEGOCIO
   ┌─────────────────┐
   │ Usuario crea    │
   │ nuevo componente│
   │ en frontend     │
   └─────────┬───────┘
             │ HTTP POST
             ▼
2. MICROSERVICIO ORIGEN
   ┌─────────────────┐
   │ ms-componentes  │
   │ procesa request │
   │ y guarda en BD  │
   └─────────┬───────┘
             │ INSERT
             ▼
3. BASE DE DATOS
   ┌─────────────────┐
   │ mysql-componentes│
   │ escribir a tabla│
   │ + binlog ROW    │
   └─────────┬───────┘
             │ Binlog Event
             ▼
4. DEBEZIUM CDC
   ┌─────────────────┐
   │ Conector lee    │
   │ binlog y crea   │
   │ evento CDC      │
   └─────────┬───────┘
             │ Kafka Message
             ▼
5. KAFKA TOPIC
   ┌─────────────────┐
   │ cocomponente    │
   │ .changes topic  │
   │ almacena evento │
   └─────────┬───────┘
             │ Event Stream
             ▼
6. MICROSERVICIOS CONSUMIDORES
   ┌─────────────────┬─────────────────┬─────────────────┐
   │ ms-cotizaciones │   ms-pedidos    │ ms-cotizador    │
   │ actualiza cache │ actualiza stock │ sincroniza      │
   │ de componentes  │ disponibilidad  │ datos maestros  │
   └─────────────────┴─────────────────┴─────────────────┘
```

### Casos de Uso de Sincronización

#### Caso 1: Actualización de Precios de Componentes

**Flujo:**
1. **Admin** actualiza precio de componente en `ms-componentes`
2. **CDC** captura cambio en `mysql-componentes`
3. **Evento** se publica en `cocomponente.changes`
4. **ms-cotizaciones** consume evento y actualiza caché de precios
5. **ms-pedidos** consume evento y recalcula costos de pedidos pendientes
6. **Frontend** recibe notificación vía WebSocket de precios actualizados

**Ejemplo de Evento CDC:**
```json
{
  "before": {
    "id": 123,
    "nombre": "RTX 4080",
    "precio": 850.00,
    "actualizado": "2025-08-17T10:30:00Z"
  },
  "after": {
    "id": 123,
    "nombre": "RTX 4080", 
    "precio": 799.99,
    "actualizado": "2025-08-17T15:45:00Z"
  },
  "source": {
    "version": "2.5.0.Final",
    "connector": "mysql",
    "name": "componentes-db",
    "ts_ms": 1692283500000,
    "db": "cotizador_componentes_db",
    "table": "cocomponente",
    "server_id": 184054
  },
  "op": "u",  // update operation
  "ts_ms": 1692283500000,
  "transaction": null
}
```

#### Caso 2: Creación de Nueva Cotización

**Flujo de Datos:**
```
ms-cotizaciones  →  mysql-cotizaciones  →  CDC  →  kafka
                                                    ↓
     ┌─────────────────────────────────────────────┘
     ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   ms-pedidos    │    │ ms-componentes  │    │ ms-cotizador    │
│                 │    │                 │    │                 │
│ • Reservar      │    │ • Verificar     │    │ • Actualizar    │
│   stock para    │    │   disponibilidad│    │   estadísticas │
│   cotización    │    │   de componentes│    │   de ventas     │
│                 │    │                 │    │                 │
│ • Crear pedido  │    │ • Actualizar    │    │ • Generar       │
│   provisional   │    │   contadores    │    │   reportes      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Patrones de Consumo

#### 1. Consumo en Tiempo Real
```java
@Service
public class ComponentePriceUpdateConsumer {
    
    @KafkaListener(topics = "cocomponente.changes")
    public void handleComponenteChange(ComponenteChangeEvent event) {
        if ("u".equals(event.getOp()) && priceChanged(event)) {
            // Actualizar caché local
            componenteCache.updatePrice(
                event.getAfter().getId(),
                event.getAfter().getPrecio()
            );
            
            // Notificar a cotizaciones activas
            cotizacionService.recalculateActiveCotizaciones(
                event.getAfter().getId()
            );
            
            // Emitir evento de negocio
            eventPublisher.publishEvent(
                new ComponentePriceUpdatedEvent(event.getAfter())
            );
        }
    }
    
    private boolean priceChanged(ComponenteChangeEvent event) {
        return !event.getBefore().getPrecio()
                .equals(event.getAfter().getPrecio());
    }
}
```

#### 2. Consumo por Lotes (Batch)
```java
@Component
public class StockSynchronizationBatchProcessor {
    
    @Scheduled(fixedDelay = 300000) // Cada 5 minutos
    public void processPendingStockUpdates() {
        // Leer eventos acumulados
        List<StockChangeEvent> events = kafkaTemplate
            .receiveAll("stock.changes", Duration.ofSeconds(30));
            
        // Procesar en lote
        Map<Long, Integer> stockUpdates = events.stream()
            .collect(Collectors.groupingBy(
                event -> event.getComponenteId(),
                Collectors.summingInt(event -> event.getStockChange())
            ));
            
        // Aplicar cambios
        stockUpdates.forEach(stockService::updateComponenteStock);
    }
}
```

### Garantías de Consistencia

#### 1. Consistencia Eventual
```
Tiempo →  T0    T1    T2    T3    T4    T5
          │     │     │     │     │     │
DB1 ──────●─────●─────●─────●─────●─────●  (Cambio inmediato)
          │     │     │     │     │     │
DB2 ──────●─────●─────◯─────●─────●─────●  (Consistente en T3)
          │     │     │     │     │     │
DB3 ──────●─────●─────●─────◯─────●─────●  (Consistente en T4)
          │     │     │     │     │     │
DB4 ──────●─────●─────●─────●─────◯─────●  (Consistente en T5)

● = Estado consistente
◯ = Estado inconsistente temporal
```

#### 2. Ordenamiento de Eventos
- **Particionado por clave**: Eventos del mismo componente van a la misma partición
- **Offset secuencial**: Garantiza orden dentro de cada partición
- **Timestamp ordering**: Eventos incluyen timestamp de origen para resolución de conflictos

#### 3. Manejo de Duplicados
```java
@Component
public class IdempotentEventProcessor {
    
    private final Set<String> processedEvents = new ConcurrentHashMap<>();
    
    @KafkaListener(topics = "#{kafkaTopics}")
    public void handleEvent(GenericCDCEvent event) {
        String eventId = generateEventId(event);
        
        // Verificar si ya fue procesado
        if (processedEvents.contains(eventId)) {
            log.debug("Evento duplicado detectado: {}", eventId);
            return;
        }
        
        try {
            // Procesar evento
            processBusinessEvent(event);
            
            // Marcar como procesado
            processedEvents.add(eventId);
            
        } catch (Exception e) {
            log.error("Error procesando evento {}: {}", eventId, e.getMessage());
            // No marcar como procesado para reintento
        }
    }
    
    private String generateEventId(GenericCDCEvent event) {
        return String.format("%s-%s-%d", 
            event.getSource().getTable(),
            event.getOp(),
            event.getTs_ms()
        );
    }
}
```

### Monitoreo de Flujo de Datos

#### Dashboard de Métricas
```bash
# Script de métricas en tiempo real
#!/bin/bash

while true; do
    clear
    echo "=== MÉTRICAS DE FLUJO CDC ==="
    echo "Timestamp: $(date)"
    echo ""
    
    # Throughput por topic
    echo "THROUGHPUT (mensajes/min):"
    for topic in componentes.changes cotizaciones.changes pedidos.changes; do
        rate=$(kafka-run-class.sh kafka.tools.ConsumerPerformance \
            --bootstrap-server localhost:9092 \
            --topic $topic \
            --messages 100 \
            --timeout 10000 2>/dev/null | grep "MB/sec" | awk '{print $3}')
        echo "  $topic: ${rate:-0} MB/sec"
    done
    
    # Lag de consumidores
    echo ""
    echo "CONSUMER LAG:"
    kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
        --group cotizador-connect --describe 2>/dev/null | \
        grep -v GROUP | awk '{print "  " $2 ": " $5 " mensajes"}'
    
    # Conectores activos
    echo ""
    echo "CONECTORES ACTIVOS:"
    curl -s http://localhost:8083/connectors 2>/dev/null | \
        jq -r '.[]' | while read connector; do
        status=$(curl -s "http://localhost:8083/connectors/$connector/status" 2>/dev/null | \
                jq -r '.connector.state')
        echo "  $connector: $status"
    done
    
    sleep 30
done
```

#### Alertas Automáticas
```bash
#!/bin/bash
# Sistema de alertas para problemas de CDC

SLACK_WEBHOOK="${SLACK_WEBHOOK_URL}"
MAX_LAG=1000
MAX_DOWNTIME=300  # 5 minutos

check_connector_health() {
    local connector=$1
    local status=$(curl -s "http://localhost:8083/connectors/$connector/status" 2>/dev/null | \
                  jq -r '.connector.state')
    
    if [ "$status" != "RUNNING" ]; then
        send_alert "🚨 Conector $connector en estado: $status"
        return 1
    fi
    return 0
}

check_consumer_lag() {
    local lag=$(kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
               --group cotizador-connect --describe 2>/dev/null | \
               grep -v GROUP | awk '{sum += $5} END {print sum}')
    
    if [ "${lag:-0}" -gt $MAX_LAG ]; then
        send_alert "⚠️ Consumer lag alto: $lag mensajes"
        return 1
    fi
    return 0
}

send_alert() {
    local message=$1
    echo "[$(date)] ALERT: $message"
    
    if [ -n "$SLACK_WEBHOOK" ]; then
        curl -X POST -H 'Content-type: application/json' \
            --data "{\"text\":\"$message\"}" \
            "$SLACK_WEBHOOK"
    fi
}

# Ejecutar verificaciones
connectors=("componentes-mysql-connector" "cotizaciones-mysql-connector" "pedidos-mysql-connector")

for connector in "${connectors[@]}"; do
    check_connector_health "$connector"
done

check_consumer_lag
```

## Arquitectura de Sincronización

### Flujo de Datos

1. **Cambio en BD** → Escribir al binlog
2. **Debezium** → Leer binlog → Publicar a Kafka
3. **Topic Kafka** → Almacenar evento de cambio
4. **Consumidores** → Procesar eventos para sincronización

### Formato de Eventos

```json
{
  "before": { /* estado anterior */ },
  "after": { /* estado nuevo */ },
  "source": {
    "version": "2.5.0.Final",
    "connector": "mysql",
    "name": "componentes-db",
    "ts_ms": 1702745123000,
    "db": "cotizador_componentes_db",
    "table": "cocomponente"
  },
  "op": "c|u|d|r",  // create, update, delete, read
  "ts_ms": 1702745123000
}
```

## Diagramas de Flujo de Datos

### Arquitectura General del Sistema

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   Frontend      │────▶│   API Gateway    │────▶│  Microservicios │
│   (Vue.js)      │     │    (Nginx)       │     │  (Spring Boot)  │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                                                           │
                                                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        CAPA DE DATOS                                   │
├─────────────────┬─────────────────┬─────────────────┬─────────────────┤
│   MySQL BD1     │    MySQL BD2    │    MySQL BD3    │    MySQL BD4    │
│  (Cotizador)    │  (Componentes)  │  (Cotizaciones) │   (Pedidos)     │
│  Server ID:     │  Server ID:     │  Server ID:     │  Server ID:     │
│    184057       │    184054       │    184055       │    184056       │
└─────────────────┴─────────────────┴─────────────────┴─────────────────┘
                                │
                                ▼ CDC (Change Data Capture)
┌─────────────────────────────────────────────────────────────────────┐
│                      KAFKA INFRASTRUCTURE                              │
│  ┌─────────────────┐     ┌─────────────────┐     ┌──────────────────┐  │
│  │ Apache Kafka    │────▶│ Kafka Connect   │────▶│ Debezium         │  │
│  │ (KRaft Mode)    │     │ (Runtime)       │     │ (MySQL CDC)      │  │
│  │ Port: 9092      │     │ Port: 8083      │     │ Version: 2.5     │  │
│  └─────────────────┘     └─────────────────┘     └──────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        TOPICS DE KAFKA                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐       │
│  │ componentes     │  │ cotizaciones    │  │ pedidos         │       │
│  │ .changes        │  │ .changes        │  │ .changes        │  ...  │
│  │ (6 particiones) │  │ (6 particiones) │  │ (6 particiones) │       │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘       │
└─────────────────────────────────────────────────────────────────────┘
```

### Flujo de Datos CDC (Change Data Capture)

```
1. CAMBIO EN BASE DE DATOS
   ┌─────────────────┐
   │ INSERT/UPDATE/  │
   │ DELETE en tabla │
   │                 │
   └─────────┬───────┘
             │
             ▼
2. MYSQL BINLOG
   ┌─────────────────┐
   │ Evento escrito  │
   │ al binlog       │
   │ (formato ROW)   │
   └─────────┬───────┘
             │
             ▼
3. DEBEZIUM CONNECTOR
   ┌─────────────────┐
   │ Lee binlog      │
   │ Transforma      │
   │ eventos CDC     │
   └─────────┬───────┘
             │
             ▼
4. KAFKA TOPIC
   ┌─────────────────┐
   │ Almacena evento │
   │ en topic        │
   │ *.changes       │
   └─────────┬───────┘
             │
             ▼
5. CONSUMIDORES
   ┌─────────────────┐
   │ Microservicios  │
   │ procesan        │
   │ sincronización  │
   └─────────────────┘
```

### Configuración de Red de Microservicios

```
Docker Network: cotizador-network
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│  ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐    │
│  │ ms-cotizador    │   │ ms-componentes  │   │ ms-cotizaciones │    │
│  │ Port: 8080      │   │ Port: 8081      │   │ Port: 8082      │    │
│  └─────────────────┘   └─────────────────┘   └─────────────────┘    │
│           │                      │                      │           │
│           ▼                      ▼                      ▼           │
│  ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐    │
│  │ mysql           │   │ mysql-          │   │ mysql-          │    │
│  │ (cotizador_db)  │   │ componentes     │   │ cotizaciones    │    │
│  │ Port: 3306      │   │ Port: 3306      │   │ Port: 3306      │    │
│  └─────────────────┘   └─────────────────┘   └─────────────────┘    │
│                                                                     │
│  ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐    │
│  │ ms-pedidos      │   │ ms-seguridad    │   │ portal-cotizador│    │
│  │ Port: 8083      │   │ Port: 8084      │   │ Port: 3000      │    │
│  └─────────────────┘   └─────────────────┘   └─────────────────┘    │
│           │                      │                      │           │
│           ▼                      ▼                      │           │
│  ┌─────────────────┐   ┌─────────────────┐              │           │
│  │ mysql-pedidos   │   │ mysql-seguridad │              │           │
│  │ Port: 3306      │   │ Port: 3306      │              │           │
│  └─────────────────┘   └─────────────────┘              │           │
│                                                         │           │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                 KAFKA INFRASTRUCTURE                       │    │
│  │  ┌─────────────────┐   ┌─────────────────┐               │    │
│  │  │ kafka           │   │ kafka-connect   │               │    │
│  │  │ Port: 9092      │   │ Port: 8083      │               │    │
│  │  └─────────────────┘   └─────────────────┘               │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                     │
│  ┌─────────────────┐                                                │
│  │ nginx-gateway   │                                                │
│  │ Port: 80, 443   │                                                │
│  └─────────────────┘                                                │
└─────────────────────────────────────────────────────────────────────┘
```

## Configuración Detallada de Conectores Debezium

### Conector 1: componentes-mysql-connector

**Configuración Principal:**
```json
{
  "name": "componentes-mysql-connector",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "database.hostname": "mysql-componentes",
    "database.port": "3306",
    "database.user": "componentes_user",
    "database.server.id": "184054",
    "database.server.name": "componentes-db",
    "database.include.list": "cotizador_componentes_db",
    "table.include.list": [
      "cotizador_componentes_db.cocomponente",
      "cotizador_componentes_db.copromocion",
      "cotizador_componentes_db.codetalle_promocion",
      "cotizador_componentes_db.codetalle_promdscto_xcant",
      "cotizador_componentes_db.copc_parte"
    ]
  }
}
```

**Transformaciones Aplicadas:**
- **RegexRouter**: Convierte nombres de topic `db.table` → `table.changes`
- **Snapshot Mode**: `initial` - Captura estado completo al inicio
- **Time Precision**: `adaptive_time_microseconds` - Precisión máxima
- **Decimal Handling**: `double` - Conversión a double para compatibilidad

**Tablas Monitoreadas:**
1. `cocomponente` - Información básica de componentes
2. `copromocion` - Definición de promociones
3. `codetalle_promocion` - Detalles de aplicación de promociones
4. `codetalle_promdscto_xcant` - Promociones por cantidad
5. `copc_parte` - Partes de PC y relaciones

### Conector 2: cotizaciones-mysql-connector

**Configuración Principal:**
```json
{
  "name": "cotizaciones-mysql-connector",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "database.hostname": "mysql-cotizaciones",
    "database.server.id": "184055",
    "database.server.name": "cotizaciones-db",
    "database.include.list": "cotizador_cotizaciones_db",
    "table.include.list": [
      "cotizador_cotizaciones_db.cocotizacion",
      "cotizador_cotizaciones_db.codetalle_cotizacion",
      "cotizador_cotizaciones_db.copromocion"
    ]
  }
}
```

**Funcionalidades Específicas:**
- **Monitoring**: Cotizaciones y sus detalles
- **Integración**: Con promociones para descuentos aplicados
- **Eventos**: Captura cambios en precios, estados, y aplicación de descuentos

### Conector 3: pedidos-mysql-connector

**Configuración Principal:**
```json
{
  "name": "pedidos-mysql-connector",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "database.hostname": "mysql-pedidos",
    "database.server.id": "184056",
    "database.server.name": "pedidos-db",
    "table.include.list": [
      "cotizador_pedidos_db.copedido",
      "cotizador_pedidos_db.codetalle_pedido",
      "cotizador_pedidos_db.coproveedor"
    ]
  }
}
```

**Gestión de Estados:**
- **Pedidos**: Estados de órdenes (PENDIENTE, CONFIRMADO, ENVIADO, COMPLETADO)
- **Proveedores**: Información de proveedores y disponibilidad
- **Detalles**: Líneas de pedido con cantidades y precios

### Conector 4: cotizador-principal-mysql-connector

**Configuración Principal:**
```json
{
  "name": "cotizador-principal-mysql-connector",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "database.hostname": "mysql",
    "database.server.id": "184057",
    "database.server.name": "cotizador-principal-db",
    "database.include.list": "cotizador",
    "table.include.list": "cotizador.*"
  }
}
```

**Rol Central:**
- **Master Database**: Base de datos principal del sistema
- **Consolidación**: Eventos de todas las entidades principales
- **Sincronización**: Punto central para consistencia de datos

## Configuración Avanzada de Kafka KRaft

### Modo KRaft (Kafka Raft)

**Características Principales:**
- **Sin Zookeeper**: Eliminación de dependencia externa
- **Consenso Raft**: Algoritmo de consenso distribuido integrado
- **Mejores Performance**: Menor latencia y mayor throughput
- **Simplified Operations**: Menos componentes a gestionar

**Configuración KRaft en docker-compose.yml:**
```yaml
kafka:
  environment:
    # Configuración de nodos
    KAFKA_NODE_ID: 1
    KAFKA_PROCESS_ROLES: broker,controller
    
    # Listeners y protocolos
    KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
    
    # Configuración de quorum
    KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
    KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
    KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
    
    # Configuración de replicación
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
    KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
```

### Configuración de Topics

**Estrategia de Particionado:**

1. **Topics Principales** (6 particiones):
   - `componentes.changes`
   - `cotizaciones.changes`
   - `pedidos.changes`
   - `proveedores.changes`
   
   **Justificación**: Mayor throughput para eventos críticos del negocio

2. **Topics de Tabla** (3 particiones):
   - `cocomponente.changes`
   - `cocotizacion.changes`
   - `copedido.changes`
   - Otros...
   
   **Justificación**: Balance entre rendimiento y recursos

3. **Topics del Sistema** (1 partición):
   - `dbhistory.*`
   - `connect-*`
   
   **Justificación**: Datos secuenciales que no requieren paralelización

**Configuración de Retención:**
```bash
# Política de limpieza: compactación + borrado por tiempo
cleanup.policy=compact,delete

# Retención: 7 días
retention.ms=604800000

# Segmentación: 1 día
segment.ms=86400000
```

## Guía Completa de Troubleshooting

### Problemas de Conectividad

#### 1. Kafka No Disponible

**Síntomas:**
```bash
# Error al listar topics
kafka-topics.sh --bootstrap-server localhost:9092 --list
# Output: Error: Connection to node -1 could not be established
```

**Diagnóstico:**
```bash
# Verificar estado del contenedor
docker ps | grep kafka

# Verificar logs
docker logs cotizador-kafka --tail 50

# Verificar puertos
netstat -tlnp | grep 9092
```

**Soluciones:**
1. **Reiniciar Kafka:**
   ```bash
   docker-compose restart kafka
   ```

2. **Verificar configuración de red:**
   ```bash
   docker network inspect cotizador-network
   ```

3. **Verificar espacio en disco:**
   ```bash
   df -h
   docker system df
   ```

#### 2. Kafka Connect No Responde

**Síntomas:**
```bash
curl http://localhost:8083/connectors
# Output: curl: (7) Failed to connect to localhost port 8083
```

**Diagnóstico:**
```bash
# Estado del servicio
docker logs cotizador-kafka-connect --tail 100

# Verificar dependencias
docker ps --filter "name=kafka"
```

**Soluciones:**
1. **Esperar inicialización completa:**
   ```bash
   # Kafka Connect tarda ~2 minutos en estar listo
   watch -n 5 'curl -s http://localhost:8083/connectors || echo "Esperando..."'
   ```

2. **Verificar configuración de bootstrap servers:**
   ```bash
   docker exec cotizador-kafka-connect env | grep BOOTSTRAP
   ```

### Problemas de Conectores Debezium

#### 1. Conector en Estado FAILED

**Diagnóstico:**
```bash
# Ver estado detallado
curl -s http://localhost:8083/connectors/componentes-mysql-connector/status | jq .

# Ver configuración
curl -s http://localhost:8083/connectors/componentes-mysql-connector/config | jq .

# Ver logs de tareas
curl -s http://localhost:8083/connectors/componentes-mysql-connector/tasks/0/status | jq .
```

**Errores Comunes y Soluciones:**

1. **Error de Autenticación MySQL:**
   ```json
   {
     "error": "Access denied for user 'componentes_user'@'%'"
   }
   ```
   
   **Solución:**
   ```bash
   # Verificar credenciales en BD
   docker exec mysql-componentes mysql -u root -p -e "SELECT user, host FROM mysql.user WHERE user='componentes_user';"
   
   # Recrear usuario si es necesario
   docker exec mysql-componentes mysql -u root -p -e "DROP USER IF EXISTS 'componentes_user'@'%'; CREATE USER 'componentes_user'@'%' IDENTIFIED BY 'componentes_pass'; GRANT ALL PRIVILEGES ON cotizador_componentes_db.* TO 'componentes_user'@'%'; FLUSH PRIVILEGES;"
   ```

2. **Binlog No Habilitado:**
   ```json
   {
     "error": "The MySQL server is not configured to use a row-level binlog"
   }
   ```
   
   **Solución:**
   ```bash
   # Verificar configuración binlog
   docker exec mysql-componentes mysql -u root -p -e "SHOW VARIABLES LIKE 'log_bin';"
   docker exec mysql-componentes mysql -u root -p -e "SHOW VARIABLES LIKE 'binlog_format';"
   
   # Si está deshabilitado, recrear contenedor con configuración correcta
   docker-compose down mysql-componentes
   docker-compose up -d mysql-componentes
   ```

3. **Server ID Duplicado:**
   ```json
   {
     "error": "The slave I/O thread stops because master and slave have equal MySQL server ids"
   }
   ```
   
   **Solución:**
   ```bash
   # Verificar server IDs en todas las BD
   for db in mysql mysql-componentes mysql-cotizaciones mysql-pedidos; do
     echo "=== $db ==="
     docker exec $db mysql -u root -p -e "SHOW VARIABLES LIKE 'server_id';"
   done
   
   # Cada BD debe tener un server_id único (184054-184057)
   ```

#### 2. Conector RUNNING pero Sin Eventos

**Diagnóstico:**
```bash
# Verificar offset del conector
curl -s http://localhost:8083/connectors/componentes-mysql-connector/status | jq '.tasks[0].id'

# Verificar topics de CDC
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic cocomponente.changes --from-beginning --max-messages 5

# Verificar binlog en MySQL
docker exec mysql-componentes mysql -u root -p -e "SHOW MASTER STATUS;"
docker exec mysql-componentes mysql -u root -p -e "SHOW BINARY LOGS;"
```

**Soluciones:**

1. **Forzar Snapshot:**
   ```bash
   # Reiniciar conector para forzar nuevo snapshot
   curl -X POST http://localhost:8083/connectors/componentes-mysql-connector/restart
   
   # O eliminar y recrear
   curl -X DELETE http://localhost:8083/connectors/componentes-mysql-connector
   sleep 10
   curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d @kafka-config/connectors/componentes-connector.json
   ```

2. **Verificar Transformaciones:**
   ```bash
   # Ver eventos originales sin transformación
   kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic componentes.cotizador_componentes_db.cocomponente --from-beginning --max-messages 1
   
   # Ver eventos transformados
   kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic cocomponente.changes --from-beginning --max-messages 1
   ```

### Problemas de Performance

#### 1. Latencia Alta en CDC

**Métricas a Verificar:**
```bash
# Lag de consumidores
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group cotizador-connect --describe

# Métricas de Kafka Connect
curl -s http://localhost:8083/connectors/componentes-mysql-connector/metrics | jq .

# Uso de recursos
docker stats cotizador-kafka cotizador-kafka-connect
```

**Optimizaciones:**

1. **Ajustar Configuración del Conector:**
   ```json
   {
     "max.batch.size": "2048",
     "max.queue.size": "8192",
     "poll.interval.ms": "1000"
   }
   ```

2. **Optimizar MySQL:**
   ```sql
   -- Aumentar buffer pool si hay memoria disponible
   SET GLOBAL innodb_buffer_pool_size = 512*1024*1024;
   
   -- Ajustar configuración de binlog
   SET GLOBAL sync_binlog = 0;  -- Solo para desarrollo
   ```

3. **Ajustar Kafka:**
   ```yaml
   # En docker-compose.yml
   environment:
     KAFKA_NUM_NETWORK_THREADS: 8
     KAFKA_NUM_IO_THREADS: 16
     KAFKA_SOCKET_SEND_BUFFER_BYTES: 102400
     KAFKA_SOCKET_RECEIVE_BUFFER_BYTES: 102400
   ```

#### 2. Acumulación de Logs

**Monitoreo:**
```bash
# Tamaño de volúmenes
docker system df -v

# Espacio en topics
kafka-log-dirs.sh --bootstrap-server localhost:9092 --describe --json | jq .

# Logs de MySQL binlog
docker exec mysql mysql -u root -p -e "SHOW BINARY LOGS;"
```

**Limpieza:**
```bash
# Limpiar logs antiguos de Kafka (cuidado en producción)
kafka-configs.sh --bootstrap-server localhost:9092 --entity-type topics --entity-name cocomponente.changes --alter --add-config retention.ms=86400000

# Limpiar binlogs de MySQL
docker exec mysql mysql -u root -p -e "PURGE BINARY LOGS BEFORE DATE(NOW() - INTERVAL 3 DAY);"

# Limpiar logs de Docker
docker system prune -f
```

### Scripts de Verificación Automática

#### 1. health-check.sh

```bash
#!/bin/bash
# Script de verificación completa del sistema

echo "=== VERIFICACIÓN DE SALUD KAFKA CDC ==="
echo "Timestamp: $(date)"
echo ""

# Verificar servicios Docker
echo "1. Estado de Contenedores:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "kafka|mysql"
echo ""

# Verificar Kafka
echo "2. Conectividad Kafka:"
if kafka-topics.sh --bootstrap-server localhost:9092 --list >/dev/null 2>&1; then
    echo "✅ Kafka disponible"
    topic_count=$(kafka-topics.sh --bootstrap-server localhost:9092 --list | wc -l)
    echo "   Topics disponibles: $topic_count"
else
    echo "❌ Kafka no disponible"
fi
echo ""

# Verificar Kafka Connect
echo "3. Estado de Kafka Connect:"
if curl -s http://localhost:8083/connectors >/dev/null 2>&1; then
    echo "✅ Kafka Connect disponible"
    connector_count=$(curl -s http://localhost:8083/connectors | jq length)
    echo "   Conectores configurados: $connector_count"
    
    echo "   Estado de conectores:"
    curl -s http://localhost:8083/connectors | jq -r '.[]' | while read connector; do
        status=$(curl -s "http://localhost:8083/connectors/$connector/status" | jq -r '.connector.state')
        echo "     - $connector: $status"
    done
else
    echo "❌ Kafka Connect no disponible"
fi
echo ""

# Verificar MySQL
echo "4. Estado de Bases de Datos:"
databases=("mysql:cotizador_user:cotizador_pass" "mysql-componentes:componentes_user:componentes_pass" "mysql-cotizaciones:cotizaciones_user:cotizaciones_pass" "mysql-pedidos:pedidos_user:pedidos_pass")

for db_info in "${databases[@]}"; do
    IFS=':' read -r host user pass <<< "$db_info"
    container_name="${host/-mysql/}-mysql"
    if docker exec "$container_name" mysqladmin ping -h localhost -u "$user" -p"$pass" >/dev/null 2>&1; then
        echo "✅ $host disponible"
        
        # Verificar binlog
        binlog_status=$(docker exec "$container_name" mysql -u "$user" -p"$pass" -e "SHOW VARIABLES LIKE 'log_bin';" 2>/dev/null | grep log_bin | awk '{print $2}')
        echo "   Binlog habilitado: $binlog_status"
    else
        echo "❌ $host no disponible"
    fi
done
echo ""

# Verificar flujo de datos
echo "5. Verificación de Flujo de Datos:"
echo "   Verificando eventos recientes en topics principales..."
main_topics=("componentes.changes" "cotizaciones.changes" "pedidos.changes")

for topic in "${main_topics[@]}"; do
    if kafka-topics.sh --bootstrap-server localhost:9092 --list | grep -q "^$topic$"; then
        message_count=$(kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic "$topic" 2>/dev/null | awk -F: '{sum += $3} END {print sum}')
        echo "   - $topic: $message_count eventos totales"
    else
        echo "   - $topic: Topic no encontrado"
    fi
done

echo ""
echo "=== VERIFICACIÓN COMPLETADA ==="
```

#### 2. monitor-cdc.sh

```bash
#!/bin/bash
# Script de monitoreo continuo

echo "=== MONITOR CDC EN TIEMPO REAL ==="
echo "Presiona Ctrl+C para salir"
echo ""

while true; do
    clear
    echo "Timestamp: $(date)"
    echo "===================================="
    
    # Estado de conectores
    echo "CONECTORES:"
    curl -s http://localhost:8083/connectors 2>/dev/null | jq -r '.[]' | while read connector; do
        status=$(curl -s "http://localhost:8083/connectors/$connector/status" 2>/dev/null | jq -r '.connector.state')
        task_status=$(curl -s "http://localhost:8083/connectors/$connector/status" 2>/dev/null | jq -r '.tasks[0].state')
        echo "  $connector: $status ($task_status)"
    done
    
    echo ""
    echo "EVENTOS RECIENTES (últimos 10 seg):"
    
    # Monitorear eventos en tiempo real
    timeout 10 kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic componentes.changes --from-beginning --max-messages 5 2>/dev/null | head -n 3
    
    echo ""
    echo "Refrescando en 15 segundos..."
    sleep 15
done
```

### Comandos de Emergencia

#### Reseteo Completo del Sistema

```bash
#!/bin/bash
# CUIDADO: Este script elimina todos los datos de Kafka y reinicia el CDC

echo "⚠️  RESETEO COMPLETO DE KAFKA CDC"
echo "Este script eliminará todos los datos de Kafka y topics"
read -p "¿Estás seguro? (escribe 'CONFIRMO'): " confirmacion

if [ "$confirmacion" != "CONFIRMO" ]; then
    echo "Operación cancelada"
    exit 1
fi

echo "Iniciando reseteo..."

# 1. Parar todos los conectores
echo "1. Eliminando conectores..."
curl -s http://localhost:8083/connectors | jq -r '.[]' | while read connector; do
    echo "   Eliminando $connector"
    curl -X DELETE "http://localhost:8083/connectors/$connector"
done

# 2. Parar servicios
echo "2. Parando servicios..."
docker-compose stop kafka kafka-connect

# 3. Eliminar volúmenes de Kafka
echo "3. Eliminando datos de Kafka..."
docker volume rm cotizadorpcpartes_kafka_data

# 4. Reiniciar servicios
echo "4. Reiniciando servicios..."
docker-compose up -d kafka kafka-connect

# 5. Esperar inicialización
echo "5. Esperando inicialización..."
sleep 60

# 6. Reconfigurar CDC
echo "6. Reconfigurando CDC..."
./kafka-config/setup-kafka-cdc.sh

echo "✅ Reseteo completado"
```

## Ejemplos Prácticos de Uso

### Ejemplo 1: Monitorear Cambios de Componentes

```bash
# Terminal 1: Monitorear eventos de componentes
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic cocomponente.changes \
  --from-beginning \
  --property print.key=true \
  --property print.timestamp=true

# Terminal 2: Realizar cambio en la base de datos
docker exec mysql-componentes mysql -u componentes_user -pcomponentes_pass -e "
  USE cotizador_componentes_db;
  UPDATE cocomponente SET precio = precio * 1.10 WHERE id = 1;
"

# Resultado esperado en Terminal 1:
# Evento CDC mostrando el cambio de precio
```

### Ejemplo 2: Sincronización entre Microservicios

```bash
# Simular creación de cotización que debe sincronizarse
docker exec mysql-cotizaciones mysql -u cotizaciones_user -pcotizaciones_pass -e "
  USE cotizador_cotizaciones_db;
  INSERT INTO cocotizacion (cliente, fecha_cotizacion, total, estado) 
  VALUES ('Cliente Test', NOW(), 1500.00, 'PENDIENTE');
"

# Verificar evento en topic
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic cocotizacion.changes \
  --from-beginning \
  --max-messages 1 | jq .

# Resultado: Evento JSON con los datos de la nueva cotización
```

### Ejemplo 3: Debugging de Conectores

```bash
# Script completo de debugging
#!/bin/bash

CONNECTOR_NAME="componentes-mysql-connector"

echo "=== DEBUGGING CONECTOR $CONNECTOR_NAME ==="

# 1. Estado del conector
echo "1. Estado del conector:"
curl -s "http://localhost:8083/connectors/$CONNECTOR_NAME/status" | jq .

# 2. Configuración actual
echo "2. Configuración:"
curl -s "http://localhost:8083/connectors/$CONNECTOR_NAME/config" | jq .

# 3. Métricas
echo "3. Métricas:"
curl -s "http://localhost:8083/connectors/$CONNECTOR_NAME/metrics" | jq '."SourceRecordActiveCount",  ."SourceRecordPollTotal"'

# 4. Logs de la tarea
echo "4. Estado de tareas:"
curl -s "http://localhost:8083/connectors/$CONNECTOR_NAME/tasks" | jq .

# 5. Verificar topic de destino
echo "5. Verificar topic de destino:"
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic cocomponente.changes

# 6. Últimos eventos
echo "6. Últimos 3 eventos:"
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic cocomponente.changes \
  --from-beginning \
  --max-messages 3
```

## Registro de Implementación

- **[2025-08-17 10:30:00 MST]** - Subagente1B - Configuración Debezium MySQL Connector completada
- **[2025-08-17 10:45:00 MST]** - Subagente1B - CDC para sincronización automática implementado  
- **[2025-08-17 11:00:00 MST]** - Subagente1B - Conectores específicos para cada base de datos configurados
- **[2025-08-17 11:15:00 MST]** - Subagente1B - Topics de Kafka creados con particiones y replicación
- **[2025-08-17 11:30:00 MST]** - Subagente1B - Infraestructura Kafka CDC completada ✅
- **[2025-08-17 14:45:00 MST]** - Subagente1C - Documentación completa de infraestructura Kafka creada
- **[2025-08-17 15:00:00 MST]** - Subagente1C - Diagramas de flujo de datos documentados
- **[2025-08-17 15:15:00 MST]** - Subagente1C - Configuraciones de conectores Debezium documentadas
- **[2025-08-17 15:30:00 MST]** - Subagente1C - Guía completa de troubleshooting implementada
- **[2025-08-17 15:45:00 MST]** - Subagente1C - Ejemplos prácticos y scripts de monitoreo agregados
- **[2025-08-17 16:00:00 MST]** - Subagente1C - T1.5 Documentación Infraestructura Kafka completada ✅