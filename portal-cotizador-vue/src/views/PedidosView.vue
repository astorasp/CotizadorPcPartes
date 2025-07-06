<template>
  <div class="container mx-auto px-4 py-8">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-2">Pedidos</h1>
      <p class="text-gray-600">Gestión y generación de pedidos desde cotizaciones</p>
    </div>

    <!-- Controles superiores -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
      <div class="flex flex-col lg:flex-row gap-4 items-start lg:items-center justify-between">
        <!-- Filtros -->
        <div class="flex flex-col sm:flex-row gap-3 flex-1">
          <div class="relative flex-1">
            <MagnifyingGlassIcon class="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              v-model="searchTerm"
              type="text"
              placeholder="Buscar por número, proveedor..."
              class="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              @input="debouncedSearch"
            >
          </div>
          
          <!-- Filtro de fecha desde -->
          <input
            v-model="fechaDesde"
            type="date"
            class="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            @change="handleDateFilter"
          >
        </div>

        <!-- Botones de acción -->
        <div class="flex gap-2">
          <button
            @click="clearLocalFilters"
            class="px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
          >
            <XMarkIcon class="h-5 w-5 inline mr-2" />
            Limpiar
          </button>
          <button
            @click="openCreateModal"
            class="px-4 py-2 bg-primary-600 text-white hover:bg-primary-700 rounded-lg transition-colors"
          >
            <PlusIcon class="h-5 w-5 inline mr-2" />
            Nuevo Pedido
          </button>
        </div>
      </div>
    </div>

    <!-- Estados de carga -->
    <div v-if="tableLoading" class="bg-white rounded-lg shadow-sm border border-gray-200 p-8">
      <div class="text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
        <p class="text-gray-600">Cargando pedidos...</p>
      </div>
    </div>

    <!-- Estado vacío -->
    <div v-else-if="!hasFilteredPedidos" class="bg-white rounded-lg shadow-sm border border-gray-200 p-8">
      <div class="text-center">
        <DocumentTextIcon class="h-16 w-16 text-gray-400 mx-auto mb-4" />
        <h3 class="text-lg font-medium text-gray-900 mb-2">
          {{ hasPedidos ? 'No se encontraron resultados' : 'No hay pedidos registrados' }}
        </h3>
        <p class="text-gray-600 mb-6">
          {{ hasPedidos ? 'Intente con otros términos de búsqueda o filtros' : 'Genere su primer pedido desde una cotización existente' }}
        </p>
        <button
          v-if="!hasPedidos"
          @click="openCreateModal"
          class="px-4 py-2 bg-primary-600 text-white hover:bg-primary-700 rounded-lg transition-colors"
        >
          <PlusIcon class="h-5 w-5 inline mr-2" />
          Generar Primer Pedido
        </button>
      </div>
    </div>

    <!-- Tabla desktop -->
    <div v-else class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
      <!-- Tabla -->
      <div class="hidden md:block">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Pedido
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Fechas
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Detalles
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Total
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Acciones
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr
              v-for="pedido in paginatedPedidos"
              :key="pedido.numPedido"
              class="hover:bg-gray-50"
            >
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div>
                    <div class="text-sm font-medium text-gray-900">#{{ pedido.numPedido }}</div>
                    <div class="text-sm text-gray-500">{{ pedido.nombreProveedor }}</div>
                    <div class="text-xs text-gray-400">{{ pedido.cveProveedor }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-900">
                  <div><strong>Emisión:</strong> {{ formatDate(pedido.fechaEmision) }}</div>
                  <div><strong>Entrega:</strong> {{ formatDate(pedido.fechaEntrega) }}</div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-900">
                  <div><strong>Líneas:</strong> {{ pedido.totalDetalles || 0 }}</div>
                  <div><strong>Surtido:</strong> {{ pedido.nivelSurtido }}%</div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-lg font-bold text-green-600">{{ formatCurrency(pedido.total) }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <button
                  @click="openDetailModal(pedido.numPedido)"
                  class="inline-flex items-center px-3 py-1 border border-transparent text-xs font-medium rounded text-white bg-blue-600 hover:bg-blue-700"
                  :disabled="loading"
                >
                  <EyeIcon class="h-4 w-4 mr-1" />
                  Ver Detalle
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Cards móvil -->
      <div class="md:hidden">
        <div class="space-y-4 p-4">
          <div
            v-for="pedido in paginatedPedidos"
            :key="pedido.numPedido"
            class="bg-white p-4 rounded-lg border border-gray-200 shadow-sm"
          >
            <div class="flex items-center justify-between mb-3">
              <div class="text-lg font-semibold text-gray-900">#{{ pedido.numPedido }}</div>
              <div class="text-lg font-bold text-green-600">{{ formatCurrency(pedido.total) }}</div>
            </div>
            
            <div class="space-y-2 mb-4">
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">Proveedor:</span>
                <span class="text-gray-900">{{ pedido.nombreProveedor }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">Emisión:</span>
                <span class="text-gray-900">{{ formatDate(pedido.fechaEmision) }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">Entrega:</span>
                <span class="text-gray-900">{{ formatDate(pedido.fechaEntrega) }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">Líneas / Surtido:</span>
                <span class="text-gray-900">{{ pedido.totalDetalles || 0 }} / {{ pedido.nivelSurtido }}%</span>
              </div>
            </div>
            
            <div class="flex justify-end">
              <button
                @click="openDetailModal(pedido.numPedido)"
                class="inline-flex items-center px-3 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                :disabled="loading"
              >
                <EyeIcon class="h-5 w-5 mr-2" />
                Ver Detalle
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Paginación -->
      <div v-if="paginatedPedidos.length > 0" class="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
        <div class="flex-1 flex justify-between sm:hidden">
          <button
            @click="goToPreviousPage"
            :disabled="!canGoPrevious"
            class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Anterior
          </button>
          <button
            @click="goToNextPage"
            :disabled="!canGoNext"
            class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Siguiente
          </button>
        </div>
        <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
          <div class="flex items-center space-x-4">
            <p class="text-sm text-gray-700">
              {{ paginationInfo.text }}
            </p>
            <select
              :value="pagination.pageSize"
              @change="setPageSize(parseInt($event.target.value))"
              class="px-3 py-1 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            >
              <option :value="5">5 por página</option>
              <option :value="10">10 por página</option>
              <option :value="20">20 por página</option>
              <option :value="30">30 por página</option>
            </select>
          </div>
          <div>
            <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
              <button
                @click="goToPreviousPage"
                :disabled="!canGoPrevious"
                class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <ChevronLeftIcon class="h-5 w-5" aria-hidden="true" />
              </button>
              <button
                v-for="page in visiblePages"
                :key="page"
                @click="goToPage(page)"
                :class="[
                  'relative inline-flex items-center px-4 py-2 border text-sm font-medium',
                  page === pagination.currentPage
                    ? 'z-10 bg-primary-50 border-primary-500 text-primary-600'
                    : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                ]"
              >
                {{ page }}
              </button>
              <button
                @click="goToNextPage"
                :disabled="!canGoNext"
                class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <ChevronRightIcon class="h-5 w-5" aria-hidden="true" />
              </button>
            </nav>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de creación de pedido -->
    <CreatePedidoModal
      :show="showCreateModal"
      :loading="modalLoading"
      :form-data="formData"
      :is-form-valid="isFormValid"
      :available-cotizaciones="availableCotizaciones"
      :available-proveedores="availableProveedores"
      :selected-cotizacion="selectedCotizacion"
      :selected-proveedor="selectedProveedor"
      :pedido-preview="pedidoPreview"
      @close="closeCreateModal"
      @submit="submitPedido"
      @cotizacion-change="handleCotizacionChange"
      @proveedor-change="handleProveedorChange"
      @nivel-surtido-change="handleNivelSurtidoChange"
    />

    <!-- Modal de detalles de pedido -->
    <PedidoDetailModal
      :show="showDetailModal"
      :loading="modalLoading"
      :pedido="currentPedido"
      @close="closeDetailModal"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { usePedidosStore } from '@/stores/usePedidosStore'
import { useAuthStore } from '@/stores/useAuthStore'
import { useUtils } from '@/composables/useUtils'
import { pedidosApi } from '@/services/pedidosApi'
import CreatePedidoModal from '@/components/pedidos/CreatePedidoModal.vue'
import PedidoDetailModal from '@/components/pedidos/PedidoDetailModal.vue'
import {
  MagnifyingGlassIcon,
  PlusIcon,
  XMarkIcon,
  DocumentTextIcon,
  EyeIcon,
  ChevronLeftIcon,
  ChevronRightIcon
} from '@heroicons/vue/24/outline'

const pedidosStore = usePedidosStore()
const authStore = useAuthStore()
const { debounce } = useUtils()

// Estados locales para la vista
const searchTerm = ref('')
const fechaDesde = ref('')

// Estado reactivo del store
const {
  // Estado
  pedidos,
  filteredPedidos,
  currentPedido,
  loading,
  tableLoading,
  modalLoading,
  showCreateModal,
  showDetailModal,
  formData,
  selectedCotizacion,
  selectedProveedor,
  pedidoPreview,
  pagination,
  filters,
  
  // Computed
  hasPedidos,
  hasFilteredPedidos,
  paginatedPedidos,
  paginationInfo,
  canGoPrevious,
  canGoNext,
  isFormValid,
  availableCotizaciones,
  availableProveedores
} = storeToRefs(pedidosStore)

// Actions del store
const {
  initialize,
  openCreateModal,
  openDetailModal,
  closeCreateModal,
  closeDetailModal,
  submitPedido,
  handleCotizacionChange,
  handleProveedorChange,
  handleNivelSurtidoChange,
  clearFilters,
  setSearchFilter,
  setDateFilter,
  goToPage,
  setPageSize,
  goToPreviousPage,
  goToNextPage
} = pedidosStore

// Páginas visibles para paginación
const visiblePages = computed(() => {
  const maxVisiblePages = 5
  const totalPages = pagination.totalPages
  const currentPage = pagination.currentPage
  
  if (totalPages <= maxVisiblePages) {
    return Array.from({ length: totalPages }, (_, i) => i + 1)
  }
  
  let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2))
  let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1)
  
  if (endPage - startPage + 1 < maxVisiblePages) {
    startPage = Math.max(1, endPage - maxVisiblePages + 1)
  }
  
  return Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i)
})

// Búsqueda con debounce
const debouncedSearch = debounce(() => {
  setSearchFilter(searchTerm.value)
}, 300)

// Manejo del filtro de fecha
const handleDateFilter = () => {
  setDateFilter(fechaDesde.value)
}

// Limpiar filtros locales
const clearLocalFilters = () => {
  searchTerm.value = ''
  fechaDesde.value = ''
  clearFilters()
}

// Formateo de datos
const formatDate = (dateString) => {
  return pedidosApi.formatDate(dateString)
}

const formatCurrency = (amount) => {
  return pedidosApi.formatCurrency(amount)
}

// Watchers
watch(() => filters.searchTerm, (newTerm) => {
  searchTerm.value = newTerm
})

watch(() => filters.fechaDesde, (newDate) => {
  fechaDesde.value = newDate
})

// Lifecycle
onMounted(async () => {
  // Esperar a que el authStore esté inicializado
  if (authStore.isLoggedIn) {
    await initialize()
  }
})

// Watcher para cargar datos cuando el usuario se autentique
watch(() => authStore.isLoggedIn, async (isLoggedIn) => {
  if (isLoggedIn) {
    await initialize()
  }
}, { immediate: true })
</script>
