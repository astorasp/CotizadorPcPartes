#!/bin/bash

# =================================================================
# SISTEMA DE ALERTAS KAFKA CDC
# Monitoreo automático con notificaciones
# =================================================================

# Configuración
ALERT_LOG="/tmp/kafka-alerts.log"
CONFIG_FILE="/tmp/kafka-alert-config.conf"
WEBHOOK_URL="${SLACK_WEBHOOK_URL:-}"
EMAIL_TO="${ALERT_EMAIL:-}"

# Umbrales configurables
MAX_CONNECTOR_DOWNTIME=300  # 5 minutos
MAX_CONSUMER_LAG=1000       # 1000 mensajes
MAX_CPU_PERCENT=80          # 80% CPU
MAX_MEMORY_MB=1024          # 1GB RAM
MIN_DISK_FREE_GB=5          # 5GB espacio libre

# Función de logging
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$ALERT_LOG"
}

# Función para enviar alertas
send_alert() {
    local level=$1    # INFO, WARNING, CRITICAL
    local message=$2
    local component=$3
    
    local full_message="[$level] Kafka CDC Alert - $component: $message"
    log "$full_message"
    
    # Enviar a Slack si está configurado
    if [ -n "$WEBHOOK_URL" ]; then
        local emoji=""
        case $level in
            "INFO") emoji="ℹ️" ;;
            "WARNING") emoji="⚠️" ;;
            "CRITICAL") emoji="🚨" ;;
        esac
        
        curl -X POST -H 'Content-type: application/json' \
            --data "{\"text\":\"$emoji $full_message\"}" \
            "$WEBHOOK_URL" >/dev/null 2>&1
    fi
    
    # Enviar email si está configurado
    if [ -n "$EMAIL_TO" ] && command -v mail &> /dev/null; then
        echo "$full_message" | mail -s "Kafka CDC Alert [$level]" "$EMAIL_TO"
    fi
    
    # Para alertas críticas, también escribir a syslog
    if [ "$level" = "CRITICAL" ]; then
        logger -p user.crit "$full_message"
    fi
}

# Verificar estado de conectores
check_connectors() {
    local failed_connectors=()
    local connectors=("componentes-mysql-connector" "cotizaciones-mysql-connector" "pedidos-mysql-connector" "cotizador-principal-mysql-connector")
    
    for connector in "${connectors[@]}"; do
        local status=$(curl -s "http://localhost:8083/connectors/$connector/status" 2>/dev/null | jq -r '.connector.state // "UNKNOWN"')
        local task_status=$(curl -s "http://localhost:8083/connectors/$connector/status" 2>/dev/null | jq -r '.tasks[0].state // "UNKNOWN"')
        
        if [ "$status" != "RUNNING" ] || [ "$task_status" != "RUNNING" ]; then
            failed_connectors+=("$connector")
            send_alert "CRITICAL" "Conector en estado: $status (task: $task_status)" "$connector"
        fi
    done
    
    if [ ${#failed_connectors[@]} -eq 0 ]; then
        log "✅ Todos los conectores están funcionando correctamente"
        return 0
    else
        log "❌ Conectores con problemas: ${failed_connectors[*]}"
        return 1
    fi
}

# Verificar consumer lag
check_consumer_lag() {
    local total_lag=$(kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
                     --group cotizador-connect --describe 2>/dev/null | \
                     grep -v GROUP | awk '{sum += $5} END {print sum}')
    
    if [ -z "$total_lag" ]; then
        send_alert "WARNING" "No se pudo obtener información de consumer lag" "Consumer Lag"
        return 1
    fi
    
    if [ "$total_lag" -gt $MAX_CONSUMER_LAG ]; then
        send_alert "WARNING" "Consumer lag alto: $total_lag mensajes (umbral: $MAX_CONSUMER_LAG)" "Consumer Lag"
        return 1
    else
        log "✅ Consumer lag dentro del umbral: $total_lag mensajes"
        return 0
    fi
}

# Verificar conectividad de servicios
check_service_connectivity() {
    local services=(
        "Kafka:localhost:9092:kafka-topics.sh --bootstrap-server localhost:9092 --list"
        "Kafka Connect:localhost:8083:curl -f http://localhost:8083/connectors"
    )
    
    local failed_services=()
    
    for service_info in "${services[@]}"; do
        IFS=':' read -r service_name host port check_cmd <<< "$service_info"
        
        if ! eval "$check_cmd" >/dev/null 2>&1; then
            failed_services+=("$service_name")
            send_alert "CRITICAL" "Servicio no responde en $host:$port" "$service_name"
        fi
    done
    
    if [ ${#failed_services[@]} -eq 0 ]; then
        log "✅ Todos los servicios están respondiendo"
        return 0
    else
        log "❌ Servicios no disponibles: ${failed_services[*]}"
        return 1
    fi
}

# Verificar recursos del sistema
check_system_resources() {
    local alerts=()
    
    # Verificar CPU y memoria de contenedores Kafka
    while IFS= read -r line; do
        local container=$(echo "$line" | awk '{print $1}')
        local cpu=$(echo "$line" | awk '{print $2}' | sed 's/%//')
        local mem_usage=$(echo "$line" | awk '{print $3}' | cut -d'/' -f1)
        
        # Convertir memoria a MB
        local mem_mb
        if [[ $mem_usage == *"GiB" ]]; then
            mem_mb=$(echo "$mem_usage" | sed 's/GiB//' | awk '{print $1 * 1024}')
        elif [[ $mem_usage == *"MiB" ]]; then
            mem_mb=$(echo "$mem_usage" | sed 's/MiB//')
        else
            mem_mb=0
        fi
        
        # Verificar CPU
        if (( $(echo "$cpu > $MAX_CPU_PERCENT" | bc -l) )); then
            alerts+=("$container: CPU alto ${cpu}%")
        fi
        
        # Verificar memoria
        if (( $(echo "$mem_mb > $MAX_MEMORY_MB" | bc -l) )); then
            alerts+=("$container: Memoria alta ${mem_mb}MB")
        fi
        
    done <<< "$(docker stats --no-stream --format '{{.Name}} {{.CPUPerc}} {{.MemUsage}}' | grep -E '(kafka|mysql)')"
    
    # Verificar espacio en disco
    local disk_free_gb=$(df -BG / | awk 'NR==2 {print $4}' | sed 's/G//')
    if [ "$disk_free_gb" -lt $MIN_DISK_FREE_GB ]; then
        alerts+=("Sistema: Poco espacio en disco ${disk_free_gb}GB disponible")
    fi
    
    if [ ${#alerts[@]} -gt 0 ]; then
        for alert in "${alerts[@]}"; do
            send_alert "WARNING" "$alert" "System Resources"
        done
        return 1
    else
        log "✅ Recursos del sistema dentro de los umbrales"
        return 0
    fi
}

# Verificar integridad de datos
check_data_integrity() {
    local issues=()
    
    # Verificar que existan los topics principales
    local main_topics=("componentes.changes" "cotizaciones.changes" "pedidos.changes")
    local missing_topics=()
    
    for topic in "${main_topics[@]}"; do
        if ! kafka-topics.sh --bootstrap-server localhost:9092 --list 2>/dev/null | grep -q "^$topic$"; then
            missing_topics+=("$topic")
        fi
    done
    
    if [ ${#missing_topics[@]} -gt 0 ]; then
        send_alert "CRITICAL" "Topics faltantes: ${missing_topics[*]}" "Data Integrity"
        return 1
    fi
    
    # Verificar binlog en bases de datos
    local databases=(
        "mysql:cotizador_user:cotizador_pass:cotizador-mysql"
        "mysql-componentes:componentes_user:componentes_pass:componentes-mysql"
        "mysql-cotizaciones:cotizaciones_user:cotizaciones_pass:cotizaciones-mysql"
        "mysql-pedidos:pedidos_user:pedidos_pass:pedidos-mysql"
    )
    
    for db_info in "${databases[@]}"; do
        IFS=':' read -r host user pass container <<< "$db_info"
        local binlog_status=$(docker exec "$container" mysql -u "$user" -p"$pass" -e "SHOW VARIABLES LIKE 'log_bin';" 2>/dev/null | grep log_bin | awk '{print $2}')
        
        if [ "$binlog_status" != "ON" ]; then
            issues+=("$host: Binlog deshabilitado")
        fi
    done
    
    if [ ${#issues[@]} -gt 0 ]; then
        for issue in "${issues[@]}"; do
            send_alert "CRITICAL" "$issue" "Data Integrity"
        done
        return 1
    else
        log "✅ Integridad de datos verificada"
        return 0
    fi
}

# Función principal de verificación
run_health_checks() {
    log "=== Iniciando verificación de salud ==="
    
    local checks=(
        "Conectividad de Servicios:check_service_connectivity"
        "Estado de Conectores:check_connectors"
        "Consumer Lag:check_consumer_lag"
        "Recursos del Sistema:check_system_resources"
        "Integridad de Datos:check_data_integrity"
    )
    
    local failed_checks=0
    local total_checks=${#checks[@]}
    
    for check_info in "${checks[@]}"; do
        IFS=':' read -r check_name check_function <<< "$check_info"
        log "Ejecutando: $check_name"
        
        if ! $check_function; then
            ((failed_checks++))
        fi
        
        sleep 2  # Pequeña pausa entre verificaciones
    done
    
    # Resumen
    local success_rate=$((100 * (total_checks - failed_checks) / total_checks))
    
    if [ $failed_checks -eq 0 ]; then
        send_alert "INFO" "Todas las verificaciones pasaron exitosamente ($success_rate%)" "Health Check"
    elif [ $failed_checks -lt 3 ]; then
        send_alert "WARNING" "$failed_checks de $total_checks verificaciones fallaron ($success_rate% éxito)" "Health Check"
    else
        send_alert "CRITICAL" "$failed_checks de $total_checks verificaciones fallaron ($success_rate% éxito)" "Health Check"
    fi
    
    log "=== Verificación completada: $failed_checks errores de $total_checks verificaciones ==="
    return $failed_checks
}

# Función de configuración inicial
setup_config() {
    cat > "$CONFIG_FILE" << EOF
# Configuración de Alertas Kafka CDC
# Generado automáticamente el $(date)

# Umbrales de alerta
MAX_CONNECTOR_DOWNTIME=$MAX_CONNECTOR_DOWNTIME
MAX_CONSUMER_LAG=$MAX_CONSUMER_LAG
MAX_CPU_PERCENT=$MAX_CPU_PERCENT
MAX_MEMORY_MB=$MAX_MEMORY_MB
MIN_DISK_FREE_GB=$MIN_DISK_FREE_GB

# Configuración de notificaciones
SLACK_WEBHOOK_URL="$WEBHOOK_URL"
EMAIL_TO="$EMAIL_TO"

# Horarios de verificación (formato cron)
# 0 */15 * * * - Cada 15 minutos
# 0 */5 * * * - Cada 5 minutos  
# * * * * * - Cada minuto (solo para testing)
EOF

    log "Archivo de configuración creado en: $CONFIG_FILE"
}

# Función de ayuda
show_help() {
    cat << EOF
Sistema de Alertas Kafka CDC

USAGE:
    $0 [OPTIONS]

OPTIONS:
    --check         Ejecutar verificación única
    --setup         Crear archivo de configuración
    --daemon        Ejecutar en modo daemon (cada 5 minutos)
    --test-alert    Enviar alerta de prueba
    --config FILE   Usar archivo de configuración específico
    --help          Mostrar esta ayuda

EJEMPLOS:
    # Verificación única
    $0 --check
    
    # Configurar alertas
    export SLACK_WEBHOOK_URL="https://hooks.slack.com/..."
    export ALERT_EMAIL="admin@example.com"
    $0 --setup
    
    # Ejecutar en modo daemon
    $0 --daemon
    
    # Probar alertas
    $0 --test-alert

VARIABLES DE ENTORNO:
    SLACK_WEBHOOK_URL   URL del webhook de Slack
    ALERT_EMAIL         Email para recibir alertas
    
EOF
}

# Función daemon
run_daemon() {
    log "Iniciando sistema de alertas en modo daemon"
    log "PID: $$"
    
    while true; do
        run_health_checks
        log "Esperando 5 minutos para próxima verificación..."
        sleep 300  # 5 minutos
    done
}

# Función de prueba
test_alert() {
    send_alert "INFO" "Sistema de alertas funcionando correctamente" "Test"
    log "Alerta de prueba enviada"
}

# Main
main() {
    case "${1:-}" in
        --check)
            run_health_checks
            ;;
        --setup)
            setup_config
            ;;
        --daemon)
            run_daemon
            ;;
        --test-alert)
            test_alert
            ;;
        --config)
            if [ -f "$2" ]; then
                source "$2"
                log "Configuración cargada desde: $2"
            else
                echo "Archivo de configuración no encontrado: $2"
                exit 1
            fi
            ;;
        --help|*)
            show_help
            ;;
    esac
}

# Verificar dependencias
if ! command -v jq &> /dev/null; then
    echo "⚠️ jq no está instalado. Instala con: sudo apt-get install jq"
    exit 1
fi

if ! command -v bc &> /dev/null; then
    echo "⚠️ bc no está instalado. Instala con: sudo apt-get install bc"
    exit 1
fi

# Crear directorio de logs si no existe
mkdir -p "$(dirname "$ALERT_LOG")"

# Ejecutar función principal
main "$@"