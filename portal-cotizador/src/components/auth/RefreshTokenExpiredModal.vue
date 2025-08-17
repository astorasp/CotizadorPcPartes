<template>
  <div
    v-if="show"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
  >
    <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md mx-4">
      <!-- Header -->
      <div class="flex items-center mb-6">
        <div class="flex-shrink-0">
          <ExclamationCircleIcon class="h-8 w-8 text-red-500" />
        </div>
        <div class="ml-3">
          <h3 class="text-lg font-medium text-gray-900">
            Sesión Expirada
          </h3>
        </div>
      </div>

      <!-- Content -->
      <div class="mb-6">
        <p class="text-sm text-gray-600 mb-4">
          {{ message }}
        </p>
        <p class="text-sm text-gray-600">
          Para continuar usando la aplicación, deberá iniciar sesión nuevamente.
        </p>
      </div>

      <!-- Info box -->
      <div class="mb-6 bg-red-50 border border-red-200 rounded-md p-3">
        <div class="flex">
          <div class="flex-shrink-0">
            <InformationCircleIcon class="h-4 w-4 text-red-400" />
          </div>
          <div class="ml-3">
            <p class="text-xs text-red-700">
              <strong>Importante:</strong> {{ infoMessage }}
            </p>
          </div>
        </div>
      </div>

      <!-- Action -->
      <div class="flex justify-center">
        <button
          @click="handleGoToLogin"
          class="w-full px-4 py-2 bg-red-600 text-white hover:bg-red-700 rounded-md transition-colors flex items-center justify-center"
        >
          <ArrowRightOnRectangleIcon class="h-4 w-4 mr-2" />
          Ir a Iniciar Sesión
        </button>
      </div>

      <!-- Footer info -->
      <div class="mt-4 text-xs text-gray-500 text-center">
        Será redirigido automáticamente a la página de inicio de sesión
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { 
  ExclamationCircleIcon, 
  InformationCircleIcon,
  ArrowRightOnRectangleIcon 
} from '@heroicons/vue/24/outline'

// Props
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  message: {
    type: String,
    default: 'Su sesión ha expirado completamente. Debe volver a autenticarse.'
  },
  reason: {
    type: String,
    default: 'refresh_token_expired'
  }
})

// Emits
const emit = defineEmits(['close'])

// Computed
const infoMessage = computed(() => {
  if (props.reason === 'session_renewal_limit_reached') {
    return 'Ha alcanzado el número máximo de renovaciones permitidas para esta sesión. Esto es una medida de seguridad del sistema.'
  } else {
    return 'Su sesión ha alcanzado el tiempo máximo permitido. Esto es una medida de seguridad del sistema.'
  }
})

// Methods
const handleGoToLogin = () => {
  emit('close')
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

/* Efecto de pulsación para el ícono de error */
.animate-pulse-slow {
  animation: pulse-slow 3s infinite;
}

@keyframes pulse-slow {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}
</style>