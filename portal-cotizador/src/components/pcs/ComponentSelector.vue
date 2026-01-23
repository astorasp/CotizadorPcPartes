<template>
  <div class="space-y-4">
    <!-- Filtros de búsqueda -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <!-- Tipo de componente -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          Tipo de Componente
        </label>
        <select
          v-model="selectedType"
          class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
        >
          <option value="">Todos los tipos</option>
          <option v-for="(label, key) in componentTypes" :key="key" :value="key">
            {{ label }}
          </option>
        </select>
      </div>

      <!-- Búsqueda por texto -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          Buscar componente
        </label>
        <input
          v-model="searchText"
          type="text"
          placeholder="ID, descripción, marca..."
          class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
        />
      </div>

      <!-- Cantidad -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          Cantidad
        </label>
        <input
          v-model.number="quantity"
          type="number"
          min="1"
          max="10"
          class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
        />
      </div>
    </div>

    <!-- Lista de componentes disponibles -->
    <div class="border rounded-lg">
      <div class="px-4 py-2 bg-gray-50 border-b font-medium text-gray-900">
        Componentes Disponibles ({{ filteredComponents.length }})
      </div>
      
      <div class="max-h-64 overflow-y-auto">
        <!-- Estado vacío -->
        <div v-if="filteredComponents.length === 0" class="p-4 text-center text-gray-500">
          <svg class="mx-auto h-8 w-8 text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6-4h6m2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
          </svg>
          <p>No se encontraron componentes</p>
          <p class="text-sm">Ajuste los filtros de búsqueda</p>
        </div>

        <!-- Lista de componentes -->
        <div v-else class="divide-y divide-gray-200">
          <div
            v-for="component in filteredComponents"
            :key="component.id"
            class="p-3 hover:bg-gray-50 cursor-pointer flex items-center justify-between"
            @click="selectComponent(component)"
            :class="{
              'bg-green-50 border-l-4 border-green-500': isComponentSelected(component.id)
            }"
          >
            <div class="flex-1">
              <div class="flex items-center space-x-3">
                <div class="flex-1">
                  <div class="font-medium text-gray-900">{{ component.id }}</div>
                  <div class="text-sm text-gray-600">{{ component.descripcion }}</div>
                  <div class="text-xs text-gray-500">{{ component.marca }} {{ component.modelo }}</div>
                </div>
                
                <div class="text-right">
                  <div class="text-sm font-medium text-gray-900">{{ formatCurrency(component.precioBase) }}</div>
                  <span class="inline-flex px-2 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-800">
                    {{ getTypeLabel(component.tipoComponente) }}
                  </span>
                </div>
              </div>
            </div>

            <div class="ml-3">
              <div
                v-if="isComponentSelected(component.id)"
                class="flex items-center text-green-600"
              >
                <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                </svg>
                <span class="ml-1 text-sm font-medium">Seleccionado</span>
              </div>
              <button
                v-else
                class="text-primary-600 hover:text-primary-800 text-sm font-medium"
              >
                Seleccionar
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useUtils } from '@/composables/useUtils'
import { COMPONENT_TYPE_LABELS } from '@/utils/constants'

// Props
const props = defineProps({
  availableComponents: {
    type: Array,
    default: () => []
  },
  selectedComponents: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['select-component', 'update:selectedComponents'])

// Composables
const { formatCurrency } = useUtils()

// Estado local
const selectedType = ref('')
const searchText = ref('')
const quantity = ref(1)

// Computed
const componentTypes = computed(() => {
  // Excluir 'PC' del filtro de tipos para subcomponentes
  const { PC, ...typesWithoutPc } = COMPONENT_TYPE_LABELS
  return typesWithoutPc
})

const filteredComponents = computed(() => {
  let components = props.availableComponents || []

  // Filtrar por tipo
  if (selectedType.value) {
    components = components.filter(c => c.tipoComponente === selectedType.value)
  }

  // Filtrar por texto de búsqueda
  if (searchText.value.trim()) {
    const search = searchText.value.toLowerCase().trim()
    components = components.filter(c => 
      c.id?.toLowerCase().includes(search) ||
      c.descripcion?.toLowerCase().includes(search) ||
      c.marca?.toLowerCase().includes(search) ||
      c.modelo?.toLowerCase().includes(search)
    )
  }

  return components
})

// Métodos
const getTypeLabel = (tipo) => {
  return COMPONENT_TYPE_LABELS[tipo] || tipo
}

const isComponentSelected = (componentId) => {
  return props.selectedComponents.some(c => c.id === componentId)
}

const selectComponent = (component) => {
  if (isComponentSelected(component.id)) {
    // Desseleccionar - quitar del array
    const updated = props.selectedComponents.filter(c => c.id !== component.id)
    emit('update:selectedComponents', updated)
  } else {
    // Seleccionar - agregar al array con cantidad
    const componentWithQuantity = {
      ...component,
      cantidad: quantity.value
    }
    const updated = [...props.selectedComponents, componentWithQuantity]
    emit('update:selectedComponents', updated)
  }
  
  emit('select-component', component)
}

// Limpiar filtros cuando cambian los componentes disponibles
watch(() => props.availableComponents, () => {
  selectedType.value = ''
  searchText.value = ''
})
</script>