package com.mgr.api.component;

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

@Component
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {
    @Autowired
    private UserServiceImpl userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // Read jwtInfo from SecurityContextHolder
            MgrJwt jwtInfo = userService.getAddInfoFromToken();
            // If decode & it has tenantId → set it to ThreadLocal
            if (jwtInfo != null && StringUtils.isNotBlank(jwtInfo.getTenantId())) {
                TenantContext.setCurrentTenant(jwtInfo.getTenantId());
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
