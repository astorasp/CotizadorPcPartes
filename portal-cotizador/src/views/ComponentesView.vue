<template>
  <div class="space-y-6">
    <!-- Header de la página -->
    <div class="bg-white rounded-lg shadow p-6">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div class="flex items-center space-x-3">
            <h1 class="text-2xl font-bold text-gray-900">Gestión de Componentes</h1>
            <span 
              v-if="componentesStore.primaryRole"
              class="inline-flex px-2 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-800"
            >
              {{ getRoleDisplayName(componentesStore.primaryRole) }}
            </span>
          </div>
          <p class="mt-1 text-sm text-gray-600">
            Administrar componentes de hardware para PCs
          </p>
        </div>
        <div class="mt-4 sm:mt-0">
          <LoadingButton
            v-if="componentesStore.canCreateComponentes"
            @click="openCreateModal" 
            variant="primary"
            size="md"
            :loading="componentesStore.isFetching"
            loading-text="Cargando..."
            class="btn-primary btn-md"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Nuevo Componente
          </LoadingButton>
        </div>
      </div>
    </div>

    <!-- Filtros y búsqueda -->
    <div class="bg-white rounded-lg shadow p-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="form-label">Buscar componentes</label>
          <input
            type="text"
            placeholder="ID, descripción, marca..."
            class="form-field"
            v-model="searchTerm"
          />
        </div>
        
        <div>
          <label class="form-label">Filtrar por tipo</label>
          <select class="form-field" v-model="selectedType">
            <option value="">Todos los tipos</option>
            <option value="MONITOR">Monitor</option>
            <option value="DISCO_DURO">Disco Duro</option>
            <option value="TARJETA_VIDEO">Tarjeta de Video</option>
            <option value="PC">PC Completa</option>
          </select>
        </div>
        
        <div class="flex items-end">
          <LoadingButton
            @click="applyFilters" 
            variant="secondary"
            size="md"
            :loading="componentesStore.isFetching"
            loading-text="Aplicando..."
            class="btn-secondary btn-md w-full"
          >
            Aplicar Filtros
          </LoadingButton>
        </div>
        
        <div class="flex items-end">
          <LoadingButton
            @click="clearFilters" 
            variant="outline"
            size="md"
            :loading="componentesStore.isFetching"
            loading-text="Limpiando..."
            class="btn-outline btn-md w-full"
          >
            Limpiar
          </LoadingButton>
        </div>
      </div>
    </div>

    <!-- Tabla de componentes -->
    <div class="bg-white rounded-lg shadow">
      <div class="px-6 py-4 border-b border-gray-200">
        <h3 class="text-lg font-medium text-gray-900">
          Componentes ({{ totalItems }})
        </h3>
      </div>
      
      <!-- Estado de carga -->
      <div v-if="componentesStore.isFetching" class="p-8 text-center">
        <div class="inline-flex items-center px-4 py-2 text-sm text-gray-600">
          <LoadingSpinner size="sm" class="mr-3" />
          Cargando componentes...
        </div>
      </div>
      
      <!-- Estado vacío -->
      <div v-else-if="!hasData" class="p-8 text-center">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
        </svg>
        <h3 class="mt-2 text-sm font-medium text-gray-900">No hay componentes</h3>
        <p class="mt-1 text-sm text-gray-500">Comience creando un nuevo componente.</p>
        <div class="mt-6">
          <LoadingButton
            v-if="componentesStore.canCreateComponentes"
            @click="openCreateModal" 
            variant="primary"
            size="md"
            :loading="componentesStore.isFetching"
            loading-text="Cargando..."
            class="btn-primary btn-md"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Nuevo Componente
          </LoadingButton>
        </div>
      </div>
      
      <!-- Tabla con datos -->
      <div v-else class="table-container">
        <table class="table-base">
          <thead class="bg-gray-50">
            <tr>
              <th class="table-header">ID</th>
              <th class="table-header">Descripción</th>
              <th class="table-header">Marca</th>
              <th class="table-header">Modelo</th>
              <th class="table-header">Tipo</th>
              <th class="table-header">Precio</th>
              <th class="table-header">Acciones</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr 
              v-for="componente in paginatedComponents" 
              :key="componente.id"
              class="table-row"
            >
              <td class="table-cell font-mono text-xs">{{ componente.id }}</td>
              <td class="table-cell">
                <div class="font-medium">{{ componente.descripcion }}</div>
                <div v-if="componentesStore.getExtraFieldsDisplay(componente)" 
                     class="text-xs text-gray-400 mt-1">
                  {{ componentesStore.getExtraFieldsDisplay(componente) }}
                </div>
              </td>
              <td class="table-cell">{{ componente.marca }}</td>
              <td class="table-cell">{{ componente.modelo }}</td>
              <td class="table-cell">
                <span class="inline-flex px-2 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-800">
                  {{ getTypeLabel(componente.tipoComponente) }}
                </span>
              </td>
              <td class="table-cell">
                <div class="text-sm text-gray-900">
                  <div v-if="componente.costo">Costo: {{ formatCurrency(componente.costo) }}</div>
                  <div>Precio: {{ formatCurrency(componente.precioBase) }}</div>
                </div>
              </td>
              <td class="table-cell">
                <div class="flex space-x-2">
                  <LoadingButton
                    @click="openDetailModal(componente)"
                    variant="ghost"
                    size="xs"
                    :loading="componentesStore.isFetching"
                    loading-text="Cargando..."
                    class="text-blue-600 hover:text-blue-700 text-sm font-medium"
                    title="Ver detalles"
                  >
                    Ver
                  </LoadingButton>
                  <LoadingButton
                    v-if="componentesStore.canEditComponentes"
                    @click="openEditModal(componente)"
                    variant="ghost"
                    size="xs"
                    :loading="componentesStore.isUpdating"
                    loading-text="Actualizando..."
                    class="text-primary-600 hover:text-primary-700 text-sm font-medium"
                    title="Editar componente"
                  >
                    Editar
                  </LoadingButton>
                  <LoadingButton
                    v-if="componentesStore.canDeleteComponentes"
                    @click="handleDelete(componente)"
                    variant="ghost"
                    size="xs"
                    :loading="componentesStore.isDeleting"
                    loading-text="Eliminando..."
                    class="text-danger-600 hover:text-danger-700 text-sm font-medium"
                    title="Eliminar componente"
                  >
                    Eliminar
                  </LoadingButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Paginación -->
      <div v-if="hasData" class="px-6 py-4 border-t border-gray-200">
        <div class="flex items-center justify-between">
          <div class="text-sm text-gray-700">
            Mostrando {{ startItem }} a {{ endItem }} de {{ totalItems }} resultados
          </div>
          <div class="flex space-x-2">
            <LoadingButton
              :disabled="!canGoPrevious"
              @click="componentesStore.goToPreviousPage()"
              variant="outline"
              size="sm"
              :loading="componentesStore.isFetching"
              loading-text="Cargando..."
              class="btn-outline btn-sm"
              :class="{ 'opacity-50 cursor-not-allowed': !canGoPrevious }"
            >
              Anterior
            </LoadingButton>
            <LoadingButton
              v-for="page in visiblePages" 
              :key="page"
              @click="handlePageChange(page)"
              :variant="page === currentPage ? 'primary' : 'outline'"
              size="sm"
              :loading="componentesStore.isFetching"
              loading-text="..."
              class="btn-sm"
            >
              {{ page }}
            </LoadingButton>
            <LoadingButton
              :disabled="!canGoNext"
              @click="componentesStore.goToNextPage()"
              variant="outline"
              size="sm"
              :loading="componentesStore.isFetching"
              loading-text="Cargando..."
              class="btn-outline btn-sm"
              :class="{ 'opacity-50 cursor-not-allowed': !canGoNext }"
            >
              Siguiente
            </LoadingButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <CreateComponenteModal
      v-if="componentesStore.showCreateModal"
      :show="componentesStore.showCreateModal"
      @close="componentesStore.closeCreateModal()"
    />

    <!-- Detail Modal -->
    <ComponenteDetailModal
      v-if="componentesStore.showDetailModal"
      :show="componentesStore.showDetailModal"
      :loading="componentesStore.isFetching"
      :componente="componentesStore.currentComponente"
      @close="componentesStore.closeDetailModal()"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useComponentesStore } from '@/stores/useComponentesStore'
import { useUtils } from '@/composables/useUtils'
import { COMPONENT_TYPE_LABELS } from '@/utils/constants'

// Modal components
import CreateComponenteModal from '@/components/componentes/CreateComponenteModal.vue'
import ComponenteDetailModal from '@/components/componentes/ComponenteDetailModal.vue'

// Loading components
import LoadingButton from '@/components/ui/LoadingButton.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

// Composables y stores
const componentesStore = useComponentesStore()
const { formatCurrency, confirm } = useUtils()

// Estado del store
const {
  paginatedComponents,
  hasFilteredComponents,
  paginationInfo,
  canGoPrevious,
  canGoNext,
  pagination,
  filters
} = storeToRefs(componentesStore)

// Estado local para los inputs
const searchTerm = ref('')
const selectedType = ref('')

// Computed properties
const hasData = computed(() => hasFilteredComponents.value)
const totalItems = computed(() => paginationInfo.value.totalItems)
const totalPages = computed(() => pagination.value.totalPages)
const startItem = computed(() => paginationInfo.value.startItem)
const endItem = computed(() => paginationInfo.value.endItem)
const currentPage = computed(() => pagination.value.currentPage)

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

// Métodos
const getTypeLabel = (tipo) => {
  return COMPONENT_TYPE_LABELS[tipo] || tipo
}

const getRoleDisplayName = (role) => {
  const roleNames = {
    'ADMIN': 'Administrador',
    'GERENTE': 'Gerente',
    'VENDEDOR': 'Vendedor',
    'INVENTARIO': 'Inventario',
    'CONSULTOR': 'Consultor'
  }
  return roleNames[role] || role
}

const handleSearch = () => {
  componentesStore.setSearchFilter(searchTerm.value)
}

const handleTypeFilter = () => {
  componentesStore.setTypeFilter(selectedType.value)
}

const applyFilters = () => {
  componentesStore.setSearchFilter(searchTerm.value)
  componentesStore.setTypeFilter(selectedType.value)
}

const clearFilters = () => {
  searchTerm.value = ''
  selectedType.value = ''
  componentesStore.clearFilters()
}

const handlePageChange = (page) => {
  componentesStore.goToPage(page)
}

const openCreateModal = () => {
  componentesStore.openCreateModal()
}

const openDetailModal = (componente) => {
  componentesStore.openDetailModal(componente)
}

const openEditModal = (componente) => {
  componentesStore.openEditModal(componente)
}

const handleDelete = async (componente) => {
  const confirmed = await confirm(
    'Eliminar Componente',
    `¿Está seguro de que desea eliminar el componente "${componente.descripcion}"?`
  )
  
  if (confirmed) {
    await componentesStore.deleteComponente(componente.id)
  }
}

// Watchers para sincronizar filtros automáticamente
watch(searchTerm, (newValue) => {
  // Debounce la búsqueda automática
  clearTimeout(searchTerm._timeout)
  searchTerm._timeout = setTimeout(() => {
    componentesStore.setSearchFilter(newValue)
  }, 300)
})

watch(selectedType, (newValue) => {
  componentesStore.setTypeFilter(newValue)
})

// Lifecycle
onMounted(() => {
  componentesStore.fetchComponentes()
})
</script>