# 🖥️ Sistema Cotizador de PC Partes

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.0-brightgreen.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.4.4-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)

> **Sistema integral para cotización y gestión de componentes de hardware de PC**, desarrollado con arquitectura moderna usando Spring Boot, Vue.js 3, MySQL y sistema de loading centralizado.

## 📋 Tabla de Contenidos

- [🚀 Inicio Rápido](#-inicio-rápido)
- [🏗️ Arquitectura del Sistema](#️-arquitectura-del-sistema)
- [💡 Sistema de Loading Centralizado](#-sistema-de-loading-centralizado)
- [📁 Estructura del Proyecto](#-estructura-del-proyecto)
- [🔧 Configuración y Desarrollo](#-configuración-y-desarrollo)
- [🧪 Testing](#-testing)
- [📚 API Documentation](#-api-documentation)
- [🌐 Portal Web](#-portal-web)
- [🐳 Docker](#-docker)
- [🛠️ Tecnologías](#️-tecnologías)
- [📖 Casos de Uso](#-casos-de-uso)
- [🚀 Próximos Pasos](#-próximos-pasos)

---

## 🚀 Inicio Rápido

### ⚡ **Despliegue con Docker (Recomendado)**

```bash
# 1. Clonar el repositorio
git clone <repository-url>
cd CotizadorPcPartes

# 2. Levantar todo el sistema
docker-compose up -d

# 3. Verificar que los servicios estén funcionando
docker-compose ps
```

### 🌐 **Acceso a los Servicios**

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| **Portal Web** | http://localhost | admin / admin123 |
| **API REST** | http://localhost:8080 | admin / admin123 |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | admin / admin123 |
| **Health Check** | http://localhost:8080/actuator/health | admin / admin123 |

---

## 🏗️ Arquitectura del Sistema

### 🎯 **Componentes Principales**

```
┌─────────────────────────────────────────────────────────────────┐
│                    Sistema Cotizador PC                          │
└─────────────────────────────────────────────────────────────────┘
                                 │
                ┌────────────────┼────────────────┐
                │                │                │
      ┌─────────▼─────────┐ ┌───▼────┐ ┌────────▼────────┐
      │   Frontend Web    │ │Backend │ │   Base de       │
      │   (Vue.js 3)      │ │API REST│ │   Datos MySQL   │
      │   Puerto 80       │ │Puerto  │ │   Puerto 3306   │
      │                   │ │8080    │ │                 │
      └─────────────────────┘ └────────┘ └─────────────────┘
```

### 🔄 **Flujo de Datos**

```
Portal Web (Vue.js 3) → Backend API (Spring Boot) → MySQL Database
      ↓                        ↓                         ↓
- Sistema de Loading    - Domain-Driven Design    - Connection Pooling
- Composables Vue       - CRUD Operations         - Transacciones ACID
- Pinia State Mgmt      - Business Logic          - Índices optimizados
- TailwindCSS          - Security (Basic Auth)    - Esquema normalizado
```

---

## 💡 Sistema de Loading Centralizado

### 🎯 **Características del Sistema**

El sistema cuenta con un **sistema de loading centralizado** que proporciona:

- ✅ **Estados de loading unificados** para todas las operaciones
- ✅ **Feedback visual inmediato** en todas las interacciones
- ✅ **Prevención de doble-click** automática
- ✅ **Componentes reutilizables** (LoadingButton, LoadingSpinner, LoadingOverlay)
- ✅ **Gestión global** de operaciones en progreso
- ✅ **Integración con sistema de permisos**

### 🛠️ **Arquitectura del Loading System**

```
┌─────────────────────────────────────────────────────────────┐
│                   Loading System Architecture                │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
  ┌─────▼─────┐        ┌─────▼─────┐        ┌─────▼─────┐
  │   Core    │        │    UI     │        │  Store    │
  │ Loading   │        │Components │        │Integration│
  │  System   │        │           │        │           │
  └───────────┘        └───────────┘        └───────────┘
        │                     │                     │
┌───────▼───────┐    ┌───────▼───────┐    ┌───────▼───────┐
│useLoadingStore│    │LoadingButton  │    │useCrudOperations│
│useAsyncOp     │    │LoadingSpinner │    │All Stores     │
│               │    │LoadingOverlay │    │Migrated       │
└───────────────┘    └───────────────┘    └───────────────┘
```

### 🔧 **Componentes del Loading System**

#### **Core Loading System**
- **`useLoadingStore.js`**: Store centralizado para gestión de estados
- **`useAsyncOperation.js`**: Composable para operaciones asíncronas
- **`useCrudOperations`**: Helper especializado para operaciones CRUD

#### **UI Components**
- **`LoadingButton.vue`**: Botón con estado de loading integrado
- **`LoadingSpinner.vue`**: Spinner configurable con mensajes
- **`LoadingOverlay.vue`**: Overlay de pantalla completa
- **`GlobalLoadingManager.vue`**: Gestor global de loading

#### **Store Integration**
- **7 Stores migrados** con loading centralizado
- **Estados reactivos** (isFetching, isCreating, isUpdating, isDeleting)
- **Operaciones especializadas** (isAddingComponent, isRemovingComponent)

### 📊 **Estados de Loading por Módulo**

| Módulo | Estados de Loading | Operaciones Especiales |
|--------|-------------------|------------------------|
| **Auth** | isLoggingIn, isLoggingOut | Login con UI bloqueante |
| **Componentes** | isFetching, isCreating, isUpdating, isDeleting | Validación de componentes |
| **Cotizaciones** | isFetching, isCreating, isUpdating, isDeleting | Cálculos de precios |
| **PCs** | isFetching, isCreating, isUpdating, isDeleting | isAddingComponent, isRemovingComponent |
| **Proveedores** | isFetching, isCreating, isUpdating, isDeleting | Búsquedas avanzadas |
| **Pedidos** | isFetching, isGeneratingPedido, isLoadingDetails | Generación desde cotizaciones |
| **Promociones** | isFetching, isCreating, isUpdating, isDeleting | Cálculos de stacking |

---

## 📁 Estructura del Proyecto

```
CotizadorPcPartes/
├── 📁 Cotizador/                    # Backend Spring Boot
│   ├── 📁 src/main/java/mx/com/qtx/cotizador/
│   │   ├── 📁 dominio/              # Domain Layer (DDD)
│   │   │   ├── 📁 core/             # Core Business Logic
│   │   │   │   ├── 📁 componentes/  # Component Domain
│   │   │   │   ├── Cotizacion.java  # Quotation Aggregate
│   │   │   │   └── DetalleCotizacion.java
│   │   │   ├── 📁 cotizadorA/       # Strategy Pattern
│   │   │   ├── 📁 cotizadorB/       # Alternative Strategy
│   │   │   ├── 📁 promos/           # Promotion Domain
│   │   │   ├── 📁 pedidos/          # Order Domain
│   │   │   └── 📁 impuestos/        # Tax Domain
│   │   ├── 📁 aplicacion/           # Application Layer
│   │   │   ├── 📁 servicio/         # Application Services
│   │   │   └── 📁 dto/              # Data Transfer Objects
│   │   ├── 📁 infraestructura/      # Infrastructure Layer
│   │   │   ├── 📁 repositorio/      # Repository Implementations
│   │   │   ├── 📁 controlador/      # REST Controllers
│   │   │   └── 📁 configuracion/    # Spring Configuration
│   │   └── 📁 excepcion/            # Exception Handling
│   ├── 📁 sql/                      # Database Scripts
│   │   ├── ddl.sql                  # Schema Definition
│   │   └── dml.sql                  # Sample Data
│   └── 📁 src/test/                 # Tests (Unit + Integration)
│
├── 📁 portal-cotizador/             # Frontend Vue.js 3
│   ├── index.html                   # Main Application
│   ├── 📁 src/
│   │   ├── 📁 components/           # Vue Components
│   │   │   ├── 📁 ui/               # UI Components
│   │   │   │   ├── LoadingButton.vue
│   │   │   │   ├── LoadingSpinner.vue
│   │   │   │   ├── LoadingOverlay.vue
│   │   │   │   └── GlobalLoadingManager.vue
│   │   │   ├── 📁 componentes/      # Business Components
│   │   │   ├── 📁 cotizaciones/     # Quotation Components
│   │   │   ├── 📁 pcs/              # PC Components
│   │   │   ├── 📁 proveedores/      # Supplier Components
│   │   │   ├── 📁 pedidos/          # Order Components
│   │   │   └── 📁 promociones/      # Promotion Components
│   │   ├── 📁 composables/          # Vue Composables
│   │   │   ├── useAsyncOperation.js # Async Operations
│   │   │   └── usePermissions.js    # Permission Management
│   │   ├── 📁 stores/               # Pinia Stores
│   │   │   ├── useLoadingStore.js   # Central Loading
│   │   │   ├── useAuthStore.js      # Authentication
│   │   │   ├── useComponentesStore.js
│   │   │   ├── useCotizacionesStore.js
│   │   │   ├── usePcsStore.js
│   │   │   ├── useProveedoresStore.js
│   │   │   ├── usePedidosStore.js
│   │   │   └── usePromocionesStore.js
│   │   ├── 📁 views/                # Page Views
│   │   │   ├── LoginView.vue
│   │   │   ├── ComponentesView.vue
│   │   │   ├── CotizacionesView.vue
│   │   │   ├── PcsView.vue
│   │   │   ├── ProveedoresView.vue
│   │   │   ├── PedidosView.vue
│   │   │   └── PromocionesView.vue
│   │   └── 📁 services/             # API Services
│   │       ├── authService.js
│   │       ├── componentesApi.js
│   │       ├── cotizacionesApi.js
│   │       ├── pcsApi.js
│   │       ├── proveedoresApi.js
│   │       ├── pedidosApi.js
│   │       └── promocionesApi.js
│   ├── 📁 public/                   # Static Assets
│   ├── package.json                 # Dependencies
│   └── tailwind.config.js           # TailwindCSS Config
│
├── docker-compose.yml               # Multi-container Setup
├── docker-scripts.sh                # Docker Management Script
├── CLAUDE.md                        # AI Assistant Instructions
└── README.md                        # This file
```

---

## 🔧 Configuración y Desarrollo

### 🛠️ **Desarrollo Local (Sin Docker)**

#### **Prerrequisitos**
- Java 21+ (OpenJDK recomendado)
- Maven 3.8+
- MySQL 8.0+
- Node.js 18+ (para desarrollo frontend)

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

# 3. Configurar variables de entorno
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

# 1. Instalar dependencias
npm install

# 2. Ejecutar en modo desarrollo
npm run dev

# 3. Construir para producción
npm run build
```

### ⚙️ **Configuración de Perfiles**

| Perfil | Archivo | Uso |
|--------|---------|-----|
| `default` | `application.yml` | Desarrollo local |
| `docker` | `application-docker.yml` | Contenedores Docker |
| `test` | `application-test.properties` | Tests de integración |

---

## 🧪 Testing

### 🔬 **Suite de Tests Completa**

```bash
cd Cotizador

# Ejecutar todos los tests
mvn test

# Ejecutar solo tests de integración
mvn test -Dtest="*IntegrationTest"

# Ejecutar tests con reporte de cobertura
mvn test jacoco:report
```

### 🧪 **Tests de Integración con TestContainers**

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
GET    /cotizador/v1/api/componentes         # Listar componentes
POST   /cotizador/v1/api/componentes         # Crear componente
GET    /cotizador/v1/api/componentes/{id}    # Obtener componente
PUT    /cotizador/v1/api/componentes/{id}    # Actualizar componente
DELETE /cotizador/v1/api/componentes/{id}    # Eliminar componente
```

#### **PCs**
```http
GET    /cotizador/v1/api/pcs                 # Listar PCs
POST   /cotizador/v1/api/pcs                 # Crear PC
GET    /cotizador/v1/api/pcs/{id}            # Obtener PC
PUT    /cotizador/v1/api/pcs/{id}            # Actualizar PC
DELETE /cotizador/v1/api/pcs/{id}            # Eliminar PC
POST   /cotizador/v1/api/pcs/{id}/componentes # Agregar componente a PC
```

#### **Cotizaciones**
```http
GET    /cotizador/v1/api/cotizaciones        # Listar cotizaciones
POST   /cotizador/v1/api/cotizaciones        # Crear cotización
GET    /cotizador/v1/api/cotizaciones/{id}   # Obtener cotización
PUT    /cotizador/v1/api/cotizaciones/{id}   # Actualizar cotización
DELETE /cotizador/v1/api/cotizaciones/{id}   # Eliminar cotización
```

#### **Pedidos**
```http
GET    /cotizador/v1/api/pedidos             # Listar pedidos
POST   /cotizador/v1/api/pedidos/generar     # Generar pedido desde cotización
GET    /cotizador/v1/api/pedidos/{id}        # Obtener pedido
```

#### **Proveedores**
```http
GET    /cotizador/v1/api/proveedores         # Listar proveedores
POST   /cotizador/v1/api/proveedores         # Crear proveedor
GET    /cotizador/v1/api/proveedores/{id}    # Obtener proveedor
PUT    /cotizador/v1/api/proveedores/{id}    # Actualizar proveedor
DELETE /cotizador/v1/api/proveedores/{id}    # Eliminar proveedor
```

#### **Promociones**
```http
GET    /cotizador/v1/api/promociones         # Listar promociones
POST   /cotizador/v1/api/promociones         # Crear promoción
GET    /cotizador/v1/api/promociones/{id}    # Obtener promoción
PUT    /cotizador/v1/api/promociones/{id}    # Actualizar promoción
DELETE /cotizador/v1/api/promociones/{id}    # Eliminar promoción
```

### 📖 **Documentación Interactiva**

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

---

## 🌐 Portal Web

### 🎯 **Características Principales**

El **Portal Web** es una aplicación SPA (Single Page Application) desarrollada con **Vue.js 3** que ofrece:

- ✅ **Autenticación integrada** con el backend
- ✅ **Sistema de permisos** basado en roles
- ✅ **Loading states** centralizados en toda la aplicación
- ✅ **Diseño responsivo** con TailwindCSS
- ✅ **Interfaz moderna** con componentes reutilizables

### 🧭 **Módulos del Portal**

| Módulo | Descripción | Funcionalidades |
|--------|-------------|-----------------|
| **🔐 Autenticación** | Login/logout | Gestión de sesiones, roles |
| **🔧 Componentes** | Gestión de hardware | CRUD, filtros, búsqueda, validación |
| **🖥️ Armado PCs** | Configuración de PCs | Crear PCs, gestionar componentes |
| **📋 Cotizaciones** | Gestión de cotizaciones | CRUD, aplicar promociones, exportar |
| **🏢 Proveedores** | Gestión de proveedores | CRUD, asignación componentes |
| **📦 Pedidos** | Gestión de pedidos | Generar desde cotizaciones, seguimiento |
| **🎁 Promociones** | Gestión de promociones | CRUD, aplicar a componentes, stacking |

### 🎨 **Características UX/UI**

#### **Sistema de Loading Centralizado**
- **LoadingButton**: Botones con estado de loading integrado
- **LoadingSpinner**: Indicadores de carga configurables
- **LoadingOverlay**: Overlays de pantalla completa
- **GlobalLoadingManager**: Gestor global de operaciones

#### **Diseño Responsivo**
- **Mobile-first design** con TailwindCSS
- **Navegación adaptativa** según dispositivo
- **Tablas responsivas** con scroll horizontal
- **Formularios optimizados** para touch

#### **Elementos Visuales**
- **Alertas contextuales**: Éxito, error, advertencia
- **Estados vacíos** con call-to-action
- **Confirmaciones** para acciones destructivas
- **Validación en tiempo real** en formularios

### 🔧 **Tecnologías Frontend**

- **Vue.js 3** - Framework principal
- **Composition API** - Lógica de componentes
- **Pinia** - Gestión de estado
- **Vue Router** - Navegación SPA
- **TailwindCSS** - Framework CSS
- **Vite** - Build tool y dev server

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
./docker-scripts.sh health     # Check health endpoints
```

### 🔍 **Health Checks**

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
- **Spring Security** - Autenticación (Basic Auth)
- **Spring Boot Actuator** - Monitoreo y métricas
- **Hibernate** - ORM
- **HikariCP** - Connection pooling
- **Maven** - Gestión de dependencias

### 🌐 **Frontend**
- **Vue.js 3** - Framework principal
- **Composition API** - Lógica de componentes reactiva
- **Pinia** - Gestión de estado moderna
- **Vue Router** - Navegación SPA
- **TailwindCSS** - Framework CSS utility-first
- **Vite** - Build tool y dev server rápido

### 🗄️ **Base de Datos**
- **MySQL 8.4.4** - Base de datos principal
- **HikariCP** - Connection pooling
- **JPA/Hibernate** - ORM

### 🐳 **DevOps & Testing**
- **Docker & Docker Compose** - Containerización
- **TestContainers** - Tests de integración
- **JUnit 5** - Framework de testing
- **Mockito** - Mocking
- **REST Assured** - Testing de APIs
- **Swagger/OpenAPI 3** - Documentación de API

---

## 📖 Casos de Uso

### 🎯 **Flujo Principal del Sistema**

#### **1. Gestión de Componentes**
- Registrar componentes de hardware (CPU, GPU, RAM, etc.)
- Configurar precios y promociones
- Gestionar inventario por proveedor
- Aplicar validaciones de negocio

#### **2. Armado de PCs**
- Crear configuraciones de PC personalizadas
- Validar compatibilidad de componentes
- Calcular precios totales automáticamente
- Gestionar cantidades y variaciones

#### **3. Cotizaciones**
- Generar cotizaciones para clientes
- Aplicar promociones y descuentos
- Gestionar múltiples proveedores
- Exportar cotizaciones (futuro)

#### **4. Gestión de Pedidos**
- Convertir cotizaciones en pedidos
- Asignar proveedores específicos
- Configurar fechas de entrega
- Seguimiento de entregas

#### **5. Administración**
- Gestionar proveedores y catálogos
- Configurar promociones por temporada
- Sistema de permisos basado en roles
- Reportes y análisis

### 🔄 **Patrones de Diseño Implementados**

#### **Backend (Domain-Driven Design)**
- **Domain Layer**: Entidades de negocio con lógica rica
- **Application Layer**: Servicios de aplicación y casos de uso
- **Infrastructure Layer**: Repositorios y adaptadores
- **Strategy Pattern**: Diferentes algoritmos de cotización
- **Builder Pattern**: Construcción de PCs complejas
- **Decorator Pattern**: Aplicación de promociones

#### **Frontend (Modern Vue.js)**
- **Composition API**: Lógica reactiva reutilizable
- **Composables**: Funcionalidades compartidas
- **Store Pattern**: Gestión de estado con Pinia
- **Observer Pattern**: Reactividad de Vue
- **Factory Pattern**: Creación de componentes

---

## 🚀 Próximos Pasos

### 📋 **Roadmap de Desarrollo**

#### **🔧 Mejoras Técnicas**
- [ ] **Autenticación JWT** - Reemplazar Basic Auth
- [ ] **Microservicios** - Separar dominio en servicios
- [ ] **Cache Redis** - Mejorar performance de consultas
- [ ] **WebSockets** - Actualizaciones en tiempo real
- [ ] **GraphQL** - API más eficiente para frontend

#### **🌟 Nuevas Funcionalidades**
- [ ] **Exportación PDF** - Cotizaciones y pedidos
- [ ] **Sistema de Notificaciones** - Email y push notifications
- [ ] **Dashboard Analytics** - Reportes y métricas
- [ ] **Inventario Avanzado** - Stock y reposición
- [ ] **API Mobile** - Aplicación móvil

#### **🔒 Seguridad y Monitoreo**
- [ ] **Audit Logging** - Trazabilidad completa
- [ ] **Rate Limiting** - Control de tráfico
- [ ] **Monitoring** - Prometheus + Grafana
- [ ] **Security Headers** - Protección adicional
- [ ] **API Versioning** - Versionado de endpoints

#### **⚡ Performance y Escalabilidad**
- [ ] **Database Optimization** - Índices y queries
- [ ] **CDN Integration** - Assets estáticos
- [ ] **Load Balancing** - Múltiples instancias
- [ ] **Caching Strategy** - Multi-nivel
- [ ] **Async Processing** - Operaciones pesadas

#### **🧪 Testing y Calidad**
- [ ] **E2E Testing** - Pruebas de extremo a extremo
- [ ] **Performance Testing** - Pruebas de carga
- [ ] **Security Testing** - Análisis de vulnerabilidades
- [ ] **Code Quality** - SonarQube integration
- [ ] **CI/CD Pipeline** - GitHub Actions

### 🤝 **Contribuciones**

1. Fork el repositorio
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

### 📝 **Convenciones de Código**

#### **Backend (Java)**
- **Naming**: CamelCase para clases, camelCase para métodos
- **Packages**: Organización por dominio
- **Tests**: Nombre descriptivo con patrón Given-When-Then
- **Documentation**: JavaDoc para métodos públicos

#### **Frontend (Vue.js)**
- **Naming**: PascalCase para componentes, camelCase para props
- **Structure**: Composables en carpeta dedicada
- **Styles**: TailwindCSS utility classes
- **Tests**: Vue Testing Library para componentes

---

## 📞 Soporte y Documentación

### 🔗 **Enlaces Útiles**

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Portal Web**: http://localhost
- **GitHub Issues**: Para reportar bugs y solicitar features
- **CLAUDE.md**: Instrucciones para IA assistant

### 🆘 **Solución de Problemas**

#### **Errores Comunes**

| Problema | Causa | Solución |
|----------|-------|----------|
| **Portal no carga** | Backend no disponible | `docker-compose ps` |
| **Error 401** | Autenticación | Verificar credenciales |
| **Base de datos vacía** | Scripts no ejecutados | Verificar DDL/DML |
| **Loading infinito** | Error en API | Verificar logs backend |

#### **Comandos de Diagnóstico**

```bash
# Verificar servicios
docker-compose ps
docker-compose logs backend
docker-compose logs frontend

# Health checks
curl http://localhost:8080/actuator/health
curl http://localhost/

# Reiniciar servicios
docker-compose restart
```

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

---

<div align="center">

**🖥️ Sistema Cotizador de PC Partes**

*Desarrollado con ❤️ usando Spring Boot, Vue.js 3 y tecnologías modernas*

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.0-brightgreen.svg)](https://vuejs.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)

</div>