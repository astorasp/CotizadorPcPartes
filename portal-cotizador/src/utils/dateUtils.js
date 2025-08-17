/**
 * Utilidades para manejo de fechas
 */

/**
 * Formatea una fecha a string legible
 * @param {string|Date} fecha - Fecha a formatear
 * @param {Object} opciones - Opciones de formato
 * @returns {string} Fecha formateada
 */
export function formatDate(fecha, opciones = {}) {
  if (!fecha) return '-'
  
  try {
    const date = typeof fecha === 'string' ? new Date(fecha) : fecha
    
    const defaultOptions = {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      ...opciones
    }
    
    return date.toLocaleDateString('es-ES', defaultOptions)
  } catch (error) {
    console.error('Error al formatear fecha:', error)
    return '-'
  }
}

/**
 * Formatea una fecha con hora
 * @param {string|Date} fecha - Fecha a formatear
 * @returns {string} Fecha y hora formateada
 */
export function formatDateTime(fecha) {
  return formatDate(fecha, {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * Obtiene tiempo relativo (ej: "hace 2 horas")
 * @param {string|Date} fecha - Fecha a comparar
 * @returns {string} Tiempo relativo
 */
export function timeAgo(fecha) {
  if (!fecha) return '-'
  
  try {
    const date = typeof fecha === 'string' ? new Date(fecha) : fecha
    const now = new Date()
    const diffMs = now - date
    const diffMinutes = Math.floor(diffMs / (1000 * 60))
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
    
    if (diffMinutes < 1) return 'ahora'
    if (diffMinutes < 60) return `hace ${diffMinutes} min`
    if (diffHours < 24) return `hace ${diffHours}h`
    if (diffDays < 30) return `hace ${diffDays}d`
    
    return formatDate(fecha)
  } catch (error) {
    console.error('Error al calcular tiempo relativo:', error)
    return '-'
  }
}