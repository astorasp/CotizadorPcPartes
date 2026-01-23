import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import { pedidosApi } from '@/services/pedidosApi'
import { cotizacionesApi } from '@/services/cotizacionesApi'
import { proveedoresApi } from '@/services/proveedoresApi'
import { useUtils } from '@/composables/useUtils'
import { authService } from '@/services/authService'
import { useCrudOperations, useAsyncOperation } from '@/composables/useAsyncOperation'
import { 
  UI_CONFIG, 
  MESSAGES, 
  DEBUG_CONFIG 
} from '@/utils/constants'

/**
 * Store de Pedidos - Migración completa del PedidosManager
 * Incluye gestión de pedidos, generación desde cotizaciones, y vista de detalles
 */
export const usePedidosStore = defineStore('pedidos', () => {
  const { showAlert, validateRequired, confirm } = useUtils()
  
  // Sistema de loading centralizado para pedidos
  const crudOps = useCrudOperations('pedido')
  
  // Para operaciones específicas como generación de pedidos
  const asyncOp = useAsyncOperation({
    showNotification: true,
    type: 'order'
  })

  // ==========================================
  // ESTADO REACTIVO (migración del constructor)
  // ==========================================
  
  const pedidos = ref([])
  const filteredPedidos = ref([])
  const cotizaciones = ref([])
  const proveedores = ref([])
  const currentPedido = ref(null)
  const loading = ref(false)
  const tableLoading = ref(false)
  const modalLoading = ref(false)
  
  // Configuración de paginación (migración exacta)
  const pagination = ref({
    currentPage: 1,
    pageSize: UI_CONFIG.DEFAULT_PAGE_SIZE,
    totalPages: 0,
    totalItems: 0
  })
  
  // Filtros de búsqueda y fecha
  const filters = ref({
    searchTerm: '',
    fechaDesde: ''
  })

  // Estado de UI para modales
  const showCreateModal = ref(false)
  const showDetailModal = ref(false)

  // Estado del formulario de creación
  const formData = ref({
    cotizacionId: '',
    cveProveedor: '',
    fechaEmision: '',
    fechaEntrega: '',
    nivelSurtido: 100
  })

  // Información adicional para el formulario
  const selectedCotizacion = ref(null)
  const selectedProveedor = ref(null)
  const pedidoPreview = ref(null)

  // ==========================================
  // COMPUTED PROPERTIES (reactividad automática)
  // ==========================================
  
  const hasPedidos = computed(() => pedidos.value.length > 0)
  
  const hasFilteredPedidos = computed(() => filteredPedidos.value.length > 0)
  
  const paginatedPedidos = computed(() => {
    const startIndex = (pagination.value.currentPage - 1) * pagination.value.pageSize
    const endIndex = startIndex + pagination.value.pageSize
    return filteredPedidos.value.slice(startIndex, endIndex)
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
    return formData.value.cotizacionId &&
           formData.value.cveProveedor &&
           formData.value.fechaEmision &&
           formData.value.fechaEntrega &&
           formData.value.nivelSurtido >= 1 &&
           formData.value.nivelSurtido <= 100
  })

  // Computed para cotizaciones disponibles para selección
  const availableCotizaciones = computed(() => {
    return (cotizaciones.value || []).map(cotizacion => ({
      ...cotizacion,
      displayText: `COT-${cotizacion.folio} - ${pedidosApi.formatDate(cotizacion.fecha)} - ${pedidosApi.formatCurrency(cotizacion.total)}`
    }))
  })

  // Computed para proveedores disponibles para selección
  const availableProveedores = computed(() => {
    return (proveedores.value || []).map(proveedor => ({
      ...proveedor,
      displayText: `${proveedor.cve} - ${proveedor.nombre}`
    }))
  })

  // ==========================================
  // COMPUTED PROPERTIES - PERMISOS
  // ==========================================
  
  const canViewPedidos = computed(() => authService.canViewPedidos())
  const canCreatePedidos = computed(() => authService.canCreatePedidos())
  const canEditPedidos = computed(() => authService.canEditPedidos())
  const canDeletePedidos = computed(() => authService.canDeletePedidos())
  const canApprovePedidos = computed(() => authService.canApprovePedidos())
  const canChangePedidoStatus = computed(() => authService.canChangePedidoStatus())
  const canViewPedidoFulfillment = computed(() => authService.canViewPedidoFulfillment())
  const canManagePedidoFulfillment = computed(() => authService.canManagePedidoFulfillment())
  const canViewPedidoFinancialData = computed(() => authService.canViewPedidoFinancialData())
  const canGeneratePedidoReports = computed(() => authService.canGeneratePedidoReports())
  const userRoles = computed(() => authService.getUserRoles())
  const isAdmin = computed(() => authService.isAdmin())
  const primaryRole = computed(() => authService.getPrimaryRole())
  
  // ==========================================
  // COMPUTED PROPERTIES - LOADING STATES
  // ==========================================
  
  const isFetching = computed(() => crudOps.loadingStore.isOperationActive('fetch-pedido'))
  const isFetchingCotizaciones = computed(() => crudOps.loadingStore.isOperationActive('fetch-cotizaciones-for-pedidos'))
  const isFetchingProveedores = computed(() => crudOps.loadingStore.isOperationActive('fetch-proveedores-for-pedidos'))
  const isGeneratingPedido = computed(() => asyncOp.loadingStore.isOperationActive('generate-pedido'))
  const isLoadingDetails = computed(() => asyncOp.loadingStore.isOperationActive('get-pedido-details'))

  // ==========================================
  // ACTIONS - OPERACIONES DE CARGA
  // ==========================================
  
  /**
   * Cargar todos los pedidos (migración de loadPedidos)
   */
  const fetchPedidos = async () => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PedidosStore] Fetching pedidos...')
    }
    
    const result = await crudOps.fetch(async () => {
      const data = await pedidosApi.getAll()
      pedidos.value = data || []
      
      // Aplicar filtros existentes
      applyFilters()
      
      if (DEBUG_CONFIG.ENABLED) {
        // console.log(`[PedidosStore] Loaded ${pedidos.value.length} pedidos`)
      }
    })
    
    if (!result.success) {
      pedidos.value = []
      filteredPedidos.value = []
      showAlert('error', result.error || MESSAGES.NETWORK_ERROR)
    }
  }

  /**
   * Cargar cotizaciones para el formulario
   */
  const fetchCotizaciones = async () => {
    const result = await crudOps.fetch(async () => {
      const data = await cotizacionesApi.getAll()
      cotizaciones.value = data || []
      
      if (DEBUG_CONFIG.ENABLED) {
        // console.log(`[PedidosStore] Loaded ${cotizaciones.value.length} cotizaciones`)
      }
    }, 'fetch-cotizaciones-for-pedidos')
    
    if (!result.success) {
      cotizaciones.value = []
      console.error('[PedidosStore] Error fetching cotizaciones:', result.error)
    }
  }

  /**
   * Cargar proveedores para el formulario
   */
  const fetchProveedores = async () => {
    const result = await crudOps.fetch(async () => {
      const data = await proveedoresApi.getAll()
      proveedores.value = data || []
      
      if (DEBUG_CONFIG.ENABLED) {
        // console.log(`[PedidosStore] Loaded ${proveedores.value.length} proveedores`)
      }
    }, 'fetch-proveedores-for-pedidos')
    
    if (!result.success) {
      proveedores.value = []
      console.error('[PedidosStore] Error fetching proveedores:', result.error)
    }
  }

  /**
   * Inicializar datos necesarios
   */
  const initialize = async () => {
    await Promise.all([
      fetchPedidos(),
      fetchCotizaciones(),
      fetchProveedores()
    ])
    setDefaultDates()
  }

  // ==========================================
  // ACTIONS - GENERACIÓN DE PEDIDOS
  // ==========================================
  
  /**
   * Generar nuevo pedido desde cotización
   */
  const generatePedido = async (pedidoData) => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PedidosStore] Generating pedido:', pedidoData)
    }
    
    const result = await asyncOp.execute(async () => {
      // Validar datos antes de enviar
      const validation = pedidosApi.validatePedido(pedidoData)
      if (!validation.isValid) {
        throw new Error(validation.errors.join('\n'))
      }
      
      // Formatear datos
      const formattedData = pedidosApi.formatPedidoData(pedidoData)
      
      const response = await pedidosApi.generate(formattedData)
      
      // Recargar pedidos para obtener datos actualizados
      await fetchPedidos()
      
      return response
    }, 'generate-pedido')
    
    if (result.success) {
      showAlert('success', MESSAGES.PEDIDO_GENERATED)
    } else {
      showAlert('error', result.error || MESSAGES.PEDIDO_GENERATION_ERROR)
    }
    
    return result
  }

  /**
   * Obtener detalles de un pedido específico
   */
  const getPedidoDetails = async (numPedido) => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PedidosStore] Getting pedido details:', numPedido)
    }
    
    const result = await asyncOp.execute(async () => {
      const pedido = await pedidosApi.getById(numPedido)
      currentPedido.value = pedido
      return pedido
    }, 'get-pedido-details')
    
    if (!result.success) {
      showAlert('error', result.error || MESSAGES.PEDIDO_NOT_FOUND)
    }
    
    return result
  }

  // ==========================================
  // ACTIONS - GESTIÓN DE MODALES
  // ==========================================
  
  /**
   * Abrir modal para crear nuevo pedido (migración de openCreateModal)
   */
  const openCreateModal = () => {
    // Verificar permisos
    if (!canCreatePedidos.value) {
      showAlert('error', MESSAGES.FORBIDDEN)
      return
    }
    
    // Resetear estado
    currentPedido.value = null
    selectedCotizacion.value = null
    selectedProveedor.value = null
    pedidoPreview.value = null
    
    // Configurar valores por defecto
    setDefaultFormData()
    
    showCreateModal.value = true
    
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PedidosStore] Opened create modal')
    }
  }

  /**
   * Abrir modal para ver detalles de pedido
   */
  const openDetailModal = async (numPedido) => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PedidosStore] Opening detail modal for pedido:', numPedido)
    }
    
    const result = await getPedidoDetails(numPedido)
    if (result.success) {
      showDetailModal.value = true
    }
  }

  /**
   * Cerrar modales
   */
  const closeCreateModal = () => {
    showCreateModal.value = false
    resetFormData()
    
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PedidosStore] Closed create modal')
    }
  }

  const closeDetailModal = () => {
    showDetailModal.value = false
    currentPedido.value = null
    
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PedidosStore] Closed detail modal')
    }
  }

  // ==========================================
  // ACTIONS - MANEJO DEL FORMULARIO
  // ==========================================
  
  /**
   * Establecer fechas por defecto
   */
  const setDefaultDates = () => {
    const defaultDates = pedidosApi.getDefaultDates()
    formData.value.fechaEmision = defaultDates.fechaEmision
    formData.value.fechaEntrega = defaultDates.fechaEntrega
  }

  /**
   * Establecer valores por defecto del formulario
   */
  const setDefaultFormData = () => {
    const defaultDates = pedidosApi.getDefaultDates()
    
    formData.value = {
      cotizacionId: '',
      cveProveedor: '',
      fechaEmision: defaultDates.fechaEmision,
      fechaEntrega: defaultDates.fechaEntrega,
      nivelSurtido: 100
    }
  }

  /**
   * Resetear datos del formulario
   */
  const resetFormData = () => {
    formData.value = {
      cotizacionId: '',
      cveProveedor: '',
      fechaEmision: '',
      fechaEntrega: '',
      nivelSurtido: 100
    }
    
    selectedCotizacion.value = null
    selectedProveedor.value = null
    pedidoPreview.value = null
  }

  /**
   * Manejar cambio de cotización seleccionada
   */
  const handleCotizacionChange = (cotizacionId) => {
    const cotizacion = cotizaciones.value.find(c => c.folio === parseInt(cotizacionId))
    selectedCotizacion.value = cotizacion
    formData.value.cotizacionId = cotizacionId
    updatePedidoPreview()
  }

  /**
   * Manejar cambio de proveedor seleccionado
   */
  const handleProveedorChange = (cveProveedor) => {
    const proveedor = proveedores.value.find(p => p.cve === cveProveedor)
    selectedProveedor.value = proveedor
    formData.value.cveProveedor = cveProveedor
    updatePedidoPreview()
  }

  /**
   * Manejar cambio de nivel de surtido
   */
  const handleNivelSurtidoChange = (nivelSurtido) => {
    formData.value.nivelSurtido = parseInt(nivelSurtido)
    updatePedidoPreview()
  }

  /**
   * Actualizar preview del pedido
   */
  const updatePedidoPreview = () => {
    if (selectedCotizacion.value && formData.value.nivelSurtido) {
      pedidoPreview.value = pedidosApi.calculatePedidoPreview(
        selectedCotizacion.value,
        formData.value.nivelSurtido
      )
    } else {
      pedidoPreview.value = null
    }
  }

  /**
   * Enviar formulario de pedido
   */
  const submitPedido = async () => {
    if (!isFormValid.value) {
      showAlert('error', 'Complete todos los campos requeridos')
      return { success: false }
    }

    const result = await generatePedido(formData.value)
    
    if (result.success) {
      closeCreateModal()
    }
    
    return result
  }

  // ==========================================
  // ACTIONS - FILTROS Y BÚSQUEDA
  // ==========================================
  
  /**
   * Aplicar filtros (migración de applyFilters)
   */
  const applyFilters = async () => {
    filteredPedidos.value = pedidosApi.applyFilters(pedidos.value, filters.value)

    // Reiniciar paginación al aplicar filtros
    pagination.value.currentPage = 1
    updatePaginationInfo()
    
    if (DEBUG_CONFIG.ENABLED) {
      // console.log(`[PedidosStore] Applied filters: ${filteredPedidos.value.length} results`)
    }
  }

  /**
   * Limpiar filtros (migración de clearFilters)
   */
  const clearFilters = () => {
    filters.value.searchTerm = ''
    filters.value.fechaDesde = ''
    filteredPedidos.value = [...pedidos.value]
    
    // Reiniciar paginación al limpiar filtros
    pagination.value.currentPage = 1
    updatePaginationInfo()
    
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PedidosStore] Filters cleared')
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
   * Establecer filtro de fecha
   */
  const setDateFilter = (fechaDesde) => {
    filters.value.fechaDesde = fechaDesde
    applyFilters()
  }

  // ==========================================
  // ACTIONS - PAGINACIÓN
  // ==========================================
  
  /**
   * Actualizar información de paginación (migración de updatePaginationInfo)
   */
  const updatePaginationInfo = () => {
    pagination.value.totalItems = filteredPedidos.value.length
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
        // console.log(`[PedidosStore] Navigated to page ${pageNumber}`)
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
      // console.log(`[PedidosStore] Page size changed to ${newPageSize}`)
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
   * Buscar pedido por número
   */
  const findPedidoByNumber = (numPedido) => {
    return pedidos.value.find(pedido => pedido.numPedido === parseInt(numPedido))
  }

  /**
   * Obtener resumen de pedido
   */
  const getPedidoSummary = (pedido) => {
    return pedidosApi.getPedidoSummary(pedido)
  }

  /**
   * Validar formulario actual
   */
  const validateCurrentForm = () => {
    return pedidosApi.validatePedido(formData.value)
  }

  /**
   * Validar fechas del formulario
   */
  const validateFormDates = () => {
    return pedidosApi.validateDates(formData.value.fechaEmision, formData.value.fechaEntrega)
  }

  // ==========================================
  // RETURN (API PÚBLICA DEL STORE)
  // ==========================================
  
  return {
    // Estado reactivo (readonly para prevenir mutaciones directas)
    pedidos: readonly(pedidos),
    filteredPedidos: readonly(filteredPedidos),
    cotizaciones: readonly(cotizaciones),
    proveedores: readonly(proveedores),
    currentPedido: readonly(currentPedido),
    showCreateModal,
    showDetailModal,
    formData,
    selectedCotizacion: readonly(selectedCotizacion),
    selectedProveedor: readonly(selectedProveedor),
    pedidoPreview: readonly(pedidoPreview),
    pagination: readonly(pagination),
    filters: readonly(filters),
    
    // Computed properties
    hasPedidos,
    hasFilteredPedidos,
    paginatedPedidos,
    paginationInfo,
    canGoPrevious,
    canGoNext,
    isFormValid,
    availableCotizaciones,
    availableProveedores,
    
    // Permisos
    canViewPedidos,
    canCreatePedidos,
    canEditPedidos,
    canDeletePedidos,
    canApprovePedidos,
    canChangePedidoStatus,
    canViewPedidoFulfillment,
    canManagePedidoFulfillment,
    canViewPedidoFinancialData,
    canGeneratePedidoReports,
    userRoles,
    isAdmin,
    primaryRole,
    
    // Loading states
    isFetching,
    isFetchingCotizaciones,
    isFetchingProveedores,
    isGeneratingPedido,
    isLoadingDetails,
    
    // Actions - Carga de datos
    fetchPedidos,
    fetchCotizaciones,
    fetchProveedores,
    initialize,
    
    // Actions - Operaciones de pedidos
    generatePedido,
    getPedidoDetails,
    
    // Actions - Modales
    openCreateModal,
    openDetailModal,
    closeCreateModal,
    closeDetailModal,
    
    // Actions - Formulario
    setDefaultDates,
    handleCotizacionChange,
    handleProveedorChange,
    handleNivelSurtidoChange,
    updatePedidoPreview,
    submitPedido,
    
    // Actions - Filtros
    applyFilters,
    clearFilters,
    setSearchFilter,
    setDateFilter,
    
    // Actions - Paginación
    goToPage,
    setPageSize,
    goToPreviousPage,
    goToNextPage,
    updatePaginationInfo,
    
    // Utilidades
    findPedidoByNumber,
    getPedidoSummary,
    validateCurrentForm,
    validateFormDates
  }
})