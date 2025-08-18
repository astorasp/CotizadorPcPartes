import pedidosApiClient from './pedidosApiClient'
import { API_ENDPOINTS } from '@/utils/constants'

/**
 * API service para Proveedores (migración exacta de PortalApi.proveedores)
 * Incluye todas las operaciones CRUD de proveedores y búsquedas específicas
 */
export const proveedoresApi = {
  /**
   * Obtener todos los proveedores
   */
  async getAll() {
    const response = await pedidosApiClient.get(API_ENDPOINTS.PROVEEDORES.BASE)
    return response.datos || []
  },

  /**
   * Obtener proveedor por clave
   */
  async getByCve(cve) {
    const response = await pedidosApiClient.get(API_ENDPOINTS.PROVEEDORES.BY_CVE(cve))
    return response.datos
  },

  /**
   * Crear nuevo proveedor
   */
  async create(proveedorData) {
    const response = await pedidosApiClient.post(API_ENDPOINTS.PROVEEDORES.BASE, proveedorData)
    return response
  },

  /**
   * Actualizar proveedor existente
   */
  async update(cve, proveedorData) {
    const response = await pedidosApiClient.put(API_ENDPOINTS.PROVEEDORES.BY_CVE(cve), proveedorData)
    return response
  },

  /**
   * Eliminar proveedor
   */
  async delete(cve) {
    const response = await pedidosApiClient.delete(API_ENDPOINTS.PROVEEDORES.BY_CVE(cve))
    return response
  },

  /**
   * Buscar proveedores por nombre comercial
   */
  async searchByName(nombre) {
    const response = await pedidosApiClient.get(API_ENDPOINTS.PROVEEDORES.BY_NAME, {
      params: { nombre }
    })
    return response.datos || []
  },

  /**
   * Buscar proveedores por razón social
   */
  async searchByRazon(razonSocial) {
    const response = await pedidosApiClient.get(API_ENDPOINTS.PROVEEDORES.BY_RAZON, {
      params: { razonSocial }
    })
    return response.datos || []
  },

  /**
   * Verificar si existe un proveedor con la clave
   */
  async exists(cve) {
    try {
      const response = await pedidosApiClient.get(API_ENDPOINTS.PROVEEDORES.BY_CVE(cve))
      return response.datos !== null
    } catch (error) {
      // Si da 404, significa que no existe
      if (error.message.includes('404') || error.message.includes('no encontrado')) {
        return false
      }
      throw error
    }
  },

  /**
   * Validar datos de proveedor antes de enviar
   */
  validateProveedor(proveedorData, isUpdate = false) {
    const errors = []

    // Clave requerida solo en creación
    if (!isUpdate && !proveedorData.cve) {
      errors.push('La clave del proveedor es requerida')
    } else if (!isUpdate && proveedorData.cve.length > 10) {
      errors.push('La clave no puede exceder 10 caracteres')
    }

    if (!proveedorData.nombre) {
      errors.push('El nombre comercial es requerido')
    } else if (proveedorData.nombre.length > 100) {
      errors.push('El nombre no puede exceder 100 caracteres')
    }

    if (!proveedorData.razonSocial) {
      errors.push('La razón social es requerida')
    } else if (proveedorData.razonSocial.length > 200) {
      errors.push('La razón social no puede exceder 200 caracteres')
    }

    return {
      isValid: errors.length === 0,
      errors: errors
    }
  },

  /**
   * Formatear datos del proveedor para envío
   */
  formatProveedorData(rawData) {
    return {
      cve: rawData.cve?.trim(),
      nombre: rawData.nombre?.trim(),
      razonSocial: rawData.razonSocial?.trim()
    }
  },

  /**
   * Obtener resumen de proveedor para display
   */
  getProveedorSummary(proveedor) {
    return {
      cve: proveedor.cve,
      nombre: proveedor.nombre,
      razonSocial: proveedor.razonSocial,
      numeroPedidos: proveedor.numeroPedidos || 0,
      hasOrders: (proveedor.numeroPedidos || 0) > 0,
      displayName: `${proveedor.cve} - ${proveedor.nombre}`,
      fullDisplayName: `${proveedor.cve} - ${proveedor.nombre} (${proveedor.razonSocial})`
    }
  },

  /**
   * Realizar búsqueda general (local) en lista de proveedores
   */
  searchLocal(proveedores, searchTerm) {
    if (!searchTerm) return proveedores
    
    const term = searchTerm.toLowerCase().trim()
    
    return proveedores.filter(proveedor => {
      return proveedor.cve.toLowerCase().includes(term) ||
             proveedor.nombre.toLowerCase().includes(term) ||
             proveedor.razonSocial.toLowerCase().includes(term)
    })
  },

  /**
   * Realizar búsqueda específica según tipo
   */
  async searchByType(searchTerm, searchType, allProveedores = []) {
    if (!searchTerm) return allProveedores

    switch (searchType) {
      case 'nombre':
        return await this.searchByName(searchTerm)
      case 'razon':
        return await this.searchByRazon(searchTerm)
      case 'general':
      default:
        return this.searchLocal(allProveedores, searchTerm)
    }
  }
}

export default proveedoresApi