package com.mgr.api.controller;

import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ErrorCode;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.category.CategoryDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.exception.NotFoundException;
import com.mgr.api.form.category.CreateCategoryForm;
import com.mgr.api.form.category.UpdateCategoryForm;
import com.mgr.api.mapper.CategoryMapper;
import com.mgr.api.model.Category;
import com.mgr.api.model.News;
import com.mgr.api.model.criteria.CategoryCriteria;
import com.mgr.api.repository.CategoryRepository;
import com.mgr.api.repository.NewsRepository;
import com.mgr.api.service.MgrApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/category")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class CategoryController extends ABasicController{
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    NewsRepository newsRepository;
    @Autowired
    private MgrApiService mgrApiService;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CAT_V')")
    public ApiMessageDto<CategoryDto> getCategory(@PathVariable("id") Long id){
        ApiMessageDto<CategoryDto> apiMessageDto = new ApiMessageDto<>();
        Category category = categoryRepository.findById(id).orElse(null);

        if (category == null){
            throw new NotFoundException("Category not found!", ErrorCode.CATEGORY_ERROR_NOT_FOUND);
        }
        apiMessageDto.setData(categoryMapper.fromCategoryToDto(category));
        apiMessageDto.setMessage("Get category success");
        return apiMessageDto;
    }


    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CAT_C')")
    public  ApiMessageDto<String> createCategory(@Valid @RequestBody CreateCategoryForm createCategoryForm, BindingResult bindingResult){
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        Category category = categoryRepository.findFirstByName(createCategoryForm.getName()).orElse(null);
        if (category != null){
            throw new BadRequestException("Category name is exist", ErrorCode.CATEGORY_ERROR_NAME_EXISTED);
        }
        category = categoryMapper.fromCreateCategoryFormToEntity(createCategoryForm);
        categoryRepository.save(category);
        apiMessageDto.setMessage("Create a new category success");
        return apiMessageDto;
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CAT_U')")
    public ApiMessageDto<String> updateCategory(@Valid @RequestBody UpdateCategoryForm updateCategoryForm, BindingResult bindingResult){
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        Category category = categoryRepository.findById(updateCategoryForm.getId()).orElse(null);
        if (category == null){
            throw  new NotFoundException("Category not found!", ErrorCode.CATEGORY_ERROR_NOT_FOUND);
        }
        if (StringUtils.isNoneBlank(updateCategoryForm.getName())){
            Category otherCategory = categoryRepository.findFirstByName(updateCategoryForm.getName()).orElse(null);
            if (otherCategory != null && !Objects.equals(updateCategoryForm.getId(), otherCategory.getId())){
                throw new BadRequestException("Can update this category because it is exist", ErrorCode.CATEGORY_ERROR_NAME_EXISTED);
            }
            category.setName(updateCategoryForm.getName());
        }
        if(StringUtils.isNoneBlank(updateCategoryForm.getDescription())){
            category.setDescription(updateCategoryForm.getDescription());
        }
        categoryRepository.save(category);
        apiMessageDto.setMessage("Update category success.");
        return apiMessageDto;
    }

    @GetMapping(value="/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CAT_L')")
    public ApiMessageDto<ResponseListDto<CategoryDto>> list(CategoryCriteria categoryCriteria, Pageable pageable){
        ApiMessageDto<ResponseListDto<CategoryDto>> apiMessageDto = new ApiMessageDto<>();
        Page<Category> page = categoryRepository.findAll(categoryCriteria.getSpecification(),pageable);
        List<CategoryDto> categoryDtos = categoryMapper.fromEntityToCategoryDtoList(page.getContent());

        ResponseListDto<CategoryDto> responseListDto = new ResponseListDto(categoryDtos, page.getTotalElements(), page.getTotalPages());
        apiMessageDto.setData(responseListDto);
        apiMessageDto.setMessage("List Category success");
        return apiMessageDto;
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CAT_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            throw new NotFoundException("Category not found!", ErrorCode.CATEGORY_ERROR_NOT_FOUND);
        }
        List<News> newsList = newsRepository.findAllByCategoryId(id);
        if (!newsList.isEmpty()) {
            for (News news : newsList) {
                if (StringUtils.isNoneBlank(news.getThumbnailUrl())) {
                    mgrApiService.deleteFile(news.getThumbnailUrl());
                }
            }
            newsRepository.deleteInBatch(newsList);
        }
        categoryRepository.deleteById(id);
        return  makeSuccessResponse("Delete Category success");
    }
}
