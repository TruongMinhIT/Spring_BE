package com.mgr.api.component;

import com.mgr.api.config.SecurityConstant;
import com.mgr.api.jwt.MgrJwt;
import com.mgr.api.service.impl.UserServiceImpl;
import com.mgr.api.ternant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {
    @Autowired
    private UserServiceImpl userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // Lấy X-Tenant từ header của request do Client gửi lên
            String requestedTenant = request.getHeader(SecurityConstant.TENANT_HEADER);
            // Đọc jwtInfo từ SecurityContextHolder
            MgrJwt jwtInfo = userService.getAddInfoFromToken();

            if (jwtInfo != null) {
                // Danh sách tenant từ token
                String allowedTenantsStr = jwtInfo.getTenantId();
                log.info("[TenantInterceptor] accountId={}, allowedTenantsStr='{}'", jwtInfo.getAccountId(), allowedTenantsStr);

                if (StringUtils.isNotBlank(requestedTenant)) {
                    if (StringUtils.isNotBlank(allowedTenantsStr)) {
                        // Tách chuỗi bằng dấu ":" thành danh sách List<String>
                        List<String> allowedTenants = Arrays.asList(allowedTenantsStr.split(":"));
                        if (allowedTenants.contains(requestedTenant)) {
                            TenantContext.setCurrentTenant(requestedTenant);
                            return true;
                        }
                    }

                    // Nếu không có quyền -> Chặn request -> trả về lỗi 403
                    log.warn("[TenantInterceptor] Access Denied: accountId={}, allowedTenants='{}', requestedTenant='{}'",
                            jwtInfo.getAccountId(), jwtInfo.getTenantId(), requestedTenant);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Access Denied: You do not have permission for this tenant");
                    return false;
                } else {
                    // Nếu không truyền X-Tenant -> Master DB
                    TenantContext.clear();
                }
            }
        } catch (Exception exp) {
            log.error("Lỗi set Tenant: ", exp);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
