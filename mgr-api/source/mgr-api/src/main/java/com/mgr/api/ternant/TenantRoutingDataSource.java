package com.mgr.api.ternant;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// AbstractRoutingDataSource -> Choose db when connection occur
public class TenantRoutingDataSource extends AbstractRoutingDataSource {
    private Map<Object, Object> dataSources = new ConcurrentHashMap<>();

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
        this.dataSources = targetDataSources;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        // Return key from Map in DataSourceConfig
        return TenantContext.getCurrentTenant();
    }
    // Get connection -> call determineCurrentLookupKey (When query db to known
    // tenant)

    public void addDataSource(String tenantName, DataSource dataSource) {
        if (this.dataSources == null) {
            this.dataSources = new ConcurrentHashMap<>();
        }
        this.dataSources.put(tenantName, dataSource);
        super.setTargetDataSources(this.dataSources);
        super.afterPropertiesSet();
    }

    public void updateDataSource(String tenantName, DataSource newDataSource) {
        if (this.dataSources != null && this.dataSources.containsKey(tenantName)) {
            // Đóng pool cũ để giải phóng tài nguyên
            Object oldDataSource = this.dataSources.get(tenantName);
            if (oldDataSource instanceof HikariDataSource) {
                ((HikariDataSource) oldDataSource).close();
            }

            // Cập nhật pool mới
            this.dataSources.put(tenantName, newDataSource);
            super.setTargetDataSources(this.dataSources);
            super.afterPropertiesSet();
        }
    }

    public void removeDataSource(String tenantName) {
        if (this.dataSources != null && this.dataSources.containsKey(tenantName)) {
            // Đóng pool trước khi xóa
            Object oldDataSource = this.dataSources.remove(tenantName);
            if (oldDataSource instanceof HikariDataSource) {
                ((HikariDataSource) oldDataSource).close();
            }

            super.setTargetDataSources(this.dataSources);
            super.afterPropertiesSet();
        }
    }
}
