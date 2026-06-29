package com.mgr.api.dto.dbConfig;

import com.mgr.api.dto.ABasicAdminDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DbConfigDto extends ABasicAdminDto {
    @ApiModelProperty(name = "name")
    private String name;

    @ApiModelProperty(name = "url")
    private String url;

    @ApiModelProperty(name = "username")
    private String username;

    @ApiModelProperty(name = "driverClassName")
    private String driverClassName;

    @ApiModelProperty(name = "userId")
    private Long userId;
}
