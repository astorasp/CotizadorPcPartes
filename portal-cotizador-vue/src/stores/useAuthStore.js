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
          
          // Emitir evento de estado de autenticación actualizado
          window.dispatchEvent(new CustomEvent('auth-state-changed', {
            detail: { authenticated: true, user: user.value }
          }))
        } catch (error) {
          console.error('Error decoding token:', error)
          logout()
        }
      }
    } else {
      user.value = null
      
      // Emitir evento de estado de autenticación actualizado
      window.dispatchEvent(new CustomEvent('auth-state-changed', {
        detail: { authenticated: false, user: null }
      }))
    }
    
    return authenticated
  }

  const handleTokenRefresh = () => {
    // Método para ser llamado cuando se renueva el token exitosamente
    checkAuthentication()
    // Emitir evento para que el monitor reinicie
    window.dispatchEvent(new CustomEvent('auth-token-refreshed'))
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
        
        // Emitir evento para iniciar el monitoreo de token
        window.dispatchEvent(new CustomEvent('auth-login-success'))
        
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
      
      // Limpiar cualquier estado pendiente
      pendingAction.value = null
      
      // Emitir evento para detener el monitoreo de token
      window.dispatchEvent(new CustomEvent('auth-logout'))
    }
  }

  const forceLogout = () => {
    // Logout inmediato sin llamar al servicio (para cuando ya se perdió la sesión)
    isAuthenticated.value = false
    user.value = null
    loading.value = false
    pendingAction.value = null
    
    // Limpiar localStorage
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('tokenType')
    localStorage.removeItem('expiresIn')
    localStorage.removeItem('issuedAt')
    
    // Emitir evento para detener el monitoreo de token
    window.dispatchEvent(new CustomEvent('auth-logout'))
  }

  const requireAuth = (actionAfterLogin = null) => {
    if (!isLoggedIn.value) {
      // En lugar de abrir modal, ahora redirigimos a login
      // Este método se mantiene para compatibilidad pero ya no se usa
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
    
    // Escuchar eventos de expiración de sesión desde apiClient
    window.addEventListener('auth-expired', async (event) => {
      // Hacer logout forzado (limpiar todo el estado)
      forceLogout()
      
      // Mostrar mensaje de sesión expirada si está disponible
      if (event.detail?.message) {
        console.warn('[Auth]', event.detail.message)
        // Aquí podrías mostrar una notificación toast si tienes un sistema de notificaciones
      }
      
      // Redirigir al login usando el router
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        // Usar location.href para forzar navegación completa y limpiar estado
        window.location.href = '/login'
      }
    })
    
    // Mantener compatibilidad con eventos de auth-required (para casos futuros)
    window.addEventListener('auth-required', () => {
      isAuthenticated.value = false
      user.value = null
      // El router guard se encargará de la redirección
    })
  }

  return {
    // Estado
    isAuthenticated,
    user,
    loading,
    
    // Computed
    isLoggedIn,
    userName,
    userRoles,
    
    // Actions
    checkAuthentication,
    handleTokenRefresh,
    login,
    logout,
    forceLogout,
    requireAuth,
    hasRole,
    hasAnyRole,
    initialize
  }
})