import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import { promocionesApi } from '@/services/promocionesApi'
import { useUtils } from '@/composables/useUtils'
import { useAuthStore } from '@/stores/useAuthStore'
import { authService } from '@/services/authService'
import { useCrudOperations } from '@/composables/useAsyncOperation'
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
  
  // Usar el sistema de loading centralizado
  const crudOps = useCrudOperations('promocion')

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
    porcentajeDescuento: 0,
    cantidadMinima: 0,
    nCompras: 0,
    mPago: 0,
    // Nuevos campos para escalas múltiples
    escalas: [] // Array de { cantidadMinima, cantidadMaxima, porcentaje }
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

  // ==========================================
  // COMPUTED PROPERTIES - PERMISOS
  // ==========================================
  
  const canViewPromociones = computed(() => authService.canViewPromociones())
  const canCreatePromociones = computed(() => authService.canCreatePromociones())
  const canEditPromociones = computed(() => authService.canEditPromociones())
  const canDeletePromociones = computed(() => authService.canDeletePromociones())
  const canApplyPromociones = computed(() => authService.canApplyPromociones())
  const canViewPromocionFinancialImpact = computed(() => authService.canViewPromocionFinancialImpact())
  const canManagePromocionStacking = computed(() => authService.canManagePromocionStacking())
  const canViewPromocionReports = computed(() => authService.canViewPromocionReports())
  const userRoles = computed(() => authService.getUserRoles())
  const isAdmin = computed(() => authService.isAdmin())
  const primaryRole = computed(() => authService.getPrimaryRole())
  
  // Computed para permisos de usuario (mantener compatibilidad)
  const userPermissions = computed(() => ({
    view: authService.canViewPromociones(),
    create: authService.canCreatePromociones(),
    edit: authService.canEditPromociones(),
    delete: authService.canDeletePromociones(),
    apply: authService.canApplyPromociones(),
    viewFinancialImpact: authService.canViewPromocionFinancialImpact(),
    manageStacking: authService.canManagePromocionStacking(),
    viewReports: authService.canViewPromocionReports()
  }))
  
  // ==========================================
  // COMPUTED PROPERTIES - LOADING STATES
  // ==========================================
  
  const isFetching = computed(() => crudOps.loadingStore.isOperationActive('fetch-promocion'))
  const isCreating = computed(() => crudOps.loadingStore.isOperationActive('create-promocion'))
  const isUpdating = computed(() => crudOps.loadingStore.isOperationActive('update-promocion'))
  const isDeleting = computed(() => crudOps.loadingStore.isOperationActive('delete-promocion'))

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
      // console.log('[PromocionesStore] Fetching promociones...')
    }
    
    const result = await crudOps.fetch(async () => {
      const data = await promocionesApi.getAll()
      promociones.value = data || []
      
      // Aplicar filtros existentes
      await applyFilters()
      
      if (DEBUG_CONFIG.ENABLED) {
        // console.log(`[PromocionesStore] Loaded ${promociones.value.length} promociones`)
      }
      
      return data
    })
    
    if (!result.success) {
      showAlert('error', result.error || MESSAGES.NETWORK_ERROR)
      promociones.value = []
      filteredPromociones.value = []
    }
    
    return result
  }

  // ==========================================
  // ACTIONS - CRUD OPERATIONS
  // ==========================================
  
  /**
   * Crear nueva promoción
   */
  const createPromocion = async (promocionData) => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PromocionesStore] Creating promocion:', promocionData)
    }
    
    // Verificar permisos antes de proceder
    if (!authService.canCreatePromociones()) {
      const error = 'No tiene permisos para crear promociones'
      showAlert('error', error)
      return { success: false, error }
    }
    
    const result = await crudOps.create(async () => {
      // Validar datos antes de enviar
      const validation = promocionesApi.validatePromocion(promocionData)
      if (!validation.isValid) {
        throw new Error(validation.errors.join('\n'))
      }
      
      // Construir payload completo
      const payload = promocionesApi.buildPromocionPayload(promocionData)
      
      const response = await promocionesApi.create(payload)
      
      // Recargar promociones para obtener datos actualizados
      await fetchPromociones()
      
      return response
    })
    
    if (result.success) {
      showAlert('success', MESSAGES.PROMOCION_CREATED)
    } else {
      showAlert('error', result.error || MESSAGES.OPERATION_ERROR)
    }
    
    return result
  }

  /**
   * Actualizar promoción existente
   */
  const updatePromocion = async (id, promocionData) => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PromocionesStore] Updating promocion:', id, promocionData)
    }
    
    // Verificar permisos antes de proceder
    if (!authService.canEditPromociones()) {
      const error = 'No tiene permisos para editar promociones'
      showAlert('error', error)
      return { success: false, error }
    }
    
    const result = await crudOps.update(async () => {
      // Validar datos antes de enviar
      const validation = promocionesApi.validatePromocion(promocionData)
      if (!validation.isValid) {
        throw new Error(validation.errors.join('\n'))
      }
      
      // Construir payload completo
      const payload = promocionesApi.buildPromocionPayload(promocionData)
      
      const response = await promocionesApi.update(id, payload)
      
      // Recargar promociones para obtener datos actualizados
      await fetchPromociones()
      
      return response
    })
    
    if (result.success) {
      showAlert('success', MESSAGES.PROMOCION_UPDATED)
    } else {
      showAlert('error', result.error || MESSAGES.OPERATION_ERROR)
    }
    
    return result
  }

  /**
   * Eliminar promoción
   */
  const deletePromocion = async (id) => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PromocionesStore] Deleting promocion:', id)
    }
    
    // Verificar permisos antes de proceder
    if (!authService.canDeletePromociones()) {
      const error = 'No tiene permisos para eliminar promociones'
      showAlert('error', error)
      return { success: false, error }
    }
    
    const result = await crudOps.remove(async () => {
      await promocionesApi.delete(id)
      
      // Recargar promociones para obtener datos actualizados
      await fetchPromociones()
      
      return true
    })
    
    if (result.success) {
      showAlert('success', MESSAGES.PROMOCION_DELETED)
    } else {
      showAlert('error', result.error || MESSAGES.OPERATION_ERROR)
    }
    
    return result
  }

  /**
   * Obtener detalles de una promoción específica
   */
  const getPromocionDetails = async (id) => {
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PromocionesStore] Getting promocion details:', id)
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

    // Verificar permisos para crear promociones
    if (!authService.canCreatePromociones()) {
      showAlert('error', 'No tiene permisos para crear promociones')
      return
    }
    
    // Si está autenticado y tiene permisos, ejecutar directamente
    // Resetear estado
    isEditMode.value = false
    currentPromocion.value = null
    
    // Configurar valores por defecto
    setDefaultFormData()
    
    showCreateModal.value = true
    
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PromocionesStore] Opened create modal')
    }
  }

  /**
   * Abrir modal para editar promoción existente
   */
  const openEditModal = async (id) => {
    // Verificar permisos para editar promociones
    if (!authService.canEditPromociones()) {
      showAlert('error', 'No tiene permisos para editar promociones')
      return
    }

    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PromocionesStore] Opening edit modal for promocion:', id)
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
      // console.log('[PromocionesStore] Opening detail modal for promocion:', id)
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
      // console.log('[PromocionesStore] Closed create modal')
    }
  }

  const closeDetailModal = () => {
    showDetailModal.value = false
    currentPromocion.value = null
    
    if (DEBUG_CONFIG.ENABLED) {
      // console.log('[PromocionesStore] Closed detail modal')
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
      porcentajeDescuento: 0,
      cantidadMinima: 0,
      nCompras: 0,
      mPago: 0,
      escalas: []
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
      porcentajeDescuento: 0,
      cantidadMinima: 0,
      nCompras: 0,
      mPago: 0,
      escalas: []
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
    
    // Resetear valores de configuración
    formData.value.tipo = ''
    formData.value.porcentajeDescuento = 0
    formData.value.cantidadMinima = 0
    formData.value.nCompras = 0
    formData.value.mPago = 0
    formData.value.escalas = []
    
    // Determinar tipo basándose en la estructura de detalles
    if (!promocion.detalles || promocion.detalles.length === 0) {
      // Promoción sin detalles = SIN_DESCUENTO
      formData.value.tipo = 'SIN_DESCUENTO'
      
    } else if (promocion.detalles && promocion.detalles.length > 0) {
      const detalleBase = promocion.detalles.find(d => d.esBase)
      const detalleAcumulable = promocion.detalles.find(d => !d.esBase)
      
      if (DEBUG_CONFIG.ENABLED) {
        console.log('[PromocionesStore] Analizando detalles para edición:', {
          detalleBase,
          detalleAcumulable,
          totalDetalles: promocion.detalles.length
        })
      }
      
      // Analizar estructura existente (compatible con datos actuales)
      if (detalleBase) {
        // Caso 1: NXM - detectar por valores llevent/paguen
        if (detalleBase.llevent > 1 && detalleBase.paguen > 0 && detalleBase.llevent > detalleBase.paguen) {
          formData.value.tipo = 'NXM'
          formData.value.nCompras = detalleBase.llevent || 0
          formData.value.mPago = detalleBase.paguen || 0
          
        // Caso 2: Descuento Plano - detectar por porcentajeDescuentoPlano > 0
        } else if (detalleBase.porcentajeDescuentoPlano > 0 && 
                   detalleBase.llevent === 1 && detalleBase.paguen === 1) {
          formData.value.tipo = 'DESCUENTO_PLANO'
          formData.value.porcentajeDescuento = detalleBase.porcentajeDescuentoPlano || 0
          
        // Caso 3: Promoción básica/regular sin descuento
        } else if (detalleBase.llevent === 1 && detalleBase.paguen === 1 && 
                   (detalleBase.porcentajeDescuentoPlano === 0 || !detalleBase.porcentajeDescuentoPlano)) {
          // Promoción regular sin descuento
          formData.value.tipo = 'SIN_DESCUENTO'
          
        } else {
          // Estructura nueva (futuras promociones)
          if (detalleBase.tipoBase === 'NXM') {
            formData.value.tipo = 'NXM'
            formData.value.nCompras = detalleBase.llevent || 0
            formData.value.mPago = detalleBase.paguen || 0
            
          } else if (detalleBase.tipoBase === 'SIN_DESCUENTO' && 
                     detalleAcumulable && detalleAcumulable.tipoAcumulable === 'DESCUENTO_PLANO') {
            formData.value.tipo = 'DESCUENTO_PLANO'
            formData.value.porcentajeDescuento = detalleAcumulable.porcentajeDescuentoPlano || 0
            
          } else if (detalleBase.tipoBase === 'SIN_DESCUENTO' && 
                     detalleAcumulable && detalleAcumulable.tipoAcumulable === 'DESCUENTO_POR_CANTIDAD') {
            formData.value.tipo = 'POR_CANTIDAD'
            
            if (detalleAcumulable.escalasDescuento && detalleAcumulable.escalasDescuento.length > 0) {
              // Mapear todas las escalas del backend al formato del frontend
              formData.value.escalas = detalleAcumulable.escalasDescuento.map(escala => ({
                cantidadMinima: escala.cantidadMinima || 0,
                cantidadMaxima: escala.cantidadMaxima || null,
                porcentaje: escala.descuento || escala.porcentajeDescuento || 0
              }))
              
              // Mantener compatibilidad con campos individuales (para componentes que aún los usen)
              const primeraEscala = detalleAcumulable.escalasDescuento[0]
              formData.value.cantidadMinima = primeraEscala.cantidadMinima || 0
              formData.value.porcentajeDescuento = primeraEscala.descuento || primeraEscala.porcentajeDescuento || 0
            }
          } else if (detalleBase.tipoBase === 'SIN_DESCUENTO' && !detalleAcumulable) {
            // CASO AGREGADO: Promoción con detalleBase SIN_DESCUENTO sin detalles acumulables
            formData.value.tipo = 'SIN_DESCUENTO'
            
          } else {
            // Caso no reconocido - log para debugging
            console.warn('[PromocionesStore] Estructura de detalles no reconocida:', promocion.detalles)
          }
        }
      }
    }

    if (DEBUG_CONFIG.ENABLED) {
      console.log('[PromocionesStore] Formulario poblado para edición:', {
        tipo: formData.value.tipo,
        porcentajeDescuento: formData.value.porcentajeDescuento,
        nCompras: formData.value.nCompras,
        mPago: formData.value.mPago,
        cantidadMinima: formData.value.cantidadMinima
      })
    }

    updatePreview()
  }

  /**
   * Manejar cambio de tipo de promoción
   */
  const handleTipoChange = (tipo) => {
    formData.value.tipo = tipo
    
    // Resetear valores específicos del tipo anterior
    formData.value.porcentajeDescuento = 0
    formData.value.cantidadMinima = 0
    formData.value.nCompras = 0
    formData.value.mPago = 0
    formData.value.escalas = []
    
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
      // console.log(`[PromocionesStore] Applied filters: ${filteredPromociones.value.length} results`)
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
      // console.log('[PromocionesStore] Filters cleared')
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
        // console.log(`[PromocionesStore] Navigated to page ${pageNumber}`)
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
      // console.log(`[PromocionesStore] Page size changed to ${newPageSize}`)
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
    userPermissions,
    isFormValid,
    modalTitle,
    tiposPromocion,
    estadosPromocion,
    showTipoConfig,
    
    // Permisos
    canViewPromociones,
    canCreatePromociones,
    canEditPromociones,
    canDeletePromociones,
    canApplyPromociones,
    canViewPromocionFinancialImpact,
    canManagePromocionStacking,
    canViewPromocionReports,
    userRoles,
    isAdmin,
    primaryRole,
    
    // Loading states
    isFetching,
    isCreating,
    isUpdating,
    isDeleting,
    
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