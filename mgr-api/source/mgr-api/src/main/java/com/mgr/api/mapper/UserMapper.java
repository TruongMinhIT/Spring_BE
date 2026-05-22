package com.mgr.api.mapper;

import com.mgr.api.dto.user.UserDto;
import com.mgr.api.form.user.CreateUserForm;
import com.mgr.api.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateUserFormToEntity")
    User fromCreateUserFormToEntity (CreateUserForm createUserForm);


    @Mapping(source = "id", target = "id")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromUserToDto")
    UserDto fromUserToDto (User user);
}
