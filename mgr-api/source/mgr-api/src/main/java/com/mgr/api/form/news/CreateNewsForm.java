package com.mgr.api.form.news;

import com.mgr.api.model.Auditable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class CreateNewsForm {
    @NotEmpty(message = "title can not empty")
    @ApiModelProperty(name = "title", required = true)
    private String title;

    @NotEmpty(message = "description can not empty")
    @ApiModelProperty(name = "description", required = true)
    private String description;

    @NotEmpty(message = "thumbNailUrl can not empty")
    @ApiModelProperty(name = "thumbnailUrl", required = true)
    private String thumbnailUrl;

    @NotNull(message = "categoryId can not null")
    @ApiModelProperty(name = "categoryId", required = true)
    private Long categoryId;
}
