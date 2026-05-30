package com.mgr.api.controller;

import com.mgr.api.client.AuthClient;
import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.form.auth.AdminLoginForm;
import com.mgr.api.form.auth.OAuth2TokenRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class AuthClientController extends ABasicController {
    @Autowired
    private AuthClient authClient;

    @PostMapping(value = "/login-internal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Object> loginInternal(@RequestHeader("Authorization") String authHeader,
                                               @Valid @RequestBody AdminLoginForm loginForm) {
        OAuth2TokenRequest request = new OAuth2TokenRequest();
        request.setUsername(loginForm.getUsername());
        request.setPassword(loginForm.getPassword());
        request.setGrantType("password");
        Object tokenResponse = authClient.getAccessToken(authHeader, request);
        return makeSuccessResponse(tokenResponse, "Login internal success");
    }
}
