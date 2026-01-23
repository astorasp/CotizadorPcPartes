import pcsApiClient from './pcsApiClient'
import { API_ENDPOINTS } from '@/utils/constants'

/**
 * API service para PCs usando el microservicio de componentes
 * Migrado para usar ms-cotizador-componentes via gateway
 */
export const pcsApi = {
  /**
   * Obtener todas las PCs
   */
  async getAll() {
    const response = await pcsApiClient.get(API_ENDPOINTS.PCS.BASE)
    return response.datos || []
  },

  /**
   * Obtener PC por ID
   */
  async getById(id) {
    const response = await pcsApiClient.get(API_ENDPOINTS.PCS.BY_ID(id))
    return response.datos
  },

  /**
   * Crear nueva PC
   */
  async create(pcData) {
    const response = await pcsApiClient.post(API_ENDPOINTS.PCS.BASE, pcData)
    return response
  },

  /**
   * Actualizar PC existente
   */
  async update(id, pcData) {
    const response = await pcsApiClient.put(API_ENDPOINTS.PCS.BY_ID(id), pcData)
    return response
  },

  /**
   * Eliminar PC
   */
  async delete(id) {
    const response = await pcsApiClient.delete(API_ENDPOINTS.PCS.BY_ID(id))
    return response
  },

  /**
   * Obtener componentes de una PC específica
   */
  async getComponents(pcId) {
    const response = await pcsApiClient.get(API_ENDPOINTS.PCS.COMPONENTS(pcId))
    return response.datos || []
  },

  /**
   * Agregar componente a una PC
   */
  async addComponent(pcId, componentData) {
    const response = await pcsApiClient.post(API_ENDPOINTS.PCS.ADD_COMPONENT(pcId), componentData)
    return response
  },

  /**
   * Quitar componente de una PC
   */
  async removeComponent(pcId, componenteId) {
    const response = await pcsApiClient.delete(API_ENDPOINTS.PCS.REMOVE_COMPONENT(pcId, componenteId))
    return response
  },

  /**
   * Verificar si existe una PC con el ID
   */
  async exists(id) {
    try {
      const response = await pcsApiClient.get(API_ENDPOINTS.PCS.BY_ID(id))
      return response.datos !== null
    } catch (error) {
      // Si da 404, significa que no existe
      if (error.message.includes('404') || error.message.includes('no encontrado')) {
        return false
      }
      throw error
    }
  },

  /**
   * Obtener estadísticas de una PC (conteo de componentes, costo total, etc.)
   */
  async getStats(pcId) {
    try {
      const components = await this.getComponents(pcId)
      const pc = await this.getById(pcId)
      
      return {
        componentCount: components.length,
        totalCost: components.reduce((sum, comp) => sum + (comp.costo || 0), 0),
        components: components,
        pc: pc
      }
    } catch (error) {
      console.error('Error getting PC stats:', error)
      throw error
    }
  }
}

export default pcsApi