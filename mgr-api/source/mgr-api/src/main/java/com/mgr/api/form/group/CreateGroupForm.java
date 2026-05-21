package com.mgr.api.form.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class CreateGroupForm {
    @NotEmpty(message = "Name cant not be empty")
    @ApiModelProperty(name = "name", required = true)
    private String name;

    @NotEmpty(message = "description cant not be empty")
    @ApiModelProperty(name = "description", required = true)
    private String description;

    @NotNull(message = "permissions cant not be null")
    @ApiModelProperty(name = "permissions", required = true)
    private Long[] permissions;
}
