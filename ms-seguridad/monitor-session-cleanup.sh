#!/bin/bash

# Script para monitorear los logs del job de limpieza de sesiones
# Uso: ./monitor-session-cleanup.sh

echo "🔍 Iniciando monitoreo del job de limpieza de sesiones..."
echo "📁 Buscando logs en:"
echo "   - Docker container: ms-seguridad"
echo "   - Archivo local: logs/seguridad-application.log"
echo ""

# Función para mostrar logs de Docker
monitor_docker_logs() {
    echo "📋 === LOGS DE DOCKER CONTAINER ==="
    echo "Mostrando logs en tiempo real del contenedor ms-seguridad..."
    echo "Presiona Ctrl+C para detener"
    echo ""
    
    # Filtrar logs relacionados con limpieza de sesiones
    docker logs -f ms-seguridad 2>&1 | grep -E "(SessionCleanup|QuartzConfig|🧹|🔍|⏰|📋|🎯|✅|⚠️|❌)"
}

# Función para mostrar logs de archivo local
monitor_file_logs() {
    LOG_FILE="logs/seguridad-application.log"
    
    if [ -f "$LOG_FILE" ]; then
        echo "📋 === LOGS DE ARCHIVO LOCAL ==="
        echo "Mostrando logs en tiempo real del archivo: $LOG_FILE"
        echo "Presiona Ctrl+C para detener"
        echo ""
        
        # Filtrar logs relacionados con limpieza de sesiones
        tail -f "$LOG_FILE" | grep -E "(SessionCleanup|QuartzConfig|🧹|🔍|⏰|📋|🎯|✅|⚠️|❌)"
    else
        echo "⚠️ Archivo de log no encontrado: $LOG_FILE"
        echo "   El archivo se creará cuando la aplicación esté ejecutándose"
    fi
}

# Función para verificar estado del contenedor
check_container_status() {
    echo "🔍 === ESTADO DEL CONTENEDOR ==="
    
    if docker ps --format "table {{.Names}}\t{{.Status}}" | grep -q "ms-seguridad"; then
        echo "✅ Contenedor ms-seguridad está ejecutándose"
        docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep "ms-seguridad"
    else
        echo "❌ Contenedor ms-seguridad no está ejecutándose"
        echo "   Ejecuta: docker-compose up -d ms-seguridad"
        return 1
    fi
    echo ""
}

# Función para verificar endpoint de monitoreo
check_monitoring_endpoint() {
    echo "🔍 === VERIFICANDO ENDPOINT DE MONITOREO ==="
    
    # Intentar acceder al endpoint de monitoreo
    RESPONSE=$(curl -s -w "\n%{http_code}" "http://localhost/api/seguridad/monitoring/session-cleanup-job" 2>/dev/null)
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✅ Endpoint de monitoreo accesible"
        echo "📊 Estado del job:"
        echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
    else
        echo "⚠️ Endpoint de monitoreo no accesible (HTTP $HTTP_CODE)"
        echo "   URL: http://localhost/api/seguridad/monitoring/session-cleanup-job"
        echo "   Verifica que el contenedor esté ejecutándose y el gateway esté activo"
    fi
    echo ""
}

# Función para ejecutar job manualmente
trigger_manual_execution() {
    echo "🚀 === EJECUTANDO JOB MANUALMENTE ==="
    
    RESPONSE=$(curl -s -w "\n%{http_code}" "http://localhost/api/seguridad/monitoring/session-cleanup-job/trigger" 2>/dev/null)
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✅ Job ejecutado manualmente"
        echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
    else
        echo "❌ Error al ejecutar job manualmente (HTTP $HTTP_CODE)"
        echo "$BODY"
    fi
    echo ""
}

# Función principal
main() {
    echo "🔧 === MONITOR DE LIMPIEZA DE SESIONES ==="
    echo "Fecha: $(date)"
    echo ""
    
    # Verificar estado del contenedor
    if ! check_container_status; then
        exit 1
    fi
    
    # Verificar endpoint de monitoreo
    check_monitoring_endpoint
    
    # Mostrar opciones
    echo "📋 === OPCIONES DISPONIBLES ==="
    echo "1. Monitorear logs de Docker en tiempo real"
    echo "2. Monitorear logs de archivo local en tiempo real"
    echo "3. Verificar estado del job"
    echo "4. Ejecutar job manualmente"
    echo "5. Salir"
    echo ""
    
    read -p "Selecciona una opción (1-5): " option
    
    case $option in
        1)
            monitor_docker_logs
            ;;
        2)
            monitor_file_logs
            ;;
        3)
            check_monitoring_endpoint
            ;;
        4)
            trigger_manual_execution
            echo "Presiona Enter para continuar..."
            read
            main
            ;;
        5)
            echo "👋 Saliendo del monitor..."
            exit 0
            ;;
        *)
            echo "❌ Opción inválida"
            main
            ;;
    esac
}

# Verificar dependencias
command -v docker >/dev/null 2>&1 || { echo "❌ Docker no está instalado. Instala Docker y vuelve a intentar."; exit 1; }
command -v curl >/dev/null 2>&1 || { echo "❌ curl no está instalado. Instala curl y vuelve a intentar."; exit 1; }

# Ejecutar función principal
main