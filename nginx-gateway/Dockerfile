# Nginx Gateway - Solo proxy
FROM nginx:alpine

# Eliminar configuración por defecto
RUN rm -rf /etc/nginx/conf.d/*

# Crear directorio para logs
RUN mkdir -p /var/log/nginx/gateway

# Copiar configuración personalizada
COPY nginx.conf /etc/nginx/nginx.conf
COPY conf/gateway.conf /etc/nginx/conf.d/gateway.conf

# Exponer puerto 80
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://127.0.0.1/health || exit 1

# Comando por defecto
CMD ["nginx", "-g", "daemon off;"]