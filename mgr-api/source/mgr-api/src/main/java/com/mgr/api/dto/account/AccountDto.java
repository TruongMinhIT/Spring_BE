package com.mgr.api.dto.account;

import com.mgr.api.dto.ABasicAdminDto;
import com.mgr.api.dto.group.GroupDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class AccountDto extends ABasicAdminDto {
    @ApiModelProperty(name = "kind")
    private int kind;
    @ApiModelProperty(name = "username")
    private String username;
    @ApiModelProperty(name = "phone")
    private String phone;
    @ApiModelProperty(name = "email")
    private String email;
    @ApiModelProperty(name = "fullName")
    private String fullName;
    @ApiModelProperty(name = "group")
    private GroupDto group;
    @ApiModelProperty(name = "lastLogin")
    private Date lastLogin;
    @ApiModelProperty(name = "avatar")
    private String avatar;
    private Boolean isSuperAdmin;
    @ApiModelProperty(name = "isMfa")
    private Boolean isMfa;
}
