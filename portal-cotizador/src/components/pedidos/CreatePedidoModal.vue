<template>
  <div
    v-if="show"
    class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
    @click="handleBackdropClick"
  >
    <div class="relative top-8 mx-auto p-5 border w-full max-w-4xl bg-white rounded-lg shadow-lg">
      <!-- Modal Header -->
      <div class="flex items-center justify-between pb-4 border-b border-gray-200">
        <h3 class="text-lg font-semibold text-gray-900">
          Generar Nuevo Pedido
        </h3>
        <button
          @click="$emit('close')"
          class="text-gray-400 hover:text-gray-600 transition-colors"
          :disabled="loading"
        >
          <XMarkIcon class="h-6 w-6" />
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="py-8">
        <div class="text-center">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto mb-4"></div>
          <p class="text-gray-600">Generando pedido...</p>
        </div>
      </div>

      <!-- Modal Content -->
      <div v-else class="py-4">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- Columna izquierda - Formulario principal -->
            <div class="space-y-4">
              <!-- Selección de cotización -->
              <div>
                <label for="cotizacion" class="block text-sm font-medium text-gray-700 mb-1">
                  Cotización Base *
                </label>
                <select
                  id="cotizacion"
                  :value="formData.cotizacionId"
                  @change="handleCotizacionChange"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  required
                >
                  <option value="">Seleccionar cotización...</option>
                  <option 
                    v-for="cotizacion in availableCotizaciones" 
                    :key="cotizacion.folio"
                    :value="cotizacion.folio"
                  >
                    {{ cotizacion.displayText }}
                  </option>
                </select>
              </div>

              <!-- Información de cotización seleccionada -->
              <div v-if="selectedCotizacion" class="p-3 bg-blue-50 rounded-lg">
                <h4 class="font-medium text-blue-900 mb-2">Información de la Cotización</h4>
                <div class="grid grid-cols-3 gap-3 text-sm">
                  <div>
                    <span class="text-blue-700">Fecha:</span>
                    <div class="font-medium">{{ formatDate(selectedCotizacion.fecha) }}</div>
                  </div>
                  <div>
                    <span class="text-blue-700">Total:</span>
                    <div class="font-medium">{{ formatCurrency(selectedCotizacion.total) }}</div>
                  </div>
                  <div>
                    <span class="text-blue-700">Componentes:</span>
                    <div class="font-medium">{{ selectedCotizacion.detalles?.length || 0 }}</div>
                  </div>
                </div>
              </div>

              <!-- Selección de proveedor -->
              <div>
                <label for="proveedor" class="block text-sm font-medium text-gray-700 mb-1">
                  Proveedor *
                </label>
                <select
                  id="proveedor"
                  :value="formData.cveProveedor"
                  @change="handleProveedorChange"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  required
                >
                  <option value="">Seleccionar proveedor...</option>
                  <option 
                    v-for="proveedor in availableProveedores" 
                    :key="proveedor.cve"
                    :value="proveedor.cve"
                  >
                    {{ proveedor.displayText }}
                  </option>
                </select>
              </div>

              <!-- Información del proveedor seleccionado -->
              <div v-if="selectedProveedor" class="p-3 bg-green-50 rounded-lg">
                <h4 class="font-medium text-green-900 mb-2">Información del Proveedor</h4>
                <div class="space-y-1 text-sm">
                  <div><span class="text-green-700">Nombre:</span> {{ selectedProveedor.nombre }}</div>
                  <div><span class="text-green-700">Razón Social:</span> {{ selectedProveedor.razonSocial }}</div>
                  <div><span class="text-green-700">Pedidos:</span> {{ selectedProveedor.numeroPedidos || 0 }}</div>
                </div>
              </div>

              <!-- Fechas -->
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label for="fecha-emision" class="block text-sm font-medium text-gray-700 mb-1">
                    Fecha de Emisión *
                  </label>
                  <input
                    id="fecha-emision"
                    v-model="formData.fechaEmision"
                    type="date"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    required
                  >
                </div>

                <div>
                  <label for="fecha-entrega" class="block text-sm font-medium text-gray-700 mb-1">
                    Fecha de Entrega *
                  </label>
                  <input
                    id="fecha-entrega"
                    v-model="formData.fechaEntrega"
                    type="date"
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    required
                  >
                </div>
              </div>
            </div>

            <!-- Columna derecha - Nivel de surtido y preview -->
            <div class="space-y-4">
              <!-- Nivel de surtido -->
              <div>
                <label for="nivel-surtido" class="block text-sm font-medium text-gray-700 mb-1">
                  Nivel de Surtido: <span class="font-bold text-primary-600">{{ formData.nivelSurtido }}%</span>
                </label>
                <input
                  id="nivel-surtido"
                  v-model="formData.nivelSurtido"
                  type="range"
                  min="1"
                  max="100"
                  step="1"
                  class="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer slider"
                  @input="handleNivelSurtidoChange"
                >
                <div class="flex justify-between text-xs text-gray-500 mt-1">
                  <span>1%</span>
                  <span>25%</span>
                  <span>50%</span>
                  <span>75%</span>
                  <span>100%</span>
                </div>
              </div>

              <!-- Preview del pedido -->
              <div v-if="pedidoPreview" class="p-4 bg-yellow-50 rounded-lg border border-yellow-200">
                <h4 class="font-medium text-yellow-900 mb-3 flex items-center">
                  <DocumentTextIcon class="h-5 w-5 mr-2" />
                  Preview del Pedido
                </h4>
                <div class="space-y-2 text-sm">
                  <div class="flex justify-between">
                    <span class="text-yellow-700">Líneas incluidas:</span>
                    <span class="font-medium">{{ pedidoPreview.componentesIncluidos }} de {{ pedidoPreview.lineasTotales }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-yellow-700">Porcentaje de surtido:</span>
                    <span class="font-medium">{{ pedidoPreview.porcentajeSurtido }}%</span>
                  </div>
                  <div class="flex justify-between border-t border-yellow-300 pt-2">
                    <span class="text-yellow-700 font-medium">Total estimado:</span>
                    <span class="font-bold text-lg text-green-600">{{ formatCurrency(pedidoPreview.totalEstimado) }}</span>
                  </div>
                </div>
              </div>

              <!-- Información adicional -->
              <div class="p-4 bg-gray-50 rounded-lg">
                <h4 class="font-medium text-gray-900 mb-2">Instrucciones</h4>
                <ul class="text-sm text-gray-600 space-y-1">
                  <li>• Seleccione una cotización base para el pedido</li>
                  <li>• Elija el proveedor que surtirá los componentes</li>
                  <li>• Ajuste el nivel de surtido según sus necesidades</li>
                  <li>• Verifique las fechas de emisión y entrega</li>
                </ul>
              </div>
            </div>
          </div>
        </form>
      </div>

      <!-- Modal Footer -->
      <div class="flex items-center justify-end pt-4 border-t border-gray-200 space-x-3">
        <button
          @click="$emit('close')"
          type="button"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
          :disabled="loading"
        >
          Cancelar
        </button>

        <button
          @click="handleSubmit"
          type="button"
          :disabled="!isFormValid || loading"
          class="px-4 py-2 text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed rounded-lg transition-colors flex items-center"
        >
          <DocumentTextIcon class="h-4 w-4 mr-2" />
          <span v-if="loading" class="flex items-center">
            <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
            Generando...
          </span>
          <span v-else>Generar Pedido</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { XMarkIcon, DocumentTextIcon } from '@heroicons/vue/24/outline'

// Props
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  formData: {
    type: Object,
    required: true
  },
  isFormValid: {
    type: Boolean,
    default: false
  },
  availableCotizaciones: {
    type: Array,
    default: () => []
  },
  availableProveedores: {
    type: Array,
    default: () => []
  },
  selectedCotizacion: {
    type: Object,
    default: null
  },
  selectedProveedor: {
    type: Object,
    default: null
  },
  pedidoPreview: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits([
  'close', 
  'submit', 
  'cotizacion-change', 
  'proveedor-change', 
  'nivel-surtido-change'
])

// Methods
const handleSubmit = () => {
  emit('submit')
}

const handleBackdropClick = (event) => {
  if (event.target === event.currentTarget) {
    emit('close')
  }
}

const handleCotizacionChange = (event) => {
  emit('cotizacion-change', event.target.value)
}

const handleProveedorChange = (event) => {
  emit('proveedor-change', event.target.value)
}

const handleNivelSurtidoChange = (event) => {
  emit('nivel-surtido-change', event.target.value)
}

// Formateo (simple para el modal)
const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('es-MX')
}

const formatCurrency = (amount) => {
  if (amount == null || isNaN(amount)) return '$0.00'
  return parseFloat(amount).toLocaleString('es-MX', {
    style: 'currency',
    currency: 'MXN'
  })
}
</script>

<style scoped>
.slider::-webkit-slider-thumb {
  appearance: none;
  height: 20px;
  width: 20px;
  border-radius: 50%;
  background: #3B82F6;
  cursor: pointer;
}

.slider::-moz-range-thumb {
  height: 20px;
  width: 20px;
  border-radius: 50%;
  background: #3B82F6;
  cursor: pointer;
  border: none;
}
</style>