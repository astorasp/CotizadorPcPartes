<template>
  <div class="container mx-auto px-4 py-8">
    <!-- Header -->
    <div class="mb-8">
      <div class="flex items-center space-x-3 mb-2">
        <h1 class="text-3xl font-bold text-gray-900">Proveedores</h1>
        <span 
          v-if="userPrimaryRole"
          class="inline-flex px-2 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-800"
        >
          {{ getRoleDisplayName(userPrimaryRole) }}
        </span>
      </div>
      <p class="text-gray-600">Gestión y administración de proveedores del sistema</p>
    </div>

    <!-- Controles superiores -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
      <div class="flex flex-col lg:flex-row gap-4 items-start lg:items-center justify-between">
        <!-- Búsqueda -->
        <div class="flex flex-col sm:flex-row gap-3 flex-1">
          <div class="relative flex-1">
            <MagnifyingGlassIcon class="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              v-model="searchTerm"
              type="text"
              placeholder="Buscar proveedores..."
              class="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              @input="debouncedSearch"
            >
          </div>
          
          <!-- Filtro de tipo de búsqueda -->
          <select
            v-model="searchType"
            class="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            @change="handleSearchTypeChange"
          >
            <option value="general">Búsqueda General</option>
            <option value="nombre">Por Nombre</option>
            <option value="razon">Por Razón Social</option>
          </select>
        </div>

        <!-- Botones de acción -->
        <div class="flex gap-2">
          <LoadingButton
            @click="clearLocalFilters"
            variant="ghost"
            :icon="XMarkIcon"
            text="Limpiar"
            :loading="proveedoresStore.isFetching"
            loading-text="Limpiando..."
          />
          <LoadingButton
            v-if="canCreateProveedores"
            @click="openCreateModal"
            variant="primary"
            :icon="PlusIcon"
            text="Nuevo Proveedor"
            :loading="proveedoresStore.isCreating"
            loading-text="Creando..."
          />
        </div>
      </div>
    </div>

    <!-- Estados de carga -->
    <div v-if="proveedoresStore.isFetching" class="bg-white rounded-lg shadow-sm border border-gray-200 p-8">
      <div class="text-center">
        <LoadingSpinner 
          size="lg" 
          color="primary" 
          message="Cargando proveedores..."
          centered
        />
      </div>
    </div>

    <!-- Estado vacío -->
    <div v-else-if="!hasFilteredProveedores" class="bg-white rounded-lg shadow-sm border border-gray-200 p-8">
      <div class="text-center">
        <BuildingOfficeIcon class="h-16 w-16 text-gray-400 mx-auto mb-4" />
        <h3 class="text-lg font-medium text-gray-900 mb-2">
          {{ hasProveedores ? 'No se encontraron resultados' : 'No hay proveedores registrados' }}
        </h3>
        <p class="text-gray-600 mb-6">
          {{ hasProveedores ? 'Intente con otros términos de búsqueda' : 'Comience creando su primer proveedor' }}
        </p>
        <LoadingButton
          v-if="!hasProveedores && canCreateProveedores"
          @click="openCreateModal"
          variant="primary"
          :icon="PlusIcon"
          text="Crear Primer Proveedor"
          :loading="proveedoresStore.isCreating"
          loading-text="Creando..."
        />
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
                Proveedor
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Razón Social
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Pedidos
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Acciones
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr
              v-for="proveedor in paginatedProveedores"
              :key="proveedor.cve"
              class="hover:bg-gray-50"
            >
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div>
                    <div class="text-sm font-medium text-gray-900">{{ proveedor.cve }}</div>
                    <div class="text-sm text-gray-500">{{ proveedor.nombre }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4">
                <div class="text-sm text-gray-900">{{ proveedor.razonSocial }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <span :class="[
                    'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                    (proveedor.numeroPedidos || 0) > 0 
                      ? 'bg-blue-100 text-blue-800' 
                      : 'bg-gray-100 text-gray-800'
                  ]">
                    {{ proveedor.numeroPedidos || 0 }} pedido{{ (proveedor.numeroPedidos || 0) !== 1 ? 's' : '' }}
                  </span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <div class="flex gap-2 justify-end">
                  <LoadingButton
                    v-if="canViewProveedores"
                    @click="openViewModal(proveedor.cve)"
                    variant="ghost"
                    size="sm"
                    text="Ver"
                    :loading="proveedoresStore.isFetching"
                    loading-text="Cargando..."
                    class="text-primary-600 hover:text-primary-900"
                  />
                  <LoadingButton
                    v-if="canEditProveedores"
                    @click="openEditModal(proveedor.cve)"
                    variant="ghost"
                    size="sm"
                    text="Editar"
                    :loading="proveedoresStore.isUpdating"
                    loading-text="Editando..."
                    class="text-indigo-600 hover:text-indigo-900"
                  />
                  <LoadingButton
                    v-if="canDeleteProveedores"
                    @click="confirmDeleteProveedor(proveedor.cve)"
                    variant="ghost"
                    size="sm"
                    text="Eliminar"
                    :loading="proveedoresStore.isDeleting"
                    loading-text="Eliminando..."
                    class="text-red-600 hover:text-red-900"
                  />
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Cards móvil -->
      <div class="md:hidden">
        <div class="space-y-4 p-4">
          <div
            v-for="proveedor in paginatedProveedores"
            :key="proveedor.cve"
            class="bg-white p-4 rounded-lg border border-gray-200 shadow-sm"
          >
            <div class="flex items-start justify-between mb-3">
              <div class="flex-1">
                <h3 class="text-sm font-medium text-gray-900">{{ proveedor.cve }}</h3>
                <p class="text-sm text-gray-500 mt-1">{{ proveedor.nombre }}</p>
              </div>
              <span :class="[
                'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                (proveedor.numeroPedidos || 0) > 0 
                  ? 'bg-blue-100 text-blue-800' 
                  : 'bg-gray-100 text-gray-800'
              ]">
                {{ proveedor.numeroPedidos || 0 }} pedido{{ (proveedor.numeroPedidos || 0) !== 1 ? 's' : '' }}
              </span>
            </div>
            
            <div class="mb-3">
              <p class="text-sm text-gray-900">{{ proveedor.razonSocial }}</p>
            </div>
            
            <div class="flex space-x-2">
              <LoadingButton
                v-if="canViewProveedores"
                @click="openViewModal(proveedor.cve)"
                variant="ghost"
                size="sm"
                text="Ver"
                :loading="proveedoresStore.isFetching"
                loading-text="Cargando..."
                full-width
                class="flex-1 text-xs text-primary-600 bg-primary-50 hover:bg-primary-100"
              />
              <LoadingButton
                v-if="canEditProveedores"
                @click="openEditModal(proveedor.cve)"
                variant="ghost"
                size="sm"
                text="Editar"
                :loading="proveedoresStore.isUpdating"
                loading-text="Editando..."
                full-width
                class="flex-1 text-xs text-indigo-600 bg-indigo-50 hover:bg-indigo-100"
              />
              <LoadingButton
                v-if="canDeleteProveedores"
                @click="confirmDeleteProveedor(proveedor.cve)"
                variant="ghost"
                size="sm"
                text="Eliminar"
                :loading="proveedoresStore.isDeleting"
                loading-text="Eliminando..."
                full-width
                class="flex-1 text-xs text-red-600 bg-red-50 hover:bg-red-100"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Paginación -->
      <div v-if="paginatedProveedores.length > 0" class="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
        <div class="flex-1 flex justify-between sm:hidden">
          <LoadingButton
            @click="goToPreviousPage"
            :disabled="!canGoPrevious"
            variant="outline"
            size="sm"
            text="Anterior"
            :loading="proveedoresStore.isFetching"
            loading-text="Cargando..."
          />
          <LoadingButton
            @click="goToNextPage"
            :disabled="!canGoNext"
            variant="outline"
            size="sm"
            text="Siguiente"
            :loading="proveedoresStore.isFetching"
            loading-text="Cargando..."
            class="ml-3"
          />
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
              <LoadingButton
                @click="goToPreviousPage"
                :disabled="!canGoPrevious"
                variant="outline"
                size="sm"
                :icon="ChevronLeftIcon"
                :loading="proveedoresStore.isFetching"
                loading-text=""
                class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
              />
              <button
                v-for="page in visiblePages"
                :key="page"
                @click="goToPage(page)"
                :disabled="proveedoresStore.isFetching"
                :class="[
                  'relative inline-flex items-center px-4 py-2 border text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed',
                  page === pagination.currentPage
                    ? 'z-10 bg-primary-50 border-primary-500 text-primary-600'
                    : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                ]"
              >
                {{ page }}
              </button>
              <LoadingButton
                @click="goToNextPage"
                :disabled="!canGoNext"
                variant="outline"
                size="sm"
                :icon="ChevronRightIcon"
                :loading="proveedoresStore.isFetching"
                loading-text=""
                class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
              />
            </nav>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de proveedor -->
    <ProveedorModal
      :show="showModal || false"
      :loading="modalLoading || proveedoresStore.isCreating || proveedoresStore.isUpdating"
      :is-edit-mode="isEditMode"
      :is-view-mode="isViewMode"
      :form-data="formData"
      :modal-title="modalTitle"
      :is-form-valid="isFormValid"
      :current-proveedor="currentProveedor"
      @close="closeModal"
      @submit="submitProveedor"
    />
    
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { storeToRefs } from 'pinia'
import { useProveedoresStore } from '@/stores/useProveedoresStore'
import { useAuthStore } from '@/stores/useAuthStore'
import { useUtils } from '@/composables/useUtils'
import { authService } from '@/services/authService'
import ProveedorModal from '@/components/proveedores/ProveedorModal.vue'
import LoadingButton from '@/components/ui/LoadingButton.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import {
  MagnifyingGlassIcon,
  PlusIcon,
  XMarkIcon,
  BuildingOfficeIcon,
  ChevronLeftIcon,
  ChevronRightIcon
} from '@heroicons/vue/24/outline'

const proveedoresStore = useProveedoresStore()
const authStore = useAuthStore()
const { debounce } = useUtils()

// Estados locales para la vista
const searchTerm = ref('')
const searchType = ref('general')

// Estado reactivo del store
const {
  // Estado
  proveedores,
  filteredProveedores,
  currentProveedor,
  modalLoading,
  isEditMode,
  isViewMode,
  showModal,
  formData,
  pagination,
  filters,
  
  // Computed
  hasProveedores,
  hasFilteredProveedores,
  paginatedProveedores,
  paginationInfo,
  canGoPrevious,
  canGoNext,
  isFormValid,
  modalTitle,
  
  // Loading states
  isFetching,
  isCreating,
  isUpdating,
  isDeleting
} = storeToRefs(proveedoresStore)

// Actions del store
const {
  fetchProveedores,
  openCreateModal,
  openViewModal,
  openEditModal,
  closeModal,
  submitProveedor,
  confirmDeleteProveedor,
  applyFilters,
  clearFilters,
  setSearchFilter,
  setSearchType,
  goToPage,
  setPageSize,
  goToPreviousPage,
  goToNextPage
} = proveedoresStore

// Computed properties para permisos de Proveedores
const canViewProveedores = computed(() => authService.canViewProveedores())
const canCreateProveedores = computed(() => authService.canCreateProveedores())
const canEditProveedores = computed(() => authService.canEditProveedores())
const canDeleteProveedores = computed(() => authService.canDeleteProveedores())
const canSearchProveedores = computed(() => authService.canSearchProveedores())
const canViewProveedorCommercialData = computed(() => authService.canViewProveedorCommercialData())
const canManageProveedorRelations = computed(() => authService.canManageProveedorRelations())
const userPrimaryRole = computed(() => authService.getPrimaryRole())

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
  applyFilters()
}, 300)

// Manejo del cambio de tipo de búsqueda
const handleSearchTypeChange = () => {
  setSearchType(searchType.value)
}

// Limpiar filtros locales
const clearLocalFilters = () => {
  searchTerm.value = ''
  searchType.value = 'general'
  clearFilters()
}

// Watchers
watch(() => filters.searchTerm, (newTerm) => {
  searchTerm.value = newTerm
})

watch(() => filters.searchType, (newType) => {
  searchType.value = newType
})

// Función para mostrar nombres de roles en español
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

// Lifecycle
onMounted(async () => {
  // Usar nextTick para asegurar que todos los stores estén inicializados
  await nextTick()
  
  // Forzar verificación de autenticación
  authStore.checkAuthentication()
  
  // Verificar tanto el estado del store como los tokens directamente
  const hasValidToken = localStorage.getItem('accessToken') && 
                       localStorage.getItem('issuedAt') && 
                       localStorage.getItem('expiresIn')
  
  if (authStore.isLoggedIn || hasValidToken) {
    await fetchProveedores()
  }
})

// Watcher para cargar datos cuando el usuario se autentique
watch(() => authStore.isLoggedIn, async (isLoggedIn) => {
  if (isLoggedIn) {
    await fetchProveedores()
  }
})
</script>
