import componentesApiClient from './componentesApiClient'
import { API_ENDPOINTS } from '@/utils/constants'

/**
 * API service para componentes usando el microservicio específico de componentes
 * Migrado para usar ms-cotizador-componentes via gateway
 */
export const componentesApi = {
  /**
   * Obtener todos los componentes
   */
  async getAll() {
    const response = await componentesApiClient.get(API_ENDPOINTS.COMPONENTES.BASE)
    return response.datos || []
  },

  /**
   * Obtener componente por ID
   */
  async getById(id) {
    const response = await componentesApiClient.get(API_ENDPOINTS.COMPONENTES.BY_ID(id))
    return response.datos
  },

  /**
   * Crear nuevo componente
   */
  async create(componenteData) {
    const response = await componentesApiClient.post(API_ENDPOINTS.COMPONENTES.BASE, componenteData)
    return response
  },

  /**
   * Actualizar componente existente
   */
  async update(id, componenteData) {
    const response = await componentesApiClient.put(API_ENDPOINTS.COMPONENTES.BY_ID(id), componenteData)
    return response
  },

  /**
   * Eliminar componente
   */
  async delete(id) {
    const response = await componentesApiClient.delete(API_ENDPOINTS.COMPONENTES.BY_ID(id))
    return response
  },

  /**
   * Obtener componentes por tipo
   */
  async getByType(tipo) {
    const response = await componentesApiClient.get(API_ENDPOINTS.COMPONENTES.BY_TYPE(tipo))
    return response.datos || []
  },

  /**
   * Verificar si existe un componente con el ID
   */
  async exists(id) {
    try {
      const response = await componentesApiClient.get(API_ENDPOINTS.COMPONENTES.EXISTS(id))
      return response.datos || false
    } catch (error) {
      // Si da 404, significa que no existe
      if (error.message.includes('404') || error.message.includes('no encontrado')) {
        return false
      }
      throw error
    }
  }
}

export default componentesApi