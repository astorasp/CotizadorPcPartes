import axios from 'axios'

const AUTH_BASE_URL = (import.meta.env.VITE_SEGURIDAD_API_BASE_URL || '/api/seguridad') + '/auth'

// Roles definidos para el sistema
export const ROLES = {
  ADMIN: 'ADMIN',
  GERENTE: 'GERENTE', 
  VENDEDOR: 'VENDEDOR',
  INVENTARIO: 'INVENTARIO',
  CONSULTOR: 'CONSULTOR'
}

// Permisos por módulo
export const PERMISSIONS = {
  COMPONENTES: {
    VIEW: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.INVENTARIO, ROLES.CONSULTOR],
    CREATE: [ROLES.ADMIN, ROLES.INVENTARIO],
    EDIT: [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO],
    DELETE: [ROLES.ADMIN]
  },
  PCS: {
    VIEW: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.INVENTARIO, ROLES.CONSULTOR],
    CREATE: [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO],
    EDIT: [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO],
    DELETE: [ROLES.ADMIN, ROLES.GERENTE],
    ADD_COMPONENT: [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO],
    REMOVE_COMPONENT: [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO],
    VIEW_COST: [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO],
    MODIFY_PRICE: [ROLES.ADMIN, ROLES.GERENTE]
  },
  COTIZACIONES: {
    VIEW: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.INVENTARIO, ROLES.CONSULTOR],
    VIEW_DETAILS: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.CONSULTOR],
    VIEW_COSTS: [ROLES.ADMIN, ROLES.GERENTE],
    CREATE: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR],
    EDIT: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR],
    DELETE: [ROLES.ADMIN, ROLES.GERENTE],
    APPROVE: [ROLES.ADMIN, ROLES.GERENTE],
    MODIFY_PRICING: [ROLES.ADMIN, ROLES.GERENTE],
    MODIFY_TAXES: [ROLES.ADMIN],
    VIEW_MARGINS: [ROLES.ADMIN, ROLES.GERENTE],
    CONVERT_TO_ORDER: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR],
    EXPORT: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.CONSULTOR],
    VIEW_REPORTS: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.INVENTARIO, ROLES.CONSULTOR],
    VIEW_FINANCIAL_REPORTS: [ROLES.ADMIN, ROLES.GERENTE]
  },
  PROVEEDORES: {
    VIEW: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.INVENTARIO, ROLES.CONSULTOR],
    CREATE: [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO],
    EDIT: [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO],
    DELETE: [ROLES.ADMIN],
    SEARCH: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.INVENTARIO, ROLES.CONSULTOR],
    VIEW_COMMERCIAL_DATA: [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO],
    MANAGE_RELATIONS: [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO]
  },
  PEDIDOS: {
    VIEW: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.INVENTARIO, ROLES.CONSULTOR],
    CREATE: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.INVENTARIO],
    EDIT: [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO],
    DELETE: [ROLES.ADMIN],
    APPROVE: [ROLES.ADMIN, ROLES.GERENTE],
    CHANGE_STATUS: [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO],
    VIEW_FULFILLMENT: [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO],
    MANAGE_FULFILLMENT: [ROLES.ADMIN, ROLES.INVENTARIO],
    VIEW_FINANCIAL_DATA: [ROLES.ADMIN, ROLES.GERENTE],
    GENERATE_REPORTS: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.INVENTARIO, ROLES.CONSULTOR]
  },
  PROMOCIONES: {
    VIEW: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.INVENTARIO, ROLES.CONSULTOR],
    CREATE: [ROLES.ADMIN, ROLES.GERENTE],
    EDIT: [ROLES.ADMIN, ROLES.GERENTE],
    DELETE: [ROLES.ADMIN, ROLES.GERENTE],
    APPLY: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR],
    VIEW_FINANCIAL_IMPACT: [ROLES.ADMIN, ROLES.GERENTE],
    MANAGE_STACKING: [ROLES.ADMIN, ROLES.GERENTE],
    VIEW_REPORTS: [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.INVENTARIO, ROLES.CONSULTOR]
  }
}

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
      
      // Verificar si es específicamente un refresh token expirado o límite alcanzado
      const errorCode = error.response?.data?.error
      if (errorCode === 'refresh_token_expired') {
        // Limpiar tokens y disparar evento para UI
        this.clearTokens()
        window.dispatchEvent(new CustomEvent('refresh-token-expired', {
          detail: {
            message: 'Su sesión ha expirado completamente. Debe volver a autenticarse.',
            timestamp: new Date(),
            reason: 'refresh_token_expired'
          }
        }))
        
        return {
          success: false,
          error: 'refresh_token_expired',
          message: 'Refresh token expirado - se requiere nueva autenticación'
        }
      } else if (errorCode === 'session_renewal_limit_reached') {
        // Limpiar tokens y disparar evento para UI con mensaje específico
        this.clearTokens()
        window.dispatchEvent(new CustomEvent('refresh-token-expired', {
          detail: {
            message: 'Ha alcanzado el límite máximo de renovaciones de sesión. Debe volver a autenticarse.',
            timestamp: new Date(),
            reason: 'session_renewal_limit_reached'
          }
        }))
        
        return {
          success: false,
          error: 'session_renewal_limit_reached',
          message: 'Límite de renovaciones alcanzado - se requiere nueva autenticación'
        }
      }
      
      this.clearTokens()
      return {
        success: false,
        error: error.response?.data?.message || 'Error renovando token'
      }
    }
  },

  /**
   * Verificar el estado del refresh token
   */
  async checkRefreshTokenStatus() {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      
      if (!refreshToken) {
        return {
          valid: false,
          error: 'No refresh token available'
        }
      }
      
      const response = await axios.post(`${AUTH_BASE_URL}/refresh-status`, {
        refreshToken
      })
      
      return {
        valid: true,
        data: response.data
      }
    } catch (error) {
      const errorCode = error.response?.data?.error
      if (errorCode === 'refresh_token_expired') {
        this.clearTokens()
        window.dispatchEvent(new CustomEvent('refresh-token-expired', {
          detail: {
            message: 'Su sesión ha expirado completamente. Debe volver a autenticarse.',
            timestamp: new Date(),
            reason: 'refresh_token_expired'
          }
        }))
      } else if (errorCode === 'session_renewal_limit_reached') {
        this.clearTokens()
        window.dispatchEvent(new CustomEvent('refresh-token-expired', {
          detail: {
            message: 'Ha alcanzado el límite máximo de renovaciones de sesión. Debe volver a autenticarse.',
            timestamp: new Date(),
            reason: 'session_renewal_limit_reached'
          }
        }))
      }
      
      return {
        valid: false,
        error: error.response?.data?.message || 'Error verificando refresh token',
        errorCode: errorCode
      }
    }
  },

  /**
   * Verificar si una renovación de token será posible
   * Revisa si el próximo access token cabría dentro del tiempo del refresh token
   */
  async canRenewToken() {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      const accessTokenDuration = localStorage.getItem('expiresIn') // en milisegundos
      
      if (!refreshToken || !accessTokenDuration) {
        return { canRenew: false, reason: 'missing_tokens' }
      }

      // Decodificar el refresh token para obtener su tiempo de expiración
      const payload = JSON.parse(atob(refreshToken.split('.')[1]))
      if (!payload.exp) {
        return { canRenew: false, reason: 'invalid_refresh_token' }
      }

      const refreshTokenExpiresAt = payload.exp * 1000 // convertir a milisegundos
      const now = Date.now()
      const accessTokenDurationMs = parseInt(accessTokenDuration)

      // Verificar si el refresh token ya expiró
      if (refreshTokenExpiresAt <= now) {
        return { canRenew: false, reason: 'refresh_token_expired' }
      }

      // Calcular si el próximo access token cabría en el tiempo restante del refresh token
      const nextAccessTokenExpiresAt = now + accessTokenDurationMs
      const canRenew = nextAccessTokenExpiresAt <= refreshTokenExpiresAt

      const timeRemaining = refreshTokenExpiresAt - now
      const result = {
        canRenew,
        reason: canRenew ? 'can_renew' : 'session_renewal_limit_reached',
        refreshTokenTimeRemaining: timeRemaining,
        accessTokenDuration: accessTokenDurationMs,
        nextAccessTokenWouldExpireAt: nextAccessTokenExpiresAt,
        refreshTokenExpiresAt: refreshTokenExpiresAt
      }

      // console.log('[AuthService] canRenewToken check:', result)
      return result

    } catch (error) {
      console.error('[AuthService] Error checking if token can be renewed:', error)
      return { canRenew: false, reason: 'check_error' }
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
   * Decodificar JWT para obtener información del usuario
   * @returns {Object|null} Información del usuario decodificada
   */
  decodeToken() {
    const token = this.getAccessToken()
    if (!token) return null

    try {
      // Decodificar JWT (solo la parte del payload)
      const base64Url = token.split('.')[1]
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      )
      
      return JSON.parse(jsonPayload)
    } catch (error) {
      console.error('Error decoding token:', error)
      return null
    }
  },

  /**
   * Obtener roles del usuario actual desde el JWT
   * @returns {Array} Array de roles del usuario
   */
  getUserRoles() {
    const decoded = this.decodeToken()
    if (!decoded || !decoded.roles) {
      // Fallback para desarrollo: usuario con todos los roles
      return [ROLES.ADMIN, ROLES.GERENTE, ROLES.VENDEDOR, ROLES.INVENTARIO, ROLES.CONSULTOR]
    }
    
    return decoded.roles || []
  },

  /**
   * Verificar si el usuario tiene al menos uno de los roles especificados
   * @param {Array|String} requiredRoles - Rol o array de roles requeridos
   * @returns {Boolean}
   */
  hasAnyRole(requiredRoles) {
    const userRoles = this.getUserRoles()
    const roles = Array.isArray(requiredRoles) ? requiredRoles : [requiredRoles]
    return roles.some(role => userRoles.includes(role))
  },

  /**
   * Verificar si el usuario tiene un rol específico
   * @param {String} role - Rol requerido
   * @returns {Boolean}
   */
  hasRole(role) {
    const userRoles = this.getUserRoles()
    return userRoles.includes(role)
  },

  /**
   * Verificar permisos específicos para el módulo de Componentes
   */
  canViewComponentes() {
    return this.hasAnyRole(PERMISSIONS.COMPONENTES.VIEW)
  },

  canCreateComponentes() {
    return this.hasAnyRole(PERMISSIONS.COMPONENTES.CREATE)
  },

  canEditComponentes() {
    return this.hasAnyRole(PERMISSIONS.COMPONENTES.EDIT)
  },

  canDeleteComponentes() {
    return this.hasAnyRole(PERMISSIONS.COMPONENTES.DELETE)
  },

  /**
   * Verificar permisos específicos para el módulo de PCs
   */
  canViewPcs() {
    return this.hasAnyRole(PERMISSIONS.PCS.VIEW)
  },

  canCreatePcs() {
    return this.hasAnyRole(PERMISSIONS.PCS.CREATE)
  },

  canEditPcs() {
    return this.hasAnyRole(PERMISSIONS.PCS.EDIT)
  },

  canDeletePcs() {
    return this.hasAnyRole(PERMISSIONS.PCS.DELETE)
  },

  canAddComponentToPc() {
    return this.hasAnyRole(PERMISSIONS.PCS.ADD_COMPONENT)
  },

  canRemoveComponentFromPc() {
    return this.hasAnyRole(PERMISSIONS.PCS.REMOVE_COMPONENT)
  },

  canViewPcCost() {
    return this.hasAnyRole(PERMISSIONS.PCS.VIEW_COST)
  },

  canModifyPcPrice() {
    return this.hasAnyRole(PERMISSIONS.PCS.MODIFY_PRICE)
  },

  /**
   * Verificar permisos específicos para el módulo de Cotizaciones
   */
  canViewCotizaciones() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.VIEW)
  },

  canViewCotizacionDetails() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.VIEW_DETAILS)
  },

  canViewCotizacionCosts() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.VIEW_COSTS)
  },

  canCreateCotizaciones() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.CREATE)
  },

  canEditCotizaciones() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.EDIT)
  },

  canDeleteCotizaciones() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.DELETE)
  },

  canApproveCotizaciones() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.APPROVE)
  },

  canModifyCotizacionPricing() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.MODIFY_PRICING)
  },

  canModifyCotizacionTaxes() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.MODIFY_TAXES)
  },

  canViewCotizacionMargins() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.VIEW_MARGINS)
  },

  canConvertCotizacionToOrder() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.CONVERT_TO_ORDER)
  },

  canExportCotizaciones() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.EXPORT)
  },

  canViewCotizacionReports() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.VIEW_REPORTS)
  },

  canViewCotizacionFinancialReports() {
    return this.hasAnyRole(PERMISSIONS.COTIZACIONES.VIEW_FINANCIAL_REPORTS)
  },

  /**
   * Verificar permisos específicos para el módulo de Proveedores
   */
  canViewProveedores() {
    return this.hasAnyRole(PERMISSIONS.PROVEEDORES.VIEW)
  },

  canCreateProveedores() {
    return this.hasAnyRole(PERMISSIONS.PROVEEDORES.CREATE)
  },

  canEditProveedores() {
    return this.hasAnyRole(PERMISSIONS.PROVEEDORES.EDIT)
  },

  canDeleteProveedores() {
    return this.hasAnyRole(PERMISSIONS.PROVEEDORES.DELETE)
  },

  canSearchProveedores() {
    return this.hasAnyRole(PERMISSIONS.PROVEEDORES.SEARCH)
  },

  canViewProveedorCommercialData() {
    return this.hasAnyRole(PERMISSIONS.PROVEEDORES.VIEW_COMMERCIAL_DATA)
  },

  canManageProveedorRelations() {
    return this.hasAnyRole(PERMISSIONS.PROVEEDORES.MANAGE_RELATIONS)
  },

  /**
   * Verificar permisos específicos para el módulo de Pedidos
   */
  canViewPedidos() {
    return this.hasAnyRole(PERMISSIONS.PEDIDOS.VIEW)
  },

  canCreatePedidos() {
    return this.hasAnyRole(PERMISSIONS.PEDIDOS.CREATE)
  },

  canEditPedidos() {
    return this.hasAnyRole(PERMISSIONS.PEDIDOS.EDIT)
  },

  canDeletePedidos() {
    return this.hasAnyRole(PERMISSIONS.PEDIDOS.DELETE)
  },

  canApprovePedidos() {
    return this.hasAnyRole(PERMISSIONS.PEDIDOS.APPROVE)
  },

  canChangePedidoStatus() {
    return this.hasAnyRole(PERMISSIONS.PEDIDOS.CHANGE_STATUS)
  },

  canViewPedidoFulfillment() {
    return this.hasAnyRole(PERMISSIONS.PEDIDOS.VIEW_FULFILLMENT)
  },

  canManagePedidoFulfillment() {
    return this.hasAnyRole(PERMISSIONS.PEDIDOS.MANAGE_FULFILLMENT)
  },

  canViewPedidoFinancialData() {
    return this.hasAnyRole(PERMISSIONS.PEDIDOS.VIEW_FINANCIAL_DATA)
  },

  canGeneratePedidoReports() {
    return this.hasAnyRole(PERMISSIONS.PEDIDOS.GENERATE_REPORTS)
  },

  /**
   * Verificar permisos específicos para el módulo de Promociones
   */
  canViewPromociones() {
    return this.hasAnyRole(PERMISSIONS.PROMOCIONES.VIEW)
  },

  canCreatePromociones() {
    return this.hasAnyRole(PERMISSIONS.PROMOCIONES.CREATE)
  },

  canEditPromociones() {
    return this.hasAnyRole(PERMISSIONS.PROMOCIONES.EDIT)
  },

  canDeletePromociones() {
    return this.hasAnyRole(PERMISSIONS.PROMOCIONES.DELETE)
  },

  canApplyPromociones() {
    return this.hasAnyRole(PERMISSIONS.PROMOCIONES.APPLY)
  },

  canViewPromocionFinancialImpact() {
    return this.hasAnyRole(PERMISSIONS.PROMOCIONES.VIEW_FINANCIAL_IMPACT)
  },

  canManagePromocionStacking() {
    return this.hasAnyRole(PERMISSIONS.PROMOCIONES.MANAGE_STACKING)
  },

  canViewPromocionReports() {
    return this.hasAnyRole(PERMISSIONS.PROMOCIONES.VIEW_REPORTS)
  },

  /**
   * Verificar si el usuario es administrador
   * @returns {Boolean}
   */
  isAdmin() {
    return this.hasRole(ROLES.ADMIN)
  },

  /**
   * Obtener el rol principal del usuario (el de mayor jerarquía)
   * @returns {String}
   */
  getPrimaryRole() {
    const userRoles = this.getUserRoles()
    const roleHierarchy = [ROLES.ADMIN, ROLES.GERENTE, ROLES.INVENTARIO, ROLES.VENDEDOR, ROLES.CONSULTOR]
    
    for (const role of roleHierarchy) {
      if (userRoles.includes(role)) {
        return role
      }
    }
    
    return null
  },

  /**
   * Obtener información del usuario actual
   * @returns {Object|null}
   */
  getCurrentUser() {
    const decoded = this.decodeToken()
    if (!decoded) return null

    return {
      username: decoded.sub || decoded.username,
      roles: this.getUserRoles(),
      primaryRole: this.getPrimaryRole()
    }
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
    
    // Prevenir múltiples interceptores
    if (this._interceptorsInitialized) {
      return
    }
    this._interceptorsInitialized = true
    
    // Interceptor para requests - agregar token automáticamente
    axios.interceptors.request.use(
      (config) => {
        // No agregar token a requests de login
        if (config.url && config.url.includes('/auth/login')) {
          return config
        }
        
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
        
        // No manejar 401s en requests de login/logout
        if (originalRequest.url && (
          originalRequest.url.includes('/auth/login') ||
          originalRequest.url.includes('/auth/logout')
        )) {
          return Promise.reject(error)
        }
        
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true
          
          try {
            const refreshResult = await this.refreshToken()
            if (refreshResult.success) {
              // Reintentar request original con nuevo token
              originalRequest.headers.Authorization = `Bearer ${refreshResult.data.accessToken}`
              return axios(originalRequest)
            } else if (refreshResult.error === 'refresh_token_expired' || refreshResult.error === 'session_renewal_limit_reached') {
              // El refresh token expiró o se alcanzó el límite - el evento ya fue disparado en refreshToken()
              // No hacer nada más aquí, la UI ya fue notificada
              return Promise.reject(error)
            }
          } catch (refreshError) {
            console.error('Token refresh failed:', refreshError)
          }
          
          // Si no se puede renovar, limpiar tokens
          this.clearTokens()
          // No forzar redirección aquí, dejar que Vue Router lo maneje
        }
        
        return Promise.reject(error)
      }
    )
  }
}

// Inicializar servicio al cargar el módulo
authService.initialize()