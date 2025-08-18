#!/bin/bash

# =================================================================
# SCRIPT DE CONFIGURACIÓN AUTOMÁTICA DE CONECTORES DEBEZIUM
# Configuración automática para Change Data Capture (CDC)
# =================================================================

echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Iniciando configuración de conectores Debezium..."

# Configuración
KAFKA_CONNECT_URL="http://localhost:8083"
CONNECTORS_DIR="/home/usuario/Asp/github repositorios/curso/CotizadorPcPartes/kafka-config/connectors"

# Función para esperar que Kafka Connect esté disponible
wait_for_kafka_connect() {
    echo "Esperando que Kafka Connect esté disponible..."
    while ! curl -f $KAFKA_CONNECT_URL/connectors >/dev/null 2>&1; do
        echo "Kafka Connect no está disponible. Esperando 10 segundos..."
        sleep 10
    done
    echo "Kafka Connect está disponible."
}

# Función para configurar un conector
configure_connector() {
    local connector_file=$1
    local connector_name=$(basename "$connector_file" .json)
    
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Configurando conector: $connector_name"
    
    # Verificar si el conector ya existe
    if curl -f "$KAFKA_CONNECT_URL/connectors/$connector_name" >/dev/null 2>&1; then
        echo "Conector $connector_name ya existe. Eliminando..."
        curl -X DELETE "$KAFKA_CONNECT_URL/connectors/$connector_name"
        sleep 2
    fi
    
    # Crear el conector
    echo "Creando conector $connector_name..."
    response=$(curl -s -w "%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d @"$connector_file" \
        "$KAFKA_CONNECT_URL/connectors")
    
    http_code="${response: -3}"
    response_body="${response%???}"
    
    if [ "$http_code" -eq 201 ] || [ "$http_code" -eq 200 ]; then
        echo "✅ Conector $connector_name configurado exitosamente"
    else
        echo "❌ Error configurando conector $connector_name. HTTP: $http_code"
        echo "Respuesta: $response_body"
        return 1
    fi
}

# Función para verificar el estado de un conector
check_connector_status() {
    local connector_name=$1
    echo "Verificando estado del conector $connector_name..."
    
    status=$(curl -s "$KAFKA_CONNECT_URL/connectors/$connector_name/status" | jq -r '.connector.state')
    if [ "$status" = "RUNNING" ]; then
        echo "✅ Conector $connector_name está RUNNING"
    else
        echo "⚠️ Conector $connector_name estado: $status"
    fi
}

# Función principal
main() {
    # Esperar que Kafka Connect esté disponible
    wait_for_kafka_connect
    
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Configurando conectores Debezium para CDC..."
    
    # Configurar todos los conectores
    connectors=(
        "$CONNECTORS_DIR/componentes-connector.json"
        "$CONNECTORS_DIR/cotizaciones-connector.json"
        "$CONNECTORS_DIR/pedidos-connector.json"
        "$CONNECTORS_DIR/cotizador-principal-connector.json"
    )
    
    for connector in "${connectors[@]}"; do
        if [ -f "$connector" ]; then
            configure_connector "$connector"
            sleep 3
        else
            echo "❌ Archivo de conector no encontrado: $connector"
        fi
    done
    
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Esperando 10 segundos para estabilización..."
    sleep 10
    
    # Verificar el estado de todos los conectores
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Verificando estado de conectores..."
    connector_names=(
        "componentes-mysql-connector"
        "cotizaciones-mysql-connector"
        "pedidos-mysql-connector"
        "cotizador-principal-mysql-connector"
    )
    
    for name in "${connector_names[@]}"; do
        check_connector_status "$name"
    done
    
    # Listar todos los conectores activos
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Conectores activos:"
    curl -s "$KAFKA_CONNECT_URL/connectors" | jq -r '.[]'
    
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Configuración de conectores Debezium completada ✅"
}

# Ejecutar función principal
main "$@"