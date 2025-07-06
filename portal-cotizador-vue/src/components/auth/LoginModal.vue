<template>
  <div
    v-if="show"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    @click="onBackdropClick"
  >
    <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md mx-4">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-xl font-bold text-gray-900">Iniciar Sesión</h2>
        <button
          @click="$emit('close')"
          class="text-gray-400 hover:text-gray-600 transition-colors"
        >
          <XMarkIcon class="h-6 w-6" />
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div>
          <label for="usuario" class="block text-sm font-medium text-gray-700 mb-2">
            Usuario
          </label>
          <input
            id="usuario"
            v-model="credentials.usuario"
            type="text"
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            placeholder="Ingrese su usuario"
            :disabled="loading"
          />
        </div>

        <div>
          <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
            Contraseña
          </label>
          <input
            id="password"
            v-model="credentials.password"
            type="password"
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            placeholder="Ingrese su contraseña"
            :disabled="loading"
          />
        </div>

        <div v-if="error" class="text-red-600 text-sm">
          {{ error }}
        </div>

        <div class="flex justify-end space-x-3 pt-4">
          <button
            type="button"
            @click="$emit('close')"
            :disabled="loading"
            class="px-4 py-2 text-gray-700 bg-gray-200 hover:bg-gray-300 rounded-md transition-colors disabled:opacity-50"
          >
            Cancelar
          </button>
          <button
            type="submit"
            :disabled="loading || !isFormValid"
            class="px-4 py-2 bg-primary-600 text-white hover:bg-primary-700 rounded-md transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
          >
            <div v-if="loading" class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
            {{ loading ? 'Iniciando...' : 'Iniciar Sesión' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { XMarkIcon } from '@heroicons/vue/24/outline'
import { useAuthStore } from '@/stores/useAuthStore'

// Props
defineProps({
  show: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['close', 'success'])

// Auth store
const authStore = useAuthStore()

// Estado del formulario
const credentials = ref({
  usuario: '',
  password: ''
})

const loading = ref(false)
const error = ref('')

// Computed
const isFormValid = computed(() => {
  return credentials.value.usuario.trim() && credentials.value.password.trim()
})

// Métodos
const handleSubmit = async () => {
  if (!isFormValid.value) return

  loading.value = true
  error.value = ''

  try {
    const result = await authStore.login(
      credentials.value.usuario.trim(),
      credentials.value.password.trim()
    )

    if (result.success) {
      emit('success', result.data)
      emit('close')
      
      // Limpiar formulario
      credentials.value = {
        usuario: '',
        password: ''
      }
    } else {
      error.value = result.error || 'Error de autenticación'
    }
  } catch (err) {
    error.value = 'Error de conexión'
    console.error('Login error:', err)
  } finally {
    loading.value = false
  }
}

const onBackdropClick = (event) => {
  if (event.target === event.currentTarget) {
    emit('close')
  }
}
</script>