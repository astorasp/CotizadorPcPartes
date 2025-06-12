# 🖥️ Sistema Cotizador de PC Partes

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.4.4-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)

> **Sistema integral para cotización y gestión de componentes de hardware de PC**, desarrollado con arquitectura de microservicios usando Spring Boot, MySQL y frontend web moderno.

## 📋 Tabla de Contenidos

- [🏗️ Arquitectura del Sistema](#️-arquitectura-del-sistema)
- [🚀 Inicio Rápido](#-inicio-rápido)
- [📁 Estructura del Proyecto](#-estructura-del-proyecto)
- [🔧 Configuración y Desarrollo](#-configuración-y-desarrollo)
- [🧪 Testing](#-testing)
- [📚 API Documentation](#-api-documentation)
- [🐳 Docker](#-docker)
- [🛠️ Tecnologías](#️-tecnologías)
- [📖 Casos de Uso](#-casos-de-uso)
- [🌐 Guía del Portal Web](#-guía-del-portal-web)

---

## 🏗️ Arquitectura del Sistema

El sistema está compuesto por **3 componentes principales** que trabajan en conjunto:

### 🎯 **Componentes del Sistema**

| Componente | Tecnología | Puerto | Descripción |
|------------|------------|--------|-------------|
| **Frontend** | HTML5 + CSS3 + JavaScript | 80 | Portal web para gestión de cotizaciones |
| **Backend** | Spring Boot 3.5.0 + Java 21 | 8080 | API REST con lógica de negocio |
| **Base de Datos** | MySQL 8.4.4 | 3306 | Almacenamiento persistente |

### 🔄 **Flujo de Datos**

```
Frontend (Puerto 80) → Backend API (Puerto 8080) → MySQL (Puerto 3306)
```

---

## 🚀 Inicio Rápido

### ⚡ **Opción 1: Docker Compose (Recomendado)**

```bash
# 1. Clonar el repositorio
git clone <repository-url>
cd CotizadorPcPartes

# 2. Levantar todo el sistema
docker-compose up -d

# 3. Verificar que los servicios estén funcionando
docker-compose ps
```

### ⚡ **Opción 2: Script Automatizado**

```bash
# Usar el script de gestión Docker
./docker-scripts.sh start
```

### 🌐 **Acceso a los Servicios**

Una vez levantado el sistema:

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| **Portal Web** | http://localhost | - |
| **API REST** | http://localhost:8080 | admin / admin123 |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | admin / admin123 |
| **Health Check** | http://localhost:8080/actuator/health | admin / admin123 |
| **MySQL** | localhost:3306 | cotizador_user / cotizador_pass |

---

## 📁 Estructura del Proyecto

```
CotizadorPcPartes/
├── 📁 Cotizador/                    # Backend Spring Boot
│   ├── 📁 src/main/java/mx/com/qtx/cotizador/
│   │   ├── 📁 controlador/          # REST Controllers
│   │   ├── 📁 servicio/             # Business Logic Services
│   │   ├── 📁 repositorio/          # Data Access Layer
│   │   ├── 📁 entidad/              # JPA Entities
│   │   ├── 📁 dto/                  # Data Transfer Objects
│   │   ├── 📁 configuracion/        # Spring Configuration
│   │   ├── 📁 excepcion/            # Exception Handling
│   │   └── 📁 util/                 # Utilities & Helpers
│   ├── 📁 src/main/resources/
│   │   ├── application.yml          # Spring Boot Configuration
│   │   └── application-docker.yml   # Docker Profile
│   ├── 📁 src/test/                 # Tests (Unit + Integration)
│   ├── 📁 sql/                      # Database Scripts
│   │   ├── ddl.sql                  # Schema Definition
│   │   └── dml.sql                  # Sample Data
│   ├── Dockerfile                   # Backend Container
│   └── pom.xml                      # Maven Dependencies
│
├── 📁 portal-cotizador/             # Frontend Web Portal
│   ├── index.html                   # Main Application
│   ├── styles.css                   # Styling
│   ├── 📁 js/                       # JavaScript Modules
│   ├── Dockerfile                   # Frontend Container
│   └── Documentacion-Endpoints.md   # API Integration Guide
│
├── docker-compose.yml               # Multi-container Setup
├── docker-scripts.sh                # Docker Management Script
├── README-Docker.md                 # Docker Documentation
└── README.md                        # This file
```

---

## 🔧 Configuración y Desarrollo

### 🛠️ **Desarrollo Local (Sin Docker)**

#### **Prerrequisitos**
- Java 21+ (OpenJDK recomendado)
- Maven 3.8+
- MySQL 8.0+
- Node.js 18+ (opcional, para desarrollo frontend)

#### **Backend Setup**

```bash
cd Cotizador

# 1. Configurar base de datos MySQL
mysql -u root -p
CREATE DATABASE cotizador;
CREATE USER 'cotizador_user'@'localhost' IDENTIFIED BY 'cotizador_pass';
GRANT ALL PRIVILEGES ON cotizador.* TO 'cotizador_user'@'localhost';

# 2. Ejecutar scripts de base de datos
mysql -u cotizador_user -p cotizador < sql/ddl.sql
mysql -u cotizador_user -p cotizador < sql/dml.sql

# 3. Configurar variables de entorno (opcional)
export DB_HOST=localhost
export DB_USERNAME=cotizador_user
export DB_PASSWORD=cotizador_pass
export SECURITY_USERNAME=admin
export SECURITY_PASSWORD=admin123

# 4. Compilar y ejecutar
mvn clean install
mvn spring-boot:run
```

#### **Frontend Setup**

```bash
cd portal-cotizador

# Servir archivos estáticos (opción 1: Python)
python3 -m http.server 8000

# Servir archivos estáticos (opción 2: Node.js)
npx http-server -p 8000

# Acceder en: http://localhost:8000
```

### ⚙️ **Configuración de Perfiles**

| Perfil | Archivo | Uso |
|--------|---------|-----|
| `default` | `application.yml` | Desarrollo local |
| `docker` | `application-docker.yml` | Contenedores Docker |
| `test` | `application-test.yml` | Tests de integración |

---

## 🧪 Testing

### 🔬 **Suite de Tests Completa**

El proyecto incluye **tests unitarios** y **tests de integración** con cobertura completa:

```bash
cd Cotizador

# Ejecutar todos los tests
mvn test

# Ejecutar solo tests de integración
mvn test -Dtest="*IntegrationTest"

# Ejecutar tests con reporte de cobertura
mvn test jacoco:report
```

### 🧪 **Tests de Integración**

Los tests de integración usan **TestContainers** con MySQL real:

| Test Suite | Cobertura | Estado |
|------------|-----------|--------|
| `ComponenteIntegrationTest` | CRUD Componentes | ✅ |
| `ProveedorIntegrationTest` | CRUD Proveedores | ✅ |
| `PromocionIntegrationTest` | CRUD Promociones | ✅ |
| `PcIntegrationTest` | Armado de PCs | ✅ |
| `CotizacionIntegrationTest` | Cotizaciones | ✅ |
| `PedidoIntegrationTest` | Gestión de Pedidos | ✅ |

### 📊 **Arquitectura de Testing**

- **Base Compartida**: `BaseIntegrationTest` con MySQL compartido
- **Datos Consistentes**: Scripts DDL/DML precargados
- **Autenticación**: Basic Auth automática (test/test123)
- **Aislamiento**: Cada test es independiente
- **Performance**: Contenedor MySQL reutilizado

---

## 📚 API Documentation

### 🔗 **Endpoints Principales**

#### **Componentes**
```http
GET    /componentes              # Listar componentes
POST   /componentes              # Crear componente
GET    /componentes/{id}         # Obtener componente
PUT    /componentes/{id}         # Actualizar componente
DELETE /componentes/{id}         # Eliminar componente
```

#### **PCs**
```http
GET    /pcs                      # Listar PCs
POST   /pcs                      # Crear PC
GET    /pcs/{id}                 # Obtener PC
PUT    /pcs/{id}                 # Actualizar PC
DELETE /pcs/{id}                 # Eliminar PC
```

#### **Cotizaciones**
```http
GET    /cotizaciones             # Listar cotizaciones
POST   /cotizaciones             # Crear cotización
GET    /cotizaciones/{id}        # Obtener cotización
PUT    /cotizaciones/{id}        # Actualizar cotización
DELETE /cotizaciones/{id}        # Eliminar cotización
```

#### **Pedidos**
```http
GET    /pedidos                  # Listar pedidos
POST   /pedidos/generar          # Generar pedido desde cotización
GET    /pedidos/{id}             # Obtener pedido
```

#### **Proveedores**
```http
GET    /proveedores              # Listar proveedores
POST   /proveedores              # Crear proveedor
GET    /proveedores/{id}         # Obtener proveedor
PUT    /proveedores/{id}         # Actualizar proveedor
DELETE /proveedores/{id}         # Eliminar proveedor
```

#### **Promociones**
```http
GET    /promociones              # Listar promociones
POST   /promociones              # Crear promoción
GET    /promociones/{id}         # Obtener promoción
PUT    /promociones/{id}         # Actualizar promoción
DELETE /promociones/{id}         # Eliminar promoción
```

### 📖 **Documentación Interactiva**

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Documentación Detallada**: `portal-cotizador/Documentacion-Endpoints.md`

---

## 🐳 Docker

### 🐋 **Gestión con Docker Compose**

```bash
# Levantar todos los servicios
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f

# Ver estado de servicios
docker-compose ps

# Parar servicios
docker-compose down

# Reconstruir imágenes
docker-compose build --no-cache

# Limpiar todo (incluyendo volúmenes)
docker-compose down -v --remove-orphans
```

### 🛠️ **Script de Gestión Avanzada**

```bash
# Usar el script de gestión
./docker-scripts.sh

# Comandos disponibles:
./docker-scripts.sh start      # Iniciar sistema
./docker-scripts.sh stop       # Parar sistema
./docker-scripts.sh restart    # Reiniciar sistema
./docker-scripts.sh logs       # Ver logs
./docker-scripts.sh status     # Ver estado
./docker-scripts.sh clean      # Limpiar sistema
```

### 🔍 **Health Checks**

Todos los servicios incluyen health checks automáticos:

```bash
# Verificar salud de servicios
curl http://localhost:8080/actuator/health
curl http://localhost/
docker-compose ps
```

---

## 🛠️ Tecnologías

### 🖥️ **Backend**
- **Java 21** - Lenguaje de programación
- **Spring Boot 3.5.0** - Framework principal
- **Spring Data JPA** - Acceso a datos
- **Spring Security** - Autenticación y autorización
- **Spring Boot Actuator** - Monitoreo y métricas
- **Hibernate** - ORM
- **MapStruct** - Mapeo de objetos
- **Lombok** - Reducción de boilerplate
- **Maven** - Gestión de dependencias

### 🗄️ **Base de Datos**
- **MySQL 8.4.4** - Base de datos principal
- **HikariCP** - Connection pooling
- **Flyway** - Migraciones (futuro)

### 🌐 **Frontend**
- **HTML5** - Estructura
- **CSS3** - Estilos y diseño responsivo
- **JavaScript ES6+** - Lógica del cliente
- **Fetch API** - Comunicación con backend

### 🐳 **DevOps & Testing**
- **Docker & Docker Compose** - Containerización
- **TestContainers** - Tests de integración
- **JUnit 5** - Framework de testing
- **Mockito** - Mocking
- **REST Assured** - Testing de APIs
- **Swagger/OpenAPI 3** - Documentación de API

### 🔧 **Herramientas de Desarrollo**
- **Spring Boot DevTools** - Hot reload
- **Spring Boot Configuration Processor** - Autocompletado
- **Maven Surefire** - Ejecución de tests
- **Jacoco** - Cobertura de código

---

## 📖 Casos de Uso

### 🎯 **Flujo Principal del Sistema**

1. **Gestión de Componentes**
   - Registrar componentes de hardware (CPU, GPU, RAM, etc.)
   - Asignar promociones y precios
   - Gestionar inventario por proveedor

2. **Armado de PCs**
   - Crear configuraciones de PC personalizadas
   - Validar compatibilidad de componentes
   - Calcular precios totales

3. **Cotizaciones**
   - Generar cotizaciones para clientes
   - Aplicar promociones y descuentos
   - Gestionar múltiples proveedores

4. **Gestión de Pedidos**
   - Convertir cotizaciones en pedidos
   - Asignar proveedores específicos
   - Seguimiento de entregas

5. **Administración**
   - Gestionar proveedores y sus catálogos
   - Configurar promociones por temporada
   - Reportes y análisis

### 🔄 **Patrones de Diseño Implementados**

- **MVC (Model-View-Controller)** - Separación de responsabilidades
- **Repository Pattern** - Abstracción de acceso a datos
- **Service Layer** - Lógica de negocio centralizada
- **DTO Pattern** - Transferencia de datos
- **Builder Pattern** - Construcción de objetos complejos
- **Strategy Pattern** - Cálculo de promociones
- **Factory Pattern** - Creación de entidades

---

## 🌐 Guía del Portal Web

El **Portal Web Cotizador** es la interfaz principal para usuarios finales que consume todos los servicios del backend. Está desarrollado con **HTML5, CSS3 (TailwindCSS) y JavaScript ES6+** y ofrece una experiencia moderna y responsiva.

### 🎯 **Acceso al Portal**

Una vez que el sistema esté ejecutándose:

```bash
# Acceder al portal web
http://localhost
```

**No requiere autenticación** - El portal maneja automáticamente la autenticación con el backend.

### 🧭 **Navegación Principal**

El portal cuenta con **6 secciones principales** accesibles desde la barra de navegación superior:

| Sección | Descripción | Funcionalidades |
|---------|-------------|-----------------|
| **🔧 Componentes** | Gestión de hardware | CRUD completo, filtros, búsqueda |
| **🖥️ Armado PCs** | Configuración de PCs | Crear PCs, gestionar componentes |
| **📋 Cotizaciones** | Gestión de cotizaciones | Crear, editar, aplicar promociones |
| **🏢 Proveedores** | Gestión de proveedores | CRUD, asignación de componentes |
| **📦 Pedidos** | Gestión de pedidos | Generar desde cotizaciones, seguimiento |
| **🎁 Promociones** | Gestión de promociones | CRUD, aplicar a componentes |

### 📱 **Diseño Responsivo**

- **Desktop**: Navegación completa en barra superior
- **Mobile**: Menú hamburguesa colapsible
- **Tablet**: Adaptación automática de layouts

---

### 🔧 **Sección: Gestión de Componentes**

**Funcionalidades principales:**

#### **📋 Listado de Componentes**
- **Vista de tabla** con información completa
- **Paginación** configurable (10, 20, 30 por página)
- **Estados visuales**: Loading, empty state, error handling

#### **🔍 Filtros y Búsqueda**
- **Búsqueda por texto**: ID, descripción, marca, modelo
- **Filtro por tipo**: Monitor, Disco Duro, Tarjeta de Video, PC
- **Botones**: Aplicar filtros, Limpiar filtros

#### **➕ Crear Componente**
```
Botón "Nuevo Componente" → Modal/Formulario
├── Información básica (ID, descripción, marca, modelo)
├── Precios (costo, precio base)
├── Tipo de componente (dropdown)
├── Campos específicos (capacidad, memoria según tipo)
└── Validación en tiempo real
```

#### **✏️ Editar Componente**
- **Botón "Editar"** en cada fila de la tabla
- **Formulario pre-poblado** con datos existentes
- **Validación** antes de guardar

#### **🗑️ Eliminar Componente**
- **Botón "Eliminar"** con confirmación
- **Validación** de dependencias (si está en uso)

---

### 🖥️ **Sección: Armado de PCs**

**Funcionalidades principales:**

#### **📋 Gestión de PCs Completas**
- **Listado** de PCs configuradas
- **Filtros por rango de precio**: $0-1K, $1K-2K, $2K-5K, $5K+
- **Búsqueda** por nombre o descripción

#### **🔧 Crear PC Personalizada**
```
Proceso de Armado:
├── 1. Información básica de la PC
├── 2. Selección de componentes
│   ├── Monitores disponibles
│   ├── Discos duros disponibles
│   ├── Tarjetas de video disponibles
│   └── Otros componentes
├── 3. Configuración de cantidades
├── 4. Cálculo automático de precios
└── 5. Validación y guardado
```

#### **💰 Cálculo de Precios**
- **Precio total automático** basado en componentes
- **Aplicación de promociones** si aplican
- **Desglose detallado** de costos

---

### 📋 **Sección: Cotizaciones**

**Funcionalidades principales:**

#### **📝 Crear Cotización**
```
Flujo de Cotización:
├── 1. Información del cliente
├── 2. Selección de PCs/Componentes
├── 3. Aplicación de promociones
├── 4. Configuración de descuentos
├── 5. Generación de cotización final
└── 6. Exportar/Imprimir (futuro)
```

#### **📊 Gestión de Cotizaciones**
- **Listado** con estados (Pendiente, Aprobada, Rechazada)
- **Filtros por fecha** y estado
- **Búsqueda** por cliente o número de cotización

#### **🔄 Conversión a Pedido**
- **Botón "Generar Pedido"** desde cotización aprobada
- **Selección de proveedor** para el pedido
- **Configuración de fechas** de entrega

---

### 🏢 **Sección: Proveedores**

**Funcionalidades principales:**

#### **📋 Gestión de Proveedores**
- **CRUD completo** de proveedores
- **Información de contacto** y términos comerciales
- **Estado activo/inactivo**

#### **🔗 Asignación de Componentes**
- **Vincular componentes** a proveedores específicos
- **Gestión de precios** por proveedor
- **Tiempos de entrega** configurables

---

### 📦 **Sección: Pedidos**

**Funcionalidades principales:**

#### **📋 Gestión de Pedidos**
- **Listado** de pedidos con estados
- **Filtros por proveedor** y fecha
- **Seguimiento** de entregas

#### **➕ Generar Pedido**
```
Proceso de Pedido:
├── 1. Seleccionar cotización aprobada
├── 2. Elegir proveedor
├── 3. Configurar fechas (emisión, entrega)
├── 4. Establecer nivel de surtido (%)
├── 5. Generar pedido automáticamente
└── 6. Notificación al proveedor (futuro)
```

---

### 🎁 **Sección: Promociones**

**Funcionalidades principales:**

#### **📋 Gestión de Promociones**
- **CRUD completo** de promociones
- **Configuración de vigencia** (fechas desde/hasta)
- **Tipos de promoción**: Sin descuento, Porcentaje, Monto fijo

#### **🔗 Aplicación a Componentes**
- **Asignación automática** por categoría
- **Aplicación manual** a componentes específicos
- **Cálculo automático** en cotizaciones

---

### 🎨 **Características de UX/UI**

#### **🎯 Elementos Visuales**
- **Alertas contextuales**: Éxito (verde), Error (rojo), Advertencia (amarillo)
- **Loading spinners** durante operaciones
- **Estados vacíos** con call-to-action
- **Confirmaciones** para acciones destructivas

#### **⚡ Interactividad**
- **Validación en tiempo real** en formularios
- **Auto-guardado** en borradores (futuro)
- **Shortcuts de teclado** para acciones comunes
- **Tooltips informativos** en campos complejos

#### **📱 Responsividad**
- **Mobile-first design** con TailwindCSS
- **Navegación adaptativa** según dispositivo
- **Tablas responsivas** con scroll horizontal
- **Formularios optimizados** para touch

---

### 🔧 **Configuración del Portal**

#### **🌐 Configuración de API**
El portal se conecta automáticamente al backend, pero puedes verificar la configuración en:

```javascript
// portal-cotizador/js/config.js
const API_CONFIG = {
    BASE_URL: 'http://localhost:8080',
    ENDPOINTS: {
        COMPONENTES: '/componentes',
        PCS: '/pcs',
        COTIZACIONES: '/cotizaciones',
        // ... otros endpoints
    }
};
```

#### **🔐 Autenticación**
El portal maneja automáticamente la autenticación Basic Auth con el backend:
- **Usuario**: admin
- **Contraseña**: admin123

#### **🎨 Personalización**
- **TailwindCSS**: Framework CSS para estilos
- **Colores personalizados** definidos en configuración
- **Componentes reutilizables** en JavaScript

---

### 🚀 **Flujo de Trabajo Típico**

#### **📋 Escenario: Crear Cotización Completa**

```
1. 🔧 Gestionar Componentes
   ├── Agregar monitores, discos, tarjetas de video
   └── Configurar precios y promociones

2. 🖥️ Armar PC
   ├── Crear configuración de PC personalizada
   ├── Seleccionar componentes necesarios
   └── Validar compatibilidad y precios

3. 🎁 Configurar Promociones
   ├── Crear promociones por temporada
   └── Aplicar a componentes específicos

4. 📋 Generar Cotización
   ├── Seleccionar PC armada
   ├── Aplicar promociones disponibles
   ├── Configurar descuentos adicionales
   └── Generar cotización final

5. 📦 Convertir a Pedido
   ├── Aprobar cotización
   ├── Seleccionar proveedor
   ├── Configurar fechas de entrega
   └── Generar pedido automáticamente
```

---

### 🔍 **Solución de Problemas**

#### **❌ Errores Comunes**

| Problema | Causa | Solución |
|----------|-------|----------|
| **Portal no carga** | Backend no disponible | Verificar `docker-compose ps` |
| **Error 401** | Problema de autenticación | Reiniciar contenedores |
| **Datos no aparecen** | Base de datos vacía | Verificar scripts DDL/DML |
| **Formularios no funcionan** | JavaScript deshabilitado | Habilitar JS en navegador |

#### **🔧 Verificación de Estado**

```bash
# Verificar servicios
curl http://localhost:8080/actuator/health
curl http://localhost/

# Ver logs del portal
docker-compose logs frontend

# Ver logs del backend
docker-compose logs backend
```

---

## 🚀 Próximos Pasos

### 📋 **Roadmap**

- [ ] **Frontend Completo** - Completar todas las vistas del portal
- [ ] **Autenticación JWT** - Reemplazar Basic Auth
- [ ] **Microservicios** - Separar en servicios independientes
- [ ] **Cache Redis** - Mejorar performance
- [ ] **Monitoring** - Prometheus + Grafana
- [ ] **CI/CD Pipeline** - GitHub Actions
- [ ] **API Versioning** - Versionado de endpoints
- [ ] **Rate Limiting** - Control de tráfico
- [ ] **Audit Logging** - Trazabilidad completa

### 🤝 **Contribuciones**

1. Fork el repositorio
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

---

## 📞 Soporte

Para soporte técnico o preguntas:

- **Issues**: Crear issue en GitHub
- **Documentación**: Ver archivos en `/docs`
- **API Docs**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

---

<div align="center">

**🖥️ Sistema Cotizador de PC Partes**

*Desarrollado con ❤️ usando Spring Boot y tecnologías modernas*

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)

</div> 