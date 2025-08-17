<template>
  <div class="space-y-6">
    <!-- Header de la página -->
    <div class="bg-white rounded-lg shadow p-6">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div class="flex items-center space-x-3">
            <h1 class="text-2xl font-bold text-gray-900">Gestión de Cotizaciones</h1>
            <span 
              v-if="cotizacionesStore.primaryRole"
              class="inline-flex px-2 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-800"
            >
              {{ getRoleDisplayName(cotizacionesStore.primaryRole) }}
            </span>
          </div>
          <p class="mt-1 text-sm text-gray-600">
            Crear y administrar cotizaciones de hardware
          </p>
        </div>
        <div class="mt-4 sm:mt-0">
          <LoadingButton 
            v-if="cotizacionesStore.canCreateCotizaciones"
            @click="openCreateModal" 
            :loading="cotizacionesStore.isCreating"
            text="Nueva Cotización"
            loading-text="Creando..."
            variant="primary"
            size="md"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Nueva Cotización
          </LoadingButton>
        </div>
      </div>
    </div>

    <!-- Filtros y búsqueda -->
    <div class="bg-white rounded-lg shadow p-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="form-label">Buscar cotizaciones</label>
          <input
            type="text"
            placeholder="Folio, tipo de cotizador..."
            class="form-field"
            v-model="searchTerm"
          />
        </div>
        
        <div>
          <label class="form-label">Filtrar por fecha</label>
          <input
            type="date"
            class="form-field"
            v-model="selectedDate"
          />
        </div>
        
        <div class="flex items-end">
          <LoadingButton 
            @click="applyFilters" 
            :loading="cotizacionesStore.isFetching"
            text="Aplicar Filtros"
            loading-text="Filtrando..."
            variant="secondary"
            size="md"
            full-width
          >
            Aplicar Filtros
          </LoadingButton>
        </div>
        
        <div class="flex items-end">
          <LoadingButton 
            @click="clearFilters" 
            :loading="cotizacionesStore.isFetching"
            text="Limpiar"
            loading-text="Limpiando..."
            variant="outline"
            size="md"
            full-width
          >
            Limpiar
          </LoadingButton>
        </div>
      </div>
    </div>

    <!-- Tabla de cotizaciones -->
    <div class="bg-white rounded-lg shadow">
      <div class="px-6 py-4 border-b border-gray-200">
        <h3 class="text-lg font-medium text-gray-900">
          Cotizaciones ({{ totalItems }})
        </h3>
      </div>
      
      <!-- Estado de carga -->
      <div v-if="cotizacionesStore.isFetching" class="p-8 text-center">
        <LoadingSpinner 
          size="lg"
          message="Cargando cotizaciones..."
          color="primary"
          centered
        />
      </div>
      
      <!-- Estado vacío -->
      <div v-else-if="!hasData && !cotizacionesStore.isFetching" class="p-8 text-center">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <h3 class="mt-2 text-sm font-medium text-gray-900">No hay cotizaciones</h3>
        <p class="mt-1 text-sm text-gray-500">Comience creando una nueva cotización.</p>
        <div class="mt-6">
          <LoadingButton 
            v-if="cotizacionesStore.canCreateCotizaciones"
            @click="openCreateModal" 
            :loading="cotizacionesStore.isCreating"
            text="Nueva Cotización"
            loading-text="Creando..."
            variant="primary"
            size="md"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Nueva Cotización
          </LoadingButton>
        </div>
      </div>
      
      <!-- Tabla con datos -->
      <div v-else-if="hasData && !cotizacionesStore.isFetching" class="table-container">
        <table class="table-base">
          <thead class="bg-gray-50">
            <tr>
              <th class="table-header">Folio</th>
              <th class="table-header">Fecha</th>
              <th class="table-header">Componentes</th>
              <th v-if="cotizacionesStore.canViewCotizacionCosts" class="table-header">Costos</th>
              <th class="table-header">Totales</th>
              <th class="table-header">Acciones</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr 
              v-for="cotizacion in paginatedCotizaciones" 
              :key="cotizacion.folio || cotizacion.id"
              class="table-row"
            >
              <td class="table-cell">
                <div class="font-medium">#{{ getCotizacionSummary(cotizacion).id }}</div>
                <div class="text-sm text-gray-500">{{ getCotizacionSummary(cotizacion).tipoCotizador }}</div>
              </td>
              <td class="table-cell">
                <div class="text-sm text-gray-900">
                  {{ formatDate(getCotizacionSummary(cotizacion).fecha) }}
                </div>
              </td>
              <td class="table-cell">
                <div class="text-sm text-gray-900">
                  <span 
                    :class="getComponentsBadgeClass(getCotizacionSummary(cotizacion).totalComponents)"
                    class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  >
                    {{ getCotizacionSummary(cotizacion).totalComponents }} componente{{ getCotizacionSummary(cotizacion).totalComponents !== 1 ? 's' : '' }}
                  </span>
                </div>
                <div class="text-xs text-gray-500 mt-1">
                  Impuestos aplicados
                </div>
              </td>
              <td v-if="cotizacionesStore.canViewCotizacionCosts" class="table-cell">
                <div class="text-sm text-gray-900">
                  <div>Costo: {{ formatCurrency(getCotizacionSummary(cotizacion).subtotal * 0.7) }}</div>
                  <div v-if="cotizacionesStore.canViewCotizacionMargins" class="text-green-600">
                    Margen: {{ formatCurrency(getCotizacionSummary(cotizacion).subtotal * 0.3) }}
                  </div>
                </div>
              </td>
              <td class="table-cell">
                <div class="text-sm font-medium text-gray-900">
                  <div>Subtotal: {{ formatCurrency(getCotizacionSummary(cotizacion).subtotal) }}</div>
                  <div class="text-warning-600">Impuestos: {{ formatCurrency(getCotizacionSummary(cotizacion).impuestos) }}</div>
                  <div class="text-lg font-bold text-success-600">Total: {{ formatCurrency(getCotizacionSummary(cotizacion).total) }}</div>
                </div>
              </td>
              <td class="table-cell">
                <div class="flex flex-wrap space-x-2">
                  <LoadingButton 
                    v-if="cotizacionesStore.canViewCotizacionDetails"
                    @click="handleOpenViewModal(getCotizacionSummary(cotizacion).id)"
                    :loading="cotizacionesStore.isFetching"
                    text="Ver detalles"
                    loading-text="Cargando..."
                    variant="ghost"
                    size="sm"
                    class="text-primary-600 hover:text-primary-700 text-sm font-medium"
                    title="Ver detalles"
                  >
                    Ver detalles
                  </LoadingButton>
                  <LoadingButton 
                    v-if="cotizacionesStore.canEditCotizaciones"
                    @click="handleEdit(cotizacion)"
                    :loading="cotizacionesStore.isUpdating"
                    text="Editar"
                    loading-text="Editando..."
                    variant="ghost"
                    size="sm"
                    class="text-green-600 hover:text-green-700 text-sm font-medium"
                    title="Editar cotización"
                  >
                    Editar
                  </LoadingButton>
                  <LoadingButton 
                    v-if="cotizacionesStore.canConvertCotizacionToOrder"
                    @click="handleConvertToOrder(cotizacion)"
                    :loading="cotizacionesStore.isUpdating"
                    text="A Pedido"
                    loading-text="Convirtiendo..."
                    variant="ghost"
                    size="sm"
                    class="text-blue-600 hover:text-blue-700 text-sm font-medium"
                    title="Convertir a pedido"
                  >
                    A Pedido
                  </LoadingButton>
                  <LoadingButton 
                    v-if="cotizacionesStore.canExportCotizaciones"
                    @click="handleExport(cotizacion)"
                    :loading="cotizacionesStore.isFetching"
                    text="Exportar"
                    loading-text="Exportando..."
                    variant="ghost"
                    size="sm"
                    class="text-purple-600 hover:text-purple-700 text-sm font-medium"
                    title="Exportar"
                  >
                    Exportar
                  </LoadingButton>
                  <LoadingButton 
                    v-if="cotizacionesStore.canDeleteCotizaciones"
                    @click="handleDelete(cotizacion)"
                    :loading="cotizacionesStore.isDeleting"
                    text="Eliminar"
                    loading-text="Eliminando..."
                    variant="ghost"
                    size="sm"
                    class="text-danger-600 hover:text-danger-700 text-sm font-medium"
                    title="Eliminar cotización"
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
      <div v-if="hasData && !cotizacionesStore.isFetching" class="px-6 py-4 border-t border-gray-200">
        <div class="flex items-center justify-between">
          <div class="text-sm text-gray-700">
            Mostrando {{ startItem }} a {{ endItem }} de {{ totalItems }} resultados
          </div>
          <div class="flex space-x-2">
            <LoadingButton 
              :disabled="!canGoPrevious"
              @click="cotizacionesStore.goToPreviousPage()"
              :loading="cotizacionesStore.isFetching"
              text="Anterior"
              loading-text="Cargando..."
              variant="outline"
              size="sm"
              :class="{ 'opacity-50 cursor-not-allowed': !canGoPrevious }"
            >
              Anterior
            </LoadingButton>
            <LoadingButton 
              v-for="page in visiblePages" 
              :key="page"
              @click="handlePageChange(page)"
              :loading="cotizacionesStore.isFetching"
              :text="page.toString()"
              loading-text="..."
              :variant="page === currentPage ? 'primary' : 'outline'"
              size="sm"
            >
              {{ page }}
            </LoadingButton>
            <LoadingButton 
              :disabled="!canGoNext"
              @click="cotizacionesStore.goToNextPage()"
              :loading="cotizacionesStore.isFetching"
              text="Siguiente"
              loading-text="Cargando..."
              variant="outline"
              size="sm"
              :class="{ 'opacity-50 cursor-not-allowed': !canGoNext }"
            >
              Siguiente
            </LoadingButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de crear cotización -->
    <CreateCotizacionModal />
    
    <!-- Modal de ver cotización -->
    <ViewCotizacionModal />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useCotizacionesStore } from '@/stores/useCotizacionesStore'
import { useUtils } from '@/composables/useUtils'
import CreateCotizacionModal from '@/components/cotizaciones/CreateCotizacionModal.vue'
import ViewCotizacionModal from '@/components/cotizaciones/ViewCotizacionModal.vue'
import LoadingButton from '@/components/ui/LoadingButton.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

// Composables y stores
const cotizacionesStore = useCotizacionesStore()
const { formatCurrency, formatDate, confirm } = useUtils()

// Estado del store
const {
  paginatedCotizaciones,
  hasFilteredCotizaciones,
  paginationInfo,
  canGoPrevious,
  canGoNext,
  pagination,
  filters
} = storeToRefs(cotizacionesStore)

// Estado local para los inputs
const searchTerm = ref('')
const selectedDate = ref('')

// Computed properties
const hasData = computed(() => hasFilteredCotizaciones.value)
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
const getComponentsBadgeClass = (count) => {
  return count > 0 
    ? 'bg-green-100 text-green-800' 
    : 'bg-gray-100 text-gray-800'
}

const getCotizacionSummary = (cotizacion) => {
  return cotizacionesStore.getCotizacionSummary(cotizacion)
}

const applyFilters = () => {
  cotizacionesStore.setSearchFilter(searchTerm.value)
  cotizacionesStore.setDateFilter(selectedDate.value)
}

const clearFilters = () => {
  searchTerm.value = ''
  selectedDate.value = ''
  cotizacionesStore.clearFilters()
}

const handlePageChange = (page) => {
  cotizacionesStore.goToPage(page)
}

const openCreateModal = async () => {
  await cotizacionesStore.openCreateModal()
}

const handleOpenViewModal = async (cotizacionId) => {
  await cotizacionesStore.openViewModal(cotizacionId)
}

const handleDelete = async (cotizacion) => {
  const summary = getCotizacionSummary(cotizacion)
  const confirmed = await confirm(
    'Eliminar Cotización',
    `¿Está seguro de que desea eliminar la cotización #${summary.id}?`
  )
  
  if (confirmed) {
    await cotizacionesStore.deleteCotizacion(summary.id)
  }
}

const handleEdit = (cotizacion) => {
  // TODO: Implementar edición de cotización
  const summary = getCotizacionSummary(cotizacion)
  // console.log('Editar cotización:', summary.id)
  // Aquí se abriría el modal de edición
}

const handleConvertToOrder = (cotizacion) => {
  // TODO: Implementar conversión a pedido
  const summary = getCotizacionSummary(cotizacion)
  // console.log('Convertir a pedido:', summary.id)
  // Aquí se realizaría la conversión a pedido
}

const handleExport = (cotizacion) => {
  // TODO: Implementar exportación
  const summary = getCotizacionSummary(cotizacion)
  // console.log('Exportar cotización:', summary.id)
  // Aquí se implementaría la exportación
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

// Watchers para sincronizar filtros automáticamente
watch(searchTerm, (newValue) => {
  // Debounce la búsqueda automática
  clearTimeout(searchTerm._timeout)
  searchTerm._timeout = setTimeout(() => {
    cotizacionesStore.setSearchFilter(newValue)
  }, 300)
})

watch(selectedDate, (newValue) => {
  cotizacionesStore.setDateFilter(newValue)
})

// Lifecycle
onMounted(async () => {
  await cotizacionesStore.fetchCotizaciones()
})
</script>