package com.mgr.api.form.news;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class UpdateNewsForm {
    @NotNull(message = "id can not null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;

    @ApiModelProperty(name = "title")
    private String title;

    @ApiModelProperty(name = "description")
    private String description;

    @ApiModelProperty(name = "thubnailUrl")
    private String thumbnailUrl;

    @ApiModelProperty(name = "categoryId")
    private Long categoryId;
}
