import pedidosApiClient from './pedidosApiClient'
import { API_ENDPOINTS } from '@/utils/constants'

/**
 * API service para Pedidos (migración exacta de PortalApi.pedidos)
 * Incluye todas las operaciones CRUD de pedidos y generación desde cotizaciones
 */
export const pedidosApi = {
  /**
   * Obtener todos los pedidos
   */
  async getAll() {
    const response = await pedidosApiClient.get(API_ENDPOINTS.PEDIDOS.BASE)
    return response.datos || []
  },

  /**
   * Obtener pedido por número/ID
   */
  async getById(numPedido) {
    const response = await pedidosApiClient.get(API_ENDPOINTS.PEDIDOS.BY_ID(numPedido))
    return response.datos
  },

  /**
   * Generar nuevo pedido desde cotización
   */
  async generate(pedidoData) {
    const response = await pedidosApiClient.post(API_ENDPOINTS.PEDIDOS.GENERATE, pedidoData)
    return response
  },

  /**
   * Buscar pedidos por fecha desde
   */
  async searchByDateFrom(fechaDesde) {
    const response = await pedidosApiClient.get(API_ENDPOINTS.PEDIDOS.BY_DATE_FROM, {
      params: { fechaDesde }
    })
    return response.datos || []
  },

  /**
   * Validar datos de pedido antes de generar
   */
  validatePedido(pedidoData) {
    const errors = []

    if (!pedidoData.cotizacionId) {
      errors.push('Debe seleccionar una cotización')
    }

    if (!pedidoData.cveProveedor) {
      errors.push('Debe seleccionar un proveedor')
    }

    if (!pedidoData.fechaEmision) {
      errors.push('La fecha de emisión es requerida')
    }

    if (!pedidoData.fechaEntrega) {
      errors.push('La fecha de entrega es requerida')
    }

    // Validar que fecha de entrega sea posterior a emisión
    if (pedidoData.fechaEmision && pedidoData.fechaEntrega) {
      const emision = new Date(pedidoData.fechaEmision)
      const entrega = new Date(pedidoData.fechaEntrega)
      
      if (entrega <= emision) {
        errors.push('La fecha de entrega debe ser posterior a la fecha de emisión')
      }
    }

    if (!pedidoData.nivelSurtido || pedidoData.nivelSurtido < 1 || pedidoData.nivelSurtido > 100) {
      errors.push('El nivel de surtido debe estar entre 1% y 100%')
    }

    return {
      isValid: errors.length === 0,
      errors: errors
    }
  },

  /**
   * Formatear datos del pedido para envío
   */
  formatPedidoData(rawData) {
    return {
      cotizacionId: parseInt(rawData.cotizacionId),
      cveProveedor: rawData.cveProveedor?.trim(),
      fechaEmision: rawData.fechaEmision,
      fechaEntrega: rawData.fechaEntrega,
      nivelSurtido: parseInt(rawData.nivelSurtido || 100)
    }
  },

  /**
   * Calcular preview de pedido basado en cotización y nivel de surtido
   */
  calculatePedidoPreview(cotizacion, nivelSurtido = 100) {
    if (!cotizacion) {
      return {
        componentesIncluidos: 0,
        totalEstimado: 0,
        lineasTotales: 0
      }
    }

    const totalComponentes = cotizacion.detalles?.length || 0
    const porcentaje = nivelSurtido / 100
    const componentesIncluidos = Math.ceil(totalComponentes * porcentaje)
    const totalEstimado = (cotizacion.total || 0) * porcentaje

    return {
      componentesIncluidos,
      totalEstimado,
      lineasTotales: totalComponentes,
      porcentajeSurtido: nivelSurtido
    }
  },

  /**
   * Obtener resumen de pedido para display
   */
  getPedidoSummary(pedido) {
    return {
      numPedido: pedido.numPedido,
      nombreProveedor: pedido.nombreProveedor,
      cveProveedor: pedido.cveProveedor,
      fechaEmision: pedido.fechaEmision,
      fechaEntrega: pedido.fechaEntrega,
      total: pedido.total || 0,
      totalDetalles: pedido.totalDetalles || 0,
      nivelSurtido: pedido.nivelSurtido || 100,
      displayName: `#${pedido.numPedido} - ${pedido.nombreProveedor}`,
      formattedTotal: this.formatCurrency(pedido.total),
      formattedEmision: this.formatDate(pedido.fechaEmision),
      formattedEntrega: this.formatDate(pedido.fechaEntrega),
      hasDetalles: (pedido.totalDetalles || 0) > 0
    }
  },

  /**
   * Realizar búsqueda local en lista de pedidos
   */
  searchLocal(pedidos, searchTerm) {
    if (!searchTerm) return pedidos
    
    const term = searchTerm.toLowerCase().trim()
    
    return pedidos.filter(pedido => {
      return pedido.numPedido.toString().includes(term) ||
             pedido.nombreProveedor?.toLowerCase().includes(term) ||
             pedido.cveProveedor?.toLowerCase().includes(term)
    })
  },

  /**
   * Filtrar pedidos por fecha desde
   */
  filterByDateFrom(pedidos, fechaDesde) {
    if (!fechaDesde) return pedidos
    
    return pedidos.filter(pedido => {
      return pedido.fechaEmision >= fechaDesde
    })
  },

  /**
   * Aplicar filtros combinados (búsqueda + fecha)
   */
  applyFilters(pedidos, filters) {
    let filtered = [...pedidos]
    
    // Aplicar búsqueda
    if (filters.searchTerm) {
      filtered = this.searchLocal(filtered, filters.searchTerm)
    }
    
    // Aplicar filtro de fecha
    if (filters.fechaDesde) {
      filtered = this.filterByDateFrom(filtered, filters.fechaDesde)
    }
    
    return filtered
  },

  /**
   * Formatear fecha para display
   */
  formatDate(dateString) {
    if (!dateString) return '-'
    
    try {
      return new Date(dateString).toLocaleDateString('es-MX', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
    } catch (error) {
      return dateString
    }
  },

  /**
   * Formatear moneda para display
   */
  formatCurrency(amount) {
    if (amount == null || isNaN(amount)) return '0.00'
    
    return parseFloat(amount).toLocaleString('es-MX', {
      style: 'currency',
      currency: 'MXN',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })
  },

  /**
   * Obtener fechas por defecto para nuevo pedido
   */
  getDefaultDates() {
    const today = new Date()
    const nextWeek = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000)
    
    return {
      fechaEmision: today.toISOString().split('T')[0],
      fechaEntrega: nextWeek.toISOString().split('T')[0]
    }
  },

  /**
   * Validar fechas del pedido
   */
  validateDates(fechaEmision, fechaEntrega) {
    const errors = []
    
    if (!fechaEmision) {
      errors.push('La fecha de emisión es requerida')
    }
    
    if (!fechaEntrega) {
      errors.push('La fecha de entrega es requerida')
    }
    
    if (fechaEmision && fechaEntrega) {
      const emision = new Date(fechaEmision)
      const entrega = new Date(fechaEntrega)
      
      if (entrega <= emision) {
        errors.push('La fecha de entrega debe ser posterior a la fecha de emisión')
      }
      
      // Validar que no sean fechas muy antiguas
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      
      if (emision < today) {
        errors.push('La fecha de emisión no puede ser anterior a hoy')
      }
    }
    
    return {
      isValid: errors.length === 0,
      errors: errors
    }
  },

  /**
   * Generar objeto de configuración para slider de nivel de surtido
   */
  getSurtidoSliderConfig() {
    return {
      min: 1,
      max: 100,
      step: 1,
      default: 100,
      marks: {
        25: '25%',
        50: '50%',
        75: '75%',
        100: '100%'
      }
    }
  }
}

export default pedidosApi