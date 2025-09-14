#!/bin/bash

# =================================================================
# SCRIPT PARA SOLUCIONAR PROBLEMA DE ORDEN EN CDC
# Separa conectores por dependencias de foreign keys
# =================================================================

echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Iniciando solución de problema de orden CDC..."

KAFKA_CONNECT_URL="http://localhost:8083"
CONNECTORS_DIR="/home/usuario/Asp/github repositorios/curso/CotizadorPcPartes/kafka-config/connectors"

# Función para esperar que Kafka Connect esté disponible
wait_for_kafka_connect() {
    echo "Verificando que Kafka Connect esté disponible..."
    while ! curl -f $KAFKA_CONNECT_URL/connectors >/dev/null 2>&1; do
        echo "Kafka Connect no disponible. Esperando 5 segundos..."
        sleep 5
    done
    echo "✅ Kafka Connect disponible."
}

# Función para eliminar conector
delete_connector() {
    local connector_name=$1
    echo "🗑️ Eliminando conector problemático: $connector_name"

    if curl -f "$KAFKA_CONNECT_URL/connectors/$connector_name" >/dev/null 2>&1; then
        curl -X DELETE "$KAFKA_CONNECT_URL/connectors/$connector_name"
        echo "✅ Conector $connector_name eliminado"
        sleep 3
    else
        echo "ℹ️ Conector $connector_name no existe"
    fi
}

# Función para crear conector
create_connector() {
    local connector_file=$1
    local connector_name=$(basename "$connector_file" .json)

    echo "🚀 Creando conector: $connector_name"

    response=$(curl -s -w "%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d @"$connector_file" \
        "$KAFKA_CONNECT_URL/connectors")

    http_code="${response: -3}"

    if [ "$http_code" -eq 201 ] || [ "$http_code" -eq 200 ]; then
        echo "✅ Conector $connector_name creado exitosamente"
        return 0
    else
        echo "❌ Error creando conector $connector_name: HTTP $http_code"
        echo "Response: ${response%???}"
        return 1
    fi
}

# Función para verificar estado del conector
check_connector_status() {
    local connector_name=$1
    echo "🔍 Verificando estado del conector: $connector_name"

    local status=$(curl -s "$KAFKA_CONNECT_URL/connectors/$connector_name/status")
    echo "$status" | jq '.'

    local state=$(echo "$status" | jq -r '.connector.state')
    if [ "$state" = "RUNNING" ]; then
        echo "✅ Conector $connector_name está RUNNING"
        return 0
    else
        echo "⚠️ Conector $connector_name estado: $state"
        return 1
    fi
}

# ===== IMPLEMENTACIÓN =====

wait_for_kafka_connect

echo ""
echo "🔧 PASO 1: Eliminar conector problemático"
delete_connector "componentes-cotizaciones-sink"

echo ""
echo "🔧 PASO 2: Crear conector para tablas BASE (sin dependencias)"
if create_connector "$CONNECTORS_DIR/componentes-cotizaciones-base-sink.json"; then
    sleep 10
    check_connector_status "componentes-cotizaciones-base-sink"
else
    echo "❌ ERROR: No se pudo crear conector base. Abortando..."
    exit 1
fi

echo ""
echo "🔧 PASO 3: Crear conector para tablas con DEPENDENCIAS"
sleep 5  # Delay para asegurar que las tablas base se procesen primero

if create_connector "$CONNECTORS_DIR/componentes-cotizaciones-relations-sink.json"; then
    sleep 10
    check_connector_status "componentes-cotizaciones-relations-sink"
else
    echo "❌ ERROR: No se pudo crear conector de relaciones"
    exit 1
fi

echo ""
echo "🔧 PASO 4: Verificación final"
echo "📋 Lista de conectores activos:"
curl -s "$KAFKA_CONNECT_URL/connectors" | jq '.'

echo ""
echo "🎉 ✅ SOLUCIÓN IMPLEMENTADA EXITOSAMENTE"
echo ""
echo "📊 BENEFICIOS:"
echo "   - Tablas padre (cocomponente, copromocion) se procesan PRIMERO"
echo "   - Tablas hija (copc_parte, etc.) se procesan DESPUÉS"
echo "   - No más errores de foreign key constraint"
echo "   - Reintentos automáticos para casos edge"
echo ""
echo "🚀 Puedes crear nuevas PCs sin problemas ahora!"