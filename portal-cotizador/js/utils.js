/**
 * Utilidades generales para el Portal Cotizador
 */

/**
 * Utilidades para manejo del DOM
 */
class DOMUtils {
    /**
     * Encuentra elemento por ID con validación
     */
    static getElementById(id) {
        const element = document.getElementById(id);
        if (!element) {
            PortalConfig.debugError(`Element with ID '${id}' not found`);
        }
        return element;
    }

    /**
     * Encuentra elementos por selector
     */
    static querySelectorAll(selector) {
        return document.querySelectorAll(selector);
    }

    /**
     * Encuentra primer elemento por selector
     */
    static querySelector(selector) {
        return document.querySelector(selector);
    }

    /**
     * Agrega event listener con validación
     */
    static addEventListener(elementId, event, handler) {
        const element = this.getElementById(elementId);
        if (element) {
            element.addEventListener(event, handler);
        }
    }

    /**
     * Muestra elemento
     */
    static show(elementId) {
        const element = this.getElementById(elementId);
        if (element) {
            element.classList.remove('hidden');
        }
    }

    /**
     * Oculta elemento
     */
    static hide(elementId) {
        const element = this.getElementById(elementId);
        if (element) {
            element.classList.add('hidden');
        }
    }

    /**
     * Toggle visibility
     */
    static toggle(elementId) {
        const element = this.getElementById(elementId);
        if (element) {
            element.classList.toggle('hidden');
        }
    }

    /**
     * Limpia contenido de elemento
     */
    static clearContent(elementId) {
        const element = this.getElementById(elementId);
        if (element) {
            element.innerHTML = '';
        }
    }

    /**
     * Establece contenido HTML
     */
    static setContent(elementId, content) {
        const element = this.getElementById(elementId);
        if (element) {
            element.innerHTML = content;
        }
    }

    /**
     * Establece valor de input
     */
    static setValue(elementId, value) {
        const element = this.getElementById(elementId);
        if (element) {
            element.value = value || '';
        }
    }

    /**
     * Obtiene valor de input
     */
    static getValue(elementId) {
        const element = this.getElementById(elementId);
        return element ? element.value : '';
    }

    /**
     * Agrega clase CSS
     */
    static addClass(elementId, className) {
        const element = this.getElementById(elementId);
        if (element) {
            element.classList.add(className);
        }
    }

    /**
     * Remueve clase CSS
     */
    static removeClass(elementId, className) {
        const element = this.getElementById(elementId);
        if (element) {
            element.classList.remove(className);
        }
    }

    /**
     * Scroll to element
     */
    static scrollToElement(elementId) {
        const element = this.getElementById(elementId);
        if (element) {
            element.scrollIntoView({ behavior: 'smooth' });
        }
    }
}

/**
 * Utilidades para formateo de datos
 */
class FormatUtils {
    /**
     * Formatea moneda
     */
    static formatCurrency(amount) {
        if (amount === null || amount === undefined || isNaN(amount)) {
            return '$0.00';
        }
        return new Intl.NumberFormat('es-MX', PortalConfig.UI_CONFIG.CURRENCY_FORMAT).format(amount);
    }

    /**
     * Formatea fecha
     */
    static formatDate(date) {
        if (!date) return '';
        
        const dateObj = typeof date === 'string' ? new Date(date) : date;
        if (isNaN(dateObj.getTime())) return '';
        
        return new Intl.DateTimeFormat('es-MX', PortalConfig.UI_CONFIG.DATE_FORMAT).format(dateObj);
    }

    /**
     * Formatea fecha y hora
     */
    static formatDateTime(datetime) {
        if (!datetime) return '';
        
        const dateObj = typeof datetime === 'string' ? new Date(datetime) : datetime;
        if (isNaN(dateObj.getTime())) return '';
        
        return new Intl.DateTimeFormat('es-MX', PortalConfig.UI_CONFIG.DATETIME_FORMAT).format(dateObj);
    }

    /**
     * Formatea texto para mostrar (capitaliza primera letra)
     */
    static formatText(text) {
        if (!text) return '';
        return text.charAt(0).toUpperCase() + text.slice(1).toLowerCase();
    }

    /**
     * Trunca texto
     */
    static truncateText(text, maxLength = 50) {
        if (!text) return '';
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
    }

    /**
     * Formatea tipo de componente
     */
    static formatComponentType(type) {
        return PortalConfig.COMPONENT_TYPE_LABELS[type] || type;
    }

    /**
     * Convierte string a formato de fecha para input
     */
    static toInputDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return '';
        return date.toISOString().split('T')[0];
    }
}

/**
 * Utilidades para validación
 */
class ValidationUtils {
    /**
     * Valida si el campo está vacío
     */
    static isEmpty(value) {
        return !value || value.toString().trim() === '';
    }

    /**
     * Valida email
     */
    static isValidEmail(email) {
        return PortalConfig.REGEX_PATTERNS.EMAIL.test(email);
    }

    /**
     * Valida ID de componente
     */
    static isValidComponentId(id) {
        return PortalConfig.REGEX_PATTERNS.COMPONENT_ID.test(id);
    }

    /**
     * Valida número
     */
    static isValidNumber(value) {
        return !isNaN(value) && isFinite(value);
    }

    /**
     * Valida precio (debe ser mayor a 0)
     */
    static isValidPrice(price) {
        const num = parseFloat(price);
        return !isNaN(num) && isFinite(num) && num > 0;
    }

    /**
     * Valida teléfono
     */
    static isValidPhone(phone) {
        return PortalConfig.REGEX_PATTERNS.PHONE.test(phone);
    }

    /**
     * Valida formato de moneda
     */
    static isValidCurrency(currency) {
        return PortalConfig.REGEX_PATTERNS.CURRENCY.test(currency);
    }

    /**
     * Valida formulario completo
     */
    static validateForm(formId, validationRules) {
        const errors = [];
        
        for (const [fieldId, rules] of Object.entries(validationRules)) {
            const value = DOMUtils.getValue(fieldId);
            const fieldLabel = rules.label || fieldId;
            
            // Validación requerido
            if (rules.required && this.isEmpty(value)) {
                errors.push(`${fieldLabel}: ${PortalConfig.MESSAGES.REQUIRED_FIELD}`);
                this.setFieldError(fieldId, PortalConfig.MESSAGES.REQUIRED_FIELD);
                continue;
            }
            
            // Validaciones específicas
            if (value && rules.type) {
                switch (rules.type) {
                    case 'email':
                        if (!this.isValidEmail(value)) {
                            errors.push(`${fieldLabel}: ${PortalConfig.MESSAGES.INVALID_EMAIL}`);
                            this.setFieldError(fieldId, PortalConfig.MESSAGES.INVALID_EMAIL);
                        }
                        break;
                    case 'number':
                        if (!this.isValidNumber(value)) {
                            errors.push(`${fieldLabel}: ${PortalConfig.MESSAGES.INVALID_NUMBER}`);
                            this.setFieldError(fieldId, PortalConfig.MESSAGES.INVALID_NUMBER);
                        }
                        break;
                    case 'price':
                        if (!this.isValidPrice(value)) {
                            errors.push(`${fieldLabel}: ${PortalConfig.MESSAGES.INVALID_PRICE}`);
                            this.setFieldError(fieldId, PortalConfig.MESSAGES.INVALID_PRICE);
                        }
                        break;
                    case 'componentId':
                        if (!this.isValidComponentId(value)) {
                            errors.push(`${fieldLabel}: ${PortalConfig.MESSAGES.INVALID_ID_FORMAT}`);
                            this.setFieldError(fieldId, PortalConfig.MESSAGES.INVALID_ID_FORMAT);
                        }
                        break;
                }
            }
            
            // Limpiar error si no hay errores
            if (!errors.some(error => error.startsWith(fieldLabel))) {
                this.clearFieldError(fieldId);
            }
        }
        
        return errors;
    }

    /**
     * Marca campo con error
     */
    static setFieldError(fieldId, message) {
        const field = DOMUtils.getElementById(fieldId);
        if (field) {
            field.classList.add('border-red-500');
            field.classList.remove('border-gray-300');
            
            // Mostrar mensaje de error
            let errorElement = document.getElementById(`${fieldId}-error`);
            if (!errorElement) {
                errorElement = document.createElement('p');
                errorElement.id = `${fieldId}-error`;
                errorElement.className = 'mt-1 text-sm text-red-600';
                field.parentNode.appendChild(errorElement);
            }
            errorElement.textContent = message;
        }
    }

    /**
     * Limpia error de campo
     */
    static clearFieldError(fieldId) {
        const field = DOMUtils.getElementById(fieldId);
        if (field) {
            field.classList.remove('border-red-500');
            field.classList.add('border-gray-300');
            
            const errorElement = document.getElementById(`${fieldId}-error`);
            if (errorElement) {
                errorElement.remove();
            }
        }
    }
}

/**
 * Utilidades para manejo de alertas y notificaciones
 */
class AlertUtils {
    /**
     * Muestra alerta
     */
    static show(message, type = PortalConfig.ALERT_TYPES.INFO, autoClose = true) {
        const container = DOMUtils.getElementById('alert-container');
        const alertMessage = DOMUtils.getElementById('alert-message');
        const alertText = DOMUtils.getElementById('alert-text');
        const alertIcon = DOMUtils.getElementById('alert-icon');
        
        if (!container || !alertMessage || !alertText || !alertIcon) {
            console.error('Alert elements not found');
            return;
        }

        // Configurar contenido
        alertText.textContent = message;
        
        // Configurar estilos según tipo
        this.setAlertStyle(alertMessage, alertIcon, type);
        
        // Mostrar alerta
        container.classList.remove('hidden');
        container.classList.add('alert-slide-in');
        
        // Auto-cierre
        if (autoClose) {
            setTimeout(() => {
                this.hide();
            }, PortalConfig.UI_CONFIG.ALERT_AUTO_CLOSE_TIME);
        }
    }

    /**
     * Oculta alerta
     */
    static hide() {
        DOMUtils.hide('alert-container');
    }

    /**
     * Configura estilos de alerta según tipo
     */
    static setAlertStyle(alertMessage, alertIcon, type) {
        // Limpiar clases existentes
        alertMessage.className = 'rounded-md p-4';
        
        switch (type) {
            case PortalConfig.ALERT_TYPES.SUCCESS:
                alertMessage.classList.add('bg-green-50', 'border', 'border-green-200');
                alertIcon.classList.add('text-green-400');
                alertIcon.innerHTML = `
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                `;
                break;
            case PortalConfig.ALERT_TYPES.ERROR:
                alertMessage.classList.add('bg-red-50', 'border', 'border-red-200');
                alertIcon.classList.add('text-red-400');
                alertIcon.innerHTML = `
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"></path>
                `;
                break;
            case PortalConfig.ALERT_TYPES.WARNING:
                alertMessage.classList.add('bg-yellow-50', 'border', 'border-yellow-200');
                alertIcon.classList.add('text-yellow-400');
                alertIcon.innerHTML = `
                    <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"></path>
                `;
                break;
            default:
                alertMessage.classList.add('bg-blue-50', 'border', 'border-blue-200');
                alertIcon.classList.add('text-blue-400');
                alertIcon.innerHTML = `
                    <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path>
                `;
                break;
        }
    }

    /**
     * Muestra alerta de éxito
     */
    static success(message) {
        this.show(message, PortalConfig.ALERT_TYPES.SUCCESS);
    }

    /**
     * Muestra alerta de error
     */
    static error(message) {
        this.show(message, PortalConfig.ALERT_TYPES.ERROR);
    }

    /**
     * Muestra alerta de advertencia
     */
    static warning(message) {
        this.show(message, PortalConfig.ALERT_TYPES.WARNING);
    }

    /**
     * Muestra alerta de información
     */
    static info(message) {
        this.show(message, PortalConfig.ALERT_TYPES.INFO);
    }
}

/**
 * Utilidades para manejo de loading
 */
class LoadingUtils {
    /**
     * Muestra spinner global
     */
    static show() {
        DOMUtils.show('loading-spinner');
    }

    /**
     * Oculta spinner global
     */
    static hide() {
        DOMUtils.hide('loading-spinner');
    }

    /**
     * Ejecuta función con loading
     */
    static async withLoading(asyncFunction) {
        try {
            this.show();
            const result = await asyncFunction();
            return result;
        } finally {
            this.hide();
        }
    }

    /**
     * Agrega loading a botón
     */
    static setButtonLoading(buttonId, isLoading) {
        const button = DOMUtils.getElementById(buttonId);
        if (button) {
            if (isLoading) {
                button.disabled = true;
                button.classList.add('btn-loading');
            } else {
                button.disabled = false;
                button.classList.remove('btn-loading');
            }
        }
    }
}

/**
 * Utilidades para debouncing
 */
class DebounceUtils {
    static timers = {};

    /**
     * Debounce function
     */
    static debounce(key, func, delay = PortalConfig.UI_CONFIG.SEARCH_DEBOUNCE_TIME) {
        clearTimeout(this.timers[key]);
        this.timers[key] = setTimeout(func, delay);
    }
}

/**
 * Utilidades para manejo de modales
 */
class ModalUtils {
    /**
     * Muestra modal
     */
    static show(modalId) {
        DOMUtils.show(modalId);
        document.body.classList.add('overflow-hidden');
    }

    /**
     * Oculta modal
     */
    static hide(modalId) {
        DOMUtils.hide(modalId);
        document.body.classList.remove('overflow-hidden');
    }

    /**
     * Configura eventos estándar de modal
     */
    static setupModal(modalId, closeButtonIds = []) {
        // Click fuera del modal para cerrar
        const modal = DOMUtils.getElementById(modalId);
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    this.hide(modalId);
                }
            });
        }

        // Botones de cerrar
        closeButtonIds.forEach(buttonId => {
            DOMUtils.addEventListener(buttonId, 'click', () => {
                this.hide(modalId);
            });
        });

        // Tecla ESC para cerrar
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && !modal.classList.contains('hidden')) {
                this.hide(modalId);
            }
        });
    }
}

// Exportar utilidades globalmente
window.PortalUtils = {
    DOM: DOMUtils,
    Format: FormatUtils,
    Validation: ValidationUtils,
    Alert: AlertUtils,
    Loading: LoadingUtils,
    Debounce: DebounceUtils,
    Modal: ModalUtils
};