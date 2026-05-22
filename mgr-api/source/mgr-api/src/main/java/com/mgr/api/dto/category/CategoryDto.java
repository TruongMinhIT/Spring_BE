package com.mgr.api.dto.category;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CategoryDto {
    @ApiModelProperty(name = "name")
    private String name;
    @ApiModelProperty(name = "description")
    private String description;

}
