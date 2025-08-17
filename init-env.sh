#!/bin/bash

# =================================================================
# SCRIPT DE INICIALIZACIÓN DE VARIABLES DE ENTORNO
# =================================================================
# Este script configura automáticamente el archivo .env para Docker Compose
# Autor: Sistema Cotizador
# Versión: 1.0
# =================================================================

set -e  # Salir si cualquier comando falla

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para imprimir mensajes con colores
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_header() {
    echo -e "${BLUE}"
    echo "================================================================="
    echo "  🐳 CONFIGURACIÓN DE ENTORNO DOCKER COMPOSE"
    echo "  Sistema CotizadorPcPartes"
    echo "================================================================="
    echo -e "${NC}"
}

# Función para generar contraseña aleatoria
generate_password() {
    local length=${1:-16}
    openssl rand -base64 $length | tr -d "=+/" | cut -c1-$length 2>/dev/null || \
    head /dev/urandom | tr -dc A-Za-z0-9 | head -c $length 2>/dev/null || \
    date +%s | sha256sum | base64 | head -c $length
}

# Función para reemplazar valor en .env
replace_env_value() {
    local key=$1
    local value=$2
    local file=".env"
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s/^${key}=.*/${key}=${value}/" "$file"
    else
        # Linux
        sed -i "s/^${key}=.*/${key}=${value}/" "$file"
    fi
}

# Función para crear el archivo .env.production del portal Vue.js
create_vue_env_production() {
    local vue_env_example="portal-cotizador/.env.example"
    local vue_env_file="portal-cotizador/.env.production"
    
    # Verificar si el directorio portal-cotizador existe
    if [[ ! -d "portal-cotizador" ]]; then
        print_warning "Directorio portal-cotizador no encontrado, omitiendo creación de .env.production"
        return
    fi
    
    # Verificar si existe el archivo .env.example
    if [[ ! -f "$vue_env_example" ]]; then
        print_warning "No se encontró $vue_env_example, omitiendo creación de .env.production"
        return
    fi
    
    # Si el archivo ya existe, preguntar si sobrescribir
    if [[ -f "$vue_env_file" ]]; then
        print_warning "El archivo $vue_env_file ya existe"
        read -p "¿Deseas sobrescribirlo? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_info "Manteniendo archivo .env.production existente"
            return
        fi
    fi
    
    # Copiar .env.example a .env.production
    cp "$vue_env_example" "$vue_env_file"
    print_success "Archivo .env.production creado desde .env.example en portal-cotizador/"
}

# Función principal
main() {
    print_header
    
    # Verificar si estamos en el directorio correcto
    if [[ ! -f "docker-compose.yml" ]]; then
        print_error "No se encontró docker-compose.yml en el directorio actual"
        print_info "Ejecuta este script desde la raíz del proyecto CotizadorPcPartes"
        exit 1
    fi
    
    # Verificar si .env.example existe
    if [[ ! -f ".env.example" ]]; then
        print_error "No se encontró el archivo .env.example"
        exit 1
    fi
    
    print_info "Verificando configuración de entorno..."
    
    # Si .env ya existe, preguntar si sobrescribir
    if [[ -f ".env" ]]; then
        print_warning "El archivo .env ya existe"
        read -p "¿Deseas sobrescribirlo? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_info "Manteniendo archivo .env existente"
            print_success "Configuración completada"
            exit 0
        fi
    fi
    
    print_info "Creando archivo .env desde .env.example..."
    cp .env.example .env
    
    # Crear archivo .env.production para el portal Vue.js
    print_info "Creando archivo .env.production para el portal Vue.js..."
    create_vue_env_production
    
    # Preguntar si generar contraseñas automáticamente
    echo
    print_info "¿Deseas generar contraseñas seguras automáticamente?"
    read -p "Recomendado para desarrollo/testing (Y/n): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]] || [[ -z $REPLY ]]; then
        print_info "Generando contraseñas seguras..."
        
        # Generar contraseñas para bases de datos
        replace_env_value "MYSQL_ROOT_PASSWORD" "$(generate_password 20)"
        replace_env_value "MYSQL_COTIZADOR_PASSWORD" "$(generate_password 16)"
        replace_env_value "MYSQL_SEGURIDAD_ROOT_PASSWORD" "$(generate_password 20)"
        replace_env_value "MYSQL_SEGURIDAD_PASSWORD" "$(generate_password 16)"
        replace_env_value "SECURITY_PASSWORD" "$(generate_password 16)"
        
        print_success "Contraseñas generadas automáticamente"
    else
        print_warning "Recuerda cambiar las contraseñas por defecto en .env antes de usar en producción"
    fi
    
    # Verificar herramientas necesarias
    echo
    print_info "Verificando herramientas necesarias..."
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker no está instalado. Instala Docker Desktop"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_error "Docker Compose no está disponible"
        exit 1
    fi
    
    print_success "Docker y Docker Compose están disponibles"
    
    # Mostrar siguiente pasos
    echo
    print_success "¡Configuración completada exitosamente!"
    echo
    print_info "Siguientes pasos:"
    echo "  1. Revisa y ajusta las variables en el archivo .env si es necesario"
    echo "  2. Inicia los servicios con: docker-compose up -d"
    echo "  3. Verifica el estado con: docker-compose ps"
    echo
    print_info "URLs de los servicios:"
    echo "  🌐 Portal Web: http://localhost (a través del gateway)"
    echo "  🔐 ms-seguridad: http://localhost/api/seguridad"
    echo "  🏪 ms-cotizador: http://localhost/api/cotizador"
    echo "  📊 Health checks:"
    echo "     - Seguridad: http://localhost/seguridad/actuator/health"
    echo "     - Cotizador: http://localhost/actuator/health"
    echo
    print_warning "IMPORTANTE: Nunca commitees los archivos .env al repositorio"
    print_info "Archivos de configuración creados:"
    echo "  - .env (desde .env.example - configuración Docker Compose)"
    echo "  - portal-cotizador/.env.production (desde .env.example - configuración frontend)"
}

# Ejecutar función principal
main "$@"