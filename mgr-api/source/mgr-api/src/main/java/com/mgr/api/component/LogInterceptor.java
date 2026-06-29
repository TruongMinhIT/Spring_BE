package com.mgr.api.component;

import com.mgr.api.config.SecurityConstant;
import com.mgr.api.jwt.MgrJwt;
import com.mgr.api.service.impl.UserServiceImpl;
import com.mgr.api.ternant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class LogInterceptor implements HandlerInterceptor {
    @Autowired
    private UserServiceImpl userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {
        if (DispatcherType.REQUEST.name().equals(request.getDispatcherType().name())
                && request.getMethod().equals(HttpMethod.GET.name())) {

        }
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        log.debug("Starting call url: [" + getUrl(request) + "]");

        // Tenant
        try {
            // Lấy X-Tenant từ header của request do Client gửi lên
            String requestedTenant = request.getHeader(SecurityConstant.TENANT_HEADER);
            // Đọc jwtInfo từ SecurityContextHolder
            MgrJwt jwtInfo = userService.getAddInfoFromToken();

            if (jwtInfo != null) {
                // Danh sách tenant từ token
                String allowedTenantsStr = jwtInfo.getTenantId();
                log.info("[Tenant] accountId={}, allowedTenantsStr='{}'", jwtInfo.getAccountId(), allowedTenantsStr);

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
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;
        log.debug("Complete [" + getUrl(request) + "] executeTime : " + executeTime + "ms");

        // Clear tenat after request complete
        TenantContext.clear();

        if (ex != null) {
            log.error("afterCompletion>> " + ex.getMessage());
        }
    }

    /**
     * get full url request
     *
     * @param req
     * @return
     */
    private static String getUrl(HttpServletRequest req) {
        String reqUrl = req.getRequestURL().toString();
        String queryString = req.getQueryString();   // d=789
        if (!StringUtils.isEmpty(queryString)) {
            reqUrl += "?" + queryString;
        }
        return reqUrl;
    }
}
