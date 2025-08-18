import BaseApiClient from './baseApiClient'
import { API_CONFIG } from '@/utils/constants'

/**
 * Cliente API específico para el microservicio de Seguridad
 * Maneja todas las operaciones relacionadas con autenticación, usuarios y roles
 */
class SeguridadApiClient extends BaseApiClient {
  constructor() {
    super(API_CONFIG.SEGURIDAD_BASE_URL)
  }
}

// Crear instancia singleton
const seguridadApiClient = new SeguridadApiClient()

export default seguridadApiClient
export { SeguridadApiClient }