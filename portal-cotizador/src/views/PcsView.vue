<template>
  <div class="space-y-6">
    <!-- Header de la página -->
    <div class="bg-white rounded-lg shadow p-6">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div class="flex items-center space-x-3">
            <h1 class="text-2xl font-bold text-gray-900">Gestión de PCs</h1>
            <span 
              v-if="pcsStore.primaryRole"
              class="inline-flex px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800"
            >
              {{ getRoleDisplayName(pcsStore.primaryRole) }}
            </span>
          </div>
          <p class="mt-1 text-sm text-gray-600">
            Administrar PCs y armado de componentes
          </p>
        </div>
        <div class="mt-4 sm:mt-0 flex space-x-4">
          <LoadingButton 
            @click="navigateToComponents" 
            variant="outline"
            size="md"
            text="Ver Componentes"
            :loading="false"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
            Ver Componentes
          </LoadingButton>
          <LoadingButton 
            v-if="pcsStore.canCreatePcs"
            @click="openCreateModal" 
            variant="primary"
            size="md"
            text="Nueva PC"
            loading-text="Preparando..."
            :loading="pcsStore.isCreating"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Nueva PC
          </LoadingButton>
        </div>
      </div>
    </div>

    <!-- Filtros y búsqueda -->
    <div class="bg-white rounded-lg shadow p-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="form-label">Buscar PCs</label>
          <input
            type="text"
            placeholder="ID, descripción, marca, modelo..."
            class="form-field"
            v-model="searchTerm"
          />
        </div>
        
        <div>
          <label class="form-label">Filtrar por precio</label>
          <select class="form-field" v-model="selectedPriceRange">
            <option value="">Todos los precios</option>
            <option value="0-1000">$0 - $1,000</option>
            <option value="1000-2000">$1,001 - $2,000</option>
            <option value="2000-5000">$2,001 - $5,000</option>
            <option value="5000+">Más de $5,000</option>
          </select>
        </div>
        
        <div class="flex items-end">
          <LoadingButton 
            @click="applyFilters" 
            variant="secondary"
            size="md"
            text="Aplicar Filtros"
            loading-text="Aplicando..."
            :loading="pcsStore.isFetching"
            full-width
          />
        </div>
        
        <div class="flex items-end">
          <LoadingButton 
            @click="clearFilters" 
            variant="outline"
            size="md"
            text="Limpiar"
            loading-text="Limpiando..."
            :loading="pcsStore.isFetching"
            full-width
          />
        </div>
      </div>
    </div>

    <!-- Tabla de PCs -->
    <div class="bg-white rounded-lg shadow">
      <div class="px-6 py-4 border-b border-gray-200">
        <h3 class="text-lg font-medium text-gray-900">
          PCs ({{ totalItems }})
        </h3>
      </div>
      
      <!-- Estado de carga -->
      <div v-if="pcsStore.isFetching" class="p-8 text-center">
        <LoadingSpinner 
          size="lg"
          color="primary"
          message="Cargando PCs..."
          centered
        />
      </div>
      
      <!-- Estado vacío -->
      <div v-else-if="!hasData" class="p-8 text-center">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
        </svg>
        <h3 class="mt-2 text-sm font-medium text-gray-900">No hay PCs</h3>
        <p class="mt-1 text-sm text-gray-500">Comience creando una nueva PC.</p>
        <div class="mt-6">
          <LoadingButton 
            v-if="pcsStore.canCreatePcs"
            @click="openCreateModal" 
            variant="primary"
            size="md"
            text="Nueva PC"
            loading-text="Preparando..."
            :loading="pcsStore.isCreating"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Nueva PC
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
              <th class="table-header">Marca/Modelo</th>
              <th class="table-header">Componentes</th>
              <th class="table-header">Precios</th>
              <th class="table-header">Acciones</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr 
              v-for="pc in paginatedPcs" 
              :key="pc.id"
              class="table-row"
            >
              <td class="table-cell">
                <div class="font-medium">{{ pc.id }}</div>
                <div class="text-xs text-gray-500">{{ pc.descripcion }}</div>
              </td>
              <td class="table-cell">
                <div class="font-medium">{{ pc.descripcion }}</div>
              </td>
              <td class="table-cell">
                <div class="text-sm text-gray-900">{{ pc.marca }}</div>
                <div class="text-sm text-gray-500">{{ pc.modelo }}</div>
              </td>
              <td class="table-cell">
                <ComponentCount :pc="pc" />
              </td>
              <td class="table-cell">
                <div class="text-sm text-gray-900">
                  <div v-if="pcsStore.canViewPcCost">Costo: {{ formatCurrency(pc.costo) }}</div>
                  <div>Precio: {{ formatCurrency(pc.precioBase) }}</div>
                </div>
              </td>
              <td class="table-cell">
                <div class="flex space-x-2">
                  <LoadingButton 
                    @click="pcsStore.openViewModal(pc.id)"
                    variant="ghost"
                    size="sm"
                    text="Ver"
                    loading-text="Cargando..."
                    :loading="pcsStore.isFetching"
                    class="text-blue-600 hover:text-blue-700 text-sm font-medium"
                    title="Ver detalles"
                  />
                  <LoadingButton 
                    v-if="pcsStore.canEditPcs || pcsStore.canAddComponentToPc"
                    @click="handleOpenManageModal(pc.id)"
                    variant="ghost"
                    size="sm"
                    text="Gestionar Armado"
                    loading-text="Cargando..."
                    :loading="pcsStore.isAddingComponent || pcsStore.isRemovingComponent"
                    class="text-primary-600 hover:text-primary-700 text-sm font-medium"
                    title="Gestionar armado"
                  />
                  <LoadingButton 
                    v-if="pcsStore.canEditPcs"
                    @click="pcsStore.openEditModal(pc.id)"
                    variant="ghost"
                    size="sm"
                    text="Editar"
                    loading-text="Cargando..."
                    :loading="pcsStore.isUpdating"
                    class="text-green-600 hover:text-green-700 text-sm font-medium"
                    title="Editar PC"
                  />
                  <LoadingButton 
                    v-if="pcsStore.canDeletePcs"
                    @click="handleDelete(pc)"
                    variant="ghost"
                    size="sm"
                    text="Eliminar"
                    loading-text="Eliminando..."
                    :loading="pcsStore.isDeleting"
                    class="text-danger-600 hover:text-danger-700 text-sm font-medium"
                    title="Eliminar PC"
                  />
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
              @click="pcsStore.goToPreviousPage()"
              variant="outline"
              size="sm"
              text="Anterior"
              loading-text="Cargando..."
              :loading="pcsStore.isFetching"
              :class="{ 'opacity-50 cursor-not-allowed': !canGoPrevious }"
            />
            <LoadingButton 
              v-for="page in visiblePages" 
              :key="page"
              @click="handlePageChange(page)"
              :variant="page === currentPage ? 'primary' : 'outline'"
              size="sm"
              :text="page.toString()"
              loading-text="..."
              :loading="pcsStore.isFetching"
            />
            <LoadingButton 
              :disabled="!canGoNext"
              @click="pcsStore.goToNextPage()"
              variant="outline"
              size="sm"
              text="Siguiente"
              loading-text="Cargando..."
              :loading="pcsStore.isFetching"
              :class="{ 'opacity-50 cursor-not-allowed': !canGoNext }"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de gestión de componentes -->
    <ManageComponentsModal />

    <!-- Modal de creación/edición de PC -->
    <CreatePcModal
      :show="showCreateModal || false"
      :loading="pcsStore.isCreating || pcsStore.isUpdating"
      :is-edit-mode="isEditMode"
      :is-view-mode="isViewMode"
      :form-data="formData"
      :modal-title="modalTitle"
      :is-form-valid="isFormValid"
      :current-pc="currentPc"
      @close="closeCreateModal"
      @submit="submitPc"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { usePcsStore } from '@/stores/usePcsStore'
import { useUtils } from '@/composables/useUtils'
import { useRouter } from 'vue-router'
import ComponentCount from '@/components/pcs/ComponentCount.vue'
import ManageComponentsModal from '@/components/pcs/ManageComponentsModal.vue'
import CreatePcModal from '@/components/pcs/CreatePcModal.vue'
import LoadingButton from '@/components/ui/LoadingButton.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

// Composables y stores
const pcsStore = usePcsStore()
const { formatCurrency, confirm } = useUtils()
const router = useRouter()

// Estado del store
const {
  paginatedPcs,
  hasFilteredPcs,
  paginationInfo,
  canGoPrevious,
  canGoNext,
  pagination,
  filters,
  showCreateModal,
  isEditMode,
  isViewMode,
  formData,
  modalTitle,
  isFormValid,
  currentPc
} = storeToRefs(pcsStore)

// Estado local para los inputs
const searchTerm = ref('')
const selectedPriceRange = ref('')

// Computed properties
const hasData = computed(() => hasFilteredPcs.value)
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
const applyFilters = () => {
  pcsStore.setSearchFilter(searchTerm.value)
  pcsStore.setPriceFilter(selectedPriceRange.value)
}

const clearFilters = () => {
  searchTerm.value = ''
  selectedPriceRange.value = ''
  pcsStore.clearFilters()
}

const handlePageChange = (page) => {
  pcsStore.goToPage(page)
}

const openCreateModal = () => {
  pcsStore.openCreateModal()
}

const closeCreateModal = () => {
  pcsStore.closeCreateModal()
}

const submitPc = async () => {
  await pcsStore.submitPc()
}

const handleOpenManageModal = async (pcId) => {
  await pcsStore.openManageModal(pcId)
}

const handleDelete = async (pc) => {
  const confirmed = await confirm(
    'Eliminar PC',
    `¿Está seguro de que desea eliminar la PC "${pc.descripcion}"?`
  )
  
  if (confirmed) {
    await pcsStore.deletePc(pc.id)
  }
}

const navigateToComponents = () => {
  router.push('/componentes')
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
    pcsStore.setSearchFilter(newValue)
  }, 300)
})

watch(selectedPriceRange, (newValue) => {
  pcsStore.setPriceFilter(newValue)
})

// Lifecycle
onMounted(async () => {
  await pcsStore.fetchPcs()
  // Cargar conteos de componentes después de cargar las PCs
  await pcsStore.loadComponentsCounts()
})
</script>