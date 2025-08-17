import { ref } from 'vue'
import { useLoadingStore } from '@/stores/useLoadingStore'

/**
 * Composable para manejar operaciones asíncronas con loading states automático
 * @param {Object} defaultConfig - Configuración por defecto para las operaciones
 */
export const useAsyncOperation = (defaultConfig = {}) => {
  const loadingStore = useLoadingStore()
  
  // Estado local de la última operación
  const lastResult = ref(null)
  const lastError = ref(null)
  
  /**
   * Ejecutar una operación asíncrona con manejo automático de loading
   * @param {Function} operation - Función asíncrona a ejecutar
   * @param {Object} config - Configuración específica de la operación
   * @param {string} config.key - Identificador único (se genera automáticamente si no se proporciona)
   * @param {string} config.message - Mensaje de carga
   * @param {string} config.successMessage - Mensaje de éxito
   * @param {string} config.errorMessage - Mensaje de error
   * @param {boolean} config.blockUI - Si debe bloquear la interfaz
   * @param {string} config.type - Tipo de operación
   * @param {number} config.timeout - Timeout en milisegundos
   * @param {Function} config.onProgress - Callback para actualizaciones de progreso
   * @param {Function} config.onSuccess - Callback de éxito
   * @param {Function} config.onError - Callback de error
   * @param {boolean} config.showNotification - Si mostrar notificaciones automáticas
   */
  const execute = async (operation, config = {}) => {
    // Combinar configuración por defecto con la específica
    const finalConfig = { ...defaultConfig, ...config }
    
    // Generar key único si no se proporciona
    const operationKey = finalConfig.key || `operation-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    
    // Limpiar estados previos
    lastResult.value = null
    lastError.value = null
    
    try {
      // Iniciar la operación en el store
      loadingStore.startOperation(operationKey, {
        message: finalConfig.message || 'Procesando...',
        type: finalConfig.type || 'default',
        blockUI: finalConfig.blockUI || false,
        timeout: finalConfig.timeout,
        cancellable: finalConfig.cancellable || false
      })
      
      // Configurar callback de progreso si existe
      let progressCallback = null
      if (finalConfig.onProgress) {
        progressCallback = (progress, message) => {
          loadingStore.updateProgress(operationKey, progress, message)
          finalConfig.onProgress(progress, message)
        }
      }
      
      // Ejecutar la operación
      const result = await operation(progressCallback)
      
      // Guardar resultado
      lastResult.value = result
      
      // Ejecutar callback de éxito
      if (finalConfig.onSuccess) {
        finalConfig.onSuccess(result)
      }
      
      // Mostrar notificación de éxito si está habilitada
      if (finalConfig.showNotification && finalConfig.successMessage) {
        // TODO: Integrar con sistema de notificaciones
        // console.log('Success:', finalConfig.successMessage)
      }
      
      return {
        success: true,
        data: result,
        error: null
      }
      
    } catch (error) {
      console.error('Async operation failed:', error)
      
      // Guardar error
      lastError.value = error
      
      // Ejecutar callback de error
      if (finalConfig.onError) {
        finalConfig.onError(error)
      }
      
      // Mostrar notificación de error si está habilitada
      if (finalConfig.showNotification && finalConfig.errorMessage) {
        // TODO: Integrar con sistema de notificaciones
        console.error('Error:', finalConfig.errorMessage)
      }
      
      return {
        success: false,
        data: null,
        error: error.message || 'Error desconocido'
      }
      
    } finally {
      // Finalizar la operación en el store
      loadingStore.endOperation(operationKey)
    }
  }
  
  /**
   * Ejecutar múltiples operaciones en paralelo
   * @param {Array} operations - Array de objetos {operation, config}
   * @param {Object} globalConfig - Configuración global para todas las operaciones
   */
  const executeParallel = async (operations, globalConfig = {}) => {
    const promises = operations.map((op, index) => {
      const config = {
        ...globalConfig,
        ...op.config,
        key: op.config?.key || `parallel-${Date.now()}-${index}`
      }
      return execute(op.operation, config)
    })
    
    try {
      const results = await Promise.allSettled(promises)
      return results.map(result => 
        result.status === 'fulfilled' ? result.value : { success: false, error: result.reason }
      )
    } catch (error) {
      console.error('Parallel operations failed:', error)
      throw error
    }
  }
  
  /**
   * Ejecutar operaciones en secuencia
   * @param {Array} operations - Array de objetos {operation, config}
   * @param {Object} globalConfig - Configuración global
   */
  const executeSequential = async (operations, globalConfig = {}) => {
    const results = []
    
    for (let i = 0; i < operations.length; i++) {
      const op = operations[i]
      const config = {
        ...globalConfig,
        ...op.config,
        key: op.config?.key || `sequential-${Date.now()}-${i}`,
        message: op.config?.message || `Paso ${i + 1} de ${operations.length}...`
      }
      
      const result = await execute(op.operation, config)
      results.push(result)
      
      // Si una operación falla y no se permite continuar, detener
      if (!result.success && !globalConfig.continueOnError) {
        break
      }
    }
    
    return results
  }
  
  /**
   * Crear una versión envuelta de una función que automáticamente maneja loading
   * @param {Function} fn - Función a envolver
   * @param {Object} config - Configuración por defecto
   */
  const wrap = (fn, config = {}) => {
    return async (...args) => {
      return await execute(() => fn(...args), config)
    }
  }
  
  return {
    // Estado
    lastResult,
    lastError,
    
    // Métodos principales
    execute,
    executeParallel,
    executeSequential,
    wrap,
    
    // Acceso al store de loading
    loadingStore
  }
}

/**
 * Hook específico para operaciones CRUD con configuraciones predefinidas
 */
export const useCrudOperations = (entityName = 'elemento') => {
  const asyncOp = useAsyncOperation({
    showNotification: true,
    type: 'crud'
  })
  
  const create = (operation) => asyncOp.execute(operation, {
    key: `create-${entityName}`,
    message: `Creando ${entityName}...`,
    successMessage: `${entityName} creado exitosamente`,
    errorMessage: `Error al crear ${entityName}`
  })
  
  const update = (operation) => asyncOp.execute(operation, {
    key: `update-${entityName}`,
    message: `Actualizando ${entityName}...`,
    successMessage: `${entityName} actualizado exitosamente`,
    errorMessage: `Error al actualizar ${entityName}`
  })
  
  const remove = (operation) => asyncOp.execute(operation, {
    key: `delete-${entityName}`,
    message: `Eliminando ${entityName}...`,
    successMessage: `${entityName} eliminado exitosamente`,
    errorMessage: `Error al eliminar ${entityName}`,
    blockUI: true // Las eliminaciones bloquean la UI por seguridad
  })
  
  const fetch = (operation) => asyncOp.execute(operation, {
    key: `fetch-${entityName}`,
    message: `Cargando ${entityName}s...`,
    errorMessage: `Error al cargar ${entityName}s`,
    type: 'table'
  })
  
  return {
    ...asyncOp,
    create,
    update,
    remove,
    fetch
  }
}