package com.mgr.api.config;

import com.mgr.api.model.DbConfig;
import com.mgr.api.repository.master.DbConfigRepository;
import com.mgr.api.ternant.TenantRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean(name = "dataSource")
    public TenantRoutingDataSource dataSource(
            @Lazy DbConfigRepository dbConfigRepository,
            @Qualifier("masterDataSource") DataSource masterDataSource) { // Lấy masterDB làm default an toàn

        Map<Object, Object> resolvedDataSource = new HashMap<>();

        // Load tất cả tenant từ MASTER DB
        List<DbConfig> configs = dbConfigRepository.findAll();

        for (DbConfig config : configs) {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(config.getUrl());
            ds.setUsername(config.getUsername());
            ds.setPassword(config.getPassword());
            ds.setDriverClassName(config.getDriverClassName());
            ds.setMaximumPoolSize(5);

            resolvedDataSource.put(config.getName(), ds);
        }

        // Routing datasource
        TenantRoutingDataSource routing = new TenantRoutingDataSource();
        routing.setTargetDataSources(resolvedDataSource);

        // Nếu ThreadLocal rỗng (không gửi X-Tenant), mặc định fallback về DB Master
        routing.setDefaultTargetDataSource(masterDataSource);

        routing.afterPropertiesSet();

        return routing;
    }
}