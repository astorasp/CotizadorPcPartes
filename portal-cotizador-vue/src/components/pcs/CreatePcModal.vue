<template>
  <div
    v-if="show"
    class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
    @click="handleBackdropClick"
  >
    <div class="relative top-20 mx-auto p-5 border w-full max-w-lg bg-white rounded-lg shadow-lg">
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
        <form @submit.prevent="handleSubmit" class="space-y-4">
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

          <!-- Costo -->
          <div>
            <label for="costo" class="block text-sm font-medium text-gray-700 mb-1">
              Costo *
            </label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">$</span>
              <input
                id="costo"
                v-model.number="formData.costo"
                type="number"
                step="0.01"
                min="0"
                required
                :disabled="loading || isViewMode"
                class="w-full pl-8 pr-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                :class="{
                  'border-red-300 focus:ring-red-500 focus:border-red-500': errors.costo
                }"
                placeholder="0.00"
              />
            </div>
            <p v-if="errors.costo" class="mt-1 text-sm text-red-600">{{ errors.costo }}</p>
          </div>

          <!-- Precio Base -->
          <div>
            <label for="precioBase" class="block text-sm font-medium text-gray-700 mb-1">
              Precio Base *
            </label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">$</span>
              <input
                id="precioBase"
                v-model.number="formData.precioBase"
                type="number"
                step="0.01"
                min="0"
                required
                :disabled="loading || isViewMode"
                class="w-full pl-8 pr-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                :class="{
                  'border-red-300 focus:ring-red-500 focus:border-red-500': errors.precioBase
                }"
                placeholder="0.00"
              />
            </div>
            <p v-if="errors.precioBase" class="mt-1 text-sm text-red-600">{{ errors.precioBase }}</p>
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
  }
})

// Emits
const emit = defineEmits(['close', 'submit'])

// Estado local
const errors = ref({})

// Computed properties
const isFormValid = computed(() => {
  if (props.isViewMode) return true
  
  return props.formData.id &&
         props.formData.descripcion &&
         props.formData.marca &&
         props.formData.modelo &&
         props.formData.costo > 0 &&
         props.formData.precioBase > 0
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
  
  if (!props.formData.costo || props.formData.costo <= 0) {
    newErrors.costo = 'El costo debe ser mayor a 0'
  }
  
  if (!props.formData.precioBase || props.formData.precioBase <= 0) {
    newErrors.precioBase = 'El precio base debe ser mayor a 0'
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