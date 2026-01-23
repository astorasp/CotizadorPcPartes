import BaseApiClient from './baseApiClient'
import { API_CONFIG } from '@/utils/constants'

/**
 * Cliente API específico para el microservicio de Componentes
 * Maneja todas las operaciones relacionadas con componentes, PCs y promociones
 */
class ComponentesApiClient extends BaseApiClient {
  constructor() {
    super(API_CONFIG.COMPONENTES_BASE_URL + '/componentes')
  }
}

// Crear instancia singleton
const componentesApiClient = new ComponentesApiClient()

export default componentesApiClient
export { ComponentesApiClient }