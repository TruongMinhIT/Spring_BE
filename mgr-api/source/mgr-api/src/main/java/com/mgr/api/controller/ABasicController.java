package com.mgr.api.controller;

import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.jwt.MgrJwt;
import com.mgr.api.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.util.List;
import java.util.function.Function;

public class ABasicController {
    @Autowired
    private UserServiceImpl userService;

    public <T> ApiMessageDto<T> makeResponse(Boolean result, T data, String message, String code) {
        ApiMessageDto<T> apiMessageDto = new ApiMessageDto<>();
        apiMessageDto.setResult(result);
        apiMessageDto.setData(data);
        apiMessageDto.setMessage(message);
        apiMessageDto.setCode(code);
        return apiMessageDto;
    }

    public <T> ApiMessageDto<T> makeSuccessResponse(String message) {
        return makeResponse(true, null, message, null);
    }

    public <T> ApiMessageDto<T> makeSuccessResponse(T data, String message) {
        return makeResponse(true, data, message, null);
    }

    public <T, R> ResponseListDto<R> makeResponseListDto(Page<T> page, Function<List<T>, R> mapper) {
        return new ResponseListDto<>(
                mapper.apply(page.getContent()),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public long getCurrentUser() {
        MgrJwt mgrJwt = userService.getAddInfoFromToken();
        return mgrJwt.getAccountId();
    }

    public long getTokenId() {
        MgrJwt mgrJwt = userService.getAddInfoFromToken();
        return mgrJwt.getTokenId();
    }

    public MgrJwt getSessionFromToken() {
        return userService.getAddInfoFromToken();
    }

    public boolean isSuperAdmin() {
        MgrJwt mgrJwt = userService.getAddInfoFromToken();
        if (mgrJwt != null) {
            return mgrJwt.getIsSuperAdmin();
        }
        return false;
    }

    public String getCurrentToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            OAuth2AuthenticationDetails oauthDetails =
                    (OAuth2AuthenticationDetails) authentication.getDetails();
            if (oauthDetails != null) {
                return oauthDetails.getTokenValue();
            }
        }
        return null;
    }
}
