/**
 * Gestor de Pedidos
 * Maneja la funcionalidad completa de pedidos:
 * - Listar pedidos con paginación y búsqueda
 * - Generar pedidos desde cotizaciones existentes
 * - Ver detalles de pedidos
 * - Estados de carga y validaciones
 */
class PedidosManager {
    constructor() {
        this.pedidos = [];
        this.cotizaciones = [];
        this.proveedores = [];
        this.currentPage = 1;
        this.pageSize = 5;
        this.totalPages = 1;
        this.filteredPedidos = [];
        this.searchTerm = '';
        this.fechaDesde = '';
        this.isLoading = false;
        this.currentPedido = null;
        this.modalMode = 'create';
        
        PortalConfig.debugLog('PedidosManager inicializado');
    }

    async initialize() {
        try {
            PortalConfig.debugLog('Iniciando inicialización de PedidosManager');
            
            this.bindEvents();
            await this.loadPedidos();
            await this.loadCotizaciones();
            await this.loadProveedores();
            this.setDefaultDates();
            
            PortalConfig.debugLog('PedidosManager inicializado correctamente');
        } catch (error) {
            PortalConfig.debugError('Error en inicialización de PedidosManager:', error);
            this.showAlert('Error al inicializar el módulo de pedidos', 'error');
        }
    }

    bindEvents() {
        // Búsqueda y filtros
        document.getElementById('search-pedidos')?.addEventListener('input', 
            this.debounce(this.handleSearch.bind(this), 300));
        document.getElementById('filter-fecha-desde')?.addEventListener('change', this.handleDateFilter.bind(this));
        document.getElementById('btn-limpiar-pedidos')?.addEventListener('click', this.clearFilters.bind(this));

        // Botones principales
        document.getElementById('btn-nuevo-pedido')?.addEventListener('click', this.openCreateModal.bind(this));
        document.getElementById('btn-crear-primer-pedido')?.addEventListener('click', this.openCreateModal.bind(this));

        // Modal de pedido
        document.getElementById('modal-pedido-close')?.addEventListener('click', this.closeModal.bind(this));
        document.getElementById('modal-pedido-cancel')?.addEventListener('click', this.closeModal.bind(this));
        document.getElementById('form-pedido')?.addEventListener('submit', this.handleSubmit.bind(this));

        // Eventos del formulario
        document.getElementById('form-pedido-cotizacion')?.addEventListener('change', this.handleCotizacionChange.bind(this));
        document.getElementById('form-pedido-proveedor')?.addEventListener('change', this.handleProveedorChange.bind(this));
        document.getElementById('form-pedido-nivel-surtido')?.addEventListener('input', this.handleNivelSurtidoChange.bind(this));

        // Paginación
        document.getElementById('pedidos-page-size')?.addEventListener('change', this.handlePageSizeChange.bind(this));
        document.getElementById('btn-prev-page-pedidos')?.addEventListener('click', () => this.changePage(this.currentPage - 1));
        document.getElementById('btn-next-page-pedidos')?.addEventListener('click', () => this.changePage(this.currentPage + 1));
        document.getElementById('btn-first-page-pedidos')?.addEventListener('click', () => this.changePage(1));
        document.getElementById('btn-last-page-pedidos')?.addEventListener('click', () => this.changePage(this.totalPages));
        document.getElementById('btn-prev-page-mobile-pedidos')?.addEventListener('click', () => this.changePage(this.currentPage - 1));
        document.getElementById('btn-next-page-mobile-pedidos')?.addEventListener('click', () => this.changePage(this.currentPage + 1));
    }

    async loadPedidos() {
        try {
            this.showLoading(true);
            this.pedidos = await window.PortalApi.pedidos.getAll() || [];
            this.applyFilters();
            PortalConfig.debugLog('Pedidos cargados:', this.pedidos.length);
        } catch (error) {
            PortalConfig.debugError('Error cargando pedidos:', error);
            const message = window.PortalApi.handleError(error);
            this.showAlert('Error al cargar los pedidos: ' + message, 'error');
            this.pedidos = [];
            this.renderPedidos();
        } finally {
            this.showLoading(false);
        }
    }

    async loadCotizaciones() {
        try {
            this.cotizaciones = await window.PortalApi.cotizaciones.getAll() || [];
            this.populateCotizacionesSelect();
        } catch (error) {
            PortalConfig.debugError('Error cargando cotizaciones:', error);
        }
    }

    async loadProveedores() {
        try {
            this.proveedores = await window.PortalApi.proveedores.getAll() || [];
            this.populateProveedoresSelect();
        } catch (error) {
            PortalConfig.debugError('Error cargando proveedores:', error);
        }
    }

    setDefaultDates() {
        const today = new Date().toISOString().split('T')[0];
        const nextWeek = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
        
        const fechaEmision = document.getElementById('form-pedido-fecha-emision');
        const fechaEntrega = document.getElementById('form-pedido-fecha-entrega');
        
        if (fechaEmision) fechaEmision.value = today;
        if (fechaEntrega) fechaEntrega.value = nextWeek;
    }

    populateCotizacionesSelect() {
        const select = document.getElementById('form-pedido-cotizacion');
        if (!select) return;

        select.innerHTML = '<option value="">Seleccionar cotización...</option>';
        
        this.cotizaciones.forEach(cotizacion => {
            const option = document.createElement('option');
            option.value = cotizacion.folio;
            option.textContent = `COT-${cotizacion.folio} - ${this.formatDate(cotizacion.fecha)} - $${this.formatCurrency(cotizacion.total)}`;
            select.appendChild(option);
        });
    }

    populateProveedoresSelect() {
        const select = document.getElementById('form-pedido-proveedor');
        if (!select) return;

        select.innerHTML = '<option value="">Seleccionar proveedor...</option>';
        
        this.proveedores.forEach(proveedor => {
            const option = document.createElement('option');
            option.value = proveedor.cve;
            option.textContent = `${proveedor.cve} - ${proveedor.nombre}`;
            select.appendChild(option);
        });
    }

    handleCotizacionChange(event) {
        const cotizacionFolio = parseInt(event.target.value);
        const cotizacion = this.cotizaciones.find(c => c.folio === cotizacionFolio);
        
        const infoDiv = document.getElementById('cotizacion-info');
        if (cotizacion) {
            document.getElementById('cotizacion-fecha').textContent = this.formatDate(cotizacion.fecha);
            document.getElementById('cotizacion-total').textContent = '$' + this.formatCurrency(cotizacion.total);
            document.getElementById('cotizacion-componentes').textContent = `${cotizacion.detalles?.length || 0} componentes`;
            infoDiv.classList.remove('hidden');
        } else {
            infoDiv.classList.add('hidden');
        }
        
        this.updatePreview();
    }

    handleProveedorChange(event) {
        const proveedorCve = event.target.value;
        const proveedor = this.proveedores.find(p => p.cve === proveedorCve);
        
        const infoDiv = document.getElementById('proveedor-info');
        if (proveedor) {
            document.getElementById('proveedor-nombre').textContent = proveedor.nombre;
            document.getElementById('proveedor-razon').textContent = proveedor.razonSocial;
            document.getElementById('proveedor-pedidos').textContent = proveedor.numeroPedidos || 0;
            infoDiv.classList.remove('hidden');
        } else {
            infoDiv.classList.add('hidden');
        }
        
        this.updatePreview();
    }

    handleNivelSurtidoChange(event) {
        const value = event.target.value;
        document.getElementById('nivel-surtido-value').textContent = value;
        this.updatePreview();
    }

    updatePreview() {
        const cotizacionFolio = parseInt(document.getElementById('form-pedido-cotizacion').value);
        const proveedorCve = document.getElementById('form-pedido-proveedor').value;
        const nivelSurtido = parseInt(document.getElementById('form-pedido-nivel-surtido').value);
        
        const previewDiv = document.getElementById('pedido-preview');
        const submitBtn = document.getElementById('modal-pedido-submit');
        
        if (cotizacionFolio && proveedorCve) {
            const cotizacion = this.cotizaciones.find(c => c.folio === cotizacionFolio);
            if (cotizacion) {
                const totalComponentes = cotizacion.detalles?.length || 0;
                const componentesIncluidos = Math.ceil(totalComponentes * (nivelSurtido / 100));
                const totalEstimado = cotizacion.total * (nivelSurtido / 100);
                
                document.getElementById('preview-lineas').textContent = componentesIncluidos;
                document.getElementById('preview-total').textContent = '$' + this.formatCurrency(totalEstimado);
                
                previewDiv.classList.remove('hidden');
                submitBtn.disabled = false;
            }
        } else {
            previewDiv.classList.add('hidden');
            submitBtn.disabled = true;
        }
    }

    handleSearch(event) {
        this.searchTerm = event.target.value.toLowerCase();
        this.currentPage = 1;
        this.applyFilters();
    }

    handleDateFilter(event) {
        this.fechaDesde = event.target.value;
        this.currentPage = 1;
        this.applyFilters();
    }

    clearFilters() {
        this.searchTerm = '';
        this.fechaDesde = '';
        document.getElementById('search-pedidos').value = '';
        document.getElementById('filter-fecha-desde').value = '';
        this.currentPage = 1;
        this.applyFilters();
    }

    applyFilters() {
        let filtered = [...this.pedidos];

        if (this.searchTerm) {
            filtered = filtered.filter(pedido => 
                pedido.numPedido.toString().includes(this.searchTerm) ||
                pedido.nombreProveedor?.toLowerCase().includes(this.searchTerm) ||
                pedido.cveProveedor?.toLowerCase().includes(this.searchTerm)
            );
        }

        if (this.fechaDesde) {
            filtered = filtered.filter(pedido => 
                pedido.fechaEmision >= this.fechaDesde
            );
        }

        this.filteredPedidos = filtered;
        this.calculatePagination();
        this.renderPedidos();
    }

    calculatePagination() {
        this.totalPages = Math.ceil(this.filteredPedidos.length / this.pageSize);
        if (this.currentPage > this.totalPages && this.totalPages > 0) {
            this.currentPage = this.totalPages;
        }
    }

    changePage(page) {
        if (page >= 1 && page <= this.totalPages) {
            this.currentPage = page;
            this.renderPedidos();
        }
    }

    handlePageSizeChange(event) {
        this.pageSize = parseInt(event.target.value);
        this.currentPage = 1;
        this.calculatePagination();
        this.renderPedidos();
    }

    renderPedidos() {
        const startIndex = (this.currentPage - 1) * this.pageSize;
        const endIndex = startIndex + this.pageSize;
        const pageData = this.filteredPedidos.slice(startIndex, endIndex);

        this.renderTable(pageData);
        this.renderCards(pageData);
        this.updatePaginationControls();
        this.showEmptyState(this.filteredPedidos.length === 0);
    }

    renderTable(pedidos) {
        const tbody = document.getElementById('pedidos-table-body');
        if (!tbody) return;

        tbody.innerHTML = '';
        
        console.log('Renderizando tabla con', pedidos.length, 'pedidos');

        pedidos.forEach(pedido => {
            const row = document.createElement('tr');
            row.className = 'hover:bg-gray-50';
            
            row.innerHTML = `
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                        <div>
                            <div class="text-sm font-medium text-gray-900">#${pedido.numPedido}</div>
                            <div class="text-sm text-gray-500">${pedido.nombreProveedor}</div>
                            <div class="text-xs text-gray-400">${pedido.cveProveedor}</div>
                        </div>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">
                        <div><strong>Emisión:</strong> ${this.formatDate(pedido.fechaEmision)}</div>
                        <div><strong>Entrega:</strong> ${this.formatDate(pedido.fechaEntrega)}</div>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">
                        <div><strong>Líneas:</strong> ${pedido.totalDetalles || 0}</div>
                        <div><strong>Surtido:</strong> ${pedido.nivelSurtido}%</div>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-lg font-bold text-green-600">$${this.formatCurrency(pedido.total)}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div class="flex items-center justify-end space-x-2">
                        <button onclick="window.PedidosManager.viewPedido(${pedido.numPedido})" 
                                class="inline-flex items-center px-3 py-1 border border-transparent text-xs font-medium rounded text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                            <i class="fas fa-eye mr-1"></i>Ver Detalle
                        </button>
                    </div>
                </td>
            `;
            
            tbody.appendChild(row);
        });
        
        console.log('Tabla renderizada con', tbody.children.length, 'filas');
    }

    renderCards(pedidos) {
        const container = document.getElementById('pedidos-cards-body');
        if (!container) return;

        container.innerHTML = '';

        pedidos.forEach(pedido => {
            const card = document.createElement('div');
            card.className = 'bg-white p-4 rounded-lg border border-gray-200 shadow-sm';
            
            card.innerHTML = `
                <div class="flex items-center justify-between mb-3">
                    <div class="text-lg font-semibold text-gray-900">#${pedido.numPedido}</div>
                    <div class="text-lg font-bold text-green-600">$${this.formatCurrency(pedido.total)}</div>
                </div>
                
                <div class="space-y-2 mb-4">
                    <div class="flex justify-between text-sm">
                        <span class="text-gray-500">Proveedor:</span>
                        <span class="text-gray-900">${pedido.nombreProveedor}</span>
                    </div>
                    <div class="flex justify-between text-sm">
                        <span class="text-gray-500">Emisión:</span>
                        <span class="text-gray-900">${this.formatDate(pedido.fechaEmision)}</span>
                    </div>
                    <div class="flex justify-between text-sm">
                        <span class="text-gray-500">Entrega:</span>
                        <span class="text-gray-900">${this.formatDate(pedido.fechaEntrega)}</span>
                    </div>
                    <div class="flex justify-between text-sm">
                        <span class="text-gray-500">Líneas / Surtido:</span>
                        <span class="text-gray-900">${pedido.totalDetalles || 0} / ${pedido.nivelSurtido}%</span>
                    </div>
                </div>
                
                <div class="flex justify-end">
                    <button onclick="window.PedidosManager.viewPedido(${pedido.numPedido})" 
                            class="inline-flex items-center px-3 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                        <i class="fas fa-eye mr-2"></i>Ver Detalle
                    </button>
                </div>
            `;
            
            container.appendChild(card);
        });
    }

    updatePaginationControls() {
        const pagination = document.getElementById('pedidos-pagination');
        const showPagination = this.filteredPedidos.length > 0;
        
        if (pagination) {
            pagination.classList.toggle('hidden', !showPagination);
        }

        if (!showPagination) return;

        const start = (this.currentPage - 1) * this.pageSize + 1;
        const end = Math.min(this.currentPage * this.pageSize, this.filteredPedidos.length);
        const infoElement = document.getElementById('pedidos-pagination-info');
        
        if (infoElement) {
            const spans = infoElement.querySelectorAll('span');
            if (spans.length >= 3) {
                spans[0].textContent = start;
                spans[1].textContent = end;
                spans[2].textContent = this.filteredPedidos.length;
            }
        }

        this.updatePaginationButtons();
        this.updatePageNumbers();
    }

    updatePaginationButtons() {
        const buttons = {
            first: document.getElementById('btn-first-page-pedidos'),
            prev: document.getElementById('btn-prev-page-pedidos'),
            next: document.getElementById('btn-next-page-pedidos'),
            last: document.getElementById('btn-last-page-pedidos'),
            prevMobile: document.getElementById('btn-prev-page-mobile-pedidos'),
            nextMobile: document.getElementById('btn-next-page-mobile-pedidos')
        };

        const isFirstPage = this.currentPage === 1;
        const isLastPage = this.currentPage === this.totalPages;

        if (buttons.first) buttons.first.disabled = isFirstPage;
        if (buttons.prev) buttons.prev.disabled = isFirstPage;
        if (buttons.next) buttons.next.disabled = isLastPage;
        if (buttons.last) buttons.last.disabled = isLastPage;
        if (buttons.prevMobile) buttons.prevMobile.disabled = isFirstPage;
        if (buttons.nextMobile) buttons.nextMobile.disabled = isLastPage;
    }

    updatePageNumbers() {
        const container = document.getElementById('pedidos-page-numbers');
        if (!container) return;

        container.innerHTML = '';

        const maxVisiblePages = 5;
        let startPage = Math.max(1, this.currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(this.totalPages, startPage + maxVisiblePages - 1);

        if (endPage - startPage + 1 < maxVisiblePages) {
            startPage = Math.max(1, endPage - maxVisiblePages + 1);
        }

        for (let i = startPage; i <= endPage; i++) {
            const button = document.createElement('button');
            button.type = 'button';
            button.textContent = i;
            button.className = `relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                i === this.currentPage
                    ? 'bg-primary-50 border-primary-500 text-primary-600'
                    : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
            }`;
            
            button.addEventListener('click', () => this.changePage(i));
            container.appendChild(button);
        }
    }

    async viewPedido(numPedido) {
        try {
            const pedido = await window.PortalApi.pedidos.getById(numPedido);
            this.showPedidoDetails(pedido);
        } catch (error) {
            PortalConfig.debugError('Error cargando detalles del pedido:', error);
            const message = window.PortalApi.handleError(error);
            this.showAlert('Error al cargar los detalles del pedido: ' + message, 'error');
        }
    }

    showPedidoDetails(pedido) {
        const detallesHtml = pedido.detalles?.map(detalle => `
            <div class="flex justify-between py-2 border-b">
                <div>
                    <div class="font-medium">${detalle.descripcion}</div>
                    <div class="text-sm text-gray-500">ID: ${detalle.idArticulo}</div>
                </div>
                <div class="text-right">
                    <div class="font-medium">$${this.formatCurrency(detalle.totalCotizado)}</div>
                    <div class="text-sm text-gray-500">${detalle.cantidad} x $${this.formatCurrency(detalle.precioUnitario)}</div>
                </div>
            </div>
        `).join('') || '<p class="text-gray-500">No hay detalles disponibles</p>';

        const content = `
            <div class="max-w-2xl">
                <div class="mb-4 grid grid-cols-2 gap-4 text-sm">
                    <div><strong>Número:</strong> #${pedido.numPedido}</div>
                    <div><strong>Proveedor:</strong> ${pedido.nombreProveedor}</div>
                    <div><strong>Emisión:</strong> ${this.formatDate(pedido.fechaEmision)}</div>
                    <div><strong>Entrega:</strong> ${this.formatDate(pedido.fechaEntrega)}</div>
                    <div><strong>Surtido:</strong> ${pedido.nivelSurtido}%</div>
                    <div><strong>Total:</strong> <span class="text-green-600 font-bold">$${this.formatCurrency(pedido.total)}</span></div>
                </div>
                <div class="border-t pt-4">
                    <h4 class="font-medium mb-3">Detalles del Pedido (${pedido.totalDetalles} líneas):</h4>
                    <div class="max-h-64 overflow-y-auto">
                        ${detallesHtml}
                    </div>
                </div>
            </div>
        `;

        this.showCustomModal(`Detalles del Pedido #${pedido.numPedido}`, content);
    }

    showCustomModal(title, content) {
        const modal = document.createElement('div');
        modal.className = 'fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50';
        modal.innerHTML = `
            <div class="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
                <div class="mt-3">
                    <div class="flex items-center justify-between mb-4">
                        <h3 class="text-lg font-medium text-gray-900">${title}</h3>
                        <button class="modal-close text-gray-400 hover:text-gray-600">
                            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>
                    <div class="modal-content">
                        ${content}
                    </div>
                    <div class="flex justify-end mt-6">
                        <button class="modal-close px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500">
                            Cerrar
                        </button>
                    </div>
                </div>
            </div>
        `;

        modal.querySelectorAll('.modal-close').forEach(btn => {
            btn.addEventListener('click', () => modal.remove());
        });

        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.remove();
        });

        document.body.appendChild(modal);
    }

    openCreateModal() {
        this.modalMode = 'create';
        this.currentPedido = null;
        this.resetForm();
        this.showModal();
    }

    showModal() {
        const modal = document.getElementById('modal-pedido');
        if (modal) {
            modal.classList.remove('hidden');
            document.body.style.overflow = 'hidden';
        }
    }

    closeModal() {
        const modal = document.getElementById('modal-pedido');
        if (modal) {
            modal.classList.add('hidden');
            document.body.style.overflow = '';
            this.resetForm();
        }
    }

    resetForm() {
        const form = document.getElementById('form-pedido');
        if (form) {
            form.reset();
            this.setDefaultDates();
        }

        document.getElementById('cotizacion-info')?.classList.add('hidden');
        document.getElementById('proveedor-info')?.classList.add('hidden');
        document.getElementById('pedido-preview')?.classList.add('hidden');
        
        const slider = document.getElementById('form-pedido-nivel-surtido');
        const valueDisplay = document.getElementById('nivel-surtido-value');
        if (slider) slider.value = 100;
        if (valueDisplay) valueDisplay.textContent = '100';
        
        const submitBtn = document.getElementById('modal-pedido-submit');
        if (submitBtn) submitBtn.disabled = true;
    }

    async handleSubmit(event) {
        event.preventDefault();
        
        if (this.isLoading) return;

        try {
            this.isLoading = true;
            const submitBtn = document.getElementById('modal-pedido-submit');
            
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Generando...';
            }

            const formData = this.getFormData();
            const pedidoGenerado = await window.PortalApi.pedidos.generate(formData);

            this.showAlert('Pedido generado exitosamente', 'success');
            this.closeModal();
            await this.loadPedidos();
        } catch (error) {
            PortalConfig.debugError('Error generando pedido:', error);
            const message = window.PortalApi.handleError(error);
            this.showAlert('Error al generar el pedido: ' + message, 'error');
        } finally {
            this.isLoading = false;
            const submitBtn = document.getElementById('modal-pedido-submit');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fas fa-file-invoice mr-2"></i>Generar Pedido';
            }
        }
    }

    getFormData() {
        return {
            cotizacionId: parseInt(document.getElementById('form-pedido-cotizacion').value),
            cveProveedor: document.getElementById('form-pedido-proveedor').value,
            fechaEmision: document.getElementById('form-pedido-fecha-emision').value,
            fechaEntrega: document.getElementById('form-pedido-fecha-entrega').value,
            nivelSurtido: parseInt(document.getElementById('form-pedido-nivel-surtido').value)
        };
    }

    showLoading(show) {
        const loading = document.getElementById('pedidos-loading');
        const table = document.getElementById('pedidos-table-container');
        const cards = document.getElementById('pedidos-cards-container');
        
        if (loading) loading.classList.toggle('hidden', !show);
        if (table) table.classList.toggle('hidden', show);
        if (cards) cards.classList.toggle('hidden', show);
    }

    showEmptyState(show) {
        const empty = document.getElementById('pedidos-empty');
        const table = document.getElementById('pedidos-table-container');
        const cards = document.getElementById('pedidos-cards-container');
        const pagination = document.getElementById('pedidos-pagination');
        
        if (empty) empty.classList.toggle('hidden', !show);
        if (table) table.classList.toggle('hidden', show);
        if (cards) cards.classList.toggle('hidden', show);
        if (pagination) pagination.classList.toggle('hidden', show);
    }

    showAlert(message, type = 'info') {
        if (window.showAlert) {
            window.showAlert(message, type);
        } else {
            console.log(`[${type.toUpperCase()}] ${message}`);
        }
    }

    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    formatDate(dateString) {
        if (!dateString) return '-';
        try {
            return new Date(dateString).toLocaleDateString('es-MX');
        } catch {
            return dateString;
        }
    }

    formatCurrency(amount) {
        if (amount == null || isNaN(amount)) return '0.00';
        return parseFloat(amount).toLocaleString('es-MX', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }
}

// Exponer globalmente
window.PedidosManager = new PedidosManager(); 