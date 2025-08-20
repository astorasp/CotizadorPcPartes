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
          {{ componentesStore.modalTitle }}
        </h3>
        <button
          @click="$emit('close')"
          class="text-gray-400 hover:text-gray-600 transition-colors"
          :disabled="loading"
        >
          <XMarkIcon class="h-6 w-6" />
        </button>
      </div>

      <!-- Modal Content -->
      <div class="py-4">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Información Básica -->
          <div class="bg-blue-50 p-4 rounded-lg">
            <h4 class="font-medium text-blue-900 mb-4 flex items-center">
              <InformationCircleIcon class="h-5 w-5 mr-2" />
              Información Básica
            </h4>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <!-- ID -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  ID del Componente *
                </label>
                <input
                  v-model="componentesStore.formData.id"
                  @input="handleInputChange"
                  type="text"
                  placeholder="Ej: MON001"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                  :class="{ 'border-red-300': !componentesStore.formData.id && showValidation }"
                  :disabled="componentesStore.isEditMode"
                  required
                />
                <div v-if="!componentesStore.formData.id && showValidation" class="text-red-500 text-xs mt-1">
                  El ID es requerido
                </div>
              </div>

              <!-- Tipo -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Tipo de Componente *
                </label>
                <select
                  v-model="componentesStore.formData.tipoComponente"
                  @change="handleTipoChange"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                  :class="{ 'border-red-300': !componentesStore.formData.tipoComponente && showValidation }"
                  required
                >
                  <option value="">Seleccione un tipo</option>
                  <option value="MONITOR">Monitor</option>
                  <option value="DISCO_DURO">Disco Duro</option>
                  <option value="TARJETA_VIDEO">Tarjeta de Video</option>
                  <option value="PC">PC Completa</option>
                </select>
                <div v-if="!componentesStore.formData.tipoComponente && showValidation" class="text-red-500 text-xs mt-1">
                  El tipo es requerido
                </div>
              </div>

              <!-- Descripción -->
              <div class="md:col-span-2">
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Descripción *
                </label>
                <input
                  v-model="componentesStore.formData.descripcion"
                  @input="handleInputChange"
                  type="text"
                  placeholder="Descripción del componente"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                  :class="{ 'border-red-300': !componentesStore.formData.descripcion && showValidation }"
                  required
                />
                <div v-if="!componentesStore.formData.descripcion && showValidation" class="text-red-500 text-xs mt-1">
                  La descripción es requerida
                </div>
              </div>

              <!-- Marca y Modelo -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Marca *
                </label>
                <input
                  v-model="componentesStore.formData.marca"
                  @input="handleInputChange"
                  type="text"
                  placeholder="Marca del componente"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                  :class="{ 'border-red-300': !componentesStore.formData.marca && showValidation }"
                  required
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Modelo *
                </label>
                <input
                  v-model="componentesStore.formData.modelo"
                  @input="handleInputChange"
                  type="text"
                  placeholder="Modelo del componente"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                  :class="{ 'border-red-300': !componentesStore.formData.modelo && showValidation }"
                  required
                />
              </div>
            </div>
          </div>

          <!-- Precios -->
          <div class="bg-green-50 p-4 rounded-lg">
            <h4 class="font-medium text-green-900 mb-4 flex items-center">
              <CurrencyDollarIcon class="h-5 w-5 mr-2" />
              Información de Precios
            </h4>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Costo
                </label>
                <input
                  v-model.number="componentesStore.formData.costo"
                  @input="handleInputChange"
                  type="number"
                  min="0"
                  step="0.01"
                  placeholder="0.00"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Precio Base *
                </label>
                <input
                  v-model.number="componentesStore.formData.precioBase"
                  @input="handleInputChange"
                  type="number"
                  min="0"
                  step="0.01"
                  placeholder="0.00"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                  :class="{ 'border-red-300': !componentesStore.formData.precioBase && showValidation }"
                  required
                />
              </div>
            </div>
          </div>

          <!-- Campos específicos por tipo -->
          <div v-if="componentesStore.formData.tipoComponente" class="bg-purple-50 p-4 rounded-lg">
            <h4 class="font-medium text-purple-900 mb-4 flex items-center">
              <CogIcon class="h-5 w-5 mr-2" />
              Características Específicas
            </h4>
            
            <!-- Disco Duro -->
            <div v-if="componentesStore.formData.tipoComponente === 'DISCO_DURO'" class="grid grid-cols-1 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Capacidad de Almacenamiento
                </label>
                <input
                  v-model="componentesStore.formData.capacidadAlm"
                  type="text"
                  placeholder="Ej: 1TB, 500GB, 2TB"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                />
                <div class="text-xs text-gray-500 mt-1">
                  Formato sugerido: 1TB, 500GB, etc.
                </div>
              </div>
            </div>

            <!-- Tarjeta de Video -->
            <div v-if="componentesStore.formData.tipoComponente === 'TARJETA_VIDEO'" class="grid grid-cols-1 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Memoria
                </label>
                <input
                  v-model="componentesStore.formData.memoria"
                  type="text"
                  placeholder="Ej: 8GB, 12GB, 16GB"
                  class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                />
                <div class="text-xs text-gray-500 mt-1">
                  Formato sugerido: 8GB, 12GB, etc.
                </div>
              </div>
            </div>
          </div>

          <!-- Validación Summary -->
          <div v-if="validationErrors.length > 0" class="bg-red-50 border border-red-200 rounded-lg p-4">
            <h4 class="font-medium text-red-900 mb-2 flex items-center">
              <ExclamationTriangleIcon class="h-5 w-5 mr-2" />
              Errores de Validación
            </h4>
            <ul class="text-sm text-red-700 space-y-1">
              <li v-for="error in validationErrors" :key="error">• {{ error }}</li>
            </ul>
          </div>
        </form>
      </div>

      <!-- Modal Footer -->
      <div class="flex items-center justify-end pt-4 border-t border-gray-200 space-x-2">
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
          class="px-4 py-2 text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="loading || !componentesStore.isFormValid"
        >
          <div v-if="loading" class="flex items-center">
            <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
            Guardando...
          </div>
          <span v-else>
            {{ componentesStore.isEditMode ? 'Actualizar' : 'Crear' }} Componente
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useComponentesStore } from '@/stores/useComponentesStore'
import { 
  XMarkIcon, 
  InformationCircleIcon, 
  CurrencyDollarIcon, 
  CogIcon, 
  ExclamationTriangleIcon 
} from '@heroicons/vue/24/outline'

// Props
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['close'])

// Composables
const componentesStore = useComponentesStore()

// Local reactive state
const loading = ref(false)
const showValidation = ref(false)

// Computed properties
const validationErrors = computed(() => {
  const errors = []
  
  if (showValidation.value) {
    if (!componentesStore.formData.id) {
      errors.push('El ID es requerido')
    }
    
    if (!componentesStore.formData.tipoComponente) {
      errors.push('El tipo de componente es requerido')
    }
    
    if (!componentesStore.formData.descripcion) {
      errors.push('La descripción es requerida')
    }
    
    if (!componentesStore.formData.marca) {
      errors.push('La marca es requerida')
    }
    
    if (!componentesStore.formData.modelo) {
      errors.push('El modelo es requerido')
    }
    
    if (!componentesStore.formData.precioBase || componentesStore.formData.precioBase <= 0) {
      errors.push('El precio base debe ser mayor a 0')
    }
  }
  
  return errors
})

// Methods
const handleBackdropClick = (event) => {
  if (event.target === event.currentTarget) {
    emit('close')
  }
}

const handleInputChange = () => {
  showValidation.value = true
}

const handleTipoChange = () => {
  showValidation.value = true
}

const handleSubmit = async () => {
  showValidation.value = true
  
  if (validationErrors.value.length > 0) {
    return
  }
  
  try {
    loading.value = true
    const result = await componentesStore.submitComponente()
    
    if (result.success) {
      emit('close')
    }
  } catch (error) {
    console.error('Error submitting componente:', error)
  } finally {
    loading.value = false
  }
}

// Watch for show prop changes to reset validation
watch(() => props.show, (newValue) => {
  if (newValue) {
    showValidation.value = false
  }
})
</script>