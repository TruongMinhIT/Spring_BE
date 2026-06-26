package com.mgr.api.service;

import com.mgr.api.model.DbConfig;
import com.mgr.api.ternant.TenantRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Service
@Slf4j
public class DbConfigService {
    @Autowired
    private TenantRoutingDataSource tenantRoutingDataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void provisionTenantDatabase(DbConfig config) throws Exception {
        createPhysicalDatabase(config);
        HikariDataSource newDataSource = createDataSource(config);
        runLiquibase(newDataSource);
        tenantRoutingDataSource.addDataSource(config.getName(), newDataSource); //Cập nhật Map và resolvedDataSources
    }

    public void updateDataSource(DbConfig config) {
        HikariDataSource newDataSource = createDataSource(config);
        tenantRoutingDataSource.updateDataSource(config.getName(), newDataSource);
        log.info("Update Datasource success for Tenant: {}", config.getName());
    }

    public void removeDataSource(String tenantName) {
        tenantRoutingDataSource.removeDataSource(tenantName);
        log.info("Delete Datasource success for Tenant: {}", tenantName);
    }

    private void createPhysicalDatabase(DbConfig config) throws Exception {
        String dbUrl = config.getUrl();
        String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1,
                dbUrl.contains("?") ? dbUrl.indexOf("?") : dbUrl.length());
        String serverUrl = dbUrl.substring(0, dbUrl.lastIndexOf("/"));
        try (Connection conn = DriverManager.getConnection(serverUrl, config.getUsername(), config.getPassword());
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
    }

    private HikariDataSource createDataSource(DbConfig config) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(config.getUrl());
        ds.setUsername(config.getUsername());
        ds.setPassword(config.getPassword());
        ds.setDriverClassName(config.getDriverClassName());
        return ds;
    }

    private void runLiquibase(DataSource dataSource) throws Exception {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:liquibase/db.changelog-tenant.xml");
        liquibase.setShouldRun(true);
        liquibase.afterPropertiesSet();
    }
}
