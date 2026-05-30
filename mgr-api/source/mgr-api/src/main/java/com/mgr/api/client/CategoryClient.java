package com.mgr.api.client;

import com.mgr.api.config.CustomFeignConfig;
import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.category.CategoryDto;
import com.mgr.api.model.criteria.CategoryCriteria;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "categoryInternalClient", url = "http://localhost:8787", configuration = CustomFeignConfig.class)
public interface CategoryClient {
    @GetMapping("/v1/category/list")
    ApiMessageDto<ResponseListDto<List<CategoryDto>>> listCategory(@RequestHeader("Authorization") String token,
                                                                   @SpringQueryMap CategoryCriteria categoryCriteria,
                                                                   @SpringQueryMap Pageable pageable);
}
