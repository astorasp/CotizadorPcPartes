# 🖥️ Sistema Cotizador de PC Partes

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.0-brightgreen.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.4.4-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)

> **Sistema integral para cotización y gestión de componentes de hardware de PC**, desarrollado con arquitectura moderna usando Spring Boot, Vue.js 3, MySQL y sistema de loading centralizado.

## 📋 Tabla de Contenidos

- [⚙️ Variables de Entorno](#️-variables-de-entorno)
- [🚀 Inicio Rápido](#-inicio-rápido)
- [🎯 ¿Qué es el Sistema Cotizador de PC Partes?](#-qué-es-el-sistema-cotizador-de-pc-partes)
- [🏗️ Arquitectura del Sistema](#️-arquitectura-del-sistema)
- [💡 Sistema de Loading Centralizado](#-sistema-de-loading-centralizado)
- [📁 Estructura del Proyecto](#-estructura-del-proyecto)
- [🔧 Configuración y Desarrollo](#-configuración-y-desarrollo)
- [🧪 Testing](#-testing)
- [📚 API Documentation](#-api-documentation)
- [🌐 Portal Web](#-portal-web)
- [🔐 Sistema de Roles y Permisos (RBAC)](#-sistema-de-roles-y-permisos-rbac)
- [🐳 Docker](#-docker)
- [🛠️ Tecnologías](#️-tecnologías)
- [📖 Casos de Uso](#-casos-de-uso)
- [🚀 Próximos Pasos](#-próximos-pasos)

---

## 🎯 ¿Qué es el Sistema Cotizador de PC Partes?

Este proyecto es una solución de software completa diseñada para empresas que venden componentes de hardware y ensamblan computadoras personalizadas. Su objetivo principal es simplificar y automatizar todo el proceso de venta, desde la gestión del inventario hasta la entrega final del pedido, a través de un portal web intuitivo y roles de usuario bien definidos.

### El Flujo de Trabajo: De la Pieza al Pedido

El sistema está organizado en módulos que reflejan un flujo de trabajo real en una tienda de computadoras:

1.  **Módulo de Seguridad:**
    *   Controla el ingreso al sistema. Todo usuario, sin importar su rol, debe **iniciar sesión** para acceder. El sistema garantiza que solo personal autorizado pueda operar y previene que un usuario tenga múltiples sesiones activas simultáneamente.

2.  **Módulo de Gestión de Componentes y Proveedores:**
    *   El ciclo comienza aquí. El **Personal de Inventario** o el **Gerente** se encargan de **registrar, consultar, editar y eliminar proveedores**.
    *   Luego, **registran cada componente de hardware** (CPU, RAM, etc.) en el sistema, asociándolo a un proveedor y definiendo su costo y precio base.

3.  **Módulo de Ensamblaje de PCs:**
    *   Con un catálogo de componentes ya cargado, el **Personal de Inventario** o el **Gerente** pueden **ensamblar PCs virtuales**.
    *   Este módulo permite **crear configuraciones de PC**, seleccionando componentes compatibles. El sistema valida reglas de negocio (ej. no más de dos tarjetas de video) y calcula el costo total de la PC ensamblada.

4.  **Módulo de Gestión de Cotizaciones:**
    *   Aquí es donde entra el **Vendedor**. Cuando un cliente solicita un presupuesto, el vendedor puede **crear una nueva cotización**, ya sea agregando componentes individuales o una PC pre-ensamblada.
    *   Opcionalmente, el **Gerente** o **Vendedor** pueden **aplicar promociones** (creadas previamente en el módulo de promociones) para ofrecer descuentos.
    *   La cotización puede ser **consultada, editada o anulada** según sea necesario.

5.  **Módulo de Gestión de Pedidos:**
    *   Una vez que el cliente aprueba la cotización, el **Vendedor** la convierte en un **pedido formal** con un solo clic.
    *   A partir de este punto, el **Personal de Inventario** puede **consultar los pedidos pendientes** y **actualizar su estado** (ej. "En ensamblaje", "Listo para entrega", "Entregado") a medida que avanza en el proceso de preparación y envío.

6.  **Módulos de Soporte y Administración:**
    *   **Gestión de Promociones:** El **Gerente** puede **crear, editar o eliminar promociones** que estarán disponibles para ser aplicadas en las cotizaciones.
    *   **Consultas y Reportes:** Todos los roles tienen la capacidad de **consultar información** relevante para su trabajo. El rol de **Consultor** está específicamente limitado a esta función de solo lectura en todo el sistema.
    *   **Gestión de Usuarios:** Exclusivamente, el **Administrador** puede **gestionar las cuentas de usuario y sus roles**, controlando así quién tiene acceso a qué funcionalidades.

En resumen, este sistema orquesta una serie de casos de uso interconectados que digitalizan y optimizan la operación comercial, proporcionando a cada rol las herramientas precisas que necesita para cumplir con sus responsabilidades.

### Funcionalidades por Rol

Cada rol tiene un conjunto específico de responsabilidades y permisos dentro del sistema, asegurando que los usuarios solo accedan a las herramientas que necesitan para su trabajo.

#### 👑 Administrador (Rol: `ADMIN`)
El rol con control total sobre el sistema. Es el único que puede gestionar la configuración fundamental y los accesos.
*   **Gestión de Usuarios:** Crear, editar y eliminar cuentas de usuario.
*   **Gestión de Roles:** Asignar y modificar los roles de los usuarios.
*   **Acceso Total:** Tiene todos los permisos de los demás roles, lo que le permite supervisar y operar en cualquier módulo del sistema (componentes, cotizaciones, pedidos, etc.).

#### 🏢 Gerente (Rol: `GERENTE`)
Responsable de la estrategia comercial y la supervisión de las operaciones.
*   **Gestión de Promociones:** Crear, modificar y eliminar las ofertas y descuentos que se aplicarán en las cotizaciones.
*   **Gestión de Inventario:** Puede editar componentes y proveedores, pero no crearlos desde cero.
*   **Supervisión de Ventas:** Tiene acceso completo al ciclo de venta, pudiendo crear y anular cotizaciones y pedidos.
*   **Visibilidad Completa:** Puede consultar toda la información del sistema, incluyendo costos y reportes financieros.

#### 💼 Vendedor (Rol: `VENDEDOR`)
El rol enfocado en el proceso de venta directa con el cliente.
*   **Creación de Cotizaciones:** Es su función principal. Puede crear cotizaciones, añadirles productos y aplicar promociones existentes.
*   **Conversión a Pedido:** Convierte una cotización aprobada por el cliente en un pedido formal.
*   **Consulta:** Puede consultar el catálogo de componentes, PCs y proveedores para asistir al cliente, pero no puede modificarlos.
*   **Seguimiento:** Puede ver el estado de sus propias cotizaciones y pedidos.

#### 📦 Personal de Inventario (Rol: `INVENTARIO`)
Responsable de la gestión física y digital del catálogo de productos.
*   **Gestión de Catálogo:** Puede crear, consultar y editar tanto componentes como PCs ensambladas.
*   **Gestión de Proveedores:** Registra y mantiene actualizada la información de los proveedores.
*   **Gestión de Pedidos:** Consulta los pedidos generados por los vendedores y actualiza su estado a medida que los prepara y despacha.
*   **Acceso Limitado a Ventas:** No puede crear ni modificar cotizaciones.

#### 📊 Consultor (Rol: `CONSULTOR`)
Un rol de solo lectura, diseñado para análisis y auditoría sin riesgo de modificar datos.
*   **Consulta Total:** Puede ver toda la información del sistema: componentes, PCs, cotizaciones, pedidos, proveedores y promociones.
*   **Sin Modificación:** No puede realizar ninguna acción de creación, edición o eliminación en ningún módulo.
---

### Matriz de Permisos por Funcionalidad

La siguiente tabla resume las capacidades clave de cada rol dentro del sistema.

| Funcionalidad | 👑 Admin | 🏢 Gerente | 💼 Vendedor | 📦 Inventario | 📊 Consultor |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **GESTIÓN DE USUARIOS** | | | | | |
| Crear / Editar / Eliminar Usuarios | ✅ | ❌ | ❌ | ❌ | ❌ |
| **GESTIÓN DE PROVEEDORES** | | | | | |
| Crear / Editar Proveedores | ✅ | ✅ | ❌ | ✅ | ❌ |
| Eliminar Proveedores | ✅ | ❌ | ❌ | ❌ | ❌ |
| Consultar Proveedores | ✅ | ✅ | ✅ | ✅ | ✅ |
| **GESTIÓN DE COMPONENTES** | | | | | |
| Crear / Editar Componentes | ✅ | ✅ | ❌ | ✅ | ❌ |
| Eliminar Componentes | ✅ | ❌ | ❌ | ❌ | ❌ |
| Consultar Componentes | ✅ | ✅ | ✅ | ✅ | ✅ |
| **GESTIÓN DE PCs ENSAMBLADAS** | | | | | |
| Ensamblar / Modificar PCs | ✅ | ✅ | ❌ | ✅ | ❌ |
| Descontinuar PCs | ✅ | ✅ | ❌ | ❌ | ❌ |
| Consultar PCs | ✅ | ✅ | ✅ | ✅ | ✅ |
| **GESTIÓN DE PROMOCIONES** | | | | | |
| Crear / Editar / Eliminar Promociones | ✅ | ✅ | ❌ | ❌ | ❌ |
| Consultar Promociones | ✅ | ✅ | ✅ | ✅ | ✅ |
| **GESTIÓN DE COTIZACIONES** | | | | | |
| Crear / Editar Cotizaciones | ✅ | ✅ | ✅ | ❌ | ❌ |
| Anular Cotizaciones | ✅ | ✅ | ❌ | ❌ | ❌ |
| Aplicar Promociones | ✅ | ✅ | ✅ | ❌ | ❌ |
| Consultar Cotizaciones | ✅ | ✅ | ✅ | ✅ | ✅ |
| **GESTIÓN DE PEDIDOS** | | | | | |
| Generar Pedido desde Cotización | ✅ | ✅ | ✅ | ❌ | ❌ |
| Actualizar Estado de Pedido | ✅ | ✅ | ❌ | ✅ | ❌ |
| Consultar Pedidos | ✅ | ✅ | ✅ | ✅ | ✅ |

---

### 🌐 **Acceso a los Servicios (Microservicios)**

| Servicio | URL Base (dev) | Swagger (dev) | Health (dev) |
|----------|-----------------|---------------|--------------|
| **Portal Web** | http://localhost | - | http://localhost/health |
| **Componentes/PCs/Promociones** | http://localhost:8082/api/v1 | http://localhost:8082/api/v1/swagger-ui/index.html | http://localhost:8082/api/v1/actuator/health |
| **Cotizaciones** | http://localhost:8083/api/v1 | http://localhost:8083/api/v1/swagger-ui/index.html | http://localhost:8083/api/v1/actuator/health |
| **Pedidos/Proveedores** | http://localhost:8084/api/v1 | http://localhost:8084/api/v1/swagger-ui/index.html | http://localhost:8084/api/v1/actuator/health |

### 🔐 **Sistema de Autenticación**

#### **🚪 Login del Portal (Usuarios Finales)**
| Usuario | Password | Rol | Descripción |
|---------|----------|-----|-------------|
| **admin** | password | ADMIN | Acceso completo al sistema |
| **gerente** | password | GERENTE | Gestión comercial y supervisión |
| **vendedor** | password | VENDEDOR | Ventas y cotizaciones |
| **inventario** | password | INVENTARIO | Gestión de inventario |
| **consultor** | password | CONSULTOR | Solo consulta y reportes |

#### **🔑 Autenticación con JWT**
1. **Login**: POST `/api/seguridad/auth/login` con credenciales
2. **Respuesta**: `accessToken` y `refreshToken`
3. **Uso**: Header `Authorization: Bearer {accessToken}`
4. **Renovación**: POST `/api/seguridad/auth/refresh` con `refreshToken`

#### **🔧 Desarrollo y Testing**
- **Tests**: Basic Auth (`test/test123`) configurado en `application-test.properties`
- **Desarrollo local**: Basic Auth (`admin/admin123`) solo si ms-seguridad no está disponible
- **Base de Datos**: `cotizador_user / cotizador_pass`

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

### 🔄 **Flujo de Datos (Microservicios)**

```
Portal Web (Vue.js 3)
  → API Gateway (Nginx)
    → ms-cotizador-componentes (8082)  ─┐
    → ms-cotizador-cotizaciones (8083) ─┼→ MySQLs por microservicio
    → ms-cotizador-pedidos (8084)      ─┘    (cotizador_componentes_db, cotizador_cotizaciones_db, cotizador_pedidos_db)

CDC (Debezium + Kafka Connect)
  Componentes/Cotizaciones/Pedidos MySQL → Kafka topics → Sinks cruzados
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
CotizadorPcPartes/                   # Arquitectura de Microservicios
├── 📁 ms-seguridad/                 # Microservicio de Autenticación (Puerto 8081)
│   ├── 📁 src/main/java/mx/com/qtx/seguridad/
│   │   ├── 📁 dominio/              # Domain Layer (Simple entities)
│   │   │   ├── Usuario.java         # User Entity
│   │   │   ├── Rol.java             # Role Entity
│   │   │   └── Acceso.java          # Access Control
│   │   ├── 📁 aplicacion/           # Application Layer
│   │   │   ├── 📁 servicio/         # Auth Services
│   │   │   └── 📁 dto/              # DTOs
│   │   ├── 📁 infraestructura/      # Infrastructure Layer
│   │   │   ├── 📁 repositorio/      # JPA Repositories
│   │   │   ├── 📁 controlador/      # REST Controllers
│   │   │   └── 📁 configuracion/    # JWT + Security Config
│   │   └── 📁 seguridad/            # JWT/Session Management
│   ├── 📁 scripts/                  # Database Scripts
│   │   └── seguridad_ddl.sql        # Schema + Sample Users
│   └── 📁 src/test/                 # Tests (Unit + Integration)
│
├── 📁 ms-cotizador-componentes/     # Microservicio de Componentes/PCs/Promociones (Puerto 8082)
│   ├── 📁 src/main/java/mx/com/qtx/cotizador/
│   │   ├── 📁 dominio/              # Domain Layer (DDD)
│   │   │   ├── 📁 core/             # Core Business Logic
│   │   │   │   ├── 📁 componentes/  # Component Domain
│   │   │   │   │   ├── Componente.java      # Component Hierarchy
│   │   │   │   │   ├── DiscoDuro.java       # Hard Drive
│   │   │   │   │   ├── TarjetaVideo.java    # Graphics Card
│   │   │   │   │   ├── Monitor.java         # Monitor
│   │   │   │   │   ├── Pc.java              # PC Composite
│   │   │   │   │   └── PcBuilder.java       # Builder Pattern
│   │   │   │   └── 📁 promos/       # Promotion Domain
│   │   │   │       ├── Promocion.java       # Promotion Base
│   │   │   │       └── PromocionBuilder.js  # Stacking System
│   │   │   ├── 📁 aplicacion/       # Application Layer
│   │   │   ├── 📁 infraestructura/  # Infrastructure Layer
│   │   │   └── 📁 excepcion/        # Exception Handling
│   │   ├── 📁 sql/                  # Database Scripts
│   │   │   ├── ddl.sql              # Componentes Schema
│   │   │   └── dml.sql              # Sample Components
│   │   └── 📁 src/test/             # Tests (Unit + Integration)
│
├── 📁 ms-cotizador-cotizaciones/    # Microservicio de Cotizaciones (Puerto 8083)
│   ├── 📁 src/main/java/mx/com/qtx/cotizador/
│   │   ├── 📁 dominio/              # Domain Layer (DDD)
│   │   │   ├── 📁 core/             # Core Business Logic
│   │   │   │   ├── Cotizacion.java  # Quotation Aggregate
│   │   │   │   └── DetalleCotizacion.java
│   │   │   ├── 📁 cotizadorA/       # Strategy Pattern A
│   │   │   ├── 📁 cotizadorB/       # Strategy Pattern B
│   │   │   └── 📁 impuestos/        # Tax Bridge Pattern
│   │   │   ├── 📁 aplicacion/       # Application Layer
│   │   │   ├── 📁 infraestructura/  # Infrastructure Layer
│   │   │   └── 📁 excepcion/        # Exception Handling
│   │   ├── 📁 sql/                  # Database Scripts
│   │   │   ├── ddl.sql              # Cotizaciones Schema
│   │   │   └── dml.sql              # Sample Quotations
│   │   └── 📁 src/test/             # Tests (Unit + Integration)
│
├── 📁 ms-cotizador-pedidos/         # Microservicio de Pedidos/Proveedores (Puerto 8084)
│   ├── 📁 src/main/java/mx/com/qtx/cotizador/
│   │   ├── 📁 dominio/              # Domain Layer (DDD)
│   │   │   ├── 📁 core/             # Core Business Logic
│   │   │   │   ├── Pedido.java      # Order Aggregate
│   │   │   │   └── Proveedor.java   # Supplier Entity
│   │   │   ├── 📁 pedidos/          # Order Domain Services
│   │   │   │   └── GestorPedidos.java
│   │   │   ├── 📁 aplicacion/       # Application Layer
│   │   │   ├── 📁 infraestructura/  # Infrastructure Layer
│   │   │   └── 📁 excepcion/        # Exception Handling
│   │   ├── 📁 sql/                  # Database Scripts
│   │   │   ├── ddl.sql              # Pedidos Schema
│   │   │   └── dml.sql              # Sample Orders/Suppliers
│   │   └── 📁 src/test/             # Tests (Unit + Integration)
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
├── 📁 kafka-config/                 # CDC (Change Data Capture) Configuration
│   ├── setup-debezium-connectors.sh # Auto-setup CDC connectors
│   ├── validate-cdc-setup.sh        # Validate CDC configuration
│   ├── health-check.sh              # Monitor CDC health
│   ├── monitor-cdc.sh               # CDC monitoring dashboard
│   └── 📁 connectors/               # Debezium connector configs
│       ├── componentes-source.json   # Components CDC source
│       ├── cotizaciones-source.json  # Quotations CDC source
│       └── pedidos-source.json       # Orders CDC source
│
├── 📁 gateway/                      # Nginx API Gateway
│   ├── nginx.conf                   # Gateway configuration
│   └── Dockerfile                   # Gateway container
│
├── docker-compose.yml               # Multi-container Setup (Microservices + Kafka + CDC)
├── docker-scripts.sh                # Docker Management Script
├── init-env.sh                      # Environment setup (Linux/macOS)
├── init-env.ps1                     # Environment setup (Windows)
├── .env.example                     # Environment variables template
├── CLAUDE.md                        # AI Assistant Instructions
└── README.md                        # This file
```

## ⚙️ Variables de Entorno

El sistema utiliza variables de entorno definidas en el archivo `.env` para configurar todos los aspectos del despliegue con Docker Compose. A continuación se documentan todas las variables disponibles:

### 🗄️ Base de Datos MySQL

#### Microservicio Cotizador
| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `MYSQL_ROOT_PASSWORD` | Contraseña del usuario root de MySQL | `vQMMbMs6fViYMNtMr5tJ` |
| `MYSQL_COTIZADOR_DATABASE` | Nombre de la base de datos | `cotizador` |
| `MYSQL_COTIZADOR_USER` | Usuario de la base de datos | `cotizador_user` |
| `MYSQL_COTIZADOR_PASSWORD` | Contraseña del usuario | `VhtM4dMIc0zVSZiI` |

#### Microservicio Seguridad
| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `MYSQL_SEGURIDAD_ROOT_PASSWORD` | Contraseña del usuario root de MySQL | `L5dQjxX6LcbufSshuiBs` |
| `MYSQL_SEGURIDAD_DATABASE` | Nombre de la base de datos | `seguridad` |
| `MYSQL_SEGURIDAD_USER` | Usuario de la base de datos | `seguridad_user` |
| `MYSQL_SEGURIDAD_PASSWORD` | Contraseña del usuario | `05Ssg9zC7BT0Gmsb` |

### 🔐 Configuración de Seguridad

#### Credenciales de Microservicios
| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `SECURITY_USERNAME` | Usuario para autenticación básica | `admin` |
| `SECURITY_PASSWORD` | Contraseña para autenticación básica | `4pwUWIbr3oOFVc2W` |

#### Configuración JWT
| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `JWT_ACCESS_TOKEN_DURATION` | Duración del access token (ms) | `300000` (5 min) |
| `JWT_REFRESH_TOKEN_DURATION` | Duración del refresh token (ms) | `900000` (15 min) |
| `JWT_ISSUER` | Emisor de los tokens JWT | `ms-seguridad` |
| `JWT_CACHE_TIMEOUT_MS` | Timeout del caché JWKS (ms) | `300000` |
| `JWT_JWKS_REFRESH_INTERVAL_MS` | Intervalo de actualización JWKS (ms) | `300000` |
| `JWT_JWKS_MAX_RETRIES` | Máximo número de reintentos | `5` |
| `JWT_JWKS_INITIAL_DELAY_MS` | Delay inicial para JWKS (ms) | `15000` |
| `JWT_MS_SEGURIDAD_TIMEOUT` | Timeout de conexión (ms) | `15000` |
| `JWT_MS_SEGURIDAD_CONNECT_TIMEOUT` | Timeout de establecimiento (ms) | `10000` |

#### Rotación de Llaves JWT
| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `JWT_KEY_ROTATION_ENABLED` | Habilitar rotación reactiva | `true` |
| `JWT_SECURITY_ALERT_THRESHOLD` | Umbral de alertas de seguridad | `3` |
| `JWT_LOG_ROTATION_EVENTS` | Registrar eventos de rotación | `true` |
| `JWT_CLEANUP_OLD_KEYS` | Limpiar llaves antiguas | `true` |

#### Rate Limiting JWKS
| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `JWT_RATE_LIMITING_ENABLED` | Habilitar rate limiting | `true` |
| `JWT_MAX_REQUESTS_PER_MINUTE` | Máximo requests por minuto | `60` |
| `JWT_MAX_REQUESTS_PER_HOUR` | Máximo requests por hora | `1000` |

### 🔄 Configuración de Sesiones

#### Validación de Sesiones
| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `JWT_SESSION_VALIDATION_ENABLED` | Habilitar validación de sesiones | `true` |
| `SESSION_CACHE_TTL_MS` | TTL del caché de sesiones (ms) | `30000` |
| `SESSION_CACHE_MAX_SIZE` | Tamaño máximo del caché | `1000` |
| `SESSION_CACHE_ENABLED` | Habilitar caché de sesiones | `true` |
| `SESSION_VALIDATION_TIMEOUT_MS` | Timeout de validación (ms) | `10000` |
| `SESSION_VALIDATION_MAX_RETRIES` | Máximo número de reintentos | `5` |
| `SESSION_VALIDATION_RETRY_DELAY_MS` | Delay entre reintentos (ms) | `2000` |

#### Limpieza de Sesiones
| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `SESSION_CLEANUP_ENABLED` | Habilitar limpieza automática | `true` |
| `SESSION_CLEANUP_INTERVAL_SECONDS` | Intervalo de limpieza (segundos) | `15` |

### 🌐 Configuración de Red y Puertos

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `COTIZADOR_PORT` | Puerto externo del cotizador | `8080` |
| `SEGURIDAD_PORT` | Puerto externo del seguridad | `8081` |
| `SEGURIDAD_MANAGEMENT_PORT` | Puerto de management | `8091` |
| `MYSQL_COTIZADOR_PORT` | Puerto externo MySQL cotizador | `3306` |
| `MYSQL_SEGURIDAD_PORT` | Puerto externo MySQL seguridad | `3307` |

### ☕ Configuración JVM

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `COTIZADOR_JAVA_OPTS` | Opciones JVM para ms-cotizador | `-Xmx1g -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200` |
| `SEGURIDAD_JAVA_OPTS` | Opciones JVM para ms-seguridad | `-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication` |

### 🔧 Configuración de Desarrollo

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Perfil Spring Boot activo | `docker` |
| `TIMEZONE` | Zona horaria para contenedores | `America/Mazatlan` |
| `LOG_LEVEL_ROOT` | Nivel de log raíz | `INFO` |
| `LOG_LEVEL_SECURITY` | Nivel de log del microservicio de seguridad | `DEBUG` |
| `LOG_LEVEL_COTIZADOR` | Nivel de log del microservicio cotizador | `INFO` |

### 🚨 Notas de Seguridad

> ⚠️ **IMPORTANTE:**
> 1. **NUNCA** commitees el archivo `.env` al repositorio
> 2. Cambia **TODAS** las contraseñas por defecto antes de usar en producción
> 3. Usa contraseñas fuertes (mínimo 12 caracteres, mayúsculas, minúsculas, números, símbolos)
> 4. En producción, considera usar secrets de Docker Swarm o Kubernetes
> 5. Rota las credenciales regularmente

### 📋 Configuración Inicial

1. **Copia el archivo de configuración:**
   ```bash
   cp .env.example .env
   ```

2. **Modifica las variables según tu entorno:**
   - Cambia todas las contraseñas por defecto
   - Ajusta los puertos si hay conflictos
   - Configura los timeouts según tu infraestructura

3. **Para generar contraseñas seguras automáticamente:**
   ```bash
   # Linux/macOS
   ./init-env.sh
   
   # Windows PowerShell
   ./init-env.ps1 -AutoGeneratePasswords
   ```

---

## 🚀 Inicio Rápido

### ⚡ **Despliegue con Docker (Recomendado)**

```bash
# 1. Clonar el repositorio
git clone <repository-url>
cd CotizadorPcPartes

# 2. Inicializar configuración de entorno
# Linux/macOS:
./init-env.sh

# Windows PowerShell:
./init-env.ps1

# 3. Levantar todo el sistema
docker-compose up -d

# 4. Verificar que los servicios estén funcionando
docker-compose ps
```

---

## 🔧 Configuración y Desarrollo

### ⚙️ **Configuración de Entorno (REQUERIDO)**

Antes de ejecutar el sistema, **debes inicializar** los archivos de configuración:

#### **🐧 Linux / 🍎 macOS**
```bash
# Ejecutar script de inicialización
./init-env.sh

# Opciones disponibles:
# - Genera automáticamente contraseñas seguras
# - Crea archivos .env y .env.production
# - Verifica dependencias (Docker, Docker Compose)
```

#### **🪟 Windows PowerShell**
```powershell
# Ejecutar script de inicialización
./init-env.ps1

# Opciones avanzadas:
./init-env.ps1 -AutoGeneratePasswords  # Genera contraseñas automáticamente
./init-env.ps1 -Force                  # Sobrescribe archivos existentes
```

#### **📁 Archivos Creados**
- **`.env`**: Configuración Docker Compose (desde `.env.example`)
- **`portal-cotizador/.env.production`**: Configuración frontend Vue.js (desde `portal-cotizador/.env.example`)

⚠️ **IMPORTANTE**: Estos archivos **NO** están en Git por seguridad. Debes ejecutar el script de inicialización en cada nuevo entorno.

---

### 🛠️ **Desarrollo Local (Sin Docker)**

#### **Prerrequisitos**
- Java 21+ (OpenJDK recomendado)
- Maven 3.8+
- MySQL 8.0+ (múltiples bases de datos)
- Node.js 18+ (para desarrollo frontend)

#### **Backend Setup (Microservicios)**

```bash
# 1. Configurar bases de datos MySQL (una por microservicio)
mysql -u root -p

# Base de datos para Seguridad
CREATE DATABASE seguridad;
CREATE USER 'seguridad_user'@'localhost' IDENTIFIED BY 'seguridad_pass';
GRANT ALL PRIVILEGES ON seguridad.* TO 'seguridad_user'@'localhost';

# Base de datos para Componentes
CREATE DATABASE cotizador_componentes_db;
CREATE USER 'componentes_user'@'localhost' IDENTIFIED BY 'componentes_pass';
GRANT ALL PRIVILEGES ON cotizador_componentes_db.* TO 'componentes_user'@'localhost';

# Base de datos para Cotizaciones
CREATE DATABASE cotizador_cotizaciones_db;
CREATE USER 'cotizaciones_user'@'localhost' IDENTIFIED BY 'cotizaciones_pass';
GRANT ALL PRIVILEGES ON cotizador_cotizaciones_db.* TO 'cotizaciones_user'@'localhost';

# Base de datos para Pedidos
CREATE DATABASE cotizador_pedidos_db;
CREATE USER 'pedidos_user'@'localhost' IDENTIFIED BY 'pedidos_pass';
GRANT ALL PRIVILEGES ON cotizador_pedidos_db.* TO 'pedidos_user'@'localhost';

FLUSH PRIVILEGES;

# 2. Ejecutar scripts de base de datos por microservicio
# (cada microservicio tiene sus propios scripts DDL/DML)
mysql -u seguridad_user -p seguridad < ms-seguridad/scripts/seguridad_ddl.sql
mysql -u componentes_user -p cotizador_componentes_db < ms-cotizador-componentes/sql/ddl.sql
mysql -u componentes_user -p cotizador_componentes_db < ms-cotizador-componentes/sql/dml.sql
mysql -u cotizaciones_user -p cotizador_cotizaciones_db < ms-cotizador-cotizaciones/sql/ddl.sql
mysql -u cotizaciones_user -p cotizador_cotizaciones_db < ms-cotizador-cotizaciones/sql/dml.sql
mysql -u pedidos_user -p cotizador_pedidos_db < ms-cotizador-pedidos/sql/ddl.sql
mysql -u pedidos_user -p cotizador_pedidos_db < ms-cotizador-pedidos/sql/dml.sql

# 3. Ejecutar cada microservicio (terminales separadas)

# Terminal 1 - Microservicio de Seguridad (Puerto 8081)
cd ms-seguridad
export DB_HOST=localhost
export MYSQL_SEGURIDAD_USER=seguridad_user
export MYSQL_SEGURIDAD_PASSWORD=seguridad_pass
mvn spring-boot:run

# Terminal 2 - Microservicio de Componentes (Puerto 8082)
cd ms-cotizador-componentes
export DB_HOST=localhost
export DB_USERNAME=componentes_user
export DB_PASSWORD=componentes_pass
export JWT_MS_SEGURIDAD_BASE_URL=http://localhost:8081
mvn spring-boot:run

# Terminal 3 - Microservicio de Cotizaciones (Puerto 8083)
cd ms-cotizador-cotizaciones
export DB_HOST=localhost
export DB_USERNAME=cotizaciones_user
export DB_PASSWORD=cotizaciones_pass
export JWT_MS_SEGURIDAD_BASE_URL=http://localhost:8081
mvn spring-boot:run

# Terminal 4 - Microservicio de Pedidos (Puerto 8084)
cd ms-cotizador-pedidos
export DB_HOST=localhost
export DB_USERNAME=pedidos_user
export DB_PASSWORD=pedidos_pass
export JWT_MS_SEGURIDAD_BASE_URL=http://localhost:8081
mvn spring-boot:run
```

#### **🚀 Script de Desarrollo Automatizado**

```bash
# Crear script para levantar todos los microservicios
# Guardar como: start-dev-services.sh

#!/bin/bash
echo "🚀 Starting all microservices for development..."

# Array de microservicios y sus puertos
declare -A services=(
    ["ms-seguridad"]="8081"
    ["ms-cotizador-componentes"]="8082"
    ["ms-cotizador-cotizaciones"]="8083"
    ["ms-cotizador-pedidos"]="8084"
)

for service in "${!services[@]}"; do
    echo "Starting $service on port ${services[$service]}..."
    cd $service
    mvn spring-boot:run &
    cd ..
    sleep 10  # Wait for service to start
done

echo "✅ All microservices started!"
echo "🌐 Portal will be available at: http://localhost"
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

### 🔬 **Suite de Tests Completa (Microservicios)**

#### **🧪 Testing por Microservicio**

```bash
# 1. Microservicio de Seguridad
cd ms-seguridad
mvn test

# 2. Microservicio de Componentes/PCs/Promociones
cd ms-cotizador-componentes
mvn test

# 3. Microservicio de Cotizaciones
cd ms-cotizador-cotizaciones
mvn test

# 4. Microservicio de Pedidos/Proveedores
cd ms-cotizador-pedidos
mvn test
```

#### **🚀 Ejecutar Todos los Tests (Script Automatizado)**

```bash
# Desde la raíz del proyecto - Ejecutar todos los microservicios
for dir in ms-*; do
  echo "🧪 Testing $dir..."
  cd $dir && mvn test && cd ..
done

# Ejecutar solo tests de integración en todos los microservicios
for dir in ms-*; do
  echo "🔍 Integration Tests in $dir..."
  cd $dir && mvn test -Dtest="*IntegrationTest" && cd ..
done

# Generar reportes de cobertura en todos los microservicios
for dir in ms-*; do
  echo "📊 Coverage Report for $dir..."
  cd $dir && mvn test jacoco:report && cd ..
done
```

### 🧪 **Tests de Integración con TestContainers**

#### **ms-seguridad (Puerto 8081)**
| Test Suite | Cobertura | Estado |
|------------|-----------|--------|
| `AuthIntegrationTest` | Autenticación JWT | ✅ |
| `UserIntegrationTest` | Gestión de Usuarios | ✅ |
| `RoleIntegrationTest` | Gestión de Roles | ✅ |
| `SessionIntegrationTest` | Gestión de Sesiones | ✅ |

#### **ms-cotizador-componentes (Puerto 8082)**
| Test Suite | Cobertura | Estado |
|------------|-----------|--------|
| `ComponenteIntegrationTest` | CRUD Componentes | ✅ |
| `PcIntegrationTest` | Armado de PCs | ✅ |
| `PromocionIntegrationTest` | CRUD Promociones | ✅ |

#### **ms-cotizador-cotizaciones (Puerto 8083)**
| Test Suite | Cobertura | Estado |
|------------|-----------|--------|
| `CotizacionIntegrationTest` | CRUD Cotizaciones | ✅ |
| `DetalleCotizacionIntegrationTest` | Gestión de Detalles | ✅ |
| `ImpuestoIntegrationTest` | Cálculo de Impuestos | ✅ |

#### **ms-cotizador-pedidos (Puerto 8084)**
| Test Suite | Cobertura | Estado |
|------------|-----------|--------|
| `PedidoIntegrationTest` | Gestión de Pedidos | ✅ |
| `ProveedorIntegrationTest` | CRUD Proveedores | ✅ |

### 📊 **Arquitectura de Testing**

#### **Por Microservicio**
- **Base Compartida**: `BaseIntegrationTest` en cada microservicio con MySQL dedicado
- **Datos Consistentes**: Scripts DDL/DML específicos por dominio
- **Autenticación**:
  - **ms-seguridad**: JWT con credenciales de test
  - **Otros microservicios**: Basic Auth (test/test123) o JWT desde ms-seguridad
- **Aislamiento**: Tests independientes por microservicio
- **Performance**: TestContainers MySQL reutilizado por microservicio

#### **Bases de Datos de Test**
| Microservicio | Base de Datos Test | Puerto |
|---------------|-------------------|--------|
| ms-seguridad | seguridad_test | TestContainer |
| ms-cotizador-componentes | cotizador_componentes_test | TestContainer |
| ms-cotizador-cotizaciones | cotizador_cotizaciones_test | TestContainer |
| ms-cotizador-pedidos | cotizador_pedidos_test | TestContainer |

#### **Orden de Ejecución Recomendado**
1. **ms-seguridad** - Tests de autenticación base
2. **ms-cotizador-componentes** - Tests de componentes y PCs
3. **ms-cotizador-cotizaciones** - Tests que dependen de componentes
4. **ms-cotizador-pedidos** - Tests que dependen de cotizaciones y proveedores

---

## 📚 API Documentation

### 🔗 **Endpoints Principales (por microservicio)**

#### **Componentes / PCs / Promociones** (ms-cotizador-componentes · 8082)
```http
GET    /api/v1/componentes                   # Listar componentes
POST   /api/v1/componentes                   # Crear componente
GET    /api/v1/componentes/{id}              # Obtener componente
PUT    /api/v1/componentes/{id}              # Actualizar componente
DELETE /api/v1/componentes/{id}              # Eliminar componente
```

#### **PCs**
```http
GET    /api/v1/pcs                            # Listar PCs
POST   /api/v1/pcs                            # Crear PC
GET    /api/v1/pcs/{id}                       # Obtener PC
PUT    /api/v1/pcs/{id}                       # Actualizar PC
DELETE /api/v1/pcs/{id}                       # Eliminar PC
POST   /api/v1/pcs/{id}/componentes           # Agregar componente a PC
```

#### **Cotizaciones** (ms-cotizador-cotizaciones · 8083)
```http
GET    /api/v1/cotizaciones                  # Listar cotizaciones
POST   /api/v1/cotizaciones                  # Crear cotización
GET    /api/v1/cotizaciones/{id}             # Obtener cotización
PUT    /api/v1/cotizaciones/{id}             # Actualizar cotización
DELETE /api/v1/cotizaciones/{id}             # Eliminar cotización
```

#### **Pedidos** (ms-cotizador-pedidos · 8084)
```http
GET    /api/v1/pedidos                        # Listar pedidos
POST   /api/v1/pedidos/generar                # Generar pedido desde cotización
GET    /api/v1/pedidos/{id}                   # Obtener pedido
```

#### **Proveedores** (ms-cotizador-pedidos · 8084)
```http
GET    /api/v1/proveedores                    # Listar proveedores
POST   /api/v1/proveedores                    # Crear proveedor
GET    /api/v1/proveedores/{id}               # Obtener proveedor
PUT    /api/v1/proveedores/{id}               # Actualizar proveedor
DELETE /api/v1/proveedores/{id}               # Eliminar proveedor
```

#### **Promociones** (ms-cotizador-componentes · 8082)
```http
GET    /api/v1/promociones                    # Listar promociones
POST   /api/v1/promociones                    # Crear promoción
GET    /api/v1/promociones/{id}               # Obtener promoción
PUT    /api/v1/promociones/{id}               # Actualizar promoción
DELETE /api/v1/promociones/{id}               # Eliminar promoción
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

## 🔐 Sistema de Roles y Permisos

### 🎯 **Roles del Sistema**

El sistema implementa un **sistema de control de acceso basado en roles (RBAC)** con 5 roles principales:

| Rol | Descripción | Nivel de Acceso |
|-----|-------------|-----------------|
| **👑 ADMIN** | Administrador del sistema | **Completo** - Acceso total a todas las funcionalidades |
| **🏢 GERENTE** | Gerente/Supervisor | **Alto** - Gestión completa excepto configuración del sistema |
| **💼 VENDEDOR** | Personal de ventas | **Medio** - Cotizaciones, pedidos, consultas |
| **📦 INVENTARIO** | Gestión de inventario | **Medio** - Componentes, PCs, proveedores, pedidos |
| **📊 CONSULTOR** | Solo consulta | **Bajo** - Solo lectura y reportes |

### 🛡️ **Matriz de Permisos por Módulo**

#### **🔧 Componentes de Hardware**

| Operación | ADMIN | GERENTE | VENDEDOR | INVENTARIO | CONSULTOR |
|-----------|-------|---------|-----------|------------|-----------|
| **Ver componentes** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Crear componentes** | ✅ | ❌ | ❌ | ✅ | ❌ |
| **Editar componentes** | ✅ | ✅ | ❌ | ✅ | ❌ |
| **Eliminar componentes** | ✅ | ❌ | ❌ | ❌ | ❌ |

#### **🖥️ Armado de PCs**

| Operación | ADMIN | GERENTE | VENDEDOR | INVENTARIO | CONSULTOR |
|-----------|-------|---------|-----------|------------|-----------|
| **Ver PCs** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Crear PCs** | ✅ | ✅ | ❌ | ✅ | ❌ |
| **Editar PCs** | ✅ | ✅ | ❌ | ✅ | ❌ |
| **Eliminar PCs** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Agregar componentes** | ✅ | ✅ | ❌ | ✅ | ❌ |
| **Remover componentes** | ✅ | ✅ | ❌ | ✅ | ❌ |
| **Ver costos** | ✅ | ✅ | ❌ | ✅ | ❌ |
| **Modificar precios** | ✅ | ✅ | ❌ | ❌ | ❌ |

#### **📋 Cotizaciones**

| Operación | ADMIN | GERENTE | VENDEDOR | INVENTARIO | CONSULTOR |
|-----------|-------|---------|-----------|------------|-----------|
| **Ver cotizaciones** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Ver detalles** | ✅ | ✅ | ✅ | ❌ | ✅ |
| **Ver costos** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Crear cotizaciones** | ✅ | ✅ | ✅ | ❌ | ❌ |
| **Editar cotizaciones** | ✅ | ✅ | ✅ | ❌ | ❌ |
| **Eliminar cotizaciones** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Aprobar cotizaciones** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Modificar precios** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Modificar impuestos** | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Ver márgenes** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Convertir a pedido** | ✅ | ✅ | ✅ | ❌ | ❌ |
| **Exportar** | ✅ | ✅ | ✅ | ❌ | ✅ |
| **Ver reportes** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Reportes financieros** | ✅ | ✅ | ❌ | ❌ | ❌ |

#### **🏢 Proveedores**

| Operación | ADMIN | GERENTE | VENDEDOR | INVENTARIO | CONSULTOR |
|-----------|-------|---------|-----------|------------|-----------|
| **Ver proveedores** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Crear proveedores** | ✅ | ✅ | ❌ | ✅ | ❌ |
| **Editar proveedores** | ✅ | ✅ | ❌ | ✅ | ❌ |
| **Eliminar proveedores** | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Búsqueda avanzada** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Ver datos comerciales** | ✅ | ✅ | ❌ | ✅ | ❌ |
| **Gestionar relaciones** | ✅ | ✅ | ❌ | ✅ | ❌ |

#### **📦 Pedidos**

| Operación | ADMIN | GERENTE | VENDEDOR | INVENTARIO | CONSULTOR |
|-----------|-------|---------|-----------|------------|-----------|
| **Ver pedidos** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Crear pedidos** | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Editar pedidos** | ✅ | ✅ | ❌ | ✅ | ❌ |
| **Eliminar pedidos** | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Aprobar pedidos** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Cambiar estado** | ✅ | ✅ | ❌ | ✅ | ❌ |
| **Ver cumplimiento** | ✅ | ✅ | ❌ | ✅ | ❌ |
| **Gestionar cumplimiento** | ✅ | ❌ | ❌ | ✅ | ❌ |
| **Ver datos financieros** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Generar reportes** | ✅ | ✅ | ✅ | ✅ | ✅ |

#### **🎁 Promociones**

| Operación | ADMIN | GERENTE | VENDEDOR | INVENTARIO | CONSULTOR |
|-----------|-------|---------|-----------|------------|-----------|
| **Ver promociones** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Crear promociones** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Editar promociones** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Eliminar promociones** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Aplicar promociones** | ✅ | ✅ | ✅ | ❌ | ❌ |
| **Ver impacto financiero** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Gestionar stacking** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Ver reportes** | ✅ | ✅ | ✅ | ✅ | ✅ |

### 💼 **Casos de Uso por Rol**

#### **👑 ADMIN (Administrador)**
- **Capacidades**: Acceso completo a todas las funcionalidades
- **Responsabilidades**: Configuración del sistema, gestión de usuarios, permisos globales
- **Flujo típico**: Configurar sistema → Gestionar usuarios → Supervisar operaciones → Generar reportes ejecutivos

#### **🏢 GERENTE (Gerente/Supervisor)**
- **Capacidades**: Gestión completa de operaciones comerciales
- **Responsabilidades**: Supervisar ventas, aprobar cotizaciones, gestionar proveedores
- **Flujo típico**: Revisar cotizaciones → Aprobar pedidos → Gestionar precios → Supervisar inventario

#### **💼 VENDEDOR (Personal de Ventas)**
- **Capacidades**: Crear cotizaciones, gestionar pedidos, consultar información
- **Responsabilidades**: Atender clientes, generar cotizaciones, convertir a pedidos
- **Flujo típico**: Consultar componentes → Crear cotización → Aplicar promociones → Convertir a pedido

#### **📦 INVENTARIO (Gestión de Inventario)**
- **Capacidades**: Gestionar componentes, PCs, proveedores y cumplimiento de pedidos
- **Responsabilidades**: Mantener catálogo actualizado, gestionar stock, coordinar entregas
- **Flujo típico**: Actualizar componentes → Gestionar stock → Coordinar proveedores → Seguir pedidos

#### **📊 CONSULTOR (Solo Consulta)**
- **Capacidades**: Acceso de lectura y generación de reportes
- **Responsabilidades**: Análisis de datos, generación de reportes, consultoría
- **Flujo típico**: Consultar datos → Generar reportes → Analizar tendencias → Proporcionar insights

### 🔧 **Implementación Técnica**

#### **Frontend (Vue.js)**
```javascript
// Verificación de permisos en componentes
computed: {
  canCreateComponents() {
    return this.authService.canCreateComponentes()
  },
  canEditCotizaciones() {
    return this.authService.canEditCotizaciones()
  }
}
```

#### **Backend (Spring Boot)**
```java
// Anotaciones de seguridad en controladores
@PreAuthorize("hasRole('ADMIN') or hasRole('GERENTE')")
@DeleteMapping("/{id}")
public ResponseEntity<ApiResponse> deleteComponente(@PathVariable String id)
```

#### **Base de Datos**
- **Tabla usuarios**: Información de usuarios
- **Tabla roles**: Definición de roles
- **Tabla usuario_roles**: Asignación de roles a usuarios
- **Middleware**: Verificación automática en cada request

### 🛡️ **Características de Seguridad**

- **Autenticación**: Basic Auth con credenciales seguras
- **Autorización**: Verificación de permisos en frontend y backend
- **Sesiones**: Gestión automática de sesiones activas
- **Middleware**: Interceptores automáticos para validación
- **UI Adaptativa**: Interfaz se adapta según permisos del usuario
- **Logging**: Registro de acciones por usuario y rol

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

### 🔄 CDC (Change Data Capture) con Kafka + Debezium

El sistema incluye CDC para replicar tablas clave entre microservicios usando Kafka y Debezium.

- Broker: Kafka 4 (KRaft)
- Conectores: Kafka Connect (Debezium)
- Configuración y scripts: `kafka-config/`
- Topics de cambios (por defecto):
  - `componentes.changes`, `promociones.changes`, `pcs.changes`, `cotizaciones.changes`, `pedidos.changes`

Flujo general:
```
MySQL (componentes/cotizaciones/pedidos) → Debezium Source → Kafka Topics → Debezium Sink → MySQL destino
```

Operación:
```bash
# Validar setup CDC
./kafka-config/validate-cdc-setup.sh

# Crear/actualizar conectores (si es necesario)
./kafka-config/setup-debezium-connectors.sh

# Monitoreo
./kafka-config/health-check.sh
./kafka-config/monitor-cdc.sh
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
- **Kafka 4 + Kafka Connect (Debezium)** - CDC entre microservicios
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
| **`docker-compose up` falla** | Archivos .env faltantes | **Ejecutar `./init-env.sh` o `./init-env.ps1` primero** |
| **Portal no carga** | Backend no disponible | `docker-compose ps` |
| **Error 401** | Autenticación | Verificar credenciales |
| **API calls fallan** | `.env.production` faltante | **Ejecutar script de inicialización** |
| **Base de datos vacía** | Scripts no ejecutados | Verificar DDL/DML |
| **Loading infinito** | Error en API | Verificar logs backend |

#### **Comandos de Diagnóstico**

```bash
# 1. Verificar archivos de configuración
ls -la .env portal-cotizador/.env.production

# 2. Si faltan archivos, ejecutar inicialización
./init-env.sh  # Linux/macOS
# o
./init-env.ps1  # Windows

# 3. Verificar servicios
docker-compose ps
docker-compose logs backend
docker-compose logs frontend

# 4. Health checks
curl http://localhost/actuator/health
curl http://localhost/

# 5. Reiniciar servicios
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