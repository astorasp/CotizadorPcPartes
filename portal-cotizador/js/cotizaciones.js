/**
 * Módulo para gestión de cotizaciones
 * Maneja la creación, visualización y administración de cotizaciones
 */

class CotizacionesManager {
    constructor() {
        this.cotizaciones = [];
        this.filteredCotizaciones = [];
        this.availableComponents = [];
        this.currentCotizacion = null;
        this.currentComponents = [];
        this.currentImpuestos = []; 
        this.isEditMode = false;
        this.searchTimeout = null;
        
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
        console.log('CotizacionesManager initialized');
        
        // Botones principales
        PortalUtils.DOM.addEventListener('btn-nueva-cotizacion', 'click', () => this.openCreateModal());
        PortalUtils.DOM.addEventListener('btn-primera-cotizacion', 'click', () => this.openCreateModal());
        
        // Búsqueda y filtros
        PortalUtils.DOM.addEventListener('search-cotizaciones', 'input', (e) => {
            this.handleSearch(e.target.value);
        });
        
        PortalUtils.DOM.addEventListener('filter-fecha-cotizacion', 'change', () => {
            this.applyFilters();
        });
        
        PortalUtils.DOM.addEventListener('btn-limpiar-cotizaciones', 'click', () => {
            this.clearFilters();
        });
    }

    /**
     * Configura manejo de paginación
     */
    setupPaginationHandling() {
        // Cambio de tamaño de página
        PortalUtils.DOM.addEventListener('page-size-cotizaciones', 'change', (e) => {
            this.handlePageSizeChange(parseInt(e.target.value));
        });

        // Navegación de páginas
        PortalUtils.DOM.addEventListener('btn-first-cotizaciones', 'click', () => this.goToPage(1));
        PortalUtils.DOM.addEventListener('btn-previous-cotizaciones', 'click', () => {
            this.goToPage(this.pagination.currentPage - 1);
        });
        PortalUtils.DOM.addEventListener('btn-next-cotizaciones', 'click', () => {
            this.goToPage(this.pagination.currentPage + 1);
        });
        PortalUtils.DOM.addEventListener('btn-last-cotizaciones', 'click', () => {
            this.goToPage(this.pagination.totalPages);
        });
    }

    /**
     * Configura manejo de modal
     */
    setupModalHandling() {
        // Cerrar modal
        PortalUtils.DOM.addEventListener('modal-cotizacion-close', 'click', () => this.closeModal());
        PortalUtils.DOM.addEventListener('modal-cotizacion-cancel', 'click', () => this.closeModal());
        
        // Formulario de cotización
        PortalUtils.DOM.addEventListener('form-cotizacion', 'submit', (e) => {
            e.preventDefault();
            this.handleSubmitCotizacion();
        });
        
        // Gestión de impuestos
        PortalUtils.DOM.addEventListener('btn-add-impuesto', 'click', () => this.addImpuestoItem());
        
        // Gestión de componentes
        PortalUtils.DOM.addEventListener('btn-agregar-componente-cotizacion', 'click', () => {
            this.handleAddComponent();
        });
    }

    /**
     * Carga todas las cotizaciones
     */
    async loadCotizaciones() {
        try {
            PortalUtils.Loading.show();
            this.showTableLoading();

            PortalConfig.debugLog('Cargando cotizaciones...');
            
            const cotizaciones = await PortalApi.cotizaciones.getAll();
            
            PortalConfig.debugLog('Cotizaciones cargadas:', cotizaciones);
            
            this.cotizaciones = Array.isArray(cotizaciones) ? cotizaciones : [];
            this.filteredCotizaciones = [...this.cotizaciones];
            
            await this.loadAvailableComponents();
            
            this.renderTable();
            
        } catch (error) {
            PortalConfig.debugError('Error al cargar cotizaciones:', error);
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
            this.showEmptyState();
        } finally {
            PortalUtils.Loading.hide();
        }
    }

    /**
     * Carga componentes disponibles para cotización
     */
    async loadAvailableComponents() {
        try {
            const components = await PortalApi.componentes.getAll();
            this.availableComponents = Array.isArray(components) ? components : [];
            this.populateComponentSelects();
        } catch (error) {
            PortalConfig.debugError('Error al cargar componentes:', error);
        }
    }

    /**
     * Pobla los selects de componentes
     */
    populateComponentSelects() {
        const select = document.getElementById('select-componente-cotizacion');
        if (!select) return;

        select.innerHTML = '<option value="">Seleccionar componente...</option>';
        
        this.availableComponents.forEach(component => {
            const option = document.createElement('option');
            option.value = component.id;
            option.textContent = `${component.id} - ${component.descripcion} (${PortalUtils.Format.formatComponentType(component.tipoComponente)}) - ${PortalUtils.Format.formatCurrency(component.precioBase)}`;
            select.appendChild(option);
        });
    }

    /**
     * Renderiza la tabla de cotizaciones
     */
    renderTable() {
        this.hideTableLoading();
        
        if (this.filteredCotizaciones.length === 0) {
            this.showEmptyState();
            return;
        }

        this.updatePaginationInfo();
        const paginatedData = this.getPaginatedData();
        
        const tableBody = document.getElementById('cotizaciones-table-body');
        const tableContainer = document.getElementById('cotizaciones-table-container');
        const emptyState = document.getElementById('cotizaciones-empty-state');
        
        if (tableBody && tableContainer && emptyState) {
            // Limpiar tabla
            tableBody.innerHTML = '';
            
            // Renderizar filas
            paginatedData.forEach(cotizacion => {
                const row = this.renderTableRow(cotizacion);
                tableBody.appendChild(row);
            });
            
            // Mostrar tabla y ocultar empty state
            tableContainer.classList.remove('hidden');
            emptyState.classList.add('hidden');
            
            this.showPagination();
            this.updatePaginationControls();
            this.attachRowEventListeners();
        }
    }

    /**
     * Renderiza una fila de la tabla
     */
    renderTableRow(cotizacion) {
        const row = document.createElement('tr');
        row.className = 'hover:bg-gray-50';
        // Usar folio en lugar de id
        const cotizacionId = cotizacion.folio || cotizacion.id;
        row.setAttribute('data-cotizacion-id', cotizacionId);
        
        const totalComponents = cotizacion.detalles ? cotizacion.detalles.length : 0;
        const componentsBadgeColor = totalComponents > 0 ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800';
        
        // Manejar diferentes formatos de fecha
        const fecha = cotizacion.fecha || cotizacion.fechaCreacion || 'N/A';
        const fechaFormateada = fecha !== 'N/A' ? PortalUtils.Format.formatDate(fecha) : 'N/A';
        
        // Asignar tipo de cotizador por defecto si no existe
        const tipoCotizador = cotizacion.tipoCotizador || 'Estándar';
        
        row.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                    <div>
                        <div class="text-sm font-medium text-gray-900">#${cotizacionId}</div>
                        <div class="text-sm text-gray-500">${tipoCotizador}</div>
                    </div>
                </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-900">
                    ${fechaFormateada}
                </div>
            </td>
            <td class="px-6 py-4">
                <div class="text-sm text-gray-900">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${componentsBadgeColor}">
                        ${totalComponents} componente${totalComponents !== 1 ? 's' : ''}
                    </span>
                </div>
                <div class="text-xs text-gray-500 mt-1">
                    Impuestos aplicados
                </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">
                    <div>Subtotal: ${PortalUtils.Format.formatCurrency(cotizacion.subtotal || 0)}</div>
                    <div class="text-orange-600">Impuestos: ${PortalUtils.Format.formatCurrency(cotizacion.impuestos || 0)}</div>
                    <div class="text-lg font-bold text-green-600">Total: ${PortalUtils.Format.formatCurrency(cotizacion.total || 0)}</div>
                </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <button class="btn-ver-cotizacion text-primary-600 hover:text-primary-900 mr-3" data-cotizacion-id="${cotizacionId}">
                    Ver detalles
                </button>
            </td>
        `;
        
        return row;
    }

    /**
     * Adjunta event listeners a las filas
     */
    attachRowEventListeners() {
        // Botones de ver detalles
        document.querySelectorAll('.btn-ver-cotizacion').forEach(button => {
            button.addEventListener('click', (e) => {
                const cotizacionId = e.target.getAttribute('data-cotizacion-id');
                this.openViewModal(cotizacionId);
            });
        });
    }

    /**
     * Abre modal para crear nueva cotización
     */
    openCreateModal() {
        this.isEditMode = false;
        this.currentCotizacion = null;
        this.currentComponents = [];
        this.currentImpuestos = [];
        
        this.resetModal();
        this.populateComponentSelects();
        this.setupDefaultImpuesto();
        this.showModal();
    }

    /**
     * Abre modal para ver cotización existente
     */
    async openViewModal(cotizacionId) {
        try {
            PortalUtils.Loading.show();
            
            const cotizacion = await PortalApi.cotizaciones.getById(cotizacionId);
            
            // Los datos llegan correctamente del backend
            
            this.isEditMode = true;
            this.currentCotizacion = cotizacion;
            
            // Manejar detalles con estructura de BD real
            this.currentComponents = cotizacion.detalles || [];
            this.currentImpuestos = cotizacion.impuestosAplicados || [];
            
            this.populateModalWithCotizacion(cotizacion);
            this.showModal();
            
        } catch (error) {
            PortalConfig.debugError('Error al cargar cotización:', error);
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
        PortalUtils.DOM.setContent('modal-cotizacion-title', 'Nueva Cotización');
        PortalUtils.DOM.setContent('modal-cotizacion-id', '-');
        
        // Fecha actual
        const today = new Date().toISOString().split('T')[0];
        PortalUtils.DOM.setValue('form-fecha-cotizacion', today);
        
        // Limpiar formulario
        document.getElementById('form-cotizacion').reset();
        PortalUtils.DOM.setValue('form-fecha-cotizacion', today);
        
        // Limpiar contenedores
        this.currentComponents = [];
        this.currentImpuestos = [];
        
        this.renderComponentsTable();
        this.renderImpuestosContainer();
        this.updateTotals();
    }

    /**
     * Configura impuesto por defecto (IVA México 16%)
     */
    setupDefaultImpuesto() {
        this.currentImpuestos = [{
            tipo: 'IVA',
            pais: 'MX',
            tasa: 16.00
        }];
        this.renderImpuestosContainer();
    }

    /**
     * Pobla el modal con datos de cotización existente
     */
    populateModalWithCotizacion(cotizacion) {
        PortalUtils.DOM.setContent('modal-cotizacion-title', 'Ver Cotización');
        
        // Usar folio o id según lo que esté disponible
        const cotizacionId = cotizacion.folio || cotizacion.id || 'N/A';
        PortalUtils.DOM.setContent('modal-cotizacion-id', cotizacionId);
        
        // Usar tipoCotizador o valor por defecto
        const tipoCotizador = cotizacion.tipoCotizador || 'Estándar';
        PortalUtils.DOM.setValue('form-tipo-cotizador', tipoCotizador);
        
        // Fecha en formato correcto - usar 'fecha' en lugar de 'fechaCreacion'
        const fechaCotizacion = cotizacion.fecha || cotizacion.fechaCreacion;
        if (fechaCotizacion) {
            // Manejar formato de fecha
            let fecha = fechaCotizacion;
            if (fechaCotizacion.includes('T')) {
                fecha = fechaCotizacion.split('T')[0];
            }
            PortalUtils.DOM.setValue('form-fecha-cotizacion', fecha);
        }
        
        // Deshabilitar campos en modo vista
        document.getElementById('form-tipo-cotizador').disabled = true;
        document.getElementById('form-fecha-cotizacion').disabled = true;
        
        // Renderizar componentes e impuestos
        this.renderComponentsTable();
        this.renderImpuestosContainer();
        this.updateTotals();
        
        // Cambiar botón
        const submitBtn = document.getElementById('modal-cotizacion-submit');
        submitBtn.style.display = 'none';
    }

    /**
     * Renderiza tabla de componentes en modal
     */
    renderComponentsTable() {
        const tableBody = document.getElementById('componentes-cotizacion-table-body');
        const emptyState = document.getElementById('componentes-cotizacion-empty');
        
        if (!tableBody || !emptyState) return;
        
        tableBody.innerHTML = '';
        
        if (this.currentComponents.length === 0) {
            emptyState.classList.remove('hidden');
            return;
        }
        
        emptyState.classList.add('hidden');
        
        this.currentComponents.forEach((component, index) => {
            const row = this.renderComponentRow(component, index);
            tableBody.appendChild(row);
        });
        
        this.attachComponentRowEventListeners();
    }

    /**
     * Renderiza fila de componente
     */
    renderComponentRow(component, index) {
        const row = document.createElement('tr');
        row.className = 'hover:bg-gray-50';
        
        // Usar estructura correcta del backend: idComponente, precioBase, etc.
        const componenteId = component.idComponente || 'N/A';
        const nombreComponente = component.nombreComponente || component.descripcion || 'Sin nombre';
        const precioUnitario = parseFloat(component.precioBase || 0);
        const cantidad = parseInt(component.cantidad || 1);
        const subtotal = component.importeTotal ? parseFloat(component.importeTotal) : (cantidad * precioUnitario);
        
        row.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">${componenteId}</div>
                <div class="text-sm text-gray-500">${nombreComponente}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                ${cantidad}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                ${PortalUtils.Format.formatCurrency(precioUnitario)}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                ${PortalUtils.Format.formatCurrency(subtotal)}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                ${!this.isEditMode ? `
                    <button class="btn-remove-component text-red-600 hover:text-red-900" data-index="${index}">
                        Quitar
                    </button>
                ` : ''}
            </td>
        `;
        
        return row;
    }

    /**
     * Adjunta event listeners a filas de componentes
     */
    attachComponentRowEventListeners() {
        document.querySelectorAll('.btn-remove-component').forEach(button => {
            button.addEventListener('click', (e) => {
                const index = parseInt(e.target.getAttribute('data-index'));
                this.removeComponent(index);
            });
        });
    }

    /**
     * Renderiza contenedor de impuestos
     */
    renderImpuestosContainer() {
        const container = document.getElementById('impuestos-container');
        if (!container) return;
        
        container.innerHTML = '';
        
        this.currentImpuestos.forEach((impuesto, index) => {
            const item = this.createImpuestoItem(impuesto, index);
            container.appendChild(item);
        });
        
        this.attachImpuestoEventListeners();
    }

    /**
     * Crea elemento de impuesto
     */
    createImpuestoItem(impuesto = {}, index = 0) {
        const div = document.createElement('div');
        div.className = 'impuesto-item bg-white p-4 rounded border mb-3';
        
        div.innerHTML = `
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700">Tipo de Impuesto *</label>
                    <select class="impuesto-tipo mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm" ${this.isEditMode ? 'disabled' : ''} required>
                        <option value="">Seleccionar...</option>
                        <option value="IVA" ${impuesto.tipo === 'IVA' ? 'selected' : ''}>IVA</option>
                        <option value="ISR" ${impuesto.tipo === 'ISR' ? 'selected' : ''}>ISR</option>
                        <option value="IEPS" ${impuesto.tipo === 'IEPS' ? 'selected' : ''}>IEPS</option>
                        <option value="SALES_TAX" ${impuesto.tipo === 'SALES_TAX' ? 'selected' : ''}>Sales Tax</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">País *</label>
                    <select class="impuesto-pais mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm" ${this.isEditMode ? 'disabled' : ''} required>
                        <option value="">Seleccionar...</option>
                        <option value="MX" ${impuesto.pais === 'MX' ? 'selected' : ''}>México</option>
                        <option value="US" ${impuesto.pais === 'US' ? 'selected' : ''}>Estados Unidos</option>
                        <option value="CA" ${impuesto.pais === 'CA' ? 'selected' : ''}>Canadá</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">Tasa (%) *</label>
                    <div class="mt-1 relative rounded-md shadow-sm">
                        <input type="number" class="impuesto-tasa block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm pr-8" 
                               step="0.01" min="0" max="100" value="${impuesto.tasa || ''}" ${this.isEditMode ? 'readonly' : ''} required placeholder="16.00">
                        <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                            <span class="text-gray-500 sm:text-sm">%</span>
                        </div>
                    </div>
                </div>
            </div>
            ${!this.isEditMode ? `
                <div class="mt-3 flex justify-end">
                    <button type="button" class="btn-remove-impuesto text-red-600 hover:text-red-800 text-sm font-medium" data-index="${index}">
                        Quitar impuesto
                    </button>
                </div>
            ` : ''}
        `;
        
        return div;
    }

    /**
     * Adjunta event listeners a impuestos
     */
    attachImpuestoEventListeners() {
        // Cambios en inputs de impuestos
        document.querySelectorAll('.impuesto-item').forEach((item, index) => {
            const inputs = item.querySelectorAll('select, input');
            inputs.forEach(input => {
                input.addEventListener('change', () => {
                    this.updateImpuestoFromDOM(index);
                    this.updateTotals();
                });
            });
        });
        
        // Botones de quitar
        document.querySelectorAll('.btn-remove-impuesto').forEach(button => {
            button.addEventListener('click', (e) => {
                const index = parseInt(e.target.getAttribute('data-index'));
                this.removeImpuesto(index);
            });
        });
    }

    /**
     * Actualiza impuesto desde DOM
     */
    updateImpuestoFromDOM(index) {
        const items = document.querySelectorAll('.impuesto-item');
        if (items[index]) {
            const item = items[index];
            this.currentImpuestos[index] = {
                tipo: item.querySelector('.impuesto-tipo').value,
                pais: item.querySelector('.impuesto-pais').value,
                tasa: parseFloat(item.querySelector('.impuesto-tasa').value) || 0
            };
        }
    }

    /**
     * Agrega nuevo item de impuesto
     */
    addImpuestoItem() {
        this.currentImpuestos.push({
            tipo: '',
            pais: '',
            tasa: 0
        });
        this.renderImpuestosContainer();
    }

    /**
     * Remueve impuesto
     */
    removeImpuesto(index) {
        this.currentImpuestos.splice(index, 1);
        this.renderImpuestosContainer();
        this.updateTotals();
    }

    /**
     * Maneja agregar componente
     */
    handleAddComponent() {
        const componentId = PortalUtils.DOM.getValue('select-componente-cotizacion');
        const cantidad = parseInt(PortalUtils.DOM.getValue('input-cantidad-cotizacion')) || 1;
        
        if (!componentId) {
            PortalUtils.Alert.error('Seleccione un componente para agregar');
            return;
        }
        
        const component = this.availableComponents.find(c => c.id === componentId);
        if (!component) {
            PortalUtils.Alert.error('Componente no encontrado');
            return;
        }
        
        // Verificar si ya existe
        const existingIndex = this.currentComponents.findIndex(c => c.componenteId === componentId);
        if (existingIndex >= 0) {
            // Actualizar cantidad
            this.currentComponents[existingIndex].cantidad += cantidad;
        } else {
            // Agregar nuevo
            this.currentComponents.push({
                componenteId: component.id,
                descripcion: component.descripcion,
                cantidad: cantidad,
                precioUnitario: component.precioBase
            });
        }
        
        // Limpiar form
        PortalUtils.DOM.setValue('select-componente-cotizacion', '');
        PortalUtils.DOM.setValue('input-cantidad-cotizacion', '1');
        
        this.renderComponentsTable();
        this.updateTotals();
    }

    /**
     * Remueve componente
     */
    removeComponent(index) {
        this.currentComponents.splice(index, 1);
        this.renderComponentsTable();
        this.updateTotals();
    }

    /**
     * Actualiza totales en tiempo real
     */
    updateTotals() {
        // Si estamos en modo vista y tenemos una cotización con totales de BD, usarlos
        if (this.isEditMode && this.currentCotizacion) {
            const subtotal = parseFloat(this.currentCotizacion.subtotal || 0);
            const totalImpuestos = parseFloat(this.currentCotizacion.impuestos || this.currentCotizacion.totalImpuestos || 0);
            const total = parseFloat(this.currentCotizacion.total || 0);
            
            PortalUtils.DOM.setContent('cotizacion-subtotal', PortalUtils.Format.formatCurrency(subtotal));
            PortalUtils.DOM.setContent('cotizacion-total-impuestos', PortalUtils.Format.formatCurrency(totalImpuestos));
            PortalUtils.DOM.setContent('cotizacion-total-final', PortalUtils.Format.formatCurrency(total));
            return;
        }
        
        // Cálculo dinámico para modo creación
        const subtotal = this.currentComponents.reduce((sum, comp) => {
            const precio = parseFloat(comp.precioBase || comp.precioUnitario || 0);
            const cantidad = parseInt(comp.cantidad || 1);
            return sum + (cantidad * precio);
        }, 0);
        
        let totalImpuestos = 0;
        this.currentImpuestos.forEach(impuesto => {
            if (impuesto.tasa > 0) {
                totalImpuestos += subtotal * (impuesto.tasa / 100);
            }
        });
        
        const total = subtotal + totalImpuestos;
        
        PortalUtils.DOM.setContent('cotizacion-subtotal', PortalUtils.Format.formatCurrency(subtotal));
        PortalUtils.DOM.setContent('cotizacion-total-impuestos', PortalUtils.Format.formatCurrency(totalImpuestos));
        PortalUtils.DOM.setContent('cotizacion-total-final', PortalUtils.Format.formatCurrency(total));
    }

    /**
     * Maneja envío del formulario
     */
    async handleSubmitCotizacion() {
        if (this.isEditMode) {
            this.closeModal();
            return;
        }
        
        if (!this.validateCotizacion()) {
            return;
        }
        
        try {
            PortalUtils.Loading.show();
            
            // Actualizar impuestos desde DOM
            this.updateAllImpuestosFromDOM();
            
            const cotizacionData = {
                tipoCotizador: PortalUtils.DOM.getValue('form-tipo-cotizador'),
                impuestos: this.currentImpuestos,
                detalles: this.currentComponents
            };
            
            PortalConfig.debugLog('Creando cotización:', cotizacionData);
            
            await PortalApi.cotizaciones.create(cotizacionData);
            
            PortalUtils.Alert.success('Cotización creada exitosamente');
            this.closeModal();
            await this.loadCotizaciones();
            
        } catch (error) {
            PortalConfig.debugError('Error al crear cotización:', error);
            const message = PortalApi.handleError(error);
            PortalUtils.Alert.error(message);
        } finally {
            PortalUtils.Loading.hide();
        }
    }

    /**
     * Actualiza todos los impuestos desde DOM
     */
    updateAllImpuestosFromDOM() {
        const items = document.querySelectorAll('.impuesto-item');
        this.currentImpuestos = [];
        
        items.forEach((item, index) => {
            const tipo = item.querySelector('.impuesto-tipo').value;
            const pais = item.querySelector('.impuesto-pais').value;
            const tasa = parseFloat(item.querySelector('.impuesto-tasa').value) || 0;
            
            if (tipo && pais && tasa > 0) {
                this.currentImpuestos.push({ tipo, pais, tasa });
            }
        });
    }

    /**
     * Valida cotización antes de enviar
     */
    validateCotizacion() {
        const errors = [];
        
        const tipoCotizador = PortalUtils.DOM.getValue('form-tipo-cotizador');
        if (!tipoCotizador) {
            errors.push('Seleccione el tipo de cotizador');
        }
        
        if (this.currentComponents.length === 0) {
            errors.push('Agregue al menos un componente');
        }
        
        if (this.currentImpuestos.length === 0) {
            errors.push('Configure al menos un impuesto');
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
        document.getElementById('modal-cotizacion').classList.remove('hidden');
        document.body.classList.add('overflow-hidden');
    }

    /**
     * Cierra el modal
     */
    closeModal() {
        document.getElementById('modal-cotizacion').classList.add('hidden');
        document.body.classList.remove('overflow-hidden');
        this.currentCotizacion = null;
        this.currentComponents = [];
        this.currentImpuestos = [];
        this.isEditMode = false;
        
        // Habilitar campos que pudieron haber sido deshabilitados
        document.getElementById('form-tipo-cotizador').disabled = false;
        document.getElementById('form-fecha-cotizacion').disabled = false;
        
        // Mostrar botón de submit
        document.getElementById('modal-cotizacion-submit').style.display = '';
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
    applyFilters() {
        const searchTerm = PortalUtils.DOM.getValue('search-cotizaciones').toLowerCase();
        const fechaFilter = PortalUtils.DOM.getValue('filter-fecha-cotizacion');

        this.filteredCotizaciones = this.cotizaciones.filter(cotizacion => {
            // Usar folio en lugar de id y manejar tipoCotizador por defecto
            const cotizacionId = cotizacion.folio || cotizacion.id || '';
            const tipoCotizador = cotizacion.tipoCotizador || 'Estándar';
            
            // Filtro de búsqueda
            const matchesSearch = !searchTerm || 
                cotizacionId.toString().toLowerCase().includes(searchTerm) ||
                tipoCotizador.toLowerCase().includes(searchTerm);

            // Filtro de fecha - usar campo 'fecha' en lugar de 'fechaCreacion'
            let matchesDate = true;
            if (fechaFilter) {
                const fechaCotizacion = cotizacion.fecha || cotizacion.fechaCreacion;
                if (fechaCotizacion) {
                    // Manejar diferentes formatos de fecha
                    const cotizacionDate = fechaCotizacion.includes('T') ? 
                        fechaCotizacion.split('T')[0] : fechaCotizacion;
                    matchesDate = cotizacionDate === fechaFilter;
                }
            }

            return matchesSearch && matchesDate;
        });

        // Reiniciar paginación al aplicar filtros
        this.pagination.currentPage = 1;
        this.renderTable();
    }

    /**
     * Limpia filtros
     */
    clearFilters() {
        PortalUtils.DOM.setValue('search-cotizaciones', '');
        PortalUtils.DOM.setValue('filter-fecha-cotizacion', '');
        this.filteredCotizaciones = [...this.cotizaciones];
        
        // Reiniciar paginación al limpiar filtros
        this.pagination.currentPage = 1;
        this.renderTable();
    }

    /**
     * Muestra estado vacío
     */
    showEmptyState() {
        const tableContainer = document.getElementById('cotizaciones-table-container');
        const emptyState = document.getElementById('cotizaciones-empty-state');
        
        if (tableContainer && emptyState) {
            tableContainer.classList.add('hidden');
            emptyState.classList.remove('hidden');
        }
        
        this.hidePagination();
    }

    /**
     * Muestra loading en tabla
     */
    showTableLoading() {
        const tableLoading = document.getElementById('cotizaciones-table-loading');
        const emptyState = document.getElementById('cotizaciones-empty-state');
        const tableContainer = document.getElementById('cotizaciones-table-container');
        
        if (tableLoading) {
            tableLoading.classList.remove('hidden');
        }
        
        if (emptyState) {
            emptyState.classList.add('hidden');
        }
        
        if (tableContainer) {
            tableContainer.classList.add('hidden');
        }
    }

    /**
     * Oculta loading en tabla
     */
    hideTableLoading() {
        const tableLoading = document.getElementById('cotizaciones-table-loading');
        
        if (tableLoading) {
            tableLoading.classList.add('hidden');
        }
    }

    // Métodos de paginación (similares a components.js y pcs.js)
    updatePaginationInfo() {
        this.pagination.totalItems = this.filteredCotizaciones.length;
        this.pagination.totalPages = Math.ceil(this.pagination.totalItems / this.pagination.pageSize);
        
        if (this.pagination.currentPage > this.pagination.totalPages) {
            this.pagination.currentPage = Math.max(1, this.pagination.totalPages);
        }
    }

    getPaginatedData() {
        const startIndex = (this.pagination.currentPage - 1) * this.pagination.pageSize;
        const endIndex = startIndex + this.pagination.pageSize;
        return this.filteredCotizaciones.slice(startIndex, endIndex);
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
        const paginationControls = document.getElementById('cotizaciones-pagination-controls');
        if (paginationControls && this.pagination.totalItems > 0) {
            paginationControls.classList.remove('hidden');
        }
    }

    hidePagination() {
        const paginationControls = document.getElementById('cotizaciones-pagination-controls');
        if (paginationControls) {
            paginationControls.classList.add('hidden');
        }
    }

    updatePaginationControls() {
        // Actualizar información de paginación
        this.updatePaginationText();
        this.updatePaginationButtons();
        this.updatePageNumbers();
    }

    updatePaginationButtons() {
        const firstBtn = document.getElementById('btn-first-cotizaciones');
        const prevBtn = document.getElementById('btn-previous-cotizaciones');
        const nextBtn = document.getElementById('btn-next-cotizaciones');
        const lastBtn = document.getElementById('btn-last-cotizaciones');
        
        if (firstBtn && prevBtn && nextBtn && lastBtn) {
            const isFirstPage = this.pagination.currentPage === 1;
            const isLastPage = this.pagination.currentPage === this.pagination.totalPages;
            
            firstBtn.disabled = isFirstPage;
            prevBtn.disabled = isFirstPage;
            nextBtn.disabled = isLastPage;
            lastBtn.disabled = isLastPage;
        }
    }

    updatePageNumbers() {
        const pageNumbersContainer = document.getElementById('cotizaciones-page-numbers');
        if (!pageNumbersContainer) return;
        
        pageNumbersContainer.innerHTML = '';
        
        const { currentPage, totalPages } = this.pagination;
        const maxVisiblePages = PortalConfig.UI_CONFIG.MAX_VISIBLE_PAGES;
        
        let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);
        
        if (endPage - startPage + 1 < maxVisiblePages) {
            startPage = Math.max(1, endPage - maxVisiblePages + 1);
        }
        
        for (let i = startPage; i <= endPage; i++) {
            const button = document.createElement('button');
            button.textContent = i;
            button.className = `relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                i === currentPage 
                    ? 'z-10 bg-primary-50 border-primary-500 text-primary-600' 
                    : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
            }`;
            
            button.addEventListener('click', () => this.goToPage(i));
            pageNumbersContainer.appendChild(button);
        }
    }

    updatePaginationText() {
        const startIndex = (this.pagination.currentPage - 1) * this.pagination.pageSize + 1;
        const endIndex = Math.min(startIndex + this.pagination.pageSize - 1, this.pagination.totalItems);
        
        PortalUtils.DOM.setContent('page-start-cotizaciones', startIndex.toString());
        PortalUtils.DOM.setContent('page-end-cotizaciones', endIndex.toString());
        PortalUtils.DOM.setContent('total-items-cotizaciones', this.pagination.totalItems.toString());
    }

    /**
     * Inicializa el manager
     */
    async initialize() {
        console.log('Inicializando CotizacionesManager...');
        await this.loadCotizaciones();
    }
}

// Crear instancia global
window.CotizacionesManager = new CotizacionesManager(); 