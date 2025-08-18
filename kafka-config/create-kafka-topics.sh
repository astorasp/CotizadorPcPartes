#!/bin/bash

# =================================================================
# SCRIPT DE CREACIÓN DE TOPICS KAFKA
# Creación de topics para sincronización de Change Data Capture
# =================================================================

echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Iniciando creación de topics Kafka..."

# Configuración
KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
PARTITIONS=3
REPLICATION_FACTOR=1

# Función para esperar que Kafka esté disponible
wait_for_kafka() {
    echo "Esperando que Kafka esté disponible..."
    while ! kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --list >/dev/null 2>&1; do
        echo "Kafka no está disponible. Esperando 10 segundos..."
        sleep 10
    done
    echo "Kafka está disponible."
}

# Función para crear un topic
create_topic() {
    local topic_name=$1
    local partitions=${2:-$PARTITIONS}
    local replication=${3:-$REPLICATION_FACTOR}
    
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Creando topic: $topic_name"
    
    # Verificar si el topic ya existe
    if kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --list | grep -q "^$topic_name$"; then
        echo "Topic $topic_name ya existe. Saltando..."
        return 0
    fi
    
    # Crear el topic
    if kafka-topics.sh --create \
        --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
        --topic $topic_name \
        --partitions $partitions \
        --replication-factor $replication \
        --config cleanup.policy=compact,delete \
        --config retention.ms=604800000 \
        --config segment.ms=86400000; then
        echo "✅ Topic $topic_name creado exitosamente"
    else
        echo "❌ Error creando topic $topic_name"
        return 1
    fi
}

# Función para verificar configuración de un topic
verify_topic() {
    local topic_name=$1
    echo "Verificando configuración del topic $topic_name..."
    kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --describe --topic $topic_name
}

# Función principal
main() {
    # Esperar que Kafka esté disponible
    wait_for_kafka
    
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Creando topics para sincronización CDC..."
    
    # Topics principales para cambios de datos
    topics_principales=(
        "componentes.changes"
        "promociones.changes"
        "cotizaciones.changes"
        "proveedores.changes"
    )
    
    # Topics adicionales para CDC y sincronización
    topics_adicionales=(
        "cocomponente.changes"
        "copromocion.changes"
        "cocotizacion.changes"
        "coproveedor.changes"
        "codetalle_cotizacion.changes"
        "codetalle_pedido.changes"
        "copedido.changes"
        "codetalle_promocion.changes"
        "copc_parte.changes"
    )
    
    # Topics para historial de Debezium
    topics_debezium=(
        "dbhistory.componentes"
        "dbhistory.cotizaciones"
        "dbhistory.pedidos"
        "dbhistory.cotizador-principal"
        "connect-configs"
        "connect-offsets"
        "connect-status"
    )
    
    # Crear topics principales con mayor número de particiones
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Creando topics principales..."
    for topic in "${topics_principales[@]}"; do
        create_topic "$topic" 6 1
    done
    
    # Crear topics adicionales
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Creando topics adicionales..."
    for topic in "${topics_adicionales[@]}"; do
        create_topic "$topic" 3 1
    done
    
    # Crear topics de Debezium con configuración específica
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Creando topics de Debezium..."
    for topic in "${topics_debezium[@]}"; do
        create_topic "$topic" 1 1
    done
    
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Esperando 5 segundos para estabilización..."
    sleep 5
    
    # Verificar topics creados
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Verificando topics creados:"
    kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --list | sort
    
    # Mostrar configuración detallada de topics principales
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Configuración de topics principales:"
    for topic in "${topics_principales[@]}"; do
        echo "--- Topic: $topic ---"
        verify_topic "$topic"
        echo ""
    done
    
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] - Subagente1B - Creación de topics Kafka completada ✅"
}

# Ejecutar función principal
main "$@"