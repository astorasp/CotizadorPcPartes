import BaseApiClient from './baseApiClient'
import { API_CONFIG } from '@/utils/constants'

/**
 * Cliente API específico para Proveedores del microservicio de Pedidos
 * Maneja todas las operaciones relacionadas con proveedores
 */
class ProveedoresApiClient extends BaseApiClient {
  constructor() {
    super(API_CONFIG.PEDIDOS_BASE_URL + '/pedidos/proveedores')
  }
}

// Crear instancia singleton
const proveedoresApiClient = new ProveedoresApiClient()

export default proveedoresApiClient
export { ProveedoresApiClient }