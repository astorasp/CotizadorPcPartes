import axios from 'axios'

const AUTH_BASE_URL = (import.meta.env.VITE_SEGURIDAD_API_BASE_URL || '/api/seguridad') + '/auth'

/**
 * Servicio de autenticación para manejar login/logout con JWT
 */
export const authService = {
  /**
   * Realizar login con credenciales de usuario
   * @param {string} usuario - Nombre de usuario
   * @param {string} password - Contraseña
   * @returns {Promise<Object>} Response con tokens JWT
   */
  async login(usuario, password) {
    try {
      const response = await axios.post(`${AUTH_BASE_URL}/login`, {
        usuario,
        password
      })
      
      if (response.data.successful) {
        // Guardar tokens en localStorage
        localStorage.setItem('accessToken', response.data.accessToken)
        localStorage.setItem('refreshToken', response.data.refreshToken)
        localStorage.setItem('tokenType', response.data.tokenType)
        localStorage.setItem('expiresIn', response.data.expiresIn)
        localStorage.setItem('issuedAt', response.data.issuedAt)
        
        // Configurar header por defecto para axios
        this.setAuthHeader(response.data.accessToken)
        
        return {
          success: true,
          data: response.data
        }
      }
      
      return {
        success: false,
        error: 'Login failed'
      }
    } catch (error) {
      console.error('Login error:', error)
      return {
        success: false,
        error: error.response?.data?.message || 'Error de autenticación'
      }
    }
  },

  /**
   * Realizar logout
   */
  async logout() {
    try {
      const accessToken = localStorage.getItem('accessToken')
      const refreshToken = localStorage.getItem('refreshToken')
      
      if (accessToken && refreshToken) {
        await axios.post(`${AUTH_BASE_URL}/logout`, {
          accessToken,
          refreshToken
        })
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      // Limpiar tokens locales
      this.clearTokens()
    }
  },

  /**
   * Renovar token usando refresh token
   */
  async refreshToken() {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      
      if (!refreshToken) {
        throw new Error('No refresh token available')
      }
      
      const response = await axios.post(`${AUTH_BASE_URL}/refresh`, {
        refreshToken
      })
      
      if (response.data.successful) {
        // Actualizar tokens
        localStorage.setItem('accessToken', response.data.accessToken)
        localStorage.setItem('refreshToken', response.data.refreshToken)
        localStorage.setItem('tokenType', response.data.tokenType)
        localStorage.setItem('expiresIn', response.data.expiresIn)
        localStorage.setItem('issuedAt', response.data.issuedAt)
        
        // Actualizar header
        this.setAuthHeader(response.data.accessToken)
        
        return {
          success: true,
          data: response.data
        }
      }
      
      return {
        success: false,
        error: 'Token refresh failed'
      }
    } catch (error) {
      console.error('Token refresh error:', error)
      this.clearTokens()
      return {
        success: false,
        error: error.response?.data?.message || 'Error renovando token'
      }
    }
  },

  /**
   * Verificar si el usuario está autenticado
   */
  isAuthenticated() {
    const accessToken = localStorage.getItem('accessToken')
    const issuedAt = localStorage.getItem('issuedAt')
    const expiresIn = localStorage.getItem('expiresIn')
    
    if (!accessToken || !issuedAt || !expiresIn) {
      return false
    }
    
    // Verificar si el token ha expirado
    const issuedTime = new Date(issuedAt).getTime()
    const expiryTime = issuedTime + (parseInt(expiresIn) * 1000)
    const currentTime = new Date().getTime()
    
    return currentTime < expiryTime
  },

  /**
   * Obtener token de acceso actual
   */
  getAccessToken() {
    return localStorage.getItem('accessToken')
  },

  /**
   * Configurar header de autorización para axios
   */
  setAuthHeader(token) {
    if (token) {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
    } else {
      delete axios.defaults.headers.common['Authorization']
    }
  },

  /**
   * Limpiar todos los tokens
   */
  clearTokens() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('tokenType')
    localStorage.removeItem('expiresIn')
    localStorage.removeItem('issuedAt')
    
    // Limpiar header de autorización
    delete axios.defaults.headers.common['Authorization']
  },

  /**
   * Inicializar servicio de autenticación
   * Configura interceptores para manejo automático de tokens
   */
  initialize() {
    // Configurar header si ya hay un token
    const accessToken = this.getAccessToken()
    if (accessToken && this.isAuthenticated()) {
      this.setAuthHeader(accessToken)
    }
    
    // Interceptor para requests - agregar token automáticamente
    axios.interceptors.request.use(
      (config) => {
        const token = this.getAccessToken()
        if (token && this.isAuthenticated()) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )
    
    // Interceptor para responses - manejar errores 401
    axios.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config
        
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true
          
          try {
            const refreshResult = await this.refreshToken()
            if (refreshResult.success) {
              // Reintentar request original con nuevo token
              originalRequest.headers.Authorization = `Bearer ${refreshResult.data.accessToken}`
              return axios(originalRequest)
            }
          } catch (refreshError) {
            console.error('Token refresh failed:', refreshError)
          }
          
          // Si no se puede renovar, limpiar tokens y redirigir a login
          this.clearTokens()
          window.location.href = '/login'
        }
        
        return Promise.reject(error)
      }
    )
  }
}

// Inicializar servicio al cargar el módulo
authService.initialize()