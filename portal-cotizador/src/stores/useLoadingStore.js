import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

/**
 * Store global para manejar estados de carga en toda la aplicación
 */
export const useLoadingStore = defineStore('loading', () => {
  // Map para rastrear operaciones activas
  const activeOperations = ref(new Map())
  
  // Estado global de carga
  const isGloballyLoading = computed(() => activeOperations.value.size > 0)
  
  // Obtener todas las operaciones activas
  const getActiveOperations = computed(() => Array.from(activeOperations.value.entries()))
  
  /**
   * Iniciar una operación de carga
   * @param {string} key - Identificador único de la operación
   * @param {Object} config - Configuración de la operación
   * @param {string} config.message - Mensaje a mostrar
   * @param {number} config.progress - Progreso de 0 a 100
   * @param {boolean} config.cancellable - Si la operación se puede cancelar
   * @param {string} config.type - Tipo de operación ('global', 'modal', 'button', 'table')
   * @param {boolean} config.blockUI - Si debe bloquear la interfaz
   */
  const startOperation = (key, config = {}) => {
    const operation = {
      key,
      message: config.message || 'Cargando...',
      progress: config.progress || 0,
      cancellable: config.cancellable || false,
      type: config.type || 'default',
      blockUI: config.blockUI || false,
      startTime: Date.now(),
      timeout: config.timeout || null
    }
    
    activeOperations.value.set(key, operation)
    
    // Si tiene timeout, programar la finalización automática
    if (operation.timeout) {
      setTimeout(() => {
        endOperation(key)
      }, operation.timeout)
    }
    
    return operation
  }
  
  /**
   * Actualizar el progreso de una operación
   * @param {string} key - Identificador de la operación
   * @param {number} progress - Nuevo progreso (0-100)
   * @param {string} message - Nuevo mensaje (opcional)
   */
  const updateProgress = (key, progress, message = null) => {
    const operation = activeOperations.value.get(key)
    if (operation) {
      operation.progress = Math.max(0, Math.min(100, progress))
      if (message) {
        operation.message = message
      }
      // Trigger reactivity
      activeOperations.value.set(key, { ...operation })
    }
  }
  
  /**
   * Finalizar una operación de carga
   * @param {string} key - Identificador de la operación
   */
  const endOperation = (key) => {
    activeOperations.value.delete(key)
  }
  
  /**
   * Verificar si una operación específica está activa
   * @param {string} key - Identificador de la operación
   */
  const isOperationActive = (key) => {
    return activeOperations.value.has(key)
  }
  
  /**
   * Obtener información de una operación específica
   * @param {string} key - Identificador de la operación
   */
  const getOperation = (key) => {
    return activeOperations.value.get(key) || null
  }
  
  /**
   * Cancelar todas las operaciones activas
   */
  const cancelAllOperations = () => {
    activeOperations.value.clear()
  }
  
  /**
   * Obtener operaciones por tipo
   * @param {string} type - Tipo de operación
   */
  const getOperationsByType = (type) => {
    return Array.from(activeOperations.value.values()).filter(op => op.type === type)
  }
  
  /**
   * Verificar si hay operaciones que bloquean la UI
   */
  const hasBlockingOperations = computed(() => {
    return Array.from(activeOperations.value.values()).some(op => op.blockUI)
  })
  
  return {
    // Estado
    activeOperations,
    isGloballyLoading,
    hasBlockingOperations,
    
    // Computed
    getActiveOperations,
    
    // Métodos
    startOperation,
    updateProgress,
    endOperation,
    isOperationActive,
    getOperation,
    cancelAllOperations,
    getOperationsByType
  }
})