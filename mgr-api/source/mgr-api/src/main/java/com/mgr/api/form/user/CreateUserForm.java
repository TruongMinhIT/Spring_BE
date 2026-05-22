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
public class CreateUserForm {
    @ApiModelProperty(name = "gender", required = true, notes = "1:male, 2:female, 3:other")
    @ValidGender(allowNull = false)
    private Integer gender;

    @NotNull(message = "Date of birth cannot null")
    @Past(message = "Date of birth must be in the past")
    @ApiModelProperty(name = "dateOfBirth", required = true)
    private Date dateOfBirth;

}
