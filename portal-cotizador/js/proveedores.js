/**
 * Gestión de Proveedores
 * Módulo para administrar proveedores del sistema
 */

class ProveedoresManager {
    constructor() {
        this.proveedores = [];
        this.filteredProveedores = [];
        this.currentProveedor = null;
        this.isEditMode = false;
        this.searchTimeout = null;
        
        // Configuración de paginación
        this.pagination = {
            currentPage: 1,
            pageSize: parseInt(PortalConfig.UI_CONFIG.DEFAULT_PAGE_SIZE) || 5,
            totalPages: 0,
            totalItems: 0
        };
    }

    /**
     * Inicializa el manager de proveedores
     */
    async initialize() {
        try {
            PortalConfig.debugLog('Inicializando ProveedoresManager...');
            
            this.initializeEventListeners();
            this.setupPaginationHandling();
            this.setupModalHandling();
            
            await this.loadProveedores();
            
            PortalConfig.debugLog('ProveedoresManager inicializado exitosamente');
            
        } catch (error) {
            PortalConfig.debugError('Error al inicializar ProveedoresManager:', error);
            
            // Mostrar estado de error al usuario
            this.showEmptyState();
            PortalUtils.Alert.error('Error al inicializar la sección de proveedores: ' + error.message);
            
            throw error; // Re-lanzar para que el error se propague al app.js
        }
    }

    /**
     * Configura los event listeners
     */
    initializeEventListeners() {
        // Búsqueda
        const searchInput = document.getElementById('search-proveedores');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.handleSearch(e.target.value);
            });
        }

        // Filtro de tipo de búsqueda
        const tipoBusquedaSelect = document.getElementById('filter-tipo-busqueda');
        if (tipoBusquedaSelect) {
            tipoBusquedaSelect.addEventListener('change', () => {
                this.applyFilters();
            });
        }

        // Botones principales
        document.getElementById('btn-nuevo-proveedor')?.addEventListener('click', () => {
            this.openCreateModal();
        });

        document.getElementById('btn-limpiar-proveedores')?.addEventListener('click', () => {
            this.clearFilters();
        });

        document.getElementById('btn-crear-primer-proveedor')?.addEventListener('click', () => {
            this.openCreateModal();
        });
    }

    /**
     * Configura el manejo de paginación
     */
    setupPaginationHandling() {
        // Cambio de tamaño de página
        const pageSizeSelect = document.getElementById('proveedores-page-size');
        if (pageSizeSelect) {
            pageSizeSelect.value = this.pagination.pageSize;
            pageSizeSelect.addEventListener('change', (e) => {
                this.handlePageSizeChange(parseInt(e.target.value));
            });
        }

        // Botones de navegación
        document.getElementById('btn-first-page-proveedores')?.addEventListener('click', () => {
            this.goToPage(1);
        });

        document.getElementById('btn-prev-page-proveedores')?.addEventListener('click', () => {
            if (this.pagination.currentPage > 1) {
                this.goToPage(this.pagination.currentPage - 1);
            }
        });

        document.getElementById('btn-next-page-proveedores')?.addEventListener('click', () => {
            if (this.pagination.currentPage < this.pagination.totalPages) {
                this.goToPage(this.pagination.currentPage + 1);
            }
        });

        document.getElementById('btn-last-page-proveedores')?.addEventListener('click', () => {
            this.goToPage(this.pagination.totalPages);
        });

        // Botones móvil
        document.getElementById('btn-prev-page-mobile-proveedores')?.addEventListener('click', () => {
            if (this.pagination.currentPage > 1) {
                this.goToPage(this.pagination.currentPage - 1);
            }
        });

        document.getElementById('btn-next-page-mobile-proveedores')?.addEventListener('click', () => {
            if (this.pagination.currentPage < this.pagination.totalPages) {
                this.goToPage(this.pagination.currentPage + 1);
            }
        });
    }

    /**
     * Configura el manejo del modal
     */
    setupModalHandling() {
        // Cerrar modal
        document.getElementById('modal-proveedor-close')?.addEventListener('click', () => {
            this.closeModal();
        });

        document.getElementById('modal-proveedor-cancel')?.addEventListener('click', () => {
            this.closeModal();
        });

        // Submit del formulario
        document.getElementById('form-proveedor')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleSubmitProveedor();
        });

        // Click fuera del modal
        document.getElementById('modal-proveedor')?.addEventListener('click', (e) => {
            if (e.target.id === 'modal-proveedor') {
                this.closeModal();
            }
        });
    }

    /**
     * Carga la lista de proveedores
     */
    async loadProveedores() {
        try {
            PortalConfig.debugLog('Iniciando carga de proveedores...');
            this.showTableLoading();
            
            const proveedores = await PortalApi.proveedores.getAll();
            
            this.proveedores = proveedores || [];
            this.filteredProveedores = [...this.proveedores];
            
            PortalConfig.debugLog('Proveedores cargados exitosamente:', this.proveedores.length);
            
            this.renderTable();
            
        } catch (error) {
            PortalConfig.debugError('Error al cargar proveedores:', error);
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error('Error al cargar proveedores: ' + message);
            this.showEmptyState();
        } finally {
            this.hideTableLoading();
        }
    }

    /**
     * Renderiza la tabla de proveedores
     */
    renderTable() {
        const tableBody = document.getElementById('proveedores-table-body');
        const cardsBody = document.getElementById('proveedores-cards-body');
        
        if (!tableBody || !cardsBody) return;

        // Verificar si hay datos
        if (this.filteredProveedores.length === 0) {
            this.showEmptyState();
            this.hidePagination();
            return;
        }

        // Ocultar estados vacíos
        document.getElementById('proveedores-empty')?.classList.add('hidden');

        // Calcular paginación
        this.pagination.totalItems = this.filteredProveedores.length;
        this.pagination.totalPages = Math.ceil(this.pagination.totalItems / this.pagination.pageSize);
        
        // Ajustar página actual si es necesario
        if (this.pagination.currentPage > this.pagination.totalPages && this.pagination.totalPages > 0) {
            this.pagination.currentPage = this.pagination.totalPages;
        }

        // Obtener datos paginados
        const paginatedData = this.getPaginatedData();

        // Limpiar contenedores
        tableBody.innerHTML = '';
        cardsBody.innerHTML = '';

        // Renderizar filas/cards
        paginatedData.forEach(proveedor => {
            // Tabla desktop
            const row = this.renderTableRow(proveedor);
            tableBody.appendChild(row);

            // Card mobile
            const card = this.renderMobileCard(proveedor);
            cardsBody.appendChild(card);
        });

        // Adjuntar event listeners
        this.attachRowEventListeners();

        // Actualizar paginación
        this.updatePaginationInfo();
        this.updatePaginationControls();
        this.showPagination();
    }

    /**
     * Renderiza una fila de la tabla
     */
    renderTableRow(proveedor) {
        const row = document.createElement('tr');
        row.className = 'hover:bg-gray-50';
        row.setAttribute('data-proveedor-cve', proveedor.cve);
        
        const numeroPedidos = proveedor.numeroPedidos || 0;
        const badgeColor = numeroPedidos > 0 ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-800';
        
        row.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                    <div>
                        <div class="text-sm font-medium text-gray-900">${proveedor.cve}</div>
                        <div class="text-sm text-gray-500">${proveedor.nombre}</div>
                    </div>
                </div>
            </td>
            <td class="px-6 py-4">
                <div class="text-sm text-gray-900">${proveedor.razonSocial}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${badgeColor}">
                        ${numeroPedidos} pedido${numeroPedidos !== 1 ? 's' : ''}
                    </span>
                </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <button class="btn-ver-proveedor text-primary-600 hover:text-primary-900 mr-3" data-proveedor-cve="${proveedor.cve}">
                    Ver
                </button>
                <button class="btn-editar-proveedor text-indigo-600 hover:text-indigo-900 mr-3" data-proveedor-cve="${proveedor.cve}">
                    Editar
                </button>
                <button class="btn-eliminar-proveedor text-red-600 hover:text-red-900" data-proveedor-cve="${proveedor.cve}">
                    Eliminar
                </button>
            </td>
        `;
        
        return row;
    }

    /**
     * Renderiza una card para móvil
     */
    renderMobileCard(proveedor) {
        const card = document.createElement('div');
        card.className = 'bg-white p-4 rounded-lg border border-gray-200 shadow-sm';
        card.setAttribute('data-proveedor-cve', proveedor.cve);
        
        const numeroPedidos = proveedor.numeroPedidos || 0;
        const badgeColor = numeroPedidos > 0 ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-800';
        
        card.innerHTML = `
            <div class="flex items-start justify-between mb-3">
                <div class="flex-1">
                    <h3 class="text-sm font-medium text-gray-900">${proveedor.cve}</h3>
                    <p class="text-sm text-gray-500 mt-1">${proveedor.nombre}</p>
                </div>
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${badgeColor}">
                    ${numeroPedidos} pedido${numeroPedidos !== 1 ? 's' : ''}
                </span>
            </div>
            
            <div class="mb-3">
                <p class="text-sm text-gray-900">${proveedor.razonSocial}</p>
            </div>
            
            <div class="flex space-x-2">
                <button class="btn-ver-proveedor flex-1 text-xs text-primary-600 bg-primary-50 px-3 py-2 rounded-md hover:bg-primary-100" data-proveedor-cve="${proveedor.cve}">
                    Ver
                </button>
                <button class="btn-editar-proveedor flex-1 text-xs text-indigo-600 bg-indigo-50 px-3 py-2 rounded-md hover:bg-indigo-100" data-proveedor-cve="${proveedor.cve}">
                    Editar
                </button>
                <button class="btn-eliminar-proveedor flex-1 text-xs text-red-600 bg-red-50 px-3 py-2 rounded-md hover:bg-red-100" data-proveedor-cve="${proveedor.cve}">
                    Eliminar
                </button>
            </div>
        `;
        
        return card;
    }

    /**
     * Adjunta event listeners a las filas
     */
    attachRowEventListeners() {
        // Botones de ver
        document.querySelectorAll('.btn-ver-proveedor').forEach(button => {
            button.addEventListener('click', (e) => {
                const cve = e.target.getAttribute('data-proveedor-cve');
                this.viewProveedor(cve);
            });
        });

        // Botones de editar
        document.querySelectorAll('.btn-editar-proveedor').forEach(button => {
            button.addEventListener('click', (e) => {
                const cve = e.target.getAttribute('data-proveedor-cve');
                this.editProveedor(cve);
            });
        });

        // Botones de eliminar
        document.querySelectorAll('.btn-eliminar-proveedor').forEach(button => {
            button.addEventListener('click', (e) => {
                const cve = e.target.getAttribute('data-proveedor-cve');
                this.deleteProveedor(cve);
            });
        });
    }

    /**
     * Abre modal para crear nuevo proveedor
     */
    openCreateModal() {
        this.isEditMode = false;
        this.currentProveedor = null;
        
        this.resetModal();
        this.showModal();
    }

    /**
     * Abre modal para ver proveedor existente
     */
    async viewProveedor(cve) {
        try {
            PortalUtils.Loading.show();
            
            const proveedor = await PortalApi.proveedores.getByCve(cve);
            
            this.currentProveedor = proveedor;
            this.isEditMode = false;
            
            this.populateModalWithProveedor(proveedor, false);
            this.showModal();
            
        } catch (error) {
            PortalConfig.debugError('Error al cargar proveedor:', error);
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
        } finally {
            PortalUtils.Loading.hide();
        }
    }

    /**
     * Abre modal para editar proveedor existente
     */
    async editProveedor(cve) {
        try {
            PortalUtils.Loading.show();
            
            const proveedor = await PortalApi.proveedores.getByCve(cve);
            
            this.currentProveedor = proveedor;
            this.isEditMode = true;
            
            this.populateModalWithProveedor(proveedor, true);
            this.showModal();
            
        } catch (error) {
            PortalConfig.debugError('Error al cargar proveedor:', error);
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
        } finally {
            PortalUtils.Loading.hide();
        }
    }

    /**
     * Elimina un proveedor
     */
    async deleteProveedor(cve) {
        const proveedor = this.proveedores.find(p => p.cve === cve);
        if (!proveedor) return;

        const confirmed = await PortalUtils.Alert.confirm(
            `¿Está seguro de que desea eliminar el proveedor "${proveedor.nombre}"?`,
            'Esta acción no se puede deshacer.'
        );

        if (!confirmed) return;

        try {
            PortalUtils.Loading.show();
            
            await PortalApi.proveedores.delete(cve);
            
            PortalUtils.Alert.success('Proveedor eliminado exitosamente');
            await this.loadProveedores();
            
        } catch (error) {
            PortalConfig.debugError('Error al eliminar proveedor:', error);
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
        } finally {
            PortalUtils.Loading.hide();
        }
    }

    /**
     * Restablece el modal a estado inicial
     */
    resetModal() {
        // Título y estado
        PortalUtils.DOM.setContent('modal-proveedor-title', 'Nuevo Proveedor');
        PortalUtils.DOM.setContent('modal-proveedor-cve', '-');
        
        // Limpiar formulario
        document.getElementById('form-proveedor').reset();
        
        // Habilitar campos
        document.getElementById('form-proveedor-cve').disabled = false;
        document.getElementById('form-proveedor-nombre').disabled = false;
        document.getElementById('form-proveedor-razon').disabled = false;
        
        // Mostrar botón de submit
        document.getElementById('modal-proveedor-submit').style.display = '';
        document.getElementById('modal-proveedor-submit').textContent = 'Guardar Proveedor';
        
        // Ocultar información adicional
        document.getElementById('proveedor-info-section').classList.add('hidden');
    }

    /**
     * Pobla el modal con datos de proveedor existente
     */
    populateModalWithProveedor(proveedor, editable) {
        // Título basado en modo
        const title = editable ? 'Editar Proveedor' : 'Ver Proveedor';
        PortalUtils.DOM.setContent('modal-proveedor-title', title);
        PortalUtils.DOM.setContent('modal-proveedor-cve', proveedor.cve);
        
        // Poblar campos
        PortalUtils.DOM.setValue('form-proveedor-cve', proveedor.cve);
        PortalUtils.DOM.setValue('form-proveedor-nombre', proveedor.nombre);
        PortalUtils.DOM.setValue('form-proveedor-razon', proveedor.razonSocial);
        
        // Configurar campos según modo
        document.getElementById('form-proveedor-cve').disabled = true; // Clave siempre deshabilitada en edición/vista
        document.getElementById('form-proveedor-nombre').disabled = !editable;
        document.getElementById('form-proveedor-razon').disabled = !editable;
        
        // Configurar botón
        if (editable) {
            document.getElementById('modal-proveedor-submit').style.display = '';
            document.getElementById('modal-proveedor-submit').textContent = 'Actualizar Proveedor';
        } else {
            document.getElementById('modal-proveedor-submit').style.display = 'none';
        }
        
        // Mostrar información adicional
        if (proveedor.numeroPedidos !== undefined) {
            PortalUtils.DOM.setContent('proveedor-num-pedidos', proveedor.numeroPedidos);
            document.getElementById('proveedor-info-section').classList.remove('hidden');
        }
    }

    /**
     * Maneja envío del formulario
     */
    async handleSubmitProveedor() {
        if (!this.validateProveedor()) {
            return;
        }

        try {
            PortalUtils.Loading.show();
            
            const proveedorData = {
                cve: PortalUtils.DOM.getValue('form-proveedor-cve').trim(),
                nombre: PortalUtils.DOM.getValue('form-proveedor-nombre').trim(),
                razonSocial: PortalUtils.DOM.getValue('form-proveedor-razon').trim()
            };
            
            if (this.isEditMode && this.currentProveedor) {
                // Actualizar proveedor existente
                const updateData = {
                    nombre: proveedorData.nombre,
                    razonSocial: proveedorData.razonSocial
                };
                
                PortalConfig.debugLog('Actualizando proveedor:', this.currentProveedor.cve, updateData);
                
                await PortalApi.proveedores.update(this.currentProveedor.cve, updateData);
                PortalUtils.Alert.success('Proveedor actualizado exitosamente');
            } else {
                // Crear nuevo proveedor
                PortalConfig.debugLog('Creando proveedor:', proveedorData);
                
                await PortalApi.proveedores.create(proveedorData);
                PortalUtils.Alert.success('Proveedor creado exitosamente');
            }
            
            this.closeModal();
            await this.loadProveedores();
            
        } catch (error) {
            PortalConfig.debugError('Error al guardar proveedor:', error);
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
        } finally {
            PortalUtils.Loading.hide();
        }
    }

    /**
     * Valida proveedor antes de enviar
     */
    validateProveedor() {
        const errors = [];
        
        const cve = PortalUtils.DOM.getValue('form-proveedor-cve').trim();
        const nombre = PortalUtils.DOM.getValue('form-proveedor-nombre').trim();
        const razonSocial = PortalUtils.DOM.getValue('form-proveedor-razon').trim();
        
        if (!cve) {
            errors.push('La clave del proveedor es requerida');
        } else if (cve.length > 10) {
            errors.push('La clave no puede exceder 10 caracteres');
        }
        
        if (!nombre) {
            errors.push('El nombre comercial es requerido');
        } else if (nombre.length > 100) {
            errors.push('El nombre no puede exceder 100 caracteres');
        }
        
        if (!razonSocial) {
            errors.push('La razón social es requerida');
        } else if (razonSocial.length > 200) {
            errors.push('La razón social no puede exceder 200 caracteres');
        }
        
        if (errors.length > 0) {
            PortalUtils.Alert.error(errors.join('\n'));
            return false;
        }
        
        return true;
    }

    /**
     * Muestra el modal
     */
    showModal() {
        document.getElementById('modal-proveedor').classList.remove('hidden');
        document.body.classList.add('overflow-hidden');
    }

    /**
     * Cierra el modal
     */
    closeModal() {
        document.getElementById('modal-proveedor').classList.add('hidden');
        document.body.classList.remove('overflow-hidden');
        this.currentProveedor = null;
        this.isEditMode = false;
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
     * Aplica filtros y búsqueda
     */
    async applyFilters() {
        const searchTerm = PortalUtils.DOM.getValue('search-proveedores').toLowerCase().trim();
        const tipoBusqueda = PortalUtils.DOM.getValue('filter-tipo-busqueda');

        if (!searchTerm) {
            // Sin búsqueda, mostrar todos
            this.filteredProveedores = [...this.proveedores];
        } else {
            try {
                PortalUtils.Loading.show();

                if (tipoBusqueda === 'nombre') {
                    // Búsqueda específica por nombre
                    const resultados = await PortalApi.proveedores.searchByName(searchTerm);
                    this.filteredProveedores = resultados || [];
                } else if (tipoBusqueda === 'razon') {
                    // Búsqueda específica por razón social
                    const resultados = await PortalApi.proveedores.searchByRazon(searchTerm);
                    this.filteredProveedores = resultados || [];
                } else {
                    // Búsqueda general (local)
                    this.filteredProveedores = this.proveedores.filter(proveedor => {
                        return proveedor.cve.toLowerCase().includes(searchTerm) ||
                               proveedor.nombre.toLowerCase().includes(searchTerm) ||
                               proveedor.razonSocial.toLowerCase().includes(searchTerm);
                    });
                }
            } catch (error) {
                PortalConfig.debugError('Error en búsqueda:', error);
                const message = PortalApi.handleError(error);
                PortalUtils.Alert.error(message);
                this.filteredProveedores = [];
            } finally {
                PortalUtils.Loading.hide();
            }
        }

        // Reiniciar paginación al aplicar filtros
        this.pagination.currentPage = 1;
        this.renderTable();
    }

    /**
     * Limpia filtros
     */
    clearFilters() {
        PortalUtils.DOM.setValue('search-proveedores', '');
        PortalUtils.DOM.setValue('filter-tipo-busqueda', 'general');
        this.filteredProveedores = [...this.proveedores];
        
        // Reiniciar paginación al limpiar filtros
        this.pagination.currentPage = 1;
        this.renderTable();
    }

    /**
     * Muestra estado vacío
     */
    showEmptyState() {
        document.getElementById('proveedores-table-container')?.classList.add('hidden');
        document.getElementById('proveedores-cards-container')?.classList.add('hidden');
        document.getElementById('proveedores-empty')?.classList.remove('hidden');
    }

    /**
     * Muestra estado de carga de tabla
     */
    showTableLoading() {
        document.getElementById('proveedores-table-container')?.classList.add('hidden');
        document.getElementById('proveedores-cards-container')?.classList.add('hidden');
        document.getElementById('proveedores-empty')?.classList.add('hidden');
        document.getElementById('proveedores-loading')?.classList.remove('hidden');
    }

    /**
     * Oculta estado de carga de tabla
     */
    hideTableLoading() {
        document.getElementById('proveedores-loading')?.classList.add('hidden');
        document.getElementById('proveedores-table-container')?.classList.remove('hidden');
        document.getElementById('proveedores-cards-container')?.classList.remove('hidden');
    }

    /**
     * Actualiza información de paginación
     */
    updatePaginationInfo() {
        const info = document.getElementById('proveedores-pagination-info');
        if (!info) return;

        const start = ((this.pagination.currentPage - 1) * this.pagination.pageSize) + 1;
        const end = Math.min(this.pagination.currentPage * this.pagination.pageSize, this.pagination.totalItems);

        info.innerHTML = `
            Mostrando <span class="font-medium">${start}</span> a <span class="font-medium">${end}</span> de <span class="font-medium">${this.pagination.totalItems}</span> resultados
        `;
    }

    /**
     * Obtiene datos paginados
     */
    getPaginatedData() {
        const start = (this.pagination.currentPage - 1) * this.pagination.pageSize;
        const end = start + this.pagination.pageSize;
        return this.filteredProveedores.slice(start, end);
    }

    /**
     * Maneja cambio de tamaño de página
     */
    handlePageSizeChange(newPageSize) {
        this.pagination.pageSize = newPageSize;
        this.pagination.currentPage = 1;
        this.renderTable();
    }

    /**
     * Va a una página específica
     */
    goToPage(pageNumber) {
        if (pageNumber >= 1 && pageNumber <= this.pagination.totalPages) {
            this.pagination.currentPage = pageNumber;
            this.renderTable();
        }
    }

    /**
     * Muestra controles de paginación
     */
    showPagination() {
        if (this.pagination.totalPages > 1) {
            document.getElementById('proveedores-pagination')?.classList.remove('hidden');
        } else {
            this.hidePagination();
        }
    }

    /**
     * Oculta controles de paginación
     */
    hidePagination() {
        document.getElementById('proveedores-pagination')?.classList.add('hidden');
    }

    /**
     * Actualiza controles de paginación
     */
    updatePaginationControls() {
        this.updatePaginationButtons();
        this.updatePageNumbers();
    }

    /**
     * Actualiza botones de paginación
     */
    updatePaginationButtons() {
        const isFirstPage = this.pagination.currentPage === 1;
        const isLastPage = this.pagination.currentPage === this.pagination.totalPages;

        // Botones desktop
        const btnFirst = document.getElementById('btn-first-page-proveedores');
        const btnPrev = document.getElementById('btn-prev-page-proveedores');
        const btnNext = document.getElementById('btn-next-page-proveedores');
        const btnLast = document.getElementById('btn-last-page-proveedores');

        if (btnFirst) btnFirst.disabled = isFirstPage;
        if (btnPrev) btnPrev.disabled = isFirstPage;
        if (btnNext) btnNext.disabled = isLastPage;
        if (btnLast) btnLast.disabled = isLastPage;

        // Botones móvil
        const btnPrevMobile = document.getElementById('btn-prev-page-mobile-proveedores');
        const btnNextMobile = document.getElementById('btn-next-page-mobile-proveedores');

        if (btnPrevMobile) btnPrevMobile.disabled = isFirstPage;
        if (btnNextMobile) btnNextMobile.disabled = isLastPage;
    }

    /**
     * Actualiza números de página
     */
    updatePageNumbers() {
        const container = document.getElementById('proveedores-page-numbers');
        if (!container) return;

        container.innerHTML = '';

        const maxVisiblePages = PortalConfig.UI_CONFIG.MAX_VISIBLE_PAGES || 5;
        let startPage = Math.max(1, this.pagination.currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(this.pagination.totalPages, startPage + maxVisiblePages - 1);

        if (endPage - startPage + 1 < maxVisiblePages) {
            startPage = Math.max(1, endPage - maxVisiblePages + 1);
        }

        for (let i = startPage; i <= endPage; i++) {
            const button = document.createElement('button');
            button.className = `relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                i === this.pagination.currentPage
                    ? 'z-10 bg-primary-50 border-primary-500 text-primary-600'
                    : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
            }`;
            button.textContent = i;
            button.addEventListener('click', () => this.goToPage(i));
            container.appendChild(button);
        }
    }
}

// Crear instancia global
window.ProveedoresManager = new ProveedoresManager(); 