import BaseApiClient from './baseApiClient'
import { API_CONFIG } from '@/utils/constants'

/**
 * Cliente API específico para PCs del microservicio de Componentes
 * Maneja todas las operaciones relacionadas con PCs
 */
class PcsApiClient extends BaseApiClient {
  constructor() {
    super(API_CONFIG.COMPONENTES_BASE_URL + '/pcs')
  }
}

// Crear instancia singleton
const pcsApiClient = new PcsApiClient()

export default pcsApiClient
export { PcsApiClient }