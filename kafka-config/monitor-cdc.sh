#!/bin/bash

# =================================================================
# MONITOR CDC EN TIEMPO REAL
# Monitoreo continuo de Change Data Capture
# =================================================================

echo "=== MONITOR CDC EN TIEMPO REAL ==="
echo "Presiona Ctrl+C para salir"
echo ""

# Configuración
REFRESH_INTERVAL=15
LOG_FILE="/tmp/kafka-cdc-monitor.log"

# Función para obtener métricas
get_connector_metrics() {
    local connector=$1
    local status=$(curl -s "http://localhost:8083/connectors/$connector/status" 2>/dev/null | jq -r '.connector.state // "UNKNOWN"')
    local task_status=$(curl -s "http://localhost:8083/connectors/$connector/status" 2>/dev/null | jq -r '.tasks[0].state // "UNKNOWN"')
    echo "$connector: $status ($task_status)"
}

get_topic_activity() {
    local topic=$1
    local partition_count=$(kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic "$topic" 2>/dev/null | grep "PartitionCount" | awk '{print $2}' | cut -d',' -f1)
    
    # Obtener offset total
    local total_offset=$(kafka-run-class.sh kafka.tools.GetOffsetShell \
        --broker-list localhost:9092 \
        --topic "$topic" 2>/dev/null | \
        awk -F: '{sum += $3} END {print sum}')
    
    printf "%-25s: %8s mensajes (%s particiones)\n" "$topic" "${total_offset:-0}" "${partition_count:-0}"
}

get_consumer_lag() {
    kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
        --group cotizador-connect --describe 2>/dev/null | \
        grep -v GROUP | awk '{
            if (NF >= 6) {
                topic = $2;
                lag = $5;
                if (lag == "" || lag == "-") lag = 0;
                total_lag += lag;
                if (lag > max_lag) max_lag = lag;
                topics[topic] += lag;
            }
        } END {
            print "Total: " total_lag " mensajes";
            print "Máximo por partición: " max_lag " mensajes";
            for (topic in topics) {
                if (topics[topic] > 0) {
                    print "  " topic ": " topics[topic] " mensajes";
                }
            }
        }'
}

# Función principal de monitoreo
monitor_loop() {
    while true; do
        clear
        echo "=== MONITOR CDC - $(date '+%H:%M:%S') ==="
        echo ""
        
        # 1. Estado de conectores
        echo "CONECTORES DEBEZIUM:"
        echo "==================="
        connectors=("componentes-mysql-connector" "cotizaciones-mysql-connector" "pedidos-mysql-connector" "cotizador-principal-mysql-connector")
        
        for connector in "${connectors[@]}"; do
            get_connector_metrics "$connector"
        done
        
        echo ""
        
        # 2. Actividad en topics
        echo "ACTIVIDAD EN TOPICS:"
        echo "==================="
        main_topics=("componentes.changes" "cotizaciones.changes" "pedidos.changes" "promociones.changes")
        table_topics=("cocomponente.changes" "cocotizacion.changes" "copedido.changes" "copromocion.changes")
        
        echo "Topics principales:"
        for topic in "${main_topics[@]}"; do
            get_topic_activity "$topic"
        done
        
        echo ""
        echo "Topics de tabla:"
        for topic in "${table_topics[@]}"; do
            get_topic_activity "$topic"
        done
        
        echo ""
        
        # 3. Consumer lag
        echo "CONSUMER LAG:"
        echo "============="
        get_consumer_lag
        
        echo ""
        
        # 4. Recursos del sistema
        echo "RECURSOS DEL SISTEMA:"
        echo "===================="
        docker stats --no-stream --format "{{.Name}}: CPU {{.CPUPerc}} | MEM {{.MemUsage}}" | grep -E "(kafka|connect|mysql)" | head -5
        
        echo ""
        
        # 5. Eventos recientes
        echo "EVENTOS RECIENTES (últimos 30 seg):"
        echo "===================================="
        
        # Mostrar algunos eventos de ejemplo
        timeout 3 kafka-console-consumer.sh \
            --bootstrap-server localhost:9092 \
            --topic componentes.changes \
            --max-messages 2 2>/dev/null | \
            jq -r 'select(.op != null) | "Componente " + (.after.nombre // "unknown") + " - " + .op' 2>/dev/null || echo "No hay eventos recientes en componentes.changes"
        
        timeout 3 kafka-console-consumer.sh \
            --bootstrap-server localhost:9092 \
            --topic cotizaciones.changes \
            --max-messages 2 2>/dev/null | \
            jq -r 'select(.op != null) | "Cotización ID:" + (.after.id // "unknown") + " - " + .op' 2>/dev/null || echo "No hay eventos recientes en cotizaciones.changes"
        
        echo ""
        echo "Siguiente actualización en $REFRESH_INTERVAL segundos..."
        echo "[Ctrl+C para salir]"
        
        # Log timestamp
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] Monitor CDC ejecutado" >> "$LOG_FILE"
        
        sleep $REFRESH_INTERVAL
    done
}

# Función de cleanup
cleanup() {
    echo ""
    echo "Deteniendo monitor..."
    echo "Log guardado en: $LOG_FILE"
    exit 0
}

# Configurar trap para Ctrl+C
trap cleanup SIGINT

# Verificar dependencias
if ! command -v jq &> /dev/null; then
    echo "⚠️ jq no está instalado. Algunas funciones pueden no funcionar correctamente."
    echo "Instala con: sudo apt-get install jq"
fi

if ! kafka-topics.sh --bootstrap-server localhost:9092 --list &> /dev/null; then
    echo "❌ No se puede conectar a Kafka en localhost:9092"
    echo "Verifica que Kafka esté ejecutándose"
    exit 1
fi

if ! curl -s http://localhost:8083/connectors &> /dev/null; then
    echo "❌ No se puede conectar a Kafka Connect en localhost:8083"
    echo "Verifica que Kafka Connect esté ejecutándose"
    exit 1
fi

# Iniciar monitoreo
echo "✅ Iniciando monitor CDC..."
echo "Conectado a Kafka (localhost:9092) y Kafka Connect (localhost:8083)"
echo ""
monitor_loop