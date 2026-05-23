package com.mgr.api.dto.user;

import com.mgr.api.dto.ABasicAdminDto;
import com.mgr.api.dto.account.AccountDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class UserDto extends ABasicAdminDto {
    @ApiModelProperty(name ="gender", notes = "1:male, 2:female, 3:other")
    private Integer gender;
    @ApiModelProperty(name = "dateOfBirth")
    private Date dateOfBirth;
    @ApiModelProperty(name = "account")
    private AccountDto account;
}