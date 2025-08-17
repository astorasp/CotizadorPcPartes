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
          <LoadingButton
            v-if="userPermissions.create"
            @click="openCreateModal()"
            :loading="promocionesStore.isFetching"
            loading-text="Cargando..."
            :icon="PlusIcon"
            variant="primary"
            class="text-sm font-medium"
          >
            Nueva Promoción
          </LoadingButton>
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
          <LoadingButton
            @click="clearFilters"
            :loading="promocionesStore.isFetching"
            loading-text="Limpiando..."
            variant="outline"
            size="sm"
            class="flex-1"
          >
            Limpiar
          </LoadingButton>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="promocionesStore.isFetching" class="bg-white rounded-lg shadow-sm p-8">
      <div class="text-center">
        <LoadingSpinner 
          size="lg" 
          color="primary" 
          message="Cargando promociones..." 
          centered
        />
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="!hasPromociones" class="bg-white rounded-lg shadow-sm p-8">
      <div class="text-center">
        <TagIcon class="h-12 w-12 mx-auto mb-4 text-gray-300" />
        <h3 class="text-lg font-medium text-gray-900 mb-2">No hay promociones</h3>
        <p class="text-gray-500 mb-4">Comienza creando tu primera promoción</p>
        <LoadingButton
          v-if="userPermissions.create"
          @click="openCreateModal()"
          :loading="promocionesStore.isFetching"
          loading-text="Cargando..."
          :icon="PlusIcon"
          variant="primary"
        >
          Crear Primera Promoción
        </LoadingButton>
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
      <div class="hidden lg:block bg-white rounded-lg shadow-sm overflow-hidden relative">
        <!-- Loading Overlay -->
        <div v-if="promocionesStore.isFetching" class="absolute inset-0 bg-white bg-opacity-75 flex items-center justify-center z-10">
          <LoadingSpinner 
            size="md" 
            color="primary" 
            message="Actualizando promociones..." 
          />
        </div>
        
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
                  <LoadingButton
                    v-if="userPermissions.view"
                    @click="openDetailModal(promocion.idPromocion)"
                    :loading="promocionesStore.isFetching"
                    loading-text="..."
                    :icon="EyeIcon"
                    variant="ghost"
                    size="sm"
                    class="text-blue-600 hover:text-blue-800 p-1"
                    title="Ver detalles"
                  />
                  <LoadingButton
                    v-if="userPermissions.edit"
                    @click="openEditModal(promocion.idPromocion)"
                    :loading="promocionesStore.isFetching"
                    loading-text="..."
                    :icon="PencilIcon"
                    variant="ghost"
                    size="sm"
                    class="text-yellow-600 hover:text-yellow-800 p-1"
                    title="Editar"
                  />
                  <LoadingButton
                    v-if="userPermissions.delete"
                    @click="confirmDeletePromocion(promocion.idPromocion)"
                    :loading="promocionesStore.isDeleting"
                    loading-text="..."
                    :icon="TrashIcon"
                    variant="ghost"
                    size="sm"
                    class="text-red-600 hover:text-red-800 p-1"
                    title="Eliminar"
                  />
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile Cards -->
      <div class="lg:hidden space-y-4 relative">
        <!-- Loading Overlay -->
        <div v-if="promocionesStore.isFetching" class="absolute inset-0 bg-white bg-opacity-75 flex items-center justify-center z-10 rounded-lg">
          <LoadingSpinner 
            size="md" 
            color="primary" 
            message="Actualizando promociones..." 
          />
        </div>
        
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
                <LoadingButton
                  v-if="userPermissions.view"
                  @click="openDetailModal(promocion.idPromocion)"
                  :loading="promocionesStore.isFetching"
                  loading-text="..."
                  :icon="EyeIcon"
                  variant="ghost"
                  size="sm"
                  class="text-blue-600 hover:text-blue-800 p-1"
                  title="Ver detalles"
                />
                <LoadingButton
                  v-if="userPermissions.edit"
                  @click="openEditModal(promocion.idPromocion)"
                  :loading="promocionesStore.isFetching"
                  loading-text="..."
                  :icon="PencilIcon"
                  variant="ghost"
                  size="sm"
                  class="text-yellow-600 hover:text-yellow-800 p-1"
                  title="Editar"
                />
                <LoadingButton
                  v-if="userPermissions.delete"
                  @click="confirmDeletePromocion(promocion.idPromocion)"
                  :loading="promocionesStore.isDeleting"
                  loading-text="..."
                  :icon="TrashIcon"
                  variant="ghost"
                  size="sm"
                  class="text-red-600 hover:text-red-800 p-1"
                  title="Eliminar"
                />
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
          <LoadingButton
            @click="goToPage(1)"
            :disabled="!canGoPrevious"
            :loading="promocionesStore.isFetching"
            loading-text="..."
            :icon="ChevronDoubleLeftIcon"
            variant="outline"
            size="sm"
            class="p-2"
          />
          <LoadingButton
            @click="goToPreviousPage()"
            :disabled="!canGoPrevious"
            :loading="promocionesStore.isFetching"
            loading-text="..."
            :icon="ChevronLeftIcon"
            variant="outline"
            size="sm"
            class="p-2"
          />
        </div>

        <div class="flex items-center space-x-2">
          <LoadingButton
            v-for="page in visiblePages"
            :key="page"
            @click="goToPage(page)"
            :loading="promocionesStore.isFetching"
            loading-text="..."
            :variant="page === pagination.currentPage ? 'primary' : 'outline'"
            size="sm"
            class="px-3 py-2"
          >
            {{ page }}
          </LoadingButton>
        </div>

        <div class="flex items-center space-x-2">
          <LoadingButton
            @click="goToNextPage()"
            :disabled="!canGoNext"
            :loading="promocionesStore.isFetching"
            loading-text="..."
            :icon="ChevronRightIcon"
            variant="outline"
            size="sm"
            class="p-2"
          />
          <LoadingButton
            @click="goToPage(pagination.totalPages)"
            :disabled="!canGoNext"
            :loading="promocionesStore.isFetching"
            loading-text="..."
            :icon="ChevronDoubleRightIcon"
            variant="outline"
            size="sm"
            class="p-2"
          />
        </div>
      </div>

      <!-- Mobile Pagination -->
      <div class="lg:hidden flex items-center justify-between bg-white rounded-lg shadow-sm px-4 py-3">
        <LoadingButton
          @click="goToPreviousPage()"
          :disabled="!canGoPrevious"
          :loading="promocionesStore.isFetching"
          loading-text="..."
          :icon="ChevronLeftIcon"
          variant="outline"
          size="sm"
          class="flex items-center"
        >
          Anterior
        </LoadingButton>
        <span class="text-sm text-gray-700">
          {{ pagination.currentPage }} de {{ pagination.totalPages }}
        </span>
        <LoadingButton
          @click="goToNextPage()"
          :disabled="!canGoNext"
          :loading="promocionesStore.isFetching"
          loading-text="..."
          variant="outline"
          size="sm"
          class="flex items-center"
        >
          Siguiente
          <ChevronRightIcon class="h-4 w-4 ml-1" />
        </LoadingButton>
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

// UI Components
import LoadingButton from '@/components/ui/LoadingButton.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

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
  userPermissions,
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