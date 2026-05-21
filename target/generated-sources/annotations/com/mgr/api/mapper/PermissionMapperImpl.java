package com.mgr.api.mapper;

import com.mgr.api.dto.permission.PermissionDto;
import com.mgr.api.form.permission.CreatePermissionForm;
import com.mgr.api.model.Permission;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-20T19:37:52+0700",
    comments = "version: 1.3.1.Final, compiler: javac, environment: Java 11.0.31 (Microsoft)"
)
@Component
public class PermissionMapperImpl implements PermissionMapper {

    @Override
    public Permission fromCreatePermissionFormToEntity(CreatePermissionForm createPermissionForm) {
        if ( createPermissionForm == null ) {
            return null;
        }

        Permission permission = new Permission();

        permission.setDescription( createPermissionForm.getDescription() );
        permission.setNameGroup( createPermissionForm.getNameGroup() );
        permission.setShowMenu( createPermissionForm.getShowMenu() );
        permission.setName( createPermissionForm.getName() );
        permission.setAction( createPermissionForm.getAction() );
        permission.setPermissionCode( createPermissionForm.getPermissionCode() );

        return permission;
    }

    @Override
    public PermissionDto fromEntityToPermissionDto(Permission permission) {
        if ( permission == null ) {
            return null;
        }

        PermissionDto permissionDto = new PermissionDto();

        if ( permission.getCreatedDate() != null ) {
            permissionDto.setCreatedDate( LocalDateTime.ofInstant( permission.getCreatedDate().toInstant(), ZoneId.of( "UTC" ) ) );
        }
        permissionDto.setShowMenu( permission.getShowMenu() );
        permissionDto.setName( permission.getName() );
        if ( permission.getModifiedDate() != null ) {
            permissionDto.setModifiedDate( LocalDateTime.ofInstant( permission.getModifiedDate().toInstant(), ZoneId.of( "UTC" ) ) );
        }
        permissionDto.setAction( permission.getAction() );
        permissionDto.setDescription( permission.getDescription() );
        permissionDto.setId( permission.getId() );
        permissionDto.setNameGroup( permission.getNameGroup() );
        permissionDto.setPermissionCode( permission.getPermissionCode() );
        permissionDto.setStatus( permission.getStatus() );

        return permissionDto;
    }

    @Override
    public List<PermissionDto> fromEntityToPermissionDtoList(List<Permission> permissions) {
        if ( permissions == null ) {
            return null;
        }

        List<PermissionDto> list = new ArrayList<PermissionDto>( permissions.size() );
        for ( Permission permission : permissions ) {
            list.add( fromEntityToPermissionDto( permission ) );
        }

        return list;
    }

    @Override
    public PermissionDto fromEntityToPermissionDtoShort(Permission permission) {
        if ( permission == null ) {
            return null;
        }

        PermissionDto permissionDto = new PermissionDto();

        permissionDto.setName( permission.getName() );
        permissionDto.setAction( permission.getAction() );
        permissionDto.setId( permission.getId() );
        permissionDto.setNameGroup( permission.getNameGroup() );
        permissionDto.setPermissionCode( permission.getPermissionCode() );

        return permissionDto;
    }

    @Override
    public List<PermissionDto> fromEntityToPermissionDtoShortList(List<Permission> permissions) {
        if ( permissions == null ) {
            return null;
        }

        List<PermissionDto> list = new ArrayList<PermissionDto>( permissions.size() );
        for ( Permission permission : permissions ) {
            list.add( fromEntityToPermissionDtoShort( permission ) );
        }

        return list;
    }
}
