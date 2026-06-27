package com.mgr.api.ternant;

import com.mgr.api.config.SecurityConstant;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {
    @Override
    public String resolveCurrentTenantIdentifier() {
        // Lấy tên tenant từ context của Request hiện tại (ThreadLocal)
        String tenantId = TenantContext.getCurrentTenant();
        // Nếu không có, mặc định trả về Master
        return StringUtils.isNotBlank(tenantId) ? tenantId : SecurityConstant.DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
