package com.mgr.api.form.dbConfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class UpdateDbConfigForm {
    @ApiModelProperty(name = "id", required = true)
    private Long id;

    @ApiModelProperty(name = "url", required = true)
    private String url;

    @ApiModelProperty(name = "username", required = true)
    private String username;

    @ApiModelProperty(name = "password", required = true)
    private String password;

    @ApiModelProperty(name = "driverClassName", example = "com.mysql.cj.jdbc.Driver", required = true)
    private String driverClassName = "com.mysql.cj.jdbc.Driver";
}
