package com.mgr.api.form.user;

import com.mgr.api.validation.ValidGender;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.util.Date;

@Data
@ApiModel
public class UpdateUserForm {
    @ValidGender(allowNull = true)
    @ApiModelProperty(name = "gender", notes = "1: male, 2: female, 3: other")
    private Integer gender;

    @ApiModelProperty(name = "dateOfBirth")
    @Past(message = "Date of birth must be in the past")
    private Date dateOfBirth;
}
