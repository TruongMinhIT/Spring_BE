package com.mgr.api.mapper;

import com.mgr.api.dto.account.AccountAutoCompleteDto;
import com.mgr.api.dto.account.AccountDto;
import com.mgr.api.form.account.CreateAccountAdminForm;
import com.mgr.api.form.account.UpdateAccountAdminForm;
import com.mgr.api.model.Account;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {GroupMapper.class})
public interface AccountMapper {
    @Mapping(target = "password", ignore = true)
    Account fromCreateAdminProfileFormToEntity(CreateAccountAdminForm form);

    @Mapping(target = "password", ignore = true)
    void mappingUpdateAdminProfileToEntity(UpdateAccountAdminForm form, @MappingTarget Account account);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "group", target = "group", qualifiedByName = "fromEntityToGroupDto")
    @Mapping(source = "lastLogin", target = "lastLogin")
    @Mapping(source = "avatarPath", target = "avatar")
    @Mapping(source = "isSuperAdmin", target = "isSuperAdmin")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromAccountToDto")
    AccountDto fromAccountToDto(Account account);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "avatarPath", target = "avatarPath")
    @Mapping(source = "fullName", target = "fullName")
    @Named("fromAccountToAutoCompleteDto")
    AccountAutoCompleteDto fromAccountToAutoCompleteDto(Account account);

    @IterableMapping(elementTargetType = AccountAutoCompleteDto.class)
    List<AccountAutoCompleteDto> convertAccountToAutoCompleteDto(List<Account> list);

    @IterableMapping(elementTargetType = AccountDto.class, qualifiedByName = "fromAccountToDto")
    @Named("fromEntityToAccountDtoList")
    List<AccountDto> fromEntityToAccountDtoList(List<Account> accounts);
}
