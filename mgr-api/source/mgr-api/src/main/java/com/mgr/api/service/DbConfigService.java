package com.mgr.api.service;

import com.mgr.api.model.DbConfig;
import com.mgr.api.ternant.CustomMultiTenantConnectionProvider;
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
    private CustomMultiTenantConnectionProvider connectionProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void provisionTenantDatabase(DbConfig config) throws Exception {
        createPhysicalDatabase(config);
        HikariDataSource newDataSource = createDataSource(config);
        runLiquibase(newDataSource);
        connectionProvider.addOrUpdateTenantDataSource(config.getName(), newDataSource); // Cập nhật Map và resolvedDataSources
    }

    public void updateDataSource(DbConfig config) {
        HikariDataSource newDataSource = createDataSource(config);
        connectionProvider.addOrUpdateTenantDataSource(config.getName(), newDataSource);
        log.info("Update Datasource success for Tenant: {}", config.getName());
    }

    public void removeDataSource(String tenantName) {
        connectionProvider.removeTenantDataSource(tenantName);
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

    public HikariDataSource createDataSource(DbConfig config) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(config.getUrl());
        ds.setUsername(config.getUsername());
        ds.setPassword(config.getPassword());
        ds.setDriverClassName(config.getDriverClassName());

        // Tối ưu Pool Size cho Multi-Tenant
        ds.setMinimumIdle(0); // Thu hồi toàn bộ connection nếu tenant không hoạt động
        ds.setMaximumPoolSize(10); // Số lượng kết nối tối đa cho mỗi tenant
        ds.setIdleTimeout(300000); // 5 phút (Đóng connection nếu nhàn rỗi quá 5 phút)
        ds.setConnectionTimeout(20000); // Timeout 20s khi không lấy được connection

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
