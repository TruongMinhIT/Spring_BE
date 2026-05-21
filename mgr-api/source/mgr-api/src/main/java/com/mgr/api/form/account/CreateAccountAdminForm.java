package com.mgr.api.form.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class CreateAccountAdminForm {
    @NotEmpty(message = "username cant not be empty")
    @ApiModelProperty(name = "username", required = true)
    private String username;

    @ApiModelProperty(name = "email")
    @Email
    private String email;

    @ApiModelProperty(name = "phone")
    private String phone;

    @NotEmpty(message = "password cant not be empty")
    @ApiModelProperty(name = "password", required = true)
    private String password;

    @NotEmpty(message = "fullName cant not be empty")
    @ApiModelProperty(name = "fullName", example = "Toan Huynh Thanh Nguyen", required = true)
    private String fullName;

    @NotNull(message = "status cant not be null")
    @ApiModelProperty(name = "status", required = true)
    private Integer status;

    @NotNull(message = "groupId cant not be null")
    @ApiModelProperty(name = "groupId", required = true)
    private Long groupId;

    @ApiModelProperty(name = "avatarPath")
    private String avatarPath;
}
