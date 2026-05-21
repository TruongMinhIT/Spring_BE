package com.mgr.api.mapper;

import com.mgr.api.dto.group.GroupDto;
import com.mgr.api.form.group.CreateGroupForm;
import com.mgr.api.model.Group;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE, //Dù kh đủ field so với entrity cũng k báo lỗi
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, //Nếu kh có data thì kh cập nhật (K mặc định là null)
        uses = {PermissionMapper.class})
public interface GroupMapper {
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @BeanMapping(ignoreByDefault = true)
    Group fromCreateGroupFormToEntity(CreateGroupForm createGroupForm);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "isSystemRole", target = "isSystemRole")
    @Mapping(source = "permissions", target = "permissions", qualifiedByName = "fromEntityToPermissionDtoShortList")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToGroupDto")
    GroupDto fromEntityToGroupDto(Group group);

    @IterableMapping(elementTargetType = GroupDto.class, qualifiedByName = "fromEntityToGroupDto")
    @Named("fromEntityToGroupDtoList")
    List<GroupDto> fromEntityToGroupDtoList(List<Group> groups);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "permissions", target = "permissions", qualifiedByName = "fromEntityToPermissionDtoShortList")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToGroupDtoShort")
    GroupDto fromEntityToGroupDtoShort(Group group);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToGroupDtoAutoComplete")
    GroupDto fromEntityToGroupDtoAutoComplete(Group group);

    @IterableMapping(elementTargetType = GroupDto.class, qualifiedByName = "fromEntityToGroupDtoAutoComplete")
    List<GroupDto> fromEntityToGroupDtoAutoCompleteList(List<Group> groups);
}
