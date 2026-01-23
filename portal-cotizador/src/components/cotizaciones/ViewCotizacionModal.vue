<template>
  <!-- Modal Overlay -->
  <div 
    v-if="showViewModal"
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
                Ver Cotización
              </h3>
              <div v-if="currentCotizacion" class="mt-1 text-sm text-gray-500">
                Cotización #{{ getCotizacionId() }} - {{ getCotizacionTipoCotizador() }}
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
        <div class="bg-gray-50 px-6 py-6" v-if="currentCotizacion">
          <!-- Información básica -->
          <div class="bg-white p-6 rounded-lg shadow-sm mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-4">Información de la Cotización</h4>
            
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700">Folio</label>
                <div class="mt-1 text-lg font-bold text-primary-600">
                  #{{ getCotizacionId() }}
                </div>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">Tipo de Cotizador</label>
                <div class="mt-1 text-sm text-gray-900">
                  {{ getCotizacionTipoCotizador() }}
                </div>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">Fecha</label>
                <div class="mt-1 text-sm text-gray-900">
                  {{ formatDate(getCotizacionFecha) }}
                </div>
              </div>
            </div>
          </div>

          <!-- Componentes de la cotización -->
          <div class="bg-white p-6 rounded-lg shadow-sm mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-4">
              Componentes ({{ currentComponents.length }})
            </h4>

            <!-- Estado vacío -->
            <div v-if="currentComponents.length === 0" class="p-8 text-center">
              <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
              </svg>
              <h3 class="mt-2 text-sm font-medium text-gray-900">No hay componentes</h3>
              <p class="mt-1 text-sm text-gray-500">Esta cotización no tiene componentes agregados.</p>
            </div>

            <!-- Tabla de componentes -->
            <div v-else class="table-container">
              <table class="table-base">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="table-header">Componente</th>
                    <th class="table-header">Cantidad</th>
                    <th class="table-header">Precio Unitario</th>
                    <th class="table-header">Subtotal</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr 
                    v-for="component in currentComponents" 
                    :key="component.idComponente || component.componenteId"
                    class="table-row"
                  >
                    <td class="table-cell">
                      <div class="font-medium">{{ component.idComponente || component.componenteId }}</div>
                      <div class="text-sm text-gray-500">{{ component.nombreComponente || component.descripcion }}</div>
                    </td>
                    <td class="table-cell">{{ component.cantidad || 1 }}</td>
                    <td class="table-cell">{{ formatCurrency(component.precioBase || component.precioUnitario || 0) }}</td>
                    <td class="table-cell font-medium">
                      {{ formatCurrency(component.importeTotal || ((component.cantidad || 1) * (component.precioBase || component.precioUnitario || 0))) }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Impuestos aplicados -->
          <div class="bg-white p-6 rounded-lg shadow-sm mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-4">
              Impuestos Aplicados ({{ currentImpuestos.length }})
            </h4>

            <!-- Estado vacío -->
            <div v-if="currentImpuestos.length === 0" class="p-8 text-center">
              <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
              </svg>
              <h3 class="mt-2 text-sm font-medium text-gray-900">No hay impuestos</h3>
              <p class="mt-1 text-sm text-gray-500">Esta cotización no tiene impuestos configurados.</p>
            </div>

            <!-- Lista de impuestos -->
            <div v-else class="space-y-3">
              <div 
                v-for="(impuesto, index) in currentImpuestos" 
                :key="index"
                class="bg-gray-50 p-4 rounded-lg border"
              >
                <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700">Tipo</label>
                    <div class="mt-1 text-sm text-gray-900">{{ impuesto.tipo }}</div>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700">País</label>
                    <div class="mt-1 text-sm text-gray-900">{{ getCountryLabel(impuesto.pais) }}</div>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700">Tasa</label>
                    <div class="mt-1 text-sm text-gray-900">{{ impuesto.tasa }}%</div>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700">Importe</label>
                    <div class="mt-1 text-sm font-medium text-warning-600">
                      {{ formatCurrency(calculateTaxAmount(impuesto.tasa)) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Resumen de Totales -->
          <div class="bg-white p-6 rounded-lg shadow-sm">
            <h4 class="text-md font-medium text-gray-900 mb-4">Resumen de Totales</h4>
            
            <div class="space-y-4">
              <!-- Desglose de componentes -->
              <div v-if="currentComponents.length > 0" class="border-b pb-4">
                <h5 class="text-sm font-medium text-gray-700 mb-2">Desglose por Componentes</h5>
                <div class="space-y-1">
                  <div 
                    v-for="component in currentComponents" 
                    :key="component.idComponente || component.componenteId"
                    class="flex justify-between text-xs text-gray-600"
                  >
                    <span>{{ component.idComponente || component.componenteId }} ({{ component.cantidad || 1 }}x)</span>
                    <span>{{ formatCurrency(component.importeTotal || ((component.cantidad || 1) * (component.precioBase || component.precioUnitario || 0))) }}</span>
                  </div>
                </div>
              </div>

              <!-- Totales principales -->
              <div class="space-y-2">
                <div class="flex justify-between">
                  <span class="text-sm text-gray-600">Subtotal:</span>
                  <span class="text-sm font-medium">{{ formatCurrency(getSubtotal()) }}</span>
                </div>
                
                <!-- Desglose de impuestos -->
                <div v-if="currentImpuestos.length > 0" class="space-y-1">
                  <div 
                    v-for="(impuesto, index) in currentImpuestos" 
                    :key="index"
                    class="flex justify-between"
                  >
                    <span class="text-xs text-gray-500">{{ impuesto.tipo }} ({{ impuesto.tasa }}%):</span>
                    <span class="text-xs text-warning-600">{{ formatCurrency(calculateTaxAmount(impuesto.tasa)) }}</span>
                  </div>
                </div>
                
                <div class="flex justify-between border-b pb-2">
                  <span class="text-sm text-gray-600">Total Impuestos:</span>
                  <span class="text-sm font-medium text-warning-600">{{ formatCurrency(getTotalImpuestos()) }}</span>
                </div>
                
                <div class="border-t pt-3">
                  <div class="flex justify-between">
                    <span class="text-lg font-semibold text-gray-900">Total Final:</span>
                    <span class="text-lg font-bold text-success-600">{{ formatCurrency(getTotal()) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Loading state -->
        <div v-else-if="modalLoading" class="bg-gray-50 px-6 py-12">
          <div class="flex justify-center">
            <div class="inline-flex items-center px-4 py-2 text-sm text-gray-600">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-gray-600" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Cargando cotización...
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="bg-gray-50 px-6 py-3 border-t border-gray-200 flex justify-end">
          <button 
            type="button"
            @click="closeModal" 
            class="btn-primary btn-md"
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
import { useCotizacionesStore } from '@/stores/useCotizacionesStore'
import { useUtils } from '@/composables/useUtils'
import { COUNTRY_LABELS } from '@/utils/constants'

// Composables y stores
const cotizacionesStore = useCotizacionesStore()
const { formatCurrency, formatDate } = useUtils()

// Estado del store
const {
  showViewModal,
  currentCotizacion,
  currentComponents,
  currentImpuestos,
  modalLoading
} = storeToRefs(cotizacionesStore)

// Métodos auxiliares para extraer datos de la cotización
const getCotizacionId = () => {
  return currentCotizacion.value?.folio || currentCotizacion.value?.id || 'N/A'
}

const getCotizacionTipoCotizador = () => {
  return currentCotizacion.value?.tipoCotizador || 'Estándar'
}

const getCotizacionFecha = computed(() => {
  // La fecha debe venir directamente de currentCotizacion.value.fecha desde la API
  if (currentCotizacion.value?.fecha) {
    return currentCotizacion.value.fecha
  }
  // Fallback por si no hay fecha (no debería pasar)
  return new Date().toISOString().split('T')[0]
})

const getCountryLabel = (pais) => {
  return COUNTRY_LABELS[pais] || pais
}

// Cálculos de totales
const getSubtotal = () => {
  if (currentCotizacion.value?.subtotal !== undefined) {
    return parseFloat(currentCotizacion.value.subtotal)
  }
  
  // Calcular desde componentes si no está disponible
  return currentComponents.value.reduce((sum, comp) => {
    const precio = parseFloat(comp.precioBase || comp.precioUnitario || 0)
    const cantidad = parseInt(comp.cantidad || 1)
    return sum + (precio * cantidad)
  }, 0)
}

const getTotalImpuestos = () => {
  if (currentCotizacion.value?.impuestos !== undefined) {
    return parseFloat(currentCotizacion.value.impuestos)
  }
  if (currentCotizacion.value?.totalImpuestos !== undefined) {
    return parseFloat(currentCotizacion.value.totalImpuestos)
  }
  
  // Calcular desde impuestos si no está disponible
  const subtotal = getSubtotal()
  return currentImpuestos.value.reduce((sum, impuesto) => {
    if (impuesto.tasa > 0) {
      return sum + (subtotal * (impuesto.tasa / 100))
    }
    return sum
  }, 0)
}

const getTotal = () => {
  if (currentCotizacion.value?.total !== undefined) {
    return parseFloat(currentCotizacion.value.total)
  }
  
  // Calcular total
  return getSubtotal() + getTotalImpuestos()
}

const calculateTaxAmount = (tasa) => {
  const subtotal = getSubtotal()
  return subtotal * (tasa / 100)
}

// Métodos de UI
const closeModal = () => {
  cotizacionesStore.closeModal()
}

const handleOutsideClick = (event) => {
  // Solo cerrar si se hace click en el overlay, no en el contenido del modal
  if (event.target === event.currentTarget) {
    closeModal()
  }
}

// Manejar tecla ESC
const handleEscapeKey = (event) => {
  if (event.key === 'Escape' && showViewModal.value) {
    closeModal()
  }
}

// Agregar listener para ESC cuando el modal esté activo
document.addEventListener('keydown', handleEscapeKey)
</script>