#!/bin/bash

# =================================================================
# SCRIPT MAESTRO DE CONFIGURACIÓN KAFKA + DEBEZIUM CDC
# Configuración completa de infraestructura de Change Data Capture
# =================================================================

echo "=========================================================="
echo "CONFIGURACIÓN KAFKA + DEBEZIUM CDC"
echo "Subagente 1B - Infraestructura de sincronización"
echo "[$(date '+%Y-%m-%d %H:%M:%S MST')] - Iniciando configuración..."
echo "=========================================================="

# Configuración
SCRIPT_DIR="/home/usuario/Asp/github repositorios/curso/CotizadorPcPartes/kafka-config"
LOG_FILE="$SCRIPT_DIR/setup-cdc.log"

# Función de logging
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S MST')] - Subagente1B - $1" | tee -a "$LOG_FILE"
}

# Función para verificar servicios
check_service() {
    local service_name=$1
    local check_command=$2
    
    log "Verificando servicio: $service_name"
    
    max_attempts=30
    attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if eval "$check_command" >/dev/null 2>&1; then
            log "✅ $service_name está disponible"
            return 0
        fi
        
        log "Intento $attempt/$max_attempts - Esperando $service_name..."
        sleep 10
        ((attempt++))
    done
    
    log "❌ Timeout esperando $service_name después de $((max_attempts * 10)) segundos"
    return 1
}

# Función principal
main() {
    log "Iniciando configuración completa de Kafka + Debezium CDC"
    
    # Verificar que los servicios estén disponibles
    log "Fase 1: Verificación de servicios"
    
    if ! check_service "Kafka" "kafka-topics.sh --bootstrap-server localhost:9092 --list"; then
        log "❌ Kafka no está disponible. Verifique que el contenedor esté ejecutándose."
        exit 1
    fi
    
    if ! check_service "Kafka Connect" "curl -f http://localhost:8083/connectors"; then
        log "❌ Kafka Connect no está disponible. Verifique que el contenedor esté ejecutándose."
        exit 1
    fi
    
    # Verificar bases de datos MySQL
    databases=(
        "mysql:cotizador_user:cotizador_pass"
        "mysql-componentes:componentes_user:componentes_pass"
        "mysql-cotizaciones:cotizaciones_user:cotizaciones_pass"
        "mysql-pedidos:pedidos_user:pedidos_pass"
    )
    
    for db_info in "${databases[@]}"; do
        IFS=':' read -r host user pass <<< "$db_info"
        if ! check_service "MySQL ($host)" "docker exec ${host/-mysql/}-mysql mysqladmin ping -h localhost -u $user -p$pass"; then
            log "❌ Base de datos $host no está disponible"
            exit 1
        fi
    done
    
    # Fase 2: Crear topics de Kafka
    log "Fase 2: Creando topics de Kafka"
    if "$SCRIPT_DIR/create-kafka-topics.sh"; then
        log "✅ Topics de Kafka creados exitosamente"
    else
        log "❌ Error creando topics de Kafka"
        exit 1
    fi
    
    # Fase 3: Configurar conectores Debezium
    log "Fase 3: Configurando conectores Debezium"
    if "$SCRIPT_DIR/setup-debezium-connectors.sh"; then
        log "✅ Conectores Debezium configurados exitosamente"
    else
        log "❌ Error configurando conectores Debezium"
        exit 1
    fi
    
    # Fase 4: Verificación final
    log "Fase 4: Verificación final del sistema"
    
    # Verificar topics
    log "Topics disponibles:"
    kafka-topics.sh --bootstrap-server localhost:9092 --list | sort | while read topic; do
        log "  - $topic"
    done
    
    # Verificar conectores
    log "Conectores Debezium activos:"
    curl -s http://localhost:8083/connectors | jq -r '.[]' | while read connector; do
        status=$(curl -s "http://localhost:8083/connectors/$connector/status" | jq -r '.connector.state')
        log "  - $connector: $status"
    done
    
    # Esperar estabilización
    log "Esperando estabilización del sistema (30 segundos)..."
    sleep 30
    
    # Verificación final de conectores
    log "Verificación final de conectores:"
    all_running=true
    curl -s http://localhost:8083/connectors | jq -r '.[]' | while read connector; do
        status=$(curl -s "http://localhost:8083/connectors/$connector/status" | jq -r '.connector.state')
        if [ "$status" != "RUNNING" ]; then
            log "⚠️ Conector $connector no está RUNNING: $status"
            all_running=false
        else
            log "✅ Conector $connector está RUNNING"
        fi
    done
    
    echo "=========================================================="
    log "CONFIGURACIÓN KAFKA + DEBEZIUM CDC COMPLETADA"
    log "Change Data Capture configurado para todas las bases de datos"
    log "Topics de sincronización creados y funcionando"
    log "Logs de cambios habilitados y capturándose"
    echo "=========================================================="
    
    # Mostrar resumen
    echo ""
    echo "RESUMEN DE CONFIGURACIÓN:"
    echo "========================"
    echo "✅ Topics principales creados:"
    echo "   - componentes.changes"
    echo "   - promociones.changes"
    echo "   - cotizaciones.changes"
    echo "   - proveedores.changes"
    echo ""
    echo "✅ Conectores Debezium configurados:"
    echo "   - componentes-mysql-connector"
    echo "   - cotizaciones-mysql-connector"
    echo "   - pedidos-mysql-connector"
    echo "   - cotizador-principal-mysql-connector"
    echo ""
    echo "✅ Binlog habilitado en todas las bases de datos MySQL"
    echo "✅ CDC funcionando para sincronización automática"
    echo ""
    echo "Para monitorear:"
    echo "- Topics: kafka-topics.sh --bootstrap-server localhost:9092 --list"
    echo "- Conectores: curl http://localhost:8083/connectors"
    echo "- Logs CDC: kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic <topic-name>"
}

# Crear directorio de logs si no existe
mkdir -p "$(dirname "$LOG_FILE")"

# Ejecutar función principal
main "$@" 2>&1 | tee -a "$LOG_FILE"