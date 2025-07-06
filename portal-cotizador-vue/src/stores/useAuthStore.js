import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/authService'

/**
 * Store de autenticación para manejar estado de usuario y sesión
 */
export const useAuthStore = defineStore('auth', () => {
  // Estado reactivo
  const isAuthenticated = ref(false)
  const user = ref(null)
  const loading = ref(false)
  const showLoginModal = ref(false)
  const pendingAction = ref(null)

  // Computed properties
  const isLoggedIn = computed(() => isAuthenticated.value && !!user.value)
  const userName = computed(() => user.value?.usuario || '')
  const userRoles = computed(() => user.value?.roles || [])

  // Actions
  const checkAuthentication = () => {
    const authenticated = authService.isAuthenticated()
    isAuthenticated.value = authenticated
    
    if (authenticated) {
      // Si hay token válido, extraer información básica del usuario
      const accessToken = authService.getAccessToken()
      if (accessToken) {
        try {
          // Decodificar JWT para obtener información del usuario
          const payload = JSON.parse(atob(accessToken.split('.')[1]))
          user.value = {
            usuario: payload.sub,
            roles: payload.roles || [],
            userId: payload.user_id
          }
        } catch (error) {
          console.error('Error decoding token:', error)
          logout()
        }
      }
    } else {
      user.value = null
    }
    
    return authenticated
  }

  const login = async (usuario, password) => {
    loading.value = true
    
    try {
      const result = await authService.login(usuario, password)
      
      if (result.success) {
        isAuthenticated.value = true
        
        // Extraer información del usuario del token
        const accessToken = result.data.accessToken
        const payload = JSON.parse(atob(accessToken.split('.')[1]))
        
        user.value = {
          usuario: payload.sub,
          roles: payload.roles || [],
          userId: payload.user_id
        }
        
        return { success: true }
      }
      
      return result
    } catch (error) {
      console.error('Login error:', error)
      return {
        success: false,
        error: 'Error de autenticación'
      }
    } finally {
      loading.value = false
    }
  }

  const logout = async () => {
    loading.value = true
    
    try {
      await authService.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      isAuthenticated.value = false
      user.value = null
      loading.value = false
    }
  }

  const openLoginModal = () => {
    showLoginModal.value = true
  }

  const closeLoginModal = () => {
    showLoginModal.value = false
  }

  const handleLoginSuccess = (tokenData) => {
    // El login ya fue manejado por el servicio y el estado ya está actualizado
    // No necesitamos llamar checkAuthentication() de nuevo
    closeLoginModal()
    
    // Ejecutar acción pendiente si existe
    if (pendingAction.value) {
      const action = pendingAction.value
      pendingAction.value = null
      action()
    }
  }

  const requireAuth = (actionAfterLogin = null) => {
    if (!isLoggedIn.value) {
      if (actionAfterLogin) {
        pendingAction.value = actionAfterLogin
      }
      openLoginModal()
      return false
    }
    return true
  }

  const hasRole = (role) => {
    return userRoles.value.includes(role)
  }

  const hasAnyRole = (roles) => {
    return roles.some(role => userRoles.value.includes(role))
  }

  // Inicializar store
  const initialize = () => {
    checkAuthentication()
    
    // Escuchar eventos de requerimiento de autenticación desde apiClient
    window.addEventListener('auth-required', () => {
      isAuthenticated.value = false
      user.value = null
      openLoginModal()
    })
  }

  return {
    // Estado
    isAuthenticated,
    user,
    loading,
    showLoginModal,
    
    // Computed
    isLoggedIn,
    userName,
    userRoles,
    
    // Actions
    checkAuthentication,
    login,
    logout,
    openLoginModal,
    closeLoginModal,
    handleLoginSuccess,
    requireAuth,
    hasRole,
    hasAnyRole,
    initialize
  }
})