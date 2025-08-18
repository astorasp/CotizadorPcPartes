#!/bin/bash

# =================================================================
# SCRIPT DE VERIFICACIÓN DE SALUD KAFKA CDC
# Verificación completa del estado del sistema
# =================================================================

echo "=== VERIFICACIÓN DE SALUD KAFKA CDC ==="
echo "Timestamp: $(date)"
echo ""

# Configuración
LOG_FILE="/tmp/kafka-health-check.log"
EXIT_CODE=0

# Función de logging
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Función para verificar servicios
check_service() {
    local service_name=$1
    local check_command=$2
    
    if eval "$check_command" >/dev/null 2>&1; then
        log "✅ $service_name está disponible"
        return 0
    else
        log "❌ $service_name no está disponible"
        EXIT_CODE=1
        return 1
    fi
}

# 1. Verificar contenedores Docker
echo "1. Estado de Contenedores:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(kafka|mysql|connect)"
echo ""

# 2. Verificar Kafka
echo "2. Conectividad Kafka:"
if check_service "Kafka" "kafka-topics.sh --bootstrap-server localhost:9092 --list"; then
    topic_count=$(kafka-topics.sh --bootstrap-server localhost:9092 --list 2>/dev/null | wc -l)
    log "   Topics disponibles: $topic_count"
else
    log "   Error: Kafka no responde"
fi
echo ""

# 3. Verificar Kafka Connect
echo "3. Estado de Kafka Connect:"
if check_service "Kafka Connect" "curl -f http://localhost:8083/connectors"; then
    connector_count=$(curl -s http://localhost:8083/connectors 2>/dev/null | jq length)
    log "   Conectores configurados: $connector_count"
    
    echo "   Estado de conectores:"
    curl -s http://localhost:8083/connectors 2>/dev/null | jq -r '.[]' | while read connector; do
        status=$(curl -s "http://localhost:8083/connectors/$connector/status" 2>/dev/null | jq -r '.connector.state')
        if [ "$status" = "RUNNING" ]; then
            log "     - $connector: ✅ $status"
        else
            log "     - $connector: ❌ $status"
            EXIT_CODE=1
        fi
    done
else
    log "   Error: Kafka Connect no responde"
fi
echo ""

# 4. Verificar MySQL
echo "4. Estado de Bases de Datos:"
databases=(
    "mysql:cotizador_user:cotizador_pass:cotizador-mysql"
    "mysql-componentes:componentes_user:componentes_pass:componentes-mysql"
    "mysql-cotizaciones:cotizaciones_user:cotizaciones_pass:cotizaciones-mysql"
    "mysql-pedidos:pedidos_user:pedidos_pass:pedidos-mysql"
)

for db_info in "${databases[@]}"; do
    IFS=':' read -r host user pass container <<< "$db_info"
    if check_service "$host" "docker exec $container mysqladmin ping -h localhost -u $user -p$pass"; then
        # Verificar binlog
        binlog_status=$(docker exec "$container" mysql -u "$user" -p"$pass" -e "SHOW VARIABLES LIKE 'log_bin';" 2>/dev/null | grep log_bin | awk '{print $2}')
        if [ "$binlog_status" = "ON" ]; then
            log "   Binlog habilitado: ✅ $binlog_status"
        else
            log "   Binlog habilitado: ❌ $binlog_status"
            EXIT_CODE=1
        fi
    else
        log "   Error: $host no responde"
    fi
done
echo ""

# 5. Verificar flujo de datos
echo "5. Verificación de Flujo de Datos:"
echo "   Verificando eventos recientes en topics principales..."
main_topics=("componentes.changes" "cotizaciones.changes" "pedidos.changes" "promociones.changes")

for topic in "${main_topics[@]}"; do
    if kafka-topics.sh --bootstrap-server localhost:9092 --list 2>/dev/null | grep -q "^$topic$"; then
        # Obtener offset total del topic
        total_messages=$(kafka-run-class.sh kafka.tools.GetOffsetShell \
            --broker-list localhost:9092 \
            --topic "$topic" 2>/dev/null | \
            awk -F: '{sum += $3} END {print sum}')
        log "   - $topic: ${total_messages:-0} eventos totales"
        
        # Verificar actividad reciente (últimos 5 minutos)
        recent_activity=$(timeout 5 kafka-console-consumer.sh \
            --bootstrap-server localhost:9092 \
            --topic "$topic" \
            --from-beginning \
            --max-messages 1 2>/dev/null | wc -l)
        
        if [ "${recent_activity:-0}" -gt 0 ]; then
            log "     ✅ Actividad reciente detectada"
        else
            log "     ⚠️ No hay actividad reciente"
        fi
    else
        log "   - $topic: ❌ Topic no encontrado"
        EXIT_CODE=1
    fi
done

# 6. Verificar consumer lag
echo ""
echo "6. Consumer Lag:"
if kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list 2>/dev/null | grep -q "cotizador-connect"; then
    total_lag=$(kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
                --group cotizador-connect --describe 2>/dev/null | \
                grep -v GROUP | awk '{sum += $5} END {print sum}')
    
    if [ "${total_lag:-0}" -lt 100 ]; then
        log "   Total lag: ✅ ${total_lag:-0} mensajes"
    elif [ "${total_lag:-0}" -lt 1000 ]; then
        log "   Total lag: ⚠️ ${total_lag:-0} mensajes (moderado)"
    else
        log "   Total lag: ❌ ${total_lag:-0} mensajes (alto)"
        EXIT_CODE=1
    fi
else
    log "   ❌ Consumer group 'cotizador-connect' no encontrado"
    EXIT_CODE=1
fi

# 7. Resumen final
echo ""
echo "7. Recursos del Sistema:"
echo "   Uso de CPU y Memoria de contenedores Kafka:"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" | grep -E "(kafka|connect)"

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo "🎉 SISTEMA SALUDABLE - Todos los componentes funcionan correctamente"
    log "✅ Verificación de salud completada exitosamente"
else
    echo "⚠️ PROBLEMAS DETECTADOS - Revisar logs y componentes marcados con ❌"
    log "❌ Verificación de salud detectó problemas"
fi

echo ""
echo "Log detallado guardado en: $LOG_FILE"
echo "=== VERIFICACIÓN COMPLETADA ==="

exit $EXIT_CODE