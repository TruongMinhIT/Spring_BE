package com.mgr.api.dto.permission;

import com.mgr.api.dto.ABasicAdminDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PermissionDto extends ABasicAdminDto {
    @ApiModelProperty(name = "name")
    private String name;
    @ApiModelProperty(name = "action")
    private String action;
    @ApiModelProperty(name = "showMenu")
    private Boolean showMenu;
    @ApiModelProperty(name = "description")
    private String description;
    @ApiModelProperty(name = "nameGroup")
    private String nameGroup;
    @ApiModelProperty(name = "permissionCode")
    private String permissionCode;
}
