import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/authService'
import { useAsyncOperation } from '@/composables/useAsyncOperation'

/**
 * Store de autenticación para manejar estado de usuario y sesión
 */
export const useAuthStore = defineStore('auth', () => {
  // Sistema de loading centralizado para auth
  const asyncOp = useAsyncOperation({
    showNotification: false, // Auth maneja sus propias notificaciones
    type: 'auth'
  })
  
  // Estado reactivo
  const isAuthenticated = ref(false)
  const user = ref(null)
  const loading = ref(false)
  const pendingAction = ref(null)

  // Computed properties
  const isLoggedIn = computed(() => isAuthenticated.value && !!user.value)
  const userName = computed(() => user.value?.usuario || '')
  const userRoles = computed(() => user.value?.roles || [])
  
  // Estados de loading específicos
  const isLoggingIn = computed(() => asyncOp.loadingStore.isOperationActive('auth-login'))
  const isLoggingOut = computed(() => asyncOp.loadingStore.isOperationActive('auth-logout'))

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
    const result = await asyncOp.execute(
      async () => {
        const authResult = await authService.login(usuario, password)
        
        if (authResult.success) {
          isAuthenticated.value = true
          
          // Extraer información del usuario del token
          const accessToken = authResult.data.accessToken
          const payload = JSON.parse(atob(accessToken.split('.')[1]))
          
          user.value = {
            usuario: payload.sub,
            roles: payload.roles || [],
            userId: payload.user_id
          }
          
          // Emitir evento para iniciar el monitoreo de token
          window.dispatchEvent(new CustomEvent('auth-login-success'))
          
          return authResult.data
        } else {
          throw new Error(authResult.error || 'Error de autenticación')
        }
      },
      {
        key: 'auth-login',
        message: 'Iniciando sesión...',
        blockUI: true, // Login bloquea la UI
        timeout: 10000 // 10 segundos timeout
      }
    )
    
    // Sincronizar estado de loading local para compatibilidad con componentes existentes
    loading.value = asyncOp.loadingStore.isOperationActive('auth-login')
    
    return result
  }

  const logout = async () => {
    const result = await asyncOp.execute(
      async () => {
        await authService.logout()
        return true
      },
      {
        key: 'auth-logout',
        message: 'Cerrando sesión...',
        blockUI: false, // Logout no necesita bloquear UI
        timeout: 5000 // 5 segundos timeout
      }
    )
    
    // Limpiar estado siempre, independiente del resultado
    isAuthenticated.value = false
    user.value = null
    loading.value = false
    pendingAction.value = null
    
    // Emitir evento para detener el monitoreo de token
    window.dispatchEvent(new CustomEvent('auth-logout'))
    
    return result
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
    loading, // Mantener para compatibilidad
    
    // Computed
    isLoggedIn,
    userName,
    userRoles,
    
    // Estados de loading específicos
    isLoggingIn,
    isLoggingOut,
    
    // Actions
    checkAuthentication,
    handleTokenRefresh,
    login,
    logout,
    forceLogout,
    requireAuth,
    hasRole,
    hasAnyRole,
    initialize,
    
    // Acceso al sistema de loading
    loadingStore: asyncOp.loadingStore
  }
})