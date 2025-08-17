import axios from 'axios'

const USUARIOS_BASE_URL = (import.meta.env.VITE_SEGURIDAD_API_BASE_URL || '/api/seguridad') + '/usuarios'
const ROLES_BASE_URL = (import.meta.env.VITE_SEGURIDAD_API_BASE_URL || '/api/seguridad') + '/roles'

/**
 * API para gestión de usuarios del microservicio de seguridad
 */
export const usuariosApi = {
  
  // =================================================================
  // GESTIÓN DE USUARIOS
  // =================================================================
  
  /**
   * Obtener lista paginada de usuarios
   * @param {Object} params - Parámetros de paginación
   * @param {number} params.page - Página (0-indexed)
   * @param {number} params.size - Tamaño de página
   * @param {string} params.sort - Campo para ordenar
   * @param {string} params.direction - Dirección del ordenamiento (ASC/DESC)
   * @returns {Promise} Lista paginada de usuarios
   */
  async obtenerUsuarios(params = {}) {
    try {
      const queryParams = new URLSearchParams({
        page: params.page || 0,
        size: params.size || 10,
        sort: params.sort || 'id',
        direction: params.direction || 'ASC'
      })
      
      const response = await axios.get(`${USUARIOS_BASE_URL}?${queryParams}`)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error al obtener usuarios:', error)
      return {
        success: false,
        error: error.response?.data?.message || 'Error al obtener usuarios'
      }
    }
  },

  /**
   * Obtener usuario por ID
   * @param {number} id - ID del usuario
   * @returns {Promise} Datos del usuario
   */
  async obtenerUsuarioPorId(id) {
    try {
      const response = await axios.get(`${USUARIOS_BASE_URL}/${id}`)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error al obtener usuario:', error)
      return {
        success: false,
        error: error.response?.data?.message || 'Usuario no encontrado'
      }
    }
  },

  /**
   * Crear nuevo usuario
   * @param {Object} usuario - Datos del usuario
   * @param {string} usuario.usuario - Nombre de usuario
   * @param {string} usuario.password - Contraseña
   * @param {boolean} usuario.activo - Estado del usuario
   * @returns {Promise} Usuario creado
   */
  async crearUsuario(usuario) {
    try {
      const response = await axios.post(USUARIOS_BASE_URL, {
        usuario: usuario.usuario,
        password: usuario.password,
        activo: usuario.activo !== undefined ? usuario.activo : true
      })
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error al crear usuario:', error)
      return {
        success: false,
        error: error.response?.data?.message || 'Error al crear usuario'
      }
    }
  },

  /**
   * Actualizar usuario existente
   * @param {number} id - ID del usuario
   * @param {Object} usuario - Datos actualizados
   * @returns {Promise} Usuario actualizado
   */
  async actualizarUsuario(id, usuario) {
    try {
      const response = await axios.put(`${USUARIOS_BASE_URL}/${id}`, usuario)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error al actualizar usuario:', error)
      return {
        success: false,
        error: error.response?.data?.message || 'Error al actualizar usuario'
      }
    }
  },

  /**
   * Desactivar usuario (soft delete)
   * @param {number} id - ID del usuario
   * @returns {Promise} Resultado de la operación
   */
  async desactivarUsuario(id) {
    try {
      const response = await axios.delete(`${USUARIOS_BASE_URL}/${id}`)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error al desactivar usuario:', error)
      return {
        success: false,
        error: error.response?.data?.message || 'Error al desactivar usuario'
      }
    }
  },

  /**
   * Obtener estadísticas de usuarios
   * @returns {Promise} Estadísticas de usuarios
   */
  async obtenerEstadisticas() {
    try {
      const response = await axios.get(`${USUARIOS_BASE_URL}/stats`)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error al obtener estadísticas:', error)
      return {
        success: false,
        error: error.response?.data?.message || 'Error al obtener estadísticas'
      }
    }
  },

  // =================================================================
  // GESTIÓN DE ROLES
  // =================================================================

  /**
   * Obtener lista de roles disponibles
   * @returns {Promise} Lista de roles
   */
  async obtenerRoles() {
    try {
      const response = await axios.get(ROLES_BASE_URL)
      return {
        success: true,
        data: response.data.roles || response.data
      }
    } catch (error) {
      console.error('Error al obtener roles:', error)
      return {
        success: false,
        error: error.response?.data?.message || 'Error al obtener roles'
      }
    }
  },

  /**
   * Obtener roles de un usuario específico
   * @param {number} usuarioId - ID del usuario
   * @returns {Promise} Roles del usuario
   */
  async obtenerRolesUsuario(usuarioId) {
    try {
      const response = await axios.get(`${ROLES_BASE_URL}/usuario/${usuarioId}`)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error al obtener roles del usuario:', error)
      return {
        success: false,
        error: error.response?.data?.message || 'Error al obtener roles del usuario'
      }
    }
  },

  /**
   * Asignar rol a usuario
   * @param {number} usuarioId - ID del usuario
   * @param {number} rolId - ID del rol
   * @returns {Promise} Resultado de la asignación
   */
  async asignarRol(usuarioId, rolId) {
    try {
      const response = await axios.post(`${ROLES_BASE_URL}/usuario/${usuarioId}/rol/${rolId}`)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error al asignar rol:', error)
      return {
        success: false,
        error: error.response?.data?.message || 'Error al asignar rol'
      }
    }
  },

  /**
   * Revocar rol de usuario
   * @param {number} usuarioId - ID del usuario
   * @param {number} rolId - ID del rol
   * @returns {Promise} Resultado de la revocación
   */
  async revocarRol(usuarioId, rolId) {
    try {
      const response = await axios.delete(`${ROLES_BASE_URL}/usuario/${usuarioId}/rol/${rolId}`)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error al revocar rol:', error)
      return {
        success: false,
        error: error.response?.data?.message || 'Error al revocar rol'
      }
    }
  },

  /**
   * Obtener estadísticas de roles
   * @returns {Promise} Estadísticas de roles
   */
  async obtenerEstadisticasRoles() {
    try {
      const response = await axios.get(`${ROLES_BASE_URL}/stats`)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error al obtener estadísticas de roles:', error)
      return {
        success: false,
        error: error.response?.data?.message || 'Error al obtener estadísticas de roles'
      }
    }
  }
}

export default usuariosApi