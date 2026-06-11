package com.mgr.api.mapper;

import com.mgr.api.dto.post.PostDto;
import com.mgr.api.form.post.CreatePostForm;
import com.mgr.api.form.post.UpdatePostForm;
import com.mgr.api.model.Post;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserMapper.class, CategoryMapper.class}
)
public interface PostMapper {
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "conditionStatus", target = "conditionStatus")
    @Mapping(source = "isFree", target = "isFree")
    @Mapping(source = "type", target = "type")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreatePostFormToEntity")
    Post fromCreatePostFormToEntity(CreatePostForm createPostForm);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "conditionStatus", target = "conditionStatus")
    @Mapping(source = "isFree", target = "isFree")
    @Mapping(source = "type", target = "type")
    @BeanMapping(ignoreByDefault = true)
    void mappingUpdatePostToEntity(UpdatePostForm form, @MappingTarget Post post);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "conditionStatus", target = "conditionStatus")
    @Mapping(source = "isFree", target = "isFree")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "user", target = "user", qualifiedByName = "fromUserToSimpleDto")
    @Mapping(source = "category", target = "category", qualifiedByName = "fromCategoryToDto")
    @Mapping(source = "tags", target = "tags")
    @BeanMapping(ignoreByDefault = true)
    PostDto fromEntityToDto(Post post);

    @IterableMapping(elementTargetType = PostDto.class, qualifiedByName = "fromEntityToDto")
    List<PostDto> fromEntityToPostDtoList(List<Post> posts);
}
