<template>
  <div class="escalas-descuento-editor">
    <!-- Header con información -->
    <div class="flex items-center justify-between mb-4">
      <div class="flex items-center">
        <ChartBarIcon class="h-5 w-5 text-blue-600 mr-2" />
        <h5 class="font-medium text-gray-900">Escalas de Descuento por Cantidad</h5>
      </div>
      <button
        @click="addEscala"
        type="button"
        class="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded text-blue-700 bg-blue-100 hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
      >
        <PlusIcon class="h-4 w-4 mr-1" />
        Agregar Escala
      </button>
    </div>

    <!-- Tabla de escalas -->
    <div class="bg-gray-50 rounded-lg p-4">
      <div v-if="escalas.length === 0" class="text-center py-8 text-gray-500">
        <ChartBarIcon class="h-12 w-12 mx-auto text-gray-300 mb-4" />
        <p class="text-sm">No hay escalas configuradas</p>
        <p class="text-xs text-gray-400 mt-1">Haz clic en "Agregar Escala" para comenzar</p>
      </div>

      <div v-else class="space-y-3">
        <!-- Header de la tabla -->
        <div class="grid grid-cols-12 gap-2 text-xs font-medium text-gray-600 uppercase tracking-wider">
          <div class="col-span-3">Cantidad Mínima</div>
          <div class="col-span-3">Cantidad Máxima</div>
          <div class="col-span-3">Descuento (%)</div>
          <div class="col-span-2">Ahorro</div>
          <div class="col-span-1">Acción</div>
        </div>

        <!-- Filas de escalas -->
        <div
          v-for="(escala, index) in escalas"
          :key="index"
          class="grid grid-cols-12 gap-2 items-center bg-white rounded-lg p-3 border border-gray-200 hover:border-gray-300 transition-colors"
          :class="{ 'border-red-300 bg-red-50': hasValidationError(index) }"
        >
          <!-- Cantidad Mínima -->
          <div class="col-span-3">
            <input
              v-model.number="escala.cantidadMinima"
              @input="validateAndUpdate"
              type="number"
              min="1"
              placeholder="1"
              class="block w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              :class="{ 'border-red-300': hasFieldError(index, 'cantidadMinima') }"
            />
          </div>

          <!-- Cantidad Máxima -->
          <div class="col-span-3">
            <input
              v-model.number="escala.cantidadMaxima"
              @input="validateAndUpdate"
              type="number"
              :min="escala.cantidadMinima || 1"
              placeholder="999"
              class="block w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              :class="{ 'border-red-300': hasFieldError(index, 'cantidadMaxima') }"
            />
          </div>

          <!-- Porcentaje de Descuento -->
          <div class="col-span-3">
            <div class="relative">
              <input
                v-model.number="escala.porcentaje"
                @input="validateAndUpdate"
                type="number"
                min="0.01"
                max="100"
                step="0.01"
                placeholder="0.00"
                class="block w-full px-2 py-1.5 pr-6 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                :class="{ 'border-red-300': hasFieldError(index, 'porcentaje') }"
              />
              <div class="absolute inset-y-0 right-0 pr-2 flex items-center pointer-events-none">
                <span class="text-gray-500 text-xs">%</span>
              </div>
            </div>
          </div>

          <!-- Ahorro Calculado -->
          <div class="col-span-2">
            <div class="text-xs text-green-600 font-medium">
              {{ formatSavings(escala) }}
            </div>
            <div class="text-xs text-gray-500">
              por unidad
            </div>
          </div>

          <!-- Acción (Eliminar) -->
          <div class="col-span-1">
            <button
              @click="removeEscala(index)"
              type="button"
              class="p-1.5 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-md transition-colors"
              :title="`Eliminar escala ${index + 1}`"
            >
              <TrashIcon class="h-4 w-4" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Validaciones y errores -->
    <div v-if="validationErrors.length > 0" class="mt-4 bg-red-50 border border-red-200 rounded-lg p-3">
      <div class="flex items-center mb-2">
        <ExclamationTriangleIcon class="h-5 w-5 text-red-400 mr-2" />
        <h6 class="text-sm font-medium text-red-800">Errores en las escalas</h6>
      </div>
      <ul class="text-sm text-red-700 space-y-1">
        <li v-for="error in validationErrors" :key="error">• {{ error }}</li>
      </ul>
    </div>

    <!-- Warnings y recomendaciones -->
    <div v-if="validationWarnings.length > 0" class="mt-4 bg-yellow-50 border border-yellow-200 rounded-lg p-3">
      <div class="flex items-center mb-2">
        <ExclamationTriangleIcon class="h-5 w-5 text-yellow-400 mr-2" />
        <h6 class="text-sm font-medium text-yellow-800">Recomendaciones</h6>
      </div>
      <ul class="text-sm text-yellow-700 space-y-1">
        <li v-for="warning in validationWarnings" :key="warning">⚠ {{ warning }}</li>
      </ul>
    </div>

    <!-- Preview de escalas -->
    <div v-if="escalas.length > 0 && validationErrors.length === 0" class="mt-4 bg-green-50 border border-green-200 rounded-lg p-4">
      <div class="flex items-center mb-3">
        <CheckCircleIcon class="h-5 w-5 text-green-400 mr-2" />
        <h6 class="text-sm font-medium text-green-800">Configuración de Escalas</h6>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2 text-sm">
        <div
          v-for="(escala, index) in sortedEscalas"
          :key="index"
          class="bg-white rounded-md p-2 border border-green-200"
        >
          <div class="font-medium text-gray-900">
            {{ escala.cantidadMinima }}{{ escala.cantidadMaxima < 999999 ? `-${escala.cantidadMaxima}` : '+' }} unidades
          </div>
          <div class="text-green-600">{{ escala.porcentaje }}% descuento</div>
        </div>
      </div>
    </div>

    <!-- Simulador rápido -->
    <div v-if="escalas.length > 0 && validationErrors.length === 0" class="mt-4 bg-blue-50 border border-blue-200 rounded-lg p-4">
      <div class="flex items-center mb-3">
        <CalculatorIcon class="h-5 w-5 text-blue-400 mr-2" />
        <h6 class="text-sm font-medium text-blue-800">Simulador de Descuentos</h6>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-xs font-medium text-gray-700 mb-1">Cantidad a simular</label>
          <input
            v-model.number="simulatorQuantity"
            type="number"
            min="1"
            placeholder="5"
            class="block w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-700 mb-1">Precio unitario ($)</label>
          <input
            v-model.number="simulatorPrice"
            type="number"
            min="0.01"
            step="0.01"
            placeholder="100.00"
            class="block w-full px-2 py-1.5 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
      </div>
      <div v-if="simulationResult" class="mt-3 p-3 bg-white rounded-md border border-blue-200">
        <div class="grid grid-cols-3 gap-4 text-sm">
          <div>
            <div class="text-gray-600">Precio original:</div>
            <div class="font-medium">${{ simulationResult.original.toFixed(2) }}</div>
          </div>
          <div>
            <div class="text-gray-600">Con descuento:</div>
            <div class="font-medium text-green-600">${{ simulationResult.discounted.toFixed(2) }}</div>
          </div>
          <div>
            <div class="text-gray-600">Ahorro:</div>
            <div class="font-medium text-green-600">
              ${{ simulationResult.savings.toFixed(2) }} ({{ simulationResult.percentage.toFixed(1) }}%)
            </div>
          </div>
        </div>
        <div v-if="simulationResult.escalaAplicada" class="mt-2 text-xs text-blue-600">
          Escala aplicada: {{ simulationResult.escalaAplicada.cantidadMinima }}{{ simulationResult.escalaAplicada.cantidadMaxima < 999999 ? `-${simulationResult.escalaAplicada.cantidadMaxima}` : '+' }} unidades → {{ simulationResult.escalaAplicada.porcentaje }}%
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import {
  PlusIcon,
  TrashIcon,
  ChartBarIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  CalculatorIcon
} from '@heroicons/vue/24/outline'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'validation'])

// Estado reactivo
const escalas = ref([...props.modelValue])
const validationErrors = ref([])
const validationWarnings = ref([])
const simulatorQuantity = ref(5)
const simulatorPrice = ref(100.00)
const isUpdatingFromParent = ref(false)

// Computed properties
const sortedEscalas = computed(() => {
  return [...escalas.value].sort((a, b) => a.cantidadMinima - b.cantidadMinima)
})

const simulationResult = computed(() => {
  if (!simulatorQuantity.value || !simulatorPrice.value || escalas.value.length === 0) {
    return null
  }

  const quantity = simulatorQuantity.value
  const price = simulatorPrice.value
  const original = quantity * price

  // Encontrar la escala aplicable (usar la lógica del backend)
  const escalaAplicada = sortedEscalas.value
    .filter(e => e.cantidadMinima <= quantity && quantity <= (e.cantidadMaxima || 999999))
    .sort((a, b) => b.cantidadMinima - a.cantidadMinima)[0] // Tomar la más específica (mayor cantidadMinima)

  if (!escalaAplicada) {
    return {
      original,
      discounted: original,
      savings: 0,
      percentage: 0,
      escalaAplicada: null
    }
  }

  const discountAmount = original * (escalaAplicada.porcentaje / 100)
  const discounted = original - discountAmount

  return {
    original,
    discounted,
    savings: discountAmount,
    percentage: escalaAplicada.porcentaje,
    escalaAplicada
  }
})

// Watchers
watch(escalas, (newEscalas) => {
  if (!isUpdatingFromParent.value) {
    emit('update:modelValue', [...newEscalas])
    validateAndUpdate()
  }
}, { deep: true })

watch(() => props.modelValue, (newValue) => {
  isUpdatingFromParent.value = true
  escalas.value = [...newValue]
  // Usar nextTick para asegurar que el flag se resetee después del ciclo de reactivity
  nextTick(() => {
    isUpdatingFromParent.value = false
  })
}, { deep: true })

// Methods
const addEscala = () => {
  const newEscala = {
    cantidadMinima: escalas.value.length === 0 ? 1 : getNextMinQuantity(),
    cantidadMaxima: null, // Se calcula automáticamente o se deja abierto
    porcentaje: 0
  }
  escalas.value.push(newEscala)
}

const removeEscala = (index) => {
  escalas.value.splice(index, 1)
}

const getNextMinQuantity = () => {
  if (escalas.value.length === 0) return 1
  
  // Encontrar la cantidad máxima definida, o la cantidad mínima más alta si no hay máximas definidas
  const cantidadesMaximas = escalas.value
    .map(e => e.cantidadMaxima)
    .filter(max => max !== null && max !== undefined)
    
  if (cantidadesMaximas.length === 0) {
    // Si no hay cantidades máximas definidas, usar la cantidad mínima más alta + 1
    const maxCantidadMinima = Math.max(...escalas.value.map(e => e.cantidadMinima))
    return maxCantidadMinima + 1
  }
  
  const maxQuantity = Math.max(...cantidadesMaximas)
  return maxQuantity + 1
}

const validateAndUpdate = () => {
  const errors = []
  const warnings = []
  
  if (escalas.value.length === 0) {
    validationErrors.value = errors
    validationWarnings.value = warnings
    emit('validation', {
      isValid: errors.length === 0,
      errors: errors,
      warnings: warnings
    })
    return
  }
  
  // Validar cada escala individualmente
  escalas.value.forEach((escala, index) => {
    // Validación básica de campos requeridos
    if (!escala.cantidadMinima || escala.cantidadMinima < 1) {
      errors.push(`Escala ${index + 1}: La cantidad mínima debe ser mayor a 0`)
    }
    
    if (escala.cantidadMaxima && escala.cantidadMaxima < escala.cantidadMinima) {
      errors.push(`Escala ${index + 1}: La cantidad máxima debe ser mayor a la mínima`)
    }
    
    if (!escala.porcentaje || escala.porcentaje <= 0 || escala.porcentaje > 100) {
      errors.push(`Escala ${index + 1}: El porcentaje debe estar entre 0.01 y 100`)
    }
    
    // Validaciones de negocio avanzadas
    if (escala.cantidadMinima && escala.cantidadMinima > 10000) {
      warnings.push(`Escala ${index + 1}: Cantidad mínima muy alta (${escala.cantidadMinima}), considere si es realista`)
    }
    
    if (escala.cantidadMaxima && escala.cantidadMaxima > 100000) {
      warnings.push(`Escala ${index + 1}: Cantidad máxima muy alta (${escala.cantidadMaxima}), considere si es realista`)
    }
    
    if (escala.porcentaje && escala.porcentaje > 50) {
      warnings.push(`Escala ${index + 1}: Descuento muy alto (${escala.porcentaje}%), verifique impacto financiero`)
    }
  })
  
  // Validaciones de conjunto (múltiples escalas)
  if (escalas.value.length > 1) {
    const sorted = [...escalas.value].sort((a, b) => a.cantidadMinima - b.cantidadMinima)
    
    // 1. Validar solapamientos
    for (let i = 0; i < sorted.length - 1; i++) {
      const current = sorted[i]
      const next = sorted[i + 1]
      
      const currentMax = current.cantidadMaxima || 999999
      if (next.cantidadMinima <= currentMax) {
        errors.push(`Las escalas se solapan: rango ${current.cantidadMinima}-${currentMax} con ${next.cantidadMinima}-${next.cantidadMaxima || '∞'}`)
      }
    }
    
    // 2. Validar progresión lógica de descuentos (mayor cantidad = mayor descuento)
    for (let i = 0; i < sorted.length - 1; i++) {
      const current = sorted[i]
      const next = sorted[i + 1]
      
      if (current.porcentaje && next.porcentaje && next.porcentaje <= current.porcentaje) {
        errors.push(`Progresión ilógica: escala con mayor cantidad (${next.cantidadMinima}+) tiene menor/igual descuento (${next.porcentaje}%) que escala anterior (${current.porcentaje}%)`)
      }
    }
    
    // 3. Detectar gaps significativos entre rangos
    for (let i = 0; i < sorted.length - 1; i++) {
      const current = sorted[i]
      const next = sorted[i + 1]
      
      const currentMax = current.cantidadMaxima
      if (currentMax && next.cantidadMinima > currentMax + 1) {
        const gap = next.cantidadMinima - currentMax - 1
        if (gap > 1) {
          warnings.push(`Hay un vacío de ${gap} unidades entre escalas: ${currentMax} a ${next.cantidadMinima}`)
        }
      }
    }
    
    // 4. Validar incrementos mínimos significativos
    for (let i = 0; i < sorted.length - 1; i++) {
      const current = sorted[i]
      const next = sorted[i + 1]
      
      if (current.porcentaje && next.porcentaje) {
        const incremento = next.porcentaje - current.porcentaje
        if (incremento < 1 && incremento > 0) {
          warnings.push(`Incremento muy pequeño entre escalas: ${incremento.toFixed(2)}% podría no ser significativo para el cliente`)
        }
      }
    }
    
    // 5. Validar cobertura desde cantidad 1
    const primeraEscala = sorted[0]
    if (primeraEscala.cantidadMinima > 1) {
      warnings.push(`La primera escala inicia en ${primeraEscala.cantidadMinima}. Considere cubrir desde cantidad 1`)
    }
  }
  
  // 6. Validación de escalas excesivas
  if (escalas.value.length > 10) {
    warnings.push(`${escalas.value.length} escalas podrían ser demasiadas. Considere simplificar para mejor experiencia del cliente`)
  }
  
  validationErrors.value = errors
  validationWarnings.value = warnings
  
  // Emitir estado de validación con warnings
  emit('validation', {
    isValid: errors.length === 0,
    errors: errors,
    warnings: warnings
  })
}

const hasValidationError = (index) => {
  return validationErrors.value.some(error => error.includes(`Escala ${index + 1}`))
}

const hasFieldError = (index, field) => {
  const escala = escalas.value[index]
  switch (field) {
    case 'cantidadMinima':
      return !escala.cantidadMinima || escala.cantidadMinima < 1
    case 'cantidadMaxima':
      return escala.cantidadMaxima && escala.cantidadMaxima < escala.cantidadMinima
    case 'porcentaje':
      return !escala.porcentaje || escala.porcentaje <= 0 || escala.porcentaje > 100
    default:
      return false
  }
}

const formatSavings = (escala) => {
  if (!escala.porcentaje || !simulatorPrice.value) return '-'
  const savings = simulatorPrice.value * (escala.porcentaje / 100)
  return `$${savings.toFixed(2)}`
}

// Initialize validation
validateAndUpdate()
</script>

<style scoped>
/* Estilos específicos para el editor si son necesarios */
.escalas-descuento-editor {
  /* Estilos base ya cubiertos por Tailwind */
}
</style>