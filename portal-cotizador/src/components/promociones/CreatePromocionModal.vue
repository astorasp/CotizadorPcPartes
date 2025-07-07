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
          {{ promocionesStore.modalTitle }}
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
          <!-- Grid Layout -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- Información Básica -->
            <div class="space-y-4">
              <div class="bg-blue-50 p-4 rounded-lg">
                <h4 class="font-medium text-blue-900 mb-4 flex items-center">
                  <InformationCircleIcon class="h-5 w-5 mr-2" />
                  Información Básica
                </h4>
                
                <!-- Nombre -->
                <div class="mb-4">
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    Nombre de la Promoción *
                  </label>
                  <input
                    v-model="promocionesStore.formData.nombre"
                    @input="handleInputChange"
                    type="text"
                    placeholder="Ej: Descuento Black Friday"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                    :class="{ 'border-red-300': !promocionesStore.formData.nombre && showValidation }"
                    required
                  />
                  <div v-if="!promocionesStore.formData.nombre && showValidation" class="text-red-500 text-xs mt-1">
                    El nombre es requerido
                  </div>
                </div>

                <!-- Descripción -->
                <div class="mb-4">
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    Descripción *
                  </label>
                  <textarea
                    v-model="promocionesStore.formData.descripcion"
                    @input="handleInputChange"
                    rows="3"
                    placeholder="Describe los detalles de la promoción..."
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                    :class="{ 'border-red-300': !promocionesStore.formData.descripcion && showValidation }"
                    required
                  ></textarea>
                  <div v-if="!promocionesStore.formData.descripcion && showValidation" class="text-red-500 text-xs mt-1">
                    La descripción es requerida
                  </div>
                </div>

                <!-- Fechas -->
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">
                      Fecha de Inicio *
                    </label>
                    <input
                      v-model="promocionesStore.formData.vigenciaDesde"
                      @change="handleInputChange"
                      type="date"
                      class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                      :class="{ 'border-red-300': !promocionesStore.formData.vigenciaDesde && showValidation }"
                      required
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">
                      Fecha de Fin *
                    </label>
                    <input
                      v-model="promocionesStore.formData.vigenciaHasta"
                      @change="handleInputChange"
                      type="date"
                      class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                      :class="{ 'border-red-300': !promocionesStore.formData.vigenciaHasta && showValidation }"
                      required
                    />
                  </div>
                </div>
                
                <!-- Validación de fechas -->
                <div v-if="dateValidationError" class="text-red-500 text-xs mt-1">
                  {{ dateValidationError }}
                </div>
              </div>
            </div>

            <!-- Configuración del Tipo -->
            <div class="space-y-4">
              <div class="bg-purple-50 p-4 rounded-lg">
                <h4 class="font-medium text-purple-900 mb-4 flex items-center">
                  <TagIcon class="h-5 w-5 mr-2" />
                  Configuración del Tipo
                </h4>

                <!-- Tipo de Promoción -->
                <div class="mb-4">
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    Tipo de Promoción *
                  </label>
                  <select
                    v-model="promocionesStore.formData.tipo"
                    @change="handleTipoChange"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                    :class="{ 'border-red-300': !promocionesStore.formData.tipo && showValidation }"
                    required
                  >
                    <option value="">Seleccione un tipo</option>
                    <option
                      v-for="tipo in promocionesStore.tiposPromocion"
                      :key="tipo.value"
                      :value="tipo.value"
                    >
                      {{ tipo.label }}
                    </option>
                  </select>
                  <div v-if="selectedTipoDescription" class="text-sm text-gray-500 mt-1">
                    {{ selectedTipoDescription }}
                  </div>
                </div>

                <!-- Configuración específica por tipo -->
                <div v-if="promocionesStore.formData.tipo" class="space-y-4">
                  <!-- Descuento Plano -->
                  <div v-if="promocionesStore.showTipoConfig('DESCUENTO_PLANO')" class="bg-green-50 p-3 rounded-md">
                    <h5 class="font-medium text-green-900 mb-2">Descuento Plano</h5>
                    <div>
                      <label class="block text-sm font-medium text-gray-700 mb-2">
                        Monto del Descuento ($) *
                      </label>
                      <input
                        v-model.number="promocionesStore.formData.montoDescuento"
                        @input="handleInputChange"
                        type="number"
                        min="0"
                        step="0.01"
                        placeholder="0.00"
                        class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                        required
                      />
                    </div>
                  </div>

                  <!-- Descuento Porcentual -->
                  <div v-if="promocionesStore.showTipoConfig('DESCUENTO_PORCENTUAL')" class="bg-yellow-50 p-3 rounded-md">
                    <h5 class="font-medium text-yellow-900 mb-2">Descuento Porcentual</h5>
                    <div>
                      <label class="block text-sm font-medium text-gray-700 mb-2">
                        Porcentaje de Descuento (%) *
                      </label>
                      <input
                        v-model.number="promocionesStore.formData.porcentajeDescuento"
                        @input="handleInputChange"
                        type="number"
                        min="1"
                        max="100"
                        placeholder="0"
                        class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                        required
                      />
                    </div>
                  </div>

                  <!-- Por Cantidad -->
                  <div v-if="promocionesStore.showTipoConfig('POR_CANTIDAD')" class="bg-blue-50 p-3 rounded-md">
                    <h5 class="font-medium text-blue-900 mb-2">Descuento por Cantidad</h5>
                    <div class="grid grid-cols-2 gap-3">
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                          Cantidad Mínima *
                        </label>
                        <input
                          v-model.number="promocionesStore.formData.cantidadMinima"
                          @input="handleInputChange"
                          type="number"
                          min="1"
                          placeholder="1"
                          class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                          required
                        />
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                          Porcentaje (%) *
                        </label>
                        <input
                          v-model.number="promocionesStore.formData.porcentajeDescuento"
                          @input="handleInputChange"
                          type="number"
                          min="1"
                          max="100"
                          placeholder="0"
                          class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                          required
                        />
                      </div>
                    </div>
                  </div>

                  <!-- N x M -->
                  <div v-if="promocionesStore.showTipoConfig('NXM')" class="bg-red-50 p-3 rounded-md">
                    <h5 class="font-medium text-red-900 mb-2">Promoción N x M</h5>
                    <div class="grid grid-cols-2 gap-3">
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                          Compra (N) *
                        </label>
                        <input
                          v-model.number="promocionesStore.formData.nCompras"
                          @input="handleInputChange"
                          type="number"
                          min="2"
                          placeholder="2"
                          class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                          required
                        />
                      </div>
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                          Paga (M) *
                        </label>
                        <input
                          v-model.number="promocionesStore.formData.mPago"
                          @input="handleInputChange"
                          type="number"
                          min="1"
                          placeholder="1"
                          class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                          required
                        />
                      </div>
                    </div>
                    <div v-if="nxmValidationError" class="text-red-500 text-xs mt-1">
                      {{ nxmValidationError }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Preview Section -->
          <div v-if="promocionesStore.showPreview" class="bg-gray-50 p-4 rounded-lg">
            <h4 class="font-medium text-gray-900 mb-3 flex items-center">
              <EyeIcon class="h-5 w-5 mr-2" />
              Vista Previa
            </h4>
            <div 
              v-html="promocionesStore.previewData"
              class="text-sm text-gray-700"
            ></div>
          </div>

          <!-- Validation Summary -->
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
      <div class="flex items-center justify-between pt-4 border-t border-gray-200">
        <div class="flex items-center space-x-2">
          <button
            @click="promocionesStore.setDefaultDates()"
            type="button"
            class="px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
          >
            Fechas por Defecto
          </button>
          <button
            @click="promocionesStore.updatePreview()"
            type="button"
            class="px-3 py-2 text-sm font-medium text-blue-700 bg-blue-100 border border-blue-300 rounded-md hover:bg-blue-200 transition-colors"
          >
            Vista Previa
          </button>
        </div>
        <div class="flex items-center space-x-2">
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
            :disabled="loading || !promocionesStore.isFormValid"
          >
            <div v-if="loading" class="flex items-center">
              <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
              Guardando...
            </div>
            <span v-else>
              {{ promocionesStore.isEditMode ? 'Actualizar' : 'Crear' }} Promoción
            </span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { usePromocionesStore } from '@/stores/usePromocionesStore'
import { promocionesApi } from '@/services/promocionesApi'
import { 
  XMarkIcon, 
  InformationCircleIcon, 
  TagIcon, 
  EyeIcon, 
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
const promocionesStore = usePromocionesStore()

// Local reactive state
const loading = ref(false)
const showValidation = ref(false)

// Computed properties
const selectedTipoDescription = computed(() => {
  const tipo = promocionesStore.tiposPromocion.find(t => t.value === promocionesStore.formData.tipo)
  return tipo ? tipo.description : ''
})

const dateValidationError = computed(() => {
  if (!promocionesStore.formData.vigenciaDesde || !promocionesStore.formData.vigenciaHasta) {
    return ''
  }
  
  const validation = promocionesApi.validateDates(
    promocionesStore.formData.vigenciaDesde,
    promocionesStore.formData.vigenciaHasta
  )
  
  return validation.errors.length > 0 ? validation.errors[0] : ''
})

const nxmValidationError = computed(() => {
  if (promocionesStore.formData.tipo !== 'NXM') return ''
  
  const { nCompras, mPago } = promocionesStore.formData
  
  if (!nCompras || !mPago) return ''
  
  if (mPago >= nCompras) {
    return 'El número a pagar debe ser menor al número de compras'
  }
  
  return ''
})

const validationErrors = computed(() => {
  const errors = []
  
  if (showValidation.value) {
    if (!promocionesStore.formData.nombre) {
      errors.push('El nombre es requerido')
    }
    
    if (!promocionesStore.formData.descripcion) {
      errors.push('La descripción es requerida')
    }
    
    if (!promocionesStore.formData.vigenciaDesde) {
      errors.push('La fecha de inicio es requerida')
    }
    
    if (!promocionesStore.formData.vigenciaHasta) {
      errors.push('La fecha de fin es requerida')
    }
    
    if (!promocionesStore.formData.tipo) {
      errors.push('El tipo de promoción es requerido')
    }
    
    if (dateValidationError.value) {
      errors.push(dateValidationError.value)
    }
    
    if (nxmValidationError.value) {
      errors.push(nxmValidationError.value)
    }
    
    // Validaciones específicas por tipo
    const tipoValidation = promocionesApi.validateTipoConfig(promocionesStore.formData)
    if (!tipoValidation.isValid) {
      errors.push(...tipoValidation.errors)
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
  promocionesStore.updatePreview()
}

const handleTipoChange = () => {
  promocionesStore.handleTipoChange(promocionesStore.formData.tipo)
  showValidation.value = true
}

const handleSubmit = async () => {
  showValidation.value = true
  
  if (!promocionesStore.isFormValid) {
    return
  }
  
  try {
    loading.value = true
    const result = await promocionesStore.submitPromocion()
    
    if (result.success) {
      emit('close')
    }
  } catch (error) {
    console.error('Error submitting promocion:', error)
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