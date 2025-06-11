/**
 * Gestión completa de Promociones
 * 
 * Funcionalidades:
 * - CRUD completo de promociones con tipos complejos
 * - Filtros por nombre/descripción y estado (VIGENTE, EXPIRADA, FUTURA)
 * - Paginación con controles desktop/mobile
 * - Validación en tiempo real de formularios
 * - Preview de promociones antes de guardar
 * - Manejo de tipos: Descuento Plano, Porcentual, Por Cantidad, NxM
 * - Estados loading, empty y error
 * - Responsive design con tabla y cards
 */

class PromocionesManager {
    constructor() {
        this.promociones = [];
        this.filteredPromociones = [];
        this.currentPage = 1;
        this.pageSize = 10;
        this.totalPages = 0;
        this.isEditing = false;
        this.editingId = null;
        
        // Filtros
        this.searchTerm = '';
        this.estadoFilter = '';
        
        this.init();
    }

    init() {
        console.log('[PromocionesManager] Iniciando gestión de promociones...');
        this.setupEventListeners();
        this.loadPromociones();
    }

    setupEventListeners() {
        // Botones principales
        document.getElementById('btn-nueva-promocion')?.addEventListener('click', () => this.showModal());
        document.getElementById('btn-crear-primera-promocion')?.addEventListener('click', () => this.showModal());

        // Modal events
        document.getElementById('modal-promocion-close')?.addEventListener('click', () => this.hideModal());
        document.getElementById('modal-promocion-cancel')?.addEventListener('click', () => this.hideModal());
        document.getElementById('form-promocion')?.addEventListener('submit', (e) => this.handleSubmit(e));

        // Filtros
        document.getElementById('search-promociones')?.addEventListener('input', (e) => this.handleSearch(e.target.value));
        document.getElementById('filter-estado')?.addEventListener('change', (e) => this.handleEstadoFilter(e.target.value));
        document.getElementById('btn-limpiar-promociones')?.addEventListener('click', () => this.clearFilters());

        // Paginación
        document.getElementById('promociones-page-size')?.addEventListener('change', (e) => this.changePageSize(parseInt(e.target.value)));
        this.setupPaginationEvents();

        // Tipo de promoción - cambio dinámico
        document.getElementById('form-promocion-tipo')?.addEventListener('change', (e) => this.handleTipoChange(e.target.value));

        // Validación en tiempo real
        this.setupRealTimeValidation();
    }

    setupPaginationEvents() {
        document.getElementById('btn-first-page-promociones')?.addEventListener('click', () => this.goToPage(1));
        document.getElementById('btn-prev-page-promociones')?.addEventListener('click', () => this.goToPage(this.currentPage - 1));
        document.getElementById('btn-next-page-promociones')?.addEventListener('click', () => this.goToPage(this.currentPage + 1));
        document.getElementById('btn-last-page-promociones')?.addEventListener('click', () => this.goToPage(this.totalPages));

        // Mobile pagination
        document.getElementById('btn-prev-page-mobile-promociones')?.addEventListener('click', () => this.goToPage(this.currentPage - 1));
        document.getElementById('btn-next-page-mobile-promociones')?.addEventListener('click', () => this.goToPage(this.currentPage + 1));
    }

    setupRealTimeValidation() {
        const form = document.getElementById('form-promocion');
        if (!form) return;

        form.addEventListener('input', () => this.updatePreview());
        form.addEventListener('change', () => this.updatePreview());

        // Validación de fechas
        const fechaInicio = document.getElementById('form-promocion-fecha-inicio');
        const fechaFin = document.getElementById('form-promocion-fecha-fin');
        
        fechaInicio?.addEventListener('change', () => this.validateDates());
        fechaFin?.addEventListener('change', () => this.validateDates());
    }

    async loadPromociones() {
        try {
            this.showLoadingState();
            const response = await window.PortalApi.promociones.getAll();
            this.promociones = response || [];
            this.applyFilters();
            console.log(`[PromocionesManager] ${this.promociones.length} promociones cargadas`);
        } catch (error) {
            console.error('[PromocionesManager] Error al cargar promociones:', error);
            window.PortalApi.handleError(error);
            this.showErrorState();
        }
    }

    applyFilters() {
        let filtered = [...this.promociones];

        // Filtro de búsqueda
        if (this.searchTerm) {
            const search = this.searchTerm.toLowerCase();
            filtered = filtered.filter(promocion => 
                (promocion.nombre && promocion.nombre.toLowerCase().includes(search)) ||
                (promocion.descripcion && promocion.descripcion.toLowerCase().includes(search))
            );
        }

        // Filtro de estado
        if (this.estadoFilter) {
            filtered = filtered.filter(promocion => 
                promocion.estadoVigencia === this.estadoFilter
            );
        }

        this.filteredPromociones = filtered;
        this.calculatePagination();
        this.renderPromociones();
    }

    calculatePagination() {
        this.totalPages = Math.ceil(this.filteredPromociones.length / this.pageSize);
        if (this.currentPage > this.totalPages) {
            this.currentPage = Math.max(1, this.totalPages);
        }
    }

    renderPromociones() {
        if (this.filteredPromociones.length === 0) {
            this.showEmptyState();
            return;
        }

        this.hideStates();
        this.renderTable();
        this.renderCards();
        this.renderPagination();
    }

    renderTable() {
        const tbody = document.getElementById('promociones-table-body');
        if (!tbody) return;

        const start = (this.currentPage - 1) * this.pageSize;
        const end = start + this.pageSize;
        const paginatedData = this.filteredPromociones.slice(start, end);

        tbody.innerHTML = paginatedData.map(promocion => `
            <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                        <div>
                            <div class="text-sm font-medium text-gray-900">${promocion.nombre || 'Sin nombre'}</div>
                            <div class="text-sm text-gray-500">${this.truncateText(promocion.descripcion || '', 50)}</div>
                        </div>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">
                        <div>Desde: ${promocion.vigenciaDesde ? this.formatDate(promocion.vigenciaDesde) : 'No definida'}</div>
                        <div>Hasta: ${promocion.vigenciaHasta ? this.formatDate(promocion.vigenciaHasta) : 'No definida'}</div>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    ${this.getEstadoBadge(promocion.estadoVigencia)}
                    ${promocion.diasRestantes ? `<div class="text-xs text-gray-500 mt-1">${promocion.diasRestantes} días</div>` : ''}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <div>${promocion.totalDetalles || 0} detalle(s)</div>
                    <div class="text-xs">${promocion.tipoPromocionPrincipal || 'Sin tipo'}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div class="flex justify-end space-x-2">
                        <button onclick="window.PromocionesManager.viewPromocion(${promocion.idPromocion})" 
                                class="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-xs">
                            <i class="fas fa-eye mr-1"></i>Ver
                        </button>
                        <button onclick="window.PromocionesManager.editPromocion(${promocion.idPromocion})" 
                                class="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded text-xs">
                            <i class="fas fa-edit mr-1"></i>Editar
                        </button>
                        <button onclick="window.PromocionesManager.deletePromocion(${promocion.idPromocion})" 
                                class="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded text-xs">
                            <i class="fas fa-trash mr-1"></i>Eliminar
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    renderCards() {
        const cardsContainer = document.getElementById('promociones-cards-body');
        if (!cardsContainer) return;

        const start = (this.currentPage - 1) * this.pageSize;
        const end = start + this.pageSize;
        const paginatedData = this.filteredPromociones.slice(start, end);

        cardsContainer.innerHTML = paginatedData.map(promocion => `
            <div class="bg-white border rounded-lg p-4 shadow-sm">
                <div class="flex justify-between items-start mb-3">
                    <div class="flex-1 min-w-0">
                        <h3 class="text-lg font-medium text-gray-900 truncate">${promocion.nombre || 'Sin nombre'}</h3>
                        <p class="text-sm text-gray-500 mt-1">${this.truncateText(promocion.descripcion || '', 100)}</p>
                    </div>
                    ${this.getEstadoBadge(promocion.estadoVigencia)}
                </div>
                
                <div class="space-y-2 text-sm">
                    <div class="flex justify-between">
                        <span class="text-gray-500">Vigencia:</span>
                        <span class="text-gray-900">
                            ${promocion.vigenciaDesde ? this.formatDate(promocion.vigenciaDesde) : 'No definida'} - 
                            ${promocion.vigenciaHasta ? this.formatDate(promocion.vigenciaHasta) : 'No definida'}
                        </span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-gray-500">Detalles:</span>
                        <span class="text-gray-900">${promocion.totalDetalles || 0} configurado(s)</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-gray-500">Tipo:</span>
                        <span class="text-gray-900">${promocion.tipoPromocionPrincipal || 'Sin tipo'}</span>
                    </div>
                    ${promocion.diasRestantes ? `
                        <div class="flex justify-between">
                            <span class="text-gray-500">Días restantes:</span>
                            <span class="text-gray-900">${promocion.diasRestantes}</span>
                        </div>
                    ` : ''}
                </div>
                
                <div class="mt-4 flex space-x-2">
                    <button onclick="window.PromocionesManager.viewPromocion(${promocion.idPromocion})" 
                            class="flex-1 bg-blue-500 hover:bg-blue-600 text-white px-3 py-2 rounded text-sm">
                        <i class="fas fa-eye mr-2"></i>Ver Detalle
                    </button>
                    <button onclick="window.PromocionesManager.editPromocion(${promocion.idPromocion})" 
                            class="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-2 rounded text-sm">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button onclick="window.PromocionesManager.deletePromocion(${promocion.idPromocion})" 
                            class="bg-red-500 hover:bg-red-600 text-white px-3 py-2 rounded text-sm">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `).join('');
    }

    renderPagination() {
        const paginationInfo = document.getElementById('promociones-pagination-info');
        const paginationContainer = document.getElementById('promociones-pagination');
        const pageNumbers = document.getElementById('promociones-page-numbers');

        if (!paginationInfo || !pageNumbers) return;

        const start = (this.currentPage - 1) * this.pageSize + 1;
        const end = Math.min(start + this.pageSize - 1, this.filteredPromociones.length);

        paginationInfo.innerHTML = `
            Mostrando <span class="font-medium">${start}</span> a <span class="font-medium">${end}</span> 
            de <span class="font-medium">${this.filteredPromociones.length}</span> resultados
        `;

        // Generar números de página
        pageNumbers.innerHTML = this.generatePageNumbers();

        // Mostrar/ocultar paginación
        if (paginationContainer) {
            paginationContainer.classList.toggle('hidden', this.totalPages <= 1);
        }

        // Habilitar/deshabilitar botones
        this.updatePaginationButtons();
    }

    generatePageNumbers() {
        const maxButtons = 5;
        let startPage = Math.max(1, this.currentPage - Math.floor(maxButtons / 2));
        let endPage = Math.min(this.totalPages, startPage + maxButtons - 1);

        if (endPage - startPage + 1 < maxButtons) {
            startPage = Math.max(1, endPage - maxButtons + 1);
        }

        let html = '';
        for (let i = startPage; i <= endPage; i++) {
            html += `
                <button onclick="window.PromocionesManager.goToPage(${i})" 
                        class="relative inline-flex items-center px-4 py-2 border text-sm font-medium focus:z-10 focus:outline-none focus:ring-1 focus:ring-primary-500 focus:border-primary-500 ${
                            i === this.currentPage 
                                ? 'z-10 bg-primary-50 border-primary-500 text-primary-600' 
                                : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                        }">
                    ${i}
                </button>
            `;
        }
        return html;
    }

    updatePaginationButtons() {
        const firstBtn = document.getElementById('btn-first-page-promociones');
        const prevBtn = document.getElementById('btn-prev-page-promociones');
        const nextBtn = document.getElementById('btn-next-page-promociones');
        const lastBtn = document.getElementById('btn-last-page-promociones');

        const prevMobileBtn = document.getElementById('btn-prev-page-mobile-promociones');
        const nextMobileBtn = document.getElementById('btn-next-page-mobile-promociones');

        const isFirstPage = this.currentPage === 1;
        const isLastPage = this.currentPage === this.totalPages;

        if (firstBtn) firstBtn.disabled = isFirstPage;
        if (prevBtn) prevBtn.disabled = isFirstPage;
        if (nextBtn) nextBtn.disabled = isLastPage;
        if (lastBtn) lastBtn.disabled = isLastPage;

        if (prevMobileBtn) prevMobileBtn.disabled = isFirstPage;
        if (nextMobileBtn) nextMobileBtn.disabled = isLastPage;
    }

    // Event Handlers
    handleSearch(searchTerm) {
        this.searchTerm = searchTerm;
        this.currentPage = 1;
        this.applyFilters();
    }

    handleEstadoFilter(estado) {
        this.estadoFilter = estado;
        this.currentPage = 1;
        this.applyFilters();
    }

    clearFilters() {
        this.searchTerm = '';
        this.estadoFilter = '';
        this.currentPage = 1;

        document.getElementById('search-promociones').value = '';
        document.getElementById('filter-estado').value = '';

        this.applyFilters();
    }

    changePageSize(newSize) {
        this.pageSize = newSize;
        this.currentPage = 1;
        this.applyFilters();
    }

    goToPage(page) {
        if (page >= 1 && page <= this.totalPages) {
            this.currentPage = page;
            this.renderPromociones();
        }
    }

    handleTipoChange(tipo) {
        // Ocultar todas las configuraciones
        this.hideAllConfigs();

        // Mostrar la configuración correspondiente
        switch(tipo) {
            case 'DESCUENTO_PLANO':
                document.getElementById('config-descuento-plano')?.classList.remove('hidden');
                break;
            case 'DESCUENTO_PORCENTUAL':
                document.getElementById('config-descuento-porcentual')?.classList.remove('hidden');
                break;
            case 'POR_CANTIDAD':
                document.getElementById('config-por-cantidad')?.classList.remove('hidden');
                break;
            case 'NXM':
                document.getElementById('config-nxm')?.classList.remove('hidden');
                break;
        }

        this.updatePreview();
    }

    hideAllConfigs() {
        const configs = [
            'config-descuento-plano',
            'config-descuento-porcentual', 
            'config-por-cantidad',
            'config-nxm'
        ];

        configs.forEach(id => {
            document.getElementById(id)?.classList.add('hidden');
        });
    }

    updatePreview() {
        const preview = document.getElementById('promocion-preview');
        const content = document.getElementById('preview-promocion-content');
        
        if (!preview || !content) return;

        const formData = this.getFormData();
        
        if (this.isFormValid(formData)) {
            preview.classList.remove('hidden');
            content.innerHTML = this.generatePreviewContent(formData);
        } else {
            preview.classList.add('hidden');
        }
    }

    validateDates() {
        const fechaInicio = document.getElementById('form-promocion-fecha-inicio');
        const fechaFin = document.getElementById('form-promocion-fecha-fin');
        
        if (!fechaInicio || !fechaFin || !fechaInicio.value || !fechaFin.value) return;

        const inicio = new Date(fechaInicio.value);
        const fin = new Date(fechaFin.value);

        if (inicio > fin) {
            fechaFin.setCustomValidity('La fecha de fin debe ser posterior a la fecha de inicio');
        } else {
            fechaFin.setCustomValidity('');
        }
    }

    // Modal Operations
    showModal(promocion = null) {
        this.isEditing = !!promocion;
        this.editingId = promocion ? promocion.idPromocion : null;

        const modal = document.getElementById('modal-promocion');
        const title = document.getElementById('modal-promocion-title');
        const subtitle = document.getElementById('modal-promocion-subtitle');
        const submitBtn = document.getElementById('modal-promocion-submit');

        if (this.isEditing) {
            title.textContent = 'Editar Promoción';
            subtitle.textContent = `Modificando: ${promocion.nombre}`;
            submitBtn.innerHTML = '<i class="fas fa-save mr-2"></i>Actualizar Promoción';
            this.fillForm(promocion);
        } else {
            title.textContent = 'Nueva Promoción';
            subtitle.textContent = 'Configure los detalles de la promoción';
            submitBtn.innerHTML = '<i class="fas fa-tag mr-2"></i>Guardar Promoción';
            this.resetForm();
        }

        modal.classList.remove('hidden');
        
        // Focus en el primer campo
        setTimeout(() => {
            document.getElementById('form-promocion-nombre')?.focus();
        }, 100);
    }

    hideModal() {
        const modal = document.getElementById('modal-promocion');
        modal.classList.add('hidden');
        this.resetForm();
        this.isEditing = false;
        this.editingId = null;
    }

    resetForm() {
        document.getElementById('form-promocion')?.reset();
        this.hideAllConfigs();
        document.getElementById('promocion-preview')?.classList.add('hidden');

        // Establecer fechas por defecto
        const today = new Date();
        const nextWeek = new Date(today);
        nextWeek.setDate(today.getDate() + 7);

        const fechaInicio = document.getElementById('form-promocion-fecha-inicio');
        const fechaFin = document.getElementById('form-promocion-fecha-fin');

        if (fechaInicio) fechaInicio.value = today.toISOString().split('T')[0];
        if (fechaFin) fechaFin.value = nextWeek.toISOString().split('T')[0];
    }

    fillForm(promocion) {
        // Llenar campos básicos
        document.getElementById('form-promocion-nombre').value = promocion.nombre || '';
        document.getElementById('form-promocion-descripcion').value = promocion.descripcion || '';
        
        if (promocion.vigenciaDesde) {
            document.getElementById('form-promocion-fecha-inicio').value = promocion.vigenciaDesde;
        }
        if (promocion.vigenciaHasta) {
            document.getElementById('form-promocion-fecha-fin').value = promocion.vigenciaHasta;
        }

        // Nota: La implementación completa requeriría cargar los detalles específicos
        // por ahora solo cargamos los campos básicos
        console.log('[PromocionesManager] Formulario llenado para promoción:', promocion.idPromocion);
    }

    getFormData() {
        return {
            nombre: document.getElementById('form-promocion-nombre')?.value || '',
            descripcion: document.getElementById('form-promocion-descripcion')?.value || '',
            vigenciaDesde: document.getElementById('form-promocion-fecha-inicio')?.value || '',
            vigenciaHasta: document.getElementById('form-promocion-fecha-fin')?.value || '',
            tipo: document.getElementById('form-promocion-tipo')?.value || '',
            // Campos específicos por tipo
            montoDescuento: parseFloat(document.getElementById('form-monto-descuento')?.value) || 0,
            porcentajeDescuento: parseFloat(document.getElementById('form-porcentaje-descuento')?.value) || 0,
            cantidadMinima: parseInt(document.getElementById('form-cantidad-minima')?.value) || 0,
            nCompras: parseInt(document.getElementById('form-n-compras')?.value) || 0,
            mPago: parseInt(document.getElementById('form-m-gratuitas')?.value) || 0
        };
    }

    isFormValid(data) {
        return data.nombre && data.descripcion && data.vigenciaDesde && 
               data.vigenciaHasta && data.tipo;
    }

    generatePreviewContent(data) {
        let content = `
            <div class="space-y-3">
                <div><strong>Nombre:</strong> ${data.nombre}</div>
                <div><strong>Descripción:</strong> ${data.descripcion}</div>
                <div><strong>Vigencia:</strong> ${this.formatDate(data.vigenciaDesde)} - ${this.formatDate(data.vigenciaHasta)}</div>
                <div><strong>Tipo:</strong> ${this.getTipoDisplayName(data.tipo)}</div>
        `;

        // Agregar detalles específicos del tipo
        switch(data.tipo) {
            case 'DESCUENTO_PLANO':
                content += `<div><strong>Monto Descuento:</strong> $${data.montoDescuento.toFixed(2)}</div>`;
                break;
            case 'DESCUENTO_PORCENTUAL':
                content += `<div><strong>Porcentaje:</strong> ${data.porcentajeDescuento}%</div>`;
                break;
            case 'POR_CANTIDAD':
                content += `<div><strong>Cantidad Mínima:</strong> ${data.cantidadMinima}</div>`;
                break;
            case 'NXM':
                content += `<div><strong>Promoción:</strong> Compra ${data.nCompras}, Paga ${data.mPago}</div>`;
                break;
        }

        content += '</div>';
        return content;
    }

    async handleSubmit(e) {
        e.preventDefault();
        
        const formData = this.getFormData();
        
        if (!this.isFormValid(formData)) {
            window.PortalUtils.Alert.error('Por favor complete todos los campos requeridos');
            return;
        }

        try {
            const submitBtn = document.getElementById('modal-promocion-submit');
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Guardando...';

            // Crear objeto promoción para enviar al backend
            const promocionData = this.buildPromocionPayload(formData);

            let response;
            if (this.isEditing) {
                response = await window.PortalApi.promociones.update(this.editingId, promocionData);
            } else {
                response = await window.PortalApi.promociones.create(promocionData);
            }

            window.PortalUtils.Alert.success(
                this.isEditing ? 'Promoción actualizada exitosamente' : 'Promoción creada exitosamente'
            );

            this.hideModal();
            await this.loadPromociones();

        } catch (error) {
            console.error('[PromocionesManager] Error al guardar promoción:', error);
            window.PortalApi.handleError(error);
        } finally {
            const submitBtn = document.getElementById('modal-promocion-submit');
            submitBtn.disabled = false;
            submitBtn.innerHTML = this.isEditing ? 
                '<i class="fas fa-save mr-2"></i>Actualizar Promoción' : 
                '<i class="fas fa-tag mr-2"></i>Guardar Promoción';
        }
    }

    buildPromocionPayload(formData) {
        const payload = {
            nombre: formData.nombre,
            descripcion: formData.descripcion,
            vigenciaDesde: formData.vigenciaDesde,
            vigenciaHasta: formData.vigenciaHasta,
            detalles: []
        };

        // Crear detalle base según el tipo
        const detalleBase = this.createDetalleForType(formData);
        if (detalleBase) {
            payload.detalles.push(detalleBase);
        }

        return payload;
    }

    createDetalleForType(data) {
        const detalle = {
            nombre: `Detalle ${data.tipo}`,
            esBase: true
        };

        switch(data.tipo) {
            case 'DESCUENTO_PLANO':
                detalle.tipoAcumulable = 'DESCUENTO_PLANO';
                detalle.porcentajeDescuentoPlano = data.montoDescuento;
                break;
            case 'DESCUENTO_PORCENTUAL':
                detalle.tipoAcumulable = 'DESCUENTO_PLANO';
                detalle.porcentajeDescuentoPlano = data.porcentajeDescuento;
                break;
            case 'NXM':
                detalle.tipoBase = 'NXM';
                detalle.parametrosNxM = {
                    llevent: data.nCompras,
                    paguen: data.mPago
                };
                break;
            default:
                detalle.tipoBase = 'SIN_DESCUENTO';
        }

        return detalle;
    }

    // CRUD Operations
    async viewPromocion(id) {
        try {
            const promocion = await window.PortalApi.promociones.getById(id);
            this.showPromocionDetails(promocion);
        } catch (error) {
            console.error('[PromocionesManager] Error al cargar promoción:', error);
            window.PortalApi.handleError(error);
        }
    }

    showPromocionDetails(promocion) {
        let details = `
            <div class="space-y-6">
                <!-- Información General -->
                <div class="bg-blue-50 rounded-lg p-4">
                    <h4 class="font-semibold text-blue-900 mb-3">Información General</h4>
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                        <div><strong>ID:</strong> ${promocion.idPromocion}</div>
                        <div><strong>Estado:</strong> ${this.getEstadoBadge(promocion.estadoVigencia)}</div>
                        <div class="md:col-span-2"><strong>Nombre:</strong> ${promocion.nombre}</div>
                        <div class="md:col-span-2"><strong>Descripción:</strong> ${promocion.descripcion}</div>
                        <div><strong>Vigencia Desde:</strong> ${this.formatDate(promocion.vigenciaDesde)}</div>
                        <div><strong>Vigencia Hasta:</strong> ${this.formatDate(promocion.vigenciaHasta)}</div>
                        ${promocion.diasRestantes ? `<div><strong>Días Restantes:</strong> ${promocion.diasRestantes}</div>` : ''}
                        <div><strong>Total Detalles:</strong> ${promocion.totalDetalles || 0}</div>
                    </div>
                </div>
        `;

        // Mostrar detalles específicos si existen
        if (promocion.detalles && promocion.detalles.length > 0) {
            details += `
                <!-- Configuración de Promoción -->
                <div class="bg-green-50 rounded-lg p-4">
                    <h4 class="font-semibold text-green-900 mb-3">Configuración de Promoción</h4>
                    <div class="space-y-4">
            `;

            promocion.detalles.forEach((detalle, index) => {
                details += `
                    <div class="border border-green-200 rounded-lg p-3 bg-white">
                        <div class="flex justify-between items-start mb-2">
                            <h5 class="font-medium text-gray-900">${detalle.nombre || `Detalle ${index + 1}`}</h5>
                            <span class="px-2 py-1 text-xs rounded-full ${detalle.esBase ? 'bg-blue-100 text-blue-800' : 'bg-purple-100 text-purple-800'}">
                                ${detalle.esBase ? 'Base' : 'Acumulable'}
                            </span>
                        </div>
                        
                        <div class="text-sm space-y-2">
                            <div><strong>Tipo:</strong> ${detalle.descripcionTipo || 'No configurado'}</div>
                `;

                // Detalles específicos según el tipo
                if (detalle.esBase && detalle.tipoBase) {
                    if (detalle.tipoBase === 'NXM' && detalle.llevent && detalle.paguen) {
                        const descuento = ((detalle.llevent - detalle.paguen) / detalle.llevent * 100).toFixed(1);
                        details += `
                            <div class="bg-yellow-50 p-2 rounded">
                                <div><strong>Configuración NxM:</strong></div>
                                <div>• Llevás: <span class="font-mono">${detalle.llevent}</span> unidades</div>
                                <div>• Pagás: <span class="font-mono">${detalle.paguen}</span> unidades</div>
                                <div>• Descuento: <span class="font-mono text-green-600">${descuento}%</span></div>
                            </div>
                        `;
                    }
                }

                if (!detalle.esBase && detalle.tipoAcumulable) {
                    if (detalle.tipoAcumulable === 'DESCUENTO_PLANO' && detalle.porcentajeDescuentoPlano) {
                        details += `
                            <div class="bg-yellow-50 p-2 rounded">
                                <div><strong>Descuento Plano:</strong></div>
                                <div>• Porcentaje: <span class="font-mono text-green-600">${detalle.porcentajeDescuentoPlano}%</span></div>
                            </div>
                        `;
                    }

                    if (detalle.escalasDescuento && detalle.escalasDescuento.length > 0) {
                        details += `
                            <div class="bg-yellow-50 p-2 rounded">
                                <div><strong>Escalas de Descuento:</strong></div>
                                <div class="mt-1 space-y-1">
                        `;
                        detalle.escalasDescuento.forEach(escala => {
                            details += `<div class="text-xs">• ${escala.cantidad} unidades: ${escala.descuento}%</div>`;
                        });
                        details += `</div></div>`;
                    }
                }

                details += `
                        </div>
                    </div>
                `;
            });

            details += `</div></div>`;
        } else {
            details += `
                <div class="bg-gray-50 rounded-lg p-4 text-center">
                    <p class="text-gray-600">Esta promoción no tiene detalles configurados.</p>
                </div>
            `;
        }

        details += `</div>`;

        // Crear modal dinámico para mostrar detalles
        this.createDetailModal('Detalle Completo de Promoción', details);
    }

    async editPromocion(id) {
        try {
            const promocion = await window.PortalApi.promociones.getById(id);
            this.showModal(promocion);
        } catch (error) {
            console.error('[PromocionesManager] Error al cargar promoción para editar:', error);
            window.PortalApi.handleError(error);
        }
    }

    async deletePromocion(id) {
        const promocion = this.promociones.find(p => p.idPromocion === id);
        const nombre = promocion ? promocion.nombre : `ID ${id}`;

        const confirmed = await this.showConfirmDialog(
            '¿Eliminar promoción?',
            `¿Está seguro de que desea eliminar la promoción "${nombre}"? Esta acción no se puede deshacer.`
        );

        if (!confirmed) return;

        try {
            await window.PortalApi.promociones.delete(id);
            window.PortalUtils.Alert.success('Promoción eliminada exitosamente');
            await this.loadPromociones();
        } catch (error) {
            console.error('[PromocionesManager] Error al eliminar promoción:', error);
            window.PortalApi.handleError(error);
        }
    }

    // State Management
    showLoadingState() {
        this.hideStates();
        document.getElementById('promociones-loading')?.classList.remove('hidden');
    }

    showEmptyState() {
        this.hideStates();
        document.getElementById('promociones-empty')?.classList.remove('hidden');
    }

    showErrorState() {
        this.hideStates();
        // El error se maneja por PortalApi.handleError()
    }

    hideStates() {
        document.getElementById('promociones-loading')?.classList.add('hidden');
        document.getElementById('promociones-empty')?.classList.add('hidden');
        document.getElementById('promociones-table-container')?.classList.remove('hidden');
        document.getElementById('promociones-cards-container')?.classList.remove('hidden');
    }

    // Utility Methods
    getEstadoBadge(estado) {
        const badges = {
            'VIGENTE': '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">Vigente</span>',
            'EXPIRADA': '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">Expirada</span>',
            'FUTURA': '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">Futura</span>'
        };
        return badges[estado] || '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">Sin estado</span>';
    }

    getEstadoText(estado) {
        const estados = {
            'VIGENTE': 'Vigente',
            'EXPIRADA': 'Expirada', 
            'FUTURA': 'Futura'
        };
        return estados[estado] || 'Sin estado';
    }

    getTipoDisplayName(tipo) {
        const tipos = {
            'DESCUENTO_PLANO': 'Descuento Plano',
            'DESCUENTO_PORCENTUAL': 'Descuento Porcentual',
            'POR_CANTIDAD': 'Por Cantidad',
            'NXM': 'N x M',
            'DESCUENTO_ESCALONADO': 'Descuento Escalonado'
        };
        return tipos[tipo] || tipo;
    }

    formatDate(dateString) {
        if (!dateString) return 'No definida';
        const date = new Date(dateString);
        return date.toLocaleDateString('es-ES', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }

    truncateText(text, maxLength) {
        if (!text) return '';
        return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    }

    // Modal Utilities
    createDetailModal(title, content) {
        // Crear modal dinámico
        const modalHtml = `
            <div id="modal-promocion-detail" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
                <div class="relative top-10 mx-auto p-5 border w-11/12 md:w-4/5 lg:w-3/4 xl:w-2/3 shadow-lg rounded-md bg-white max-h-screen overflow-y-auto">
                    <div class="mt-3">
                        <div class="flex items-center justify-between mb-4">
                            <h3 class="text-lg font-medium text-gray-900">${title}</h3>
                            <button id="modal-detail-close" class="text-gray-400 hover:text-gray-600">
                                <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                                </svg>
                            </button>
                        </div>
                        <div class="mb-4">${content}</div>
                        <div class="flex justify-end">
                            <button id="modal-detail-ok" class="px-4 py-2 bg-primary-600 text-white rounded hover:bg-primary-700">
                                Cerrar
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Remover modal existente si existe
        const existingModal = document.getElementById('modal-promocion-detail');
        if (existingModal) {
            existingModal.remove();
        }

        // Agregar modal al DOM
        document.body.insertAdjacentHTML('beforeend', modalHtml);

        // Configurar eventos
        document.getElementById('modal-detail-close').addEventListener('click', () => {
            document.getElementById('modal-promocion-detail').remove();
        });
        document.getElementById('modal-detail-ok').addEventListener('click', () => {
            document.getElementById('modal-promocion-detail').remove();
        });

        // Cerrar con ESC
        const escHandler = (e) => {
            if (e.key === 'Escape') {
                const modal = document.getElementById('modal-promocion-detail');
                if (modal) {
                    modal.remove();
                    document.removeEventListener('keydown', escHandler);
                }
            }
        };
        document.addEventListener('keydown', escHandler);
    }

    showConfirmDialog(title, message) {
        return new Promise((resolve) => {
            // Crear modal de confirmación dinámico
            const modalHtml = `
                <div id="modal-promocion-confirm" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
                    <div class="relative top-20 mx-auto p-5 border w-11/12 md:w-1/2 shadow-lg rounded-md bg-white">
                        <div class="mt-3">
                            <div class="flex items-center justify-between mb-4">
                                <h3 class="text-lg font-medium text-gray-900">${title}</h3>
                                <button id="modal-confirm-close" class="text-gray-400 hover:text-gray-600">
                                    <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                                    </svg>
                                </button>
                            </div>
                            <div class="mb-6">
                                <p class="text-gray-600">${message}</p>
                            </div>
                            <div class="flex justify-end space-x-3">
                                <button id="modal-confirm-cancel" class="px-4 py-2 border border-gray-300 text-gray-700 rounded hover:bg-gray-50">
                                    Cancelar
                                </button>
                                <button id="modal-confirm-ok" class="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700">
                                    Eliminar
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;

            // Remover modal existente si existe
            const existingModal = document.getElementById('modal-promocion-confirm');
            if (existingModal) {
                existingModal.remove();
            }

            // Agregar modal al DOM
            document.body.insertAdjacentHTML('beforeend', modalHtml);

            // Configurar eventos
            const closeModal = (result) => {
                document.getElementById('modal-promocion-confirm').remove();
                resolve(result);
            };

            document.getElementById('modal-confirm-close').addEventListener('click', () => closeModal(false));
            document.getElementById('modal-confirm-cancel').addEventListener('click', () => closeModal(false));
            document.getElementById('modal-confirm-ok').addEventListener('click', () => closeModal(true));

            // Cerrar con ESC
            const escHandler = (e) => {
                if (e.key === 'Escape') {
                    const modal = document.getElementById('modal-promocion-confirm');
                    if (modal) {
                        closeModal(false);
                        document.removeEventListener('keydown', escHandler);
                    }
                }
            };
            document.addEventListener('keydown', escHandler);
        });
    }
}

// Inicialización global
window.PromocionesManager = new PromocionesManager();

console.log('[PromocionesManager] Módulo de promociones cargado exitosamente'); 