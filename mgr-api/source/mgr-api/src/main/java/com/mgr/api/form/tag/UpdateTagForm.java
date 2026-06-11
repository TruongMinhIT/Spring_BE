package com.mgr.api.form.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class UpdateTagForm {
    @NotBlank(message = "id can not null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;

    @NotBlank(message = "name can not empty")
    @ApiModelProperty(name = "name", required = true)
    private String name;

    @NotBlank(message = "slug can not empty")
    @ApiModelProperty(name = "slug", required = true)
    private String slug;
}
