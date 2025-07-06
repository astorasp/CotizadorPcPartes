<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Header -->
      <div class="text-center">
        <div class="mx-auto h-16 w-16 bg-primary-600 rounded-full flex items-center justify-center mb-6">
          <svg class="h-10 w-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <h2 class="text-3xl font-bold text-gray-900 mb-2">Portal Cotizador Vue</h2>
        <p class="text-gray-600">Inicia sesión para acceder al sistema</p>
      </div>

      <!-- Login Form -->
      <div class="bg-white rounded-lg shadow-xl p-8">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Error Message -->
          <Transition name="error-fade">
            <div v-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-4">
              <div class="flex items-center">
                <svg class="h-5 w-5 text-red-400 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div class="text-red-800 text-sm font-medium">
                  {{ error }}
                </div>
              </div>
            </div>
          </Transition>

          <!-- Usuario Field -->
          <div>
            <label for="usuario" class="block text-sm font-medium text-gray-700 mb-2">
              Usuario
            </label>
            <input
              id="usuario"
              v-model="credentials.usuario"
              type="text"
              required
              class="w-full px-4 py-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-colors"
              placeholder="Ingrese su usuario"
              :disabled="loading"
              @input="clearErrorOnInput"
            />
          </div>

          <!-- Password Field -->
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
              Contraseña
            </label>
            <input
              id="password"
              v-model="credentials.password"
              type="password"
              required
              class="w-full px-4 py-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-colors"
              placeholder="Ingrese su contraseña"
              :disabled="loading"
              @input="clearErrorOnInput"
            />
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="loading || !isFormValid"
            class="w-full px-4 py-3 bg-primary-600 text-white font-medium rounded-md hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
          >
            <div v-if="loading" class="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-3"></div>
            {{ loading ? 'Iniciando sesión...' : 'Iniciar Sesión' }}
          </button>
        </form>

        <!-- Footer -->
        <div class="mt-6 text-center">
          <p class="text-sm text-gray-500">
            Sistema de gestión de cotizaciones de PC
          </p>
        </div>
      </div>

      <!-- Additional Info -->
      <div class="text-center">
        <p class="text-xs text-gray-500">
          © 2025 Portal Cotizador Vue. Todos los derechos reservados.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'

// Router
const router = useRouter()

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
  // NO limpiar el error aquí - solo cuando sea exitoso o haya un nuevo error

  try {
    const result = await authStore.login(
      credentials.value.usuario.trim(),
      credentials.value.password.trim()
    )

    if (result.success) {
      // Limpiar error solo en caso de éxito
      error.value = ''
      // Redirigir al dashboard después del login exitoso
      router.push('/dashboard')
    } else {
      // Mostrar nuevo error (reemplaza el anterior si existía)
      error.value = result.error || 'Error de autenticación'
    }
  } catch (err) {
    error.value = 'Error de conexión. Verifique su conexión e intente nuevamente.'
    console.error('Login error:', err)
  } finally {
    loading.value = false
  }
}

// Limpiar error cuando el usuario empiece a escribir de nuevo
const clearErrorOnInput = () => {
  if (error.value) {
    error.value = ''
  }
}

// Lifecycle
onMounted(() => {
  // Si ya está autenticado, redirigir al dashboard
  if (authStore.isLoggedIn) {
    router.push('/dashboard')
  }
})
</script>

<style scoped>
/* Transiciones para el mensaje de error */
.error-fade-enter-active,
.error-fade-leave-active {
  transition: all 0.3s ease;
}

.error-fade-enter-from,
.error-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.error-fade-enter-to,
.error-fade-leave-from {
  opacity: 1;
  transform: translateY(0);
}
</style>