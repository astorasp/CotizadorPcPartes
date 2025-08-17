<template>
  <Teleport to="body">
    <div class="fixed top-4 right-4 z-50 space-y-2">
      <TransitionGroup name="alert" tag="div">
        <div
          v-for="alert in alerts"
          :key="alert.id"
          :class="alertClasses(alert.type)"
          class="min-w-96 max-w-md rounded-lg shadow-lg"
        >
          <div class="flex items-center p-4">
            <!-- Icono según tipo -->
            <div class="flex-shrink-0">
              <CheckCircleIcon v-if="alert.type === 'success'" class="w-5 h-5" />
              <ExclamationCircleIcon v-else-if="alert.type === 'error'" class="w-5 h-5" />
              <ExclamationTriangleIcon v-else-if="alert.type === 'warning'" class="w-5 h-5" />
              <InformationCircleIcon v-else class="w-5 h-5" />
            </div>
            
            <!-- Mensaje -->
            <div class="ml-3 flex-1">
              <p class="text-sm font-medium">
                {{ alert.message }}
              </p>
            </div>
            
            <!-- Botón cerrar -->
            <div class="ml-4 flex-shrink-0">
              <button
                @click="closeAlert(alert.id)"
                class="inline-flex rounded-md p-1.5 focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors"
                :class="closeButtonClasses(alert.type)"
              >
                <XMarkIcon class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import { useUtils } from '@/composables/useUtils'
import {
  CheckCircleIcon,
  ExclamationCircleIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  XMarkIcon
} from '@heroicons/vue/24/outline'

const { alerts, closeAlert } = useUtils()

// Clases CSS para diferentes tipos de alerta
const alertClasses = (type) => {
  const baseClasses = 'alert-base'
  
  switch (type) {
    case 'success':
      return `${baseClasses} alert-success`
    case 'error':
      return `${baseClasses} alert-error`
    case 'warning':
      return `${baseClasses} alert-warning`
    case 'info':
    default:
      return `${baseClasses} alert-info`
  }
}

// Clases para botón de cierre
const closeButtonClasses = (type) => {
  switch (type) {
    case 'success':
      return 'text-success-500 hover:bg-success-100 focus:ring-success-600'
    case 'error':
      return 'text-danger-500 hover:bg-danger-100 focus:ring-danger-600'
    case 'warning':
      return 'text-warning-500 hover:bg-warning-100 focus:ring-warning-600'
    case 'info':
    default:
      return 'text-blue-500 hover:bg-blue-100 focus:ring-blue-600'
  }
}
</script>

<style scoped>
.alert-enter-active {
  transition: all 0.3s ease-out;
}

.alert-leave-active {
  transition: all 0.3s ease-in;
}

.alert-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.alert-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.alert-move {
  transition: transform 0.3s ease;
}
</style>