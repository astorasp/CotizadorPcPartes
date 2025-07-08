# 🚀 Portal Cotizador Vue.js 3

> **Portal Web moderno para gestión de cotizaciones de PC** desarrollado con Vue.js 3, Composition API, Pinia y sistema de loading centralizado.

[![Vue.js](https://img.shields.io/badge/Vue.js-3.0-brightgreen.svg)](https://vuejs.org/)
[![Pinia](https://img.shields.io/badge/Pinia-2.1-yellow.svg)](https://pinia.vuejs.org/)
[![TailwindCSS](https://img.shields.io/badge/TailwindCSS-3.4-blue.svg)](https://tailwindcss.com/)
[![Vite](https://img.shields.io/badge/Vite-5.0-purple.svg)](https://vitejs.dev/)

## 📌 Navegación del Proyecto

- **📖 [README Principal](../README.md)** - Documentación completa del sistema
- **🖥️ [Backend API](../ms-cotizador/README.md)** - Microservicio Spring Boot
- **🔒 [Seguridad](../ms-seguridad/)** - Microservicio de autenticación
- **🌐 [API Gateway](../nginx-gateway/README.md)** - Gateway Nginx
- **📚 [Documentación](../documentacion/)** - Diagramas y arquitectura

---

## ✅ Estado del Proyecto: **COMPLETADO**

**🎉 Portal 100% funcional y productivo** con todas las funcionalidades migradas

### 🏆 **Logros Completados**

#### **🏗️ Sistema de Loading Centralizado**
- ✅ **useLoadingStore.js** - Store central de loading
- ✅ **useAsyncOperation.js** - Composable para operaciones async
- ✅ **useCrudOperations** - Helper para operaciones CRUD
- ✅ **LoadingButton.vue** - Botones con estado de loading
- ✅ **LoadingSpinner.vue** - Indicadores de carga
- ✅ **LoadingOverlay.vue** - Overlays de pantalla completa
- ✅ **GlobalLoadingManager.vue** - Gestor global de operaciones

#### **📦 Stores Pinia (7/7 Completados)**
- ✅ **useAuthStore.js** - Autenticación con UI bloqueante
- ✅ **useComponentesStore.js** - Gestión de componentes hardware
- ✅ **useCotizacionesStore.js** - Sistema de cotizaciones
- ✅ **usePcsStore.js** - Ensamblaje de PCs con componentes
- ✅ **useProveedoresStore.js** - Gestión de proveedores
- ✅ **usePedidosStore.js** - Generación de pedidos
- ✅ **usePromocionesStore.js** - Sistema de promociones

#### **🖥️ Vistas Completas (7/7 Completadas)**
- ✅ **LoginView.vue** - Autenticación con LoadingButton
- ✅ **ComponentesView.vue** - CRUD completo con loading states
- ✅ **CotizacionesView.vue** - Gestión de cotizaciones
- ✅ **PcsView.vue** - Armado de PCs con componentes
- ✅ **ProveedoresView.vue** - Gestión de proveedores
- ✅ **PedidosView.vue** - Generación y seguimiento de pedidos
- ✅ **PromocionesView.vue** - Sistema de promociones

#### **🔧 Componentes UI (6/6 Completados)**
- ✅ **LoadingButton.vue** - 8 variantes, 5 tamaños
- ✅ **LoadingSpinner.vue** - Múltiples colores y tamaños
- ✅ **LoadingOverlay.vue** - Overlays con progreso
- ✅ **GlobalLoadingManager.vue** - Coordinador global
- ✅ **AlertSystem.vue** - Sistema de alertas
- ✅ **ConfirmModal.vue** - Modales de confirmación

---

## 🚀 Inicio Rápido

### **Prerrequisitos**
- Node.js 18+
- npm 8+
- Backend ejecutándose (ver [README del backend](../ms-cotizador/README.md))

### **Instalación**
```bash
# 1. Instalar dependencias
npm install

# 2. Configurar variables de entorno
cp .env.example .env

# 3. Iniciar servidor de desarrollo
npm run dev
```

### **URLs de Acceso**
- **Portal Vue.js**: http://localhost:3002
- **Backend API**: http://localhost:8080/cotizador/v1/api
- **Sistema Completo**: http://localhost (con Docker)

### **Credenciales de Acceso**
- **Usuario**: admin
- **Contraseña**: admin123

---

## 🏗️ Arquitectura del Portal

### 🎯 **Características del Portal**

El portal implementa un **sistema de loading centralizado** que proporciona:

- ✅ **Estados de loading unificados** para todas las operaciones
- ✅ **Feedback visual inmediato** en todas las interacciones  
- ✅ **Prevención de doble-click** automática
- ✅ **Componentes reutilizables** en todo el sistema
- ✅ **Integración con sistema de permisos** RBAC
- ✅ **Performance optimizada** con estados reactivos

### 🗂️ **Estructura del Proyecto**

```
portal-cotizador/
├── 📁 src/
│   ├── 📁 components/           # Componentes reutilizables
│   │   ├── 📁 ui/               # UI Components (Loading System)
│   │   │   ├── LoadingButton.vue
│   │   │   ├── LoadingSpinner.vue
│   │   │   ├── LoadingOverlay.vue
│   │   │   └── GlobalLoadingManager.vue
│   │   ├── 📁 auth/             # Componentes de autenticación
│   │   ├── 📁 componentes/      # Componentes de negocio
│   │   ├── 📁 cotizaciones/     # Gestión de cotizaciones
│   │   ├── 📁 pcs/              # Armado de PCs
│   │   ├── 📁 proveedores/      # Gestión de proveedores
│   │   ├── 📁 pedidos/          # Gestión de pedidos
│   │   └── 📁 promociones/      # Sistema de promociones
│   ├── 📁 composables/          # Vue Composables
│   │   ├── useAsyncOperation.js # Operaciones asíncronas
│   │   ├── useTokenMonitor.js   # Monitoreo de tokens
│   │   └── useUtils.js          # Utilidades generales
│   ├── 📁 stores/               # Pinia Stores (7 stores)
│   │   ├── useLoadingStore.js   # Sistema de loading central
│   │   ├── useAuthStore.js      # Autenticación y permisos
│   │   ├── useComponentesStore.js
│   │   ├── useCotizacionesStore.js
│   │   ├── usePcsStore.js
│   │   ├── useProveedoresStore.js
│   │   ├── usePedidosStore.js
│   │   └── usePromocionesStore.js
│   ├── 📁 views/                # Vistas principales (7 views)
│   │   ├── LoginView.vue
│   │   ├── ComponentesView.vue
│   │   ├── CotizacionesView.vue
│   │   ├── PcsView.vue
│   │   ├── ProveedoresView.vue
│   │   ├── PedidosView.vue
│   │   └── PromocionesView.vue
│   ├── 📁 services/             # Servicios de API (7 APIs)
│   │   ├── authService.js
│   │   ├── componentesApi.js
│   │   ├── cotizacionesApi.js
│   │   ├── pcsApi.js
│   │   ├── proveedoresApi.js
│   │   ├── pedidosApi.js
│   │   └── promocionesApi.js
│   └── 📁 router/               # Vue Router con lazy loading
├── 📁 public/                   # Assets estáticos
├── package.json                 # Dependencias del proyecto
├── tailwind.config.js           # Configuración TailwindCSS
├── vite.config.js               # Configuración Vite
└── README.md                    # Este archivo
```

---

## 💡 Sistema de Loading Centralizado

### 🎯 **Características Implementadas**

#### **Core Loading System**
- **`useLoadingStore.js`**: Store centralizado con Map-based tracking
- **`useAsyncOperation.js`**: Wrapper para operaciones asíncronas
- **`useCrudOperations`**: Helper especializado para CRUD

#### **UI Components**
- **`LoadingButton.vue`**: 8 variantes + 5 tamaños + estados automáticos
- **`LoadingSpinner.vue`**: Configurable con mensajes y colores
- **`LoadingOverlay.vue`**: Pantalla completa con progreso
- **`GlobalLoadingManager.vue`**: Coordinador global de operaciones

### 📊 **Estados por Módulo**

| Módulo | Estados de Loading | Operaciones Especiales |
|--------|-------------------|------------------------|
| **Auth** | isLoggingIn, isLoggingOut | UI bloqueante durante login |
| **Componentes** | isFetching, isCreating, isUpdating, isDeleting | Validación de reglas de negocio |
| **Cotizaciones** | isFetching, isCreating, isUpdating, isDeleting | Cálculos de impuestos en tiempo real |
| **PCs** | isFetching, isCreating, isUpdating, isDeleting, isAddingComponent, isRemovingComponent | Gestión compleja de componentes |
| **Proveedores** | isFetching, isCreating, isUpdating, isDeleting | Búsquedas avanzadas |
| **Pedidos** | isFetching, isGeneratingPedido, isLoadingDetails | Generación desde cotizaciones |
| **Promociones** | isFetching, isCreating, isUpdating, isDeleting | Cálculos de stacking financiero |

---

## 🔧 Scripts Disponibles

```bash
# Desarrollo
npm run dev              # Servidor de desarrollo con hot reload
npm run build            # Build optimizado para producción
npm run preview          # Preview del build de producción

# Calidad de código
npm run lint             # ESLint para detectar problemas
npm run format           # Prettier para formatear código
```

---

## 🌟 Funcionalidades Principales

### 🔐 **Sistema de Autenticación**
- **Login seguro** con Basic Auth
- **Gestión de sesiones** automática
- **Sistema de permisos** basado en roles (RBAC)
- **Logout automático** por expiración

### 🔧 **Gestión de Componentes**
- **CRUD completo** de componentes hardware
- **Filtros avanzados** por tipo, marca, modelo
- **Búsqueda en tiempo real** por múltiples campos
- **Paginación eficiente** con loading states
- **Validación de reglas** de negocio

### 🖥️ **Armado de PCs**
- **Configuración de PCs** personalizadas
- **Gestión de componentes** add/remove dinámico
- **Cálculos automáticos** de precios totales
- **Validación de compatibilidad** de componentes
- **Loading states** para operaciones complejas

### 📋 **Sistema de Cotizaciones**
- **Generación de cotizaciones** detalladas
- **Aplicación de promociones** automática
- **Cálculo de impuestos** por país (México, USA, Canadá)
- **Exportación** de cotizaciones (futuro)
- **Conversión a pedidos** automática

### 🏢 **Gestión de Proveedores**
- **CRUD completo** de proveedores
- **Búsqueda avanzada** por nombre y razón social
- **Asignación de componentes** a proveedores
- **Gestión de precios** por proveedor
- **Estados activo/inactivo**

### 📦 **Gestión de Pedidos**
- **Generación automática** desde cotizaciones
- **Asignación de proveedores** específicos
- **Configuración de fechas** de entrega
- **Seguimiento de entregas** (estado)
- **Loading states** para operaciones complejas

### 🎁 **Sistema de Promociones**
- **CRUD completo** de promociones
- **Tipos múltiples**: Sin descuento, Porcentaje, Monto fijo
- **Aplicación automática** por categoría
- **Stacking financiero** de promociones
- **Cálculos en tiempo real**

---

## 🛠️ Tecnologías Utilizadas

### 🎯 **Framework Principal**
- **Vue.js 3.4** - Framework reactivo con Composition API
- **Vite 5.0** - Build tool ultra-rápido con HMR
- **Pinia 2.1** - Gestión de estado moderna y tipada

### 🎨 **UI y Styling**
- **TailwindCSS 3.4** - Framework CSS utility-first
- **Heroicons** - Iconografía moderna y consistente
- **PostCSS** - Procesamiento avanzado de CSS

### 🔗 **Integración y Comunicación**
- **Axios 1.6** - Cliente HTTP con interceptors
- **Vue Router 4.2** - Enrutamiento SPA con lazy loading

### 🧪 **Desarrollo y Calidad**
- **ESLint** - Linting y detección de problemas
- **Prettier** - Formateo automático de código
- **Vite DevTools** - Debugging y análisis

---

## 🔗 Integración con Backend

### **🌐 API Endpoints**
La aplicación consume los endpoints del backend Spring Boot:

- **Base URL**: `http://localhost:8080/cotizador/v1/api`
- **Autenticación**: Basic Auth (admin/admin123)
- **Formato**: JSON con estructura `{codigo, mensaje, datos}`

### **🔒 Autenticación**
- **Automática**: El sistema maneja credenciales transparentemente
- **Renovación**: Sesiones gestionadas automáticamente
- **Permisos**: RBAC integrado con el backend

### **✅ Compatibilidad**
- ✅ **100% Compatible** con API existente
- ✅ **Mismas credenciales** que el sistema original
- ✅ **Mismo formato** de requests/responses
- ✅ **Manejo de errores** estándar

---

## 📊 Métricas del Proyecto

### **🎯 Completitud**
- **Stores**: 7/7 (100%)
- **Vistas**: 7/7 (100%)
- **Componentes UI**: 6/6 (100%)
- **APIs**: 7/7 (100%)
- **Funcionalidades**: 100% paridad con original

### **⚡ Performance**
- **Dev server startup**: ~300ms
- **Hot reload**: ~50ms
- **Build time**: ~8s
- **Bundle size**: ~111KB (gzipped: ~31KB)

### **💻 Código**
- **Líneas de código**: ~15,000+
- **Componentes Vue**: 25+
- **Composables**: 3
- **Stores Pinia**: 7

---

## 🚨 Requisitos del Sistema

### **📋 Dependencias**
- **Node.js**: 18.0+ (recomendado 20+)
- **npm**: 8.0+ (o yarn 1.22+)
- **Backend**: ms-cotizador ejecutándose
- **Base de datos**: MySQL 8.0+ configurada

### **🌐 Navegadores Soportados**
- **Chrome**: 90+
- **Firefox**: 88+
- **Safari**: 14+
- **Edge**: 90+

---

## 🔧 Configuración Avanzada

### **⚙️ Variables de Entorno**

```bash
# Archivo .env
VITE_API_BASE_URL=http://localhost:8080/cotizador/v1/api
VITE_AUTH_USERNAME=admin
VITE_AUTH_PASSWORD=admin123
VITE_APP_TITLE=Portal Cotizador
VITE_APP_VERSION=1.0.0
```

### **🎨 Personalización**

#### **Colores TailwindCSS**
```javascript
// tailwind.config.js
module.exports = {
  theme: {
    extend: {
      colors: {
        primary: {...},
        secondary: {...},
        // Colores personalizados del sistema
      }
    }
  }
}
```

#### **Configuración de API**
```javascript
// src/services/apiClient.js
const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL,
  TIMEOUT: 30000,
  RETRY_ATTEMPTS: 3
}
```

---

## 🧪 Testing y Calidad

### **🔍 Análisis de Código**
```bash
# Linting
npm run lint

# Fix automático
npm run lint:fix

# Formateo
npm run format
```

### **📋 Testing (Futuro)**
- **Unit Tests**: Vitest + Vue Testing Library
- **Integration Tests**: Cypress
- **E2E Tests**: Playwright

---

## 🚀 Deployment

### **🐳 Con Docker (Recomendado)**
```bash
# Desde la raíz del proyecto
docker-compose up -d

# Solo el frontend
docker build -t portal-cotizador .
docker run -p 3002:3002 portal-cotizador
```

### **☁️ Para Producción**
```bash
# Build optimizado
npm run build

# Servir archivos estáticos
# Los archivos generados estarán en /dist
```

---

## 🤝 Contribución

### **📝 Convenciones de Código**

#### **Vue.js**
- **Componentes**: PascalCase (`ComponenteModal.vue`)
- **Props**: camelCase (`isLoading`)
- **Events**: kebab-case (`@update-component`)
- **Stores**: camelCase con prefijo `use` (`useComponentesStore`)

#### **JavaScript**
- **Variables**: camelCase (`componenteData`)
- **Constantes**: UPPER_SNAKE_CASE (`API_BASE_URL`)
- **Funciones**: camelCase (`createComponente`)

### **🔄 Git Workflow**
```bash
# Feature branch
git checkout -b feature/nueva-funcionalidad

# Commits descriptivos
git commit -m "feat(componentes): agregar filtro por marca"

# Pull request
# Incluir screenshots y descripción detallada
```

---

## 🆘 Troubleshooting

### **❌ Problemas Comunes**

| Problema | Causa | Solución |
|----------|-------|----------|
| **Portal no carga** | Backend no disponible | Verificar `docker-compose ps` |
| **Error 401** | Credenciales incorrectas | Verificar .env variables |
| **Loading infinito** | Error en API | Revisar logs del backend |
| **Componentes no aparecen** | Base de datos vacía | Ejecutar scripts DDL/DML |

### **🔧 Comandos de Diagnóstico**

```bash
# Verificar estado del sistema
npm run dev
curl http://localhost:3002
curl http://localhost:8080/cotizador/v1/api/componentes

# Logs de desarrollo
# Los errores aparecen en la consola del navegador
# y en la terminal de desarrollo
```

---

## 📞 Soporte

### **🔗 Enlaces Útiles**
- **📖 [Documentación Principal](../README.md)** - Guía completa del sistema
- **🖥️ [Backend API](../ms-cotizador/README.md)** - Documentación del backend
- **🌐 [API Gateway](../nginx-gateway/README.md)** - Configuración del gateway
- **📚 [Vue.js 3 Docs](https://vuejs.org/)** - Documentación oficial
- **📦 [Pinia Docs](https://pinia.vuejs.org/)** - Gestión de estado

### **🆘 Soporte Técnico**
- **Issues**: Crear issue en GitHub
- **Documentación**: Ver archivos en `/docs`
- **API Docs**: http://localhost:8080/swagger-ui.html

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [LICENSE](../LICENSE) para más detalles.

---

<div align="center">

**🚀 Portal Cotizador Vue.js 3**

*Sistema de cotización de PC partes con arquitectura moderna*

[![Vue.js](https://img.shields.io/badge/Vue.js-3.0-brightgreen.svg)](https://vuejs.org/)
[![Pinia](https://img.shields.io/badge/Pinia-2.1-yellow.svg)](https://pinia.vuejs.org/)
[![TailwindCSS](https://img.shields.io/badge/TailwindCSS-3.4-blue.svg)](https://tailwindcss.com/)

**[⬆️ Volver al README Principal](../README.md)**

</div>