package com.mgr.api.dto.news;

import com.mgr.api.dto.ABasicAdminDto;
import com.mgr.api.dto.category.CategoryDto;
import com.mgr.api.dto.user.UserDto;
import com.mgr.api.dto.user.UserSimpleDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel
public class NewsDto extends ABasicAdminDto {
    @ApiModelProperty(name = "title")
    private String title;
    @ApiModelProperty(name = "description")
    private String description;
    @ApiModelProperty(name = "thumbnailUrl")
    private String thumbnailUrl;
    @ApiModelProperty(name = "category")
    private CategoryDto category;
    @ApiModelProperty(name = "user")
    private UserSimpleDto user;
}
