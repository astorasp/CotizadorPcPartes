<template>
  <div class="text-sm text-gray-900">
    <span 
      :class="countBadgeClass"
      class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
    >
      {{ displayText }}
    </span>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { pcsApi } from '@/services/pcsApi'

const props = defineProps({
  pc: {
    type: Object,
    required: true
  }
})

// Estado local
const componentCount = ref(null)
const loading = ref(false)
const error = ref(false)

// Computed properties
const displayText = computed(() => {
  if (loading.value) return 'Cargando...'
  if (error.value) return 'Error'
  if (componentCount.value === null) return 'Cargando...'
  
  const count = componentCount.value
  return `${count} componente${count !== 1 ? 's' : ''}`
})

const countBadgeClass = computed(() => {
  if (loading.value) return 'bg-gray-100 text-gray-800'
  if (error.value) return 'bg-red-100 text-red-800'
  if (componentCount.value === null) return 'bg-gray-100 text-gray-800'
  
  const count = componentCount.value
  if (count > 0) {
    return 'bg-green-100 text-green-800'
  } else {
    return 'bg-gray-100 text-gray-800'
  }
})

// Métodos
const loadComponentCount = async () => {
  try {
    loading.value = true
    error.value = false
    
    const components = await pcsApi.getComponents(props.pc.id)
    componentCount.value = components.length
    
  } catch (err) {
    console.error(`Error loading components count for PC ${props.pc.id}:`, err)
    error.value = true
    componentCount.value = 0
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(() => {
  // Si el PC ya tiene el conteo cargado, usarlo
  if (props.pc._componentCount !== undefined) {
    componentCount.value = props.pc._componentCount
    error.value = !props.pc._componentCountLoaded
  } else {
    // Cargar el conteo si no está disponible
    loadComponentCount()
  }
})

// Watch para actualizar cuando cambie el PC
watch(() => props.pc.id, () => {
  loadComponentCount()
})

// Watch para actualizar cuando el store actualice los conteos
watch(() => props.pc._componentCount, (newCount) => {
  if (newCount !== undefined) {
    componentCount.value = newCount
    error.value = !props.pc._componentCountLoaded
  }
})
</script>