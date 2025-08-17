<template>
  <!-- Overlay global para operaciones que bloquean la UI -->
  <LoadingOverlay
    :show="hasBlockingOperations"
    :message="primaryBlockingOperation?.message || 'Procesando...'"
    :progress="primaryBlockingOperation?.progress || 0"
    :cancellable="primaryBlockingOperation?.cancellable || false"
    :operations="blockingOperations"
    :show-operations-list="blockingOperations.length > 1"
    block-interaction
    @cancel="handleCancel"
  />
  
  <!-- Indicador global en la esquina para operaciones no bloqueantes -->
  <Transition name="indicator-slide">
    <div 
      v-if="hasNonBlockingOperations" 
      class="global-loading-indicator"
      @click="toggleIndicatorExpanded"
    >
      <div class="indicator-header">
        <LoadingSpinner size="xs" color="white" />
        <span class="indicator-text">
          {{ nonBlockingOperations.length }} operación{{ nonBlockingOperations.length !== 1 ? 'es' : '' }} en progreso
        </span>
        <button class="indicator-toggle" @click.stop="toggleIndicatorExpanded">
          <svg 
            :class="{ 'rotate-180': indicatorExpanded }"
            class="w-4 h-4 transition-transform"
            fill="none" 
            viewBox="0 0 24 24" 
            stroke="currentColor"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </button>
      </div>
      
      <!-- Lista expandible de operaciones -->
      <Transition name="indicator-expand">
        <div v-if="indicatorExpanded" class="indicator-operations">
          <div 
            v-for="operation in nonBlockingOperations"
            :key="operation.key"
            class="indicator-operation"
          >
            <LoadingSpinner size="xs" color="white" />
            <span class="operation-text">{{ operation.message }}</span>
            <span v-if="operation.progress > 0" class="operation-progress">
              {{ operation.progress }}%
            </span>
          </div>
        </div>
      </Transition>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useLoadingStore } from '@/stores/useLoadingStore'
import LoadingOverlay from './LoadingOverlay.vue'
import LoadingSpinner from './LoadingSpinner.vue'

const loadingStore = useLoadingStore()

// Estado local
const indicatorExpanded = ref(false)

// Computadas para filtrar operaciones
const blockingOperations = computed(() => {
  return (loadingStore.getActiveOperations.value || [])
    .filter(([key, operation]) => operation.blockUI)
    .map(([key, operation]) => operation)
})

const nonBlockingOperations = computed(() => {
  return (loadingStore.getActiveOperations.value || [])
    .filter(([key, operation]) => !operation.blockUI)
    .map(([key, operation]) => operation)
})

const hasBlockingOperations = computed(() => {
  return blockingOperations.value.length > 0
})

const hasNonBlockingOperations = computed(() => {
  return nonBlockingOperations.value.length > 0
})

const primaryBlockingOperation = computed(() => {
  return blockingOperations.value[0] || null
})

// Métodos
const toggleIndicatorExpanded = () => {
  indicatorExpanded.value = !indicatorExpanded.value
}

const handleCancel = () => {
  if (primaryBlockingOperation.value?.cancellable) {
    loadingStore.endOperation(primaryBlockingOperation.value.key)
    // Emitir evento de cancelación si es necesario
    // emit('operation-cancelled', primaryBlockingOperation.value.key)
  }
}

// Auto-colapsar el indicador cuando no hay operaciones
const checkAutoCollapse = () => {
  if (nonBlockingOperations.value.length === 0) {
    indicatorExpanded.value = false
  }
}

// Lifecycle
onMounted(() => {
  // Colapsar automáticamente cuando no hay operaciones
  const unwatch = loadingStore.$subscribe(() => {
    checkAutoCollapse()
  })
  
  onUnmounted(() => {
    unwatch()
  })
})
</script>

<style scoped>
/* Indicador global */
.global-loading-indicator {
  @apply fixed bottom-4 right-4 z-40;
  @apply bg-primary-600 text-white rounded-lg shadow-lg;
  @apply cursor-pointer select-none;
  @apply transition-all duration-300;
  min-width: 250px;
  max-width: 350px;
}

.global-loading-indicator:hover {
  @apply bg-primary-700 shadow-xl;
  transform: translateY(-1px);
}

/* Header del indicador */
.indicator-header {
  @apply flex items-center p-3 space-x-2;
}

.indicator-text {
  @apply flex-1 text-sm font-medium;
}

.indicator-toggle {
  @apply p-1 rounded hover:bg-primary-500 transition-colors;
}

/* Lista de operaciones */
.indicator-operations {
  @apply border-t border-primary-500 bg-primary-700;
  @apply max-h-40 overflow-y-auto;
}

.indicator-operation {
  @apply flex items-center p-2 space-x-2;
  @apply border-b border-primary-600 last:border-b-0;
}

.operation-text {
  @apply flex-1 text-xs;
}

.operation-progress {
  @apply text-xs font-medium bg-primary-500 px-2 py-1 rounded;
}

/* Transiciones */
.indicator-slide-enter-active,
.indicator-slide-leave-active {
  @apply transition-all duration-300;
}

.indicator-slide-enter-from,
.indicator-slide-leave-to {
  @apply opacity-0 translate-x-full;
}

.indicator-slide-enter-to,
.indicator-slide-leave-from {
  @apply opacity-100 translate-x-0;
}

.indicator-expand-enter-active,
.indicator-expand-leave-active {
  @apply transition-all duration-200;
}

.indicator-expand-enter-from,
.indicator-expand-leave-to {
  @apply opacity-0 max-h-0;
}

.indicator-expand-enter-to,
.indicator-expand-leave-from {
  @apply opacity-100 max-h-40;
}

/* Scrollbar personalizado para la lista de operaciones */
.indicator-operations::-webkit-scrollbar {
  width: 4px;
}

.indicator-operations::-webkit-scrollbar-track {
  @apply bg-primary-600;
}

.indicator-operations::-webkit-scrollbar-thumb {
  @apply bg-primary-400 rounded;
}

.indicator-operations::-webkit-scrollbar-thumb:hover {
  @apply bg-primary-300;
}
</style>