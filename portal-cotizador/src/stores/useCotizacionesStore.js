import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import { cotizacionesApi } from '@/services/cotizacionesApi'
import { componentesApi } from '@/services/componentesApi'
import { useUtils } from '@/composables/useUtils'
import { authService } from '@/services/authService'
import { useCrudOperations } from '@/composables/useAsyncOperation'
import { 
  UI_CONFIG, 
  MESSAGES, 
  COTIZADOR_TYPES,
  TAX_TYPES,
  COUNTRIES,
  DEBUG_CONFIG 
} from '@/utils/constants'

/**
 * Store de Cotizaciones - Migración completa del CotizacionesManager
 * Incluye gestión de cotizaciones, componentes, impuestos y cálculos en tiempo real
 */
export const useCotizacionesStore = defineStore('cotizaciones', () => {
  const { showAlert, validateRequired, validatePrice, formatCurrency, confirm } = useUtils()
  
  // Sistema de loading centralizado para cotizaciones
  const crudOps = useCrudOperations('cotización')

  // ==========================================
  // ESTADO REACTIVO (migración del constructor)
  // ==========================================
  
  const cotizaciones = ref([])
  const filteredCotizaciones = ref([])
  const currentCotizacion = ref(null)
  const currentComponents = ref([])
  const currentImpuestos = ref([])
  const availableComponents = ref([])
  const loading = ref(false)
  const tableLoading = ref(false)
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
    fechaFilter: ''
  })

  // Estado de UI para los modales
  const showModal = ref(false)
  const showViewModal = ref(false)
  const modalLoading = ref(false)

  // Estado del formulario
  const formData = ref({
    tipoCotizador: '',
    fecha: new Date().toISOString().split('T')[0],
    observaciones: ''
  })

  // Estado para agregar componentes
  const componentSelectValue = ref('')
  const componentQuantity = ref(1)

  // ==========================================
  // COMPUTED PROPERTIES (reactividad automática)
  // ==========================================
  
  const hasCotizaciones = computed(() => cotizaciones.value.length > 0)
  
  const hasFilteredCotizaciones = computed(() => filteredCotizaciones.value.length > 0)
  
  const paginatedCotizaciones = computed(() => {
    const startIndex = (pagination.value.currentPage - 1) * pagination.value.pageSize
    const endIndex = startIndex + pagination.value.pageSize
    return filteredCotizaciones.value.slice(startIndex, endIndex)
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

  // Computed para cálculos de la cotización actual
  const currentCotizacionTotals = computed(() => {
    return cotizacionesApi.calculateTotals(currentComponents.value, currentImpuestos.value)
  })

  const availableComponentsForSelect = computed(() => {
    // Filtrar componentes que no sean PCs para evitar problemas (validación defensiva)
    return (availableComponents.value || []).filter(comp => comp.tipoComponente !== 'PC')
  })

  // Computed para validación del formulario
  const isFormValid = computed(() => {
    if (isEditMode.value) return true
    
    return formData.value.tipoCotizador &&
           currentComponents.value.length > 0 &&
           currentImpuestos.value.length > 0 &&
           currentImpuestos.value.every(imp => imp.tipo && imp.pais && imp.tasa > 0)
  })

  // ==========================================
  // COMPUTED PROPERTIES - PERMISOS
  // ==========================================
  
  const canViewCotizaciones = computed(() => authService.canViewCotizaciones())
  const canViewCotizacionDetails = computed(() => authService.canViewCotizacionDetails())
  const canViewCotizacionCosts = computed(() => authService.canViewCotizacionCosts())
  const canCreateCotizaciones = computed(() => authService.canCreateCotizaciones())
  const canEditCotizaciones = computed(() => authService.canEditCotizaciones())
  const canDeleteCotizaciones = computed(() => authService.canDeleteCotizaciones())
  const canApproveCotizaciones = computed(() => authService.canApproveCotizaciones())
  const canModifyCotizacionPricing = computed(() => authService.canModifyCotizacionPricing())
  const canModifyCotizacionTaxes = computed(() => authService.canModifyCotizacionTaxes())
  const canViewCotizacionMargins = computed(() => authService.canViewCotizacionMargins())
  const canConvertCotizacionToOrder = computed(() => authService.canConvertCotizacionToOrder())
  const canExportCotizaciones = computed(() => authService.canExportCotizaciones())
  const canViewCotizacionReports = computed(() => authService.canViewCotizacionReports())
  const canViewCotizacionFinancialReports = computed(() => authService.canViewCotizacionFinancialReports())
  const userRoles = computed(() => authService.getUserRoles())
  const isAdmin = computed(() => authService.isAdmin())
  const primaryRole = computed(() => authService.getPrimaryRole())

  // ==========================================
  // COMPUTED PROPERTIES - LOADING STATES
  // ==========================================
  
  const isFetching = computed(() => crudOps.loadingStore.isOperationActive('fetch-cotización'))
  const isCreating = computed(() => crudOps.loadingStore.isOperationActive('create-cotización'))
  const isUpdating = computed(() => crudOps.loadingStore.isOperationActive('update-cotización'))
  const isDeleting = computed(() => crudOps.loadingStore.isOperationActive('delete-cotización'))

  // ==========================================
  // ACTIONS - CRUD OPERATIONS
  // ==========================================
  
  /**
   * Cargar todas las cotizaciones (migración de loadCotizaciones)
   */
  const fetchCotizaciones = async () => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[CotizacionesStore] Fetching cotizaciones...')
    }
    
    const result = await crudOps.fetch(async () => {
      const data = await cotizacionesApi.getAll()
      cotizaciones.value = data || []
      
      // Cargar componentes disponibles para modales (solo si está autenticado)
      if (authService.isAuthenticated()) {
        await loadAvailableComponents()
      }
      
      // Aplicar filtros existentes
      applyFilters()
      
      if (DEBUG_CONFIG.ENABLED) {
        // console.log(`[CotizacionesStore] Loaded ${cotizaciones.value.length} cotizaciones`)
      }
      
      return data
    })
    
    if (!result.success) {
      showAlert('error', result.error || MESSAGES.NETWORK_ERROR)
      cotizaciones.value = []
      filteredCotizaciones.value = []
    }
    
    return result
  }

  /**
   * Crear nueva cotización
   */
  const createCotizacion = async (cotizacionData) => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[CotizacionesStore] Creating cotización:', cotizacionData)
    }
    
    // Verificar permisos antes de proceder
    if (!authService.canCreateCotizaciones()) {
      const error = 'No tienes permisos para crear cotizaciones'
      showAlert('error', error)
      return { success: false, error }
    }
    
    const result = await crudOps.create(async () => {
      // Validar datos antes de enviar
      const validation = cotizacionesApi.validateCotizacion(cotizacionData)
      if (!validation.isValid) {
        throw new Error(validation.errors.join('\n'))
      }
      
      const response = await cotizacionesApi.create(cotizacionData)
      
      // Recargar cotizaciones para obtener datos actualizados
      await fetchCotizaciones()
      
      return response
    })
    
    if (result.success) {
      showAlert('success', MESSAGES.COTIZACION_CREATED)
    } else {
      showAlert('error', result.error || MESSAGES.OPERATION_ERROR)
    }
    
    return result
  }

  /**
   * Actualizar cotización existente
   */
  const updateCotizacion = async (id, cotizacionData) => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[CotizacionesStore] Updating cotización:', id, cotizacionData)
    }
    
    // Verificar permisos antes de proceder
    if (!authService.canEditCotizaciones()) {
      const error = 'No tienes permisos para editar cotizaciones'
      showAlert('error', error)
      return { success: false, error }
    }
    
    const result = await crudOps.update(async () => {
      const validation = cotizacionesApi.validateCotizacion(cotizacionData)
      if (!validation.isValid) {
        throw new Error(validation.errors.join('\n'))
      }
      
      const response = await cotizacionesApi.update(id, cotizacionData)
      
      // Recargar cotizaciones para obtener datos actualizados
      await fetchCotizaciones()
      
      return response
    })
    
    if (result.success) {
      showAlert('success', MESSAGES.COTIZACION_UPDATED)
    } else {
      showAlert('error', result.error || MESSAGES.OPERATION_ERROR)
    }
    
    return result
  }

  /**
   * Eliminar cotización
   */
  const deleteCotizacion = async (id) => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[CotizacionesStore] Deleting cotización:', id)
    }
    
    // Verificar permisos antes de proceder
    if (!authService.canDeleteCotizaciones()) {
      const error = 'No tienes permisos para eliminar cotizaciones'
      showAlert('error', error)
      return { success: false, error }
    }
    
    const result = await crudOps.remove(async () => {
      await cotizacionesApi.delete(id)
      
      // Recargar cotizaciones para obtener datos actualizados
      await fetchCotizaciones()
      
      return true
    })
    
    if (result.success) {
      showAlert('success', MESSAGES.COTIZACION_DELETED)
    } else {
      showAlert('error', result.error || MESSAGES.OPERATION_ERROR)
    }
    
    return result
  }

  // ==========================================
  // ACTIONS - GESTIÓN DE MODALES
  // ==========================================
  
  /**
   * Abrir modal para crear nueva cotización (migración de openCreateModal)
   */
  const openCreateModal = async () => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[CotizacionesStore] Opening create modal')
    }
    
    try {
      modalLoading.value = true
      
      // Cargar componentes disponibles (solo si está autenticado)
      if (authService.isAuthenticated()) {
        await loadAvailableComponents()
      }
      
      // Resetear estado
      isEditMode.value = false
      currentCotizacion.value = null
      currentComponents.value = []
      currentImpuestos.value = []
      
      // Configurar valores por defecto
      formData.value = {
        tipoCotizador: '',
        fecha: new Date().toISOString().split('T')[0]
      }
      
      // Configurar impuesto por defecto (IVA México 16%)
      setupDefaultImpuesto()
      
      // Limpiar formulario de componentes
      componentSelectValue.value = ''
      componentQuantity.value = 1
      
      showModal.value = true
      
    } catch (error) {
      console.error('[CotizacionesStore] Error opening create modal:', error)
      showAlert('error', error.message || MESSAGES.OPERATION_ERROR)
    } finally {
      modalLoading.value = false
    }
  }

  /**
   * Abrir modal para ver cotización existente (migración de openViewModal)
   */
  const openViewModal = async (cotizacionId) => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[CotizacionesStore] Opening view modal for cotización:', cotizacionId)
    }
    
    try {
      modalLoading.value = true
      
      // Obtener información de la cotización
      const cotizacion = await cotizacionesApi.getById(cotizacionId)
      currentCotizacion.value = cotizacion
      
      // Configurar modo vista
      isEditMode.value = true
      
      // Cargar datos de la cotización
      currentComponents.value = cotizacion.detalles || []
      currentImpuestos.value = cotizacion.impuestosAplicados || []
      
      // Configurar formulario con datos de la cotización
      formData.value = {
        tipoCotizador: cotizacion.tipoCotizador || 'Estándar',
        fecha: cotizacion.fecha ? cotizacion.fecha.split('T')[0] : new Date().toISOString().split('T')[0]
      }
      
      showViewModal.value = true
      
    } catch (error) {
      console.error('[CotizacionesStore] Error opening view modal:', error)
      showAlert('error', error.message || MESSAGES.OPERATION_ERROR)
    } finally {
      modalLoading.value = false
    }
  }

  /**
   * Cerrar modales
   */
  const closeModal = () => {
    showModal.value = false
    showViewModal.value = false
    currentCotizacion.value = null
    currentComponents.value = []
    currentImpuestos.value = []
    isEditMode.value = false
    componentSelectValue.value = ''
    componentQuantity.value = 1
    
    // Resetear formData
    formData.value = {
      tipoCotizador: '',
      fecha: new Date().toISOString().split('T')[0],
      observaciones: ''
    }
    
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[CotizacionesStore] Closed modal')
    }
  }

  // ==========================================
  // ACTIONS - GESTIÓN DE COMPONENTES
  // ==========================================
  
  /**
   * Cargar componentes disponibles (migración de loadAvailableComponents)
   */
  const loadAvailableComponents = async () => {
    // Verificar autenticación antes de hacer la llamada API
    if (!authService.isAuthenticated()) {
      if (DEBUG_CONFIG.ENABLED) {
        // console.log('[CotizacionesStore] Usuario no autenticado, omitiendo carga de componentes')
      }
      availableComponents.value = []
      return
    }
    
    try {
      const allComponents = await componentesApi.getAll()
      // Filtrar solo componentes que no sean PCs (validación defensiva)
      availableComponents.value = (allComponents || []).filter(comp => comp.tipoComponente !== 'PC')
      
      if (DEBUG_CONFIG.ENABLED) {
        // console.log(`[CotizacionesStore] Loaded ${availableComponents.value.length} available components`)
      }
      
    } catch (error) {
      console.error('[CotizacionesStore] Error loading available components:', error)
      availableComponents.value = []
    }
  }

  /**
   * Agregar componente a la cotización (migración de handleAddComponent)
   */
  const addComponentToCotizacion = async (componentId, quantity = 1) => {
    if (!componentId) {
      showAlert('error', 'Seleccione un componente para agregar')
      return { success: false }
    }

    try {
      const component = availableComponents.value.find(c => c.id === componentId)
      if (!component) {
        showAlert('error', 'Componente no encontrado')
        return { success: false }
      }

      // Verificar si ya existe
      const existingIndex = currentComponents.value.findIndex(c => 
        (c.componenteId || c.idComponente) === componentId
      )
      
      if (existingIndex >= 0) {
        // Actualizar cantidad
        currentComponents.value[existingIndex].cantidad += quantity
      } else {
        // Agregar nuevo
        currentComponents.value.push({
          componenteId: component.id,
          idComponente: component.id,
          descripcion: component.descripcion,
          nombreComponente: component.descripcion,
          cantidad: quantity,
          precioUnitario: component.precioBase,
          precioBase: component.precioBase
        })
      }

      showAlert('success', MESSAGES.COTIZACION_COMPONENT_ADDED)
      
      // Limpiar formulario
      componentSelectValue.value = ''
      componentQuantity.value = 1
      
      return { success: true }
      
    } catch (error) {
      console.error('[CotizacionesStore] Error adding component:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    }
  }

  /**
   * Quitar componente de la cotización
   */
  const removeComponentFromCotizacion = async (index) => {
    const confirmed = await confirm(
      'Quitar Componente',
      '¿Está seguro de que desea quitar este componente de la cotización?'
    )

    if (!confirmed) {
      return { success: false }
    }

    try {
      currentComponents.value.splice(index, 1)
      showAlert('success', MESSAGES.COTIZACION_COMPONENT_REMOVED)
      return { success: true }
      
    } catch (error) {
      console.error('[CotizacionesStore] Error removing component:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    }
  }

  // ==========================================
  // ACTIONS - GESTIÓN DE IMPUESTOS
  // ==========================================
  
  /**
   * Configurar impuesto por defecto (IVA México 16%)
   */
  const setupDefaultImpuesto = () => {
    currentImpuestos.value = [{
      tipo: TAX_TYPES.IVA,
      pais: COUNTRIES.MX,
      tasa: 16.00
    }]
    
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[CotizacionesStore] Setup default tax (IVA 16%)')
    }
  }

  /**
   * Agregar nuevo impuesto
   */
  const addImpuesto = () => {
    // Verificar permisos para modificar impuestos
    if (!authService.canModifyCotizacionTaxes()) {
      showAlert('error', 'No tienes permisos para modificar impuestos')
      return { success: false, error: 'Sin permisos para modificar impuestos' }
    }

    currentImpuestos.value.push({
      tipo: '',
      pais: '',
      tasa: 0
    })
    
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[CotizacionesStore] Added new tax item')
    }
    
    return { success: true }
  }

  /**
   * Quitar impuesto
   */
  const removeImpuesto = async (index) => {
    // Verificar permisos para modificar impuestos
    if (!authService.canModifyCotizacionTaxes()) {
      showAlert('error', 'No tienes permisos para modificar impuestos')
      return { success: false, error: 'Sin permisos para modificar impuestos' }
    }

    const confirmed = await confirm(
      'Quitar Impuesto',
      '¿Está seguro de que desea quitar este impuesto?'
    )

    if (!confirmed) {
      return { success: false }
    }

    try {
      currentImpuestos.value.splice(index, 1)
      return { success: true }
      
    } catch (error) {
      console.error('[CotizacionesStore] Error removing tax:', error)
      return { success: false, error: error.message }
    }
  }

  /**
   * Actualizar impuesto
   */
  const updateImpuesto = (index, impuestoData) => {
    // Verificar permisos para modificar impuestos
    if (!authService.canModifyCotizacionTaxes()) {
      showAlert('error', 'No tienes permisos para modificar impuestos')
      return { success: false, error: 'Sin permisos para modificar impuestos' }
    }

    if (index >= 0 && index < currentImpuestos.value.length) {
      currentImpuestos.value[index] = { ...impuestoData }
      return { success: true }
    }
    
    return { success: false, error: 'Índice de impuesto inválido' }
  }

  // ==========================================
  // ACTIONS - ENVÍO DEL FORMULARIO
  // ==========================================
  
  /**
   * Enviar formulario de cotización (migración de handleSubmitCotizacion)
   */
  const submitCotizacion = async () => {
    if (isEditMode.value) {
      closeModal()
      return { success: true }
    }

    if (!isFormValid.value) {
      showAlert('error', 'Complete todos los campos requeridos')
      return { success: false }
    }

    try {
      modalLoading.value = true
      
      const cotizacionData = {
        tipoCotizador: formData.value.tipoCotizador,
        impuestos: (currentImpuestos.value || [])
          .filter(imp => imp.tipo && imp.pais && imp.tasa > 0)
          .map(imp => imp.tipo), // Convertir a array de strings según API spec
        detalles: currentComponents.value.map(comp => ({
          idComponente: comp.componenteId || comp.idComponente,
          cantidad: comp.cantidad,
          descripcion: comp.descripcion,
          precioBase: comp.precioUnitario || comp.precioBase
        })),
        observaciones: formData.value.observaciones || ""
      }

      const result = await createCotizacion(cotizacionData)
      
      if (result.success) {
        closeModal()
      }
      
      return result
      
    } catch (error) {
      console.error('[CotizacionesStore] Error submitting cotización:', error)
      return { success: false, error: error.message }
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
    const fechaFilter = filters.value.fechaFilter
    
    filteredCotizaciones.value = (cotizaciones.value || []).filter(cotizacion => {
      // Usar folio en lugar de id y manejar tipoCotizador por defecto
      const cotizacionId = cotizacion.folio || cotizacion.id || ''
      const tipoCotizador = cotizacion.tipoCotizador || 'Estándar'
      
      // Filtro de búsqueda
      const matchesSearch = !searchTerm || 
        cotizacionId.toString().toLowerCase().includes(searchTerm) ||
        tipoCotizador.toLowerCase().includes(searchTerm)

      // Filtro de fecha
      let matchesDate = true
      if (fechaFilter) {
        const fechaCotizacion = cotizacion.fecha || cotizacion.fechaCreacion
        if (fechaCotizacion) {
          const cotizacionDate = fechaCotizacion.includes('T') ? 
            fechaCotizacion.split('T')[0] : fechaCotizacion
          matchesDate = cotizacionDate === fechaFilter
        }
      }

      return matchesSearch && matchesDate
    })

    // Reiniciar paginación al aplicar filtros
    pagination.value.currentPage = 1
    updatePaginationInfo()
    
    if (DEBUG_CONFIG.ENABLED) {
      // console.log(`[CotizacionesStore] Applied filters: ${filteredCotizaciones.value.length} results`)
    }
  }

  /**
   * Limpiar filtros (migración de clearFilters)
   */
  const clearFilters = () => {
    filters.value.searchTerm = ''
    filters.value.fechaFilter = ''
    applyFilters()
    
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[CotizacionesStore] Filters cleared')
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
  const setDateFilter = (fecha) => {
    filters.value.fechaFilter = fecha
    applyFilters()
  }

  // ==========================================
  // ACTIONS - PAGINACIÓN
  // ==========================================
  
  /**
   * Actualizar información de paginación (migración de updatePaginationInfo)
   */
  const updatePaginationInfo = () => {
    pagination.value.totalItems = filteredCotizaciones.value.length
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
        // console.log(`[CotizacionesStore] Navigated to page ${pageNumber}`)
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
      // console.log(`[CotizacionesStore] Page size changed to ${newPageSize}`)
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
   * Buscar cotización por ID
   */
  const findCotizacionById = (id) => {
    return cotizaciones.value.find(cot => cot.folio === id || cot.id === id)
  }

  /**
   * Verificar si existe una cotización con el ID
   */
  const cotizacionExists = async (id) => {
    try {
      return await cotizacionesApi.exists(id)
    } catch (error) {
      console.error('[CotizacionesStore] Error checking if cotización exists:', error)
      return false
    }
  }

  /**
   * Obtener resumen de cotización
   */
  const getCotizacionSummary = (cotizacion) => {
    const cotizacionId = cotizacion.folio || cotizacion.id
    const totalComponents = cotizacion.detalles ? cotizacion.detalles.length : 0
    const fecha = cotizacion.fecha || cotizacion.fechaCreacion || 'N/A'
    
    return {
      id: cotizacionId,
      fecha: fecha !== 'N/A' ? fecha.split('T')[0] : 'N/A',
      tipoCotizador: cotizacion.tipoCotizador || 'Estándar',
      totalComponents,
      subtotal: parseFloat(cotizacion.subtotal || 0),
      impuestos: parseFloat(cotizacion.impuestos || cotizacion.totalImpuestos || 0),
      total: parseFloat(cotizacion.total || 0)
    }
  }

  // ==========================================
  // RETURN (API PÚBLICA DEL STORE)
  // ==========================================
  
  return {
    // Estado reactivo (readonly para prevenir mutaciones directas)
    cotizaciones: readonly(cotizaciones),
    filteredCotizaciones: readonly(filteredCotizaciones),
    currentCotizacion: readonly(currentCotizacion),
    currentComponents,
    currentImpuestos,
    availableComponents: readonly(availableComponents),
    loading: readonly(loading),
    tableLoading: readonly(tableLoading),
    modalLoading: readonly(modalLoading),
    isEditMode: readonly(isEditMode),
    showModal,
    showViewModal,
    formData,
    componentSelectValue,
    componentQuantity,
    pagination: readonly(pagination),
    filters: readonly(filters),
    
    // Computed properties
    hasCotizaciones,
    hasFilteredCotizaciones,
    paginatedCotizaciones,
    paginationInfo,
    canGoPrevious,
    canGoNext,
    currentCotizacionTotals,
    availableComponentsForSelect,
    isFormValid,
    
    // Permisos
    canViewCotizaciones,
    canViewCotizacionDetails,
    canViewCotizacionCosts,
    canCreateCotizaciones,
    canEditCotizaciones,
    canDeleteCotizaciones,
    canApproveCotizaciones,
    canModifyCotizacionPricing,
    canModifyCotizacionTaxes,
    canViewCotizacionMargins,
    canConvertCotizacionToOrder,
    canExportCotizaciones,
    canViewCotizacionReports,
    canViewCotizacionFinancialReports,
    userRoles,
    isAdmin,
    primaryRole,
    
    // Loading states
    isFetching,
    isCreating,
    isUpdating,
    isDeleting,
    
    // Actions - CRUD
    fetchCotizaciones,
    createCotizacion,
    updateCotizacion,
    deleteCotizacion,
    
    // Actions - Modales
    openCreateModal,
    openViewModal,
    closeModal,
    
    // Actions - Componentes
    loadAvailableComponents,
    addComponentToCotizacion,
    removeComponentFromCotizacion,
    
    // Actions - Impuestos
    setupDefaultImpuesto,
    addImpuesto,
    removeImpuesto,
    updateImpuesto,
    
    // Actions - Formulario
    submitCotizacion,
    
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
    
    // Utilidades
    findCotizacionById,
    cotizacionExists,
    getCotizacionSummary
  }
})