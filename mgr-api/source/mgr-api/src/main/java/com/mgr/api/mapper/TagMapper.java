package com.mgr.api.mapper;

import com.mgr.api.dto.tag.TagDto;
import com.mgr.api.form.tag.CreateTagForm;
import com.mgr.api.model.Tag;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TagMapper {
    @Mapping(source = "name", target = "name")
    @Mapping(source = "slug", target = "slug")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateTagFormToEntity")
    Tag fromCreateTagFormToEntity(CreateTagForm createTagForm);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "slug", target = "slug")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToTagDto")
    TagDto fromEntityToTagDto(Tag tag);

    @IterableMapping(elementTargetType = TagDto.class, qualifiedByName = "fromEntityToTagDto")
    List<TagDto> fromEntityToTagDtoList(List<Tag> tags);
}
