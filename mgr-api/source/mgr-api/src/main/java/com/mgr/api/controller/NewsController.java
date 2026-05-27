package com.mgr.api.controller;

import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ErrorCode;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.news.NewsDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.exception.NotFoundException;
import com.mgr.api.form.news.CreateNewsForm;
import com.mgr.api.form.news.UpdateNewsForm;
import com.mgr.api.mapper.NewsMapper;
import com.mgr.api.model.Category;
import com.mgr.api.model.News;
import com.mgr.api.model.User;
import com.mgr.api.model.criteria.NewsCriteria;
import com.mgr.api.repository.CategoryRepository;
import com.mgr.api.repository.NewsRepository;
import com.mgr.api.repository.UserRepository;
import com.mgr.api.service.MgrApiService;
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

@RestController
@RequestMapping("v1/news")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class NewsController extends ABasicController {
    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MgrApiService mgrApiService;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NEW_V')")
    public ApiMessageDto<NewsDto> getNews(@PathVariable("id") Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("News not found", ErrorCode.NEWS_ERROR_NOT_FOUND));
        return makeSuccessResponse(newsMapper.fromNewsToDto(news), "Get news success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NEW_C')")
    public ApiMessageDto<String> createNews(@Valid @RequestBody CreateNewsForm createNewsForm, BindingResult bindingResult) {
        Long userId = getCurrentUser();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_ERROR_NOT_FOUND));
        Category category = categoryRepository.findById(createNewsForm.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found", ErrorCode.CATEGORY_ERROR_NOT_FOUND));
        News news = newsMapper.fromCreateNewsFormToEntity(createNewsForm);
        news.setCategory(category);
        news.setUser(user);
        newsRepository.save(news);
        return makeSuccessResponse("Create News success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NEW_U')")
    public ApiMessageDto<String> updateNews(@Valid @RequestBody UpdateNewsForm updateNewsForm, BindingResult bindingResult) {
        News news = newsRepository.findById(updateNewsForm.getId())
                .orElseThrow(() -> new NotFoundException("New not found", ErrorCode.NEWS_ERROR_NOT_FOUND));
        long currentUserId = getCurrentUser();
        if (!news.getUser().getId().equals(currentUserId)) {
            throw new BadRequestException("You don't have permission to update this news", ErrorCode.NEWS_ERROR_UNABLE_UPDATE);
        }
        if (StringUtils.isNoneBlank(updateNewsForm.getTitle())) {
            news.setTitle(updateNewsForm.getTitle());
        }
        if (StringUtils.isNoneBlank(updateNewsForm.getDescription())) {
            news.setDescription(updateNewsForm.getDescription());
        }
        if (StringUtils.isNoneBlank(updateNewsForm.getThumbnailUrl())) {
            if (updateNewsForm.getThumbnailUrl() != null && !news.getThumbnailUrl().equals(updateNewsForm.getThumbnailUrl())) {
                mgrApiService.deleteFile(updateNewsForm.getThumbnailUrl());
            }
            news.setThumbnailUrl(updateNewsForm.getThumbnailUrl());
        }
        if (updateNewsForm.getCategoryId() != null && !updateNewsForm.getCategoryId().equals(news.getCategory().getId())) {
            Category category = categoryRepository.findById(updateNewsForm.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found", ErrorCode.CATEGORY_ERROR_NOT_FOUND));
            news.setCategory(category);
        }
        newsRepository.save(news);
        return makeSuccessResponse("Update news success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NEW_L')")
    public ApiMessageDto<ResponseListDto<NewsDto>> list(NewsCriteria newsCriteria, Pageable pageable) {
        Page<News> page = newsRepository.findAll(newsCriteria.getSpecification(), pageable);
        ResponseListDto<NewsDto> responseListDto = new ResponseListDto(newsMapper.fromEntityToNewsDtoList(page.getContent()),
                page.getTotalElements(),
                page.getTotalPages());
        return makeSuccessResponse(responseListDto, "List news success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NEW_D')")
    public ApiMessageDto<String> delete(@PathVariable("id") Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("News not found", ErrorCode.NEWS_ERROR_NOT_FOUND));
        Long currentUserId = getCurrentUser();
        if (!isSuperAdmin() && !news.getUser().getId().equals(currentUserId)) {
            throw new BadRequestException("You don not have permission to delete this news", ErrorCode.NEWS_ERROR_UNABLE_DELETE);
        }
        if (StringUtils.isNoneBlank(news.getThumbnailUrl())) {
            mgrApiService.deleteFile(news.getThumbnailUrl());
        }
        newsRepository.deleteById(id);
        return makeSuccessResponse("Delete news success.");
    }
}