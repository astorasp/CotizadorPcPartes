import { ref, readonly } from 'vue'
import { UI_CONFIG, REGEX_PATTERNS } from '@/utils/constants'

// Estado global para alertas
const alerts = ref([])

export const useUtils = () => {
  
  // 🚨 Sistema de alertas (migración de PortalUtils.Alert)
  const showAlert = (type, message, duration = UI_CONFIG.ALERT_AUTO_CLOSE_TIME) => {
    const id = Date.now()
    const alert = {
      id,
      type, // 'success', 'error', 'warning', 'info'
      message,
      visible: true
    }
    
    alerts.value.push(alert)
    
    // Auto-cerrar después del duration
    if (duration > 0) {
      setTimeout(() => {
        closeAlert(id)
      }, duration)
    }
    
    return id
  }
  
  const closeAlert = (id) => {
    const index = alerts.value.findIndex(alert => alert.id === id)
    if (index > -1) {
      alerts.value.splice(index, 1)
    }
  }
  
  const clearAllAlerts = () => {
    alerts.value = []
  }
  
  // 💰 Formateo de moneda (migración de PortalUtils.Format)
  const formatCurrency = (amount) => {
    if (amount === null || amount === undefined || isNaN(amount)) {
      return '$0.00'
    }
    
    return new Intl.NumberFormat('es-MX', UI_CONFIG.CURRENCY_FORMAT).format(amount)
  }
  
  // 📅 Formateo de fechas
  const formatDate = (date, options = {}) => {
    if (!date) return ''
    
    const defaultOptions = UI_CONFIG.DATE_FORMAT
    
    try {
      // Manejo especial para strings de fecha en formato ISO (yyyy-MM-dd)
      // para evitar problemas de timezone
      let dateObj
      if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
        // Para fechas en formato yyyy-MM-dd, crearlas como fecha local
        const [year, month, day] = date.split('-').map(Number)
        dateObj = new Date(year, month - 1, day) // month - 1 porque los meses son 0-indexed
      } else {
        dateObj = new Date(date)
      }
      
      return new Intl.DateTimeFormat('es-MX', {
        ...defaultOptions,
        ...options
      }).format(dateObj)
    } catch (error) {
      console.error('Error formatting date:', error)
      return date.toString()
    }
  }
  
  const formatDateTime = (date) => {
    return formatDate(date, UI_CONFIG.DATETIME_FORMAT)
  }
  
  // ✅ Confirmaciones
  const confirm = (title, message) => {
    return new Promise((resolve) => {
      // TODO: Implementar modal de confirmación personalizado
      // Por ahora usar confirm nativo
      const result = window.confirm(`${title}\n\n${message}`)
      resolve(result)
    })
  }
  
  // 🔄 Debounce para búsquedas
  const debounce = (func, delay = UI_CONFIG.SEARCH_DEBOUNCE_TIME) => {
    let timeoutId
    return (...args) => {
      clearTimeout(timeoutId)
      timeoutId = setTimeout(() => func.apply(null, args), delay)
    }
  }
  
  // 📝 Validaciones (migración de validaciones del proyecto original)
  const validateRequired = (value) => {
    return value !== null && value !== undefined && value !== ''
  }
  
  const validateEmail = (email) => {
    return REGEX_PATTERNS.EMAIL.test(email)
  }
  
  const validatePrice = (price) => {
    return !isNaN(price) && parseFloat(price) > 0
  }
  
  const validateComponentId = (id) => {
    return REGEX_PATTERNS.COMPONENT_ID.test(id)
  }
  
  const validateCurrency = (value) => {
    return REGEX_PATTERNS.CURRENCY.test(value)
  }
  
  const validatePhone = (phone) => {
    return REGEX_PATTERNS.PHONE.test(phone)
  }
  
  // 🎨 Utilidades de UI
  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
  
  const copyToClipboard = async (text) => {
    try {
      await navigator.clipboard.writeText(text)
      showAlert('success', 'Copiado al portapapeles')
      return true
    } catch (error) {
      showAlert('error', 'Error al copiar al portapapeles')
      return false
    }
  }
  
  // 🔢 Utilidades numéricas
  const parseNumber = (value) => {
    const parsed = parseFloat(value)
    return isNaN(parsed) ? 0 : parsed
  }
  
  const formatNumber = (value, decimals = 2) => {
    return parseNumber(value).toFixed(decimals)
  }
  
  // 📊 Utilidades de arrays
  const groupBy = (array, key) => {
    return array.reduce((groups, item) => {
      const group = item[key]
      if (!groups[group]) {
        groups[group] = []
      }
      groups[group].push(item)
      return groups
    }, {})
  }
  
  const sortBy = (array, key, order = 'asc') => {
    return [...array].sort((a, b) => {
      const aVal = a[key]
      const bVal = b[key]
      
      if (order === 'desc') {
        return bVal > aVal ? 1 : -1
      }
      return aVal > bVal ? 1 : -1
    })
  }
  
  // 🔍 Utilidades de búsqueda
  const filterBySearch = (items, searchTerm, searchKeys) => {
    if (!searchTerm.trim()) return items
    
    const term = searchTerm.toLowerCase()
    
    return items.filter(item => {
      return searchKeys.some(key => {
        const value = item[key]
        return value && value.toString().toLowerCase().includes(term)
      })
    })
  }
  
  return {
    // Alertas
    alerts: readonly(alerts),
    showAlert,
    closeAlert,
    clearAllAlerts,
    
    // Formateo
    formatCurrency,
    formatDate,
    formatDateTime,
    formatNumber,
    
    // UI
    confirm,
    debounce,
    scrollToTop,
    copyToClipboard,
    
    // Validaciones
    validateRequired,
    validateEmail,
    validatePrice,
    validateComponentId,
    validateCurrency,
    validatePhone,
    
    // Utilidades
    parseNumber,
    groupBy,
    sortBy,
    filterBySearch
  }
}