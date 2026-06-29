package com.mgr.api.dto.nation;

import com.mgr.api.dto.ABasicAdminDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class NationDto extends ABasicAdminDto {
    @ApiModelProperty(name = "name")
    private String name;

    @ApiModelProperty(name = "kind")
    private Integer kind;

    @ApiModelProperty(name = "parentId")
    private Long parentId;
}
