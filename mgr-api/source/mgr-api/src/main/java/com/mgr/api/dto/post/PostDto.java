package com.mgr.api.dto.post;

import com.mgr.api.dto.ABasicAdminDto;
import com.mgr.api.dto.category.CategoryDto;
import com.mgr.api.dto.tag.TagDto;
import com.mgr.api.dto.user.UserDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel
public class PostDto extends ABasicAdminDto {
    @ApiModelProperty(name = "title")
    private String title;
    @ApiModelProperty(name = "description")
    private String description;
    @ApiModelProperty(name = "price")
    private BigDecimal price;
    @ApiModelProperty(name = "conditionStatus")
    private Integer conditionStatus;
    @ApiModelProperty(name = "isFree")
    private Boolean isFree;
    @ApiModelProperty(name = "type")
    private Integer type;
    @ApiModelProperty(name = "user")
    private UserDto user;
    @ApiModelProperty(name = "category")
    private CategoryDto category;
    @ApiModelProperty(name = "tags")
    private List<TagDto> tags;
}
