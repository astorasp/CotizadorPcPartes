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