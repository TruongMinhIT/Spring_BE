package com.mgr.api.form.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class UpdateUserProfileForm {
    @ApiModelProperty(name = "username")
    private String username;

    @ApiModelProperty(name = "email")
    private String email;

    @ApiModelProperty(name = "phone")
    private String phone;

    @ApiModelProperty(name = "password")
    private String password;

    @ApiModelProperty(name = "fullName")
    private String fullName;

    @ApiModelProperty(name = "avatarPath")
    private String avatarPath;
}