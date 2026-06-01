package com.mgr.api.form.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OAuth2TokenRequest {
    private String username;

    private String password;

    @JsonProperty("grant_type")
    private String grantType = "password";
}
