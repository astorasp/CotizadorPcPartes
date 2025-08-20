<template>
  <div class="border rounded-lg">
    <!-- Encabezado -->
    <div class="px-4 py-3 bg-gray-50 border-b flex items-center justify-between">
      <h4 class="font-medium text-gray-900">
        Componentes Seleccionados ({{ components.length }})
      </h4>
      <div class="text-sm text-gray-600">
        Total: {{ formatCurrency(totalCost) }}
      </div>
    </div>

    <!-- Estado vacío -->
    <div v-if="components.length === 0" class="p-8 text-center">
      <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 002 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
      </svg>
      <h3 class="text-sm font-medium text-gray-900 mb-1">No hay componentes seleccionados</h3>
      <p class="text-sm text-gray-500">Seleccione componentes del listado de arriba para armar la PC</p>
    </div>

    <!-- Lista de componentes -->
    <div v-else class="divide-y divide-gray-200">
      <div
        v-for="(component, index) in components"
        :key="component.id"
        class="p-4 hover:bg-gray-50"
      >
        <div class="flex items-center space-x-4">
          <!-- Información del componente -->
          <div class="flex-1">
            <div class="flex items-start justify-between">
              <div class="min-w-0 flex-1">
                <div class="flex items-center space-x-2">
                  <div class="font-medium text-gray-900">{{ component.id }}</div>
                  <span class="inline-flex px-2 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-800">
                    {{ getTypeLabel(component.tipoComponente) }}
                  </span>
                </div>
                <div class="text-sm text-gray-600 mt-1">{{ component.descripcion }}</div>
                <div class="text-xs text-gray-500 mt-1">
                  {{ component.marca }} {{ component.modelo }}
                  <span v-if="component.capacidadAlm" class="ml-2">{{ component.capacidadAlm }}</span>
                  <span v-if="component.memoria" class="ml-2">{{ component.memoria }}</span>
                </div>
              </div>

              <!-- Precios -->
              <div class="text-right ml-4">
                <div class="text-sm font-medium text-gray-900">
                  {{ formatCurrency(component.precioBase) }}
                </div>
                <div class="text-xs text-gray-500">
                  Costo: {{ formatCurrency(component.costo) }}
                </div>
              </div>
            </div>
          </div>

          <!-- Controles de cantidad -->
          <div class="flex items-center space-x-2">
            <label class="text-sm text-gray-700">Cant:</label>
            <div class="flex items-center border rounded-md">
              <button
                @click="decrementQuantity(index)"
                :disabled="component.cantidad <= 1"
                class="px-2 py-1 text-sm text-gray-600 hover:text-gray-800 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                -
              </button>
              <input
                :value="component.cantidad || 1"
                @input="updateQuantity(index, $event.target.value)"
                type="number"
                min="1"
                max="10"
                class="w-12 px-2 py-1 text-sm text-center border-0 focus:ring-0"
              />
              <button
                @click="incrementQuantity(index)"
                :disabled="component.cantidad >= 10"
                class="px-2 py-1 text-sm text-gray-600 hover:text-gray-800 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                +
              </button>
            </div>
          </div>

          <!-- Subtotal -->
          <div class="text-right min-w-0">
            <div class="text-sm font-medium text-gray-900">
              {{ formatCurrency(calculateSubtotal(component)) }}
            </div>
            <div class="text-xs text-gray-500">
              {{ component.cantidad || 1 }} × {{ formatCurrency(component.precioBase) }}
            </div>
          </div>

          <!-- Botón eliminar -->
          <div>
            <button
              @click="removeComponent(index)"
              class="text-red-600 hover:text-red-800 p-1 rounded-md hover:bg-red-50"
              title="Quitar componente"
            >
              <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Resumen de costos -->
    <div v-if="components.length > 0" class="px-4 py-3 bg-gray-50 border-t">
      <div class="space-y-2">
        <div class="flex justify-between text-sm">
          <span class="text-gray-600">Subtotal ({{ totalQuantity }} componentes):</span>
          <span class="font-medium">{{ formatCurrency(subtotal) }}</span>
        </div>
        <div class="flex justify-between text-sm">
          <span class="text-gray-600">Costo total:</span>
          <span class="font-medium">{{ formatCurrency(totalCostPrice) }}</span>
        </div>
        <div class="flex justify-between text-base font-medium border-t pt-2">
          <span>Total PC:</span>
          <span class="text-primary-600">{{ formatCurrency(totalCost) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUtils } from '@/composables/useUtils'
import { COMPONENT_TYPE_LABELS } from '@/utils/constants'

// Props
const props = defineProps({
  components: {
    type: Array,
    default: () => []
  },
  readonly: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['update:components', 'remove', 'update-quantity'])

// Composables
const { formatCurrency } = useUtils()

// Computed
const totalQuantity = computed(() => {
  return props.components.reduce((total, comp) => total + (comp.cantidad || 1), 0)
})

const subtotal = computed(() => {
  return props.components.reduce((total, comp) => {
    return total + (comp.precioBase * (comp.cantidad || 1))
  }, 0)
})

const totalCostPrice = computed(() => {
  return props.components.reduce((total, comp) => {
    return total + (comp.costo * (comp.cantidad || 1))
  }, 0)
})

const totalCost = computed(() => subtotal.value)

// Métodos
const getTypeLabel = (tipo) => {
  return COMPONENT_TYPE_LABELS[tipo] || tipo
}

const calculateSubtotal = (component) => {
  return component.precioBase * (component.cantidad || 1)
}

const removeComponent = (index) => {
  const updatedComponents = [...props.components]
  updatedComponents.splice(index, 1)
  emit('update:components', updatedComponents)
  emit('remove', index)
}

const updateQuantity = (index, value) => {
  const quantity = Math.max(1, Math.min(10, parseInt(value) || 1))
  const updatedComponents = [...props.components]
  updatedComponents[index] = {
    ...updatedComponents[index],
    cantidad: quantity
  }
  emit('update:components', updatedComponents)
  emit('update-quantity', { index, quantity })
}

const incrementQuantity = (index) => {
  const currentQuantity = props.components[index].cantidad || 1
  if (currentQuantity < 10) {
    updateQuantity(index, currentQuantity + 1)
  }
}

const decrementQuantity = (index) => {
  const currentQuantity = props.components[index].cantidad || 1
  if (currentQuantity > 1) {
    updateQuantity(index, currentQuantity - 1)
  }
}
</script>