import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import { promocionesApi } from '@/services/promocionesApi'
import { useUtils } from '@/composables/useUtils'
import { useAuthStore } from '@/stores/useAuthStore'
import { 
  UI_CONFIG, 
  MESSAGES, 
  DEBUG_CONFIG 
} from '@/utils/constants'

/**
 * Store de Promociones - Migración completa del PromocionesManager
 * Incluye gestión de promociones complejas con múltiples tipos y validaciones
 */
export const usePromocionesStore = defineStore('promociones', () => {
  const { showAlert, validateRequired, confirm } = useUtils()

  // ==========================================
  // ESTADO REACTIVO (migración del constructor)
  // ==========================================
  
  const promociones = ref([])
  const filteredPromociones = ref([])
  const currentPromocion = ref(null)
  const loading = ref(false)
  const tableLoading = ref(false)
  const modalLoading = ref(false)
  const isEditMode = ref(false)
  
  // Configuración de paginación (migración exacta)
  const pagination = ref({
    currentPage: 1,
    pageSize: 10, // PromocionesManager usa 10 por defecto
    totalPages: 0,
    totalItems: 0
  })
  
  // Filtros de búsqueda y estado
  const filters = ref({
    searchTerm: '',
    estadoFilter: ''
  })

  // Estado de UI para modales
  const showCreateModal = ref(false)
  const showDetailModal = ref(false)

  // Estado del formulario complejo
  const formData = ref({
    nombre: '',
    descripcion: '',
    vigenciaDesde: '',
    vigenciaHasta: '',
    tipo: '',
    // Configuraciones específicas por tipo
    montoDescuento: 0,
    porcentajeDescuento: 0,
    cantidadMinima: 0,
    nCompras: 0,
    mPago: 0
  })

  // Estado del preview
  const previewData = ref(null)
  const showPreview = ref(false)

  // ==========================================
  // COMPUTED PROPERTIES (reactividad automática)
  // ==========================================
  
  const hasPromociones = computed(() => promociones.value.length > 0)
  
  const hasFilteredPromociones = computed(() => filteredPromociones.value.length > 0)
  
  const paginatedPromociones = computed(() => {
    const startIndex = (pagination.value.currentPage - 1) * pagination.value.pageSize
    const endIndex = startIndex + pagination.value.pageSize
    return filteredPromociones.value.slice(startIndex, endIndex)
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
    const basicValid = formData.value.nombre &&
                      formData.value.descripcion &&
                      formData.value.vigenciaDesde &&
                      formData.value.vigenciaHasta &&
                      formData.value.tipo

    if (!basicValid) return false

    // Validar fechas
    const fechaInicio = new Date(formData.value.vigenciaDesde)
    const fechaFin = new Date(formData.value.vigenciaHasta)
    if (fechaFin <= fechaInicio) return false

    // Validar configuración específica por tipo
    const tipoValidation = promocionesApi.validateTipoConfig(formData.value)
    return tipoValidation.isValid
  })

  // Computed para el título del modal
  const modalTitle = computed(() => {
    return isEditMode.value ? 'Editar Promoción' : 'Nueva Promoción'
  })

  // Computed para opciones de tipos de promoción
  const tiposPromocion = computed(() => {
    return promocionesApi.getTiposPromocion()
  })

  // Computed para opciones de estados
  const estadosPromocion = computed(() => {
    return promocionesApi.getEstadosPromocion()
  })

  // Computed para mostrar configuración específica por tipo
  const showTipoConfig = computed(() => {
    return (tipo) => formData.value.tipo === tipo
  })

  // ==========================================
  // ACTIONS - OPERACIONES DE CARGA
  // ==========================================
  
  /**
   * Cargar todas las promociones (migración de loadPromociones)
   */
  const fetchPromociones = async () => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PromocionesStore] Fetching promociones...')
    }
    
    try {
      loading.value = true
      tableLoading.value = true
      
      const data = await promocionesApi.getAll()
      promociones.value = data || []
      
      // Aplicar filtros existentes
      await applyFilters()
      
      if (DEBUG_CONFIG.ENABLED) {
        console.log(`[PromocionesStore] Loaded ${promociones.value.length} promociones`)
      }
      
    } catch (error) {
      console.error('[PromocionesStore] Error fetching promociones:', error)
      showAlert('error', error.message || MESSAGES.NETWORK_ERROR)
      promociones.value = []
      filteredPromociones.value = []
    } finally {
      loading.value = false
      tableLoading.value = false
    }
  }

  // ==========================================
  // ACTIONS - CRUD OPERATIONS
  // ==========================================
  
  /**
   * Crear nueva promoción
   */
  const createPromocion = async (promocionData) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PromocionesStore] Creating promocion:', promocionData)
    }
    
    try {
      loading.value = true
      
      // Validar datos antes de enviar
      const validation = promocionesApi.validatePromocion(promocionData)
      if (!validation.isValid) {
        showAlert('error', validation.errors.join('\n'))
        return { success: false, errors: validation.errors }
      }
      
      // Construir payload completo
      const payload = promocionesApi.buildPromocionPayload(promocionData)
      
      const response = await promocionesApi.create(payload)
      
      showAlert('success', MESSAGES.PROMOCION_CREATED)
      
      // Recargar promociones para obtener datos actualizados
      await fetchPromociones()
      
      return { success: true, data: response }
      
    } catch (error) {
      console.error('[PromocionesStore] Error creating promocion:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  /**
   * Actualizar promoción existente
   */
  const updatePromocion = async (id, promocionData) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PromocionesStore] Updating promocion:', id, promocionData)
    }
    
    try {
      loading.value = true
      
      // Validar datos antes de enviar
      const validation = promocionesApi.validatePromocion(promocionData)
      if (!validation.isValid) {
        showAlert('error', validation.errors.join('\n'))
        return { success: false, errors: validation.errors }
      }
      
      // Construir payload completo
      const payload = promocionesApi.buildPromocionPayload(promocionData)
      
      const response = await promocionesApi.update(id, payload)
      
      showAlert('success', MESSAGES.PROMOCION_UPDATED)
      
      // Recargar promociones para obtener datos actualizados
      await fetchPromociones()
      
      return { success: true, data: response }
      
    } catch (error) {
      console.error('[PromocionesStore] Error updating promocion:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  /**
   * Eliminar promoción
   */
  const deletePromocion = async (id) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PromocionesStore] Deleting promocion:', id)
    }
    
    try {
      loading.value = true
      
      await promocionesApi.delete(id)
      
      showAlert('success', MESSAGES.PROMOCION_DELETED)
      
      // Recargar promociones para obtener datos actualizados
      await fetchPromociones()
      
      return { success: true }
      
    } catch (error) {
      console.error('[PromocionesStore] Error deleting promocion:', error)
      const errorMessage = error.message || MESSAGES.OPERATION_ERROR
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  /**
   * Obtener detalles de una promoción específica
   */
  const getPromocionDetails = async (id) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PromocionesStore] Getting promocion details:', id)
    }
    
    try {
      modalLoading.value = true
      
      const promocion = await promocionesApi.getById(id)
      currentPromocion.value = promocion
      
      return { success: true, data: promocion }
      
    } catch (error) {
      console.error('[PromocionesStore] Error getting promocion details:', error)
      const errorMessage = error.message || MESSAGES.PROMOCION_NOT_FOUND
      showAlert('error', errorMessage)
      return { success: false, error: errorMessage }
    } finally {
      modalLoading.value = false
    }
  }

  // ==========================================
  // ACTIONS - GESTIÓN DE MODALES
  // ==========================================
  
  /**
   * Abrir modal para crear nueva promoción (migración de showModal)
   */
  const openCreateModal = () => {
    const authStore = useAuthStore()
    
    // Verificar autenticación directamente
    if (!authStore.isLoggedIn) {
      // El router guard ya se encargará de redirigir a login
      return
    }
    
    // Si está autenticado, ejecutar directamente
    // Resetear estado
    isEditMode.value = false
    currentPromocion.value = null
    
    // Configurar valores por defecto
    setDefaultFormData()
    
    showCreateModal.value = true
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PromocionesStore] Opened create modal')
    }
  }

  /**
   * Abrir modal para editar promoción existente
   */
  const openEditModal = async (id) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PromocionesStore] Opening edit modal for promocion:', id)
    }
    
    try {
      modalLoading.value = true
      
      // Obtener información de la promoción
      const promocion = await promocionesApi.getById(id)
      currentPromocion.value = promocion
      
      // Configurar modo edición
      isEditMode.value = true
      
      // Configurar formulario con datos de la promoción
      populateFormWithPromocion(promocion)
      
      showCreateModal.value = true
      
    } catch (error) {
      console.error('[PromocionesStore] Error opening edit modal:', error)
      showAlert('error', error.message || MESSAGES.OPERATION_ERROR)
    } finally {
      modalLoading.value = false
    }
  }

  /**
   * Abrir modal para ver detalles de promoción
   */
  const openDetailModal = async (id) => {
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PromocionesStore] Opening detail modal for promocion:', id)
    }
    
    const result = await getPromocionDetails(id)
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
    hidePreview()
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PromocionesStore] Closed create modal')
    }
  }

  const closeDetailModal = () => {
    showDetailModal.value = false
    currentPromocion.value = null
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PromocionesStore] Closed detail modal')
    }
  }

  // ==========================================
  // ACTIONS - MANEJO DEL FORMULARIO
  // ==========================================
  
  /**
   * Establecer fechas por defecto
   */
  const setDefaultDates = () => {
    const defaultDates = promocionesApi.getDefaultDates()
    formData.value.vigenciaDesde = defaultDates.vigenciaDesde
    formData.value.vigenciaHasta = defaultDates.vigenciaHasta
  }

  /**
   * Establecer valores por defecto del formulario
   */
  const setDefaultFormData = () => {
    const defaultDates = promocionesApi.getDefaultDates()
    
    formData.value = {
      nombre: '',
      descripcion: '',
      vigenciaDesde: defaultDates.vigenciaDesde,
      vigenciaHasta: defaultDates.vigenciaHasta,
      tipo: '',
      montoDescuento: 0,
      porcentajeDescuento: 0,
      cantidadMinima: 0,
      nCompras: 0,
      mPago: 0
    }
  }

  /**
   * Resetear datos del formulario
   */
  const resetFormData = () => {
    formData.value = {
      nombre: '',
      descripcion: '',
      vigenciaDesde: '',
      vigenciaHasta: '',
      tipo: '',
      montoDescuento: 0,
      porcentajeDescuento: 0,
      cantidadMinima: 0,
      nCompras: 0,
      mPago: 0
    }
    
    currentPromocion.value = null
    isEditMode.value = false
  }

  /**
   * Poblar formulario con datos de promoción existente
   */
  const populateFormWithPromocion = (promocion) => {
    formData.value.nombre = promocion.nombre || ''
    formData.value.descripcion = promocion.descripcion || ''
    formData.value.vigenciaDesde = promocion.vigenciaDesde || ''
    formData.value.vigenciaHasta = promocion.vigenciaHasta || ''
    
    // Determinar tipo principal y configuraciones
    if (promocion.detalles && promocion.detalles.length > 0) {
      const detalleBase = promocion.detalles.find(d => d.esBase) || promocion.detalles[0]
      
      if (detalleBase.tipoBase === 'NXM') {
        formData.value.tipo = 'NXM'
        formData.value.nCompras = detalleBase.llevent || 0
        formData.value.mPago = detalleBase.paguen || 0
      } else if (detalleBase.tipoAcumulable === 'DESCUENTO_PLANO') {
        if (detalleBase.porcentajeDescuentoPlano < 1) {
          formData.value.tipo = 'DESCUENTO_PLANO'
          formData.value.montoDescuento = detalleBase.porcentajeDescuentoPlano
        } else {
          formData.value.tipo = 'DESCUENTO_PORCENTUAL'
          formData.value.porcentajeDescuento = detalleBase.porcentajeDescuentoPlano
        }
      }
    }

    updatePreview()
  }

  /**
   * Manejar cambio de tipo de promoción
   */
  const handleTipoChange = (tipo) => {
    formData.value.tipo = tipo
    
    // Resetear valores específicos del tipo anterior
    formData.value.montoDescuento = 0
    formData.value.porcentajeDescuento = 0
    formData.value.cantidadMinima = 0
    formData.value.nCompras = 0
    formData.value.mPago = 0
    
    updatePreview()
  }

  /**
   * Validar fechas del formulario
   */
  const validateFormDates = () => {
    return promocionesApi.validateDates(formData.value.vigenciaDesde, formData.value.vigenciaHasta)
  }

  /**
   * Actualizar preview de promoción
   */
  const updatePreview = () => {
    if (isFormValid.value) {
      previewData.value = promocionesApi.generatePreviewContent(formData.value)
      showPreview.value = true
    } else {
      hidePreview()
    }
  }

  /**
   * Ocultar preview
   */
  const hidePreview = () => {
    showPreview.value = false
    previewData.value = null
  }

  /**
   * Enviar formulario de promoción
   */
  const submitPromocion = async () => {
    if (!isFormValid.value) {
      showAlert('error', 'Complete todos los campos requeridos correctamente')
      return { success: false }
    }

    try {
      modalLoading.value = true
      
      let result
      
      if (isEditMode.value && currentPromocion.value) {
        // Actualizar promoción existente
        result = await updatePromocion(currentPromocion.value.idPromocion, formData.value)
      } else {
        // Crear nueva promoción
        result = await createPromocion(formData.value)
      }
      
      if (result.success) {
        closeCreateModal()
      }
      
      return result
      
    } catch (error) {
      console.error('[PromocionesStore] Error submitting promocion:', error)
      return { success: false, error: error.message }
    } finally {
      modalLoading.value = false
    }
  }

  /**
   * Confirmar y eliminar promoción
   */
  const confirmDeletePromocion = async (id) => {
    const promocion = promociones.value.find(p => p.idPromocion === id)
    const nombre = promocion ? promocion.nombre : `ID ${id}`

    const confirmed = await confirm(
      'Eliminar Promoción',
      `¿Está seguro de que desea eliminar la promoción "${nombre}"?\n\nEsta acción no se puede deshacer.`
    )

    if (!confirmed) {
      return { success: false }
    }

    return await deletePromocion(id)
  }

  // ==========================================
  // ACTIONS - FILTROS Y BÚSQUEDA
  // ==========================================
  
  /**
   * Aplicar filtros (migración de applyFilters)
   */
  const applyFilters = async () => {
    filteredPromociones.value = promocionesApi.applyFilters(promociones.value, filters.value)

    // Reiniciar paginación al aplicar filtros
    pagination.value.currentPage = 1
    updatePaginationInfo()
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log(`[PromocionesStore] Applied filters: ${filteredPromociones.value.length} results`)
    }
  }

  /**
   * Limpiar filtros (migración de clearFilters)
   */
  const clearFilters = () => {
    filters.value.searchTerm = ''
    filters.value.estadoFilter = ''
    filteredPromociones.value = [...promociones.value]
    
    // Reiniciar paginación al limpiar filtros
    pagination.value.currentPage = 1
    updatePaginationInfo()
    
    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PromocionesStore] Filters cleared')
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
   * Establecer filtro de estado
   */
  const setEstadoFilter = (estado) => {
    filters.value.estadoFilter = estado
    applyFilters()
  }

  // ==========================================
  // ACTIONS - PAGINACIÓN
  // ==========================================
  
  /**
   * Actualizar información de paginación (migración de updatePaginationInfo)
   */
  const updatePaginationInfo = () => {
    pagination.value.totalItems = filteredPromociones.value.length
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
        console.log(`[PromocionesStore] Navigated to page ${pageNumber}`)
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
      console.log(`[PromocionesStore] Page size changed to ${newPageSize}`)
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
   * Buscar promoción por ID
   */
  const findPromocionById = (id) => {
    return promociones.value.find(promocion => promocion.idPromocion === parseInt(id))
  }

  /**
   * Obtener resumen de promoción
   */
  const getPromocionSummary = (promocion) => {
    return promocionesApi.getPromocionSummary(promocion)
  }

  /**
   * Validar formulario actual
   */
  const validateCurrentForm = () => {
    return promocionesApi.validatePromocion(formData.value)
  }

  /**
   * Obtener badge de estado
   */
  const getEstadoBadge = (estado) => {
    return promocionesApi.getEstadoBadge(estado)
  }

  /**
   * Formatear fecha para display
   */
  const formatDate = (dateString) => {
    return promocionesApi.formatDate(dateString)
  }

  /**
   * Truncar texto para display
   */
  const truncateText = (text, maxLength) => {
    return promocionesApi.truncateText(text, maxLength)
  }

  /**
   * Obtener nombre de display para tipo
   */
  const getTipoDisplayName = (tipo) => {
    return promocionesApi.getTipoDisplayName(tipo)
  }

  // ==========================================
  // RETURN (API PÚBLICA DEL STORE)
  // ==========================================
  
  return {
    // Estado reactivo (readonly para prevenir mutaciones directas)
    promociones: readonly(promociones),
    filteredPromociones: readonly(filteredPromociones),
    currentPromocion: readonly(currentPromocion),
    loading: readonly(loading),
    tableLoading: readonly(tableLoading),
    modalLoading: readonly(modalLoading),
    isEditMode: readonly(isEditMode),
    showCreateModal,
    showDetailModal,
    formData,
    previewData: readonly(previewData),
    showPreview: readonly(showPreview),
    pagination: readonly(pagination),
    filters: readonly(filters),
    
    // Computed properties
    hasPromociones,
    hasFilteredPromociones,
    paginatedPromociones,
    paginationInfo,
    canGoPrevious,
    canGoNext,
    isFormValid,
    modalTitle,
    tiposPromocion,
    estadosPromocion,
    showTipoConfig,
    
    // Actions - Carga de datos
    fetchPromociones,
    
    // Actions - CRUD
    createPromocion,
    updatePromocion,
    deletePromocion,
    getPromocionDetails,
    confirmDeletePromocion,
    
    // Actions - Modales
    openCreateModal,
    openEditModal,
    openDetailModal,
    closeCreateModal,
    closeDetailModal,
    
    // Actions - Formulario
    setDefaultDates,
    handleTipoChange,
    validateFormDates,
    updatePreview,
    hidePreview,
    submitPromocion,
    
    // Actions - Filtros
    applyFilters,
    clearFilters,
    setSearchFilter,
    setEstadoFilter,
    
    // Actions - Paginación
    goToPage,
    setPageSize,
    goToPreviousPage,
    goToNextPage,
    updatePaginationInfo,
    
    // Utilidades
    findPromocionById,
    getPromocionSummary,
    validateCurrentForm,
    getEstadoBadge,
    formatDate,
    truncateText,
    getTipoDisplayName
  }
})