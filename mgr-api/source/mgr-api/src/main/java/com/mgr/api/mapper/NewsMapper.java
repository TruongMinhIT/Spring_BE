package com.mgr.api.mapper;

import com.mgr.api.dto.news.NewsDto;
import com.mgr.api.form.news.CreateNewsForm;
import com.mgr.api.model.News;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy= NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserMapper.class, CategoryMapper.class}
)
public interface NewsMapper {

    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateNewsFormToEntity")
    News fromCreateNewsFormToEntity (CreateNewsForm createNewsForm);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "category", target = "category", qualifiedByName = "fromCategoryToDto")
    @Mapping(source = "user", target = "user", qualifiedByName = "fromUserToDto")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromNewsToDto")
    NewsDto fromNewsToDto(News news);

    @IterableMapping(elementTargetType = NewsDto.class)
    List<NewsDto> fromEntityToNewsDtoList(List<News> news);
}
