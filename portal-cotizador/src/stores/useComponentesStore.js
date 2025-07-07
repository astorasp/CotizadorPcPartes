import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import { componentesApi } from '@/services/componentesApi'
import { useUtils } from '@/composables/useUtils'
import { authService } from '@/services/authService'
import { 
  UI_CONFIG, 
  MESSAGES, 
  COMPONENT_TYPES, 
  DEBUG_CONFIG 
} from '@/utils/constants'

/**
 * Store de Componentes - Migración completa del ComponentesManager
 * Mantiene toda la funcionalidad del manager original pero con reactividad Vue
 */
export const useComponentesStore = defineStore('componentes', () => {
  const { showAlert, validateRequired, validatePrice, parseNumber } = useUtils()

  // ==========================================
  // ESTADO REACTIVO (migración del constructor)
  // ==========================================
  
  const componentes = ref([])
  const filteredComponentes = ref([])
  const currentComponent = ref(null)
  const loading = ref(false)
  const isEditMode = ref(false)
  
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
    selectedType: ''
  })

  // Estado de UI para modales
  const showCreateModal = ref(false)
  const showDetailModal = ref(false)
  const modalLoading = ref(false)
  const tableLoading = ref(false)
  
  // Estado del formulario
  const formData = ref({
    id: '',
    tipoComponente: '',
    descripcion: '',
    marca: '',
    modelo: '',
    costo: 0,
    precioBase: 0,
    // Campos específicos por tipo
    tamano: 0,
    resolucion: '',
    capacidad: 0,
    tipoConexion: '',
    memoriaGpu: 0,
    tipoMemoria: ''
  })
  
  // Componente actual para detalles
  const currentComponente = ref(null)

  // ==========================================
  // COMPUTED PROPERTIES (reactividad automática)
  // ==========================================
  
  const hasComponents = computed(() => componentes.value.length > 0)
  
  const hasFilteredComponents = computed(() => filteredComponentes.value.length > 0)
  
  const paginatedComponents = computed(() => {
    const startIndex = (pagination.value.currentPage - 1) * pagination.value.pageSize
    const endIndex = startIndex + pagination.value.pageSize
    return filteredComponentes.value.slice(startIndex, endIndex)
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
  
  // Computed para modales
  const modalTitle = computed(() => {
    return isEditMode.value ? 'Editar Componente' : 'Nuevo Componente'
  })
  
  const isFormValid = computed(() => {
    return formData.value.id &&
           formData.value.tipoComponente &&
           formData.value.descripcion &&
           formData.value.marca &&
           formData.value.modelo &&
           formData.value.precioBase > 0
  })

  // ==========================================
  // COMPUTED PROPERTIES - PERMISOS
  // ==========================================
  
  const canViewComponentes = computed(() => authService.canViewComponentes())
  const canCreateComponentes = computed(() => authService.canCreateComponentes())
  const canEditComponentes = computed(() => authService.canEditComponentes())
  const canDeleteComponentes = computed(() => authService.canDeleteComponentes())
  const userRoles = computed(() => authService.getUserRoles())
  const isAdmin = computed(() => authService.isAdmin())
  const primaryRole = computed(() => authService.getPrimaryRole())

  // ==========================================
  // ACTIONS - CRUD OPERATIONS
  // ==========================================
  
  /**
   * Cargar todos los componentes (migración de loadComponentes)
   */
  const fetchComponentes = async () => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ComponentesStore] Fetching componentes...')
    }
    
    try {
      loading.value = true
      tableLoading.value = true
      
      const data = await componentesApi.getAll()
      componentes.value = data || []
      
      // Aplicar filtros existentes
      applyFilters()
      
      if (DEBUG_CONFIG.ENABLED) {
        console.log(`[ComponentesStore] Loaded ${componentes.value.length} componentes`)
      }
      
    } catch (error) {
      console.error('[ComponentesStore] Error fetching componentes:', error)
      showAlert('error', error.message || MESSAGES.NETWORK_ERROR)
      componentes.value = []
      filteredComponentes.value = []
    } finally {
      loading.value = false
      tableLoading.value = false
    }
  }

  /**
   * Crear nuevo componente (migración de createComponente)
   */
  const createComponente = async (componenteData) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ComponentesStore] Creating componente:', componenteData)
    }
    
    // Verificar permisos antes de proceder
    if (!authService.canCreateComponentes()) {
      const error = 'No tienes permisos para crear componentes'
      showAlert('error', error)
      return { success: false, error }
    }
    
    try {
      loading.value = true
      
      // Validar datos antes de enviar
      const validationErrors = validateComponenteData(componenteData)
      if (validationErrors.length > 0) {
        showAlert('error', validationErrors.join('\n'))
        return { success: false, errors: validationErrors }
      }
      
      const response = await componentesApi.create(componenteData)
      
      showAlert('success', MESSAGES.COMPONENT_CREATED)
      
      // Recargar componentes para obtener datos actualizados
      await fetchComponentes()
      
      return { success: true, data: response }
      
    } catch (error) {
      console.error('[ComponentesStore] Error creating componente:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  /**
   * Actualizar componente existente (migración de updateComponente)
   */
  const updateComponente = async (id, componenteData) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ComponentesStore] Updating componente:', id, componenteData)
    }
    
    // Verificar permisos antes de proceder
    if (!authService.canEditComponentes()) {
      const error = 'No tienes permisos para editar componentes'
      showAlert('error', error)
      return { success: false, error }
    }
    
    try {
      loading.value = true
      
      // Validar datos antes de enviar
      const validationErrors = validateComponenteData(componenteData, true)
      if (validationErrors.length > 0) {
        showAlert('error', validationErrors.join('\n'))
        return { success: false, errors: validationErrors }
      }
      
      const response = await componentesApi.update(id, componenteData)
      
      showAlert('success', MESSAGES.COMPONENT_UPDATED)
      
      // Recargar componentes para obtener datos actualizados
      await fetchComponentes()
      
      return { success: true, data: response }
      
    } catch (error) {
      console.error('[ComponentesStore] Error updating componente:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  /**
   * Eliminar componente (migración de deleteComponent)
   */
  const deleteComponente = async (id) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ComponentesStore] Deleting componente:', id)
    }
    
    // Verificar permisos antes de proceder
    if (!authService.canDeleteComponentes()) {
      const error = 'No tienes permisos para eliminar componentes'
      showAlert('error', error)
      return { success: false, error }
    }
    
    try {
      loading.value = true
      
      await componentesApi.delete(id)
      
      showAlert('success', MESSAGES.COMPONENT_DELETED)
      
      // Recargar componentes para obtener datos actualizados
      await fetchComponentes()
      
      return { success: true }
      
    } catch (error) {
      console.error('[ComponentesStore] Error deleting componente:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  // ==========================================
  // ACTIONS - FILTROS Y BÚSQUEDA
  // ==========================================
  
  /**
   * Aplicar filtros (migración de applyFilters)
   */
  const applyFilters = () => {
    const searchTerm = filters.value.searchTerm.toLowerCase()
    const selectedType = filters.value.selectedType
    
    filteredComponentes.value = componentes.value.filter(componente => {
      // Filtro de búsqueda (migración exacta de la lógica original)
      const matchesSearch = !searchTerm || 
        componente.id.toLowerCase().includes(searchTerm) ||
        componente.descripcion.toLowerCase().includes(searchTerm) ||
        componente.marca.toLowerCase().includes(searchTerm) ||
        componente.modelo.toLowerCase().includes(searchTerm)

      // Filtro de tipo
      const matchesType = !selectedType || componente.tipoComponente === selectedType

      return matchesSearch && matchesType
    })

    // Reiniciar paginación al aplicar filtros (migración exacta)
    pagination.value.currentPage = 1
    updatePaginationInfo()
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log(`[ComponentesStore] Applied filters: ${filteredComponentes.value.length} results`)
    }
  }

  /**
   * Limpiar filtros (migración de clearFilters)
   */
  const clearFilters = () => {
    filters.value.searchTerm = ''
    filters.value.selectedType = ''
    applyFilters()
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ComponentesStore] Filters cleared')
    }
  }

  /**
   * Establecer filtro de búsqueda
   */
  const setSearchFilter = (searchTerm) => {
    filters.value.searchTerm = searchTerm
    applyFilters()
  }

  /**
   * Establecer filtro de tipo
   */
  const setTypeFilter = (type) => {
    filters.value.selectedType = type
    applyFilters()
  }

  // ==========================================
  // ACTIONS - PAGINACIÓN
  // ==========================================
  
  /**
   * Actualizar información de paginación (migración de updatePaginationInfo)
   */
  const updatePaginationInfo = () => {
    pagination.value.totalItems = filteredComponentes.value.length
    pagination.value.totalPages = Math.ceil(pagination.value.totalItems / pagination.value.pageSize)
    
    // Ajustar página actual si es necesario (migración exacta)
    if (pagination.value.currentPage > pagination.value.totalPages) {
      pagination.value.currentPage = Math.max(1, pagination.value.totalPages)
    }
  }

  /**
   * Ir a página específica (migración de goToPage)
   */
  const goToPage = (pageNumber) => {
    if (pageNumber >= 1 && pageNumber <= pagination.value.totalPages) {
      pagination.value.currentPage = pageNumber
      
      if (DEBUG_CONFIG.ENABLED) {
        console.log(`[ComponentesStore] Navigated to page ${pageNumber}`)
      }
    }
  }

  /**
   * Cambiar tamaño de página (migración de handlePageSizeChange)
   */
  const setPageSize = (newPageSize) => {
    pagination.value.pageSize = newPageSize
    pagination.value.currentPage = 1 // Volver a la primera página
    updatePaginationInfo()
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log(`[ComponentesStore] Page size changed to ${newPageSize}`)
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
  // ACTIONS - MODAL Y FORMULARIO
  // ==========================================
  
  /**
   * Abrir modal para crear componente
   */
  const openCreateModal = () => {
    resetFormData()
    isEditMode.value = false
    showCreateModal.value = true
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ComponentesStore] Opened create modal')
    }
  }

  /**
   * Abrir modal para editar componente
   */
  const openEditModal = (componente) => {
    populateFormData(componente)
    isEditMode.value = true
    showCreateModal.value = true
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ComponentesStore] Opened edit modal for:', componente.id)
    }
  }

  /**
   * Abrir modal para ver detalles
   */
  const openDetailModal = async (componente) => {
    try {
      modalLoading.value = true
      currentComponente.value = componente
      showDetailModal.value = true
      
      if (DEBUG_CONFIG.ENABLED) {
        console.log('[ComponentesStore] Opened detail modal for:', componente.id)
      }
    } catch (error) {
      console.error('[ComponentesStore] Error opening detail modal:', error)
      showAlert('error', 'Error al cargar detalles del componente')
    } finally {
      modalLoading.value = false
    }
  }

  /**
   * Cerrar modal de crear/editar
   */
  const closeCreateModal = () => {
    showCreateModal.value = false
    resetFormData()
    isEditMode.value = false
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ComponentesStore] Closed create modal')
    }
  }

  /**
   * Cerrar modal de detalles
   */
  const closeDetailModal = () => {
    showDetailModal.value = false
    currentComponente.value = null
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[ComponentesStore] Closed detail modal')
    }
  }

  /**
   * Resetear datos del formulario
   */
  const resetFormData = () => {
    formData.value = {
      id: '',
      tipoComponente: '',
      descripcion: '',
      marca: '',
      modelo: '',
      costo: 0,
      precioBase: 0,
      tamano: 0,
      resolucion: '',
      capacidad: 0,
      tipoConexion: '',
      memoriaGpu: 0,
      tipoMemoria: ''
    }
  }

  /**
   * Poblar formulario con datos del componente
   */
  const populateFormData = (componente) => {
    formData.value = {
      id: componente.id || '',
      tipoComponente: componente.tipoComponente || '',
      descripcion: componente.descripcion || '',
      marca: componente.marca || '',
      modelo: componente.modelo || '',
      costo: componente.costo || 0,
      precioBase: componente.precioBase || 0,
      tamano: componente.tamano || 0,
      resolucion: componente.resolucion || '',
      capacidad: componente.capacidad || 0,
      tipoConexion: componente.tipoConexion || '',
      memoriaGpu: componente.memoriaGpu || 0,
      tipoMemoria: componente.tipoMemoria || ''
    }
  }

  /**
   * Enviar formulario
   */
  const submitComponente = async () => {
    try {
      modalLoading.value = true
      
      let result
      if (isEditMode.value) {
        result = await updateComponente(formData.value.id, formData.value)
      } else {
        result = await createComponente(formData.value)
      }
      
      if (result.success) {
        closeCreateModal()
        return { success: true }
      }
      
      return result
    } catch (error) {
      console.error('[ComponentesStore] Error submitting componente:', error)
      return { success: false, error: error.message }
    } finally {
      modalLoading.value = false
    }
  }

  // ==========================================
  // UTILIDADES Y VALIDACIONES
  // ==========================================
  
  /**
   * Validar datos del componente (migración de validateFormData)
   */
  const validateComponenteData = (data, isUpdate = false) => {
    const errors = []

    // ID requerido solo en creación
    if (!isUpdate && !validateRequired(data.id)) {
      errors.push('ID del componente es requerido')
    }
    
    if (!validateRequired(data.descripcion)) {
      errors.push('Descripción es requerida')
    }
    
    if (!validateRequired(data.tipoComponente)) {
      errors.push('Tipo de componente es requerido')
    }
    
    if (!validateRequired(data.marca)) {
      errors.push('Marca es requerida')
    }
    
    if (!validateRequired(data.modelo)) {
      errors.push('Modelo es requerido')
    }
    
    if (!validatePrice(data.costo)) {
      errors.push('Costo debe ser un número válido mayor a 0')
    }
    
    if (!validatePrice(data.precioBase)) {
      errors.push('Precio base debe ser un número válido mayor a 0')
    }

    // Validaciones específicas por tipo
    if (data.tipoComponente === COMPONENT_TYPES.DISCO_DURO && !validateRequired(data.capacidadAlm)) {
      errors.push('Capacidad de almacenamiento es requerida para discos duros')
    }
    
    if (data.tipoComponente === COMPONENT_TYPES.TARJETA_VIDEO && !validateRequired(data.memoria)) {
      errors.push('Memoria es requerida para tarjetas de video')
    }

    return errors
  }

  /**
   * Obtener campos adicionales para mostrar según tipo
   */
  const getExtraFieldsDisplay = (componente) => {
    if (componente.tipoComponente === COMPONENT_TYPES.DISCO_DURO && componente.capacidadAlm) {
      return `Capacidad: ${componente.capacidadAlm}`
    }
    if (componente.tipoComponente === COMPONENT_TYPES.TARJETA_VIDEO && componente.memoria) {
      return `Memoria: ${componente.memoria}`
    }
    return ''
  }

  /**
   * Buscar componente por ID
   */
  const findComponenteById = (id) => {
    return componentes.value.find(comp => comp.id === id)
  }

  /**
   * Verificar si existe un componente con el ID
   */
  const componenteExists = async (id) => {
    try {
      return await componentesApi.exists(id)
    } catch (error) {
      console.error('[ComponentesStore] Error checking if componente exists:', error)
      return false
    }
  }

  // ==========================================
  // RETURN (API PÚBLICA DEL STORE)
  // ==========================================
  
  return {
    // Estado reactivo (readonly para prevenir mutaciones directas)
    componentes: readonly(componentes),
    filteredComponentes: readonly(filteredComponentes),
    currentComponent,
    currentComponente: readonly(currentComponente),
    loading: readonly(loading),
    tableLoading: readonly(tableLoading),
    modalLoading: readonly(modalLoading),
    isEditMode: readonly(isEditMode),
    showCreateModal: readonly(showCreateModal),
    showDetailModal: readonly(showDetailModal),
    formData,
    pagination: readonly(pagination),
    filters: readonly(filters),
    
    // Computed properties
    hasComponents,
    hasFilteredComponents,
    paginatedComponents,
    paginationInfo,
    canGoPrevious,
    canGoNext,
    modalTitle,
    isFormValid,
    
    // Permisos
    canViewComponentes,
    canCreateComponentes,
    canEditComponentes,
    canDeleteComponentes,
    userRoles,
    isAdmin,
    primaryRole,
    
    // Actions - CRUD
    fetchComponentes,
    createComponente,
    updateComponente,
    deleteComponente,
    
    // Actions - Filtros
    applyFilters,
    clearFilters,
    setSearchFilter,
    setTypeFilter,
    
    // Actions - Paginación
    goToPage,
    setPageSize,
    goToPreviousPage,
    goToNextPage,
    
    // Actions - Modal
    openCreateModal,
    openEditModal,
    openDetailModal,
    closeCreateModal,
    closeDetailModal,
    resetFormData,
    populateFormData,
    submitComponente,
    
    // Utilidades
    getExtraFieldsDisplay,
    findComponenteById,
    componenteExists,
    validateComponenteData
  }
})