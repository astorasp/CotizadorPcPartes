/**
 * Configuración global del Portal Cotizador
 */

// Configuración de la API
const API_CONFIG = {
    BASE_URL: 'http://localhost:8080/cotizador/v1/api',
    TIMEOUT: 30000, // 30 segundos
    HEADERS: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    },
    // Configuración de autenticación básica
    AUTH: {
        USERNAME: 'admin',
        PASSWORD: 'admin123',
        ENABLED: true
    }
};

// Endpoints de la API
const API_ENDPOINTS = {
    // Componentes
    COMPONENTES: {
        BASE: '/componentes',
        BY_ID: (id) => `/componentes/${id}`,
        BY_TYPE: (tipo) => `/componentes/tipo/${tipo}`,
        EXISTS: (id) => `/componentes/${id}/existe`
    },
    
    // PCs
    PCS: {
        BASE: '/pcs',
        BY_ID: (id) => `/pcs/${id}`,
        COMPONENTS: (pcId) => `/pcs/${pcId}/componentes`,
        ADD_COMPONENT: (pcId) => `/pcs/${pcId}/componentes`,
        REMOVE_COMPONENT: (pcId, componenteId) => `/pcs/${pcId}/componentes/${componenteId}`
    },
    
    // Cotizaciones
    COTIZACIONES: {
        BASE: '/cotizaciones',
        BY_ID: (id) => `/cotizaciones/${id}`,
        BY_DATE: '/cotizaciones/buscar/fecha'
    },
    
    // Proveedores
    PROVEEDORES: {
        BASE: '/proveedores',
        BY_CVE: (cve) => `/proveedores/${cve}`,
        BY_NAME: '/proveedores/buscar/nombre',
        BY_RAZON: '/proveedores/buscar/razon-social'
    },
    
    // Pedidos
    PEDIDOS: {
        BASE: '/pedidos',
        BY_ID: (id) => `/pedidos/${id}`,
        GENERATE: '/pedidos/generar'
    },
    
    // Promociones
    PROMOCIONES: {
        BASE: '/promociones',
        BY_ID: (id) => `/promociones/${id}`
    }
};

// Tipos de componentes válidos
const COMPONENT_TYPES = {
    MONITOR: 'MONITOR',
    DISCO_DURO: 'DISCO_DURO',
    TARJETA_VIDEO: 'TARJETA_VIDEO',
    PC: 'PC'
};

// Labels para tipos de componentes
const COMPONENT_TYPE_LABELS = {
    [COMPONENT_TYPES.MONITOR]: 'Monitor',
    [COMPONENT_TYPES.DISCO_DURO]: 'Disco Duro',
    [COMPONENT_TYPES.TARJETA_VIDEO]: 'Tarjeta de Video',
    [COMPONENT_TYPES.PC]: 'PC Completa'
};

// Códigos de respuesta de la API
const API_RESPONSE_CODES = {
    SUCCESS: '0',
    VALIDATION_ERROR: '1',
    NOT_FOUND: '2',
    INTERNAL_ERROR: '3',
    BUSINESS_RULE_ERROR: '4'
};

// Tipos de alertas para el UI
const ALERT_TYPES = {
    SUCCESS: 'success',
    ERROR: 'error',
    WARNING: 'warning',
    INFO: 'info'
};

// Configuraciones de UI
const UI_CONFIG = {
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
};

// Mensajes del sistema
const MESSAGES = {
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
};

// Expresiones regulares para validaciones
const REGEX_PATTERNS = {
    COMPONENT_ID: /^[A-Z0-9]{2,10}$/,
    EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    PHONE: /^\+?[\d\s\-\(\)]{8,15}$/,
    CURRENCY: /^\d+(\.\d{1,2})?$/,
    ALPHANUMERIC: /^[a-zA-Z0-9\s]+$/
};

// Configuración de desarrollo/debug
const DEBUG_CONFIG = {
    ENABLED: window.location.hostname === 'localhost',
    LOG_API_CALLS: true,
    LOG_STATE_CHANGES: true,
    SHOW_DEBUG_INFO: false
};

// Función helper para logging condicional
const debugLog = (message, data = null) => {
    if (DEBUG_CONFIG.ENABLED) {
        if (data) {
            console.log(`[Portal Cotizador] ${message}`, data);
        } else {
            console.log(`[Portal Cotizador] ${message}`);
        }
    }
};

// Función helper para errores
const debugError = (message, error = null) => {
    if (DEBUG_CONFIG.ENABLED) {
        if (error) {
            console.error(`[Portal Cotizador ERROR] ${message}`, error);
        } else {
            console.error(`[Portal Cotizador ERROR] ${message}`);
        }
    }
};

// Exportar configuraciones para uso global
window.PortalConfig = {
    API_CONFIG,
    API_ENDPOINTS,
    COMPONENT_TYPES,
    COMPONENT_TYPE_LABELS,
    API_RESPONSE_CODES,
    ALERT_TYPES,
    UI_CONFIG,
    MESSAGES,
    REGEX_PATTERNS,
    DEBUG_CONFIG,
    debugLog,
    debugError
}; 