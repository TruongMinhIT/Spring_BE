package com.mgr.api.mapper;

import com.mgr.api.dto.nation.NationDto;
import com.mgr.api.form.nation.CreateNationForm;
import com.mgr.api.model.Nation;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NationMapper {
    @Mapping(source = "name", target = "name")
    @Mapping(source = "kind", target = "kind")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateNationFormToEntity")
    Nation fromCreateNationFormToEntity(CreateNationForm createNationForm);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "parent.id", target = "parentId")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToNationDto")
    NationDto fromEntityToNationDto(Nation nation);

    @IterableMapping(elementTargetType = NationDto.class, qualifiedByName = "fromEntityToNationDto")
    List<NationDto> fromEntityToNationDtoList(List<Nation> nations);
}
