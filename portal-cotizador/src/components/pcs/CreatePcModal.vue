<template>
  <div
    v-if="show"
    class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
    @click="handleBackdropClick"
  >
    <div class="relative top-10 mx-auto p-5 border w-full max-w-4xl bg-white rounded-lg shadow-lg max-h-screen overflow-y-auto">
      <!-- Modal Header -->
      <div class="flex items-center justify-between pb-4 border-b border-gray-200">
        <h3 class="text-lg font-semibold text-gray-900">
          {{ modalTitle }}
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
          <p class="text-gray-600">Cargando...</p>
        </div>
      </div>

      <!-- Modal Content -->
      <div v-else class="py-4">
        <!-- Información adicional para vista/edición -->
        <div v-if="currentPc && (isViewMode || isEditMode)" class="mb-4 p-3 bg-gray-50 rounded-lg">
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span class="font-medium text-gray-700">ID:</span>
              <span class="text-gray-900 ml-1">{{ currentPc.id }}</span>
            </div>
            <div v-if="currentPc.componentCount !== undefined">
              <span class="font-medium text-gray-700">Componentes:</span>
              <span class="text-gray-900 ml-1">{{ currentPc.componentCount || 0 }}</span>
            </div>
          </div>
        </div>

        <!-- Formulario -->
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Información Básica de la PC -->
          <div class="bg-white">
            <h4 class="text-md font-medium text-gray-900 mb-4 flex items-center">
              <svg class="h-5 w-5 text-primary-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
              Información Básica de la PC
            </h4>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <!-- ID de la PC -->
              <div>
                <label for="id" class="block text-sm font-medium text-gray-700 mb-1">
                  ID de la PC *
                </label>
                <input
                  id="id"
                  v-model="formData.id"
                  type="text"
                  required
                  :disabled="loading || isViewMode || isEditMode"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                  :class="{
                    'border-red-300 focus:ring-red-500 focus:border-red-500': errors.id
                  }"
                  placeholder="Ejemplo: PC001"
                />
                <p v-if="errors.id" class="mt-1 text-sm text-red-600">{{ errors.id }}</p>
              </div>

              <!-- Descripción -->
              <div>
                <label for="descripcion" class="block text-sm font-medium text-gray-700 mb-1">
                  Descripción *
                </label>
                <input
                  id="descripcion"
                  v-model="formData.descripcion"
                  type="text"
                  required
                  :disabled="loading || isViewMode"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                  :class="{
                    'border-red-300 focus:ring-red-500 focus:border-red-500': errors.descripcion
                  }"
                  placeholder="Descripción de la PC"
                />
                <p v-if="errors.descripcion" class="mt-1 text-sm text-red-600">{{ errors.descripcion }}</p>
              </div>

              <!-- Marca -->
              <div>
                <label for="marca" class="block text-sm font-medium text-gray-700 mb-1">
                  Marca *
                </label>
                <input
                  id="marca"
                  v-model="formData.marca"
                  type="text"
                  required
                  :disabled="loading || isViewMode"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                  :class="{
                    'border-red-300 focus:ring-red-500 focus:border-red-500': errors.marca
                  }"
                  placeholder="Marca de la PC"
                />
                <p v-if="errors.marca" class="mt-1 text-sm text-red-600">{{ errors.marca }}</p>
              </div>

              <!-- Modelo -->
              <div>
                <label for="modelo" class="block text-sm font-medium text-gray-700 mb-1">
                  Modelo *
                </label>
                <input
                  id="modelo"
                  v-model="formData.modelo"
                  type="text"
                  required
                  :disabled="loading || isViewMode"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                  :class="{
                    'border-red-300 focus:ring-red-500 focus:border-red-500': errors.modelo
                  }"
                  placeholder="Modelo de la PC"
                />
                <p v-if="errors.modelo" class="mt-1 text-sm text-red-600">{{ errors.modelo }}</p>
              </div>
            </div>
          </div>

          <!-- Sección de Componentes -->
          <div v-if="!isViewMode" class="bg-gray-50 p-4 rounded-lg">
            <h4 class="text-md font-medium text-gray-900 mb-4 flex items-center">
              <svg class="h-5 w-5 text-primary-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14-7H5a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2V5a2 2 0 00-2-2zM9 11l3 3 8-8" />
              </svg>
              Componentes de la PC
              <span v-if="formData.selectedComponents.length > 0" class="ml-2 px-2 py-1 text-xs bg-primary-100 text-primary-800 rounded-full">
                {{ formData.selectedComponents.length }} seleccionados
              </span>
            </h4>
            
            <!-- Selector de componentes -->
            <div class="mb-6">
              <ComponentSelector
                :available-components="availableComponents"
                v-model:selected-components="formData.selectedComponents"
                :loading="loading"
              />
            </div>
            
            <!-- Lista de componentes seleccionados -->
            <ComponentList
              :components="formData.selectedComponents"
              @update:components="formData.selectedComponents = $event"
            />
          </div>

          <!-- Resumen de Precios (calculado automáticamente) -->
          <div v-if="formData.selectedComponents.length > 0" class="bg-primary-50 p-4 rounded-lg">
            <h4 class="text-md font-medium text-gray-900 mb-3 flex items-center">
              <svg class="h-5 w-5 text-primary-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
              </svg>
              Resumen de Costos
            </h4>
            
            <div class="grid grid-cols-2 gap-4 text-sm">
              <div class="space-y-1">
                <div class="flex justify-between">
                  <span class="text-gray-600">Costo Total:</span>
                  <span class="font-medium">{{ formatCurrency(calculatedCosto) }}</span>
                </div>
                <div class="flex justify-between border-t pt-1">
                  <span class="text-gray-900 font-medium">Precio de Venta:</span>
                  <span class="font-bold text-primary-600">{{ formatCurrency(calculatedPrecio) }}</span>
                </div>
              </div>
              <div class="space-y-1">
                <div class="flex justify-between">
                  <span class="text-gray-600">Margen:</span>
                  <span class="font-medium text-green-600">{{ formatCurrency(calculatedMargen) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600">% Ganancia:</span>
                  <span class="font-medium">{{ calculatedPercentage }}%</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Botones -->
          <div class="flex justify-end space-x-3 pt-6 border-t border-gray-200">
            <button
              type="button"
              @click="$emit('close')"
              :disabled="loading"
              class="px-4 py-2 text-gray-700 bg-gray-200 hover:bg-gray-300 rounded-md transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ isViewMode ? 'Cerrar' : 'Cancelar' }}
            </button>
            <button
              v-if="!isViewMode"
              type="submit"
              :disabled="loading || !isFormValid"
              class="px-4 py-2 bg-primary-600 text-white hover:bg-primary-700 rounded-md transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
            >
              <div v-if="loading" class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
              {{ loading ? 'Guardando...' : (isEditMode ? 'Actualizar' : 'Crear PC') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { XMarkIcon } from '@heroicons/vue/24/outline'
import { useUtils } from '@/composables/useUtils'
import ComponentSelector from './ComponentSelector.vue'
import ComponentList from './ComponentList.vue'

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
  isEditMode: {
    type: Boolean,
    default: false
  },
  isViewMode: {
    type: Boolean,
    default: false
  },
  formData: {
    type: Object,
    required: true
  },
  modalTitle: {
    type: String,
    default: 'Nueva PC'
  },
  isFormValid: {
    type: Boolean,
    default: false
  },
  currentPc: {
    type: Object,
    default: null
  },
  availableComponents: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['close', 'submit'])

// Composables
const { formatCurrency } = useUtils()

// Estado local
const errors = ref({})

// Computed properties
const isFormValid = computed(() => {
  if (props.isViewMode) return true
  
  return props.formData.id &&
         props.formData.descripcion &&
         props.formData.marca &&
         props.formData.modelo &&
         props.formData.selectedComponents.length > 0
})

// Computed para cálculos automáticos de precios
const calculatedCosto = computed(() => {
  return props.formData.selectedComponents.reduce((total, comp) => {
    return total + (comp.costo * (comp.cantidad || 1))
  }, 0)
})

const calculatedPrecio = computed(() => {
  return props.formData.selectedComponents.reduce((total, comp) => {
    return total + (comp.precioBase * (comp.cantidad || 1))
  }, 0)
})

const calculatedMargen = computed(() => {
  return calculatedPrecio.value - calculatedCosto.value
})

const calculatedPercentage = computed(() => {
  if (calculatedCosto.value === 0) return 0
  return Math.round((calculatedMargen.value / calculatedCosto.value) * 100)
})

// Métodos
const handleSubmit = () => {
  clearErrors()
  
  if (validateForm()) {
    emit('submit')
  }
}

const validateForm = () => {
  const newErrors = {}
  
  if (!props.formData.id?.trim()) {
    newErrors.id = 'El ID es requerido'
  }
  
  if (!props.formData.descripcion?.trim()) {
    newErrors.descripcion = 'La descripción es requerida'
  }
  
  if (!props.formData.marca?.trim()) {
    newErrors.marca = 'La marca es requerida'
  }
  
  if (!props.formData.modelo?.trim()) {
    newErrors.modelo = 'El modelo es requerido'
  }
  
  if (!props.formData.selectedComponents || props.formData.selectedComponents.length === 0) {
    newErrors.components = 'Debe seleccionar al menos un componente para armar la PC'
  }
  
  errors.value = newErrors
  return Object.keys(newErrors).length === 0
}

const clearErrors = () => {
  errors.value = {}
}

const handleBackdropClick = (event) => {
  if (event.target === event.currentTarget && !props.loading) {
    emit('close')
  }
}

// Limpiar errores cuando cambian los datos del formulario
watch(() => props.formData, clearErrors, { deep: true })

// Limpiar errores cuando se cierra el modal
watch(() => props.show, (newValue) => {
  if (!newValue) {
    clearErrors()
  }
})
</script>