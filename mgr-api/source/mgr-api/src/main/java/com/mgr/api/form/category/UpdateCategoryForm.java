package com.mgr.api.form.category;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class UpdateCategoryForm {
    @NotNull(message = "id cant not be null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;

    @ApiModelProperty(name = "name", required = true)
    private String name;

    @ApiModelProperty(name = "description", required = true)
    private String description;
}
