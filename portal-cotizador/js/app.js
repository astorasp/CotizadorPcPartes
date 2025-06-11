/**
 * Aplicación principal del Portal Cotizador
 */

class PortalApp {
    constructor() {
        this.currentSection = 'componentes';
        this.managers = {
            componentes: window.ComponentesManager,
            pcs: window.PcsManager,
            cotizaciones: window.CotizacionesManager,
            proveedores: window.ProveedoresManager,
            pedidos: window.PedidosManager,
            promociones: window.PromocionesManager
        };
        
        this.initializeApp();
    }

    /**
     * Inicializa la aplicación
     */
    async initializeApp() {
        PortalConfig.debugLog('Initializing Portal Cotizador App');
        
        try {
            this.setupNavigation();
            this.setupMobileMenu();
            await this.loadSection(this.currentSection);
            
            PortalConfig.debugLog('App initialized successfully');
            
        } catch (error) {
            PortalConfig.debugError('Error initializing app', error);
            PortalUtils.Alert.error('Error al inicializar la aplicación');
        }
    }

    /**
     * Configura la navegación del SPA
     */
    setupNavigation() {
        // Navegación desktop
        const navLinks = document.querySelectorAll('.nav-link');
        navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                
                // Obtener sección del ID del botón
                const sectionName = link.id.replace('nav-', '');
                this.navigateToSection(sectionName);
            });
        });

        // Navegación mobile
        const mobileNavLinks = document.querySelectorAll('.nav-link-mobile');
        mobileNavLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                
                // Obtener sección del data attribute
                const sectionName = link.getAttribute('data-section');
                this.navigateToSection(sectionName);
                
                // Cerrar menú mobile
                this.closeMobileMenu();
            });
        });
    }

    /**
     * Configura el menú mobile
     */
    setupMobileMenu() {
        const mobileMenuButton = document.getElementById('mobile-menu-button');
        const mobileMenu = document.getElementById('mobile-menu');

        if (mobileMenuButton && mobileMenu) {
            mobileMenuButton.addEventListener('click', () => {
                this.toggleMobileMenu();
            });

            // Cerrar menú al hacer click fuera
            document.addEventListener('click', (e) => {
                if (!mobileMenuButton.contains(e.target) && !mobileMenu.contains(e.target)) {
                    this.closeMobileMenu();
                }
            });
        }
    }

    /**
     * Configura listeners globales
     */
    setupGlobalEventListeners() {
        // Manejo de errores no capturados
        window.addEventListener('unhandledrejection', (event) => {
            PortalConfig.debugError('Unhandled promise rejection', event.reason);
            PortalUtils.Alert.error('Ha ocurrido un error inesperado');
            event.preventDefault();
        });

        // Manejo de errores JavaScript
        window.addEventListener('error', (event) => {
            PortalConfig.debugError('JavaScript error', event.error);
        });

        // Manejo de cambios de conectividad
        window.addEventListener('online', () => {
            PortalUtils.Alert.success('Conexión restaurada');
        });

        window.addEventListener('offline', () => {
            PortalUtils.Alert.warning('Sin conexión a internet');
        });
    }

    /**
     * Navega a una sección específica
     */
    async navigateToSection(sectionName) {
        try {
            // Actualizar navegación visual
            this.updateNavigationState(sectionName);
            
            // Ocultar sección actual
            this.hideCurrentSection();
            
            // Cargar nueva sección
            await this.loadSection(sectionName);
            
            // Mostrar nueva sección
            this.showSection(sectionName);
            
            this.currentSection = sectionName;
            
            PortalConfig.debugLog(`Navigated to section: ${sectionName}`);
            
        } catch (error) {
            PortalConfig.debugError(`Error navigating to section: ${sectionName}`, error);
            PortalUtils.Alert.error(`Error al cargar la sección ${sectionName}`);
        }
    }

    /**
     * Actualiza el estado visual de la navegación
     */
    updateNavigationState(activeSection) {
        // Desktop navigation
        document.querySelectorAll('.nav-link').forEach(link => {
            const sectionName = link.id.replace('nav-', '');
            
            if (sectionName === activeSection) {
                link.classList.remove('text-gray-500', 'hover:text-gray-700');
                link.classList.add('text-primary-600', 'border-b-2', 'border-primary-600');
            } else {
                link.classList.remove('text-primary-600', 'border-b-2', 'border-primary-600');
                link.classList.add('text-gray-500', 'hover:text-gray-700');
            }
        });

        // Mobile navigation
        document.querySelectorAll('.nav-link-mobile').forEach(link => {
            const sectionName = link.getAttribute('data-section');
            
            if (sectionName === activeSection) {
                link.classList.remove('text-gray-700', 'hover:text-gray-900', 'hover:bg-gray-100');
                link.classList.add('text-primary-600', 'bg-primary-50');
            } else {
                link.classList.remove('text-primary-600', 'bg-primary-50');
                link.classList.add('text-gray-700', 'hover:text-gray-900', 'hover:bg-gray-100');
            }
        });
    }

    /**
     * Oculta la sección actual
     */
    hideCurrentSection() {
        const currentSectionElement = document.getElementById(`section-${this.currentSection}`);
        if (currentSectionElement) {
            currentSectionElement.classList.remove('active');
            currentSectionElement.classList.add('hidden');
        }
    }

    /**
     * Muestra una sección específica
     */
    showSection(sectionName) {
        const sectionElement = document.getElementById(`section-${sectionName}`);
        if (sectionElement) {
            sectionElement.classList.remove('hidden');
            sectionElement.classList.add('active');
        }
    }

    /**
     * Carga una sección específica
     */
    async loadSection(sectionName) {
        PortalConfig.debugLog(`Loading section: ${sectionName}`);

        switch (sectionName) {
            case 'componentes':
                if (this.managers.componentes) {
                    await this.managers.componentes.initialize();
                }
                break;
            case 'pcs':
                if (this.managers.pcs) {
                    await this.managers.pcs.initialize();
                }
                break;
            case 'cotizaciones':
                if (this.managers.cotizaciones) {
                    await this.managers.cotizaciones.initialize();
                }
                break;
            case 'proveedores':
                if (this.managers.proveedores) {
                    await this.managers.proveedores.initialize();
                }
                break;
            case 'pedidos':
                if (this.managers.pedidos) {
                    await this.managers.pedidos.initialize();
                }
                break;
            case 'promociones':
                await this.loadPromocionesSection();
                break;
            default:
                PortalConfig.debugLog(`Section ${sectionName} - under development`);
                break;
        }
    }

    /**
     * Carga la sección de PCs
     */
    async loadPcsSection() {
        PortalConfig.debugLog('Loading PCs section');
        if (this.managers.pcs) {
            await this.managers.pcs.initialize();
        }
    }

    /**
     * Carga la sección de cotizaciones
     */
    async loadCotizacionesSection() {
        PortalConfig.debugLog('Loading Cotizaciones section');
        if (this.managers.cotizaciones) {
            await this.managers.cotizaciones.initialize();
        }
    }

    /**
     * Carga la sección de pedidos
     */
    async loadPedidosSection() {
        PortalConfig.debugLog('Loading Pedidos section');
        if (this.managers.pedidos) {
            await this.managers.pedidos.initialize();
        }
    }

    /**
     * Carga la sección de promociones
     */
    async loadPromocionesSection() {
        PortalConfig.debugLog('Loading Promociones section');
        if (this.managers.promociones) {
            // El PromocionesManager se inicializa automáticamente en su constructor
            // Solo verificamos que esté disponible
            console.log('[App] Sección de promociones cargada correctamente');
        } else {
            console.error('[App] PromocionesManager no está disponible');
        }
    }

    /**
     * Toggle del menú mobile
     */
    toggleMobileMenu() {
        const mobileMenu = document.getElementById('mobile-menu');
        if (mobileMenu) {
            mobileMenu.classList.toggle('hidden');
        }
    }

    /**
     * Cierra el menú mobile
     */
    closeMobileMenu() {
        const mobileMenu = document.getElementById('mobile-menu');
        if (mobileMenu) {
            mobileMenu.classList.add('hidden');
        }
    }

    /**
     * Método para registrar nuevos managers de sección
     */
    registerSectionManager(sectionName, manager) {
        this.managers[sectionName] = manager;
        PortalConfig.debugLog(`Registered manager for section: ${sectionName}`);
    }

    /**
     * Obtiene el manager de una sección
     */
    getSectionManager(sectionName) {
        return this.managers[sectionName];
    }

    /**
     * Método para debugging - obtener estado actual
     */
    getAppState() {
        return {
            currentSection: this.currentSection,
            registeredManagers: Object.keys(this.managers),
            apiBaseUrl: PortalConfig.API_CONFIG.BASE_URL
        };
    }
}

/**
 * Inicialización de la aplicación cuando el DOM esté listo
 */
document.addEventListener('DOMContentLoaded', () => {
    PortalConfig.debugLog('DOM Content Loaded - Starting Portal App');
    
    try {
        // Crear instancia global de la aplicación
        window.PortalApp = new PortalApp();
        
        // Hacer disponibles utilidades globalmente para debugging
        if (PortalConfig.DEBUG_CONFIG.ENABLED) {
            window.PortalDebug = {
                getAppState: () => window.PortalApp.getAppState(),
                getConfig: () => PortalConfig,
                getUtils: () => PortalUtils,
                getApi: () => PortalApi,
                testApiConnection: async () => {
                    try {
                        const componentes = await PortalApi.componentes.getAll();
                        console.log('API Connection Test - Success:', componentes);
                        return { success: true, data: componentes };
                    } catch (error) {
                        console.error('API Connection Test - Failed:', error);
                        return { success: false, error: error.message };
                    }
                },
                testAuthentication: async () => {
                    const result = await PortalApi.testConnection();
                    console.log('Authentication Test:', result);
                    return result;
                },
                getAuthHeaders: () => {
                    return PortalApi.client.defaultHeaders;
                }
            };
            
            console.log('Portal Cotizador Debug Mode Enabled');
            console.log('Available debug methods:', Object.keys(window.PortalDebug));
        }
        
    } catch (error) {
        console.error('Failed to initialize Portal App:', error);
        
        // Mostrar error al usuario
        const errorMessage = 'Error al inicializar la aplicación. Por favor, recargue la página.';
        
        // Intentar mostrar alerta, si no funciona usar alert nativo
        try {
            PortalUtils.Alert.error(errorMessage);
        } catch (alertError) {
            alert(errorMessage);
        }
    }
});

/**
 * Manejo de errores globales para mejorar UX
 */
window.addEventListener('error', (event) => {
    PortalConfig.debugError('Global error caught:', event.error);
    
    // No mostrar alertas para errores menores o de recursos
    if (event.error && !event.error.message.includes('Script error')) {
        PortalUtils.Alert.error('Ha ocurrido un error. Si el problema persiste, recargue la página.');
    }
});

window.addEventListener('unhandledrejection', (event) => {
    PortalConfig.debugError('Unhandled Promise rejection:', event.reason);
    
    // Prevenir que el error se muestre en consola por defecto
    event.preventDefault();
    
    // Mostrar mensaje amigable al usuario
    PortalUtils.Alert.error('Error de conectividad. Verifique su conexión a internet.');
});