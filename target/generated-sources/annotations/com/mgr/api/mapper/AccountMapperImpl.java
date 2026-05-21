package com.mgr.api.mapper;

import com.mgr.api.dto.account.AccountAutoCompleteDto;
import com.mgr.api.dto.account.AccountDto;
import com.mgr.api.form.account.CreateAccountAdminForm;
import com.mgr.api.form.account.UpdateAccountAdminForm;
import com.mgr.api.model.Account;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-20T19:37:53+0700",
    comments = "version: 1.3.1.Final, compiler: javac, environment: Java 11.0.31 (Microsoft)"
)
@Component
public class AccountMapperImpl implements AccountMapper {

    @Autowired
    private GroupMapper groupMapper;

    @Override
    public Account fromCreateAdminProfileFormToEntity(CreateAccountAdminForm form) {
        if ( form == null ) {
            return null;
        }

        Account account = new Account();

        if ( form.getStatus() != null ) {
            account.setStatus( form.getStatus() );
        }
        account.setUsername( form.getUsername() );
        account.setPhone( form.getPhone() );
        account.setEmail( form.getEmail() );
        account.setFullName( form.getFullName() );
        account.setAvatarPath( form.getAvatarPath() );

        return account;
    }

    @Override
    public void mappingUpdateAdminProfileToEntity(UpdateAccountAdminForm form, Account account) {
        if ( form == null ) {
            return;
        }

        if ( form.getId() != null ) {
            account.setId( form.getId() );
        }
        if ( form.getStatus() != null ) {
            account.setStatus( form.getStatus() );
        }
        if ( form.getPhone() != null ) {
            account.setPhone( form.getPhone() );
        }
        if ( form.getEmail() != null ) {
            account.setEmail( form.getEmail() );
        }
        if ( form.getFullName() != null ) {
            account.setFullName( form.getFullName() );
        }
        if ( form.getAvatarPath() != null ) {
            account.setAvatarPath( form.getAvatarPath() );
        }
    }

    @Override
    public AccountDto fromAccountToDto(Account account) {
        if ( account == null ) {
            return null;
        }

        AccountDto accountDto = new AccountDto();

        accountDto.setLastLogin( account.getLastLogin() );
        accountDto.setKind( account.getKind() );
        accountDto.setFullName( account.getFullName() );
        accountDto.setIsSuperAdmin( account.getIsSuperAdmin() );
        accountDto.setAvatar( account.getAvatarPath() );
        accountDto.setPhone( account.getPhone() );
        accountDto.setId( account.getId() );
        accountDto.setEmail( account.getEmail() );
        accountDto.setUsername( account.getUsername() );
        accountDto.setGroup( groupMapper.fromEntityToGroupDto( account.getGroup() ) );
        accountDto.setStatus( account.getStatus() );

        return accountDto;
    }

    @Override
    public AccountAutoCompleteDto fromAccountToAutoCompleteDto(Account account) {
        if ( account == null ) {
            return null;
        }

        AccountAutoCompleteDto accountAutoCompleteDto = new AccountAutoCompleteDto();

        accountAutoCompleteDto.setAvatarPath( account.getAvatarPath() );
        accountAutoCompleteDto.setFullName( account.getFullName() );
        accountAutoCompleteDto.setId( account.getId() );

        return accountAutoCompleteDto;
    }

    @Override
    public List<AccountAutoCompleteDto> convertAccountToAutoCompleteDto(List<Account> list) {
        if ( list == null ) {
            return null;
        }

        List<AccountAutoCompleteDto> list1 = new ArrayList<AccountAutoCompleteDto>( list.size() );
        for ( Account account : list ) {
            list1.add( accountToAccountAutoCompleteDto( account ) );
        }

        return list1;
    }

    @Override
    public List<AccountDto> fromEntityToAccountDtoList(List<Account> accounts) {
        if ( accounts == null ) {
            return null;
        }

        List<AccountDto> list = new ArrayList<AccountDto>( accounts.size() );
        for ( Account account : accounts ) {
            list.add( fromAccountToDto( account ) );
        }

        return list;
    }

    protected AccountAutoCompleteDto accountToAccountAutoCompleteDto(Account account) {
        if ( account == null ) {
            return null;
        }

        AccountAutoCompleteDto accountAutoCompleteDto = new AccountAutoCompleteDto();

        accountAutoCompleteDto.setId( account.getId() );
        accountAutoCompleteDto.setFullName( account.getFullName() );
        accountAutoCompleteDto.setAvatarPath( account.getAvatarPath() );

        return accountAutoCompleteDto;
    }
}
