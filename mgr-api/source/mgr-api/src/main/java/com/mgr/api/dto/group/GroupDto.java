package com.mgr.api.dto.group;

import com.mgr.api.dto.ABasicAdminDto;
import com.mgr.api.dto.permission.PermissionDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class GroupDto extends ABasicAdminDto {
    @ApiModelProperty(name = "name")
    private String name;
    @ApiModelProperty(name = "description")
    private String description;
    @ApiModelProperty(name = "kind")
    private int kind;
    @ApiModelProperty(name = "isSystemRole")
    private Boolean isSystemRole;
    @ApiModelProperty(name = "permissions")
    private List<PermissionDto> permissions;
}
