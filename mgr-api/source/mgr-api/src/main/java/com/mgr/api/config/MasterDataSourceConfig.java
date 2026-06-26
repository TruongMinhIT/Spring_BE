package com.mgr.api.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
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
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.mgr.api.repository.master", // chỉ map với package master -> sử dụng EntityManagerFactory này
        entityManagerFactoryRef = "masterEntityManagerFactory",
        transactionManagerRef = "masterTransactionManager"
)
// Không phụ thuộc RoutingDataSource -> Query dùng mặc định Master
public class MasterDataSourceConfig {
    @Bean(name = "masterDataSource")
    @Primary // Spring sẽ nhận dện đây là DB chính
    @ConfigurationProperties(prefix = "spring.datasource") // Đọc application.properties qua tiền tố spring.datasource
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build(); // Khởi tạo kết nối tới db master
    }

    @Bean(name = "masterEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean masterEntityManagerFactory(
            @Qualifier("masterDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em =
                new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.mgr.api.model");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setPersistenceUnitName("master");
        Properties jpaProperties = new Properties();
        // Giúp convert field camelCase (dateOfBirth) thành snake_case (date_of_birth)
        jpaProperties.setProperty(
                "hibernate.physical_naming_strategy",
                "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy"
        );
        em.setJpaProperties(jpaProperties);
        return em;
    }

    @Bean(name = "masterTransactionManager")
    @Primary
    // Khi @transaction dùng entityManagerFactory
    public PlatformTransactionManager masterTransactionManager(
            @Qualifier("masterEntityManagerFactory")
            EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
