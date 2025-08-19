/**
 * Constantes y configuraciones migradas del proyecto original
 */

// Configuración de la API
export const API_CONFIG = {
  // URLs base por microservicio via gateway
  COMPONENTES_BASE_URL: import.meta.env.VITE_COMPONENTES_API_BASE_URL || '/api/componentes',
  COTIZACIONES_BASE_URL: import.meta.env.VITE_COTIZACIONES_API_BASE_URL || '/api/cotizaciones',
  PEDIDOS_BASE_URL: import.meta.env.VITE_PEDIDOS_API_BASE_URL || '/api/pedidos',
  SEGURIDAD_BASE_URL: import.meta.env.VITE_SEGURIDAD_API_BASE_URL || '/api/seguridad',
  // URL legacy para compatibilidad (deprecada)
  BASE_URL: import.meta.env.VITE_API_BASE_URL || '/api/cotizador',
  TIMEOUT: 30000, // 30 segundos
  HEADERS: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
}

// Endpoints de la API
export const API_ENDPOINTS = {
  // Componentes
  COMPONENTES: {
    BASE: '',
    BY_ID: (id) => `/${id}`,
    BY_TYPE: (tipo) => `/tipo/${tipo}`,
    EXISTS: (id) => `/${id}/existe`
  },
  
  // PCs
  PCS: {
    BASE: '',
    BY_ID: (id) => `/${id}`,
    COMPONENTS: (pcId) => `/${pcId}/componentes`,
    ADD_COMPONENT: (pcId) => `/${pcId}/componentes`,
    REMOVE_COMPONENT: (pcId, componenteId) => `/${pcId}/componentes/${componenteId}`
  },
  
  // Cotizaciones
  COTIZACIONES: {
    BASE: '',
    BY_ID: (id) => `/${id}`,
    BY_DATE: '/buscar/fecha'
  },
  
  // Proveedores
  PROVEEDORES: {
    BASE: '',
    BY_CVE: (cve) => `/${cve}`,
    BY_NAME: '/buscar/nombre',
    BY_RAZON: '/buscar/razon-social'
  },
  
  // Pedidos
  PEDIDOS: {
    BASE: '',
    BY_ID: (id) => `/${id}`,
    GENERATE: '/generar',
    BY_DATE_FROM: '/buscar/fecha'
  },
  
  // Promociones
  PROMOCIONES: {
    BASE: '',
    BY_ID: (id) => `/${id}`
  }
}

// Tipos de componentes válidos
export const COMPONENT_TYPES = {
  MONITOR: 'MONITOR',
  DISCO_DURO: 'DISCO_DURO',
  TARJETA_VIDEO: 'TARJETA_VIDEO',
  PC: 'PC'
}

// Labels para tipos de componentes
export const COMPONENT_TYPE_LABELS = {
  [COMPONENT_TYPES.MONITOR]: 'Monitor',
  [COMPONENT_TYPES.DISCO_DURO]: 'Disco Duro',
  [COMPONENT_TYPES.TARJETA_VIDEO]: 'Tarjeta de Video',
  [COMPONENT_TYPES.PC]: 'PC Completa'
}

// Tipos de cotizador
export const COTIZADOR_TYPES = {
  COTIZADOR_A: 'COTIZADOR_A',
  COTIZADOR_B: 'COTIZADOR_B'
}

// Labels para tipos de cotizador
export const COTIZADOR_TYPE_LABELS = {
  [COTIZADOR_TYPES.COTIZADOR_A]: 'Cotizador A',
  [COTIZADOR_TYPES.COTIZADOR_B]: 'Cotizador B'
}

// Tipos de impuestos
export const TAX_TYPES = {
  IVA: 'IVA',
  ISR: 'ISR',
  IEPS: 'IEPS',
  SALES_TAX: 'SALES_TAX'
}

// Labels para tipos de impuestos
export const TAX_TYPE_LABELS = {
  [TAX_TYPES.IVA]: 'IVA',
  [TAX_TYPES.ISR]: 'ISR',
  [TAX_TYPES.IEPS]: 'IEPS',
  [TAX_TYPES.SALES_TAX]: 'Sales Tax'
}

// Países para impuestos
export const COUNTRIES = {
  MX: 'MX',
  US: 'US',
  CA: 'CA'
}

// Labels para países
export const COUNTRY_LABELS = {
  [COUNTRIES.MX]: 'México',
  [COUNTRIES.US]: 'Estados Unidos',
  [COUNTRIES.CA]: 'Canadá'
}

// Códigos de respuesta de la API
export const API_RESPONSE_CODES = {
  SUCCESS: '0',
  VALIDATION_ERROR: '1',
  NOT_FOUND: '2',
  INTERNAL_ERROR: '3',
  BUSINESS_RULE_ERROR: '4'
}

// Tipos de alertas para el UI
export const ALERT_TYPES = {
  SUCCESS: 'success',
  ERROR: 'error',
  WARNING: 'warning',
  INFO: 'info'
}

// Configuraciones de UI
export const UI_CONFIG = {
  // Tiempo de auto-cierre para alertas (milisegundos)
  ALERT_AUTO_CLOSE_TIME: 5000,
  
  // Debounce time para búsquedas (milisegundos)
  SEARCH_DEBOUNCE_TIME: 300,
  
  // Paginación
  DEFAULT_PAGE_SIZE: 5,
  PAGE_SIZE_OPTIONS: [5, 10, 20, 30],
  MAX_VISIBLE_PAGES: 5,
  
  // Validaciones
  MAX_FILE_SIZE: 5 * 1024 * 1024, // 5MB
  ALLOWED_IMAGE_TYPES: ['image/jpeg', 'image/png', 'image/gif'],
  
  // Formatos
  CURRENCY_FORMAT: {
    style: 'currency',
    currency: 'MXN',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  },
  
  DATE_FORMAT: {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  },
  
  DATETIME_FORMAT: {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }
}

// Mensajes del sistema
export const MESSAGES = {
  // Generales
  LOADING: 'Cargando...',
  SAVING: 'Guardando...',
  DELETING: 'Eliminando...',
  CONFIRM_DELETE: '¿Está seguro de que desea eliminar este elemento?',
  OPERATION_SUCCESS: 'Operación realizada exitosamente',
  OPERATION_ERROR: 'Error al realizar la operación',
  
  // Autenticación
  AUTH_REQUIRED: 'Autenticación requerida',
  UNAUTHORIZED: 'No autorizado. Verifique sus credenciales.',
  FORBIDDEN: 'Acceso denegado.',
  
  // Componentes
  COMPONENT_CREATED: 'Componente creado exitosamente',
  COMPONENT_UPDATED: 'Componente actualizado exitosamente',
  COMPONENT_DELETED: 'Componente eliminado exitosamente',
  COMPONENT_NOT_FOUND: 'Componente no encontrado',
  COMPONENT_EXISTS: 'Ya existe un componente con este ID',
  
  // PCs
  PC_CREATED: 'PC creada exitosamente',
  PC_UPDATED: 'PC actualizada exitosamente',
  PC_DELETED: 'PC eliminada exitosamente',
  PC_NOT_FOUND: 'PC no encontrada',
  PC_EXISTS: 'Ya existe una PC con este ID',
  PC_COMPONENT_ADDED: 'Componente agregado a la PC exitosamente',
  PC_COMPONENT_REMOVED: 'Componente quitado de la PC exitosamente',
  PC_COMPONENT_NOT_FOUND: 'Componente no encontrado en la PC',
  
  // Cotizaciones
  COTIZACION_CREATED: 'Cotización creada exitosamente',
  COTIZACION_UPDATED: 'Cotización actualizada exitosamente',
  COTIZACION_DELETED: 'Cotización eliminada exitosamente',
  COTIZACION_NOT_FOUND: 'Cotización no encontrada',
  COTIZACION_COMPONENT_ADDED: 'Componente agregado a la cotización exitosamente',
  COTIZACION_COMPONENT_REMOVED: 'Componente quitado de la cotización exitosamente',
  COTIZACION_VALIDATION_ERROR: 'Error de validación en la cotización',
  
  // Proveedores
  PROVEEDOR_CREATED: 'Proveedor creado exitosamente',
  PROVEEDOR_UPDATED: 'Proveedor actualizado exitosamente',
  PROVEEDOR_DELETED: 'Proveedor eliminado exitosamente',
  PROVEEDOR_NOT_FOUND: 'Proveedor no encontrado',
  PROVEEDOR_EXISTS: 'Ya existe un proveedor con esta clave',
  PROVEEDOR_VALIDATION_ERROR: 'Error de validación en el proveedor',
  
  // Pedidos
  PEDIDO_GENERATED: 'Pedido generado exitosamente',
  PEDIDO_NOT_FOUND: 'Pedido no encontrado',
  PEDIDO_VALIDATION_ERROR: 'Error de validación en el pedido',
  PEDIDO_GENERATION_ERROR: 'Error al generar el pedido',
  INVALID_COTIZACION: 'Debe seleccionar una cotización válida',
  INVALID_PROVEEDOR: 'Debe seleccionar un proveedor válido',
  INVALID_DATES: 'Las fechas ingresadas no son válidas',
  INVALID_SURTIDO_LEVEL: 'El nivel de surtido debe estar entre 1% y 100%',
  
  // Promociones
  PROMOCION_CREATED: 'Promoción creada exitosamente',
  PROMOCION_UPDATED: 'Promoción actualizada exitosamente',
  PROMOCION_DELETED: 'Promoción eliminada exitosamente',
  PROMOCION_NOT_FOUND: 'Promoción no encontrada',
  PROMOCION_VALIDATION_ERROR: 'Error de validación en la promoción',
  INVALID_PROMOTION_TYPE: 'Tipo de promoción no válido',
  INVALID_PROMOTION_DATES: 'Las fechas de vigencia no son válidas',
  INVALID_DISCOUNT_AMOUNT: 'El monto de descuento debe ser mayor a 0',
  INVALID_DISCOUNT_PERCENTAGE: 'El porcentaje debe estar entre 1% y 100%',
  INVALID_QUANTITY_CONFIG: 'La configuración de cantidad no es válida',
  INVALID_NXM_CONFIG: 'La configuración N x M no es válida',
  
  // Validaciones
  REQUIRED_FIELD: 'Este campo es obligatorio',
  INVALID_EMAIL: 'Formato de email inválido',
  INVALID_NUMBER: 'Debe ser un número válido',
  INVALID_PRICE: 'El precio debe ser mayor a 0',
  INVALID_ID_FORMAT: 'Formato de ID inválido',
  
  // Errores de red
  NETWORK_ERROR: 'Error de conexión. Verifique su conexión a internet.',
  SERVER_ERROR: 'Error del servidor. Intente nuevamente más tarde.',
  TIMEOUT_ERROR: 'Tiempo de espera agotado. Intente nuevamente.',
  
  // Estados vacíos
  NO_DATA_FOUND: 'No se encontraron datos',
  NO_COMPONENTS_FOUND: 'No se encontraron componentes',
  NO_SEARCH_RESULTS: 'No se encontraron resultados para su búsqueda'
}

// Expresiones regulares para validaciones
export const REGEX_PATTERNS = {
  COMPONENT_ID: /^[A-Z0-9]{2,10}$/,
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PHONE: /^\+?[\d\s\-\(\)]{8,15}$/,
  CURRENCY: /^\d+(\.\d{1,2})?$/,
  ALPHANUMERIC: /^[a-zA-Z0-9\s]+$/
}

// Configuración de desarrollo/debug
export const DEBUG_CONFIG = {
  ENABLED: import.meta.env.DEV,
  LOG_API_CALLS: true,
  LOG_STATE_CHANGES: true,
  SHOW_DEBUG_INFO: false
}