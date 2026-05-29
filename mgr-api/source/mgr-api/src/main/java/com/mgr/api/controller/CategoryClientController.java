package com.mgr.api.controller;

import com.mgr.api.client.CategoryClient;
import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.category.CategoryDto;
import com.mgr.api.model.criteria.CategoryCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("v1/category-internal")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class CategoryClientController extends ABasicController {
    @Autowired
    private CategoryClient categoryClient;

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CAT_L')")
    public ApiMessageDto<ResponseListDto<List<CategoryDto>>> listCategory(CategoryCriteria categoryCriteria, Pageable pageable) {
        String currentToken = getCurrentToken();
        ApiMessageDto<ResponseListDto<List<CategoryDto>>> response = categoryClient
                .listCategory("Bearer " + currentToken, categoryCriteria, pageable);
        return makeSuccessResponse(response.getData(), "Get internal list success");
    }
}
