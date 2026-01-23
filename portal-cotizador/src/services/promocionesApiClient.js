import BaseApiClient from './baseApiClient'
import { API_CONFIG } from '@/utils/constants'

/**
 * Cliente API específico para Promociones del microservicio de Componentes
 * Maneja todas las operaciones relacionadas con promociones
 */
class PromocionesApiClient extends BaseApiClient {
  constructor() {
    super(API_CONFIG.COMPONENTES_BASE_URL + '/promociones')
  }
}

// Crear instancia singleton
const promocionesApiClient = new PromocionesApiClient()

export default promocionesApiClient
export { PromocionesApiClient }