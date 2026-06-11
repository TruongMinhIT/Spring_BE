package com.mgr.api.form.post;

import com.mgr.api.validation.ValidPostConditionStatus;
import com.mgr.api.validation.ValidPostType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel
public class CreatePostForm {
    @NotBlank(message = "title can not empty")
    @ApiModelProperty(name = "title", required = true)
    private String title;

    @NotBlank(message = "description can not empty")
    @ApiModelProperty(name = "description", required = true)
    private String description;

    @ApiModelProperty(name = "price")
    private BigDecimal price;

    @ValidPostConditionStatus
    @ApiModelProperty(name = "conditionStatus", required = true, notes = "1: Brand new, 2: Used")
    private Integer conditionStatus;

    @NotNull(message = "is free can not null")
    @ApiModelProperty(name = "isFree", required = true)
    private Boolean isFree;

    @ValidPostType
    @ApiModelProperty(name = "type", required = true, notes = "1: For sale, 2: For buy")
    private Integer type;

    @NotNull(message = "categoryId can not null")
    @ApiModelProperty(name = "categoryId", required = true)
    private Long categoryId;

    @ApiModelProperty(name = "tagIds")
    private List<Long> tagIds;
}
