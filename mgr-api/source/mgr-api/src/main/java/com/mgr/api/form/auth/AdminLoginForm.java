package com.mgr.api.form.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class AdminLoginForm {
    @NotBlank(message = "username cant not be empty")
    @ApiModelProperty(name = "username", required = true)
    private String username;

    @NotBlank(message = "password cant not be emty")
    @ApiModelProperty(name = "password", required = true)
    private String password;
}
