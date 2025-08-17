# 🖥️ Sistema Cotizador de PC Partes

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
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

### 🌐 **Acceso a los Servicios**

| Servicio | URL | Autenticación |
|----------|-----|---------------|
| **Portal Web** | http://localhost | Login con usuarios del sistema (JWT) |
| **API REST** | http://localhost:8080 | JWT Token (Authorization: Bearer) |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | JWT Token (Authorization: Bearer) |
| **Health Check** | http://localhost:8080/actuator/health | JWT Token (Authorization: Bearer) |

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