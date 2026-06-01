package com.mgr.api.client;

import com.mgr.api.config.CustomFeignConfig;
import com.mgr.api.form.auth.OAuth2TokenRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "authInternalClient", url = "${feign.client.auth-server.url}", configuration = CustomFeignConfig.class)
public interface AuthClient {
    @PostMapping(value = "/api/token", consumes = MediaType.APPLICATION_JSON_VALUE)
    OAuth2AccessToken getAccessToken(@RequestHeader("Authorization") String authHeader,
                                     @RequestBody OAuth2TokenRequest request);
}
