#!/bin/bash

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para mostrar ayuda
show_help() {
    echo -e "${BLUE}=== Scripts de Utilidad - Cotizador Docker ===${NC}"
    echo ""
    echo "Uso: $0 [COMANDO]"
    echo ""
    echo "Comandos disponibles:"
    echo -e "  ${GREEN}start${NC}     - Levantar todos los servicios"
    echo -e "  ${GREEN}stop${NC}      - Detener todos los servicios"
    echo -e "  ${GREEN}restart${NC}   - Reiniciar todos los servicios"
    echo -e "  ${GREEN}build${NC}     - Construir todas las imágenes"
    echo -e "  ${GREEN}rebuild${NC}   - Reconstruir todas las imágenes sin cache"
    echo -e "  ${GREEN}logs${NC}      - Ver logs de todos los servicios"
    echo -e "  ${GREEN}logs-f${NC}    - Ver logs en tiempo real"
    echo -e "  ${GREEN}status${NC}    - Ver estado de los servicios"
    echo -e "  ${GREEN}clean${NC}     - Limpiar contenedores, imágenes y volúmenes"
    echo -e "  ${GREEN}reset${NC}     - Reset completo (clean + rebuild + start)"
    echo -e "  ${GREEN}db-init${NC}   - Reinicializar base de datos"
    echo -e "  ${GREEN}health${NC}    - Verificar salud de todos los servicios"
    echo -e "  ${GREEN}metrics${NC}   - Ver métricas del backend"
    echo -e "  ${GREEN}info${NC}      - Ver información del backend"
    echo -e "  ${GREEN}endpoints${NC} - Ver endpoints de Actuator disponibles"
    echo -e "  ${GREEN}monitor${NC}   - Monitorear el inicio del backend en tiempo real"
    echo -e "  ${GREEN}shell-backend${NC} - Acceder al shell del backend"
    echo -e "  ${GREEN}shell-seguridad${NC} - Acceder al shell del microservicio seguridad"
    echo -e "  ${GREEN}shell-mysql${NC}   - Acceder al shell de MySQL Cotizador"
    echo -e "  ${GREEN}shell-mysql-seguridad${NC} - Acceder al shell de MySQL Seguridad"
    echo ""
    echo "Servicios individuales:"
    echo -e "  ${YELLOW}start-mysql${NC}     - Solo MySQL Cotizador"
    echo -e "  ${YELLOW}start-mysql-seguridad${NC} - Solo MySQL Seguridad"
    echo -e "  ${YELLOW}start-seguridad${NC} - Solo Microservicio Seguridad"
    echo -e "  ${YELLOW}start-backend${NC}   - Solo Backend"
    echo -e "  ${YELLOW}start-frontend${NC}  - Solo Frontend"
    echo ""
}

# Función para mostrar estado
show_status() {
    echo -e "${BLUE}=== Estado de los Servicios ===${NC}"
    docker-compose ps
    echo ""
    echo -e "${BLUE}=== URLs de Acceso ===${NC}"
    echo -e "Frontend:  ${GREEN}http://localhost${NC}"
    echo -e "Backend:   ${GREEN}http://localhost:8080/cotizador/v1/api${NC}"
    echo -e "Seguridad: ${GREEN}http://localhost:8081${NC}"
    echo -e "MySQL Cotizador: ${GREEN}localhost:3306${NC}"
    echo -e "MySQL Seguridad: ${GREEN}localhost:3307${NC}"
}

# Función para verificar salud
check_health() {
    echo -e "${BLUE}=== Verificando Salud de los Servicios ===${NC}"
    
    # MySQL Cotizador
    echo -n "MySQL Cotizador: "
    if docker-compose exec mysql mysqladmin ping -h localhost -u cotizador_user -pcotizador_pass --silent 2>/dev/null; then
        echo -e "${GREEN}✓ Saludable${NC}"
    else
        echo -e "${RED}✗ No responde${NC}"
    fi
    
    # MySQL Seguridad
    echo -n "MySQL Seguridad: "
    if docker-compose exec mysql-seguridad mysqladmin ping -h localhost -u seguridad_user -pseguridad_pass --silent 2>/dev/null; then
        echo -e "${GREEN}✓ Saludable${NC}"
    else
        echo -e "${RED}✗ No responde${NC}"
    fi
    
    # Microservicio Seguridad
    echo -n "MS-Seguridad: "
    health_response=$(curl -s -f http://localhost:8081/actuator/health 2>/dev/null)
    if [ $? -eq 0 ]; then
        status=$(echo "$health_response" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)
        if [ "$status" = "UP" ]; then
            echo -e "${GREEN}✓ Saludable (Status: UP)${NC}"
        else
            echo -e "${YELLOW}⚠ Responde pero con problemas (Status: $status)${NC}"
        fi
    else
        echo -e "${RED}✗ No responde${NC}"
    fi
    
    # Backend (con detalle de Actuator)
    echo -n "Backend: "
    health_response=$(curl -u admin:admin123 -s -f http://localhost:8080/cotizador/v1/api/actuator/health 2>/dev/null)
    if [ $? -eq 0 ]; then
        status=$(echo "$health_response" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)
        if [ "$status" = "UP" ]; then
            echo -e "${GREEN}✓ Saludable (Status: UP)${NC}"
        else
            echo -e "${YELLOW}⚠ Responde pero con problemas (Status: $status)${NC}"
        fi
    else
        echo -e "${RED}✗ No responde${NC}"
    fi
    
    # Frontend
    echo -n "Frontend: "
    if curl -s -f http://localhost/ > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Saludable${NC}"
    else
        echo -e "${RED}✗ No responde${NC}"
    fi
}

# Función para mostrar métricas del backend
show_metrics() {
    echo -e "${BLUE}=== Métricas del Backend ===${NC}"
    curl -u admin:admin123 -s http://localhost:8080/cotizador/v1/api/actuator/metrics 2>/dev/null | jq '.' 2>/dev/null || \
        curl -u admin:admin123 -s http://localhost:8080/cotizador/v1/api/actuator/metrics
}

# Función para mostrar información del backend
show_info() {
    echo -e "${BLUE}=== Información del Backend ===${NC}"
    curl -u admin:admin123 -s http://localhost:8080/cotizador/v1/api/actuator/info 2>/dev/null | jq '.' 2>/dev/null || \
        curl -u admin:admin123 -s http://localhost:8080/cotizador/v1/api/actuator/info
}

# Función para mostrar endpoints disponibles
show_endpoints() {
    echo -e "${BLUE}=== Endpoints de Actuator Disponibles ===${NC}"
    curl -u admin:admin123 -s http://localhost:8080/cotizador/v1/api/actuator 2>/dev/null | jq '.' 2>/dev/null || \
        curl -u admin:admin123 -s http://localhost:8080/cotizador/v1/api/actuator
}

# Función para monitorear el inicio del backend
monitor_startup() {
    echo -e "${BLUE}=== Monitoreando Inicio del Backend ===${NC}"
    echo -e "${YELLOW}Presiona Ctrl+C para salir${NC}"
    echo ""
    
    local start_time=$(date +%s)
    local max_wait=300  # 5 minutos máximo
    local check_interval=5
    
    while true; do
        local current_time=$(date +%s)
        local elapsed=$((current_time - start_time))
        
        if [ $elapsed -gt $max_wait ]; then
            echo -e "${RED}⏰ Tiempo máximo de espera alcanzado (5 minutos)${NC}"
            break
        fi
        
        # Verificar estado del contenedor
        local container_status=$(docker inspect -f '{{.State.Health.Status}}' cotizador-backend 2>/dev/null || echo "no-health")
        local container_running=$(docker inspect -f '{{.State.Running}}' cotizador-backend 2>/dev/null || echo "false")
        
        printf "\r⏱️  %02d:%02d - Container: " $((elapsed/60)) $((elapsed%60))
        
        if [ "$container_running" = "true" ]; then
            case "$container_status" in
                "healthy")
                    echo -e "${GREEN}✓ HEALTHY${NC}"
                    echo ""
                    echo -e "${GREEN}🎉 Backend iniciado correctamente!${NC}"
                    echo -e "Tiempo total: $(($elapsed/60))m $(($elapsed%60))s"
                    break
                    ;;
                "unhealthy")
                    echo -e "${RED}✗ UNHEALTHY${NC}"
                    ;;
                "starting")
                    echo -e "${YELLOW}⏳ STARTING${NC}"
                    ;;
                "no-health")
                    echo -e "${BLUE}📋 NO-HEALTH-CHECK${NC}"
                    ;;
                *)
                    echo -e "${YELLOW}❓ $container_status${NC}"
                    ;;
            esac
        else
            echo -e "${RED}🛑 STOPPED${NC}"
        fi
        
        # Verificar logs recientes para detectar errores críticos
        local recent_logs=$(docker logs --tail 5 cotizador-backend 2>/dev/null | grep -i "error\|exception\|failed" | tail -1)
        if [ ! -z "$recent_logs" ]; then
            echo -e "${RED}🚨 Error reciente: ${recent_logs:0:80}...${NC}"
        fi
        
        sleep $check_interval
    done
    
    echo ""
    echo -e "${BLUE}=== Estado Final ===${NC}"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep cotizador
}

# Función para limpiar todo
clean_all() {
    echo -e "${YELLOW}Deteniendo servicios...${NC}"
    docker-compose down
    
    echo -e "${YELLOW}Eliminando contenedores...${NC}"
    docker-compose rm -f
    
    echo -e "${YELLOW}Eliminando imágenes del proyecto...${NC}"
    docker rmi $(docker images | grep cotizador | awk '{print $3}') 2>/dev/null || true
    
    echo -e "${YELLOW}Eliminando volúmenes...${NC}"
    docker volume rm cotizador_mysql_data cotizador_backend_logs 2>/dev/null || true
    
    echo -e "${YELLOW}Limpiando Docker...${NC}"
    docker system prune -f
    
    echo -e "${GREEN}Limpieza completada${NC}"
}

# Función para reset completo
reset_all() {
    echo -e "${RED}¿Está seguro de realizar un reset completo? Esto eliminará todos los datos. [y/N]${NC}"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        clean_all
        echo -e "${BLUE}Reconstruyendo...${NC}"
        docker-compose build --no-cache
        echo -e "${BLUE}Iniciando servicios...${NC}"
        docker-compose up -d
        echo -e "${GREEN}Reset completado${NC}"
    else
        echo -e "${YELLOW}Reset cancelado${NC}"
    fi
}

# Función para reinicializar BD
init_db() {
    echo -e "${YELLOW}Reinicializando bases de datos...${NC}"
    
    # Detener servicios que dependen de las bases de datos
    docker-compose stop backend ms-seguridad
    
    # Detener y limpiar MySQL Cotizador
    docker-compose stop mysql
    docker volume rm cotizador_mysql_data 2>/dev/null || true
    
    # Detener y limpiar MySQL Seguridad  
    docker-compose stop mysql-seguridad
    docker volume rm mysql_seguridad_data 2>/dev/null || true
    
    # Reiniciar ambas bases de datos
    docker-compose up -d mysql mysql-seguridad
    
    echo -e "${GREEN}Bases de datos reinicializadas${NC}"
    echo -e "${BLUE}Esperando que las bases de datos estén listas...${NC}"
    sleep 30
}

# Script principal
case "$1" in
    "start")
        echo -e "${BLUE}Iniciando todos los servicios...${NC}"
        docker-compose up -d
        echo -e "${GREEN}Servicios iniciados${NC}"
        show_status
        ;;
    "stop")
        echo -e "${YELLOW}Deteniendo todos los servicios...${NC}"
        docker-compose down
        echo -e "${GREEN}Servicios detenidos${NC}"
        ;;
    "restart")
        echo -e "${YELLOW}Reiniciando servicios...${NC}"
        docker-compose restart
        echo -e "${GREEN}Servicios reiniciados${NC}"
        ;;
    "build")
        echo -e "${BLUE}Construyendo imágenes...${NC}"
        docker-compose build
        echo -e "${GREEN}Construcción completada${NC}"
        ;;
    "rebuild")
        echo -e "${BLUE}Reconstruyendo imágenes sin cache...${NC}"
        docker-compose build --no-cache
        echo -e "${GREEN}Reconstrucción completada${NC}"
        ;;
    "logs")
        docker-compose logs
        ;;
    "logs-f")
        docker-compose logs -f
        ;;
    "status")
        show_status
        ;;
    "clean")
        clean_all
        ;;
    "reset")
        reset_all
        ;;
    "db-init")
        init_db
        ;;
    "health")
        check_health
        ;;
    "metrics")
        show_metrics
        ;;
    "info")
        show_info
        ;;
    "endpoints")
        show_endpoints
        ;;
    "monitor")
        monitor_startup
        ;;
    "shell-backend")
        docker-compose exec backend /bin/bash
        ;;
    "shell-seguridad")
        docker-compose exec ms-seguridad /bin/bash
        ;;
    "shell-mysql")
        docker-compose exec mysql mysql -u cotizador_user -pcotizador_pass cotizador
        ;;
    "shell-mysql-seguridad")
        docker-compose exec mysql-seguridad mysql -u seguridad_user -pseguridad_pass seguridad
        ;;
    "start-mysql")
        docker-compose up -d mysql
        ;;
    "start-mysql-seguridad")
        docker-compose up -d mysql-seguridad
        ;;
    "start-seguridad")
        docker-compose up -d ms-seguridad
        ;;
    "start-backend")
        docker-compose up -d backend
        ;;
    "start-frontend")
        docker-compose up -d frontend
        ;;
    "help"|"-h"|"--help"|"")
        show_help
        ;;
    *)
        echo -e "${RED}Comando no reconocido: $1${NC}"
        show_help
        exit 1
        ;;
esac 