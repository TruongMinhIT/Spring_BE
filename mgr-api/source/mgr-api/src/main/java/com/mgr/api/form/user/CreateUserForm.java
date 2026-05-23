package com.mgr.api.form.user;

import com.mgr.api.validation.ValidGender;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.util.Date;

@Data
@ApiModel
public class CreateUserForm {
    @NotEmpty(message = "username can not empty")
    @ApiModelProperty(name = "username", required = true)
    private String username;

    @NotEmpty(message = "password can not be empty")
    @ApiModelProperty(name = "password", required = true)
    private String password;

    @NotEmpty(message = "Full name can not be empty")
    @ApiModelProperty(name = "fullName")
    private String fullName;

    @ApiModelProperty(name = "email")
    @Email
    private String email;

    @ApiModelProperty(name = "phone")
    private String phone;

    @NotNull(message = "status can not null")
    @ApiModelProperty(name = "status", required = true)
    private Integer status;

    @NotNull(message = "groupId can not null")
    @ApiModelProperty(name = "groupId", required = true)
    private Long groupId;

    @ApiModelProperty(name = "avatarPath")
    private String avatarPath;


    @ApiModelProperty(name = "gender", required = true, notes = "1:male, 2:female, 3:other")
    @ValidGender(allowNull = false)
    private Integer gender;

    @NotNull(message = "Date of birth cannot null")
    @Past(message = "Date of birth must be in the past")
    @ApiModelProperty(name = "dateOfBirth", required = true)
    private Date dateOfBirth;
}