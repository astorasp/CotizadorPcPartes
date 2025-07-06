# 🚀 Portal Cotizador Vue.js 3

> Migración del Portal Cotizador a Vue.js 3 con arquitectura moderna y escalable

## 📋 Estado del Proyecto

**🚧 En desarrollo activo** - Fase 1 completada (Setup y estructura base)

### ✅ Completado
- [x] Proyecto Vue.js 3 configurado con Vite
- [x] Estructura de carpetas según arquitectura definida
- [x] TailwindCSS migrado con colores y estilos originales
- [x] Pinia configurado para estado global
- [x] Vue Router con lazy loading
- [x] Composables base (useUtils)
- [x] Cliente API base migrado
- [x] Sistema de alertas global
- [x] Navegación responsive
- [x] **Store de Componentes (useComponentesStore) - COMPLETADO**
- [x] **API service para componentes - COMPLETADO**
- [x] **Vista de Componentes conectada al backend - COMPLETADO**
- [x] **CRUD completo de componentes funcionando - COMPLETADO**
- [x] **Filtros, búsqueda y paginación - COMPLETADO**
- [x] **Store de PCs (usePcsStore) - COMPLETADO**
- [x] **API service para PCs - COMPLETADO**
- [x] **Vista de PCs con gestión de armado - COMPLETADO**
- [x] **Modal de gestión de componentes - COMPLETADO**
- [x] **Lógica de armado y validación de PCs - COMPLETADO**
- [x] **Store de Cotizaciones (useCotizacionesStore) - COMPLETADO**
- [x] **API service para Cotizaciones - COMPLETADO**
- [x] **Vista de Cotizaciones con funcionalidad completa - COMPLETADO**
- [x] **Modal de crear cotización con componentes e impuestos - COMPLETADO**
- [x] **Modal de ver cotización con detalles completos - COMPLETADO**
- [x] **Lógica de cálculos de impuestos en tiempo real - COMPLETADO**
- [x] **Store de Proveedores (useProveedoresStore) - COMPLETADO**
- [x] **API service para Proveedores - COMPLETADO**
- [x] **Vista de Proveedores con funcionalidad completa - COMPLETADO**
- [x] **Modal de gestión de proveedores - COMPLETADO**
- [x] **Búsqueda avanzada por nombre y razón social - COMPLETADO**
- [x] ESLint + Prettier configurados

### 🔄 En desarrollo
- [ ] Componentes UI reutilizables (Modal, Form, Table)
- [ ] Testing de integración completa
- [ ] Migración de otras secciones (Cotizaciones, Proveedores, etc.)

## 🚀 Inicio Rápido

### **Prerrequisitos**
- Node.js 18+
- npm 8+

### **Instalación**
```bash
# 1. Instalar dependencias
npm install

# 2. Copiar variables de entorno
cp .env.example .env

# 3. Ajustar configuración en .env si es necesario
# VITE_API_BASE_URL=http://localhost:8080/cotizador/v1/api

# 4. Iniciar servidor de desarrollo
npm run dev
```

### **URLs de desarrollo**
- **Aplicación Vue**: http://localhost:3002
- **API Backend**: http://localhost:8080/cotizador/v1/api
- **Portal Original**: http://localhost (para comparación)

## 🏗️ Arquitectura

```
src/
├── components/          # Componentes reutilizables
│   ├── ui/             # Componentes UI base (Alert, Modal, Button)
│   ├── forms/          # Formularios especializados
│   ├── tables/         # Tablas de datos
│   └── layout/         # Componentes de layout (Navbar)
├── views/              # Páginas/vistas de la aplicación
├── stores/             # Pinia stores (estado global)
├── composables/        # Lógica reutilizable
├── services/           # Servicios de API
├── router/             # Configuración de rutas
├── utils/              # Utilidades y constantes
└── assets/             # Assets estáticos
```

## 🔧 Scripts Disponibles

```bash
# Desarrollo
npm run dev          # Servidor de desarrollo
npm run build        # Build para producción
npm run preview      # Preview del build

# Calidad de código
npm run lint         # Linter ESLint
npm run format       # Formatear con Prettier
```

## 🎯 Progreso de Migración

### **Secciones**

| Sección | Estado | Progreso | Notas |
|---------|--------|----------|-------|
| **Componentes** | ✅ **COMPLETADO** | **100%** | CRUD completo, filtros, paginación funcionando |
| **PCs** | ✅ **COMPLETADO** | **100%** | Gestión completa de armado, modal de componentes |
| **Cotizaciones** | ✅ **COMPLETADO** | **100%** | Creación/visualización, componentes, impuestos, cálculos |
| **Proveedores** | ✅ **COMPLETADO** | **100%** | CRUD completo, búsqueda avanzada, modal de gestión |
| **Pedidos** | ⏳ Pendiente | 0% | Programado próximamente |
| **Promociones** | ⏳ Pendiente | 0% | Programado próximamente |

### **Componentes Core**

| Componente | Estado | Progreso |
|------------|--------|----------|
| Navbar | ✅ Completo | 100% |
| AlertSystem | ✅ Completo | 100% |
| ApiClient | ✅ Completo | 100% |
| useUtils | ✅ Completo | 100% |
| **useComponentesStore** | ✅ **Completo** | **100%** |
| **componentesApi** | ✅ **Completo** | **100%** |
| **ComponentesView** | ✅ **Completo** | **100%** |
| **usePcsStore** | ✅ **Completo** | **100%** |
| **pcsApi** | ✅ **Completo** | **100%** |
| **PcsView** | ✅ **Completo** | **100%** |
| **ManageComponentsModal** | ✅ **Completo** | **100%** |
| **ComponentCount** | ✅ **Completo** | **100%** |
| **useCotizacionesStore** | ✅ **Completo** | **100%** |
| **cotizacionesApi** | ✅ **Completo** | **100%** |
| **CotizacionesView** | ✅ **Completo** | **100%** |
| **CreateCotizacionModal** | ✅ **Completo** | **100%** |
| **ViewCotizacionModal** | ✅ **Completo** | **100%** |
| **useProveedoresStore** | ✅ **Completo** | **100%** |
| **proveedoresApi** | ✅ **Completo** | **100%** |
| **ProveedoresView** | ✅ **Completo** | **100%** |
| **ProveedorModal** | ✅ **Completo** | **100%** |
| Modal | ⏳ Pendiente | 0% |
| Table | ⏳ Pendiente | 0% |
| Form Components | ⏳ Pendiente | 0% |

## 🔄 Comparación con Original

### **Beneficios Ya Visibles**
- **Hot Reload**: Cambios instantáneos en desarrollo
- **Navegación SPA**: Sin recargas de página
- **Componentes reutilizables**: Navbar, AlertSystem
- **TypeScript ready**: Preparado para tipado
- **Bundle optimizado**: Code splitting automático

### **Paridad Funcional**
- ✅ Navegación entre secciones
- ✅ Sistema de alertas
- ✅ Colores y estilos TailwindCSS
- ✅ **CRUD completo de componentes**
- ✅ **Filtros y búsqueda de componentes**
- ✅ **Paginación de componentes**
- ✅ **Integración con backend real**
- ✅ **CRUD completo de PCs**
- ✅ **Gestión de armado de PCs con componentes**
- ✅ **Modal interactivo para gestión de componentes**
- ✅ **Cálculos en tiempo real de costos**
- ✅ **CRUD completo de cotizaciones**
- ✅ **Creación de cotizaciones con componentes e impuestos**
- ✅ **Cálculos automáticos de impuestos por país**
- ✅ **Visualización detallada de cotizaciones**
- ✅ **CRUD completo de proveedores**
- ✅ **Búsqueda avanzada de proveedores (general, nombre, razón social)**
- ✅ **Modal de gestión de proveedores con validaciones**
- ✅ **Paginación y filtrado de proveedores**

## 🛠️ Configuración Técnica

### **Dependencias Principales**
- **Vue.js 3.4**: Framework reactivo
- **Pinia 2.1**: Estado global
- **Vue Router 4.2**: Enrutamiento SPA
- **Axios 1.6**: Cliente HTTP
- **TailwindCSS 3.4**: Framework CSS

### **Herramientas de Desarrollo**
- **Vite 5.0**: Build tool y dev server
- **ESLint + Prettier**: Calidad de código
- **Heroicons**: Iconografía

### **Variables de Entorno**
```bash
VITE_API_BASE_URL=http://localhost:8080/cotizador/v1/api
VITE_AUTH_USERNAME=admin
VITE_AUTH_PASSWORD=admin123
```

## 📊 Métricas de Desarrollo

### **Performance**
- **Dev server startup**: ~350ms
- **Hot reload**: ~50ms
- **Build time**: TBD
- **Bundle size**: TBD

### **Código**
- **Líneas migradas**: ~500/7,092 (7%)
- **Componentes Vue**: 4/estimado 20
- **Stores**: 0/6
- **Tests**: 0/pendientes

## 🔗 Integración con Backend

### **API Endpoints**
La aplicación consume los mismos endpoints que el portal original:
- **Base URL**: `http://localhost:8080/cotizador/v1/api`
- **Autenticación**: Basic Auth (admin/admin123)
- **Formato**: JSON con estructura `{codigo, mensaje, datos}`

### **Compatibilidad**
- ✅ Mantiene exactamente la misma interfaz de API
- ✅ Mismas credenciales de autenticación
- ✅ Mismo formato de requests/responses

## 🚨 Próximos Pasos Inmediatos

### **Esta Semana**
1. **Completar sección Componentes**
   - Crear `useComponentesStore`
   - Implementar CRUD completo
   - Migrar formularios y modales

2. **Componentes UI base**
   - Modal reutilizable
   - Componentes de formulario
   - Tabla genérica con paginación

### **Siguientes 2 Semanas**
1. **Sección PCs** (más compleja)
2. **Sección Cotizaciones** (crítica para negocio)
3. **Testing e integración**

## 📖 Referencias

- **Plan completo**: Ver `/portal-cotizador/PLAN.md`
- **Proyecto original**: `/portal-cotizador/`
- **Vue.js 3 docs**: https://vuejs.org/
- **Pinia docs**: https://pinia.vuejs.org/
- **TailwindCSS**: https://tailwindcss.com/

## 🤝 Contribución

### **Estructura de commits**
```bash
git commit -m "feat(componentes): implementar store de componentes"
git commit -m "fix(navbar): corregir navegación móvil"
git commit -m "style(forms): aplicar estilos TailwindCSS"
```

### **Pull Requests**
- Crear PR por sección migrada
- Incluir screenshots de antes/después
- Verificar que todas las features funcionen

---

**🎯 Objetivo**: Mantener 100% de paridad funcional mientras mejoramos significativamente la arquitectura, mantenibilidad y developer experience.

**📞 Support**: Ver `/portal-cotizador/PLAN.md` para contactos y escalation path.