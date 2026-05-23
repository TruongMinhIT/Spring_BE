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
public class UpdateUserForm {

    @NotNull(message = "id can not null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;

    @ApiModelProperty(name = "password")
    private String password;

    @ApiModelProperty(name = "fullName")
    private String fullName;

    @ApiModelProperty(name = "email")
    @Email
    private String email;

    @ApiModelProperty(name = "phone")
    private String phone;

    @ApiModelProperty(name = "status")
    private Integer status;

    @ApiModelProperty(name = "groupId")
    private Long groupId;

    @ApiModelProperty(name = "avatarPath")
    private String avatarPath;

    @ValidGender(allowNull = true)
    @ApiModelProperty(name = "gender", notes = "1: male, 2: female, 3: other")
    private Integer gender;

    @ApiModelProperty(name = "dateOfBirth")
    @Past(message = "Date of birth must be in the past")
    private Date dateOfBirth;
}
