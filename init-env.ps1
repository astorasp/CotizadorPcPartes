# =================================================================
# SCRIPT DE INICIALIZACIÓN DE VARIABLES DE ENTORNO (PowerShell)
# =================================================================
# Este script configura automáticamente el archivo .env para Docker Compose
# Compatible con Windows PowerShell y PowerShell Core
# Autor: Sistema Cotizador
# Versión: 1.0
# =================================================================

param(
    [switch]$AutoGeneratePasswords = $false,
    [switch]$Force = $false
)

# Configuración de colores para output
$Host.UI.RawUI.ForegroundColor = "White"

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
}

function Write-Header {
    Write-Host ""
    Write-Host "=================================================================" -ForegroundColor Blue
    Write-Host "  🐳 CONFIGURACIÓN DE ENTORNO DOCKER COMPOSE" -ForegroundColor Blue
    Write-Host "  Sistema CotizadorPcPartes" -ForegroundColor Blue
    Write-Host "=================================================================" -ForegroundColor Blue
    Write-Host ""
}

function Generate-Password {
    param([int]$Length = 16)
    
    $characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*'
    $password = ""
    
    for ($i = 0; $i -lt $Length; $i++) {
        $password += $characters[(Get-Random -Maximum $characters.Length)]
    }
    
    return $password
}

function Replace-EnvValue {
    param(
        [string]$Key,
        [string]$Value,
        [string]$FilePath = ".env"
    )
    
    $content = Get-Content $FilePath
    $newContent = $content -replace "^$Key=.*", "$Key=$Value"
    $newContent | Set-Content $FilePath
}

function Test-DockerAvailable {
    try {
        $null = docker --version
        return $true
    }
    catch {
        return $false
    }
}

function Test-DockerComposeAvailable {
    try {
        $null = docker-compose --version
        return $true
    }
    catch {
        try {
            $null = docker compose version
            return $true
        }
        catch {
            return $false
        }
    }
}

function Main {
    Write-Header
    
    # Verificar si estamos en el directorio correcto
    if (-not (Test-Path "docker-compose.yml")) {
        Write-Error "No se encontró docker-compose.yml en el directorio actual"
        Write-Info "Ejecuta este script desde la raíz del proyecto CotizadorPcPartes"
        exit 1
    }
    
    # Verificar si .env.example existe
    if (-not (Test-Path ".env.example")) {
        Write-Error "No se encontró el archivo .env.example"
        exit 1
    }
    
    Write-Info "Verificando configuración de entorno..."
    
    # Si .env ya existe, preguntar si sobrescribir
    if ((Test-Path ".env") -and -not $Force) {
        Write-Warning "El archivo .env ya existe"
        $response = Read-Host "¿Deseas sobrescribirlo? (y/N)"
        if ($response -ne "y" -and $response -ne "Y") {
            Write-Info "Manteniendo archivo .env existente"
            Write-Success "Configuración completada"
            exit 0
        }
    }
    
    Write-Info "Creando archivo .env desde .env.example..."
    Copy-Item ".env.example" ".env"
    
    # Preguntar si generar contraseñas automáticamente (si no se especificó en parámetro)
    $generatePasswords = $AutoGeneratePasswords
    if (-not $AutoGeneratePasswords) {
        Write-Host ""
        Write-Info "¿Deseas generar contraseñas seguras automáticamente?"
        $response = Read-Host "Recomendado para desarrollo/testing (Y/n)"
        $generatePasswords = ($response -eq "Y" -or $response -eq "y" -or $response -eq "")
    }
    
    if ($generatePasswords) {
        Write-Info "Generando contraseñas seguras..."
        
        # Generar contraseñas para bases de datos
        Replace-EnvValue "MYSQL_ROOT_PASSWORD" (Generate-Password 20)
        Replace-EnvValue "MYSQL_COTIZADOR_PASSWORD" (Generate-Password 16)
        Replace-EnvValue "MYSQL_SEGURIDAD_ROOT_PASSWORD" (Generate-Password 20)
        Replace-EnvValue "MYSQL_SEGURIDAD_PASSWORD" (Generate-Password 16)
        Replace-EnvValue "SECURITY_PASSWORD" (Generate-Password 16)
        
        Write-Success "Contraseñas generadas automáticamente"
    }
    else {
        Write-Warning "Recuerda cambiar las contraseñas por defecto en .env antes de usar en producción"
    }
    
    # Verificar herramientas necesarias
    Write-Host ""
    Write-Info "Verificando herramientas necesarias..."
    
    if (-not (Test-DockerAvailable)) {
        Write-Error "Docker no está instalado. Instala Docker Desktop"
        exit 1
    }
    
    if (-not (Test-DockerComposeAvailable)) {
        Write-Error "Docker Compose no está disponible"
        exit 1
    }
    
    Write-Success "Docker y Docker Compose están disponibles"
    
    # Mostrar siguiente pasos
    Write-Host ""
    Write-Success "¡Configuración completada exitosamente!"
    Write-Host ""
    Write-Info "Siguientes pasos:"
    Write-Host "  1. Revisa y ajusta las variables en el archivo .env si es necesario"
    Write-Host "  2. Inicia los servicios con: docker-compose up -d"
    Write-Host "  3. Verifica el estado con: docker-compose ps"
    Write-Host ""
    Write-Info "URLs de los servicios:"
    Write-Host "  🔐 ms-seguridad: http://localhost:8081"
    Write-Host "  🏪 ms-cotizador: http://localhost:8080/cotizador/v1/api"
    Write-Host "  📊 Health checks:"
    Write-Host "     - Seguridad: http://localhost:8091/actuator/health"
    Write-Host "     - Cotizador: http://localhost:8080/cotizador/v1/api/actuator/health"
    Write-Host ""
    Write-Warning "IMPORTANTE: Nunca commitees el archivo .env al repositorio"
}

# Ejecutar función principal
Main