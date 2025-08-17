<template>
  <Teleport to="body">
    <Transition name="modal" appear>
      <div
        v-if="show"
        class="fixed inset-0 z-50 overflow-y-auto"
        aria-labelledby="modal-title"
        role="dialog"
        aria-modal="true"
      >
        <!-- Overlay -->
        <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
          <Transition
            name="modal-overlay"
            enter-active-class="ease-out duration-300"
            enter-from-class="opacity-0"
            enter-to-class="opacity-100"
            leave-active-class="ease-in duration-200"
            leave-from-class="opacity-100"
            leave-to-class="opacity-0"
          >
            <div
              v-if="show"
              class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"
              @click="$emit('cancel')"
            ></div>
          </Transition>

          <!-- Modal Content -->
          <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
          
          <Transition
            name="modal-content"
            enter-active-class="ease-out duration-300"
            enter-from-class="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
            enter-to-class="opacity-100 translate-y-0 sm:scale-100"
            leave-active-class="ease-in duration-200"
            leave-from-class="opacity-100 translate-y-0 sm:scale-100"
            leave-to-class="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
          >
            <div
              v-if="show"
              class="inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6"
            >
              <div class="sm:flex sm:items-start">
                <!-- Icono -->
                <div
                  class="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full sm:mx-0 sm:h-10 sm:w-10"
                  :class="iconBgClass"
                >
                  <component :is="iconComponent" class="h-6 w-6" :class="iconClass" />
                </div>
                
                <!-- Contenido -->
                <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                  <h3 class="text-lg leading-6 font-medium text-gray-900" id="modal-title">
                    {{ title }}
                  </h3>
                  <div class="mt-2">
                    <p class="text-sm text-gray-500">
                      {{ message }}
                    </p>
                  </div>
                </div>
              </div>
              
              <!-- Botones -->
              <div class="mt-5 sm:mt-4" :class="type === 'info' ? 'sm:flex sm:justify-center' : 'sm:flex sm:flex-row-reverse'">
                <button
                  type="button"
                  class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
                  :class="confirmButtonClass"
                  @click="$emit('confirm')"
                >
                  {{ type === 'info' ? 'Entendido' : confirmText }}
                </button>
                
                <button
                  v-if="type !== 'info'"
                  type="button"
                  class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 sm:mt-0 sm:w-auto sm:text-sm"
                  @click="$emit('cancel')"
                >
                  Cancelar
                </button>
              </div>
            </div>
          </Transition>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import {
  ExclamationTriangleIcon,
  InformationCircleIcon,
  CheckCircleIcon,
  XCircleIcon
} from '@heroicons/vue/24/outline'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: 'Confirmar acción'
  },
  message: {
    type: String,
    default: '¿Está seguro que desea continuar?'
  },
  type: {
    type: String,
    default: 'warning', // warning, danger, info, success
    validator: (value) => ['warning', 'danger', 'info', 'success'].includes(value)
  },
  confirmText: {
    type: String,
    default: 'Confirmar'
  }
})

const emit = defineEmits(['confirm', 'cancel'])

// Computed properties para estilos dinámicos
const iconComponent = computed(() => {
  const iconMap = {
    warning: ExclamationTriangleIcon,
    danger: XCircleIcon,
    info: InformationCircleIcon,
    success: CheckCircleIcon
  }
  return iconMap[props.type] || ExclamationTriangleIcon
})

const iconBgClass = computed(() => {
  const bgMap = {
    warning: 'bg-yellow-100',
    danger: 'bg-red-100',
    info: 'bg-blue-100',
    success: 'bg-green-100'
  }
  return bgMap[props.type] || 'bg-yellow-100'
})

const iconClass = computed(() => {
  const colorMap = {
    warning: 'text-yellow-600',
    danger: 'text-red-600',
    info: 'text-blue-600',
    success: 'text-green-600'
  }
  return colorMap[props.type] || 'text-yellow-600'
})

const confirmButtonClass = computed(() => {
  const buttonMap = {
    warning: 'bg-yellow-600 hover:bg-yellow-700 focus:ring-yellow-500',
    danger: 'bg-red-600 hover:bg-red-700 focus:ring-red-500',
    info: 'bg-blue-600 hover:bg-blue-700 focus:ring-blue-500',
    success: 'bg-green-600 hover:bg-green-700 focus:ring-green-500'
  }
  return buttonMap[props.type] || 'bg-yellow-600 hover:bg-yellow-700 focus:ring-yellow-500'
})
</script>