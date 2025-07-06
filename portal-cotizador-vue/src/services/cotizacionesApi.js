import apiClient from './apiClient'
import { API_ENDPOINTS } from '@/utils/constants'

/**
 * API service para Cotizaciones (migración exacta de PortalApi.cotizaciones)
 * Incluye todas las operaciones CRUD de cotizaciones y gestión de componentes/impuestos
 */
export const cotizacionesApi = {
  /**
   * Obtener todas las cotizaciones
   */
  async getAll() {
    const response = await apiClient.get(API_ENDPOINTS.COTIZACIONES.BASE)
    return response.datos || []
  },

  /**
   * Obtener cotización por ID
   */
  async getById(id) {
    const response = await apiClient.get(API_ENDPOINTS.COTIZACIONES.BY_ID(id))
    return response.datos
  },

  /**
   * Crear nueva cotización
   */
  async create(cotizacionData) {
    const response = await apiClient.post(API_ENDPOINTS.COTIZACIONES.BASE, cotizacionData)
    return response
  },

  /**
   * Actualizar cotización existente
   */
  async update(id, cotizacionData) {
    const response = await apiClient.put(API_ENDPOINTS.COTIZACIONES.BY_ID(id), cotizacionData)
    return response
  },

  /**
   * Eliminar cotización
   */
  async delete(id) {
    const response = await apiClient.delete(API_ENDPOINTS.COTIZACIONES.BY_ID(id))
    return response
  },

  /**
   * Buscar cotizaciones por fecha
   */
  async getByDate(fecha) {
    const response = await apiClient.get(API_ENDPOINTS.COTIZACIONES.BY_DATE, {
      params: { fecha }
    })
    return response.datos || []
  },

  /**
   * Verificar si existe una cotización con el ID
   */
  async exists(id) {
    try {
      const response = await apiClient.get(API_ENDPOINTS.COTIZACIONES.BY_ID(id))
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
   * Obtener estadísticas de una cotización (detalles, totales, etc.)
   */
  async getStats(cotizacionId) {
    try {
      const cotizacion = await this.getById(cotizacionId)
      
      const detalles = cotizacion.detalles || []
      const impuestos = cotizacion.impuestosAplicados || []
      
      return {
        componentCount: detalles.length,
        taxCount: impuestos.length,
        subtotal: parseFloat(cotizacion.subtotal || 0),
        totalImpuestos: parseFloat(cotizacion.impuestos || cotizacion.totalImpuestos || 0),
        total: parseFloat(cotizacion.total || 0),
        detalles: detalles,
        impuestos: impuestos,
        cotizacion: cotizacion
      }
    } catch (error) {
      console.error('Error getting cotización stats:', error)
      throw error
    }
  },

  /**
   * Validar datos de cotización antes de enviar
   */
  validateCotizacion(cotizacionData) {
    const errors = []

    if (!cotizacionData.tipoCotizador) {
      errors.push('Tipo de cotizador es requerido')
    }

    if (!cotizacionData.detalles || cotizacionData.detalles.length === 0) {
      errors.push('Al menos un componente es requerido')
    }

    if (!cotizacionData.impuestos || cotizacionData.impuestos.length === 0) {
      errors.push('Al menos un impuesto es requerido')
    }

    // Validar detalles de componentes
    if (cotizacionData.detalles) {
      cotizacionData.detalles.forEach((detalle, index) => {
        if (!detalle.componenteId) {
          errors.push(`Componente ${index + 1}: ID es requerido`)
        }
        if (!detalle.cantidad || detalle.cantidad <= 0) {
          errors.push(`Componente ${index + 1}: Cantidad debe ser mayor a 0`)
        }
        if (!detalle.precioUnitario || detalle.precioUnitario <= 0) {
          errors.push(`Componente ${index + 1}: Precio unitario debe ser mayor a 0`)
        }
      })
    }

    // Validar impuestos
    if (cotizacionData.impuestos) {
      cotizacionData.impuestos.forEach((impuesto, index) => {
        if (!impuesto.tipo) {
          errors.push(`Impuesto ${index + 1}: Tipo es requerido`)
        }
        if (!impuesto.pais) {
          errors.push(`Impuesto ${index + 1}: País es requerido`)
        }
        if (!impuesto.tasa || impuesto.tasa <= 0 || impuesto.tasa > 100) {
          errors.push(`Impuesto ${index + 1}: Tasa debe estar entre 0.01 y 100`)
        }
      })
    }

    return {
      isValid: errors.length === 0,
      errors: errors
    }
  },

  /**
   * Calcular totales de una cotización
   */
  calculateTotals(detalles = [], impuestos = []) {
    // Calcular subtotal de componentes
    const subtotal = detalles.reduce((sum, detalle) => {
      const precio = parseFloat(detalle.precioUnitario || detalle.precioBase || 0)
      const cantidad = parseInt(detalle.cantidad || 1)
      return sum + (precio * cantidad)
    }, 0)

    // Calcular impuestos
    let totalImpuestos = 0
    impuestos.forEach(impuesto => {
      if (impuesto.tasa > 0) {
        totalImpuestos += subtotal * (impuesto.tasa / 100)
      }
    })

    // Total final
    const total = subtotal + totalImpuestos

    return {
      subtotal,
      totalImpuestos,
      total,
      desglose: {
        componentes: detalles.map(detalle => ({
          id: detalle.componenteId || detalle.idComponente,
          descripcion: detalle.descripcion || detalle.nombreComponente,
          cantidad: detalle.cantidad,
          precioUnitario: detalle.precioUnitario || detalle.precioBase,
          subtotal: (detalle.cantidad || 1) * (detalle.precioUnitario || detalle.precioBase || 0)
        })),
        impuestos: impuestos.map(impuesto => ({
          tipo: impuesto.tipo,
          pais: impuesto.pais,
          tasa: impuesto.tasa,
          importe: subtotal * (impuesto.tasa / 100)
        }))
      }
    }
  }
}

export default cotizacionesApi