import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import { pcsApi } from '@/services/pcsApi'
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
 * Store de PCs - Migración completa del PcsManager
 * Incluye gestión de PCs, armado de componentes, y cálculos en tiempo real
 */
export const usePcsStore = defineStore('pcs', () => {
  const { showAlert, validateRequired, validatePrice, formatCurrency, confirm } = useUtils()

  // ==========================================
  // ESTADO REACTIVO (migración del constructor)
  // ==========================================
  
  const pcs = ref([])
  const filteredPcs = ref([])
  const currentPc = ref(null)
  const currentPcComponents = ref([])
  const availableComponents = ref([])
  const loading = ref(false)
  const tableLoading = ref(false)
  
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
    priceRange: ''
  })

  // Estado de UI para el modal de gestión
  const showManageModal = ref(false)
  const modalLoading = ref(false)
  const componentSelectValue = ref('')
  const componentQuantity = ref(1)

  // Estado de UI para el modal de creación/edición
  const showCreateModal = ref(false)
  const isEditMode = ref(false)
  const isViewMode = ref(false)

  // Estado del formulario de PC
  const formData = ref({
    id: '',
    descripcion: '',
    marca: '',
    modelo: '',
    costo: 0,
    precioBase: 0
  })

  // ==========================================
  // COMPUTED PROPERTIES (reactividad automática)
  // ==========================================
  
  const hasPcs = computed(() => pcs.value.length > 0)
  
  const hasFilteredPcs = computed(() => filteredPcs.value.length > 0)
  
  const paginatedPcs = computed(() => {
    const startIndex = (pagination.value.currentPage - 1) * pagination.value.pageSize
    const endIndex = startIndex + pagination.value.pageSize
    return filteredPcs.value.slice(startIndex, endIndex)
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

  // Computed para modal de gestión
  const currentPcStats = computed(() => {
    if (!currentPcComponents.value.length) {
      return {
        totalComponents: 0,
        totalCost: 0,
        formattedCost: formatCurrency(0)
      }
    }
    
    const totalCost = currentPcComponents.value.reduce((sum, comp) => sum + (comp.costo || 0), 0)
    
    return {
      totalComponents: currentPcComponents.value.length,
      totalCost,
      formattedCost: formatCurrency(totalCost)
    }
  })

  const availableComponentsForSelect = computed(() => {
    // Filtrar componentes que no sean PCs
    return availableComponents.value.filter(comp => comp.tipoComponente !== COMPONENT_TYPES.PC)
  })

  // Computed para validación del formulario de PC
  const isFormValid = computed(() => {
    if (isViewMode.value) return true
    
    return formData.value.id &&
           formData.value.descripcion &&
           formData.value.marca &&
           formData.value.modelo &&
           formData.value.costo > 0 &&
           formData.value.precioBase > 0
  })

  // Computed para el título del modal de PC
  const modalTitle = computed(() => {
    if (isViewMode.value) return 'Ver PC'
    if (isEditMode.value) return 'Editar PC'
    return 'Nueva PC'
  })

  // ==========================================
  // COMPUTED PROPERTIES - PERMISOS
  // ==========================================
  
  const canViewPcs = computed(() => authService.canViewPcs())
  const canCreatePcs = computed(() => authService.canCreatePcs())
  const canEditPcs = computed(() => authService.canEditPcs())
  const canDeletePcs = computed(() => authService.canDeletePcs())
  const canAddComponentToPc = computed(() => authService.canAddComponentToPc())
  const canRemoveComponentFromPc = computed(() => authService.canRemoveComponentFromPc())
  const canViewPcCost = computed(() => authService.canViewPcCost())
  const canModifyPcPrice = computed(() => authService.canModifyPcPrice())
  const userRoles = computed(() => authService.getUserRoles())
  const isAdmin = computed(() => authService.isAdmin())
  const primaryRole = computed(() => authService.getPrimaryRole())

  // ==========================================
  // ACTIONS - CRUD OPERATIONS
  // ==========================================
  
  /**
   * Cargar todas las PCs (migración de loadPcs)
   */
  const fetchPcs = async () => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PcsStore] Fetching PCs...')
    }
    
    try {
      loading.value = true
      tableLoading.value = true
      
      const data = await pcsApi.getAll()
      pcs.value = data || []
      
      // Aplicar filtros existentes
      applyFilters()
      
      if (DEBUG_CONFIG.ENABLED) {
        console.log(`[PcsStore] Loaded ${pcs.value.length} PCs`)
      }
      
    } catch (error) {
      console.error('[PcsStore] Error fetching PCs:', error)
      showAlert('error', error.message || MESSAGES.NETWORK_ERROR)
      pcs.value = []
      filteredPcs.value = []
    } finally {
      loading.value = false
      tableLoading.value = false
    }
  }

  /**
   * Crear nueva PC
   */
  const createPc = async (pcData) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PcsStore] Creating PC:', pcData)
    }
    
    // Verificar permisos antes de proceder
    if (!authService.canCreatePcs()) {
      const error = 'No tienes permisos para crear configuraciones de PC'
      showAlert('error', error)
      return { success: false, error }
    }
    
    try {
      loading.value = true
      
      // Validar datos antes de enviar
      const validationErrors = validatePcData(pcData)
      if (validationErrors.length > 0) {
        showAlert('error', validationErrors.join('\n'))
        return { success: false, errors: validationErrors }
      }
      
      const response = await pcsApi.create(pcData)
      
      showAlert('success', MESSAGES.PC_CREATED)
      
      // Recargar PCs para obtener datos actualizados
      await fetchPcs()
      
      return { success: true, data: response }
      
    } catch (error) {
      console.error('[PcsStore] Error creating PC:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  /**
   * Actualizar PC existente
   */
  const updatePc = async (id, pcData) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PcsStore] Updating PC:', id, pcData)
    }
    
    // Verificar permisos antes de proceder
    if (!authService.canEditPcs()) {
      const error = 'No tienes permisos para editar configuraciones de PC'
      showAlert('error', error)
      return { success: false, error }
    }
    
    try {
      loading.value = true
      
      // Validar datos antes de enviar
      const validationErrors = validatePcData(pcData, true)
      if (validationErrors.length > 0) {
        showAlert('error', validationErrors.join('\n'))
        return { success: false, errors: validationErrors }
      }
      
      const response = await pcsApi.update(id, pcData)
      
      showAlert('success', MESSAGES.PC_UPDATED)
      
      // Recargar PCs para obtener datos actualizados
      await fetchPcs()
      
      return { success: true, data: response }
      
    } catch (error) {
      console.error('[PcsStore] Error updating PC:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  /**
   * Eliminar PC
   */
  const deletePc = async (id) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PcsStore] Deleting PC:', id)
    }
    
    // Verificar permisos antes de proceder
    if (!authService.canDeletePcs()) {
      const error = 'No tienes permisos para eliminar configuraciones de PC'
      showAlert('error', error)
      return { success: false, error }
    }
    
    try {
      loading.value = true
      
      await pcsApi.delete(id)
      
      showAlert('success', MESSAGES.PC_DELETED)
      
      // Recargar PCs para obtener datos actualizados
      await fetchPcs()
      
      return { success: true }
      
    } catch (error) {
      console.error('[PcsStore] Error deleting PC:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  // ==========================================
  // ACTIONS - GESTIÓN DE COMPONENTES
  // ==========================================
  
  /**
   * Abrir modal de gestión de componentes (migración de openManageModal)
   */
  const openManageModal = async (pcId) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PcsStore] Opening manage modal for PC:', pcId)
    }
    
    try {
      modalLoading.value = true
      
      // Obtener información de la PC
      const pc = await pcsApi.getById(pcId)
      currentPc.value = pc
      
      // Cargar componentes disponibles
      await loadAvailableComponents()
      
      // Cargar componentes de la PC
      await loadPcComponents(pcId)
      
      // Mostrar modal
      showManageModal.value = true
      
    } catch (error) {
      console.error('[PcsStore] Error opening manage modal:', error)
      showAlert('error', error.message || MESSAGES.OPERATION_ERROR)
    } finally {
      modalLoading.value = false
    }
  }

  /**
   * Cerrar modal de gestión
   */
  const closeManageModal = () => {
    showManageModal.value = false
    currentPc.value = null
    currentPcComponents.value = []
    availableComponents.value = []
    componentSelectValue.value = ''
    componentQuantity.value = 1
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PcsStore] Closed manage modal')
    }
  }

  /**
   * Cargar componentes disponibles (migración de loadAvailableComponents)
   */
  const loadAvailableComponents = async () => {
    try {
      const allComponents = await componentesApi.getAll()
      // Filtrar solo componentes que no sean PCs
      availableComponents.value = allComponents.filter(comp => comp.tipoComponente !== COMPONENT_TYPES.PC)
      
      if (DEBUG_CONFIG.ENABLED) {
        console.log(`[PcsStore] Loaded ${availableComponents.value.length} available components`)
      }
      
    } catch (error) {
      console.error('[PcsStore] Error loading available components:', error)
      availableComponents.value = []
    }
  }

  /**
   * Cargar componentes de la PC actual (migración de loadPcComponents)
   */
  const loadPcComponents = async (pcId) => {
    try {
      currentPcComponents.value = await pcsApi.getComponents(pcId)
      
      if (DEBUG_CONFIG.ENABLED) {
        console.log(`[PcsStore] Loaded ${currentPcComponents.value.length} components for PC ${pcId}`)
      }
      
    } catch (error) {
      console.error('[PcsStore] Error loading PC components:', error)
      currentPcComponents.value = []
    }
  }

  /**
   * Agregar componente a la PC (migración de handleAddComponent)
   */
  const addComponentToPc = async (componentId, quantity = 1) => {
    // Verificar permisos antes de proceder
    if (!authService.canAddComponentToPc()) {
      const error = 'No tienes permisos para agregar componentes a PCs'
      showAlert('error', error)
      return { success: false, error }
    }

    if (!componentId) {
      showAlert('error', 'Seleccione un componente para agregar')
      return { success: false }
    }

    if (!currentPc.value) {
      showAlert('error', 'No hay PC seleccionada')
      return { success: false }
    }

    try {
      modalLoading.value = true
      
      await pcsApi.addComponent(currentPc.value.id, {
        id: componentId,
        cantidad: quantity
      })
      
      showAlert('success', MESSAGES.PC_COMPONENT_ADDED)
      
      // Recargar componentes de la PC
      await loadPcComponents(currentPc.value.id)
      
      // Limpiar formulario
      componentSelectValue.value = ''
      componentQuantity.value = 1
      
      return { success: true }
      
    } catch (error) {
      console.error('[PcsStore] Error adding component to PC:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      modalLoading.value = false
    }
  }

  /**
   * Quitar componente de la PC (migración de handleRemoveComponent)
   */
  const removeComponentFromPc = async (componentId) => {
    // Verificar permisos antes de proceder
    if (!authService.canRemoveComponentFromPc()) {
      const error = 'No tienes permisos para quitar componentes de PCs'
      showAlert('error', error)
      return { success: false, error }
    }

    if (!currentPc.value) {
      showAlert('error', 'No hay PC seleccionada')
      return { success: false }
    }

    const confirmed = await confirm(
      'Quitar Componente',
      '¿Está seguro de que desea quitar este componente de la PC?'
    )

    if (!confirmed) {
      return { success: false }
    }

    try {
      modalLoading.value = true
      
      await pcsApi.removeComponent(currentPc.value.id, componentId)
      
      showAlert('success', MESSAGES.PC_COMPONENT_REMOVED)
      
      // Recargar componentes de la PC
      await loadPcComponents(currentPc.value.id)
      
      return { success: true }
      
    } catch (error) {
      console.error('[PcsStore] Error removing component from PC:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      modalLoading.value = false
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
    const priceRange = filters.value.priceRange
    
    filteredPcs.value = pcs.value.filter(pc => {
      // Filtro de búsqueda (migración exacta de la lógica original)
      const matchesSearch = !searchTerm || 
        pc.id.toLowerCase().includes(searchTerm) ||
        pc.descripcion.toLowerCase().includes(searchTerm) ||
        pc.marca.toLowerCase().includes(searchTerm) ||
        pc.modelo.toLowerCase().includes(searchTerm)

      // Filtro de precio (migración de matchesPriceRange)
      const matchesPrice = matchesPriceRange(pc.precioBase, priceRange)

      return matchesSearch && matchesPrice
    })

    // Reiniciar paginación al aplicar filtros (migración exacta)
    pagination.value.currentPage = 1
    updatePaginationInfo()
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log(`[PcsStore] Applied filters: ${filteredPcs.value.length} results`)
    }
  }

  /**
   * Verificar si un precio coincide con el rango (migración de matchesPriceRange)
   */
  const matchesPriceRange = (price, range) => {
    if (!range) return true

    switch (range) {
      case '0-1000':
        return price >= 0 && price <= 1000
      case '1000-2000':
        return price > 1000 && price <= 2000
      case '2000-5000':
        return price > 2000 && price <= 5000
      case '5000+':
        return price > 5000
      default:
        return true
    }
  }

  /**
   * Limpiar filtros (migración de clearFilters)
   */
  const clearFilters = () => {
    filters.value.searchTerm = ''
    filters.value.priceRange = ''
    applyFilters()
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PcsStore] Filters cleared')
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
   * Establecer filtro de precio
   */
  const setPriceFilter = (priceRange) => {
    filters.value.priceRange = priceRange
    applyFilters()
  }

  // ==========================================
  // ACTIONS - PAGINACIÓN
  // ==========================================
  
  /**
   * Actualizar información de paginación (migración de updatePaginationInfo)
   */
  const updatePaginationInfo = () => {
    pagination.value.totalItems = filteredPcs.value.length
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
        console.log(`[PcsStore] Navigated to page ${pageNumber}`)
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
      console.log(`[PcsStore] Page size changed to ${newPageSize}`)
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
   * Validar datos de PC
   */
  const validatePcData = (data, isUpdate = false) => {
    const errors = []

    // ID requerido solo en creación
    if (!isUpdate && !validateRequired(data.id)) {
      errors.push('ID de la PC es requerido')
    }
    
    if (!validateRequired(data.descripcion)) {
      errors.push('Descripción es requerida')
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

    return errors
  }

  /**
   * Buscar PC por ID
   */
  const findPcById = (id) => {
    return pcs.value.find(pc => pc.id === id)
  }

  /**
   * Verificar si existe una PC con el ID
   */
  const pcExists = async (id) => {
    try {
      return await pcsApi.exists(id)
    } catch (error) {
      console.error('[PcsStore] Error checking if PC exists:', error)
      return false
    }
  }

  /**
   * Obtener estadísticas de conteo de componentes para las PCs mostradas
   */
  const loadComponentsCounts = async () => {
    for (const pc of paginatedPcs.value) {
      try {
        const components = await pcsApi.getComponents(pc.id)
        // Actualizar el conteo en el PC para mostrar en la UI
        pc._componentCount = components.length
        pc._componentCountLoaded = true
      } catch (error) {
        console.error(`[PcsStore] Error loading components count for PC ${pc.id}:`, error)
        pc._componentCount = 0
        pc._componentCountLoaded = false
      }
    }
  }

  // ==========================================
  // ACTIONS - GESTIÓN DE MODALES DE PC
  // ==========================================
  
  /**
   * Abrir modal para crear nueva PC
   */
  const openCreateModal = () => {
    // Importar dinámicamente para evitar circular imports
    import('@/stores/useAuthStore').then(({ useAuthStore }) => {
      const authStore = useAuthStore()
      
      // Verificar autenticación
      if (!authStore.requireAuth()) {
        return
      }
      
      // Resetear estado
      isEditMode.value = false
      isViewMode.value = false
      currentPc.value = null
      
      // Configurar valores por defecto
      formData.value = {
        id: '',
        descripcion: '',
        marca: '',
        modelo: '',
        costo: 0,
        precioBase: 0
      }
      
      showCreateModal.value = true
      
      if (DEBUG_CONFIG.ENABLED) {
        console.log('[PcsStore] Opened create PC modal')
      }
    })
  }

  /**
   * Abrir modal para ver PC existente
   */
  const openViewModal = async (id) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PcsStore] Opening view modal for PC:', id)
    }
    
    try {
      modalLoading.value = true
      
      // Obtener información de la PC
      const pc = await pcsApi.getById(id)
      currentPc.value = pc
      
      // Configurar modo vista
      isEditMode.value = false
      isViewMode.value = true
      
      // Configurar formulario con datos de la PC
      formData.value = {
        id: pc.id,
        descripcion: pc.descripcion,
        marca: pc.marca,
        modelo: pc.modelo,
        costo: pc.costo,
        precioBase: pc.precioBase
      }
      
      showCreateModal.value = true
      
    } catch (error) {
      console.error('[PcsStore] Error opening view modal:', error)
      showAlert('error', error.message || MESSAGES.OPERATION_ERROR)
    } finally {
      modalLoading.value = false
    }
  }

  /**
   * Abrir modal para editar PC existente
   */
  const openEditModal = async (id) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PcsStore] Opening edit modal for PC:', id)
    }
    
    try {
      modalLoading.value = true
      
      // Obtener información de la PC
      const pc = await pcsApi.getById(id)
      currentPc.value = pc
      
      // Configurar modo edición
      isEditMode.value = true
      isViewMode.value = false
      
      // Configurar formulario con datos de la PC
      formData.value = {
        id: pc.id,
        descripcion: pc.descripcion,
        marca: pc.marca,
        modelo: pc.modelo,
        costo: pc.costo,
        precioBase: pc.precioBase
      }
      
      showCreateModal.value = true
      
    } catch (error) {
      console.error('[PcsStore] Error opening edit modal:', error)
      showAlert('error', error.message || MESSAGES.OPERATION_ERROR)
    } finally {
      modalLoading.value = false
    }
  }

  /**
   * Cerrar modal de PC
   */
  const closeCreateModal = () => {
    showCreateModal.value = false
    currentPc.value = null
    isEditMode.value = false
    isViewMode.value = false
    modalLoading.value = false
    
    // Resetear formulario
    formData.value = {
      id: '',
      descripcion: '',
      marca: '',
      modelo: '',
      costo: 0,
      precioBase: 0
    }
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PcsStore] Closed PC modal')
    }
  }

  /**
   * Enviar formulario de PC
   */
  const submitPc = async () => {
    if (isViewMode.value) {
      closeCreateModal()
      return { success: true }
    }

    if (!isFormValid.value) {
      showAlert('error', 'Complete todos los campos requeridos')
      return { success: false }
    }

    try {
      modalLoading.value = true
      
      let result
      
      if (isEditMode.value && currentPc.value) {
        // Actualizar PC existente
        result = await updatePc(currentPc.value.id, formData.value)
      } else {
        // Crear nueva PC
        result = await createPc(formData.value)
      }
      
      if (result.success) {
        closeCreateModal()
      }
      
      return result
      
    } catch (error) {
      console.error('[PcsStore] Error submitting PC:', error)
      return { success: false, error: error.message }
    } finally {
      modalLoading.value = false
    }
  }

  // ==========================================
  // RETURN (API PÚBLICA DEL STORE)
  // ==========================================
  
  return {
    // Estado reactivo (readonly para prevenir mutaciones directas)
    pcs: readonly(pcs),
    filteredPcs: readonly(filteredPcs),
    currentPc: readonly(currentPc),
    currentPcComponents: readonly(currentPcComponents),
    availableComponents: readonly(availableComponents),
    loading: readonly(loading),
    tableLoading: readonly(tableLoading),
    modalLoading: readonly(modalLoading),
    showManageModal,
    componentSelectValue,
    componentQuantity,
    pagination: readonly(pagination),
    filters: readonly(filters),
    
    // Estado de modal de PC
    showCreateModal,
    isEditMode: readonly(isEditMode),
    isViewMode: readonly(isViewMode),
    formData,
    
    // Computed properties
    hasPcs,
    hasFilteredPcs,
    paginatedPcs,
    paginationInfo,
    canGoPrevious,
    canGoNext,
    currentPcStats,
    availableComponentsForSelect,
    isFormValid,
    modalTitle,
    
    // Permisos
    canViewPcs,
    canCreatePcs,
    canEditPcs,
    canDeletePcs,
    canAddComponentToPc,
    canRemoveComponentFromPc,
    canViewPcCost,
    canModifyPcPrice,
    userRoles,
    isAdmin,
    primaryRole,
    
    // Actions - CRUD
    fetchPcs,
    createPc,
    updatePc,
    deletePc,
    
    // Actions - Gestión de modales de PC
    openCreateModal,
    openViewModal,
    openEditModal,
    closeCreateModal,
    submitPc,
    
    // Actions - Gestión de componentes
    openManageModal,
    closeManageModal,
    loadAvailableComponents,
    loadPcComponents,
    addComponentToPc,
    removeComponentFromPc,
    
    // Actions - Filtros
    applyFilters,
    clearFilters,
    setSearchFilter,
    setPriceFilter,
    
    // Actions - Paginación
    goToPage,
    setPageSize,
    goToPreviousPage,
    goToNextPage,
    
    // Utilidades
    findPcById,
    pcExists,
    validatePcData,
    loadComponentsCounts
  }
})