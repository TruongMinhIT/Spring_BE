package com.mgr.api.dto.account;

import lombok.Data;

@Data
public class AccountAutoCompleteDto {
    private Long id;
    private String fullName;
    private String avatarPath;
}
