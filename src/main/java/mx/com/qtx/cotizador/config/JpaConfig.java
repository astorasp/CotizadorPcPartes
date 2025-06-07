package mx.com.qtx.cotizador.config;

/**
 * Configuración de JPA para la aplicación.
 * <p>
 * Esta clase define los beans necesarios para la configuración de JPA,
 * incluyendo DataSource, EntityManagerFactory y TransactionManager.
 * Lee la configuración desde el archivo application.properties.
 * </p>
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "mx.com.qtx.cotizadorv1ds.persistencia.repositorios")
@EnableTransactionManagement
public class JpaConfig {

    /**
     * Crea y configura el DataSource utilizando propiedades desde application.properties.
     * <p>
     * Lee las propiedades de conexión a la base de datos y propiedades adicionales
     * del pool de conexiones si están definidas.
     * </p>
     * 
     * @return DataSource configurado para la aplicación
     */
    @Bean
    public DataSource dataSource() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                throw new IllegalStateException("No se pudo encontrar el archivo application.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar el archivo de propiedades", e);
        }

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(props.getProperty("db.driver"));
        dataSource.setUrl(props.getProperty("db.url"));
        dataSource.setUsername(props.getProperty("db.username"));
        dataSource.setPassword(props.getProperty("db.password"));
        
        return dataSource;
    }

    /**
     * Crea y configura el EntityManagerFactory utilizando propiedades desde application.properties.
     * <p>
     * Configura el vendorAdapter de Hibernate y establece propiedades JPA adicionales
     * desde el archivo de propiedades.
     * </p>
     * 
     * @return EntityManagerFactory configurado para la aplicación
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        // Cargar propiedades desde el archivo de configuración
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar propiedades para entityManagerFactory", e);
        }
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        // Utilizar la propiedad hibernate.hbm2ddl.auto si está definida para determinar generateDdl
        String hbm2ddl = props.getProperty("hibernate.hbm2ddl.auto");
        boolean generateDdl = hbm2ddl != null && (hbm2ddl.equals("create") || hbm2ddl.equals("create-drop") || hbm2ddl.equals("update"));
        vendorAdapter.setGenerateDdl(generateDdl);
        
        // Configurar showSql desde las propiedades
        boolean showSql = Boolean.parseBoolean(props.getProperty("hibernate.show_sql", "false"));
        vendorAdapter.setShowSql(showSql);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("mx.com.qtx.cotizadorv1ds.persistencia.entidades");
        factory.setDataSource(dataSource());
        
        Properties jpaProperties = new Properties();
        
        // Usar el dialecto de Hibernate desde las propiedades
        String dialect = props.getProperty("hibernate.dialect");
        if (dialect != null && !dialect.isEmpty()) {
            jpaProperties.put("hibernate.dialect", dialect);
        } else {
            jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        }
        
        // Configurar otras propiedades de Hibernate desde el archivo de propiedades
        jpaProperties.put("hibernate.format_sql", 
                Boolean.parseBoolean(props.getProperty("hibernate.format_sql", "true")));
        jpaProperties.put("hibernate.show_sql", showSql);
        jpaProperties.put("hibernate.id.new_generator_mappings", false);
        
        // Configurar hibernte.hbm2ddl.auto si está definido
        if (hbm2ddl != null && !hbm2ddl.isEmpty()) {
            jpaProperties.put("hibernate.hbm2ddl.auto", hbm2ddl);
        }
        
        // Configurar caché de segundo nivel si está definido
        String secondLevelCache = props.getProperty("hibernate.cache.use_second_level_cache");
        if (secondLevelCache != null && !secondLevelCache.isEmpty()) {
            jpaProperties.put("hibernate.cache.use_second_level_cache", 
                    Boolean.parseBoolean(secondLevelCache));
        }
        
        factory.setJpaProperties(jpaProperties);
        
        return factory;
    }

    /**
     * Crea y configura el TransactionManager para la aplicación.
     * <p>
     * Utiliza el EntityManagerFactory configurado para gestionar las transacciones.
     * </p>
     * 
     * @return TransactionManager configurado para la aplicación
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return txManager;
    }
}
