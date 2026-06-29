package com.mgr.api.config;

import com.mgr.api.ternant.CustomMultiTenantConnectionProvider;
import com.mgr.api.ternant.TenantIdentifierResolver;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.mgr.api.repository.tenant",
        entityManagerFactoryRef = "tenantEntityManagerFactory",
        transactionManagerRef = "tenantTransactionManager"
)
public class MultiTenantJpaConfig {
    @Autowired
    private CustomMultiTenantConnectionProvider connectionProvider;

    @Autowired
    private TenantIdentifierResolver tenantResolver;

    @Bean(name = "tenantEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
            @Qualifier("masterDataSource")DataSource masterDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        // Set DataSource mặc định cho Spring
        em.setDataSource(masterDataSource);
        // Truyền master DataSource vào provider
        connectionProvider.setMasterDataSource(masterDataSource);
        // Trỏ đến thư mục chứa Entity của dự án
        em.setPackagesToScan("com.mgr.api.model");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        // Thiết lập multiTenant cho Hibernate
        Map<String, Object> properties = new HashMap<>();
        properties.put(Environment.MULTI_TENANT, "DATABASE");
        properties.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
        properties.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);

        // Các cấu hình Hibernate tiêu chuẩn
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.SHOW_SQL, true);
        properties.put(Environment.FORMAT_SQL, true);

        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(name = "tenantTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("tenantEntityManagerFactory")EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}
