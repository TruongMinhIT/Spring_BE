package com.mgr.api.form.post;

import com.mgr.api.validation.ValidPostConditionStatus;
import com.mgr.api.validation.ValidPostType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel
public class UpdatePostForm {
    @NotNull(message = "id can not null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;

    @ApiModelProperty(name = "title", required = true)
    private String title;

    @ApiModelProperty(name = "description", required = true)
    private String description;

    @ApiModelProperty(name = "price")
    private BigDecimal price;

    @ValidPostConditionStatus(allowNull = true)
    @ApiModelProperty(name = "conditionStatus", required = true, notes = "1: Brand new, 2: Used")
    private Integer conditionStatus;

    @ApiModelProperty(name = "isFree", required = true)
    private Boolean isFree;

    @ValidPostType(allowNull = true)
    @ApiModelProperty(name = "type", required = true, notes = "1: For sale, 2: For buy")
    private Integer type;

    @ApiModelProperty(name = "categoryId", required = true)
    private Long categoryId;

    @ApiModelProperty(name = "tagIds")
    private List<Long> tagIds;
}
