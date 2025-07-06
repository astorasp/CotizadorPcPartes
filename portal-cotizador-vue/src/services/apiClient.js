import axios from 'axios'
import { API_CONFIG, API_RESPONSE_CODES, DEBUG_CONFIG } from '@/utils/constants'

/**
 * Cliente API migrado del proyecto original (api.js)
 * Ahora usa JWT en lugar de Basic Auth
 */
class ApiClient {
  constructor() {
    this.baseURL = API_CONFIG.BASE_URL
    this.timeout = API_CONFIG.TIMEOUT
    
    // Crear instancia de axios
    this.client = axios.create({
      baseURL: this.baseURL,
      timeout: this.timeout,
      headers: API_CONFIG.HEADERS
    })
    
    // Configurar interceptors
    this.setupInterceptors()
  }
  
  setupInterceptors() {
    // Request interceptor - agregar JWT token automáticamente
    this.client.interceptors.request.use(
      (config) => {
        // Obtener token JWT del localStorage
        const accessToken = localStorage.getItem('accessToken')
        if (accessToken) {
          config.headers.Authorization = `Bearer ${accessToken}`
        }
        
        if (DEBUG_CONFIG.ENABLED && DEBUG_CONFIG.LOG_API_CALLS) {
          console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, config.data)
        }
        return config
      },
      (error) => {
        console.error('[API Request Error]', error)
        return Promise.reject(error)
      }
    )
    
    // Response interceptor
    this.client.interceptors.response.use(
      (response) => {
        if (DEBUG_CONFIG.ENABLED && DEBUG_CONFIG.LOG_API_CALLS) {
          console.log(`[API Response] ${response.status}`, response.data)
        }
        return response
      },
      (error) => {
        // Manejar error 401 - token expirado o inválido
        if (error.response?.status === 401) {
          // Limpiar tokens locales
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('tokenType')
          localStorage.removeItem('expiresIn')
          localStorage.removeItem('issuedAt')
          
          // Emitir evento para que el componente muestre el modal de login
          window.dispatchEvent(new CustomEvent('auth-required'))
        }
        
        console.error('[API Response Error]', error)
        return Promise.reject(this.handleError(error))
      }
    )
  }
  
  handleError(error) {
    if (error.response) {
      // Error de respuesta del servidor
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          return new Error(data?.mensaje || 'Solicitud inválida')
        case 401:
          return new Error('No autorizado. Verifique sus credenciales.')
        case 403:
          return new Error('Acceso denegado')
        case 404:
          return new Error('Recurso no encontrado')
        case 500:
          return new Error('Error interno del servidor')
        default:
          return new Error(data?.mensaje || `Error ${status}`)
      }
    } else if (error.request) {
      // Error de red
      return new Error('Error de conexión. Verifique su conexión a internet.')
    } else {
      // Error de configuración
      return new Error('Error en la configuración de la solicitud')
    }
  }
  
  // Métodos HTTP base (migración exacta de api.js)
  async get(endpoint, config = {}) {
    try {
      const response = await this.client.get(endpoint, config)
      return this.handleResponse(response)
    } catch (error) {
      throw error
    }
  }
  
  async post(endpoint, data = {}, config = {}) {
    try {
      const response = await this.client.post(endpoint, data, config)
      return this.handleResponse(response)
    } catch (error) {
      throw error
    }
  }
  
  async put(endpoint, data = {}, config = {}) {
    try {
      const response = await this.client.put(endpoint, data, config)
      return this.handleResponse(response)
    } catch (error) {
      throw error
    }
  }
  
  async delete(endpoint, config = {}) {
    try {
      const response = await this.client.delete(endpoint, config)
      return this.handleResponse(response)
    } catch (error) {
      throw error
    }
  }
  
  async patch(endpoint, data = {}, config = {}) {
    try {
      const response = await this.client.patch(endpoint, data, config)
      return this.handleResponse(response)
    } catch (error) {
      throw error
    }
  }
  
  // Manejo de respuesta (migración de lógica original)
  handleResponse(response) {
    const data = response.data
    
    // Verificar estructura de respuesta del backend
    if (data && typeof data === 'object') {
      // Si tiene la estructura esperada {codigo, mensaje, datos}
      if ('codigo' in data) {
        if (data.codigo === API_RESPONSE_CODES.SUCCESS) {
          return data
        } else {
          // Error de negocio
          throw new Error(data.mensaje || 'Error en la operación')
        }
      }
      
      // Si es una respuesta directa (array, objeto)
      return {
        codigo: API_RESPONSE_CODES.SUCCESS,
        mensaje: 'Operación exitosa',
        datos: data
      }
    }
    
    // Respuesta simple (string, number, boolean)
    return {
      codigo: API_RESPONSE_CODES.SUCCESS,
      mensaje: 'Operación exitosa',
      datos: data
    }
  }
  
  // Método para probar conexión
  async testConnection() {
    try {
      const response = await this.get('/health')
      return {
        success: true,
        message: 'Conexión exitosa',
        data: response
      }
    } catch (error) {
      return {
        success: false,
        message: error.message,
        error
      }
    }
  }
  
  // Método para establecer headers dinámicos
  setHeader(key, value) {
    this.client.defaults.headers[key] = value
  }
  
  // Método para remover headers
  removeHeader(key) {
    delete this.client.defaults.headers[key]
  }
  
  // Getter para acceso a la configuración
  get config() {
    return {
      baseURL: this.baseURL,
      timeout: this.timeout,
      headers: this.client.defaults.headers
    }
  }
}

// Crear instancia singleton
const apiClient = new ApiClient()

export default apiClient
export { ApiClient }