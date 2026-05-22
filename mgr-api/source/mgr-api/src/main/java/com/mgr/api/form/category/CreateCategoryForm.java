package com.mgr.api.form.category;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel
public class CreateCategoryForm {
    @NotEmpty(message = "name cant not be empty")
    @ApiModelProperty(name = "name", required = true)
    private String name;
    @NotEmpty(message = "description cant not be emty")
    @ApiModelProperty(name = "description", required = true)
    private String description;
}
