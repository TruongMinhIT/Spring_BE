package com.mgr.api.config;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;

import java.util.Map;

public class CustomTokenConverter extends DefaultAccessTokenConverter {
    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> claims) {
        // 1. Gọi hàm chuẩn của Spring để xử lý các thông tin mặc định
        OAuth2Authentication authentication
                = super.extractAuthentication(claims);
        // 2. "Ghi nhớ" toàn bộ thông tin JWT vào trong đối tượng Authentication
        authentication.setDetails(claims); //Giữ lại addition detail vào detail trong authen
        return authentication;
    }
}
