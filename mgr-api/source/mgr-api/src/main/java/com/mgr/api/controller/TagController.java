package com.mgr.api.controller;

import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ErrorCode;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.tag.TagDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.exception.NotFoundException;
import com.mgr.api.form.tag.CreateTagForm;
import com.mgr.api.form.tag.UpdateTagForm;
import com.mgr.api.mapper.TagMapper;
import com.mgr.api.model.Tag;
import com.mgr.api.model.criteria.TagCriteria;
import com.mgr.api.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("v1/tag")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class TagController extends ABasicController {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagController(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TAG_V')")
    public ApiMessageDto<TagDto> getTag(@PathVariable("id") Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag not found", ErrorCode.TAG_ERROR_NOT_FOUND));
        return makeSuccessResponse(tagMapper.fromEntityToTagDto(tag), "Get tag success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TAG_C')")
    public ApiMessageDto<String> createTag(@Valid @RequestBody CreateTagForm createTagForm, BindingResult bindingResult) {
        if (tagRepository.findFirstByName(createTagForm.getName()).isPresent()) {
            throw new BadRequestException("Tag name is existed", ErrorCode.TAG_ERROR_NAME_EXISTED);
        }
        if (tagRepository.findFirstBySlug(createTagForm.getSlug()).isPresent()) {
            throw new BadRequestException("Tag slug is existed", ErrorCode.TAG_ERROR_SLUG_EXISTED);
        }
        Tag tag = tagMapper.fromCreateTagFormToEntity(createTagForm);
        tagRepository.save(tag);
        return makeSuccessResponse("Create tag success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TAG_U')")
    public ApiMessageDto<String> update(@Valid @RequestBody UpdateTagForm updateTagForm, BindingResult bindingResult) {
        Tag tag = tagRepository.findById(updateTagForm.getId())
                .orElseThrow(() -> new NotFoundException("Tag not found", ErrorCode.TAG_ERROR_NOT_FOUND));
        Tag existedNameTag = tagRepository.findFirstByName(updateTagForm.getName()).orElse(null);
        if (existedNameTag != null && !existedNameTag.getId().equals(updateTagForm.getId())) {
            throw new BadRequestException("Tag name is existed", ErrorCode.TAG_ERROR_NAME_EXISTED);
        }
        tag.setName(updateTagForm.getName());
        Tag existedNameSlug = tagRepository.findFirstBySlug(updateTagForm.getSlug()).orElse(null);
        if (existedNameSlug != null && !existedNameSlug.getId().equals(updateTagForm.getId())) {
            throw new BadRequestException("Tag slug is existed", ErrorCode.TAG_ERROR_SLUG_EXISTED);
        }
        tag.setSlug(updateTagForm.getSlug());
        tagRepository.save(tag);
        return makeSuccessResponse("Update tag successs");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TAG_L')")
    public ApiMessageDto<ResponseListDto<TagDto>> list(TagCriteria tagCriteria, Pageable pageable) {
        Page<Tag> page = tagRepository.findAll(tagCriteria.getSpecification(), pageable);
        ResponseListDto<TagDto> responseListDto = new ResponseListDto(tagMapper.fromEntityToTagDtoList(page.getContent()),
                page.getTotalElements(),
                page.getTotalPages());
        return makeSuccessResponse(responseListDto, "List tags success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TAG_D')")
    public ApiMessageDto<String> delete(@PathVariable("id") Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag not found", ErrorCode.TAG_ERROR_NOT_FOUND));
        tagRepository.removeTagFromAllNews(id);
        tagRepository.removeTagFromAllPost(id);
        tagRepository.deleteById(id);
        return makeSuccessResponse("Delete tag success");
    }
}
