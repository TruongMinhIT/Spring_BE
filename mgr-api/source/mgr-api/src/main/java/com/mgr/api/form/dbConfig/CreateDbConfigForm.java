package com.mgr.api.form.dbConfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateDbConfigForm {
    @NotBlank(message = "name (tenantId) is required")
    @ApiModelProperty(name = "name", required = true)
    private String name;

    @NotBlank(message = "url to connect db is required")
    @ApiModelProperty(name = "url", required = true)
    private String url;

    @NotBlank(message = "username db is required")
    @ApiModelProperty(name = "username", required = true)
    private String username;

    @NotBlank(message = "password db is required")
    @ApiModelProperty(name = "password", required = true)
    private String password;

    @NotBlank(message = "Driver class is required")
    @ApiModelProperty(name = "driverClassName", example = "com.mysql.cj.jdbc.Driver", required = true)
    private String driverClassName = "com.mysql.cj.jdbc.Driver";
}
