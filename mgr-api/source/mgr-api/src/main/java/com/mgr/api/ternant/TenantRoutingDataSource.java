package com.mgr.api.ternant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

// AbstractRoutingDataSource -> Choose db when connection occur
public class TenantRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        // Return key from Map in DataSourceConfig
        return TenantContext.getCurrentTenant();
    }
    // Get connection -> call determineCurrentLookupKey (When query db to known tenant)
}
