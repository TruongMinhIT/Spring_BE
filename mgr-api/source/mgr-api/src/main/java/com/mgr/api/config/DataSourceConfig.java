package com.mgr.api.config;

import com.mgr.api.ternant.TenantRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class DataSourceConfig {
    private final String defaultTenant = "tenant_1";

    @Bean
    @Primary
    public DataSource dataSource() {
        // Read the allTenant/ directory , retrieve all file .properties
        File[] files = Paths.get("allTenants").toFile().listFiles();
        Map<Object, Object> resolvedDataSource = new HashMap<>();

        if (files != null) {
            for (File propertyFile : files) {
                if (propertyFile.isFile() && propertyFile.getName().endsWith(".properties")) {
                    Properties tenantProperties = new Properties();
                    try (FileInputStream fis = new FileInputStream(propertyFile)) {
                        tenantProperties.load(fis);
                        // per file → create 1 HikariDataSource
                        String tenantId = tenantProperties.getProperty("name");
                        HikariDataSource dataSource = new HikariDataSource();
                        dataSource.setDriverClassName(tenantProperties.getProperty("datasource.driver-class-name"));
                        dataSource.setJdbcUrl(tenantProperties.getProperty("datasource.url"));
                        dataSource.setUsername(tenantProperties.getProperty("datasource.username"));
                        dataSource.setPassword(tenantProperties.getProperty("datasource.password"));
                        dataSource.setMaximumPoolSize(5);
                        // Save to Map: key = "tenant_1", value = DataSource of tenant_1
                        resolvedDataSource.put(tenantId, dataSource);
                    } catch (IOException exp) {
                        throw new RuntimeException("Error when read tenant config: " + exp.getMessage());
                    }
                }
            }
        }
        TenantRoutingDataSource dataSource = new TenantRoutingDataSource();
        // Set default for each request without tenant
        dataSource.setDefaultTargetDataSource(resolvedDataSource.get(defaultTenant));
        dataSource.setTargetDataSources(resolvedDataSource);

        dataSource.afterPropertiesSet();
        return dataSource;
    }
}
