package com.mgr.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Accept to receive excess field
public class OAuth2TokenDto {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("jti")
    private String jti;

    @JsonProperty("user_kind")
    private Integer userKind;

    @JsonProperty("tenant_info")
    private String tenantInfo;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("grant_type")
    private String grantType;

    @JsonProperty("additional_info")
    private String additionalInfo;
}
