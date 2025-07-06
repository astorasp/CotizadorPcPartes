# 📋 Plan de Migración: Portal-Cotizador a Vue.js 3

## 🎯 Resumen Ejecutivo

Este documento describe la estrategia completa para migrar el portal-cotizador actual (HTML/CSS/JS vanilla) a una aplicación Vue.js 3 moderna y escalable.

### 📊 Estado Actual
- **1,950 líneas HTML** (archivo monolítico)
- **7,092 líneas JavaScript** (distribuidas en 11 archivos)
- **Arquitectura SPA** con managers por sección
- **TailwindCSS** ya implementado
- **Sistema de autenticación** Basic Auth funcional

### 🚀 Objetivo Final
- **Aplicación Vue.js 3** con arquitectura moderna
- **Reducción 60-70%** en líneas de código
- **Componentes reutilizables** y mantenibles
- **Performance mejorada** con Virtual DOM
- **Developer Experience** superior

---

## 📈 Análisis Costo-Beneficio

### ✅ Beneficios
- **Mantenimiento simplificado**: Componentes modulares y reutilizables
- **Performance mejorada**: Virtual DOM + optimizaciones automáticas
- **Escalabilidad**: Arquitectura preparada para crecimiento
- **Developer Experience**: Hot reload, debugging avanzado, tooling moderno
- **Testing facilitado**: Vue Test Utils y ecosystem robusto
- **Code splitting**: Carga optimizada por secciones

### 💰 Costos
- **Tiempo de migración**: 6-8 semanas
- **Curva de aprendizaje**: 2-3 semanas
- **Setup inicial**: Build tools y configuración
- **Actualización Docker**: Para servir aplicación compilada

### 📊 ROI Estimado
- **Reducción tiempo desarrollo**: 40-50% a largo plazo
- **Reducción bugs**: 30-40% por mejor estructura
- **Velocidad nuevas features**: 50-60% más rápido

---

## 🏗️ Arquitectura Vue.js 3 Propuesta

```
portal-cotizador-vue/
├── 📁 src/
│   ├── 📁 components/           # Componentes reutilizables
│   │   ├── 📁 ui/              # Componentes UI base
│   │   │   ├── Alert.vue        # Migración de PortalUtils.Alert
│   │   │   ├── Modal.vue        # Modales reutilizables
│   │   │   ├── Pagination.vue   # Componente de paginación
│   │   │   ├── LoadingSpinner.vue
│   │   │   ├── Button.vue       # Botones con variantes
│   │   │   └── FormField.vue    # Campos de formulario
│   │   │
│   │   ├── 📁 forms/           # Componentes de formularios
│   │   │   ├── ComponenteForm.vue
│   │   │   ├── PcForm.vue
│   │   │   ├── CotizacionForm.vue
│   │   │   ├── ProveedorForm.vue
│   │   │   ├── PedidoForm.vue
│   │   │   └── PromocionForm.vue
│   │   │
│   │   ├── 📁 tables/          # Componentes de tablas
│   │   │   ├── ComponentesTable.vue
│   │   │   ├── PcsTable.vue
│   │   │   ├── CotizacionesTable.vue
│   │   │   ├── ProveedoresTable.vue
│   │   │   ├── PedidosTable.vue
│   │   │   ├── PromocionesTable.vue
│   │   │   └── GenericTable.vue # Tabla base reutilizable
│   │   │
│   │   └── 📁 layout/          # Componentes de layout
│   │       ├── Navbar.vue      # Navegación principal
│   │       ├── MobileMenu.vue  # Menú móvil
│   │       └── PageHeader.vue  # Encabezados de página
│   │
│   ├── 📁 views/               # Vistas por sección (páginas)
│   │   ├── ComponentesView.vue # Migración sección componentes
│   │   ├── PcsView.vue         # Migración sección PCs
│   │   ├── CotizacionesView.vue
│   │   ├── ProveedoresView.vue
│   │   ├── PedidosView.vue
│   │   └── PromocionesView.vue
│   │
│   ├── 📁 stores/              # Pinia stores (estado global)
│   │   ├── useComponentesStore.js # Migración ComponentesManager
│   │   ├── usePcsStore.js         # Migración PcsManager
│   │   ├── useCotizacionesStore.js
│   │   ├── useProveedoresStore.js
│   │   ├── usePedidosStore.js
│   │   ├── usePromocionesStore.js
│   │   └── useAppStore.js         # Estado global de app
│   │
│   ├── 📁 composables/         # Lógica reutilizable
│   │   ├── useApi.js          # Migración completa de api.js
│   │   ├── useUtils.js        # Migración completa de utils.js
│   │   ├── usePagination.js   # Lógica de paginación extraída
│   │   ├── useModal.js        # Manejo de modales
│   │   ├── useSearch.js       # Lógica de búsqueda y filtros
│   │   ├── useAuth.js         # Manejo de autenticación
│   │   └── useValidation.js   # Validaciones de formularios
│   │
│   ├── 📁 services/           # Servicios de API
│   │   ├── apiClient.js       # Cliente HTTP base
│   │   ├── componentesApi.js  # API específica de componentes
│   │   ├── pcsApi.js         # API específica de PCs
│   │   ├── cotizacionesApi.js
│   │   ├── proveedoresApi.js
│   │   ├── pedidosApi.js
│   │   ├── promocionesApi.js
│   │   └── index.js          # Exports centralizados
│   │
│   ├── 📁 router/             # Vue Router
│   │   └── index.js          # Configuración de rutas + lazy loading
│   │
│   ├── 📁 utils/             # Utilidades
│   │   ├── constants.js      # Migración de config.js
│   │   ├── validators.js     # Funciones de validación
│   │   └── formatters.js     # Formateo de datos
│   │
│   ├── 📁 assets/            # Assets estáticos
│   │   └── styles/
│   │       ├── main.css      # Estilos globales + Tailwind
│   │       └── components.css # Estilos de componentes
│   │
│   ├── App.vue               # Componente raíz
│   ├── main.js              # Entry point
│   └── env.d.ts            # Tipos TypeScript (opcional)
│
├── 📁 public/               # Assets públicos
├── index.html              # HTML template
├── vite.config.js          # Configuración Vite
├── tailwind.config.js      # Configuración Tailwind migrada
├── package.json           # Dependencias
└── README.md             # Documentación del proyecto Vue
```

---

## 🔄 Mapeo de Migración Detallado

### 📂 Managers → Pinia Stores

| Manager Actual | Store Vue | Responsabilidades | Métodos Clave |
|----------------|-----------|-------------------|---------------|
| `ComponentesManager` | `useComponentesStore` | CRUD componentes, filtros, paginación | `loadComponentes()`, `createComponente()`, `updateComponente()`, `deleteComponente()`, `handleSearch()`, `applyFilters()` |
| `PcsManager` | `usePcsStore` | CRUD PCs, ensamblaje, validación | `loadPcs()`, `createPc()`, `addComponentToPc()`, `removeComponentFromPc()`, `validatePcConfiguration()` |
| `CotizacionesManager` | `useCotizacionesStore` | CRUD cotizaciones, promociones | `loadCotizaciones()`, `createCotizacion()`, `updateCotizacion()`, `applyPromotion()`, `calculateTotal()` |
| `ProveedoresManager` | `useProveedoresStore` | CRUD proveedores | `loadProveedores()`, `createProveedor()`, `updateProveedor()`, `deleteProveedor()` |
| `PedidosManager` | `usePedidosStore` | CRUD pedidos, generación | `loadPedidos()`, `generatePedido()`, `updatePedido()`, `trackPedido()` |
| `PromocionesManager` | `usePromocionesStore` | CRUD promociones, aplicación | `loadPromociones()`, `createPromocion()`, `updatePromocion()`, `applyToComponents()` |

### 🔧 Servicios → Composables

| Archivo Actual | Composable Vue | Función |
|----------------|----------------|---------|
| `api.js` | `useApi.js` + `services/` | Cliente HTTP y endpoints específicos |
| `utils.js` | `useUtils.js` | Utilidades generales, alertas, formatos |
| `config.js` | `utils/constants.js` | Configuraciones y constantes |
| Lógica de paginación | `usePagination.js` | Manejo de paginación reutilizable |
| Lógica de modales | `useModal.js` | Apertura/cierre de modales |
| Lógica de búsqueda | `useSearch.js` | Filtros y búsquedas |

### 🎨 UI → Componentes Vue

| Elemento Actual | Componente Vue | Descripción |
|-----------------|----------------|-------------|
| Alertas JavaScript | `Alert.vue` | Migración completa de `PortalUtils.Alert` |
| Modales HTML | `Modal.vue` | Modal base reutilizable |
| Tablas HTML | `GenericTable.vue` | Tabla con paginación y filtros |
| Formularios HTML | Componentes Form específicos | Formularios con validación |
| Navegación HTML | `Navbar.vue` + `MobileMenu.vue` | Navegación responsive |
| Botones HTML | `Button.vue` | Botones con variantes y estados |

---

## 📅 Cronograma de Migración (8 semanas)

### 🚀 Semana 1: Setup y Preparación
**Objetivos:**
- Configurar proyecto Vue.js 3
- Instalar y configurar dependencias
- Migrar configuraciones base

**Tareas:**
```bash
# 1. Crear proyecto Vue.js 3
npm create vue@latest portal-cotizador-vue
cd portal-cotizador-vue

# 2. Instalar dependencias necesarias
npm install pinia @pinia/nuxt
npm install @tailwindcss/forms @tailwindcss/typography
npm install axios date-fns

# 3. Configurar herramientas de desarrollo
npm install -D @vue/test-utils vitest jsdom
npm install -D eslint prettier @vue/eslint-config-prettier
```

**Entregables:**
- ✅ Proyecto Vue.js 3 configurado
- ✅ TailwindCSS migrado con colores actuales
- ✅ Configuración de desarrollo con hot reload
- ✅ Estructura de carpetas implementada

### 🧪 Semana 2: Componentes Base y Sección Piloto
**Objetivos:**
- Crear componentes UI reutilizables
- Migrar servicios core (API, utils)
- Implementar sección piloto (Componentes)

**Tareas:**
1. **Migrar utilidades base:**
   ```javascript
   // composables/useUtils.js
   export const useUtils = () => {
     // Migrar PortalUtils.Alert, Format, etc.
   }
   
   // services/apiClient.js
   class ApiClient {
     // Migrar toda la lógica de PortalApi
   }
   ```

2. **Crear componentes UI:**
   - `Alert.vue` - Sistema de alertas
   - `Modal.vue` - Modales reutilizables
   - `Button.vue` - Botones con variantes
   - `LoadingSpinner.vue` - Indicadores de carga

3. **Migrar sección Componentes completa:**
   - `ComponentesView.vue` - Vista principal
   - `ComponentesTable.vue` - Tabla de componentes
   - `ComponenteForm.vue` - Formulario CRUD
   - `useComponentesStore.js` - Store con toda la lógica

**Entregables:**
- ✅ Componentes UI base funcionales
- ✅ Sección Componentes 100% migrada y funcional
- ✅ Sistema de alertas y modales operativo
- ✅ Validación de arquitectura con sección piloto

### 🏗️ Semanas 3-4: Secciones Core del Negocio
**Objetivos:**
- Migrar las secciones más complejas y críticas
- Validar integración entre secciones
- Implementar navegación completa

**Semana 3 - Sección PCs:**
- `PcsView.vue` - Vista de armado de PCs
- `PcForm.vue` - Formulario de configuración
- `usePcsStore.js` - Lógica de ensamblaje y validación
- Componentes específicos para selección de partes

**Semana 4 - Sección Cotizaciones:**
- `CotizacionesView.vue` - Vista de cotizaciones
- `CotizacionForm.vue` - Formulario con cálculos dinámicos
- `useCotizacionesStore.js` - Lógica de negocio y promociones
- Integración con PCs y Componentes

**Entregables:**
- ✅ Sección PCs 100% funcional con validaciones
- ✅ Sección Cotizaciones con cálculos automáticos
- ✅ Navegación entre secciones operativa
- ✅ Integración de datos entre stores

### 📦 Semanas 5-6: Secciones Secundarias
**Objetivos:**
- Completar las secciones restantes
- Implementar funcionalidades avanzadas
- Optimizar performance

**Semana 5:**
- Sección Proveedores - `ProveedoresView.vue` + store
- Sección Pedidos - `PedidosView.vue` + store

**Semana 6:**
- Sección Promociones - `PromocionesView.vue` + store
- Integración completa de promociones con cotizaciones
- Testing de integración entre todas las secciones

**Entregables:**
- ✅ Todas las secciones migradas y funcionales
- ✅ Flujo completo de negocio operativo
- ✅ Sistema de promociones integrado
- ✅ Performance optimizada

### 🚀 Semanas 7-8: Optimización y Deploy
**Objetivos:**
- Optimizar bundle y performance
- Implementar testing
- Configurar deployment
- Documentación final

**Semana 7 - Optimización:**
```javascript
// router/index.js - Lazy loading
const routes = [
  {
    path: '/componentes',
    component: () => import('../views/ComponentesView.vue')
  }
]

// vite.config.js - Code splitting
export default {
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['vue', 'pinia'],
          ui: ['./src/components/ui']
        }
      }
    }
  }
}
```

**Semana 8 - Deploy y Documentación:**
- Actualizar Dockerfile para aplicación Vue
- Configurar variables de entorno para producción
- Documentación de componentes y stores
- Guía de migración para futuros desarrolladores

**Entregables:**
- ✅ Bundle optimizado con code splitting
- ✅ Aplicación dockerizada y deployable
- ✅ Testing suite implementado
- ✅ Documentación completa

---

## 🧩 Ejemplos de Código de Migración

### 📊 Store Ejemplo (ComponentesStore)

```javascript
// stores/useComponentesStore.js
import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import { componentesApi } from '@/services'

export const useComponentesStore = defineStore('componentes', () => {
  // 📝 Estado (migrar del constructor del manager)
  const componentes = ref([])
  const filteredComponentes = ref([])
  const currentComponent = ref(null)
  const loading = ref(false)
  const isEditMode = ref(false)
  
  const pagination = ref({
    currentPage: 1,
    pageSize: 5,
    totalPages: 0,
    totalItems: 0
  })
  
  const filters = ref({
    searchTerm: '',
    tipo: '',
    priceRange: null
  })
  
  // 🔄 Computed (reactividad automática)
  const hasComponents = computed(() => componentes.value.length > 0)
  const paginatedComponents = computed(() => {
    const start = (pagination.value.currentPage - 1) * pagination.value.pageSize
    const end = start + pagination.value.pageSize
    return filteredComponentes.value.slice(start, end)
  })
  
  // ⚡ Actions (migrar métodos del manager)
  const fetchComponentes = async () => {
    loading.value = true
    try {
      // Conservar lógica exacta de loadComponentes()
      const apiResponse = await componentesApi.getAll()
      if (apiResponse.codigo === '0') {
        componentes.value = apiResponse.datos
        applyFilters() // Aplicar filtros actuales
      } else {
        throw new Error(apiResponse.mensaje)
      }
    } catch (error) {
      console.error('Error fetching componentes:', error)
      // Usar composable de alertas
      const { showAlert } = useUtils()
      showAlert('error', 'Error al cargar componentes')
    } finally {
      loading.value = false
    }
  }
  
  const createComponente = async (componenteData) => {
    // Conservar validaciones y lógica actual
    try {
      loading.value = true
      const response = await componentesApi.create(componenteData)
      if (response.codigo === '0') {
        await fetchComponentes() // Recargar lista
        const { showAlert } = useUtils()
        showAlert('success', 'Componente creado exitosamente')
        return { success: true }
      }
      return { success: false, error: response.mensaje }
    } catch (error) {
      return { success: false, error: error.message }
    } finally {
      loading.value = false
    }
  }
  
  const updateComponente = async (id, componenteData) => {
    // Migrar lógica de updateComponente()
    try {
      loading.value = true
      const response = await componentesApi.update(id, componenteData)
      if (response.codigo === '0') {
        await fetchComponentes()
        const { showAlert } = useUtils()
        showAlert('success', 'Componente actualizado exitosamente')
        return { success: true }
      }
      return { success: false, error: response.mensaje }
    } catch (error) {
      return { success: false, error: error.message }
    } finally {
      loading.value = false
    }
  }
  
  const deleteComponente = async (id) => {
    // Migrar lógica de deleteComponente()
    try {
      loading.value = true
      const response = await componentesApi.delete(id)
      if (response.codigo === '0') {
        await fetchComponentes()
        const { showAlert } = useUtils()
        showAlert('success', 'Componente eliminado exitosamente')
        return { success: true }
      }
      return { success: false, error: response.mensaje }
    } catch (error) {
      return { success: false, error: error.message }
    } finally {
      loading.value = false
    }
  }
  
  const applyFilters = () => {
    let filtered = [...componentes.value]
    
    // Conservar lógica de filtros actual
    if (filters.value.searchTerm) {
      const searchLower = filters.value.searchTerm.toLowerCase()
      filtered = filtered.filter(comp => 
        comp.descripcion.toLowerCase().includes(searchLower) ||
        comp.marca.toLowerCase().includes(searchLower) ||
        comp.modelo.toLowerCase().includes(searchLower)
      )
    }
    
    if (filters.value.tipo) {
      filtered = filtered.filter(comp => comp.tipo === filters.value.tipo)
    }
    
    if (filters.value.priceRange) {
      const [min, max] = filters.value.priceRange
      filtered = filtered.filter(comp => 
        comp.precioBase >= min && comp.precioBase <= max
      )
    }
    
    filteredComponentes.value = filtered
    updatePagination()
  }
  
  const updatePagination = () => {
    pagination.value.totalItems = filteredComponentes.value.length
    pagination.value.totalPages = Math.ceil(
      pagination.value.totalItems / pagination.value.pageSize
    )
    
    // Ajustar página actual si es necesario
    if (pagination.value.currentPage > pagination.value.totalPages) {
      pagination.value.currentPage = 1
    }
  }
  
  const setFilter = (key, value) => {
    filters.value[key] = value
    applyFilters()
  }
  
  const clearFilters = () => {
    filters.value = {
      searchTerm: '',
      tipo: '',
      priceRange: null
    }
    applyFilters()
  }
  
  const setPage = (page) => {
    pagination.value.currentPage = page
  }
  
  const setPageSize = (size) => {
    pagination.value.pageSize = size
    pagination.value.currentPage = 1
    updatePagination()
  }
  
  return {
    // 📊 State (readonly para prevenir mutaciones directas)
    componentes: readonly(componentes),
    filteredComponentes: readonly(filteredComponentes),
    currentComponent,
    loading: readonly(loading),
    pagination: readonly(pagination),
    filters: readonly(filters),
    
    // 🔄 Computed
    hasComponents,
    paginatedComponents,
    
    // ⚡ Actions
    fetchComponentes,
    createComponente,
    updateComponente,
    deleteComponente,
    applyFilters,
    setFilter,
    clearFilters,
    setPage,
    setPageSize
  }
})
```

### 🎨 Componente de Vista Ejemplo

```vue
<!-- views/ComponentesView.vue -->
<template>
  <div class="p-6">
    <!-- Header de la página -->
    <PageHeader
      title="Gestión de Componentes"
      subtitle="Administrar componentes de hardware para PCs"
    >
      <template #actions>
        <Button
          variant="primary"
          @click="openCreateModal"
          :disabled="loading"
        >
          <PlusIcon class="w-4 h-4 mr-2" />
          Nuevo Componente
        </Button>
      </template>
    </PageHeader>

    <!-- Filtros y búsqueda -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <FormField
          v-model="searchTerm"
          type="text"
          placeholder="Buscar componentes..."
          @input="handleSearch"
        >
          <template #prefix>
            <SearchIcon class="w-4 h-4" />
          </template>
        </FormField>
        
        <FormField
          v-model="selectedType"
          type="select"
          placeholder="Filtrar por tipo"
          :options="typeOptions"
          @change="handleTypeFilter"
        />
        
        <Button
          variant="secondary"
          @click="applyFilters"
        >
          Aplicar Filtros
        </Button>
        
        <Button
          variant="outline"
          @click="clearFilters"
        >
          Limpiar
        </Button>
      </div>
    </div>

    <!-- Tabla de componentes -->
    <div class="bg-white rounded-lg shadow">
      <ComponentesTable
        :componentes="paginatedComponents"
        :loading="loading"
        @edit="openEditModal"
        @delete="handleDelete"
        @view="openViewModal"
      />
      
      <!-- Paginación -->
      <div class="px-6 py-4 border-t">
        <Pagination
          v-model:page="currentPage"
          v-model:page-size="pageSize"
          :total-pages="totalPages"
          :total-items="totalItems"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </div>

    <!-- Modal de formulario -->
    <Modal
      v-model="showModal"
      :title="isEditMode ? 'Editar Componente' : 'Nuevo Componente'"
      size="lg"
    >
      <ComponenteForm
        :componente="currentComponent"
        :is-edit-mode="isEditMode"
        @submit="handleFormSubmit"
        @cancel="closeModal"
      />
    </Modal>

    <!-- Modal de vista detalle -->
    <Modal
      v-model="showViewModal"
      title="Detalle del Componente"
      size="md"
    >
      <ComponenteDetail
        :componente="currentComponent"
        @close="closeViewModal"
      />
    </Modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useComponentesStore } from '@/stores/useComponentesStore'
import { useUtils } from '@/composables/useUtils'

// Components
import PageHeader from '@/components/layout/PageHeader.vue'
import ComponentesTable from '@/components/tables/ComponentesTable.vue'
import ComponenteForm from '@/components/forms/ComponenteForm.vue'
import ComponenteDetail from '@/components/ComponenteDetail.vue'
import Button from '@/components/ui/Button.vue'
import FormField from '@/components/ui/FormField.vue'
import Modal from '@/components/ui/Modal.vue'
import Pagination from '@/components/ui/Pagination.vue'

// Icons
import { PlusIcon, SearchIcon } from '@heroicons/vue/24/outline'

// Composables
const componentesStore = useComponentesStore()
const { showAlert, confirm } = useUtils()

// State del store
const {
  paginatedComponents,
  loading,
  hasComponents,
  currentComponent,
  pagination,
  filters
} = storeToRefs(componentesStore)

// State local del componente
const showModal = ref(false)
const showViewModal = ref(false)
const isEditMode = ref(false)
const searchTerm = ref('')
const selectedType = ref('')

// Computed
const currentPage = computed({
  get: () => pagination.value.currentPage,
  set: (value) => componentesStore.setPage(value)
})

const pageSize = computed({
  get: () => pagination.value.pageSize,
  set: (value) => componentesStore.setPageSize(value)
})

const totalPages = computed(() => pagination.value.totalPages)
const totalItems = computed(() => pagination.value.totalItems)

const typeOptions = computed(() => [
  { value: '', label: 'Todos los tipos' },
  { value: 'MONITOR', label: 'Monitor' },
  { value: 'DISCO_DURO', label: 'Disco Duro' },
  { value: 'TARJETA_VIDEO', label: 'Tarjeta de Video' },
  { value: 'PC', label: 'PC Completa' }
])

// Methods
const handleSearch = (value) => {
  componentesStore.setFilter('searchTerm', value)
}

const handleTypeFilter = (value) => {
  componentesStore.setFilter('tipo', value)
}

const applyFilters = () => {
  componentesStore.applyFilters()
}

const clearFilters = () => {
  searchTerm.value = ''
  selectedType.value = ''
  componentesStore.clearFilters()
}

const handlePageChange = (page) => {
  componentesStore.setPage(page)
}

const handlePageSizeChange = (size) => {
  componentesStore.setPageSize(size)
}

const openCreateModal = () => {
  currentComponent.value = null
  isEditMode.value = false
  showModal.value = true
}

const openEditModal = (componente) => {
  currentComponent.value = { ...componente }
  isEditMode.value = true
  showModal.value = true
}

const openViewModal = (componente) => {
  currentComponent.value = componente
  showViewModal.value = true
}

const closeModal = () => {
  showModal.value = false
  currentComponent.value = null
}

const closeViewModal = () => {
  showViewModal.value = false
  currentComponent.value = null
}

const handleFormSubmit = async (formData) => {
  try {
    let result
    if (isEditMode.value) {
      result = await componentesStore.updateComponente(currentComponent.value.id, formData)
    } else {
      result = await componentesStore.createComponente(formData)
    }
    
    if (result.success) {
      closeModal()
    } else {
      showAlert('error', result.error || 'Error al guardar el componente')
    }
  } catch (error) {
    showAlert('error', 'Error inesperado al guardar')
  }
}

const handleDelete = async (componente) => {
  const confirmed = await confirm(
    'Eliminar Componente',
    `¿Está seguro de que desea eliminar el componente "${componente.descripcion}"?`
  )
  
  if (confirmed) {
    const result = await componentesStore.deleteComponente(componente.id)
    if (!result.success) {
      showAlert('error', result.error || 'Error al eliminar el componente')
    }
  }
}

// Lifecycle
onMounted(() => {
  componentesStore.fetchComponentes()
})
</script>
```

### 🔧 Composable Ejemplo (useUtils)

```javascript
// composables/useUtils.js
import { ref, nextTick } from 'vue'

export const useUtils = () => {
  // 🚨 Sistema de alertas (migración de PortalUtils.Alert)
  const alerts = ref([])
  
  const showAlert = (type, message, duration = 5000) => {
    const id = Date.now()
    const alert = {
      id,
      type, // 'success', 'error', 'warning', 'info'
      message,
      visible: true
    }
    
    alerts.value.push(alert)
    
    // Auto-cerrar después del duration
    if (duration > 0) {
      setTimeout(() => {
        closeAlert(id)
      }, duration)
    }
    
    return id
  }
  
  const closeAlert = (id) => {
    const index = alerts.value.findIndex(alert => alert.id === id)
    if (index > -1) {
      alerts.value.splice(index, 1)
    }
  }
  
  // 💰 Formateo de moneda (migración de PortalUtils.Format)
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount)
  }
  
  // 📅 Formateo de fechas
  const formatDate = (date, options = {}) => {
    const defaultOptions = {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }
    
    return new Intl.DateTimeFormat('es-MX', {
      ...defaultOptions,
      ...options
    }).format(new Date(date))
  }
  
  const formatDateTime = (date) => {
    return formatDate(date, {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  }
  
  // ✅ Confirmaciones
  const confirm = (title, message) => {
    return new Promise((resolve) => {
      // Implementar modal de confirmación personalizado
      // Por ahora usar confirm nativo
      const result = window.confirm(`${title}\n\n${message}`)
      resolve(result)
    })
  }
  
  // 🔄 Debounce para búsquedas
  const debounce = (func, delay) => {
    let timeoutId
    return (...args) => {
      clearTimeout(timeoutId)
      timeoutId = setTimeout(() => func.apply(null, args), delay)
    }
  }
  
  // 📝 Validaciones
  const validateRequired = (value) => {
    return value !== null && value !== undefined && value !== ''
  }
  
  const validateEmail = (email) => {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return regex.test(email)
  }
  
  const validatePrice = (price) => {
    return !isNaN(price) && parseFloat(price) > 0
  }
  
  const validateComponentId = (id) => {
    const regex = /^[A-Z0-9]{2,10}$/
    return regex.test(id)
  }
  
  // 🎨 Utilidades de UI
  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
  
  const copyToClipboard = async (text) => {
    try {
      await navigator.clipboard.writeText(text)
      showAlert('success', 'Copiado al portapapeles')
      return true
    } catch (error) {
      showAlert('error', 'Error al copiar al portapapeles')
      return false
    }
  }
  
  return {
    // Alertas
    alerts: readonly(alerts),
    showAlert,
    closeAlert,
    
    // Formateo
    formatCurrency,
    formatDate,
    formatDateTime,
    
    // UI
    confirm,
    debounce,
    scrollToTop,
    copyToClipboard,
    
    // Validaciones
    validateRequired,
    validateEmail,
    validatePrice,
    validateComponentId
  }
}
```

---

## 🎨 Migración de TailwindCSS

### 📋 Configuración Migrada

```javascript
// tailwind.config.js
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Migrar colores exactos del HTML actual
        primary: {
          50: '#eff6ff',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
        },
        success: {
          500: '#10b981',
          600: '#059669',
        },
        warning: {
          500: '#f59e0b',
          600: '#d97706',
        },
        danger: {
          500: '#ef4444',
          600: '#dc2626',
        }
      },
      spacing: {
        '18': '4.5rem',
        '88': '22rem',
      },
      animation: {
        'fade-in': 'fadeIn 0.2s ease-in-out',
        'slide-up': 'slideUp 0.3s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        }
      }
    }
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/typography'),
  ],
}
```

### 🧩 Componentes CSS Reutilizables

```css
/* assets/styles/components.css */

/* Clases base para botones */
.btn-base {
  @apply font-medium rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2;
}

.btn-primary {
  @apply btn-base bg-primary-600 hover:bg-primary-700 text-white focus:ring-primary-500;
}

.btn-secondary {
  @apply btn-base bg-gray-200 hover:bg-gray-300 text-gray-900 focus:ring-gray-500;
}

.btn-danger {
  @apply btn-base bg-danger-500 hover:bg-danger-600 text-white focus:ring-danger-500;
}

/* Tamaños de botones */
.btn-sm {
  @apply px-3 py-1.5 text-sm;
}

.btn-md {
  @apply px-4 py-2 text-sm;
}

.btn-lg {
  @apply px-6 py-3 text-base;
}

/* Clases para formularios */
.form-field {
  @apply block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm 
         focus:outline-none focus:ring-primary-500 focus:border-primary-500;
}

.form-field-error {
  @apply border-danger-500 focus:ring-danger-500 focus:border-danger-500;
}

.form-label {
  @apply block text-sm font-medium text-gray-700 mb-1;
}

.form-error {
  @apply text-sm text-danger-600 mt-1;
}

/* Clases para tablas */
.table-container {
  @apply overflow-x-auto shadow ring-1 ring-black ring-opacity-5 rounded-lg;
}

.table-base {
  @apply min-w-full divide-y divide-gray-200;
}

.table-header {
  @apply bg-gray-50 px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider;
}

.table-cell {
  @apply px-6 py-4 whitespace-nowrap text-sm text-gray-900;
}

.table-row {
  @apply hover:bg-gray-50 transition-colors;
}

/* Clases para alertas */
.alert-base {
  @apply p-4 rounded-lg mb-4 flex items-center;
}

.alert-success {
  @apply alert-base bg-success-50 border border-success-200 text-success-800;
}

.alert-error {
  @apply alert-base bg-danger-50 border border-danger-200 text-danger-800;
}

.alert-warning {
  @apply alert-base bg-warning-50 border border-warning-200 text-warning-800;
}

.alert-info {
  @apply alert-base bg-blue-50 border border-blue-200 text-blue-800;
}

/* Animaciones */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active {
  transition: all 0.3s ease-out;
}

.slide-up-leave-active {
  transition: all 0.3s cubic-bezier(1.0, 0.5, 0.8, 1.0);
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(10px);
  opacity: 0;
}
```

---

## 📊 Métricas de Éxito

### 🎯 KPIs Técnicos
- **Reducción de líneas de código**: Objetivo 60-70%
- **Tiempo de carga inicial**: < 3 segundos
- **Performance Score**: > 90 (Lighthouse)
- **Bundle size**: < 500KB gzipped
- **Time to Interactive**: < 2 segundos

### 🔧 KPIs de Desarrollo
- **Tiempo para nuevas features**: Reducción 50%
- **Bugs en producción**: Reducción 30%
- **Tiempo de onboarding**: Reducción 40%
- **Satisfacción del desarrollador**: > 8/10

### 📈 KPIs de Usuario
- **Tiempo de respuesta UI**: < 100ms
- **Errores de formulario**: Reducción 40%
- **Tasa de conversión**: Mejora 20%
- **Satisfacción del usuario**: > 4.5/5

---

## 🚨 Riesgos y Mitigaciones

### ⚠️ Riesgos Técnicos

**1. Pérdida de funcionalidad durante migración**
- **Mitigación**: Migración incremental sección por sección
- **Plan B**: Mantener versión actual funcionando en paralelo

**2. Performance degraded**
- **Mitigación**: Implementar lazy loading y code splitting desde el inicio
- **Plan B**: Optimización post-migración con herramientas Vue

**3. Incompatibilidad con backend**
- **Mitigación**: Mantener exactamente la misma API client y endpoints
- **Plan B**: Adapters para compatibilidad

### 👥 Riesgos de Equipo

**4. Curva de aprendizaje Vue.js**
- **Mitigación**: Training sessions y documentación detallada
- **Plan B**: Pair programming y mentoring

**5. Resistencia al cambio**
- **Mitigación**: Demostrar beneficios con sección piloto
- **Plan B**: Migración más gradual y voluntary

### 📅 Riesgos de Proyecto

**6. Retrasos en cronograma**
- **Mitigación**: Buffer del 20% en estimaciones
- **Plan B**: Priorizar secciones críticas primero

**7. Scope creep**
- **Mitigación**: Mantener paridad funcional como objetivo principal
- **Plan B**: Postponer mejoras para fase 2

---

## 📚 Recursos y Referencias

### 📖 Documentación Oficial
- [Vue.js 3 Official Guide](https://vuejs.org/guide/)
- [Pinia State Management](https://pinia.vuejs.org/)
- [Vue Router](https://router.vuejs.org/)
- [TailwindCSS Documentation](https://tailwindcss.com/docs)

### 🛠️ Herramientas Recomendadas
- **IDE**: VS Code + Vetur extension
- **Testing**: Vitest + Vue Test Utils
- **Linting**: ESLint + Prettier
- **Build**: Vite (included in Vue 3)
- **Deployment**: Docker + Nginx

### 📝 Templates y Ejemplos
- [Vue 3 Enterprise Template](https://github.com/antfu/vitesse)
- [Pinia Best Practices](https://github.com/vuejs/pinia/tree/v2/packages/testing)
- [TailwindUI Components](https://tailwindui.com/components)

### 🎓 Training Resources
- [Vue Mastery](https://www.vuemastery.com/)
- [Vue School](https://vueschool.io/)
- [Pinia Course](https://vueschool.io/courses/pinia-the-enjoyable-vue-store)

---

## 🔄 Plan de Rollback

### 🚨 Criterios para Rollback
- Performance degradation > 20%
- Critical bugs affecting > 50% of features
- User satisfaction < 3/5
- Desarrollo bloqueado > 3 días

### 📋 Proceso de Rollback

**1. Rollback Inmediato (< 1 hora)**
```bash
# Revertir a imagen Docker anterior
docker-compose down
docker-compose up -d --force-recreate
```

**2. Rollback Planificado (< 1 día)**
- Identificar problemas específicos
- Fix crítico en versión Vue
- Re-deploy con fixes

**3. Rollback Completo (< 1 semana)**
- Revertir a versión HTML/JS vanilla
- Análisis post-mortem
- Plan de re-migración

---

## 🎯 Criterios de Aceptación

### ✅ Definition of Done - Sección Migrada

**Funcionalidad:**
- [ ] Todas las features de la sección original funcionan
- [ ] CRUD completo operativo
- [ ] Filtros y búsqueda funcionando
- [ ] Paginación implementada
- [ ] Validaciones de formulario activas
- [ ] Manejo de errores implementado

**Performance:**
- [ ] Tiempo de carga < 3 segundos
- [ ] Interacciones UI < 100ms response time
- [ ] No memory leaks detectados

**Quality:**
- [ ] Tests unitarios > 80% coverage
- [ ] No errores en consola
- [ ] Responsive design funcionando
- [ ] Accessibility standards met

**Documentation:**
- [ ] Componentes documentados
- [ ] Store documentado
- [ ] Props y eventos definidos

### ✅ Definition of Done - Proyecto Completo

**Technical:**
- [ ] Todas las secciones migradas y funcionando
- [ ] Performance goals achieved
- [ ] Bundle optimizado
- [ ] Docker image funcionando

**User Experience:**
- [ ] Paridad funcional 100%
- [ ] No degradación de UX
- [ ] Responsive en todos los dispositivos
- [ ] Loading states implementados

**Development:**
- [ ] Developer environment setup documentado
- [ ] Build process automatizado
- [ ] Testing suite completo
- [ ] Deployment pipeline funcionando

---

## 📞 Contacts y Support

### 👥 Equipo de Migración
- **Tech Lead**: [Nombre] - Arquitectura y decisiones técnicas
- **Vue.js Expert**: [Nombre] - Implementación y best practices
- **UI/UX**: [Nombre] - Migración de componentes y styling
- **QA Engineer**: [Nombre] - Testing y quality assurance

### 🆘 Escalation Path
1. **Daily blockers**: Tech Lead
2. **Architecture decisions**: Senior Architect
3. **Timeline issues**: Project Manager
4. **Budget concerns**: Product Owner

### 📅 Review Schedule
- **Daily standups**: 9:00 AM
- **Weekly progress review**: Viernes 4:00 PM
- **Milestone reviews**: Fin de cada semana
- **Retrospectives**: Fin de cada fase

---

## 📝 Change Log

| Fecha | Versión | Cambios | Autor |
|-------|---------|---------|-------|
| 2024-07-06 | 1.0 | Creación del plan inicial | [Autor] |
| | | Definición de arquitectura Vue.js 3 | |
| | | Cronograma detallado de 8 semanas | |
| | | Ejemplos de código de migración | |

---

## 📋 Apéndices

### A. Checklist de Setup
```bash
# A.1 Setup inicial del proyecto
□ npm create vue@latest portal-cotizador-vue
□ cd portal-cotizador-vue
□ npm install
□ npm install pinia @pinia/nuxt
□ npm install @tailwindcss/forms @tailwindcss/typography
□ npm install axios date-fns
□ npm install -D @vue/test-utils vitest jsdom
□ npm install -D eslint prettier @vue/eslint-config-prettier

# A.2 Configuración de desarrollo
□ Configurar TailwindCSS
□ Setup ESLint y Prettier
□ Configurar Vite para desarrollo
□ Setup VS Code extensions
□ Configurar Git hooks

# A.3 Estructura de carpetas
□ Crear estructura src/ completa
□ Setup de stores con Pinia
□ Configurar Vue Router
□ Setup de services y composables
```

### B. Code Style Guide
```javascript
// B.1 Naming Conventions
// - Componentes: PascalCase (ComponenteForm.vue)
// - Stores: camelCase (useComponentesStore)
// - Composables: camelCase (useUtils)
// - Variables: camelCase (currentComponent)
// - Constants: UPPER_SNAKE_CASE (API_ENDPOINTS)

// B.2 File Structure
// - Un componente por archivo
// - Props definidos con defineProps()
// - Emits definidos con defineEmits()
// - Imports organizados por categoría

// B.3 Store Structure
// - State reactivo con ref()
// - Computed con computed()
// - Actions async/await
// - Return object organized
```

### C. Testing Strategy
```javascript
// C.1 Unit Tests - Componentes
// - Props rendering
// - Events emission
// - User interactions
// - Conditional rendering

// C.2 Unit Tests - Stores
// - State mutations
// - Actions execution
// - Computed properties
// - Error handling

// C.3 Integration Tests
// - API calls
// - Store-component integration
// - Navigation flows
// - Form submissions
```

---

**📌 Nota Final**: Este documento es un living document que debe actualizarse según evolucione el proyecto. Cualquier cambio significativo en la arquitectura o cronograma debe ser documentado y comunicado al equipo.

**🎯 Objetivo**: Migrar exitosamente el portal-cotizador a Vue.js 3 manteniendo 100% de paridad funcional mientras mejoramos significativamente la maintainability, performance y developer experience.