import BaseApiClient from './baseApiClient'
import { API_CONFIG } from '@/utils/constants'

/**
 * Cliente API específico para el microservicio de Cotizaciones
 * Maneja todas las operaciones relacionadas con cotizaciones e impuestos
 */
class CotizacionesApiClient extends BaseApiClient {
  constructor() {
    super(API_CONFIG.COTIZACIONES_BASE_URL + '/cotizaciones')
  }
}

// Crear instancia singleton
const cotizacionesApiClient = new CotizacionesApiClient()

export default cotizacionesApiClient
export { CotizacionesApiClient }