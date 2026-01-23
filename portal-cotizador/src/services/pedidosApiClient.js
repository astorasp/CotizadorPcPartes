import BaseApiClient from './baseApiClient'
import { API_CONFIG } from '@/utils/constants'

/**
 * Cliente API específico para el microservicio de Pedidos
 * Maneja todas las operaciones relacionadas con pedidos y proveedores
 */
class PedidosApiClient extends BaseApiClient {
  constructor() {
    super(API_CONFIG.PEDIDOS_BASE_URL + '/pedidos')
  }
}

// Crear instancia singleton
const pedidosApiClient = new PedidosApiClient()

export default pedidosApiClient
export { PedidosApiClient }