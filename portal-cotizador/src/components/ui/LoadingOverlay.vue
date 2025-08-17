<template>
  <Teleport to="body">
    <Transition name="overlay-fade">
      <div 
        v-if="show"
        class="loading-overlay"
        :class="{ 'overlay-blocking': blockInteraction }"
        @click="handleOverlayClick"
      >
        <div class="overlay-content" @click.stop>
          <!-- Spinner principal -->
          <LoadingSpinner 
            :size="spinnerSize"
            :color="spinnerColor"
            :message="displayMessage"
            centered
          />
          
          <!-- Barra de progreso si se proporciona -->
          <div v-if="showProgress" class="progress-container">
            <div class="progress-bar">
              <div 
                class="progress-fill"
                :style="{ width: `${progress}%` }"
                :class="progressColorClass"
              ></div>
            </div>
            <div class="progress-text">
              {{ progress }}%
            </div>
          </div>
          
          <!-- Botón de cancelar si es cancelable -->
          <button
            v-if="cancellable && onCancel"
            @click="handleCancel"
            class="cancel-button"
          >
            Cancelar
          </button>
          
          <!-- Lista de operaciones activas si hay múltiples -->
          <div v-if="showOperationsList && operations.length > 1" class="operations-list">
            <div class="operations-title">Operaciones en progreso:</div>
            <div 
              v-for="operation in operations" 
              :key="operation.key"
              class="operation-item"
            >
              <LoadingSpinner size="xs" :color="spinnerColor" />
              <span class="operation-message">{{ operation.message }}</span>
              <span v-if="operation.progress > 0" class="operation-progress">
                {{ operation.progress }}%
              </span>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import LoadingSpinner from './LoadingSpinner.vue'

const props = defineProps({
  // Control de visibilidad
  show: {
    type: Boolean,
    default: false
  },
  
  // Mensaje principal
  message: {
    type: String,
    default: 'Cargando...'
  },
  
  // Progreso (0-100)
  progress: {
    type: Number,
    default: 0,
    validator: (value) => value >= 0 && value <= 100
  },
  
  // Si se puede cancelar
  cancellable: {
    type: Boolean,
    default: false
  },
  
  // Si bloquea toda la interacción
  blockInteraction: {
    type: Boolean,
    default: true
  },
  
  // Tamaño del spinner
  spinnerSize: {
    type: String,
    default: 'lg'
  },
  
  // Color del spinner  
  spinnerColor: {
    type: String,
    default: 'primary'
  },
  
  // Operaciones activas (para mostrar lista)
  operations: {
    type: Array,
    default: () => []
  },
  
  // Si mostrar la lista de operaciones
  showOperationsList: {
    type: Boolean,
    default: false
  },
  
  // Tipo de overlay
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'modal', 'fullscreen'].includes(value)
  }
})

const emit = defineEmits(['cancel', 'click'])

// Computadas
const showProgress = computed(() => props.progress > 0)

const displayMessage = computed(() => {
  if (props.operations.length === 1) {
    return props.operations[0].message || props.message
  }
  return props.message
})

const progressColorClass = computed(() => {
  const colors = {
    primary: 'bg-primary-600',
    secondary: 'bg-gray-600',
    success: 'bg-green-600',
    warning: 'bg-yellow-600',
    error: 'bg-red-600'
  }
  return colors[props.spinnerColor] || colors.primary
})

// Métodos
const handleCancel = () => {
  emit('cancel')
}

const handleOverlayClick = () => {
  if (!props.blockInteraction) {
    emit('click')
  }
}
</script>

<style scoped>
/* Overlay base */
.loading-overlay {
  @apply fixed inset-0 z-50 flex items-center justify-center;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
}

.loading-overlay.overlay-blocking {
  @apply cursor-wait;
}

/* Contenido del overlay */
.overlay-content {
  @apply bg-white rounded-lg shadow-xl p-8 max-w-md w-full mx-4;
  min-width: 300px;
}

/* Barra de progreso */
.progress-container {
  @apply mt-6 space-y-2;
}

.progress-bar {
  @apply w-full bg-gray-200 rounded-full h-2 overflow-hidden;
}

.progress-fill {
  @apply h-full transition-all duration-300 ease-out rounded-full;
}

.progress-text {
  @apply text-center text-sm text-gray-600 font-medium;
}

/* Botón cancelar */
.cancel-button {
  @apply mt-6 w-full px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 border border-gray-300 rounded-md;
  @apply hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500;
  @apply transition-colors duration-200;
}

/* Lista de operaciones */
.operations-list {
  @apply mt-6 space-y-3;
}

.operations-title {
  @apply text-sm font-medium text-gray-700 mb-3;
}

.operation-item {
  @apply flex items-center space-x-3 p-2 bg-gray-50 rounded-md;
}

.operation-message {
  @apply flex-1 text-sm text-gray-600;
}

.operation-progress {
  @apply text-xs text-gray-500 font-medium;
}

/* Transiciones */
.overlay-fade-enter-active,
.overlay-fade-leave-active {
  @apply transition-all duration-300;
}

.overlay-fade-enter-from,
.overlay-fade-leave-to {
  @apply opacity-0;
}

.overlay-fade-enter-to,
.overlay-fade-leave-from {
  @apply opacity-100;
}

.overlay-fade-enter-from .overlay-content,
.overlay-fade-leave-to .overlay-content {
  @apply scale-95 opacity-0;
  transform: scale(0.95) translateY(-10px);
}

.overlay-fade-enter-to .overlay-content,
.overlay-fade-leave-from .overlay-content {
  @apply scale-100 opacity-100;
  transform: scale(1) translateY(0);
}

/* Variantes de tipo */
.loading-overlay[data-type="modal"] .overlay-content {
  @apply max-w-lg;
}

.loading-overlay[data-type="fullscreen"] {
  @apply bg-white bg-opacity-95;
}

.loading-overlay[data-type="fullscreen"] .overlay-content {
  @apply bg-transparent shadow-none max-w-none;
}
</style>