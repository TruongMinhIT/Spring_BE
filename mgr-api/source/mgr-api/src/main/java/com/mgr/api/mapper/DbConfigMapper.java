package com.mgr.api.mapper;

import com.mgr.api.dto.dbConfig.DbConfigDto;
import com.mgr.api.form.dbConfig.CreateDbConfigForm;
import com.mgr.api.form.dbConfig.UpdateDbConfigForm;
import com.mgr.api.model.DbConfig;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DbConfigMapper {
    @Mapping(source = "name", target = "name")
    @Mapping(source = "url", target = "url")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "driverClassName", target = "driverClassName")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateDbConfigFormToEntity")
    DbConfig fromCreateDbConfigFormToEntity(CreateDbConfigForm createDbConfigForm);

    @Mapping(source = "url", target = "url")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "driverClassName", target = "driverClassName")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromUpdateDbConfigFormToEntity")
    void fromUpdateDbConfigFormToEntity(UpdateDbConfigForm updateDbConfigForm, @MappingTarget DbConfig dbConfig);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "url", target = "url")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "driverClassName", target = "driverClassName")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "createdDate", target = "createdDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToDbConfigDto")
    DbConfigDto fromEntityToDbConfigDto(DbConfig dbConfig);

    @IterableMapping(elementTargetType = DbConfigDto.class, qualifiedByName = "fromEntityToDbConfigDto")
    List<DbConfigDto> fromEntityToDbConfigDtoList(List<DbConfig> dbConfigs);
}
