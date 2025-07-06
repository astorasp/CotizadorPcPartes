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
        <div v-if="currentProveedor && (isViewMode || isEditMode)" class="mb-4 p-3 bg-gray-50 rounded-lg">
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span class="font-medium text-gray-700">Clave:</span>
              <span class="text-gray-900 ml-1">{{ currentProveedor.cve }}</span>
            </div>
            <div v-if="currentProveedor.numeroPedidos !== undefined">
              <span class="font-medium text-gray-700">Pedidos:</span>
              <span class="text-gray-900 ml-1">{{ currentProveedor.numeroPedidos || 0 }}</span>
            </div>
          </div>
        </div>

        <!-- Formulario -->
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <!-- Clave del proveedor -->
          <div>
            <label for="cve" class="block text-sm font-medium text-gray-700 mb-1">
              Clave del Proveedor *
            </label>
            <input
              id="cve"
              v-model="formData.cve"
              type="text"
              maxlength="10"
              placeholder="Ingrese la clave del proveedor"
              :disabled="isViewMode || isEditMode"
              :class="[
                'w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent',
                isViewMode || isEditMode 
                  ? 'bg-gray-100 border-gray-300 text-gray-500 cursor-not-allowed' 
                  : 'border-gray-300'
              ]"
              required
            >
            <p class="text-xs text-gray-500 mt-1">Máximo 10 caracteres</p>
          </div>

          <!-- Nombre comercial -->
          <div>
            <label for="nombre" class="block text-sm font-medium text-gray-700 mb-1">
              Nombre Comercial *
            </label>
            <input
              id="nombre"
              v-model="formData.nombre"
              type="text"
              maxlength="100"
              placeholder="Ingrese el nombre comercial"
              :disabled="isViewMode"
              :class="[
                'w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent',
                isViewMode 
                  ? 'bg-gray-100 border-gray-300 text-gray-500 cursor-not-allowed' 
                  : 'border-gray-300'
              ]"
              required
            >
            <p class="text-xs text-gray-500 mt-1">Máximo 100 caracteres</p>
          </div>

          <!-- Razón social -->
          <div>
            <label for="razon-social" class="block text-sm font-medium text-gray-700 mb-1">
              Razón Social *
            </label>
            <input
              id="razon-social"
              v-model="formData.razonSocial"
              type="text"
              maxlength="200"
              placeholder="Ingrese la razón social"
              :disabled="isViewMode"
              :class="[
                'w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent',
                isViewMode 
                  ? 'bg-gray-100 border-gray-300 text-gray-500 cursor-not-allowed' 
                  : 'border-gray-300'
              ]"
              required
            >
            <p class="text-xs text-gray-500 mt-1">Máximo 200 caracteres</p>
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
          {{ isViewMode ? 'Cerrar' : 'Cancelar' }}
        </button>

        <button
          v-if="!isViewMode"
          @click="handleSubmit"
          type="button"
          :disabled="!isFormValid || loading"
          class="px-4 py-2 text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed rounded-lg transition-colors"
        >
          <span v-if="loading" class="flex items-center">
            <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
            Guardando...
          </span>
          <span v-else>
            {{ isEditMode ? 'Actualizar Proveedor' : 'Guardar Proveedor' }}
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
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
    required: true
  },
  isFormValid: {
    type: Boolean,
    default: false
  },
  currentProveedor: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['close', 'submit'])

// Methods
const handleSubmit = () => {
  emit('submit')
}

const handleBackdropClick = (event) => {
  // Solo cerrar si se hace clic en el backdrop, no en el modal
  if (event.target === event.currentTarget) {
    emit('close')
  }
}
</script>