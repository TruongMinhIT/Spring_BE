package com.mgr.api.ternant;

import com.mgr.api.config.SecurityConstant;
import com.mgr.api.model.DbConfig;
import com.mgr.api.repository.master.DbConfigRepository;
import com.mgr.api.service.DbConfigService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class CustomMultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    @Autowired
    private ApplicationContext context;

    // Nơi lưu trữ tất cả Connection Pool của các chi nhánh
    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();

    // Connection Pool của hệ thống Master
    private DataSource masterDataSource;

    public void setMasterDataSource(DataSource masterDataSource) {
        this.masterDataSource = masterDataSource;
    }

    public void addOrUpdateTenantDataSource(String tenantName, DataSource dataSource) {
        DataSource oldDs = tenantDataSources.put(tenantName, dataSource);
        // Đóng Pool cũ nếu tồn tại để tránh rò rỉ Connection
        if (oldDs instanceof AutoCloseable) {
            try {
                ((AutoCloseable) oldDs).close();
                log.info(">>> Đã đóng DataSource cũ của Tenant: {}", tenantName);
            } catch (Exception e) {
                log.error(">>> Lỗi khi đóng DataSource cũ của Tenant: {}", tenantName, e);
            }
        }
    }

    public void removeTenantDataSource(String tenantName) {
        DataSource oldDs = tenantDataSources.remove(tenantName);
        // Đóng Pool khi xóa Tenant
        if (oldDs instanceof AutoCloseable) {
            try {
                ((AutoCloseable) oldDs).close();
                log.info(">>> Đã đóng DataSource của Tenant: {}", tenantName);
            } catch (Exception e) {
                log.error(">>> Lỗi khi đóng DataSource của Tenant: {}", tenantName, e);
            }
        }
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return masterDataSource;
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        // Nếu là Master hoặc không xác định được Tenant thì trả về Master DB
        if (SecurityConstant.DEFAULT_TENANT.equals(tenantIdentifier) || tenantIdentifier == null) {
            return masterDataSource;
        }
        // Tạo Pool nếu chưa có trong Map
        return tenantDataSources.computeIfAbsent(tenantIdentifier, key -> {
            DbConfigRepository dbConfigRepository = context.getBean(DbConfigRepository.class);
            DbConfigService dbConfigService = context.getBean(DbConfigService.class);

            try {
                log.info(">>> Đang khởi tạo Pool mới cho Tenant: {}", key);
                DbConfig config = dbConfigRepository.findByName(key)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình Tenant: " + key));

                return dbConfigService.createDataSource(config);
            } catch (Exception e) {
                log.error(">>> Lỗi tạo DataSource cho {}: {}", key, e.getMessage());
                return masterDataSource; // Fallback về master nếu có lỗi
            }
        });
    }
}