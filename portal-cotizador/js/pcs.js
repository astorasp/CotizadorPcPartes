/**
 * Módulo para gestión de PCs y sus componentes
 */

class PcsManager {
    constructor() {
        this.pcs = [];
        this.filteredPcs = [];
        this.availableComponents = [];
        this.currentPc = null;
        this.currentPcComponents = [];
        
        // Configuración de paginación
        this.pagination = {
            currentPage: 1,
            pageSize: PortalConfig.UI_CONFIG.DEFAULT_PAGE_SIZE,
            totalPages: 0,
            totalItems: 0
        };
        
        this.initializeEventListeners();
        this.setupPaginationHandling();
        this.setupModalHandling();
    }

    /**
     * Inicializa event listeners
     */
    initializeEventListeners() {
        // Búsqueda y filtros
        document.getElementById('search-pcs')?.addEventListener('input', (e) => {
            this.handleSearch(e.target.value);
        });

        document.getElementById('filter-precio-rango')?.addEventListener('change', (e) => {
            this.handlePriceFilter(e.target.value);
        });

        document.getElementById('btn-aplicar-filtros-pcs')?.addEventListener('click', () => {
            this.applyFilters();
        });

        document.getElementById('btn-limpiar-filtros-pcs')?.addEventListener('click', () => {
            this.clearFilters();
        });

        // Navegación a componentes
        document.getElementById('btn-ir-componentes')?.addEventListener('click', () => {
            window.PortalApp.navigateToSection('componentes');
        });
    }

    /**
     * Configura manejo de la paginación
     */
    setupPaginationHandling() {
        // Selector de tamaño de página
        document.getElementById('pcs-page-size-select')?.addEventListener('change', (e) => {
            this.handlePageSizeChange(parseInt(e.target.value));
        });

        // Botones de navegación
        document.getElementById('btn-first-pcs')?.addEventListener('click', () => {
            this.goToPage(1);
        });

        document.getElementById('btn-prev-pcs')?.addEventListener('click', () => {
            this.goToPage(this.pagination.currentPage - 1);
        });

        document.getElementById('btn-next-pcs')?.addEventListener('click', () => {
            this.goToPage(this.pagination.currentPage + 1);
        });

        document.getElementById('btn-last-pcs')?.addEventListener('click', () => {
            this.goToPage(this.pagination.totalPages);
        });

        // Botones móviles
        document.getElementById('btn-prev-mobile-pcs')?.addEventListener('click', () => {
            this.goToPage(this.pagination.currentPage - 1);
        });

        document.getElementById('btn-next-mobile-pcs')?.addEventListener('click', () => {
            this.goToPage(this.pagination.currentPage + 1);
        });
    }

    /**
     * Configura manejo del modal
     */
    setupModalHandling() {
        // Botones de cerrar modal
        ['modal-pc-close', 'modal-pc-cancel'].forEach(id => {
            document.getElementById(id)?.addEventListener('click', () => {
                this.closeModal();
            });
        });

        // Click fuera del modal
        document.getElementById('modal-pc-componentes')?.addEventListener('click', (e) => {
            if (e.target.id === 'modal-pc-componentes') {
                this.closeModal();
            }
        });

        // Tecla ESC
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                const modal = document.getElementById('modal-pc-componentes');
                if (modal && !modal.classList.contains('hidden')) {
                    this.closeModal();
                }
            }
        });

        // Agregar componente
        document.getElementById('btn-agregar-componente')?.addEventListener('click', () => {
            this.handleAddComponent();
        });
    }

    /**
     * Carga PCs desde la API
     */
    async loadPcs() {
        try {
            PortalUtils.Loading.show();
            this.showTableLoading();

            // Obtener PCs usando el endpoint específico
            this.pcs = await PortalApi.pcs.getAll() || [];
            this.filteredPcs = [...this.pcs];
            
            PortalConfig.debugLog(`Loaded ${this.pcs.length} PCs from API`);
            
            this.renderTable();
            
            if (this.pcs.length === 0) {
                this.showEmptyState();
            }

        } catch (error) {
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
            PortalConfig.debugError('Error loading PCs:', error);
            this.showEmptyState();
        } finally {
            PortalUtils.Loading.hide();
            this.hideTableLoading();
        }
    }

    /**
     * Renderiza la tabla de PCs
     */
    renderTable() {
        const tableBody = document.getElementById('pcs-table-body');
        const emptyState = document.getElementById('pcs-empty-state');
        
        if (!tableBody) return;

        if (this.filteredPcs.length === 0) {
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
        
        const html = paginatedData.map(pc => this.renderTableRow(pc)).join('');
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
    renderTableRow(pc) {
        return `
            <tr class="table-row-hover" data-pc-id="${pc.id}">
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                        <div>
                            <div class="text-sm font-medium text-gray-900">${pc.id}</div>
                            <div class="text-sm text-gray-500">${pc.descripcion}</div>
                        </div>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">${pc.marca}</div>
                    <div class="text-sm text-gray-500">${pc.modelo}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">
                        <span id="components-count-${pc.id}" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                            Cargando...
                        </span>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">
                        <div>Costo: ${PortalUtils.Format.formatCurrency(pc.costo)}</div>
                        <div>Precio: ${PortalUtils.Format.formatCurrency(pc.precioBase)}</div>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div class="flex items-center space-x-2">
                        <button 
                            class="btn-gestionar-pc text-primary-600 hover:text-primary-900 font-medium"
                            data-pc-id="${pc.id}">
                            Gestionar Armado
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }

    /**
     * Agrega event listeners a las filas de la tabla
     */
    attachRowEventListeners() {
        // Botones de gestionar armado
        document.querySelectorAll('.btn-gestionar-pc').forEach(button => {
            button.addEventListener('click', (e) => {
                const pcId = e.target.dataset.pcId;
                this.openManageModal(pcId);
            });
        });

        // Cargar conteo de componentes para cada PC
        this.loadComponentsCounts();
    }

    /**
     * Carga el conteo de componentes para cada PC
     */
    async loadComponentsCounts() {
        for (const pc of this.getPaginatedData()) {
            try {
                const components = await PortalApi.pcs.getComponents(pc.id);
                const countElement = document.getElementById(`components-count-${pc.id}`);
                if (countElement) {
                    const count = components.length;
                    countElement.textContent = `${count} componente${count !== 1 ? 's' : ''}`;
                    countElement.className = count > 0 
                        ? 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800'
                        : 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800';
                }
            } catch (error) {
                const countElement = document.getElementById(`components-count-${pc.id}`);
                if (countElement) {
                    countElement.textContent = 'Error';
                    countElement.className = 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800';
                }
            }
        }
    }

    /**
     * Abre modal para gestionar componentes de PC
     */
    async openManageModal(pcId) {
        try {
            PortalUtils.Loading.show();
            
            // Obtener información de la PC
            const pc = await PortalApi.pcs.getById(pcId);
            this.currentPc = pc;

            // Cargar componentes disponibles
            await this.loadAvailableComponents();

            // Cargar componentes de la PC
            await this.loadPcComponents(pcId);
            
            // Actualizar información del modal
            this.updateModalInfo();
            
            // Mostrar modal
            this.showModal();
            
        } catch (error) {
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
        } finally {
            PortalUtils.Loading.hide();
        }
    }

    /**
     * Carga componentes disponibles para agregar
     */
    async loadAvailableComponents() {
        try {
            const allComponents = await PortalApi.componentes.getAll();
            // Filtrar solo componentes que no sean PCs
            this.availableComponents = allComponents.filter(comp => comp.tipoComponente !== 'PC');
            this.populateComponentSelect();
        } catch (error) {
            console.error('Error loading available components:', error);
            this.availableComponents = [];
        }
    }

    /**
     * Puebla el select de componentes disponibles
     */
    populateComponentSelect() {
        const select = document.getElementById('select-componente');
        if (!select) return;

        const optionsHtml = this.availableComponents.map(comp => 
            `<option value="${comp.id}">${comp.id} - ${comp.descripcion} (${PortalUtils.Format.formatComponentType(comp.tipoComponente)})</option>`
        ).join('');

        select.innerHTML = `<option value="">Seleccionar componente...</option>${optionsHtml}`;
    }

    /**
     * Carga componentes de la PC actual
     */
    async loadPcComponents(pcId) {
        try {
            this.currentPcComponents = await PortalApi.pcs.getComponents(pcId);
            this.renderPcComponentsTable();
            this.updateCostCalculations();
        } catch (error) {
            console.error('Error loading PC components:', error);
            this.currentPcComponents = [];
            this.renderPcComponentsTable();
        }
    }

    /**
     * Renderiza la tabla de componentes de la PC
     */
    renderPcComponentsTable() {
        const tableBody = document.getElementById('pc-componentes-table-body');
        const emptyState = document.getElementById('pc-componentes-empty');
        
        if (!tableBody) return;

        if (this.currentPcComponents.length === 0) {
            tableBody.innerHTML = '';
            if (emptyState) emptyState.classList.remove('hidden');
            return;
        }

        if (emptyState) emptyState.classList.add('hidden');

        const html = this.currentPcComponents.map(comp => this.renderPcComponentRow(comp)).join('');
        tableBody.innerHTML = html;

        // Agregar event listeners a botones de eliminar
        this.attachComponentRowEventListeners();
    }

    /**
     * Renderiza una fila de componente de PC
     */
    renderPcComponentRow(component) {
        const quantity = 1; // Por ahora asumimos cantidad 1, se puede mejorar
        const subtotal = component.costo * quantity;

        return `
            <tr>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm font-medium text-gray-900">${component.id}</div>
                    <div class="text-sm text-gray-500">${component.descripcion}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                        ${PortalUtils.Format.formatComponentType(component.tipoComponente)}
                    </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    ${quantity}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    ${PortalUtils.Format.formatCurrency(component.costo)}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    ${PortalUtils.Format.formatCurrency(subtotal)}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button 
                        class="btn-remove-component text-red-600 hover:text-red-900 font-medium"
                        data-component-id="${component.id}">
                        Quitar
                    </button>
                </td>
            </tr>
        `;
    }

    /**
     * Agrega event listeners a las filas de componentes
     */
    attachComponentRowEventListeners() {
        document.querySelectorAll('.btn-remove-component').forEach(button => {
            button.addEventListener('click', (e) => {
                const componentId = e.target.dataset.componentId;
                this.handleRemoveComponent(componentId);
            });
        });
    }

    /**
     * Maneja agregar componente a la PC
     */
    async handleAddComponent() {
        const componentId = PortalUtils.DOM.getValue('select-componente');
        const quantity = parseInt(PortalUtils.DOM.getValue('input-cantidad')) || 1;

        if (!componentId) {
            PortalUtils.Alert.error('Seleccione un componente para agregar');
            return;
        }

        try {
            PortalUtils.Loading.show();

            await PortalApi.pcs.addComponent(this.currentPc.id, {
                id: componentId,
                cantidad: quantity
            });

            PortalUtils.Alert.success('Componente agregado exitosamente');
            
            // Recargar componentes de la PC
            await this.loadPcComponents(this.currentPc.id);
            
            // Limpiar formulario
            PortalUtils.DOM.setValue('select-componente', '');
            PortalUtils.DOM.setValue('input-cantidad', '1');

        } catch (error) {
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
        } finally {
            PortalUtils.Loading.hide();
        }
    }

    /**
     * Maneja quitar componente de la PC
     */
    async handleRemoveComponent(componentId) {
        if (!confirm('¿Está seguro de que desea quitar este componente de la PC?')) {
            return;
        }

        try {
            PortalUtils.Loading.show();

            await PortalApi.pcs.removeComponent(this.currentPc.id, componentId);
            PortalUtils.Alert.success('Componente quitado exitosamente');
            
            // Recargar componentes de la PC
            await this.loadPcComponents(this.currentPc.id);

        } catch (error) {
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
        } finally {
            PortalUtils.Loading.hide();
        }
    }

    /**
     * Actualiza información del modal
     */
    updateModalInfo() {
        if (!this.currentPc) return;

        PortalUtils.DOM.setContent('modal-pc-id', this.currentPc.id);
        PortalUtils.DOM.setContent('pc-info-descripcion', this.currentPc.descripcion);
        PortalUtils.DOM.setContent('pc-info-marca-modelo', `${this.currentPc.marca} ${this.currentPc.modelo}`);
    }

    /**
     * Actualiza cálculos de costo en tiempo real
     */
    updateCostCalculations() {
        const totalCost = this.currentPcComponents.reduce((sum, comp) => sum + (comp.costo || 0), 0);
        const totalComponents = this.currentPcComponents.length;

        PortalUtils.DOM.setContent('pc-costo-total', PortalUtils.Format.formatCurrency(totalCost));
        PortalUtils.DOM.setContent('pc-total-componentes', totalComponents.toString());
    }

    /**
     * Muestra el modal
     */
    showModal() {
        document.getElementById('modal-pc-componentes').classList.remove('hidden');
        document.body.classList.add('overflow-hidden');
    }

    /**
     * Cierra el modal
     */
    closeModal() {
        document.getElementById('modal-pc-componentes').classList.add('hidden');
        document.body.classList.remove('overflow-hidden');
        this.currentPc = null;
        this.currentPcComponents = [];
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
     * Maneja filtro por rango de precios
     */
    handlePriceFilter(priceRange) {
        this.applyFilters();
    }

    /**
     * Aplica filtros y búsqueda
     */
    applyFilters() {
        const searchTerm = PortalUtils.DOM.getValue('search-pcs').toLowerCase();
        const priceRange = PortalUtils.DOM.getValue('filter-precio-rango');

        this.filteredPcs = this.pcs.filter(pc => {
            // Filtro de búsqueda
            const matchesSearch = !searchTerm || 
                pc.id.toLowerCase().includes(searchTerm) ||
                pc.descripcion.toLowerCase().includes(searchTerm) ||
                pc.marca.toLowerCase().includes(searchTerm) ||
                pc.modelo.toLowerCase().includes(searchTerm);

            // Filtro de precio
            const matchesPrice = this.matchesPriceRange(pc.precioBase, priceRange);

            return matchesSearch && matchesPrice;
        });

        // Reiniciar paginación al aplicar filtros
        this.pagination.currentPage = 1;
        this.renderTable();
    }

    /**
     * Verifica si un precio coincide con el rango seleccionado
     */
    matchesPriceRange(price, range) {
        if (!range) return true;

        switch (range) {
            case '0-1000':
                return price >= 0 && price <= 1000;
            case '1000-2000':
                return price > 1000 && price <= 2000;
            case '2000-5000':
                return price > 2000 && price <= 5000;
            case '5000+':
                return price > 5000;
            default:
                return true;
        }
    }

    /**
     * Limpia filtros
     */
    clearFilters() {
        PortalUtils.DOM.setValue('search-pcs', '');
        PortalUtils.DOM.setValue('filter-precio-rango', '');
        this.filteredPcs = [...this.pcs];
        
        // Reiniciar paginación al limpiar filtros
        this.pagination.currentPage = 1;
        this.renderTable();
    }

    /**
     * Muestra estado vacío
     */
    showEmptyState() {
        const tableBody = document.getElementById('pcs-table-body');
        const emptyState = document.getElementById('pcs-empty-state');
        
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
        const tableLoading = document.getElementById('pcs-table-loading');
        const emptyState = document.getElementById('pcs-empty-state');
        
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
        const tableLoading = document.getElementById('pcs-table-loading');
        
        if (tableLoading) {
            tableLoading.classList.add('hidden');
        }
    }

    // Métodos de paginación (similares a components.js)
    updatePaginationInfo() {
        this.pagination.totalItems = this.filteredPcs.length;
        this.pagination.totalPages = Math.ceil(this.pagination.totalItems / this.pagination.pageSize);
        
        if (this.pagination.currentPage > this.pagination.totalPages) {
            this.pagination.currentPage = Math.max(1, this.pagination.totalPages);
        }
    }

    getPaginatedData() {
        const startIndex = (this.pagination.currentPage - 1) * this.pagination.pageSize;
        const endIndex = startIndex + this.pagination.pageSize;
        return this.filteredPcs.slice(startIndex, endIndex);
    }

    handlePageSizeChange(newPageSize) {
        this.pagination.pageSize = newPageSize;
        this.pagination.currentPage = 1;
        this.renderTable();
    }

    goToPage(pageNumber) {
        if (pageNumber >= 1 && pageNumber <= this.pagination.totalPages) {
            this.pagination.currentPage = pageNumber;
            this.renderTable();
        }
    }

    showPagination() {
        const paginationControls = document.getElementById('pcs-pagination-controls');
        if (paginationControls && this.pagination.totalItems > 0) {
            paginationControls.classList.remove('hidden');
        }
    }

    hidePagination() {
        const paginationControls = document.getElementById('pcs-pagination-controls');
        if (paginationControls) {
            paginationControls.classList.add('hidden');
        }
    }

    updatePaginationControls() {
        this.updatePaginationInfo();
        this.updatePaginationButtons();
        this.updatePageNumbers();
        this.updatePaginationText();
    }

    updatePaginationButtons() {
        const { currentPage, totalPages } = this.pagination;
        
        const btnFirst = document.getElementById('btn-first-pcs');
        const btnPrev = document.getElementById('btn-prev-pcs');
        const btnNext = document.getElementById('btn-next-pcs');
        const btnLast = document.getElementById('btn-last-pcs');
        
        if (btnFirst) btnFirst.disabled = currentPage === 1;
        if (btnPrev) btnPrev.disabled = currentPage === 1;
        if (btnNext) btnNext.disabled = currentPage === totalPages;
        if (btnLast) btnLast.disabled = currentPage === totalPages;
        
        const btnPrevMobile = document.getElementById('btn-prev-mobile-pcs');
        const btnNextMobile = document.getElementById('btn-next-mobile-pcs');
        
        if (btnPrevMobile) btnPrevMobile.disabled = currentPage === 1;
        if (btnNextMobile) btnNextMobile.disabled = currentPage === totalPages;
    }

    updatePageNumbers() {
        const pageNumbersContainer = document.getElementById('pcs-page-numbers');
        if (!pageNumbersContainer) return;

        const { currentPage, totalPages } = this.pagination;
        const maxVisiblePages = PortalConfig.UI_CONFIG.MAX_VISIBLE_PAGES;

        let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

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

        pageNumbersContainer.querySelectorAll('button[data-page]').forEach(button => {
            button.addEventListener('click', (e) => {
                const pageNumber = parseInt(e.target.dataset.page);
                this.goToPage(pageNumber);
            });
        });
    }

    updatePaginationText() {
        const paginationInfo = document.getElementById('pcs-pagination-info');
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
        PortalConfig.debugLog('Initializing PcsManager');
        await this.loadPcs();
    }
}

// Crear instancia global
window.PcsManager = new PcsManager(); 