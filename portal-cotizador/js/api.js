/**
 * Utilidades para consumir la API del Portal Cotizador
 */

class ApiClient {
    constructor() {
        this.baseURL = PortalConfig.API_CONFIG.BASE_URL;
        this.timeout = PortalConfig.API_CONFIG.TIMEOUT;
        this.defaultHeaders = this.buildDefaultHeaders();
    }

    /**
     * Construye headers por defecto incluyendo autenticación básica si está habilitada
     */
    buildDefaultHeaders() {
        const headers = { ...PortalConfig.API_CONFIG.HEADERS };
        
        // Agregar autenticación básica si está habilitada
        if (PortalConfig.API_CONFIG.AUTH.ENABLED) {
            const credentials = btoa(`${PortalConfig.API_CONFIG.AUTH.USERNAME}:${PortalConfig.API_CONFIG.AUTH.PASSWORD}`);
            headers['Authorization'] = `Basic ${credentials}`;
        }
        
        return headers;
    }

    /**
     * Método genérico para realizar peticiones HTTP
     */
    async makeRequest(method, endpoint, data = null, customHeaders = {}) {
        const { debugLog, debugError } = PortalConfig;
        
        try {
            const url = `${this.baseURL}${endpoint}`;
            const headers = { ...this.defaultHeaders, ...customHeaders };
            
            const config = {
                method: method.toUpperCase(),
                headers,
                signal: AbortSignal.timeout(this.timeout)
            };

            // Agregar body solo para métodos que lo requieren
            if (data && ['POST', 'PUT', 'PATCH'].includes(method.toUpperCase())) {
                config.body = JSON.stringify(data);
            }

            debugLog(`API ${method.toUpperCase()} ${url}`, data);

            const response = await fetch(url, config);
            const responseData = await response.json();

            debugLog(`API Response ${response.status}`, responseData);

            return {
                success: response.ok,
                status: response.status,
                data: responseData
            };

        } catch (error) {
            debugError(`API Error: ${method} ${endpoint}`, error);
            
            if (error.name === 'TimeoutError') {
                throw new Error(PortalConfig.MESSAGES.TIMEOUT_ERROR);
            } else if (error.name === 'TypeError' && error.message.includes('fetch')) {
                throw new Error(PortalConfig.MESSAGES.NETWORK_ERROR);
            } else {
                throw new Error(PortalConfig.MESSAGES.SERVER_ERROR);
            }
        }
    }

    /**
     * Método GET
     */
    async get(endpoint, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const url = queryString ? `${endpoint}?${queryString}` : endpoint;
        return this.makeRequest('GET', url);
    }

    /**
     * Método POST
     */
    async post(endpoint, data) {
        return this.makeRequest('POST', endpoint, data);
    }

    /**
     * Método PUT
     */
    async put(endpoint, data) {
        return this.makeRequest('PUT', endpoint, data);
    }

    /**
     * Método DELETE
     */
    async delete(endpoint) {
        return this.makeRequest('DELETE', endpoint);
    }
}

/**
 * Servicio para manejar respuestas de la API
 */
class ApiResponseHandler {
    /**
     * Procesa la respuesta de la API y maneja errores
     */
    static handleResponse(response) {
        const { API_RESPONSE_CODES } = PortalConfig;
        
        if (!response.success) {
            throw new Error(`HTTP ${response.status}: ${response.data?.mensaje || 'Error desconocido'}`);
        }

        const apiResponse = response.data;
        
        // Verificar el código de respuesta de la API
        if (apiResponse.codigo !== API_RESPONSE_CODES.SUCCESS) {
            const errorMessage = apiResponse.mensaje || 'Error en la operación';
            throw new Error(errorMessage);
        }

        return apiResponse.datos;
    }

    /**
     * Maneja errores y los transforma en mensajes amigables
     */
    static handleError(error) {
        const { debugError } = PortalConfig;
        debugError('Error handling response', error);

        // Errores de red o timeout ya vienen con mensaje personalizado
        if (error.message.includes('conexión') || 
            error.message.includes('tiempo') || 
            error.message.includes('servidor')) {
            return error.message;
        }

        // Errores HTTP específicos
        if (error.message.includes('HTTP 401')) {
            return PortalConfig.MESSAGES.UNAUTHORIZED;
        }
        
        if (error.message.includes('HTTP 403')) {
            return PortalConfig.MESSAGES.FORBIDDEN;
        }
        
        if (error.message.includes('HTTP 404')) {
            return 'Recurso no encontrado';
        }
        
        if (error.message.includes('HTTP 400')) {
            return 'Datos inválidos o incompletos';
        }
        
        if (error.message.includes('HTTP 500')) {
            return 'Error interno del servidor';
        }

        // Errores de la API
        return error.message || PortalConfig.MESSAGES.OPERATION_ERROR;
    }
}

/**
 * Servicio para gestión de componentes
 */
class ComponentesApiService {
    constructor(apiClient) {
        this.api = apiClient;
        this.endpoints = PortalConfig.API_ENDPOINTS.COMPONENTES;
    }

    /**
     * Obtener todos los componentes
     */
    async getAll() {
        const response = await this.api.get(this.endpoints.BASE);
        return ApiResponseHandler.handleResponse(response);
    }

    /**
     * Obtener componente por ID
     */
    async getById(id) {
        const response = await this.api.get(this.endpoints.BY_ID(id));
        return ApiResponseHandler.handleResponse(response);
    }

    /**
     * Obtener componentes por tipo
     */
    async getByType(tipo) {
        const response = await this.api.get(this.endpoints.BY_TYPE(tipo));
        return ApiResponseHandler.handleResponse(response);
    }

    /**
     * Crear nuevo componente
     */
    async create(componenteData) {
        const response = await this.api.post(this.endpoints.BASE, componenteData);
        return ApiResponseHandler.handleResponse(response);
    }

    /**
     * Actualizar componente
     */
    async update(id, componenteData) {
        const response = await this.api.put(this.endpoints.BY_ID(id), componenteData);
        return ApiResponseHandler.handleResponse(response);
    }

    /**
     * Eliminar componente
     */
    async delete(id) {
        const response = await this.api.delete(this.endpoints.BY_ID(id));
        return ApiResponseHandler.handleResponse(response);
    }

    /**
     * Verificar si existe un componente
     */
    async exists(id) {
        const response = await this.api.get(this.endpoints.EXISTS(id));
        return ApiResponseHandler.handleResponse(response);
    }
}

/**
 * Servicio para gestión de PCs
 */
class PcsApiService {
    constructor(apiClient) {
        this.api = apiClient;
        this.endpoints = PortalConfig.API_ENDPOINTS.PCS;
    }

    async getAll() {
        const response = await this.api.get(this.endpoints.BASE);
        return ApiResponseHandler.handleResponse(response);
    }

    async getById(id) {
        const response = await this.api.get(this.endpoints.BY_ID(id));
        return ApiResponseHandler.handleResponse(response);
    }

    async create(pcData) {
        const response = await this.api.post(this.endpoints.BASE, pcData);
        return ApiResponseHandler.handleResponse(response);
    }

    async update(id, pcData) {
        const response = await this.api.put(this.endpoints.BY_ID(id), pcData);
        return ApiResponseHandler.handleResponse(response);
    }

    async delete(id) {
        const response = await this.api.delete(this.endpoints.BY_ID(id));
        return ApiResponseHandler.handleResponse(response);
    }

    async getComponents(pcId) {
        const response = await this.api.get(this.endpoints.COMPONENTS(pcId));
        return ApiResponseHandler.handleResponse(response);
    }

    async addComponent(pcId, componentData) {
        const response = await this.api.post(this.endpoints.ADD_COMPONENT(pcId), componentData);
        return ApiResponseHandler.handleResponse(response);
    }

    async removeComponent(pcId, componenteId) {
        const response = await this.api.delete(this.endpoints.REMOVE_COMPONENT(pcId, componenteId));
        return ApiResponseHandler.handleResponse(response);
    }
}

/**
 * Servicio para gestión de cotizaciones
 */
class CotizacionesApiService {
    constructor(apiClient) {
        this.api = apiClient;
        this.endpoints = PortalConfig.API_ENDPOINTS.COTIZACIONES;
    }

    async getAll() {
        const response = await this.api.get(this.endpoints.BASE);
        return ApiResponseHandler.handleResponse(response);
    }

    async getById(id) {
        const response = await this.api.get(this.endpoints.BY_ID(id));
        return ApiResponseHandler.handleResponse(response);
    }

    async create(cotizacionData) {
        const response = await this.api.post(this.endpoints.BASE, cotizacionData);
        return ApiResponseHandler.handleResponse(response);
    }

    async getByDate(fecha) {
        const response = await this.api.get(this.endpoints.BY_DATE, { fecha });
        return ApiResponseHandler.handleResponse(response);
    }
}

/**
 * Servicio para gestión de proveedores
 */
class ProveedoresApiService {
    constructor(apiClient) {
        this.api = apiClient;
        this.endpoints = PortalConfig.API_ENDPOINTS.PROVEEDORES;
    }

    async getAll() {
        const response = await this.api.get(this.endpoints.BASE);
        return ApiResponseHandler.handleResponse(response);
    }

    async getByCve(cve) {
        const response = await this.api.get(this.endpoints.BY_CVE(cve));
        return ApiResponseHandler.handleResponse(response);
    }

    async create(proveedorData) {
        const response = await this.api.post(this.endpoints.BASE, proveedorData);
        return ApiResponseHandler.handleResponse(response);
    }

    async update(cve, proveedorData) {
        const response = await this.api.put(this.endpoints.BY_CVE(cve), proveedorData);
        return ApiResponseHandler.handleResponse(response);
    }

    async delete(cve) {
        const response = await this.api.delete(this.endpoints.BY_CVE(cve));
        return ApiResponseHandler.handleResponse(response);
    }

    async searchByName(nombre) {
        const response = await this.api.get(this.endpoints.BY_NAME, { nombre });
        return ApiResponseHandler.handleResponse(response);
    }

    async searchByRazon(razonSocial) {
        const response = await this.api.get(this.endpoints.BY_RAZON, { razonSocial });
        return ApiResponseHandler.handleResponse(response);
    }
}

/**
 * Servicio para gestión de pedidos
 */
class PedidosApiService {
    constructor(apiClient) {
        this.api = apiClient;
        this.endpoints = PortalConfig.API_ENDPOINTS.PEDIDOS;
    }

    async getAll() {
        const response = await this.api.get(this.endpoints.BASE);
        return ApiResponseHandler.handleResponse(response);
    }

    async getById(id) {
        const response = await this.api.get(this.endpoints.BY_ID(id));
        return ApiResponseHandler.handleResponse(response);
    }

    async generate(pedidoData) {
        const response = await this.api.post(this.endpoints.GENERATE, pedidoData);
        return ApiResponseHandler.handleResponse(response);
    }
}

/**
 * Servicio para gestión de promociones
 */
class PromocionesApiService {
    constructor(apiClient) {
        this.api = apiClient;
        this.endpoints = PortalConfig.API_ENDPOINTS.PROMOCIONES;
    }

    async getAll() {
        const response = await this.api.get(this.endpoints.BASE);
        return ApiResponseHandler.handleResponse(response);
    }

    async getById(id) {
        const response = await this.api.get(this.endpoints.BY_ID(id));
        return ApiResponseHandler.handleResponse(response);
    }

    async create(promocionData) {
        const response = await this.api.post(this.endpoints.BASE, promocionData);
        return ApiResponseHandler.handleResponse(response);
    }

    async update(id, promocionData) {
        const response = await this.api.put(this.endpoints.BY_ID(id), promocionData);
        return ApiResponseHandler.handleResponse(response);
    }

    async delete(id) {
        const response = await this.api.delete(this.endpoints.BY_ID(id));
        return ApiResponseHandler.handleResponse(response);
    }
}

/**
 * Cliente API principal - Singleton
 */
class PortalApiClient {
    constructor() {
        if (PortalApiClient.instance) {
            return PortalApiClient.instance;
        }

        this.client = new ApiClient();
        
        // Inicializar servicios
        this.componentes = new ComponentesApiService(this.client);
        this.pcs = new PcsApiService(this.client);
        this.cotizaciones = new CotizacionesApiService(this.client);
        this.proveedores = new ProveedoresApiService(this.client);
        this.pedidos = new PedidosApiService(this.client);
        this.promociones = new PromocionesApiService(this.client);

        PortalApiClient.instance = this;
    }

    /**
     * Método helper para manejar errores de manera consistente
     */
    handleError(error) {
        return ApiResponseHandler.handleError(error);
    }

    /**
     * Método para probar la conexión y autenticación con la API
     */
    async testConnection() {
        try {
            const response = await this.client.get('/componentes');
            return {
                success: true,
                authenticated: true,
                message: 'Conexión y autenticación exitosa'
            };
        } catch (error) {
            if (error.message.includes('401') || error.message.includes('403')) {
                return {
                    success: false,
                    authenticated: false,
                    message: 'Error de autenticación: ' + this.handleError(error)
                };
            }
            return {
                success: false,
                authenticated: true,
                message: 'Error de conexión: ' + this.handleError(error)
            };
        }
    }
}

// Crear instancia global
window.PortalApi = new PortalApiClient(); 