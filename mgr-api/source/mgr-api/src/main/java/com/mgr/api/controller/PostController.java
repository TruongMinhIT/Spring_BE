package com.mgr.api.controller;

import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ErrorCode;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.post.PostDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.exception.NotFoundException;
import com.mgr.api.form.post.CreatePostForm;
import com.mgr.api.form.post.UpdatePostForm;
import com.mgr.api.mapper.PostMapper;
import com.mgr.api.model.Category;
import com.mgr.api.model.Post;
import com.mgr.api.model.Tag;
import com.mgr.api.model.User;
import com.mgr.api.model.criteria.PostCriteria;
import com.mgr.api.repository.CategoryRepository;
import com.mgr.api.repository.PostRepository;
import com.mgr.api.repository.TagRepository;
import com.mgr.api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/v1/post")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class PostController extends ABasicController{
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('POST_V')")
    public ApiMessageDto<PostDto> getPost(@PathVariable("id") Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found", ErrorCode.POST_ERROR_NOT_FOUND));
        return makeSuccessResponse(postMapper.fromEntityToDto(post), "Get post success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('POST_C')")
    public ApiMessageDto<String> createPost(@Valid @RequestBody CreatePostForm createPostForm, BindingResult bindingResult) {
        Post post = postMapper.fromCreatePostFormToEntity(createPostForm);
        Long userId = getCurrentUser();
        User user = userRepository.findById(userId)
                .orElseThrow(() ->new NotFoundException("User not found", ErrorCode.USER_ERROR_NOT_FOUND));
        post.setUser(user);
        Category category = categoryRepository.findById(createPostForm.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found", ErrorCode.CATEGORY_ERROR_NOT_FOUND));
        post.setCategory(category);
        if (createPostForm.getTagIds() != null && !createPostForm.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(createPostForm.getTagIds());
            if (tags.size() != createPostForm.getTagIds().size()) {
                throw new BadRequestException("One or more tags do not exist", ErrorCode.TAG_ERROR_NOT_FOUND);
            }
            post.setTags(tags);
        }
        postRepository.save(post);
        return makeSuccessResponse("Create post success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('POST_U')")
    public ApiMessageDto<String> updatePost(@Valid @RequestBody UpdatePostForm updatePostForm, BindingResult bindingResult) {
        Post post = postRepository.findById(updatePostForm.getId())
                .orElseThrow(() -> new NotFoundException("Post not found", ErrorCode.POST_ERROR_NOT_FOUND));
        Long currentUserId = getCurrentUser();
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new BadRequestException("You do not have permission to update this post", ErrorCode.POST_ERROR_UNABLE_UPDATE);
        }
        postMapper.mappingUpdatePostToEntity(updatePostForm, post);
        if (updatePostForm.getCategoryId() != null) {
            Category category = categoryRepository.findById(updatePostForm.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found", ErrorCode.CATEGORY_ERROR_NOT_FOUND));
            post.setCategory(category);
        }
        if (updatePostForm.getTagIds() != null && !updatePostForm.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(updatePostForm.getTagIds());
            if (tags.size() != updatePostForm.getTagIds().size()) {
                throw new BadRequestException("One or more tags do not exist", ErrorCode.TAG_ERROR_NOT_FOUND);
            }
            post.setTags(tags);
        }
        postRepository.save(post);
        return makeSuccessResponse("Update post success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('POST_L')")
    public ApiMessageDto<ResponseListDto<PostDto>> listPost(PostCriteria postCriteria, Pageable pageable) {
        Page<Post> page = postRepository.findAll(postCriteria.getSpecification(), pageable);
        ResponseListDto<PostDto> responseListDto = new ResponseListDto(postMapper.fromEntityToPostDtoList(page.getContent()),
                page.getTotalElements(),
                page.getTotalPages());
        return makeSuccessResponse(responseListDto, "List post success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('POST_D')")
    public ApiMessageDto<String> deletePost(@PathVariable("id") Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found", ErrorCode.POST_ERROR_NOT_FOUND));
        Long currentUserId = getCurrentUser();
        if (!isSuperAdmin() && !post.getUser().getId().equals(currentUserId)) {
            throw new BadRequestException("You do not have permission to delete this post", ErrorCode.POST_ERROR_UNABLE_DELETE);
        }
        postRepository.deleteById(id);
        return makeSuccessResponse("Delete post success.");
    }
}
