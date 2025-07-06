<template>
  <!-- Modal Overlay -->
  <div 
    v-if="showManageModal"
    @click="handleOutsideClick"
    class="fixed inset-0 z-50 overflow-y-auto"
    aria-labelledby="modal-title" 
    role="dialog" 
    aria-modal="true"
  >
    <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <!-- Background overlay -->
      <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" aria-hidden="true"></div>

      <!-- Modal Panel -->
      <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-4xl sm:w-full">
        <!-- Header -->
        <div class="bg-white px-6 py-4 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-lg leading-6 font-medium text-gray-900" id="modal-title">
                Gestionar Componentes - PC {{ currentPc?.id }}
              </h3>
              <div v-if="currentPc" class="mt-1 text-sm text-gray-600">
                {{ currentPc.descripcion }} ({{ currentPc.marca }} {{ currentPc.modelo }})
              </div>
            </div>
            <button 
              @click="closeModal" 
              class="bg-white rounded-md text-gray-400 hover:text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              <span class="sr-only">Cerrar</span>
              <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <!-- Content -->
        <div class="bg-gray-50 px-6 py-4">
          <!-- Estadísticas resumen -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div class="bg-white p-4 rounded-lg shadow-sm">
              <div class="text-2xl font-bold text-primary-600">{{ currentPcStats.totalComponents }}</div>
              <div class="text-sm text-gray-600">Total Componentes</div>
            </div>
            <div class="bg-white p-4 rounded-lg shadow-sm">
              <div class="text-2xl font-bold text-success-600">{{ currentPcStats.formattedCost }}</div>
              <div class="text-sm text-gray-600">Costo Total</div>
            </div>
            <div class="bg-white p-4 rounded-lg shadow-sm">
              <div class="text-sm text-gray-600">Estado del Armado</div>
              <div class="text-sm font-medium" :class="buildStatusClass">{{ buildStatus }}</div>
            </div>
          </div>

          <!-- Formulario para agregar componente -->
          <div class="bg-white p-4 rounded-lg shadow-sm mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-4">Agregar Componente</h4>
            
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div class="md:col-span-2">
                <label class="form-label">Componente</label>
                <select 
                  v-model="componentSelectValue" 
                  class="form-field"
                  :disabled="modalLoading"
                >
                  <option value="">Seleccionar componente...</option>
                  <option 
                    v-for="component in availableComponentsForSelect" 
                    :key="component.id" 
                    :value="component.id"
                  >
                    {{ component.id }} - {{ component.descripcion }} ({{ getTypeLabel(component.tipoComponente) }})
                  </option>
                </select>
              </div>
              
              <div>
                <label class="form-label">Cantidad</label>
                <input
                  type="number"
                  v-model.number="componentQuantity"
                  min="1"
                  max="10"
                  class="form-field"
                  :disabled="modalLoading"
                />
              </div>
            </div>
            
            <div class="mt-4">
              <button 
                @click="handleAddComponent"
                :disabled="!componentSelectValue || modalLoading"
                class="btn-primary btn-md"
                :class="{ 'opacity-50 cursor-not-allowed': !componentSelectValue || modalLoading }"
              >
                <svg v-if="modalLoading" class="animate-spin -ml-1 mr-3 h-4 w-4" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                {{ modalLoading ? 'Agregando...' : 'Agregar Componente' }}
              </button>
            </div>
          </div>

          <!-- Lista de componentes actuales -->
          <div class="bg-white rounded-lg shadow-sm">
            <div class="px-4 py-3 border-b border-gray-200">
              <h4 class="text-md font-medium text-gray-900">
                Componentes Instalados ({{ currentPcComponents.length }})
              </h4>
            </div>

            <!-- Estado vacío -->
            <div v-if="currentPcComponents.length === 0" class="p-8 text-center">
              <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
              </svg>
              <h3 class="mt-2 text-sm font-medium text-gray-900">No hay componentes instalados</h3>
              <p class="mt-1 text-sm text-gray-500">Agregue componentes usando el formulario de arriba.</p>
            </div>

            <!-- Tabla de componentes -->
            <div v-else class="table-container">
              <table class="table-base">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="table-header">Componente</th>
                    <th class="table-header">Tipo</th>
                    <th class="table-header">Cantidad</th>
                    <th class="table-header">Costo Unit.</th>
                    <th class="table-header">Subtotal</th>
                    <th class="table-header">Acciones</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr 
                    v-for="component in currentPcComponents" 
                    :key="component.id"
                    class="table-row"
                  >
                    <td class="table-cell">
                      <div class="font-medium">{{ component.id }}</div>
                      <div class="text-sm text-gray-500">{{ component.descripcion }}</div>
                    </td>
                    <td class="table-cell">
                      <span class="inline-flex px-2 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-800">
                        {{ getTypeLabel(component.tipoComponente) }}
                      </span>
                    </td>
                    <td class="table-cell">1</td>
                    <td class="table-cell">{{ formatCurrency(component.costo) }}</td>
                    <td class="table-cell font-medium">{{ formatCurrency(component.costo) }}</td>
                    <td class="table-cell">
                      <button 
                        @click="handleRemoveComponent(component.id)"
                        :disabled="modalLoading"
                        class="text-danger-600 hover:text-danger-700 text-sm font-medium"
                        :class="{ 'opacity-50 cursor-not-allowed': modalLoading }"
                        title="Quitar componente"
                      >
                        Quitar
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="bg-gray-50 px-6 py-3 border-t border-gray-200 flex justify-end">
          <button 
            @click="closeModal" 
            class="btn-outline btn-md"
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { usePcsStore } from '@/stores/usePcsStore'
import { useUtils } from '@/composables/useUtils'
import { COMPONENT_TYPE_LABELS } from '@/utils/constants'

// Composables y stores
const pcsStore = usePcsStore()
const { formatCurrency } = useUtils()

// Estado del store
const {
  showManageModal,
  currentPc,
  currentPcComponents,
  modalLoading,
  componentSelectValue,
  componentQuantity,
  availableComponentsForSelect,
  currentPcStats
} = storeToRefs(pcsStore)

// Computed properties
const buildStatus = computed(() => {
  const count = currentPcStats.value.totalComponents
  if (count === 0) return 'Sin componentes'
  if (count < 3) return 'Armado básico'
  if (count < 6) return 'Armado intermedio'
  return 'Armado completo'
})

const buildStatusClass = computed(() => {
  const count = currentPcStats.value.totalComponents
  if (count === 0) return 'text-gray-600'
  if (count < 3) return 'text-warning-600'
  if (count < 6) return 'text-primary-600'
  return 'text-success-600'
})

// Métodos
const getTypeLabel = (tipo) => {
  return COMPONENT_TYPE_LABELS[tipo] || tipo
}

const handleAddComponent = async () => {
  await pcsStore.addComponentToPc(componentSelectValue.value, componentQuantity.value)
}

const handleRemoveComponent = async (componentId) => {
  await pcsStore.removeComponentFromPc(componentId)
}

const closeModal = () => {
  pcsStore.closeManageModal()
}

const handleOutsideClick = (event) => {
  // Solo cerrar si se hace click en el overlay, no en el contenido del modal
  if (event.target === event.currentTarget) {
    closeModal()
  }
}

// Manejar tecla ESC
const handleEscapeKey = (event) => {
  if (event.key === 'Escape' && showManageModal.value) {
    closeModal()
  }
}

// Agregar listener para ESC cuando el modal esté activo
document.addEventListener('keydown', handleEscapeKey)
</script>