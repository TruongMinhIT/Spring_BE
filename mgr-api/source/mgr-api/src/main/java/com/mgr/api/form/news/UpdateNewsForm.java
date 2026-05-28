package com.mgr.api.form.news;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class UpdateNewsForm {
    @NotNull(message = "id can not null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;

    @NotBlank(message = "title can not empty")
    @ApiModelProperty(name = "title", required = true)
    private String title;

    @NotBlank(message = "description can not empty")
    @ApiModelProperty(name = "description", required = true)
    private String description;

    @ApiModelProperty(name = "thumbnailUrl")
    private String thumbnailUrl;

    @NotNull(message = "categoryId can not null")
    @ApiModelProperty(name = "categoryId", required = true)
    private Long categoryId;
}