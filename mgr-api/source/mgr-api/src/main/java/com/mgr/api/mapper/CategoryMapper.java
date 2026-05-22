package com.mgr.api.mapper;

import com.mgr.api.dto.category.CategoryDto;
import com.mgr.api.form.category.CreateCategoryForm;
import com.mgr.api.model.Category;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateCategoryFormToEntity")
    Category fromCreateCategoryFormToEntity(CreateCategoryForm createCategoryForm);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCategoryToDto")
    CategoryDto fromCategoryToDto(Category category);

    @IterableMapping(elementTargetType = CategoryDto.class)
    List<CategoryDto> fromEntityToCategoryDtoList(List<Category> categories);
}
