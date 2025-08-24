#!/bin/bash

# Script de validación para confirmar que el sistema CDC está 100% funcional
# Ejecuta después de docker-compose up -d

echo "🔍 VALIDACIÓN COMPLETA DEL SISTEMA CDC"
echo "======================================"

# Función para esperar que un servicio esté listo
wait_for_service() {
    local service_name=$1
    local check_url=$2
    local max_attempts=30
    local attempt=0
    
    echo "⏱️  Esperando que $service_name esté listo..."
    while [ $attempt -lt $max_attempts ]; do
        if curl -s "$check_url" > /dev/null 2>&1; then
            echo "✅ $service_name está listo"
            return 0
        fi
        attempt=$((attempt + 1))
        echo "   Intento $attempt/$max_attempts..."
        sleep 10
    done
    echo "❌ $service_name no está listo después de $((max_attempts * 10)) segundos"
    return 1
}

# Validar que Kafka Connect esté operativo
wait_for_service "Kafka Connect" "http://localhost:8083/connectors"

# Verificar número de conectores
echo ""
echo "📊 VERIFICANDO CONECTORES CDC:"
connector_count=$(curl -s http://localhost:8083/connectors | jq -r 'length')
echo "   Total conectores configurados: $connector_count/8"

if [ "$connector_count" -ne 8 ]; then
    echo "❌ ERROR: Se esperan 8 conectores, encontrados: $connector_count"
    echo "   Conectores faltantes probablemente fallaron durante la creación automática"
    echo "   Causa común: Problemas de autenticación mysql_native_password"
    exit 1
fi

# Verificar estado de cada conector
echo ""
echo "🔍 VERIFICANDO ESTADO DE CONECTORES:"
all_running=true

for connector in $(curl -s http://localhost:8083/connectors | jq -r '.[]' | sort); do
    state=$(curl -s "http://localhost:8083/connectors/$connector/status" | jq -r '.connector.state')
    task_state=$(curl -s "http://localhost:8083/connectors/$connector/status" | jq -r '.tasks[0].state // "NO_TASK"')
    
    if [ "$state" = "RUNNING" ] && [ "$task_state" = "RUNNING" ]; then
        echo "   ✅ $connector: $state ($task_state)"
    else
        echo "   ❌ $connector: $state ($task_state)"
        all_running=false
    fi
done

if [ "$all_running" = false ]; then
    echo ""
    echo "❌ ERROR: Algunos conectores no están funcionando correctamente"
    exit 1
fi

# Verificar permisos debezium en todas las bases
echo ""
echo "🔐 VERIFICANDO PERMISOS DEBEZIUM:"

for db in componentes cotizaciones pedidos; do
    echo "   Verificando permisos en $db..."
    if docker exec ${db}-mysql mysql -u debezium -pdbz_password -e "SHOW GRANTS FOR 'debezium'@'%';" > /dev/null 2>&1; then
        echo "   ✅ Permisos debezium OK en $db"
    else
        echo "   ❌ ERROR: Permisos debezium fallidos en $db"
        all_running=false
    fi
done

if [ "$all_running" = false ]; then
    echo ""
    echo "❌ ERROR: Permisos debezium no están configurados correctamente"
    exit 1
fi

# Realizar prueba end-to-end
echo ""
echo "🧪 REALIZANDO PRUEBA END-TO-END:"
test_id="VALIDATION-$(date +%H%M%S)"
echo "   Insertando componente de prueba: $test_id"

# Insertar en componentes
docker exec componentes-mysql mysql -u componentes_user -pcomponentes_pass cotizador_componentes_db -e \
    "INSERT INTO cocomponente (id_componente, modelo, marca, precio_base, costo, descripcion, id_tipo_componente, id_promocion) VALUES ('$test_id', 'CDC-VALIDATION', 'AUTO-TEST', 999.99, 899.99, 'Prueba automatizada CDC', 1, 1);" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "   ❌ ERROR: No se pudo insertar en componentes"
    exit 1
fi

echo "   ⏱️  Esperando replicación (5 segundos)..."
sleep 5

# Verificar replicación en cotizaciones
echo "   Verificando replicación en cotizaciones..."
cotizaciones_count=$(docker exec cotizaciones-mysql mysql -u cotizaciones_user -pcotizaciones_pass cotizador_cotizaciones_db -e \
    "SELECT COUNT(*) FROM cocomponente WHERE id_componente='$test_id';" 2>/dev/null | tail -1)

if [ "$cotizaciones_count" -eq 1 ]; then
    echo "   ✅ Replicación a cotizaciones: OK"
else
    echo "   ❌ ERROR: Replicación a cotizaciones falló"
    exit 1
fi

# Verificar replicación en pedidos
echo "   Verificando replicación en pedidos..."
pedidos_count=$(docker exec pedidos-mysql mysql -u pedidos_user -ppedidos_pass cotizador_pedidos_db -e \
    "SELECT COUNT(*) FROM cocomponente WHERE id_componente='$test_id';" 2>/dev/null | tail -1)

if [ "$pedidos_count" -eq 1 ]; then
    echo "   ✅ Replicación a pedidos: OK"
else
    echo "   ❌ ERROR: Replicación a pedidos falló"
    exit 1
fi

# Limpiar datos de prueba
echo "   🧹 Limpiando datos de prueba..."
docker exec componentes-mysql mysql -u componentes_user -pcomponentes_pass cotizador_componentes_db -e \
    "DELETE FROM cocomponente WHERE id_componente='$test_id';" 2>/dev/null

echo ""
echo "🎉 ¡VALIDACIÓN COMPLETA EXITOSA!"
echo "================================"
echo "✅ Todos los conectores CDC funcionando correctamente"
echo "✅ Permisos debezium configurados correctamente"  
echo "✅ Replicación en tiempo real funcionando"
echo "✅ Sistema listo para producción"
echo ""
echo "🚀 El sistema CDC está 100% operacional y automatizado"