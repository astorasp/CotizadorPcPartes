<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="buttonClasses"
    @click="handleClick"
  >
    <!-- Spinner de carga -->
    <LoadingSpinner
      v-if="loading"
      size="xs"
      :color="spinnerColor"
      :class="spinnerClasses"
    />
    
    <!-- Icono -->
    <component
      v-else-if="icon"
      :is="icon"
      :class="iconClasses"
    />
    
    <!-- Contenido del slot -->
    <slot>
      {{ loading ? loadingText : text }}
    </slot>
  </button>
</template>

<script setup>
import { computed, useSlots } from 'vue'
import LoadingSpinner from './LoadingSpinner.vue'

const slots = useSlots()

const props = defineProps({
  // Texto del botón
  text: {
    type: String,
    default: ''
  },
  
  // Texto cuando está cargando
  loadingText: {
    type: String,
    default: 'Cargando...'
  },
  
  // Estado de carga
  loading: {
    type: Boolean,
    default: false
  },
  
  // Estado deshabilitado
  disabled: {
    type: Boolean,
    default: false
  },
  
  // Tipo de botón HTML
  type: {
    type: String,
    default: 'button'
  },
  
  // Variante de estilo
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => [
      'primary', 'secondary', 'success', 'warning', 'danger', 'ghost', 'outline'
    ].includes(value)
  },
  
  // Tamaño del botón
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['xs', 'sm', 'md', 'lg', 'xl'].includes(value)
  },
  
  // Icono a mostrar
  icon: {
    type: [String, Object],
    default: null
  },
  
  // Si el botón ocupa todo el ancho
  fullWidth: {
    type: Boolean,
    default: false
  },
  
  // Si es un botón redondeado
  rounded: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

// Clases computadas
const buttonClasses = computed(() => {
  const baseClasses = [
    'inline-flex items-center justify-center font-medium transition-all duration-200',
    'focus:outline-none focus:ring-2 focus:ring-offset-2',
    'disabled:opacity-50 disabled:cursor-not-allowed'
  ]
  
  // Tamaños
  const sizeClasses = {
    xs: 'px-2 py-1 text-xs',
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-3 text-base',
    xl: 'px-8 py-4 text-lg'
  }
  
  // Variantes de color
  const variantClasses = {
    primary: 'bg-primary-600 text-white hover:bg-primary-700 focus:ring-primary-500',
    secondary: 'bg-gray-600 text-white hover:bg-gray-700 focus:ring-gray-500',
    success: 'bg-green-600 text-white hover:bg-green-700 focus:ring-green-500',
    warning: 'bg-yellow-600 text-white hover:bg-yellow-700 focus:ring-yellow-500',
    danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
    ghost: 'text-gray-700 hover:bg-gray-100 focus:ring-gray-500',
    outline: 'border border-gray-300 text-gray-700 bg-white hover:bg-gray-50 focus:ring-primary-500'
  }
  
  // Forma
  const shapeClasses = props.rounded ? 'rounded-full' : 'rounded-md'
  
  // Ancho completo
  const widthClasses = props.fullWidth ? 'w-full' : ''
  
  return [
    ...baseClasses,
    sizeClasses[props.size],
    variantClasses[props.variant],
    shapeClasses,
    widthClasses
  ].filter(Boolean)
})

const spinnerClasses = computed(() => {
  return ['mr-2']
})

const iconClasses = computed(() => {
  const sizeMap = {
    xs: 'w-3 h-3',
    sm: 'w-4 h-4',
    md: 'w-4 h-4',
    lg: 'w-5 h-5',
    xl: 'w-6 h-6'
  }
  
  return [
    sizeMap[props.size] || sizeMap.md,
    props.text || slots.default ? 'mr-2' : ''
  ].filter(Boolean)
})

const spinnerColor = computed(() => {
  const colorMap = {
    primary: 'white',
    secondary: 'white',
    success: 'white',
    warning: 'white',
    danger: 'white',
    ghost: 'primary',
    outline: 'primary'
  }
  
  return colorMap[props.variant] || 'white'
})

// Métodos
const handleClick = (event) => {
  if (!props.loading && !props.disabled) {
    emit('click', event)
  }
}
</script>

<style scoped>
/* Transiciones suaves para estados hover y focus */
button {
  transition-property: background-color, border-color, color, box-shadow, transform;
}

/* Efecto de presionado */
button:not(:disabled):active {
  transform: translateY(1px);
}

/* Estados específicos para loading */
button[data-loading="true"] {
  position: relative;
}

/* Animación para cuando aparece el spinner */
.loading-enter-active,
.loading-leave-active {
  transition: all 0.2s ease;
}

.loading-enter-from,
.loading-leave-to {
  opacity: 0;
  transform: scale(0.8);
}
</style>