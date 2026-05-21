package com.mgr.api.mapper;

import com.mgr.api.dto.permission.PermissionDto;
import com.mgr.api.form.permission.CreatePermissionForm;
import com.mgr.api.model.Permission;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PermissionMapper {
    @Mapping(source = "name", target = "name")
    @Mapping(source = "action", target = "action")
    @Mapping(source = "showMenu", target = "showMenu")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "nameGroup", target = "nameGroup")
    @Mapping(source = "permissionCode", target = "permissionCode")
    @BeanMapping(ignoreByDefault = true)
    Permission fromCreatePermissionFormToEntity(CreatePermissionForm createPermissionForm);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "action", target = "action")
    @Mapping(source = "showMenu", target = "showMenu")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "nameGroup", target = "nameGroup")
    @Mapping(source = "permissionCode", target = "permissionCode")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToPermissionDto")
    PermissionDto fromEntityToPermissionDto(Permission permission);

    @IterableMapping(elementTargetType = PermissionDto.class, qualifiedByName = "fromEntityToPermissionDto")
    @Named("fromEntityToPermissionDtoList")
    List<PermissionDto> fromEntityToPermissionDtoList(List<Permission> permissions);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "action", target = "action")
    @Mapping(source = "nameGroup", target = "nameGroup")
    @Mapping(source = "permissionCode", target = "permissionCode")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToPermissionDtoShort")
    PermissionDto fromEntityToPermissionDtoShort(Permission permission);

    @IterableMapping(elementTargetType = PermissionDto.class, qualifiedByName = "fromEntityToPermissionDtoShort")
    @Named("fromEntityToPermissionDtoShortList")
    List<PermissionDto> fromEntityToPermissionDtoShortList(List<Permission> permissions);
}
