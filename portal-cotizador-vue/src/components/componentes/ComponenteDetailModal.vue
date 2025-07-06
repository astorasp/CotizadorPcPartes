<template>
  <div
    v-if="show"
    class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
    @click="handleBackdropClick"
  >
    <div class="relative top-8 mx-auto p-5 border w-full max-w-2xl bg-white rounded-lg shadow-lg">
      <!-- Modal Header -->
      <div class="flex items-center justify-between pb-4 border-b border-gray-200">
        <h3 class="text-lg font-semibold text-gray-900">
          {{ componente ? `Detalles: ${componente.descripcion}` : 'Detalles del Componente' }}
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
          <p class="text-gray-600">Cargando detalles del componente...</p>
        </div>
      </div>

      <!-- Modal Content -->
      <div v-else-if="componente" class="py-4">
        <div class="space-y-4">
          <!-- Información básica -->
          <div class="bg-blue-50 p-4 rounded-lg">
            <h4 class="font-medium text-blue-900 mb-3 flex items-center">
              <InformationCircleIcon class="h-5 w-5 mr-2" />
              Información Básica
            </h4>
            <div class="grid grid-cols-2 gap-3 text-sm">
              <div>
                <span class="text-blue-700 font-medium">ID:</span>
                <div class="text-gray-900 font-mono">{{ componente.id }}</div>
              </div>
              <div>
                <span class="text-blue-700 font-medium">Tipo:</span>
                <div class="text-gray-900">
                  <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                    {{ getTypeLabel(componente.tipoComponente) }}
                  </span>
                </div>
              </div>
              <div class="col-span-2">
                <span class="text-blue-700 font-medium">Descripción:</span>
                <div class="text-gray-900">{{ componente.descripcion }}</div>
              </div>
              <div>
                <span class="text-blue-700 font-medium">Marca:</span>
                <div class="text-gray-900">{{ componente.marca }}</div>
              </div>
              <div>
                <span class="text-blue-700 font-medium">Modelo:</span>
                <div class="text-gray-900">{{ componente.modelo }}</div>
              </div>
            </div>
          </div>

          <!-- Información de precios -->
          <div class="bg-green-50 p-4 rounded-lg">
            <h4 class="font-medium text-green-900 mb-3 flex items-center">
              <CurrencyDollarIcon class="h-5 w-5 mr-2" />
              Información de Precios
            </h4>
            <div class="grid grid-cols-2 gap-3 text-sm">
              <div v-if="componente.costo">
                <span class="text-green-700 font-medium">Costo:</span>
                <div class="text-gray-900">{{ formatCurrency(componente.costo) }}</div>
              </div>
              <div>
                <span class="text-green-700 font-medium">Precio Base:</span>
                <div class="text-gray-900 font-medium">{{ formatCurrency(componente.precioBase) }}</div>
              </div>
              <div v-if="componente.costo" class="col-span-2">
                <span class="text-green-700 font-medium">Margen:</span>
                <div class="text-gray-900">{{ calculateMargin(componente.costo, componente.precioBase) }}%</div>
              </div>
            </div>
          </div>

          <!-- Características específicas -->
          <div v-if="hasSpecificFields(componente)" class="bg-purple-50 p-4 rounded-lg">
            <h4 class="font-medium text-purple-900 mb-3 flex items-center">
              <CogIcon class="h-5 w-5 mr-2" />
              Características Específicas
            </h4>
            
            <!-- Monitor -->
            <div v-if="componente.tipoComponente === 'MONITOR'" class="grid grid-cols-2 gap-3 text-sm">
              <div v-if="componente.tamano">
                <span class="text-purple-700 font-medium">Tamaño:</span>
                <div class="text-gray-900">{{ componente.tamano }}"</div>
              </div>
              <div v-if="componente.resolucion">
                <span class="text-purple-700 font-medium">Resolución:</span>
                <div class="text-gray-900">{{ componente.resolucion }}</div>
              </div>
            </div>

            <!-- Disco Duro -->
            <div v-if="componente.tipoComponente === 'DISCO_DURO'" class="grid grid-cols-2 gap-3 text-sm">
              <div v-if="componente.capacidad">
                <span class="text-purple-700 font-medium">Capacidad:</span>
                <div class="text-gray-900">{{ formatCapacity(componente.capacidad) }}</div>
              </div>
              <div v-if="componente.tipoConexion">
                <span class="text-purple-700 font-medium">Conexión:</span>
                <div class="text-gray-900">{{ componente.tipoConexion }}</div>
              </div>
            </div>

            <!-- Tarjeta de Video -->
            <div v-if="componente.tipoComponente === 'TARJETA_VIDEO'" class="grid grid-cols-2 gap-3 text-sm">
              <div v-if="componente.memoriaGpu">
                <span class="text-purple-700 font-medium">Memoria GPU:</span>
                <div class="text-gray-900">{{ componente.memoriaGpu }} GB</div>
              </div>
              <div v-if="componente.tipoMemoria">
                <span class="text-purple-700 font-medium">Tipo de Memoria:</span>
                <div class="text-gray-900">{{ componente.tipoMemoria }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Estado sin datos -->
      <div v-else class="py-8 text-center text-gray-500">
        <InformationCircleIcon class="h-12 w-12 mx-auto mb-4 text-gray-300" />
        <p>No se pudo cargar la información del componente</p>
      </div>

      <!-- Modal Footer -->
      <div class="flex items-center justify-end pt-4 border-t border-gray-200">
        <button
          @click="$emit('close')"
          type="button"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
          :disabled="loading"
        >
          Cerrar
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { 
  XMarkIcon, 
  InformationCircleIcon, 
  CurrencyDollarIcon, 
  CogIcon 
} from '@heroicons/vue/24/outline'

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
  componente: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['close'])

// Methods
const handleBackdropClick = (event) => {
  if (event.target === event.currentTarget) {
    emit('close')
  }
}

const formatCurrency = (amount) => {
  if (amount == null || isNaN(amount)) return '$0.00'
  return parseFloat(amount).toLocaleString('es-MX', {
    style: 'currency',
    currency: 'MXN',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const formatCapacity = (capacity) => {
  if (!capacity) return ''
  if (capacity >= 1000) {
    return `${(capacity / 1000).toFixed(0)} TB`
  }
  return `${capacity} GB`
}

const calculateMargin = (costo, precio) => {
  if (!costo || !precio || costo <= 0) return 0
  return ((precio - costo) / costo * 100).toFixed(1)
}

const getTypeLabel = (tipo) => {
  const labels = {
    'MONITOR': 'Monitor',
    'DISCO_DURO': 'Disco Duro',
    'TARJETA_VIDEO': 'Tarjeta de Video',
    'PC': 'PC Completa'
  }
  return labels[tipo] || tipo
}

const hasSpecificFields = (componente) => {
  if (!componente) return false
  
  switch (componente.tipoComponente) {
    case 'MONITOR':
      return componente.tamano || componente.resolucion
    case 'DISCO_DURO':
      return componente.capacidad || componente.tipoConexion
    case 'TARJETA_VIDEO':
      return componente.memoriaGpu || componente.tipoMemoria
    default:
      return false
  }
}
</script>