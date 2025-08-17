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
        <h2 class="text-3xl font-bold text-gray-900 mb-2">Portal Cotizador</h2>
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
              :disabled="authStore.isLoggingIn"
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
              :disabled="authStore.isLoggingIn"
              @input="clearErrorOnInput"
            />
          </div>

          <!-- Submit Button -->
          <LoadingButton
            type="submit"
            :loading="authStore.isLoggingIn"
            :disabled="!isFormValid"
            variant="primary"
            size="lg"
            full-width
            loading-text="Iniciando sesión..."
          >
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
            </svg>
            Iniciar Sesión
          </LoadingButton>
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
          © 2025 Portal Cotizador. Todos los derechos reservados.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'
import LoadingButton from '@/components/ui/LoadingButton.vue'

// Router
const router = useRouter()

// Auth store
const authStore = useAuthStore()

// Estado del formulario
const credentials = ref({
  usuario: '',
  password: ''
})

const error = ref('')

// Computed
const isFormValid = computed(() => {
  return credentials.value.usuario.trim() && credentials.value.password.trim()
})

// Métodos
const handleSubmit = async () => {
  if (!isFormValid.value) return

  // Limpiar error antes de intentar login
  error.value = ''

  try {
    const result = await authStore.login(
      credentials.value.usuario.trim(),
      credentials.value.password.trim()
    )

    if (result.success) {
      // Redirigir al dashboard después del login exitoso
      router.push('/dashboard')
    } else {
      // Mostrar error específico del login
      error.value = result.error || 'Error de autenticación'
    }
  } catch (err) {
    error.value = 'Error de conexión. Verifique su conexión e intente nuevamente.'
    console.error('Login error:', err)
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