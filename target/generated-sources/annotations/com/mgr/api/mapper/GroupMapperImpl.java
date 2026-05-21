package com.mgr.api.mapper;

import com.mgr.api.dto.group.GroupDto;
import com.mgr.api.form.group.CreateGroupForm;
import com.mgr.api.model.Group;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
public class GroupMapperImpl implements GroupMapper {

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public Group fromCreateGroupFormToEntity(CreateGroupForm createGroupForm) {
        if ( createGroupForm == null ) {
            return null;
        }

        Group group = new Group();

        group.setName( createGroupForm.getName() );
        group.setDescription( createGroupForm.getDescription() );

        return group;
    }

    @Override
    public GroupDto fromEntityToGroupDto(Group group) {
        if ( group == null ) {
            return null;
        }

        GroupDto groupDto = new GroupDto();

        if ( group.getCreatedDate() != null ) {
            groupDto.setCreatedDate( LocalDateTime.ofInstant( group.getCreatedDate().toInstant(), ZoneId.of( "UTC" ) ) );
        }
        groupDto.setIsSystemRole( group.getIsSystemRole() );
        groupDto.setPermissions( permissionMapper.fromEntityToPermissionDtoShortList( group.getPermissions() ) );
        groupDto.setName( group.getName() );
        if ( group.getModifiedDate() != null ) {
            groupDto.setModifiedDate( LocalDateTime.ofInstant( group.getModifiedDate().toInstant(), ZoneId.of( "UTC" ) ) );
        }
        groupDto.setDescription( group.getDescription() );
        groupDto.setId( group.getId() );
        groupDto.setStatus( group.getStatus() );

        return groupDto;
    }

    @Override
    public List<GroupDto> fromEntityToGroupDtoList(List<Group> groups) {
        if ( groups == null ) {
            return null;
        }

        List<GroupDto> list = new ArrayList<GroupDto>( groups.size() );
        for ( Group group : groups ) {
            list.add( fromEntityToGroupDto( group ) );
        }

        return list;
    }

    @Override
    public GroupDto fromEntityToGroupDtoShort(Group group) {
        if ( group == null ) {
            return null;
        }

        GroupDto groupDto = new GroupDto();

        groupDto.setPermissions( permissionMapper.fromEntityToPermissionDtoShortList( group.getPermissions() ) );
        groupDto.setName( group.getName() );
        groupDto.setDescription( group.getDescription() );
        groupDto.setId( group.getId() );

        return groupDto;
    }

    @Override
    public GroupDto fromEntityToGroupDtoAutoComplete(Group group) {
        if ( group == null ) {
            return null;
        }

        GroupDto groupDto = new GroupDto();

        groupDto.setName( group.getName() );
        groupDto.setId( group.getId() );

        return groupDto;
    }

    @Override
    public List<GroupDto> fromEntityToGroupDtoAutoCompleteList(List<Group> groups) {
        if ( groups == null ) {
            return null;
        }

        List<GroupDto> list = new ArrayList<GroupDto>( groups.size() );
        for ( Group group : groups ) {
            list.add( fromEntityToGroupDtoAutoComplete( group ) );
        }

        return list;
    }
}
