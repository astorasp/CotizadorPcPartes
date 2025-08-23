import promocionesApiClient from './promocionesApiClient'
import { API_ENDPOINTS } from '@/utils/constants'

/**
 * API service para Promociones (migración exacta de PortalApi.promociones)
 * Incluye todas las operaciones CRUD de promociones con tipos complejos
 */
export const promocionesApi = {
  /**
   * Obtener todas las promociones
   */
  async getAll() {
    const response = await promocionesApiClient.get(API_ENDPOINTS.PROMOCIONES.BASE)
    return response.datos || []
  },

  /**
   * Obtener promoción por ID
   */
  async getById(id) {
    const response = await promocionesApiClient.get(API_ENDPOINTS.PROMOCIONES.BY_ID(id))
    return response.datos
  },

  /**
   * Crear nueva promoción
   */
  async create(promocionData) {
    const response = await promocionesApiClient.post(API_ENDPOINTS.PROMOCIONES.BASE, promocionData)
    return response
  },

  /**
   * Actualizar promoción existente
   */
  async update(id, promocionData) {
    const response = await promocionesApiClient.put(API_ENDPOINTS.PROMOCIONES.BY_ID(id), promocionData)
    return response
  },

  /**
   * Eliminar promoción
   */
  async delete(id) {
    const response = await promocionesApiClient.delete(API_ENDPOINTS.PROMOCIONES.BY_ID(id))
    return response
  },

  /**
   * Validar datos de promoción antes de enviar
   */
  validatePromocion(promocionData) {
    const errors = []

    if (!promocionData.nombre) {
      errors.push('El nombre de la promoción es requerido')
    } else if (promocionData.nombre.length > 100) {
      errors.push('El nombre no puede exceder 100 caracteres')
    }

    if (!promocionData.descripcion) {
      errors.push('La descripción es requerida')
    } else if (promocionData.descripcion.length > 500) {
      errors.push('La descripción no puede exceder 500 caracteres')
    }

    if (!promocionData.vigenciaDesde) {
      errors.push('La fecha de inicio de vigencia es requerida')
    }

    if (!promocionData.vigenciaHasta) {
      errors.push('La fecha de fin de vigencia es requerida')
    }

    // Validar fechas
    if (promocionData.vigenciaDesde && promocionData.vigenciaHasta) {
      const fechaInicio = new Date(promocionData.vigenciaDesde)
      const fechaFin = new Date(promocionData.vigenciaHasta)
      
      if (fechaFin <= fechaInicio) {
        errors.push('La fecha de fin debe ser posterior a la fecha de inicio')
      }
    }

    return {
      isValid: errors.length === 0,
      errors: errors
    }
  },

  /**
   * Validar fechas específicamente
   */
  validateDates(fechaInicio, fechaFin) {
    const errors = []
    
    if (!fechaInicio) {
      errors.push('La fecha de inicio es requerida')
    }
    
    if (!fechaFin) {
      errors.push('La fecha de fin es requerida')
    }
    
    if (fechaInicio && fechaFin) {
      const inicio = new Date(fechaInicio)
      const fin = new Date(fechaFin)
      
      if (fin <= inicio) {
        errors.push('La fecha de fin debe ser posterior a la fecha de inicio')
      }
      
      // Validar que no sean fechas muy antiguas
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      
      if (fin < today) {
        errors.push('La fecha de fin no puede ser anterior a hoy')
      }
    }
    
    return {
      isValid: errors.length === 0,
      errors: errors
    }
  },

  /**
   * Formatear datos de promoción para envío
   */
  formatPromocionData(rawData) {
    return {
      nombre: rawData.nombre?.trim(),
      descripcion: rawData.descripcion?.trim(),
      vigenciaDesde: rawData.vigenciaDesde,
      vigenciaHasta: rawData.vigenciaHasta,
      detalles: rawData.detalles || []
    }
  },

  /**
   * Crear detalles de promoción según el tipo (arquitectura base + acumulable)
   */
  createDetallesForType(tipoData) {
    const detalles = []

    switch(tipoData.tipo) {
      case 'SIN_DESCUENTO':
        // No crear detalles - promoción sin descuento se guarda solo en tabla principal
        break
        
      case 'DESCUENTO_PLANO':
      case 'DESCUENTO_PORCENTUAL':
        // Crear detalle base obligatorio
        detalles.push({
          nombre: 'Base Sin Descuento',
          esBase: true,
          tipoBase: 'SIN_DESCUENTO'
        })
        
        // Crear detalle acumulable con el descuento
        detalles.push({
          nombre: `Descuento Plano ${tipoData.porcentajeDescuento}%`,
          esBase: false,
          tipoAcumulable: 'DESCUENTO_PLANO',
          porcentajeDescuentoPlano: parseFloat(tipoData.porcentajeDescuento) || 0
        })
        break
        
      case 'POR_CANTIDAD':
        // Crear detalle base obligatorio
        detalles.push({
          nombre: 'Base Sin Descuento',
          esBase: true,
          tipoBase: 'SIN_DESCUENTO'
        })
        
        // Crear detalle acumulable por cantidad con múltiples escalas
        const escalasDescuento = this.buildEscalasDescuento(tipoData)
        const nombreDetalle = escalasDescuento.length === 1 
          ? `Descuento por Cantidad (${escalasDescuento[0].cantidadMinima}+ unidades)`
          : `Descuento por Cantidad (${escalasDescuento.length} escalas)`
        
        detalles.push({
          nombre: nombreDetalle,
          esBase: false,
          tipoAcumulable: 'DESCUENTO_POR_CANTIDAD',
          escalasDescuento: escalasDescuento
        })
        break
        
      case 'NXM':
        // Solo crear detalle base para NXM
        detalles.push({
          nombre: `Promoción ${tipoData.nCompras}x${tipoData.mPago}`,
          esBase: true,
          tipoBase: 'NXM',
          parametrosNxM: {
            llevent: parseInt(tipoData.nCompras) || 1,
            paguen: parseInt(tipoData.mPago) || 1
          }
        })
        break
        
      default:
        // Promoción sin descuento
        detalles.push({
          nombre: 'Base Sin Descuento',
          esBase: true,
          tipoBase: 'SIN_DESCUENTO'
        })
    }

    return detalles
  },

  /**
   * Construir payload de promoción para envío al backend
   */
  buildPromocionPayload(formData) {
    const payload = {
      nombre: formData.nombre,
      descripcion: formData.descripcion,
      vigenciaDesde: formData.vigenciaDesde,
      vigenciaHasta: formData.vigenciaHasta,
      detalles: []
    }

    // Crear detalles según el tipo (puede generar múltiples detalles)
    if (formData.tipo) {
      const detalles = this.createDetallesForType(formData)
      if (detalles && detalles.length > 0) {
        payload.detalles.push(...detalles)
      }
    }

    return payload
  },

  /**
   * Obtener resumen de promoción para display
   */
  getPromocionSummary(promocion) {
    return {
      idPromocion: promocion.idPromocion,
      nombre: promocion.nombre,
      descripcion: promocion.descripcion,
      vigenciaDesde: promocion.vigenciaDesde,
      vigenciaHasta: promocion.vigenciaHasta,
      estadoVigencia: promocion.estadoVigencia,
      totalDetalles: promocion.totalDetalles || 0,
      tipoPromocionPrincipal: promocion.tipoPromocionPrincipal,
      diasRestantes: promocion.diasRestantes,
      displayName: `${promocion.nombre} (${this.getEstadoText(promocion.estadoVigencia)})`,
      hasDetalles: (promocion.totalDetalles || 0) > 0,
      isVigente: promocion.estadoVigencia === 'VIGENTE',
      isExpirada: promocion.estadoVigencia === 'EXPIRADA',
      isFutura: promocion.estadoVigencia === 'FUTURA'
    }
  },

  /**
   * Realizar búsqueda local en lista de promociones
   */
  searchLocal(promociones, searchTerm) {
    if (!searchTerm) return promociones
    
    const term = searchTerm.toLowerCase().trim()
    
    return promociones.filter(promocion => {
      return promocion.nombre?.toLowerCase().includes(term) ||
             promocion.descripcion?.toLowerCase().includes(term)
    })
  },

  /**
   * Filtrar promociones por estado
   */
  filterByEstado(promociones, estado) {
    if (!estado) return promociones
    
    return promociones.filter(promocion => {
      return promocion.estadoVigencia === estado
    })
  },

  /**
   * Aplicar filtros combinados (búsqueda + estado)
   */
  applyFilters(promociones, filters) {
    let filtered = [...promociones]
    
    // Aplicar búsqueda
    if (filters.searchTerm) {
      filtered = this.searchLocal(filtered, filters.searchTerm)
    }
    
    // Aplicar filtro de estado
    if (filters.estadoFilter) {
      filtered = this.filterByEstado(filtered, filters.estadoFilter)
    }
    
    return filtered
  },

  /**
   * Obtener badge HTML para estado
   */
  getEstadoBadge(estado) {
    const badges = {
      'VIGENTE': 'bg-green-100 text-green-800',
      'EXPIRADA': 'bg-red-100 text-red-800',
      'FUTURA': 'bg-blue-100 text-blue-800'
    }
    
    const badgeClass = badges[estado] || 'bg-gray-100 text-gray-800'
    const estadoText = this.getEstadoText(estado)
    
    return {
      class: badgeClass,
      text: estadoText
    }
  },

  /**
   * Obtener texto del estado
   */
  getEstadoText(estado) {
    const estados = {
      'VIGENTE': 'Vigente',
      'EXPIRADA': 'Expirada', 
      'FUTURA': 'Futura'
    }
    return estados[estado] || 'Sin estado'
  },

  /**
   * Obtener nombre de display para tipo de promoción
   */
  getTipoDisplayName(tipo) {
    const tipos = {
      'DESCUENTO_PLANO': 'Descuento Plano',
      'DESCUENTO_PORCENTUAL': 'Descuento Porcentual',
      'POR_CANTIDAD': 'Por Cantidad',
      'NXM': 'N x M',
      'DESCUENTO_ESCALONADO': 'Descuento Escalonado',
      'SIN_DESCUENTO': 'Sin Descuento'
    }
    return tipos[tipo] || tipo
  },

  /**
   * Obtener opciones de tipos de promoción
   */
  getTiposPromocion() {
    return [
      { value: 'SIN_DESCUENTO', label: 'Sin Descuento', description: 'Promoción regular sin descuentos' },
      { value: 'DESCUENTO_PLANO', label: 'Descuento Plano', description: 'Descuento porcentual aplicado al total' },
      { value: 'POR_CANTIDAD', label: 'Por Cantidad', description: 'Descuento por cantidad mínima' },
      { value: 'NXM', label: 'N x M', description: 'Lleva N unidades, paga M' }
    ]
  },

  /**
   * Obtener opciones de estados de promoción
   */
  getEstadosPromocion() {
    return [
      { value: 'VIGENTE', label: 'Vigente' },
      { value: 'EXPIRADA', label: 'Expirada' },
      { value: 'FUTURA', label: 'Futura' }
    ]
  },

  /**
   * Formatear fecha para display
   */
  formatDate(dateString) {
    if (!dateString) return 'No definida'
    
    try {
      return new Date(dateString).toLocaleDateString('es-MX', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    } catch (error) {
      return dateString
    }
  },

  /**
   * Truncar texto para display
   */
  truncateText(text, maxLength) {
    if (!text) return ''
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
  },

  /**
   * Calcular días restantes de una promoción
   */
  calculateDaysRemaining(fechaFin) {
    if (!fechaFin) return null
    
    try {
      const today = new Date()
      const endDate = new Date(fechaFin)
      const diffTime = endDate - today
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      
      return diffDays > 0 ? diffDays : 0
    } catch (error) {
      return null
    }
  },

  /**
   * Generar preview de promoción
   */
  generatePreviewContent(formData) {
    let content = `
      <div class="space-y-3">
        <div><strong>Nombre:</strong> ${formData.nombre}</div>
        <div><strong>Descripción:</strong> ${formData.descripcion}</div>
        <div><strong>Vigencia:</strong> ${this.formatDate(formData.vigenciaDesde)} - ${this.formatDate(formData.vigenciaHasta)}</div>
        <div><strong>Tipo:</strong> ${this.getTipoDisplayName(formData.tipo)}</div>
    `

    // Generar detalles para preview
    if (formData.tipo) {
      const detalles = this.createDetallesForType(formData)
      if (detalles && detalles.length > 0) {
        content += `<div><strong>Detalles:</strong></div>`
        detalles.forEach((detalle, index) => {
          content += `<div class="ml-4 text-sm">`
          content += `<strong>${index + 1}.</strong> ${detalle.nombre}`
          if (detalle.esBase) {
            content += ` <span class="text-blue-600">(Base)</span>`
          } else {
            content += ` <span class="text-green-600">(Acumulable)</span>`
          }
          content += `</div>`
        })
      }
    }

    // Agregar detalles específicos del tipo (información adicional)
    switch(formData.tipo) {
      case 'SIN_DESCUENTO':
        content += `<div><strong>Tipo:</strong> Promoción regular sin descuentos</div>`
        break
      case 'DESCUENTO_PLANO':
      case 'DESCUENTO_PORCENTUAL':
        content += `<div><strong>Porcentaje:</strong> ${formData.porcentajeDescuento}%</div>`
        break
      case 'POR_CANTIDAD':
        // Mostrar escalas múltiples si existen
        if (formData.escalas && Array.isArray(formData.escalas) && formData.escalas.length > 0) {
          content += `<div><strong>Escalas de Descuento:</strong></div>`
          content += `<div class="ml-4 space-y-1">`
          formData.escalas
            .sort((a, b) => a.cantidadMinima - b.cantidadMinima)
            .forEach((escala, index) => {
              const rango = escala.cantidadMaxima && escala.cantidadMaxima < 999999 
                ? `${escala.cantidadMinima}-${escala.cantidadMaxima}`
                : `${escala.cantidadMinima}+`
              content += `<div class="text-sm bg-blue-50 p-2 rounded">
                <strong>Escala ${index + 1}:</strong> ${rango} unidades → ${escala.porcentaje}% descuento
              </div>`
            })
          content += `</div>`
        }
        // Fallback para formato legacy
        else if (formData.cantidadMinima && formData.porcentajeDescuento) {
          content += `<div><strong>Cantidad Mínima:</strong> ${formData.cantidadMinima}</div>`
          content += `<div><strong>Descuento:</strong> ${formData.porcentajeDescuento}%</div>`
        }
        break
      case 'NXM':
        const descuentoCalculado = formData.nCompras && formData.mPago 
          ? ((formData.nCompras - formData.mPago) / formData.nCompras * 100).toFixed(1)
          : 0
        content += `<div><strong>Promoción:</strong> Compra ${formData.nCompras}, Paga ${formData.mPago}</div>`
        content += `<div><strong>Descuento equivalente:</strong> ${descuentoCalculado}%</div>`
        break
    }

    content += '</div>'
    return content
  },

  /**
   * Construir escalas de descuento para promociones POR_CANTIDAD
   */
  buildEscalasDescuento(tipoData) {
    // Si se proporcionan escalas múltiples (nuevo formato)
    if (tipoData.escalas && Array.isArray(tipoData.escalas) && tipoData.escalas.length > 0) {
      return tipoData.escalas
        .filter(escala => escala.cantidadMinima && escala.porcentaje)
        .map(escala => ({
          cantidadMinima: parseInt(escala.cantidadMinima),
          cantidadMaxima: escala.cantidadMaxima ? parseInt(escala.cantidadMaxima) : 999999,
          descuento: parseFloat(escala.porcentaje)
        }))
        .sort((a, b) => a.cantidadMinima - b.cantidadMinima)
    }
    
    // Fallback: formato legacy (una sola escala)
    if (tipoData.cantidadMinima && tipoData.porcentajeDescuento) {
      return [{
        cantidadMinima: parseInt(tipoData.cantidadMinima) || 1,
        cantidadMaxima: 999999,
        descuento: parseFloat(tipoData.porcentajeDescuento) || 0
      }]
    }
    
    // Fallback: escalas por defecto si no se proporciona información
    return [{
      cantidadMinima: 1,
      cantidadMaxima: 999999,
      descuento: 5.0
    }]
  },

  /**
   * Validar escalas de descuento múltiples
   */
  validateEscalasDescuento(escalas) {
    const errors = []
    
    if (!escalas || !Array.isArray(escalas) || escalas.length === 0) {
      return { isValid: false, errors: ['Se requiere al menos una escala de descuento'] }
    }
    
    // Validar cada escala individualmente
    escalas.forEach((escala, index) => {
      if (!escala.cantidadMinima || escala.cantidadMinima < 1) {
        errors.push(`Escala ${index + 1}: La cantidad mínima debe ser mayor a 0`)
      }
      
      if (escala.cantidadMaxima && escala.cantidadMaxima < escala.cantidadMinima) {
        errors.push(`Escala ${index + 1}: La cantidad máxima debe ser mayor a la mínima`)
      }
      
      if (!escala.porcentaje || escala.porcentaje <= 0 || escala.porcentaje > 100) {
        errors.push(`Escala ${index + 1}: El porcentaje debe estar entre 0.01 y 100`)
      }
    })
    
    // Validar que no hay solapamientos entre escalas
    const sorted = [...escalas].sort((a, b) => a.cantidadMinima - b.cantidadMinima)
    for (let i = 0; i < sorted.length - 1; i++) {
      const current = sorted[i]
      const next = sorted[i + 1]
      
      const currentMax = current.cantidadMaxima || 999999
      if (next.cantidadMinima <= currentMax) {
        errors.push(`Las escalas con rangos ${current.cantidadMinima}-${currentMax} y ${next.cantidadMinima}-${next.cantidadMaxima || '∞'} se solapan`)
      }
    }
    
    // Validar que los porcentajes son progresivos (opcional, pero recomendado)
    const porcentajes = sorted.map(e => e.porcentaje)
    for (let i = 0; i < porcentajes.length - 1; i++) {
      if (porcentajes[i] >= porcentajes[i + 1]) {
        errors.push('Se recomienda que los porcentajes de descuento sean progresivos (mayor cantidad = mayor descuento)')
        break
      }
    }
    
    return {
      isValid: errors.length === 0,
      errors: errors
    }
  },

  /**
   * Validar configuración específica por tipo
   */
  validateTipoConfig(tipoData) {
    const errors = []
    
    switch(tipoData.tipo) {
      case 'SIN_DESCUENTO':
        // No hay validaciones específicas para promociones sin descuento
        break
      case 'DESCUENTO_PLANO':
        if (!tipoData.porcentajeDescuento || tipoData.porcentajeDescuento <= 0 || tipoData.porcentajeDescuento > 100) {
          errors.push('El porcentaje debe estar entre 0.01 y 100')
        }
        break
      case 'DESCUENTO_PORCENTUAL':
        if (!tipoData.porcentajeDescuento || tipoData.porcentajeDescuento <= 0 || tipoData.porcentajeDescuento > 100) {
          errors.push('El porcentaje debe estar entre 1 y 100')
        }
        break
      case 'POR_CANTIDAD':
        // Validación para escalas múltiples (nuevo formato)
        if (tipoData.escalas && Array.isArray(tipoData.escalas) && tipoData.escalas.length > 0) {
          const escalasValidation = this.validateEscalasDescuento(tipoData.escalas)
          if (!escalasValidation.isValid) {
            errors.push(...escalasValidation.errors)
          }
        }
        // Validación legacy (formato anterior - cantidadMinima + porcentajeDescuento)
        else if (tipoData.cantidadMinima || tipoData.porcentajeDescuento) {
          if (!tipoData.cantidadMinima || tipoData.cantidadMinima <= 0) {
            errors.push('La cantidad mínima debe ser mayor a 0')
          }
          if (!tipoData.porcentajeDescuento || tipoData.porcentajeDescuento <= 0 || tipoData.porcentajeDescuento > 100) {
            errors.push('El porcentaje debe estar entre 1 y 100')
          }
        }
        // Si no hay ninguna configuración válida
        else {
          errors.push('Se requiere configurar escalas de descuento o una cantidad mínima con porcentaje')
        }
        break
      case 'NXM':
        if (!tipoData.nCompras || tipoData.nCompras <= 0) {
          errors.push('El número de compras debe ser mayor a 0')
        }
        if (!tipoData.mPago || tipoData.mPago <= 0) {
          errors.push('El número a pagar debe ser mayor a 0')
        }
        if (tipoData.nCompras && tipoData.mPago && tipoData.mPago >= tipoData.nCompras) {
          errors.push('El número a pagar debe ser menor al número de compras')
        }
        break
    }
    
    return {
      isValid: errors.length === 0,
      errors: errors
    }
  },

  /**
   * Obtener fechas por defecto para nueva promoción
   */
  getDefaultDates() {
    const today = new Date()
    const nextWeek = new Date(today)
    nextWeek.setDate(today.getDate() + 7)
    
    return {
      vigenciaDesde: today.toISOString().split('T')[0],
      vigenciaHasta: nextWeek.toISOString().split('T')[0]
    }
  }
}

export default promocionesApi