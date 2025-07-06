<template>
  <div
    v-if="show"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    @click="onBackdropClick"
  >
    <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md mx-4">
      <!-- Header -->
      <div class="flex items-center mb-6">
        <div class="flex-shrink-0">
          <ExclamationTriangleIcon class="h-8 w-8 text-yellow-500" />
        </div>
        <div class="ml-3">
          <h3 class="text-lg font-medium text-gray-900">
            Su sesión está por expirar
          </h3>
        </div>
      </div>

      <!-- Content -->
      <div class="mb-6">
        <p class="text-sm text-gray-600 mb-4">
          Su sesión expirará en <span class="font-bold text-red-600">{{ secondsUntilExpiry }}</span> segundos.
        </p>
        <p class="text-sm text-gray-600">
          ¿Desea extender el tiempo de su sesión? Si no responde, será desconectado automáticamente.
        </p>
      </div>

      <!-- Progress bar -->
      <div class="mb-6">
        <div class="w-full bg-gray-200 rounded-full h-2">
          <div 
            class="bg-red-500 h-2 rounded-full transition-all duration-1000"
            :style="{ width: `${progressPercentage}%` }"
          ></div>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex justify-end space-x-3">
        <button
          @click="handleReject"
          :disabled="isRenewing"
          class="px-4 py-2 text-gray-700 bg-gray-200 hover:bg-gray-300 rounded-md transition-colors disabled:opacity-50"
        >
          Cerrar Sesión
        </button>
        <button
          @click="handleExtend"
          :disabled="isRenewing"
          class="px-4 py-2 bg-primary-600 text-white hover:bg-primary-700 rounded-md transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
        >
          <div v-if="isRenewing" class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
          {{ isRenewing ? 'Extendiendo...' : 'Extender Sesión' }}
        </button>
      </div>

      <!-- Auto logout warning -->
      <div class="mt-4 text-xs text-gray-500 text-center">
        La sesión se cerrará automáticamente si no responde
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import { ExclamationTriangleIcon } from '@heroicons/vue/24/outline'

// Props
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  secondsUntilExpiry: {
    type: Number,
    default: 0
  },
  isRenewing: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['extend', 'reject', 'close'])

// Computed
const progressPercentage = computed(() => {
  const maxSeconds = Math.max(30, props.secondsUntilExpiry || 30) // Usar el tiempo inicial o 30 como mínimo
  const remaining = props.secondsUntilExpiry
  return Math.max(0, Math.min(100, (remaining / maxSeconds) * 100))
})

// Debug - watch props changes
watch(() => props.show, (newValue) => {
  console.log('[SessionExpirationModal] Show prop changed:', newValue)
})

watch(() => props.secondsUntilExpiry, (newValue) => {
  // Solo log cuando es relevante (cada 10 segundos o menos de 10)
  if (newValue <= 10 || newValue % 10 === 0) {
    console.log('[SessionExpirationModal] Seconds until expiry:', newValue)
  }
})

// Methods
const handleExtend = () => {
  emit('extend')
}

const handleReject = () => {
  emit('reject')
}

const onBackdropClick = (event) => {
  // No permitir cerrar haciendo click fuera - forzar decisión
  // if (event.target === event.currentTarget) {
  //   emit('close')
  // }
}
</script>

<style scoped>
/* Animación para el modal */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

/* Animación de pulsación para el ícono de advertencia */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.animate-pulse {
  animation: pulse 2s infinite;
}
</style>