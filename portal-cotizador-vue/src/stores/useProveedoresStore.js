import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import { proveedoresApi } from '@/services/proveedoresApi'
import { useUtils } from '@/composables/useUtils'
import { useAuthStore } from '@/stores/useAuthStore'
import { 
  UI_CONFIG, 
  MESSAGES, 
  DEBUG_CONFIG 
} from '@/utils/constants'

/**
 * Store de Proveedores - Migración completa del ProveedoresManager
 * Incluye gestión de proveedores, búsquedas específicas, y vistas desktop/móvil
 */
export const useProveedoresStore = defineStore('proveedores', () => {
  const { showAlert, validateRequired, confirm } = useUtils()

  // ==========================================
  // ESTADO REACTIVO (migración del constructor)
  // ==========================================
  
  const proveedores = ref([])
  const filteredProveedores = ref([])
  const currentProveedor = ref(null)
  const loading = ref(false)
  const tableLoading = ref(false)
  const isEditMode = ref(false)
  const isViewMode = ref(false)
  
  // Configuración de paginación (migración exacta)
  const pagination = ref({
    currentPage: 1,
    pageSize: UI_CONFIG.DEFAULT_PAGE_SIZE,
    totalPages: 0,
    totalItems: 0
  })
  
  // Filtros de búsqueda
  const filters = ref({
    searchTerm: '',
    searchType: 'general' // general, nombre, razon
  })

  // Estado de UI para el modal
  const showModal = ref(false)
  const modalLoading = ref(false)

  // Estado del formulario
  const formData = ref({
    cve: '',
    nombre: '',
    razonSocial: ''
  })

  // ==========================================
  // COMPUTED PROPERTIES (reactividad automática)
  // ==========================================
  
  const hasProveedores = computed(() => proveedores.value.length > 0)
  
  const hasFilteredProveedores = computed(() => filteredProveedores.value.length > 0)
  
  const paginatedProveedores = computed(() => {
    const startIndex = (pagination.value.currentPage - 1) * pagination.value.pageSize
    const endIndex = startIndex + pagination.value.pageSize
    return filteredProveedores.value.slice(startIndex, endIndex)
  })
  
  const paginationInfo = computed(() => {
    const { currentPage, pageSize, totalItems } = pagination.value
    const startItem = (currentPage - 1) * pageSize + 1
    const endItem = Math.min(currentPage * pageSize, totalItems)
    
    return {
      startItem,
      endItem,
      totalItems,
      text: `Mostrando ${startItem} a ${endItem} de ${totalItems} resultados`
    }
  })
  
  const canGoPrevious = computed(() => pagination.value.currentPage > 1)
  const canGoNext = computed(() => pagination.value.currentPage < pagination.value.totalPages)

  // Computed para validación del formulario
  const isFormValid = computed(() => {
    if (isViewMode.value) return true
    
    return formData.value.cve &&
           formData.value.nombre &&
           formData.value.razonSocial
  })

  // Computed para el título del modal
  const modalTitle = computed(() => {
    if (isViewMode.value) return 'Ver Proveedor'
    if (isEditMode.value) return 'Editar Proveedor'
    return 'Nuevo Proveedor'
  })

  // ==========================================
  // ACTIONS - CRUD OPERATIONS
  // ==========================================
  
  /**
   * Cargar todos los proveedores (migración de loadProveedores)
   */
  const fetchProveedores = async () => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ProveedoresStore] Fetching proveedores...')
    }
    
    try {
      loading.value = true
      tableLoading.value = true
      
      const data = await proveedoresApi.getAll()
      proveedores.value = data || []
      
      // Aplicar filtros existentes
      await applyFilters()
      
      if (DEBUG_CONFIG.ENABLED) {
        console.log(`[ProveedoresStore] Loaded ${proveedores.value.length} proveedores`)
      }
      
    } catch (error) {
      console.error('[ProveedoresStore] Error fetching proveedores:', error)
      showAlert('error', error.message || MESSAGES.NETWORK_ERROR)
      proveedores.value = []
      filteredProveedores.value = []
    } finally {
      loading.value = false
      tableLoading.value = false
    }
  }

  /**
   * Crear nuevo proveedor
   */
  const createProveedor = async (proveedorData) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ProveedoresStore] Creating proveedor:', proveedorData)
    }
    
    try {
      loading.value = true
      
      // Validar datos antes de enviar
      const validation = proveedoresApi.validateProveedor(proveedorData)
      if (!validation.isValid) {
        showAlert('error', validation.errors.join('\n'))
        return { success: false, errors: validation.errors }
      }
      
      // Formatear datos
      const formattedData = proveedoresApi.formatProveedorData(proveedorData)
      
      const response = await proveedoresApi.create(formattedData)
      
      showAlert('success', MESSAGES.PROVEEDOR_CREATED)
      
      // Recargar proveedores para obtener datos actualizados
      await fetchProveedores()
      
      return { success: true, data: response }
      
    } catch (error) {
      console.error('[ProveedoresStore] Error creating proveedor:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  /**
   * Actualizar proveedor existente
   */
  const updateProveedor = async (cve, proveedorData) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ProveedoresStore] Updating proveedor:', cve, proveedorData)
    }
    
    try {
      loading.value = true
      
      // Validar datos antes de enviar (sin validar clave en actualización)
      const validation = proveedoresApi.validateProveedor(proveedorData, true)
      if (!validation.isValid) {
        showAlert('error', validation.errors.join('\n'))
        return { success: false, errors: validation.errors }
      }
      
      // Formatear datos (sin incluir clave en actualización)
      const updateData = {
        nombre: proveedorData.nombre?.trim(),
        razonSocial: proveedorData.razonSocial?.trim()
      }
      
      const response = await proveedoresApi.update(cve, updateData)
      
      showAlert('success', MESSAGES.PROVEEDOR_UPDATED)
      
      // Recargar proveedores para obtener datos actualizados
      await fetchProveedores()
      
      return { success: true, data: response }
      
    } catch (error) {
      console.error('[ProveedoresStore] Error updating proveedor:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  /**
   * Eliminar proveedor
   */
  const deleteProveedor = async (cve) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ProveedoresStore] Deleting proveedor:', cve)
    }
    
    try {
      loading.value = true
      
      await proveedoresApi.delete(cve)
      
      showAlert('success', MESSAGES.PROVEEDOR_DELETED)
      
      // Recargar proveedores para obtener datos actualizados
      await fetchProveedores()
      
      return { success: true }
      
    } catch (error) {
      console.error('[ProveedoresStore] Error deleting proveedor:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  // ==========================================
  // ACTIONS - GESTIÓN DE MODALES
  // ==========================================
  
  /**
   * Abrir modal para crear nuevo proveedor (migración de openCreateModal)
   */
  const openCreateModal = () => {
    const authStore = useAuthStore()
    
    // Verificar autenticación directamente
    if (!authStore.isLoggedIn) {
      authStore.openLoginModal()
      return
    }
    
    // Si está autenticado, ejecutar directamente
    // Resetear estado
    isEditMode.value = false
    isViewMode.value = false
    currentProveedor.value = null
    
    // Configurar valores por defecto
    formData.value = {
      cve: '',
      nombre: '',
      razonSocial: ''
    }
    
    showModal.value = true
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ProveedoresStore] Opened create modal')
    }
  }

  /**
   * Abrir modal para ver proveedor existente (migración de viewProveedor)
   */
  const openViewModal = async (cve) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ProveedoresStore] Opening view modal for proveedor:', cve)
    }
    
    try {
      modalLoading.value = true
      
      // Obtener información del proveedor
      const proveedor = await proveedoresApi.getByCve(cve)
      currentProveedor.value = proveedor
      
      // Configurar modo vista
      isEditMode.value = false
      isViewMode.value = true
      
      // Configurar formulario con datos del proveedor
      formData.value = {
        cve: proveedor.cve,
        nombre: proveedor.nombre,
        razonSocial: proveedor.razonSocial
      }
      
      showModal.value = true
      
    } catch (error) {
      console.error('[ProveedoresStore] Error opening view modal:', error)
      showAlert('error', error.message || MESSAGES.OPERATION_ERROR)
    } finally {
      modalLoading.value = false
    }
  }

  /**
   * Abrir modal para editar proveedor existente (migración de editProveedor)
   */
  const openEditModal = async (cve) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ProveedoresStore] Opening edit modal for proveedor:', cve)
    }
    
    try {
      modalLoading.value = true
      
      // Obtener información del proveedor
      const proveedor = await proveedoresApi.getByCve(cve)
      currentProveedor.value = proveedor
      
      // Configurar modo edición
      isEditMode.value = true
      isViewMode.value = false
      
      // Configurar formulario con datos del proveedor
      formData.value = {
        cve: proveedor.cve,
        nombre: proveedor.nombre,
        razonSocial: proveedor.razonSocial
      }
      
      showModal.value = true
      
    } catch (error) {
      console.error('[ProveedoresStore] Error opening edit modal:', error)
      showAlert('error', error.message || MESSAGES.OPERATION_ERROR)
    } finally {
      modalLoading.value = false
    }
  }

  /**
   * Cerrar modal
   */
  const closeModal = () => {
    showModal.value = false
    currentProveedor.value = null
    isEditMode.value = false
    isViewMode.value = false
    modalLoading.value = false
    
    // Resetear formulario
    formData.value = {
      cve: '',
      nombre: '',
      razonSocial: ''
    }
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ProveedoresStore] Closed modal')
    }
  }

  // ==========================================
  // ACTIONS - ENVÍO DEL FORMULARIO
  // ==========================================
  
  /**
   * Enviar formulario de proveedor (migración de handleSubmitProveedor)
   */
  const submitProveedor = async () => {
    if (isViewMode.value) {
      closeModal()
      return { success: true }
    }

    if (!isFormValid.value) {
      showAlert('error', 'Complete todos los campos requeridos')
      return { success: false }
    }

    try {
      modalLoading.value = true
      
      let result
      
      if (isEditMode.value && currentProveedor.value) {
        // Actualizar proveedor existente
        result = await updateProveedor(currentProveedor.value.cve, formData.value)
      } else {
        // Crear nuevo proveedor
        result = await createProveedor(formData.value)
      }
      
      if (result.success) {
        closeModal()
      }
      
      return result
      
    } catch (error) {
      console.error('[ProveedoresStore] Error submitting proveedor:', error)
      return { success: false, error: error.message }
    } finally {
      modalLoading.value = false
    }
  }

  /**
   * Confirmar y eliminar proveedor
   */
  const confirmDeleteProveedor = async (cve) => {
    const proveedor = proveedores.value.find(p => p.cve === cve)
    if (!proveedor) return { success: false }

    const confirmed = await confirm(
      'Eliminar Proveedor',
      `¿Está seguro de que desea eliminar el proveedor "${proveedor.nombre}"?\n\nEsta acción no se puede deshacer.`
    )

    if (!confirmed) {
      return { success: false }
    }

    return await deleteProveedor(cve)
  }

  // ==========================================
  // ACTIONS - FILTROS Y BÚSQUEDA
  // ==========================================
  
  /**
   * Aplicar filtros (migración de applyFilters)
   */
  const applyFilters = async () => {
    const searchTerm = filters.value.searchTerm.toLowerCase().trim()
    const searchType = filters.value.searchType
    
    if (!searchTerm) {
      // Sin búsqueda, mostrar todos
      filteredProveedores.value = [...proveedores.value]
    } else {
      try {
        loading.value = true
        
        // Usar búsqueda específica según tipo
        filteredProveedores.value = await proveedoresApi.searchByType(
          searchTerm, 
          searchType, 
          proveedores.value
        )
        
      } catch (error) {
        console.error('[ProveedoresStore] Error in search:', error)
        showAlert('error', error.message || MESSAGES.OPERATION_ERROR)
        filteredProveedores.value = []
      } finally {
        loading.value = false
      }
    }

    // Reiniciar paginación al aplicar filtros
    pagination.value.currentPage = 1
    updatePaginationInfo()
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log(`[ProveedoresStore] Applied filters: ${filteredProveedores.value.length} results`)
    }
  }

  /**
   * Limpiar filtros (migración de clearFilters)
   */
  const clearFilters = () => {
    filters.value.searchTerm = ''
    filters.value.searchType = 'general'
    filteredProveedores.value = [...proveedores.value]
    
    // Reiniciar paginación al limpiar filtros
    pagination.value.currentPage = 1
    updatePaginationInfo()
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ProveedoresStore] Filters cleared')
    }
  }

  /**
   * Establecer filtro de búsqueda
   */
  const setSearchFilter = (searchTerm) => {
    filters.value.searchTerm = searchTerm
    // El filtrado se dispara automáticamente desde el watcher en la vista
  }

  /**
   * Establecer tipo de búsqueda
   */
  const setSearchType = (searchType) => {
    filters.value.searchType = searchType
    applyFilters()
  }

  // ==========================================
  // ACTIONS - PAGINACIÓN
  // ==========================================
  
  /**
   * Actualizar información de paginación (migración de updatePaginationInfo)
   */
  const updatePaginationInfo = () => {
    pagination.value.totalItems = filteredProveedores.value.length
    pagination.value.totalPages = Math.ceil(pagination.value.totalItems / pagination.value.pageSize)
    
    // Ajustar página actual si es necesario
    if (pagination.value.currentPage > pagination.value.totalPages) {
      pagination.value.currentPage = Math.max(1, pagination.value.totalPages)
    }
  }

  /**
   * Ir a página específica
   */
  const goToPage = (pageNumber) => {
    if (pageNumber >= 1 && pageNumber <= pagination.value.totalPages) {
      pagination.value.currentPage = pageNumber
      
      if (DEBUG_CONFIG.ENABLED) {
        console.log(`[ProveedoresStore] Navigated to page ${pageNumber}`)
      }
    }
  }

  /**
   * Cambiar tamaño de página
   */
  const setPageSize = (newPageSize) => {
    pagination.value.pageSize = newPageSize
    pagination.value.currentPage = 1
    updatePaginationInfo()
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log(`[ProveedoresStore] Page size changed to ${newPageSize}`)
    }
  }

  /**
   * Ir a página anterior
   */
  const goToPreviousPage = () => {
    if (canGoPrevious.value) {
      goToPage(pagination.value.currentPage - 1)
    }
  }

  /**
   * Ir a página siguiente
   */
  const goToNextPage = () => {
    if (canGoNext.value) {
      goToPage(pagination.value.currentPage + 1)
    }
  }

  // ==========================================
  // UTILIDADES Y VALIDACIONES
  // ==========================================
  
  /**
   * Buscar proveedor por clave
   */
  const findProveedorByCve = (cve) => {
    return proveedores.value.find(prov => prov.cve === cve)
  }

  /**
   * Verificar si existe un proveedor con la clave
   */
  const proveedorExists = async (cve) => {
    try {
      return await proveedoresApi.exists(cve)
    } catch (error) {
      console.error('[ProveedoresStore] Error checking if proveedor exists:', error)
      return false
    }
  }

  /**
   * Obtener resumen de proveedor
   */
  const getProveedorSummary = (proveedor) => {
    return proveedoresApi.getProveedorSummary(proveedor)
  }

  /**
   * Validar formulario actual
   */
  const validateCurrentForm = () => {
    return proveedoresApi.validateProveedor(formData.value, isEditMode.value)
  }

  // ==========================================
  // RETURN (API PÚBLICA DEL STORE)
  // ==========================================
  
  return {
    // Estado reactivo (readonly para prevenir mutaciones directas)
    proveedores: readonly(proveedores),
    filteredProveedores: readonly(filteredProveedores),
    currentProveedor: readonly(currentProveedor),
    loading: readonly(loading),
    tableLoading: readonly(tableLoading),
    modalLoading: readonly(modalLoading),
    isEditMode: readonly(isEditMode),
    isViewMode: readonly(isViewMode),
    showModal,
    formData,
    pagination: readonly(pagination),
    filters: readonly(filters),
    
    // Computed properties
    hasProveedores,
    hasFilteredProveedores,
    paginatedProveedores,
    paginationInfo,
    canGoPrevious,
    canGoNext,
    isFormValid,
    modalTitle,
    
    // Actions - CRUD
    fetchProveedores,
    createProveedor,
    updateProveedor,
    deleteProveedor,
    confirmDeleteProveedor,
    
    // Actions - Modales
    openCreateModal,
    openViewModal,
    openEditModal,
    closeModal,
    
    // Actions - Formulario
    submitProveedor,
    
    // Actions - Filtros
    applyFilters,
    clearFilters,
    setSearchFilter,
    setSearchType,
    
    // Actions - Paginación
    goToPage,
    setPageSize,
    goToPreviousPage,
    goToNextPage,
    updatePaginationInfo,
    
    // Utilidades
    findProveedorByCve,
    proveedorExists,
    getProveedorSummary,
    validateCurrentForm
  }
})