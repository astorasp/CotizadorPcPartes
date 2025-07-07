import { ref, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { authService } from '@/services/authService'

/**
 * Composable para monitorear la expiración del token y manejar renovación automática
 */
export function useTokenMonitor() {
  const authStore = useAuthStore()
  
  // Estado reactivo
  const showExpirationWarning = ref(false)
  const secondsUntilExpiry = ref(0)
  const isRenewing = ref(false)
  
  // Variables de control
  let monitorInterval = null
  let warningTimeout = null
  let expirationTimeout = null
  
  // Configuración
  const WARNING_SECONDS = 30 // Mostrar warning 30 segundos antes de expirar
  const AUTO_LOGOUT_SECONDS = 30 // Auto logout después de 30 segundos de warning
  
  /**
   * Calcular segundos hasta la expiración del token
   */
  const getSecondsUntilExpiry = () => {
    const accessToken = localStorage.getItem('accessToken')
    
    if (!accessToken) {
      return 0
    }
    
    try {
      // Decodificar JWT para obtener tiempo de expiración directamente
      const payload = JSON.parse(atob(accessToken.split('.')[1]))
      
      if (!payload.exp) {
        console.warn('[TokenMonitor] Token no tiene campo exp')
        return 0
      }
      
      // exp está en segundos desde epoch, convertir a milisegundos
      const expiryTime = payload.exp * 1000
      const currentTime = Date.now()
      
      const secondsLeft = Math.max(0, Math.floor((expiryTime - currentTime) / 1000))
      
      // Debug solo cuando sea crítico
      if (secondsLeft <= 60 && (secondsLeft % 30 === 0 || secondsLeft <= 10)) {
        console.log(`[TokenMonitor] Token expira en ${secondsLeft} segundos (usando JWT exp field)`)
      }
      
      return secondsLeft
    } catch (error) {
      console.error('[TokenMonitor] Error decodificando JWT:', error)
      
      // Fallback al método anterior si falla el JWT
      const issuedAt = localStorage.getItem('issuedAt')
      const expiresIn = localStorage.getItem('expiresIn')
      
      if (!issuedAt || !expiresIn) {
        return 0
      }
      
      const issuedTime = new Date(issuedAt).getTime()
      const expiryTime = issuedTime + (parseInt(expiresIn) * 1000)
      const currentTime = new Date().getTime()
      
      return Math.max(0, Math.floor((expiryTime - currentTime) / 1000))
    }
  }
  
  /**
   * Mostrar modal de advertencia de expiración
   */
  const showWarning = () => {
    if (!authStore.isLoggedIn) {
      console.warn('[TokenMonitor] No se puede mostrar warning - usuario no autenticado')
      return
    }
    
    // Calcular segundos reales hasta la expiración
    const realSecondsLeft = getSecondsUntilExpiry()
    
    console.log('[TokenMonitor] Mostrando advertencia de expiración')
    console.log('[TokenMonitor] Segundos reales hasta expiración:', realSecondsLeft)
    
    showExpirationWarning.value = true
    secondsUntilExpiry.value = realSecondsLeft
    
    console.log('[TokenMonitor] Estado después de mostrar warning:', {
      showExpirationWarning: showExpirationWarning.value,
      secondsUntilExpiry: secondsUntilExpiry.value,
      realSecondsLeft: realSecondsLeft
    })
    
    // Countdown para el modal usando tiempo real
    const countdownInterval = setInterval(() => {
      const currentSecondsLeft = getSecondsUntilExpiry()
      secondsUntilExpiry.value = currentSecondsLeft
      
      // Solo log cuando queden menos de 10 segundos o cada 10 segundos
      if (currentSecondsLeft <= 10 || currentSecondsLeft % 10 === 0) {
        console.log('[TokenMonitor] Countdown tiempo real:', currentSecondsLeft)
      }
      
      if (currentSecondsLeft <= 0) {
        console.log('[TokenMonitor] Token expirado - ejecutando auto logout')
        clearInterval(countdownInterval)
        handleAutoLogout()
      }
    }, 1000)
    
    // Auto logout después del tiempo límite (safety net)
    expirationTimeout = setTimeout(() => {
      console.log('[TokenMonitor] Timeout de seguridad alcanzado - ejecutando auto logout')
      clearInterval(countdownInterval)
      handleAutoLogout()
    }, (realSecondsLeft + 5) * 1000) // Un poco más del tiempo real para safety
  }
  
  /**
   * Extender sesión renovando el token
   */
  const extendSession = async () => {
    isRenewing.value = true
    
    try {
      console.log('[TokenMonitor] Intentando renovar token...')
      const result = await authService.refreshToken()
      
      if (result.success) {
        console.log('[TokenMonitor] Token renovado exitosamente')
        
        // Actualizar estado del authStore
        authStore.checkAuthentication()
        
        // Cerrar modal y reiniciar monitoreo
        closeWarning()
        startMonitoring()
        
        return { success: true, message: 'Sesión extendida exitosamente' }
      } else {
        console.error('[TokenMonitor] Error renovando token:', result.error)
        handleAutoLogout()
        return { success: false, error: result.error }
      }
    } catch (error) {
      console.error('[TokenMonitor] Error inesperado renovando token:', error)
      handleAutoLogout()
      return { success: false, error: 'Error inesperado' }
    } finally {
      isRenewing.value = false
    }
  }
  
  /**
   * Rechazar extensión y cerrar sesión
   */
  const rejectExtension = () => {
    console.log('[TokenMonitor] Usuario rechazó extender sesión')
    handleAutoLogout()
  }
  
  /**
   * Cerrar modal de advertencia
   */
  const closeWarning = () => {
    showExpirationWarning.value = false
    secondsUntilExpiry.value = 0
    
    // Limpiar timeouts
    if (expirationTimeout) {
      clearTimeout(expirationTimeout)
      expirationTimeout = null
    }
  }
  
  /**
   * Manejar logout automático
   */
  const handleAutoLogout = async () => {
    console.log('[TokenMonitor] Ejecutando logout automático por expiración')
    closeWarning()
    stopMonitoring()
    
    await authStore.logout()
    window.location.href = '/login'
  }
  
  /**
   * Iniciar monitoreo del token
   */
  const startMonitoring = () => {
    // Detener monitoreo previo
    stopMonitoring()
    
    if (!authStore.isLoggedIn) {
      console.log('[TokenMonitor] Usuario no autenticado, no iniciando monitoreo')
      return
    }
    
    console.log('[TokenMonitor] Iniciando monitoreo de token')
    
    // Verificar inmediatamente
    checkTokenExpiration()
    
    // Verificar cada 5 segundos
    monitorInterval = setInterval(() => {
      checkTokenExpiration()
    }, 5000)
  }
  
  /**
   * Detener monitoreo del token
   */
  const stopMonitoring = () => {
    if (monitorInterval) {
      clearInterval(monitorInterval)
      monitorInterval = null
    }
    
    if (warningTimeout) {
      clearTimeout(warningTimeout)
      warningTimeout = null
    }
    
    if (expirationTimeout) {
      clearTimeout(expirationTimeout)
      expirationTimeout = null
    }
    
    closeWarning()
    console.log('[TokenMonitor] Monitoreo detenido')
  }
  
  /**
   * Verificar si el token está próximo a expirar
   */
  const checkTokenExpiration = () => {
    if (!authStore.isLoggedIn) {
      stopMonitoring()
      return
    }
    
    const secondsLeft = getSecondsUntilExpiry()
    
    // Si ya está en warning, no hacer nada
    if (showExpirationWarning.value) {
      return
    }
    
    // Si el token ya expiró
    if (secondsLeft <= 0) {
      console.log('[TokenMonitor] Token ya expirado')
      handleAutoLogout()
      return
    }
    
    // Si está próximo a expirar, mostrar warning
    if (secondsLeft <= WARNING_SECONDS) {
      console.log(`[TokenMonitor] Token expira en ${secondsLeft} segundos, mostrando warning`)
      showWarning()
      return
    }
    
    // Log periódico solo cuando sea relevante
    if (secondsLeft <= 120 && secondsLeft % 30 === 0) { // Solo cuando quedan 2 minutos o menos, cada 30s
      console.log(`[TokenMonitor] Token expira en ${secondsLeft} segundos (${Math.floor(secondsLeft / 60)}m ${secondsLeft % 60}s)`)
    } else if (secondsLeft > 120 && secondsLeft % 300 === 0) { // Cada 5 minutos cuando hay tiempo
      console.log(`[TokenMonitor] Token expira en ${Math.floor(secondsLeft / 60)} minutos`)
    }
  }
  
  /**
   * Lifecycle hooks
   */
  onMounted(() => {
    console.log('[TokenMonitor] Component mounted, checking auth status...')
    
    // Verificar inmediatamente si hay token válido
    const accessToken = localStorage.getItem('accessToken')
    if (accessToken) {
      console.log('[TokenMonitor] Token found in localStorage, checking validity...')
      
      // Forzar verificación de autenticación
      authStore.checkAuthentication()
      
      // Esperar un tick y verificar de nuevo
      setTimeout(() => {
        if (authStore.isLoggedIn) {
          console.log('[TokenMonitor] User is authenticated, starting monitoring')
          startMonitoring()
        } else {
          console.log('[TokenMonitor] User not authenticated after check')
        }
      }, 100)
    } else {
      console.log('[TokenMonitor] No token found in localStorage')
    }
    
    // Escuchar eventos de login exitoso para iniciar monitoreo
    window.addEventListener('auth-login-success', () => {
      console.log('[TokenMonitor] Login exitoso detectado, iniciando monitoreo')
      startMonitoring()
    })
    
    // Escuchar eventos de logout para detener monitoreo
    window.addEventListener('auth-logout', () => {
      console.log('[TokenMonitor] Logout detectado, deteniendo monitoreo')
      stopMonitoring()
    })
    
    // También escuchar cambios en el estado de autenticación
    window.addEventListener('auth-state-changed', () => {
      console.log('[TokenMonitor] Auth state changed, rechecking...')
      if (authStore.isLoggedIn) {
        startMonitoring()
      } else {
        stopMonitoring()
      }
    })
  })
  
  onUnmounted(() => {
    stopMonitoring()
    
    // Limpiar event listeners  
    // Nota: necesitamos referencias a las funciones para poder removerlas
    // Por simplicidad, solo detenemos el monitoreo
  })
  
  // Método de debugging para verificar el estado actual
  const debugStatus = () => {
    const secondsLeft = getSecondsUntilExpiry()
    const accessToken = localStorage.getItem('accessToken')
    const issuedAt = localStorage.getItem('issuedAt')
    const expiresIn = localStorage.getItem('expiresIn')
    
    const status = {
      isLoggedIn: authStore.isLoggedIn,
      hasToken: !!accessToken,
      accessToken: accessToken ? accessToken.substring(0, 50) + '...' : null,
      issuedAt: issuedAt,
      expiresIn: expiresIn,
      secondsUntilExpiry: secondsLeft,
      showingWarning: showExpirationWarning.value,
      isMonitoring: !!monitorInterval,
      warningThreshold: WARNING_SECONDS,
      currentTime: new Date().toISOString()
    }
    
    console.log('[TokenMonitor] Debug Status:', status)
    
    if (issuedAt && expiresIn) {
      const issuedTime = new Date(issuedAt).getTime()
      const expiryTime = issuedTime + (parseInt(expiresIn) * 1000)
      const currentTime = new Date().getTime()
      
      console.log('[TokenMonitor] Token timing details:')
      console.log('  - Issued at:', new Date(issuedTime).toISOString())
      console.log('  - Expires at:', new Date(expiryTime).toISOString())
      console.log('  - Current time:', new Date(currentTime).toISOString())
      console.log('  - Token age (seconds):', Math.floor((currentTime - issuedTime) / 1000))
      console.log('  - Token duration (seconds):', parseInt(expiresIn))
      console.log('  - Time left (seconds):', secondsLeft)
    }
    
    return status
  }
  
  // Función para testing manual - simular token próximo a expirar
  const simulateExpiration = (seconds = 15) => {
    console.log(`[TokenMonitor] Intentando simular expiración en ${seconds} segundos`)
    console.log(`[TokenMonitor] Usuario autenticado: ${authStore.isLoggedIn}`)
    
    if (!authStore.isLoggedIn) {
      console.warn('[TokenMonitor] No se puede simular - usuario no autenticado')
      return false
    }
    
    // Directamente mostrar el warning con el tiempo especificado
    secondsUntilExpiry.value = seconds
    showWarning()
    return true
  }
  
  // Función para forzar mostrar el modal inmediatamente (para testing)
  const forceShowModal = (seconds = 15) => {
    console.log(`[TokenMonitor] Forzando mostrar modal con ${seconds} segundos`)
    
    if (!authStore.isLoggedIn) {
      console.warn('[TokenMonitor] No se puede mostrar modal - usuario no autenticado')
      return false
    }
    
    // Forzar mostrar modal independientemente del tiempo real
    showExpirationWarning.value = true
    secondsUntilExpiry.value = seconds
    
    console.log('[TokenMonitor] Modal forzado - estado:', {
      showExpirationWarning: showExpirationWarning.value,
      secondsUntilExpiry: secondsUntilExpiry.value
    })
    
    // Iniciar countdown manual
    const countdownInterval = setInterval(() => {
      secondsUntilExpiry.value--
      
      // Solo log cuando sea relevante
      if (secondsUntilExpiry.value <= 5 || secondsUntilExpiry.value % 5 === 0) {
        console.log('[TokenMonitor] Countdown forzado:', secondsUntilExpiry.value)
      }
      
      if (secondsUntilExpiry.value <= 0) {
        clearInterval(countdownInterval)
        console.log('[TokenMonitor] Countdown terminado')
        // No hacer logout automático en testing
      }
    }, 1000)
    
    return true
  }
  
  // Función para limpiar manualmente el estado (para debugging)
  const clearWarning = () => {
    console.log('[TokenMonitor] Limpiando warning manualmente')
    closeWarning()
    stopMonitoring()
  }
  
  // Función para crear un token que expire en X segundos (para testing real)
  const createTestTokenExpiring = (secondsFromNow = 45) => {
    if (!authStore.isLoggedIn) {
      console.warn('[TokenMonitor] No se puede crear token de prueba - usuario no autenticado')
      return false
    }
    
    const currentTime = Date.now()
    const expiryTime = currentTime + (secondsFromNow * 1000)
    
    console.log(`[TokenMonitor] Creando token de prueba que expira en ${secondsFromNow} segundos`)
    console.log(`[TokenMonitor] Modal aparecerá cuando falten ${WARNING_SECONDS} segundos`)
    
    // Modificar el localStorage para simular un token que expira pronto
    localStorage.setItem('issuedAt', new Date(currentTime).toISOString())
    localStorage.setItem('expiresIn', secondsFromNow.toString())
    
    console.log(`[TokenMonitor] Tiempo actual: ${new Date(currentTime).toISOString()}`)
    console.log(`[TokenMonitor] Expira en: ${new Date(expiryTime).toISOString()}`)
    
    // Forzar actualización del authStore
    authStore.checkAuthentication()
    
    // Reiniciar el monitoreo para que detecte el nuevo tiempo
    stopMonitoring()
    startMonitoring()
    
    console.log(`[TokenMonitor] Monitoreo reiniciado, esperando ${secondsFromNow - WARNING_SECONDS} segundos para mostrar modal`)
    
    return true
  }
  
  // Exponer métodos globalmente para testing
  if (typeof window !== 'undefined') {
    window.debugTokenMonitor = debugStatus
    window.simulateTokenExpiration = simulateExpiration
    window.clearTokenWarning = clearWarning
    window.createTestTokenExpiring = createTestTokenExpiring
    window.forceShowModal = forceShowModal
  }

  return {
    // Estado
    showExpirationWarning,
    secondsUntilExpiry,
    isRenewing,
    
    // Métodos
    extendSession,
    rejectExtension,
    startMonitoring,
    stopMonitoring,
    
    // Info para debugging
    getSecondsUntilExpiry,
    debugStatus
  }
}