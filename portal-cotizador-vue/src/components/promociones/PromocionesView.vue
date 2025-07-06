<template>
  <!-- Migración completa de promociones.html - Gestión de Promociones -->
  <div class="space-y-6">
    <!-- Header Section -->
    <div class="bg-white rounded-lg shadow-sm p-6">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Promociones</h1>
          <p class="mt-1 text-sm text-gray-600">
            Gestión completa de promociones con tipos complejos
          </p>
        </div>
        <div class="mt-4 sm:mt-0">
          <button
            @click="openCreateModal()"
            class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
          >
            <PlusIcon class="h-4 w-4 mr-2" />
            Nueva Promoción
          </button>
        </div>
      </div>
    </div>

    <!-- Filters Section -->
    <div class="bg-white rounded-lg shadow-sm p-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <!-- Búsqueda -->
        <div class="space-y-1">
          <label class="block text-sm font-medium text-gray-700">
            Buscar promoción
          </label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <MagnifyingGlassIcon class="h-5 w-5 text-gray-400" />
            </div>
            <input
              v-model="searchTerm"
              @input="handleSearch"
              type="text"
              placeholder="Buscar por nombre o descripción..."
              class="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-primary-500 focus:border-primary-500"
            />
          </div>
        </div>

        <!-- Filtro por Estado -->
        <div class="space-y-1">
          <label class="block text-sm font-medium text-gray-700">
            Estado
          </label>
          <select
            v-model="estadoFilter"
            @change="handleEstadoFilter"
            class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
          >
            <option value="">Todos los estados</option>
            <option
              v-for="estado in estadosPromocion"
              :key="estado.value"
              :value="estado.value"
            >
              {{ estado.label }}
            </option>
          </select>
        </div>

        <!-- Acciones de filtro -->
        <div class="flex items-end space-x-2">
          <button
            @click="clearFilters"
            class="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
          >
            Limpiar
          </button>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="bg-white rounded-lg shadow-sm p-8">
      <div class="text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto mb-4"></div>
        <p class="text-gray-600">Cargando promociones...</p>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="!hasPromociones" class="bg-white rounded-lg shadow-sm p-8">
      <div class="text-center">
        <TagIcon class="h-12 w-12 mx-auto mb-4 text-gray-300" />
        <h3 class="text-lg font-medium text-gray-900 mb-2">No hay promociones</h3>
        <p class="text-gray-500 mb-4">Comienza creando tu primera promoción</p>
        <button
          @click="openCreateModal()"
          class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
        >
          <PlusIcon class="h-4 w-4 mr-2" />
          Crear Primera Promoción
        </button>
      </div>
    </div>

    <!-- Results Section -->
    <div v-else class="space-y-4">
      <!-- Results Summary -->
      <div class="bg-white rounded-lg shadow-sm p-4">
        <div class="flex items-center justify-between">
          <div class="text-sm text-gray-700">
            {{ paginationInfo.text }}
          </div>
          <div class="flex items-center space-x-2">
            <label class="text-sm text-gray-700">Mostrar:</label>
            <select
              :value="pagination.pageSize"
              @change="handlePageSizeChange"
              class="border border-gray-300 rounded px-2 py-1 text-sm"
            >
              <option value="5">5</option>
              <option value="10">10</option>
              <option value="20">20</option>
              <option value="30">30</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Desktop Table -->
      <div class="hidden lg:block bg-white rounded-lg shadow-sm overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Promoción
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tipo
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Vigencia
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Estado
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Acciones
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr
              v-for="promocion in paginatedPromociones"
              :key="promocion.idPromocion"
              class="hover:bg-gray-50"
            >
              <td class="px-6 py-4">
                <div class="flex flex-col">
                  <div class="text-sm font-medium text-gray-900">
                    {{ promocion.nombre }}
                  </div>
                  <div class="text-sm text-gray-500">
                    {{ truncateText(promocion.descripcion, 50) }}
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                  {{ getTipoDisplayName(promocion.tipoPromocionPrincipal) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                <div class="flex flex-col">
                  <span>{{ formatDate(promocion.vigenciaDesde) }}</span>
                  <span class="text-gray-500">{{ formatDate(promocion.vigenciaHasta) }}</span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span 
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :class="getEstadoBadge(promocion.estadoVigencia).class"
                >
                  {{ getEstadoBadge(promocion.estadoVigencia).text }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <div class="flex space-x-2">
                  <button
                    @click="openDetailModal(promocion.idPromocion)"
                    class="text-blue-600 hover:text-blue-800 transition-colors"
                    title="Ver detalles"
                  >
                    <EyeIcon class="h-4 w-4" />
                  </button>
                  <button
                    @click="openEditModal(promocion.idPromocion)"
                    class="text-yellow-600 hover:text-yellow-800 transition-colors"
                    title="Editar"
                  >
                    <PencilIcon class="h-4 w-4" />
                  </button>
                  <button
                    @click="confirmDeletePromocion(promocion.idPromocion)"
                    class="text-red-600 hover:text-red-800 transition-colors"
                    title="Eliminar"
                  >
                    <TrashIcon class="h-4 w-4" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile Cards -->
      <div class="lg:hidden space-y-4">
        <div
          v-for="promocion in paginatedPromociones"
          :key="promocion.idPromocion"
          class="bg-white rounded-lg shadow-sm p-4 border border-gray-200"
        >
          <div class="space-y-3">
            <!-- Header -->
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <h3 class="text-sm font-medium text-gray-900">
                  {{ promocion.nombre }}
                </h3>
                <p class="text-xs text-gray-500 mt-1">
                  {{ truncateText(promocion.descripcion, 60) }}
                </p>
              </div>
              <div class="flex space-x-2 ml-2">
                <button
                  @click="openDetailModal(promocion.idPromocion)"
                  class="text-blue-600 hover:text-blue-800 transition-colors"
                  title="Ver detalles"
                >
                  <EyeIcon class="h-4 w-4" />
                </button>
                <button
                  @click="openEditModal(promocion.idPromocion)"
                  class="text-yellow-600 hover:text-yellow-800 transition-colors"
                  title="Editar"
                >
                  <PencilIcon class="h-4 w-4" />
                </button>
                <button
                  @click="confirmDeletePromocion(promocion.idPromocion)"
                  class="text-red-600 hover:text-red-800 transition-colors"
                  title="Eliminar"
                >
                  <TrashIcon class="h-4 w-4" />
                </button>
              </div>
            </div>

            <!-- Details -->
            <div class="grid grid-cols-2 gap-3 text-xs">
              <div>
                <span class="text-gray-500">Tipo:</span>
                <div class="font-medium">
                  {{ getTipoDisplayName(promocion.tipoPromocionPrincipal) }}
                </div>
              </div>
              <div>
                <span class="text-gray-500">Estado:</span>
                <div>
                  <span 
                    class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium"
                    :class="getEstadoBadge(promocion.estadoVigencia).class"
                  >
                    {{ getEstadoBadge(promocion.estadoVigencia).text }}
                  </span>
                </div>
              </div>
              <div>
                <span class="text-gray-500">Vigencia:</span>
                <div class="font-medium">
                  {{ formatDate(promocion.vigenciaDesde) }}
                </div>
              </div>
              <div>
                <span class="text-gray-500">Hasta:</span>
                <div class="font-medium">
                  {{ formatDate(promocion.vigenciaHasta) }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Desktop Pagination -->
      <div class="hidden lg:flex items-center justify-between bg-white rounded-lg shadow-sm px-6 py-4">
        <div class="flex items-center space-x-2">
          <button
            @click="goToPage(1)"
            :disabled="!canGoPrevious"
            class="p-2 rounded-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <ChevronDoubleLeftIcon class="h-4 w-4" />
          </button>
          <button
            @click="goToPreviousPage()"
            :disabled="!canGoPrevious"
            class="p-2 rounded-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <ChevronLeftIcon class="h-4 w-4" />
          </button>
        </div>

        <div class="flex items-center space-x-2">
          <button
            v-for="page in visiblePages"
            :key="page"
            @click="goToPage(page)"
            :class="[
              'px-3 py-2 rounded-md text-sm font-medium',
              page === pagination.currentPage
                ? 'bg-primary-600 text-white'
                : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
            ]"
          >
            {{ page }}
          </button>
        </div>

        <div class="flex items-center space-x-2">
          <button
            @click="goToNextPage()"
            :disabled="!canGoNext"
            class="p-2 rounded-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <ChevronRightIcon class="h-4 w-4" />
          </button>
          <button
            @click="goToPage(pagination.totalPages)"
            :disabled="!canGoNext"
            class="p-2 rounded-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <ChevronDoubleRightIcon class="h-4 w-4" />
          </button>
        </div>
      </div>

      <!-- Mobile Pagination -->
      <div class="lg:hidden flex items-center justify-between bg-white rounded-lg shadow-sm px-4 py-3">
        <button
          @click="goToPreviousPage()"
          :disabled="!canGoPrevious"
          class="flex items-center px-3 py-2 rounded-md text-sm font-medium text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <ChevronLeftIcon class="h-4 w-4 mr-1" />
          Anterior
        </button>
        <span class="text-sm text-gray-700">
          {{ pagination.currentPage }} de {{ pagination.totalPages }}
        </span>
        <button
          @click="goToNextPage()"
          :disabled="!canGoNext"
          class="flex items-center px-3 py-2 rounded-md text-sm font-medium text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Siguiente
          <ChevronRightIcon class="h-4 w-4 ml-1" />
        </button>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <CreatePromocionModal
      v-if="showCreateModal"
      :show="showCreateModal"
      @close="closeCreateModal()"
    />
    

    <!-- Detail Modal -->
    <PromocionDetailModal
      v-if="showDetailModal"
      :show="showDetailModal"
      :loading="modalLoading"
      :promocion="currentPromocion"
      @close="closeDetailModal()"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { usePromocionesStore } from '@/stores/usePromocionesStore'
import { useUtils } from '@/composables/useUtils'
import { UI_CONFIG } from '@/utils/constants'
import {
  PlusIcon,
  MagnifyingGlassIcon,
  TagIcon,
  EyeIcon,
  PencilIcon,
  TrashIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  ChevronDoubleLeftIcon,
  ChevronDoubleRightIcon
} from '@heroicons/vue/24/outline'

// Dynamic imports for modals
import CreatePromocionModal from './CreatePromocionModal.vue'
import PromocionDetailModal from './PromocionDetailModal.vue'

// Composables
const promocionesStore = usePromocionesStore()
const { debounce } = useUtils()

// Estado reactivo del store
const {
  // Estado
  promociones,
  filteredPromociones,
  currentPromocion,
  loading,
  tableLoading,
  modalLoading,
  isEditMode,
  showCreateModal,
  showDetailModal,
  formData,
  previewData,
  showPreview,
  pagination,
  filters,
  
  // Computed
  hasPromociones,
  hasFilteredPromociones,
  paginatedPromociones,
  paginationInfo,
  canGoPrevious,
  canGoNext,
  isFormValid,
  modalTitle,
  tiposPromocion,
  estadosPromocion,
  showTipoConfig
} = storeToRefs(promocionesStore)

// Actions del store
const {
  fetchPromociones,
  createPromocion,
  updatePromocion,
  deletePromocion,
  getPromocionDetails,
  confirmDeletePromocion,
  openCreateModal,
  openEditModal,
  openDetailModal,
  closeCreateModal,
  closeDetailModal,
  setSearchFilter,
  setEstadoFilter,
  clearFilters: clearStoreFilters,
  goToPage,
  setPageSize,
  goToPreviousPage,
  goToNextPage,
  getEstadoBadge,
  formatDate,
  truncateText,
  getTipoDisplayName
} = promocionesStore

// Local reactive state
const searchTerm = ref('')
const estadoFilter = ref('')

// Computed properties
const visiblePages = computed(() => {
  const currentPage = pagination.value.currentPage
  const totalPages = pagination.value.totalPages
  const maxVisible = UI_CONFIG.MAX_VISIBLE_PAGES
  
  if (totalPages <= maxVisible) {
    return Array.from({ length: totalPages }, (_, i) => i + 1)
  }
  
  let startPage = Math.max(1, currentPage - Math.floor(maxVisible / 2))
  let endPage = Math.min(totalPages, startPage + maxVisible - 1)
  
  if (endPage - startPage + 1 < maxVisible) {
    startPage = Math.max(1, endPage - maxVisible + 1)
  }
  
  return Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i)
})

// Methods
const handleSearch = debounce((value) => {
  setSearchFilter(value)
}, UI_CONFIG.SEARCH_DEBOUNCE_TIME)

const handleEstadoFilter = () => {
  setEstadoFilter(estadoFilter.value)
}

const handlePageSizeChange = (event) => {
  setPageSize(parseInt(event.target.value))
}

const clearFilters = () => {
  searchTerm.value = ''
  estadoFilter.value = ''
  clearStoreFilters()
}

// Lifecycle
onMounted(() => {
  fetchPromociones()
})
</script>