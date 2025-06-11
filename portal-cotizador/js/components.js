/**
 * Módulo para gestión de componentes
 */

class ComponentesManager {
    constructor() {
        this.componentes = [];
        this.filteredComponentes = [];
        this.currentComponent = null;
        this.isEditMode = false;
        
        // Configuración de paginación
        this.pagination = {
            currentPage: 1,
            pageSize: PortalConfig.UI_CONFIG.DEFAULT_PAGE_SIZE,
            totalPages: 0,
            totalItems: 0
        };
        
        this.initializeEventListeners();
        this.setupFormHandling();
        this.setupModalHandling();
        this.setupPaginationHandling();
    }

    /**
     * Inicializa event listeners
     */
    initializeEventListeners() {
        // Botones de nueva acción
        document.getElementById('btn-nuevo-componente')?.addEventListener('click', () => {
            this.openCreateModal();
        });
        
        document.getElementById('btn-nuevo-componente-empty')?.addEventListener('click', () => {
            this.openCreateModal();
        });

        // Búsqueda y filtros
        document.getElementById('search-componentes')?.addEventListener('input', (e) => {
            this.handleSearch(e.target.value);
        });

        document.getElementById('filter-tipo')?.addEventListener('change', (e) => {
            this.handleTypeFilter(e.target.value);
        });

        document.getElementById('btn-aplicar-filtros')?.addEventListener('click', () => {
            this.applyFilters();
        });

        document.getElementById('btn-limpiar-filtros')?.addEventListener('click', () => {
            this.clearFilters();
        });

        // Cerrar alertas
        document.getElementById('alert-close')?.addEventListener('click', () => {
            PortalUtils.Alert.hide();
        });
    }

    /**
     * Configura manejo de la paginación
     */
    setupPaginationHandling() {
        // Selector de tamaño de página
        document.getElementById('page-size-select')?.addEventListener('change', (e) => {
            this.handlePageSizeChange(parseInt(e.target.value));
        });

        // Botones de navegación
        document.getElementById('btn-first')?.addEventListener('click', () => {
            this.goToPage(1);
        });

        document.getElementById('btn-prev')?.addEventListener('click', () => {
            this.goToPage(this.pagination.currentPage - 1);
        });

        document.getElementById('btn-next')?.addEventListener('click', () => {
            this.goToPage(this.pagination.currentPage + 1);
        });

        document.getElementById('btn-last')?.addEventListener('click', () => {
            this.goToPage(this.pagination.totalPages);
        });

        // Botones móviles
        document.getElementById('btn-prev-mobile')?.addEventListener('click', () => {
            this.goToPage(this.pagination.currentPage - 1);
        });

        document.getElementById('btn-next-mobile')?.addEventListener('click', () => {
            this.goToPage(this.pagination.currentPage + 1);
        });
    }

    /**
     * Configura manejo del formulario
     */
    setupFormHandling() {
        const form = document.getElementById('form-componente');
        const tipoSelect = document.getElementById('form-tipo');

        if (form) {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleFormSubmit();
            });
        }

        if (tipoSelect) {
            tipoSelect.addEventListener('change', (e) => {
                this.handleTypeChange(e.target.value);
            });
        }
    }

    /**
     * Configura manejo del modal
     */
    setupModalHandling() {
        // Botones de cerrar modal
        ['modal-close', 'modal-cancel'].forEach(id => {
            document.getElementById(id)?.addEventListener('click', () => {
                this.closeModal();
            });
        });

        // Click fuera del modal
        document.getElementById('modal-componente')?.addEventListener('click', (e) => {
            if (e.target.id === 'modal-componente') {
                this.closeModal();
            }
        });

        // Tecla ESC
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                const modal = document.getElementById('modal-componente');
                if (modal && !modal.classList.contains('hidden')) {
                    this.closeModal();
                }
            }
        });
    }

    /**
     * Carga componentes desde la API
     */
    async loadComponentes() {
        try {
            PortalUtils.Loading.show();
            this.showTableLoading();

            const componentes = await PortalApi.componentes.getAll();
            this.componentes = componentes || [];
            this.filteredComponentes = [...this.componentes];
            
            this.renderTable();
            
            if (this.componentes.length === 0) {
                this.showEmptyState();
            }

        } catch (error) {
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
            this.showEmptyState();
        } finally {
            PortalUtils.Loading.hide();
            this.hideTableLoading();
        }
    }

    /**
     * Renderiza la tabla de componentes
     */
    renderTable() {
        const tableBody = document.getElementById('componentes-table-body');
        const emptyState = document.getElementById('empty-state');
        
        if (!tableBody) return;

        if (this.filteredComponentes.length === 0) {
            this.showEmptyState();
            this.hidePagination();
            return;
        }

        // Ocultar estado vacío
        if (emptyState) {
            emptyState.classList.add('hidden');
        }

        // Calcular paginación
        this.updatePaginationInfo();

        // Obtener datos de la página actual
        const paginatedData = this.getPaginatedData();
        
        const html = paginatedData.map(componente => this.renderTableRow(componente)).join('');
        tableBody.innerHTML = html;

        // Agregar event listeners a botones de acción
        this.attachRowEventListeners();
        
        // Mostrar y actualizar controles de paginación
        this.showPagination();
        this.updatePaginationControls();
    }

    /**
     * Renderiza una fila de la tabla
     */
    renderTableRow(componente) {
        const promocionBadge = componente.promocionId 
            ? `<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                 ${componente.promocionDescripcion || 'Promoción activa'}
               </span>`
            : '<span class="text-gray-400 text-sm">Sin promoción</span>';

        const extraFields = this.getExtraFieldsDisplay(componente);

        return `
            <tr class="table-row-hover" data-component-id="${componente.id}">
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                        <div>
                            <div class="text-sm font-medium text-gray-900">${componente.id}</div>
                            <div class="text-sm text-gray-500">${componente.descripcion}</div>
                            ${extraFields ? `<div class="text-xs text-gray-400 mt-1">${extraFields}</div>` : ''}
                        </div>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                        ${PortalUtils.Format.formatComponentType(componente.tipoComponente)}
                    </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">${componente.marca}</div>
                    <div class="text-sm text-gray-500">${componente.modelo}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">
                        <div>Costo: ${PortalUtils.Format.formatCurrency(componente.costo)}</div>
                        <div>Precio: ${PortalUtils.Format.formatCurrency(componente.precioBase)}</div>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    ${promocionBadge}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div class="flex items-center space-x-2">
                        <button 
                            class="btn-edit text-indigo-600 hover:text-indigo-900"
                            data-component-id="${componente.id}"
                            title="Editar componente">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                            </svg>
                        </button>
                        <button 
                            class="btn-delete text-red-600 hover:text-red-900"
                            data-component-id="${componente.id}"
                            title="Eliminar componente">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                            </svg>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }

    /**
     * Obtiene campos extras para mostrar según el tipo
     */
    getExtraFieldsDisplay(componente) {
        if (componente.tipoComponente === 'DISCO_DURO' && componente.capacidadAlm) {
            return `Capacidad: ${componente.capacidadAlm}`;
        }
        if (componente.tipoComponente === 'TARJETA_VIDEO' && componente.memoria) {
            return `Memoria: ${componente.memoria}`;
        }
        return '';
    }

    /**
     * Agrega event listeners a las filas de la tabla
     */
    attachRowEventListeners() {
        // Botones de editar
        document.querySelectorAll('.btn-edit').forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                const componentId = button.getAttribute('data-component-id');
                this.openEditModal(componentId);
            });
        });

        // Botones de eliminar
        document.querySelectorAll('.btn-delete').forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                const componentId = button.getAttribute('data-component-id');
                this.confirmDelete(componentId);
            });
        });
    }

    /**
     * Maneja búsqueda con debounce
     */
    handleSearch(searchTerm) {
        clearTimeout(this.searchTimeout);
        this.searchTimeout = setTimeout(() => {
            this.applyFilters();
        }, 300);
    }

    /**
     * Maneja filtro por tipo
     */
    handleTypeFilter(tipo) {
        this.applyFilters();
    }

    /**
     * Aplica filtros y búsqueda
     */
    applyFilters() {
        const searchTerm = PortalUtils.DOM.getValue('search-componentes').toLowerCase();
        const selectedType = PortalUtils.DOM.getValue('filter-tipo');

        this.filteredComponentes = this.componentes.filter(componente => {
            // Filtro de búsqueda
            const matchesSearch = !searchTerm || 
                componente.id.toLowerCase().includes(searchTerm) ||
                componente.descripcion.toLowerCase().includes(searchTerm) ||
                componente.marca.toLowerCase().includes(searchTerm) ||
                componente.modelo.toLowerCase().includes(searchTerm);

            // Filtro de tipo
            const matchesType = !selectedType || componente.tipoComponente === selectedType;

            return matchesSearch && matchesType;
        });

        // Reiniciar paginación al aplicar filtros
        this.pagination.currentPage = 1;
        this.renderTable();
    }

    /**
     * Limpia filtros
     */
    clearFilters() {
        PortalUtils.DOM.setValue('search-componentes', '');
        PortalUtils.DOM.setValue('filter-tipo', '');
        this.filteredComponentes = [...this.componentes];
        
        // Reiniciar paginación al limpiar filtros
        this.pagination.currentPage = 1;
        this.renderTable();
    }

    /**
     * Abre modal para crear componente
     */
    openCreateModal() {
        this.isEditMode = false;
        this.currentComponent = null;
        
        document.getElementById('modal-title').textContent = 'Nuevo Componente';
        document.getElementById('modal-submit').textContent = 'Crear Componente';
        
        this.clearForm();
        this.showModal();
    }

    /**
     * Abre modal para editar componente
     */
    async openEditModal(componentId) {
        try {
            PortalUtils.Loading.show();
            
            const componente = await PortalApi.componentes.getById(componentId);
            
            this.isEditMode = true;
            this.currentComponent = componente;
            
            document.getElementById('modal-title').textContent = 'Editar Componente';
            document.getElementById('modal-submit').textContent = 'Actualizar Componente';
            
            this.populateForm(componente);
            this.showModal();
            
        } catch (error) {
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
        } finally {
            PortalUtils.Loading.hide();
        }
    }

    /**
     * Muestra el modal
     */
    showModal() {
        document.getElementById('modal-componente').classList.remove('hidden');
        document.body.classList.add('overflow-hidden');
        
        // Focus en primer campo
        setTimeout(() => {
            document.getElementById('form-id')?.focus();
        }, 100);
    }

    /**
     * Cierra el modal
     */
    closeModal() {
        document.getElementById('modal-componente').classList.add('hidden');
        document.body.classList.remove('overflow-hidden');
        this.clearForm();
    }

    /**
     * Limpia el formulario
     */
    clearForm() {
        const form = document.getElementById('form-componente');
        if (form) {
            form.reset();
        }
        
        // Habilitar campo ID si no es edición
        const idField = document.getElementById('form-id');
        if (idField) {
            idField.disabled = false;
        }
        
        // Ocultar campos específicos
        this.hideSpecificFields();
    }

    /**
     * Puebla el formulario con datos del componente
     */
    populateForm(componente) {
        PortalUtils.DOM.setValue('form-id', componente.id);
        PortalUtils.DOM.setValue('form-descripcion', componente.descripcion);
        PortalUtils.DOM.setValue('form-tipo', componente.tipoComponente);
        PortalUtils.DOM.setValue('form-marca', componente.marca);
        PortalUtils.DOM.setValue('form-modelo', componente.modelo);
        PortalUtils.DOM.setValue('form-costo', componente.costo);
        PortalUtils.DOM.setValue('form-precio-base', componente.precioBase);
        
        // Campos específicos
        if (componente.capacidadAlm) {
            PortalUtils.DOM.setValue('form-capacidad', componente.capacidadAlm);
        }
        if (componente.memoria) {
            PortalUtils.DOM.setValue('form-memoria', componente.memoria);
        }
        
        // Deshabilitar campo ID en edición
        const idField = document.getElementById('form-id');
        if (idField) {
            idField.disabled = true;
        }
        
        // Mostrar campos específicos según tipo
        this.handleTypeChange(componente.tipoComponente);
    }

    /**
     * Maneja cambio de tipo en el formulario
     */
    handleTypeChange(tipo) {
        this.hideSpecificFields();
        
        if (tipo === 'DISCO_DURO') {
            document.getElementById('field-capacidad')?.classList.remove('hidden');
        } else if (tipo === 'TARJETA_VIDEO') {
            document.getElementById('field-memoria')?.classList.remove('hidden');
        }
    }

    /**
     * Oculta campos específicos
     */
    hideSpecificFields() {
        document.getElementById('field-capacidad')?.classList.add('hidden');
        document.getElementById('field-memoria')?.classList.add('hidden');
    }

    /**
     * Maneja envío del formulario
     */
    async handleFormSubmit() {
        try {
            const formData = this.getFormData();
            
            // Validar datos
            const validationErrors = this.validateFormData(formData);
            if (validationErrors.length > 0) {
                PortalUtils.Alert.error(validationErrors.join('\n'));
                return;
            }

            PortalUtils.Loading.show();

            if (this.isEditMode) {
                await PortalApi.componentes.update(this.currentComponent.id, formData);
                PortalUtils.Alert.success(PortalConfig.MESSAGES.COMPONENT_UPDATED);
            } else {
                await PortalApi.componentes.create(formData);
                PortalUtils.Alert.success(PortalConfig.MESSAGES.COMPONENT_CREATED);
            }

            this.closeModal();
            await this.loadComponentes();

        } catch (error) {
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
        } finally {
            PortalUtils.Loading.hide();
        }
    }

    /**
     * Obtiene datos del formulario
     */
    getFormData() {
        const data = {
            id: PortalUtils.DOM.getValue('form-id'),
            descripcion: PortalUtils.DOM.getValue('form-descripcion'),
            tipoComponente: PortalUtils.DOM.getValue('form-tipo'),
            marca: PortalUtils.DOM.getValue('form-marca'),
            modelo: PortalUtils.DOM.getValue('form-modelo'),
            costo: parseFloat(PortalUtils.DOM.getValue('form-costo')),
            precioBase: parseFloat(PortalUtils.DOM.getValue('form-precio-base'))
        };

        // Campos específicos según tipo
        if (data.tipoComponente === 'DISCO_DURO') {
            data.capacidadAlm = PortalUtils.DOM.getValue('form-capacidad');
        }
        if (data.tipoComponente === 'TARJETA_VIDEO') {
            data.memoria = PortalUtils.DOM.getValue('form-memoria');
        }

        return data;
    }

    /**
     * Valida datos del formulario
     */
    validateFormData(data) {
        const errors = [];

        if (PortalUtils.Validation.isEmpty(data.id)) {
            errors.push('ID del componente es requerido');
        }
        
        if (PortalUtils.Validation.isEmpty(data.descripcion)) {
            errors.push('Descripción es requerida');
        }
        
        if (PortalUtils.Validation.isEmpty(data.tipoComponente)) {
            errors.push('Tipo de componente es requerido');
        }
        
        if (PortalUtils.Validation.isEmpty(data.marca)) {
            errors.push('Marca es requerida');
        }
        
        if (PortalUtils.Validation.isEmpty(data.modelo)) {
            errors.push('Modelo es requerido');
        }
        
        if (!PortalUtils.Validation.isValidPrice(data.costo)) {
            errors.push('Costo debe ser un número válido mayor a 0');
        }
        
        if (!PortalUtils.Validation.isValidPrice(data.precioBase)) {
            errors.push('Precio base debe ser un número válido mayor a 0');
        }

        return errors;
    }

    /**
     * Confirma eliminación de componente
     */
    async confirmDelete(componentId) {
        if (confirm(PortalConfig.MESSAGES.CONFIRM_DELETE)) {
            await this.deleteComponent(componentId);
        }
    }

    /**
     * Elimina componente
     */
    async deleteComponent(componentId) {
        try {
            PortalUtils.Loading.show();
            
            await PortalApi.componentes.delete(componentId);
            PortalUtils.Alert.success(PortalConfig.MESSAGES.COMPONENT_DELETED);
            
            await this.loadComponentes();
            
        } catch (error) {
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
        } finally {
            PortalUtils.Loading.hide();
        }
    }

    /**
     * Muestra estado vacío
     */
    showEmptyState() {
        const tableBody = document.getElementById('componentes-table-body');
        const emptyState = document.getElementById('empty-state');
        
        if (tableBody) {
            tableBody.innerHTML = '';
        }
        
        if (emptyState) {
            emptyState.classList.remove('hidden');
        }
    }

    /**
     * Muestra loading en tabla
     */
    showTableLoading() {
        const tableLoading = document.getElementById('table-loading');
        const emptyState = document.getElementById('empty-state');
        
        if (tableLoading) {
            tableLoading.classList.remove('hidden');
        }
        
        if (emptyState) {
            emptyState.classList.add('hidden');
        }
    }

    /**
     * Oculta loading en tabla
     */
    hideTableLoading() {
        const tableLoading = document.getElementById('table-loading');
        
        if (tableLoading) {
            tableLoading.classList.add('hidden');
        }
    }

    /**
     * Actualiza la información de paginación
     */
    updatePaginationInfo() {
        this.pagination.totalItems = this.filteredComponentes.length;
        this.pagination.totalPages = Math.ceil(this.pagination.totalItems / this.pagination.pageSize);
        
        // Ajustar página actual si es necesario
        if (this.pagination.currentPage > this.pagination.totalPages) {
            this.pagination.currentPage = Math.max(1, this.pagination.totalPages);
        }
    }

    /**
     * Obtiene los datos de la página actual
     */
    getPaginatedData() {
        const startIndex = (this.pagination.currentPage - 1) * this.pagination.pageSize;
        const endIndex = startIndex + this.pagination.pageSize;
        return this.filteredComponentes.slice(startIndex, endIndex);
    }

    /**
     * Maneja el cambio de tamaño de página
     */
    handlePageSizeChange(newPageSize) {
        this.pagination.pageSize = newPageSize;
        this.pagination.currentPage = 1; // Volver a la primera página
        this.renderTable();
    }

    /**
     * Navega a una página específica
     */
    goToPage(pageNumber) {
        if (pageNumber >= 1 && pageNumber <= this.pagination.totalPages) {
            this.pagination.currentPage = pageNumber;
            this.renderTable();
        }
    }

    /**
     * Muestra los controles de paginación
     */
    showPagination() {
        const paginationControls = document.getElementById('pagination-controls');
        if (paginationControls && this.pagination.totalItems > 0) {
            paginationControls.classList.remove('hidden');
        }
    }

    /**
     * Oculta los controles de paginación
     */
    hidePagination() {
        const paginationControls = document.getElementById('pagination-controls');
        if (paginationControls) {
            paginationControls.classList.add('hidden');
        }
    }

    /**
     * Actualiza los controles de paginación
     */
    updatePaginationControls() {
        this.updatePaginationInfo();
        this.updatePaginationButtons();
        this.updatePageNumbers();
        this.updatePaginationText();
    }

    /**
     * Actualiza el estado de los botones de paginación
     */
    updatePaginationButtons() {
        const { currentPage, totalPages } = this.pagination;
        
        // Botones principales
        const btnFirst = document.getElementById('btn-first');
        const btnPrev = document.getElementById('btn-prev');
        const btnNext = document.getElementById('btn-next');
        const btnLast = document.getElementById('btn-last');
        
        if (btnFirst) btnFirst.disabled = currentPage === 1;
        if (btnPrev) btnPrev.disabled = currentPage === 1;
        if (btnNext) btnNext.disabled = currentPage === totalPages;
        if (btnLast) btnLast.disabled = currentPage === totalPages;
        
        // Botones móviles
        const btnPrevMobile = document.getElementById('btn-prev-mobile');
        const btnNextMobile = document.getElementById('btn-next-mobile');
        
        if (btnPrevMobile) btnPrevMobile.disabled = currentPage === 1;
        if (btnNextMobile) btnNextMobile.disabled = currentPage === totalPages;
    }

    /**
     * Actualiza los números de página
     */
    updatePageNumbers() {
        const pageNumbersContainer = document.getElementById('page-numbers');
        if (!pageNumbersContainer) return;

        const { currentPage, totalPages } = this.pagination;
        const maxVisiblePages = PortalConfig.UI_CONFIG.MAX_VISIBLE_PAGES;

        let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

        // Ajustar si hay pocas páginas al final
        if (endPage - startPage + 1 < maxVisiblePages) {
            startPage = Math.max(1, endPage - maxVisiblePages + 1);
        }

        let pageNumbersHTML = '';

        for (let i = startPage; i <= endPage; i++) {
            const isActive = i === currentPage;
            const buttonClass = isActive
                ? 'relative inline-flex items-center px-4 py-2 border border-primary-500 bg-primary-50 text-sm font-medium text-primary-600'
                : 'relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50';

            pageNumbersHTML += `
                <button class="${buttonClass}" data-page="${i}">
                    ${i}
                </button>
            `;
        }

        pageNumbersContainer.innerHTML = pageNumbersHTML;

        // Agregar event listeners a los números de página
        pageNumbersContainer.querySelectorAll('button[data-page]').forEach(button => {
            button.addEventListener('click', (e) => {
                const pageNumber = parseInt(e.target.dataset.page);
                this.goToPage(pageNumber);
            });
        });
    }

    /**
     * Actualiza el texto de información de paginación
     */
    updatePaginationText() {
        const paginationInfo = document.getElementById('pagination-info');
        if (!paginationInfo) return;

        const { currentPage, pageSize, totalItems } = this.pagination;
        const startItem = (currentPage - 1) * pageSize + 1;
        const endItem = Math.min(currentPage * pageSize, totalItems);

        paginationInfo.innerHTML = `
            Mostrando <span class="font-medium">${startItem}</span> a 
            <span class="font-medium">${endItem}</span> de 
            <span class="font-medium">${totalItems}</span> resultados
        `;
    }

    /**
     * Inicializa el módulo
     */
    async initialize() {
        PortalConfig.debugLog('Initializing ComponentesManager');
        await this.loadComponentes();
    }
}

// Crear instancia global
window.ComponentesManager = new ComponentesManager();