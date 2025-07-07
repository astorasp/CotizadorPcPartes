<template>
  <div class="loading-spinner" :class="sizeClass">
    <div class="spinner" :class="[colorClass, animationClass]"></div>
    <div v-if="message" class="loading-message" :class="messageColorClass">
      {{ message }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // Tamaño del spinner
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['xs', 'sm', 'md', 'lg', 'xl'].includes(value)
  },
  
  // Color del spinner
  color: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'success', 'warning', 'error', 'white'].includes(value)
  },
  
  // Mensaje a mostrar
  message: {
    type: String,
    default: ''
  },
  
  // Tipo de animación
  animation: {
    type: String,
    default: 'spin',
    validator: (value) => ['spin', 'pulse', 'bounce'].includes(value)
  },
  
  // Si se muestra en modo centrado
  centered: {
    type: Boolean,
    default: false
  }
})

// Clases computadas para tamaños
const sizeClass = computed(() => {
  const classes = {
    xs: 'spinner-xs',
    sm: 'spinner-sm', 
    md: 'spinner-md',
    lg: 'spinner-lg',
    xl: 'spinner-xl'
  }
  
  const baseClass = classes[props.size] || classes.md
  return [baseClass, { 'centered': props.centered }]
})

// Clases computadas para colores
const colorClass = computed(() => {
  const colors = {
    primary: 'text-primary-600 border-primary-600',
    secondary: 'text-gray-600 border-gray-600',
    success: 'text-green-600 border-green-600',
    warning: 'text-yellow-600 border-yellow-600',
    error: 'text-red-600 border-red-600',
    white: 'text-white border-white'
  }
  return colors[props.color] || colors.primary
})

// Clases para el color del mensaje
const messageColorClass = computed(() => {
  const colors = {
    primary: 'text-gray-700',
    secondary: 'text-gray-600',
    success: 'text-green-700',
    warning: 'text-yellow-700',
    error: 'text-red-700',
    white: 'text-white'
  }
  return colors[props.color] || colors.primary
})

// Clases para animaciones
const animationClass = computed(() => {
  const animations = {
    spin: 'animate-spin',
    pulse: 'animate-pulse',
    bounce: 'animate-bounce'
  }
  return animations[props.animation] || animations.spin
})
</script>

<style scoped>
.loading-spinner {
  @apply flex flex-col items-center justify-center space-y-2;
}

.loading-spinner.centered {
  @apply min-h-[200px];
}

.spinner {
  @apply border-2 border-solid rounded-full border-opacity-20;
}

.spinner::before {
  content: '';
  @apply absolute rounded-full border-2 border-solid border-transparent;
}

/* Tamaños del spinner */
.spinner-xs .spinner {
  @apply w-4 h-4 border border-t-current;
}

.spinner-sm .spinner {
  @apply w-5 h-5 border border-t-current;
}

.spinner-md .spinner {
  @apply w-6 h-6 border-2 border-t-current;
}

.spinner-lg .spinner {
  @apply w-8 h-8 border-2 border-t-current;
}

.spinner-xl .spinner {
  @apply w-12 h-12 border-4 border-t-current;
}

/* Mensaje de carga */
.loading-message {
  @apply text-sm font-medium;
}

.spinner-xs + .loading-message {
  @apply text-xs;
}

.spinner-lg + .loading-message,
.spinner-xl + .loading-message {
  @apply text-base;
}

/* Animación personalizada para mejor efecto visual */
@keyframes spin-smooth {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin-smooth 1s linear infinite;
}
</style>