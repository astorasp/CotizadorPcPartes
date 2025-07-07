<template>
  <!-- Modal Overlay -->
  <div 
    v-if="showModal"
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
      <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-6xl sm:w-full">
        <!-- Header -->
        <div class="bg-white px-6 py-4 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-lg leading-6 font-medium text-gray-900" id="modal-title">
                Nueva Cotización
              </h3>
              <p class="mt-1 text-sm text-gray-500">
                Complete la información para crear una nueva cotización
              </p>
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
        <div class="bg-gray-50 px-6 py-6">
          <form @submit.prevent="handleSubmit" class="space-y-6">
            <!-- Información básica -->
            <div class="bg-white p-6 rounded-lg shadow-sm">
              <h4 class="text-md font-medium text-gray-900 mb-4">Información Básica</h4>
              
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="form-label">Tipo de Cotizador *</label>
                  <select 
                    v-model="formData.tipoCotizador" 
                    class="form-field" 
                    required
                    :disabled="modalLoading"
                  >
                    <option value="">Seleccionar tipo...</option>
                    <option value="COTIZADOR_A">Cotizador A</option>
                    <option value="COTIZADOR_B">Cotizador B</option>
                  </select>
                </div>
                
                <div>
                  <label class="form-label">Fecha</label>
                  <input
                    type="date"
                    v-model="formData.fecha"
                    class="form-field"
                    :disabled="modalLoading"
                  />
                </div>
              </div>
            </div>

            <!-- Sección de Componentes -->
            <div class="bg-white p-6 rounded-lg shadow-sm">
              <h4 class="text-md font-medium text-gray-900 mb-4">Agregar Componentes</h4>
              
              <!-- Formulario para agregar componente -->
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
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
                      {{ component.id }} - {{ component.descripcion }} ({{ getTypeLabel(component.tipoComponente) }}) - {{ formatCurrency(component.precioBase) }}
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
              
              <div class="mb-6">
                <button 
                  type="button"
                  @click="handleAddComponent"
                  :disabled="!componentSelectValue || modalLoading"
                  class="btn-secondary btn-md"
                  :class="{ 'opacity-50 cursor-not-allowed': !componentSelectValue || modalLoading }"
                >
                  <svg v-if="modalLoading" class="animate-spin -ml-1 mr-3 h-4 w-4" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  {{ modalLoading ? 'Agregando...' : 'Agregar Componente' }}
                </button>
              </div>

              <!-- Lista de componentes agregados -->
              <div class="border rounded-lg">
                <div class="px-4 py-3 border-b bg-gray-50">
                  <h5 class="text-sm font-medium text-gray-900">
                    Componentes Agregados ({{ currentComponents.length }})
                  </h5>
                </div>

                <!-- Estado vacío -->
                <div v-if="currentComponents.length === 0" class="p-8 text-center">
                  <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
                  </svg>
                  <h3 class="mt-2 text-sm font-medium text-gray-900">No hay componentes agregados</h3>
                  <p class="mt-1 text-sm text-gray-500">Agregue componentes usando el formulario de arriba.</p>
                </div>

                <!-- Tabla de componentes -->
                <div v-else class="table-container">
                  <table class="table-base">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="table-header">Componente</th>
                        <th class="table-header">Cantidad</th>
                        <th class="table-header">Precio Unit.</th>
                        <th class="table-header">Subtotal</th>
                        <th class="table-header">Acciones</th>
                      </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                      <tr 
                        v-for="(component, index) in currentComponents" 
                        :key="component.componenteId || component.idComponente"
                        class="table-row"
                      >
                        <td class="table-cell">
                          <div class="font-medium">{{ component.componenteId || component.idComponente }}</div>
                          <div class="text-sm text-gray-500">{{ component.descripcion || component.nombreComponente }}</div>
                        </td>
                        <td class="table-cell">{{ component.cantidad }}</td>
                        <td class="table-cell">{{ formatCurrency(component.precioUnitario || component.precioBase) }}</td>
                        <td class="table-cell font-medium">{{ formatCurrency((component.cantidad || 1) * (component.precioUnitario || component.precioBase || 0)) }}</td>
                        <td class="table-cell">
                          <button 
                            type="button"
                            @click="handleRemoveComponent(index)"
                            :disabled="modalLoading"
                            class="text-danger-600 hover:text-danger-700 text-sm font-medium"
                            :class="{ 'opacity-50 cursor-not-allowed': modalLoading }"
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

            <!-- Sección de Impuestos -->
            <div class="bg-white p-6 rounded-lg shadow-sm">
              <div class="flex items-center justify-between mb-4">
                <h4 class="text-md font-medium text-gray-900">Configuración de Impuestos</h4>
                <button 
                  type="button"
                  @click="addImpuesto"
                  class="btn-outline btn-sm"
                  :disabled="modalLoading"
                >
                  <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                  </svg>
                  Agregar Impuesto
                </button>
              </div>

              <div class="space-y-4">
                <div 
                  v-for="(impuesto, index) in currentImpuestos" 
                  :key="index"
                  class="bg-gray-50 p-4 rounded-lg border"
                >
                  <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                      <label class="form-label">Tipo de Impuesto *</label>
                      <select 
                        v-model="impuesto.tipo" 
                        @change="updateTotals"
                        class="form-field" 
                        required
                        :disabled="modalLoading"
                      >
                        <option value="">Seleccionar...</option>
                        <option value="IVA">IVA</option>
                        <option value="ISR">ISR</option>
                        <option value="IEPS">IEPS</option>
                        <option value="SALES_TAX">Sales Tax</option>
                      </select>
                    </div>
                    <div>
                      <label class="form-label">País *</label>
                      <select 
                        v-model="impuesto.pais" 
                        @change="updateTotals"
                        class="form-field" 
                        required
                        :disabled="modalLoading"
                      >
                        <option value="">Seleccionar...</option>
                        <option value="MX">México</option>
                        <option value="US">Estados Unidos</option>
                        <option value="CA">Canadá</option>
                      </select>
                    </div>
                    <div>
                      <label class="form-label">Tasa (%) *</label>
                      <div class="flex">
                        <input 
                          type="number" 
                          v-model.number="impuesto.tasa"
                          @input="updateTotals"
                          step="0.01" 
                          min="0" 
                          max="100" 
                          class="form-field" 
                          required 
                          placeholder="16.00"
                          :disabled="modalLoading"
                        />
                        <button 
                          v-if="currentImpuestos.length > 1"
                          type="button"
                          @click="handleRemoveImpuesto(index)"
                          class="ml-2 text-danger-600 hover:text-danger-700 p-2"
                          :disabled="modalLoading"
                        >
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                          </svg>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Resumen de Totales -->
            <div class="bg-white p-6 rounded-lg shadow-sm">
              <h4 class="text-md font-medium text-gray-900 mb-4">Resumen de Totales</h4>
              
              <div class="space-y-3">
                <div class="flex justify-between">
                  <span class="text-sm text-gray-600">Subtotal:</span>
                  <span class="text-sm font-medium">{{ formatCurrency(currentCotizacionTotals.subtotal) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-sm text-gray-600">Impuestos:</span>
                  <span class="text-sm font-medium text-warning-600">{{ formatCurrency(currentCotizacionTotals.totalImpuestos) }}</span>
                </div>
                <div class="border-t pt-3">
                  <div class="flex justify-between">
                    <span class="text-lg font-semibold text-gray-900">Total:</span>
                    <span class="text-lg font-bold text-success-600">{{ formatCurrency(currentCotizacionTotals.total) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </form>
        </div>

        <!-- Footer -->
        <div class="bg-gray-50 px-6 py-3 border-t border-gray-200 flex justify-end space-x-3">
          <button 
            type="button"
            @click="closeModal" 
            class="btn-outline btn-md"
            :disabled="modalLoading"
          >
            Cancelar
          </button>
          <button 
            type="button"
            @click="handleSubmit"
            :disabled="!isFormValid || modalLoading"
            class="btn-primary btn-md"
            :class="{ 'opacity-50 cursor-not-allowed': !isFormValid || modalLoading }"
          >
            <svg v-if="modalLoading" class="animate-spin -ml-1 mr-3 h-4 w-4" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ modalLoading ? 'Creando...' : 'Crear Cotización' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useCotizacionesStore } from '@/stores/useCotizacionesStore'
import { useUtils } from '@/composables/useUtils'
import { COMPONENT_TYPE_LABELS } from '@/utils/constants'

// Composables y stores
const cotizacionesStore = useCotizacionesStore()
const { formatCurrency } = useUtils()

// Estado del store
const {
  showModal,
  formData,
  currentComponents,
  currentImpuestos,
  modalLoading,
  componentSelectValue,
  componentQuantity,
  availableComponentsForSelect,
  currentCotizacionTotals,
  isFormValid
} = storeToRefs(cotizacionesStore)

// Métodos
const getTypeLabel = (tipo) => {
  return COMPONENT_TYPE_LABELS[tipo] || tipo
}

const handleAddComponent = async () => {
  await cotizacionesStore.addComponentToCotizacion(componentSelectValue.value, componentQuantity.value)
}

const handleRemoveComponent = async (index) => {
  await cotizacionesStore.removeComponentFromCotizacion(index)
}

const addImpuesto = () => {
  cotizacionesStore.addImpuesto()
}

const handleRemoveImpuesto = async (index) => {
  await cotizacionesStore.removeImpuesto(index)
}

const handleSubmit = async () => {
  await cotizacionesStore.submitCotizacion()
}

const closeModal = () => {
  cotizacionesStore.closeModal()
}

const handleOutsideClick = (event) => {
  // Solo cerrar si se hace click en el overlay, no en el contenido del modal
  if (event.target === event.currentTarget) {
    closeModal()
  }
}

// Método para actualizar totales cuando cambian los impuestos
const updateTotals = () => {
  // Los totales se actualizan automáticamente gracias a la reactividad del store
}

// Manejar tecla ESC
const handleEscapeKey = (event) => {
  if (event.key === 'Escape' && showModal.value) {
    closeModal()
  }
}

// Agregar listener para ESC cuando el modal esté activo
document.addEventListener('keydown', handleEscapeKey)
</script>