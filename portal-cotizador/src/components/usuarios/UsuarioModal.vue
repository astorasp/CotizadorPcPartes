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
          <div
            class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"
            @click="$emit('close')"
          ></div>

          <!-- Modal Content -->
          <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
          
          <div class="inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
            <!-- Header -->
            <div class="mb-4">
              <h3 class="text-lg leading-6 font-medium text-gray-900" id="modal-title">
                {{ isEditing ? 'Editar Usuario' : 'Crear Nuevo Usuario' }}
              </h3>
              <p class="mt-1 text-sm text-gray-500">
                {{ isEditing ? 'Modifica los datos del usuario seleccionado' : 'Completa la información para crear un nuevo usuario' }}
              </p>
            </div>

            <!-- Formulario -->
            <form @submit.prevent="handleSubmit" class="space-y-4">
              <!-- Campo Usuario -->
              <div>
                <label for="usuario" class="block text-sm font-medium text-gray-700 mb-1">
                  Nombre de Usuario *
                </label>
                <input
                  id="usuario"
                  v-model="formData.usuario"
                  type="text"
                  required
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                  :class="{ 'border-red-300': errors.usuario }"
                  placeholder="ej: usuario123, adminSistema"
                  :disabled="loading || (isEditing && props.usuario?.usuario === 'admin')"
                  @input="validateUsuarioField"
                  @keypress="onUsuarioKeypress"
                />
                
                <!-- Nota informativa -->
                <p v-if="isEditing && props.usuario?.usuario === 'admin'" class="mt-1 text-xs text-amber-600">
                  ⚠️ El nombre del usuario administrador no se puede modificar por seguridad.
                </p>
                <p v-else class="mt-1 text-xs text-gray-500">
                  Solo letras y números. Debe empezar con letra. Sin espacios ni caracteres especiales.
                </p>
                
                <!-- Error específico -->
                <p v-if="errors.usuario" class="mt-1 text-sm text-red-600">
                  {{ errors.usuario }}
                </p>
              </div>

              <!-- Campo Contraseña -->
              <div>
                <label for="password" class="block text-sm font-medium text-gray-700 mb-1">
                  {{ isEditing ? 'Nueva Contraseña (dejar vacío para mantener actual)' : 'Contraseña *' }}
                </label>
                <div class="relative">
                  <input
                    id="password"
                    v-model="formData.password"
                    :type="showPassword ? 'text' : 'password'"
                    :required="!isEditing"
                    class="w-full px-3 py-2 pr-10 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                    :class="{ 'border-red-300': errors.password }"
                    placeholder="Ingrese la contraseña"
                    :disabled="loading"
                    @input="validatePassword"
                  />
                  <button
                    type="button"
                    class="absolute inset-y-0 right-0 pr-3 flex items-center"
                    @click="showPassword = !showPassword"
                  >
                    <component
                      :is="showPassword ? EyeSlashIcon : EyeIcon"
                      class="h-4 w-4 text-gray-400 hover:text-gray-600"
                    />
                  </button>
                </div>
                <p v-if="errors.password" class="mt-1 text-sm text-red-600">
                  {{ errors.password }}
                </p>
                
                <!-- Indicador de fortaleza de contraseña -->
                <div v-if="formData.password && passwordStrength" class="mt-2">
                  <div class="text-xs text-gray-500 mb-1">
                    Fortaleza: <span :class="getStrengthClass(passwordStrength.strength)">{{ passwordStrength.strength }}</span>
                  </div>
                  <div class="w-full bg-gray-200 rounded-full h-2">
                    <div
                      class="h-2 rounded-full transition-all duration-300"
                      :class="getStrengthBarClass(passwordStrength.strength)"
                      :style="{ width: getStrengthPercentage(passwordStrength.strength) }"
                    ></div>
                  </div>
                  <div v-if="passwordStrength.errors.length > 0" class="mt-1 text-xs text-red-600">
                    <ul class="list-disc list-inside">
                      <li v-for="error in passwordStrength.errors" :key="error">{{ error }}</li>
                    </ul>
                  </div>
                </div>
              </div>

              <!-- Campo Estado -->
              <div>
                <div class="flex items-center">
                  <input
                    id="activo"
                    v-model="formData.activo"
                    type="checkbox"
                    class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                    :disabled="loading"
                  />
                  <label for="activo" class="ml-2 block text-sm font-medium text-gray-700">
                    Usuario activo
                  </label>
                </div>
                <p class="mt-1 text-sm text-gray-500">
                  Los usuarios inactivos no podrán iniciar sesión en el sistema
                </p>
              </div>

              <!-- Roles -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  {{ isEditing ? 'Roles Actuales' : 'Roles Iniciales' }}
                </label>
                
                <!-- Selector de roles para creación -->
                <div v-if="!isEditing">
                  <div class="space-y-2">
                    <div 
                      v-for="rol in rolesDisponibles" 
                      :key="rol.id"
                      class="flex items-center"
                    >
                      <input
                        :id="`rol-${rol.id}`"
                        v-model="rolesSeleccionados"
                        :value="rol.id"
                        type="checkbox"
                        class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                      />
                      <label 
                        :for="`rol-${rol.id}`"
                        class="ml-2 block text-sm text-gray-900"
                      >
                        <span class="font-medium">{{ rol.nombre }}</span>
                        <span class="text-gray-500 ml-1">({{ getRolDescription(rol.nombre) }})</span>
                      </label>
                    </div>
                  </div>
                  <p class="mt-1 text-sm text-gray-500">
                    Selecciona los roles que tendrá el usuario al ser creado
                  </p>
                </div>
                
                <!-- Mostrar roles actuales para edición -->
                <div v-else-if="usuario?.roles?.length > 0">
                  <div class="flex flex-wrap gap-1">
                    <span
                      v-for="rol in usuario.roles"
                      :key="rol.id"
                      class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                    >
                      {{ rol.nombre }}
                    </span>
                  </div>
                  <p class="mt-1 text-sm text-gray-500">
                    Para modificar roles, usa el botón "Gestionar Roles" en la tabla
                  </p>
                </div>
              </div>

              <!-- Error general -->
              <div v-if="generalError" class="bg-red-50 border border-red-200 rounded-md p-3">
                <div class="flex">
                  <ExclamationTriangleIcon class="h-5 w-5 text-red-400" />
                  <div class="ml-3">
                    <p class="text-sm text-red-800">{{ generalError }}</p>
                  </div>
                </div>
              </div>

              <!-- Botones -->
              <div class="flex justify-end space-x-3 pt-4">
                <button
                  type="button"
                  class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                  @click="$emit('close')"
                  :disabled="loading"
                >
                  Cancelar
                </button>
                
                <button
                  type="submit"
                  class="px-4 py-2 bg-primary-600 border border-transparent rounded-md text-sm font-medium text-white hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
                  :disabled="loading || !isFormValid"
                >
                  <span v-if="loading" class="flex items-center">
                    <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Guardando...
                  </span>
                  <span v-else>
                    {{ isEditing ? 'Actualizar' : 'Crear' }} Usuario
                  </span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import {
  EyeIcon,
  EyeSlashIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/outline'

import { validatePassword } from '@/utils/helpers'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  usuario: {
    type: Object,
    default: null
  },
  rolesDisponibles: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['close', 'save'])

// Estado reactivo
const loading = ref(false)
const showPassword = ref(false)
const generalError = ref('')
const rolesSeleccionados = ref([])

const formData = reactive({
  usuario: '',
  password: '',
  activo: true
})

const errors = reactive({
  usuario: '',
  password: ''
})

// Computed properties
const isEditing = computed(() => !!props.usuario?.id)

const passwordStrength = computed(() => {
  if (!formData.password) return null
  return validatePassword(formData.password)
})

const isFormValid = computed(() => {
  const hasUsuario = formData.usuario.trim().length > 0
  const hasPassword = isEditing.value || formData.password.length > 0
  const passwordValid = !formData.password || (passwordStrength.value?.isValid !== false)
  
  return hasUsuario && hasPassword && passwordValid && !errors.usuario && !errors.password
})

// Methods
const validateForm = () => {
  // Reset errores
  errors.usuario = ''
  errors.password = ''
  generalError.value = ''

  // Validar usuario
  if (!formData.usuario.trim()) {
    errors.usuario = 'El nombre de usuario es obligatorio'
  } else if (formData.usuario.length < 3) {
    errors.usuario = 'El nombre de usuario debe tener al menos 3 caracteres'
  } else if (!/^[a-zA-Z][a-zA-Z0-9]*$/.test(formData.usuario)) {
    if (/^\d/.test(formData.usuario)) {
      errors.usuario = 'El nombre de usuario debe empezar con una letra'
    } else if (/[^a-zA-Z0-9]/.test(formData.usuario)) {
      errors.usuario = 'Solo se permiten letras y números, sin espacios ni caracteres especiales'
    } else {
      errors.usuario = 'Formato de usuario inválido'
    }
  }

  // Validar contraseña
  if (!isEditing.value && !formData.password) {
    errors.password = 'La contraseña es obligatoria'
  } else if (formData.password && passwordStrength.value && !passwordStrength.value.isValid) {
    errors.password = 'La contraseña no cumple con los requisitos mínimos'
  }

  return !errors.usuario && !errors.password
}

const validatePasswordField = () => {
  if (formData.password && passwordStrength.value && !passwordStrength.value.isValid) {
    errors.password = 'La contraseña no cumple con los requisitos mínimos'
  } else {
    errors.password = ''
  }
}

// Validación del nombre de usuario en tiempo real
const validateUsuarioField = () => {
  const usuario = formData.usuario.trim()
  
  if (!usuario) {
    errors.usuario = ''
    return
  }
  
  if (usuario.length < 3) {
    errors.usuario = 'Mínimo 3 caracteres'
    return
  }
  
  if (!/^[a-zA-Z][a-zA-Z0-9]*$/.test(usuario)) {
    if (/^\d/.test(usuario)) {
      errors.usuario = 'Debe empezar con una letra'
    } else if (/[^a-zA-Z0-9]/.test(usuario)) {
      errors.usuario = 'Solo letras y números permitidos'
    } else {
      errors.usuario = 'Formato inválido'
    }
    return
  }
  
  // Si pasa todas las validaciones, limpiar error
  errors.usuario = ''
}

// Prevenir caracteres no permitidos durante la escritura
const onUsuarioKeypress = (event) => {
  const char = event.key
  const currentValue = formData.usuario
  
  // Permitir teclas de control (backspace, delete, arrows, etc.)
  if (event.ctrlKey || event.metaKey || char.length > 1) {
    return
  }
  
  // Si es el primer carácter, debe ser una letra
  if (currentValue.length === 0 && /\d/.test(char)) {
    event.preventDefault()
    return
  }
  
  // Solo permitir letras y números
  if (!/[a-zA-Z0-9]/.test(char)) {
    event.preventDefault()
    return
  }
}

const handleSubmit = async () => {
  if (!validateForm()) return

  loading.value = true
  generalError.value = ''

  try {
    const userData = {
      usuario: formData.usuario.trim(),
      activo: formData.activo
    }

    // Solo incluir contraseña si se proporciona
    if (formData.password) {
      userData.password = formData.password
    }

    // Si es edición, incluir ID
    if (isEditing.value) {
      userData.id = props.usuario.id
    }

    // Si es creación, incluir roles seleccionados
    if (!isEditing.value && rolesSeleccionados.value.length > 0) {
      userData.roles = rolesSeleccionados.value
    }

    emit('save', userData)
  } catch (error) {
    generalError.value = 'Error inesperado al procesar el formulario'
    console.error('Error en formulario de usuario:', error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formData.usuario = ''
  formData.password = ''
  formData.activo = true
  errors.usuario = ''
  errors.password = ''
  generalError.value = ''
  showPassword.value = false
  rolesSeleccionados.value = []
}

// Utilidades para roles
const getRolDescription = (rolNombre) => {
  const descriptions = {
    'ADMIN': 'Acceso completo al sistema',
    'USER': 'Acceso básico de usuario',
    'SUPERVISOR': 'Supervisión y reportes',
    'GUEST': 'Acceso limitado de invitado'
  }
  return descriptions[rolNombre] || 'Rol del sistema'
}

// Utilidades para indicador de fortaleza
const getStrengthClass = (strength) => {
  const classMap = {
    'débil': 'text-red-600 font-medium',
    'media': 'text-yellow-600 font-medium',
    'fuerte': 'text-green-600 font-medium'
  }
  return classMap[strength] || 'text-gray-600'
}

const getStrengthBarClass = (strength) => {
  const classMap = {
    'débil': 'bg-red-500',
    'media': 'bg-yellow-500',
    'fuerte': 'bg-green-500'
  }
  return classMap[strength] || 'bg-gray-300'
}

const getStrengthPercentage = (strength) => {
  const percentageMap = {
    'débil': '33%',
    'media': '66%',
    'fuerte': '100%'
  }
  return percentageMap[strength] || '0%'
}

// Watchers
watch(() => props.show, (newValue) => {
  if (newValue) {
    if (props.usuario) {
      // Modo edición
      formData.usuario = props.usuario.usuario || ''
      formData.password = ''
      formData.activo = props.usuario.activo !== false
    } else {
      // Modo creación
      resetForm()
    }
  }
})

watch(() => formData.password, validatePasswordField)

// Lifecycle
onMounted(() => {
  if (props.show && props.usuario) {
    formData.usuario = props.usuario.usuario || ''
    formData.password = ''
    formData.activo = props.usuario.activo !== false
  }
})
</script>

<style scoped>
/* Transiciones del modal */
.modal-enter-active, .modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from, .modal-leave-to {
  opacity: 0;
}
</style>