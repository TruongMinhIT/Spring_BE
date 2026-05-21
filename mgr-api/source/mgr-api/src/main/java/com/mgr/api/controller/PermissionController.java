package com.mgr.api.controller;

import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ErrorCode;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.permission.PermissionDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.form.permission.CreatePermissionForm;
import com.mgr.api.mapper.PermissionMapper;
import com.mgr.api.model.Permission;
import com.mgr.api.model.criteria.PermissionCriteria;
import com.mgr.api.repository.PermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/permission")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
@ApiIgnore //Giấu api khỏi swagger
public class PermissionController extends ABasicController {
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PermissionMapper permissionMapper;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)// Api trả về json
    @PreAuthorize("hasRole('PER_C')")
    //BindingResult Gửi thếu trường cho Form -> 400 BadRequest
    public ApiMessageDto<String> create(@Valid @RequestBody CreatePermissionForm createPermissionForm, BindingResult bindingResult) {
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();

        Permission permission = permissionRepository.findFirstByName(createPermissionForm.getName());
        if (permission != null) {
            throw new BadRequestException("Permission name is existed!", ErrorCode.PERMISSION_ERROR_NAME_EXISTED);
        }
        boolean permissionCodeExist = permissionRepository.existsByPermissionCode(createPermissionForm.getPermissionCode());
        if (permissionCodeExist) {
            throw new BadRequestException("Permission code is existed!", ErrorCode.PERMISSION_ERROR_CODE_EXISTED);
        }
        permission = permissionMapper.fromCreatePermissionFormToEntity(createPermissionForm);
        permissionRepository.save(permission);
        apiMessageDto.setMessage("Create a new permission success.");
        return apiMessageDto;
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PER_L')")
    public ApiMessageDto<ResponseListDto<PermissionDto>> listPermissions(PermissionCriteria permissionCriteria) {
        ApiMessageDto<ResponseListDto<PermissionDto>> apiMessageDto = new ApiMessageDto<>();
        Page<Permission> page = permissionRepository.findAll(permissionCriteria.getSpecification(), PageRequest.of(0, 1000, Sort.by(new Sort.Order(Sort.Direction.DESC, "createdDate"))));
        ResponseListDto<PermissionDto> responseListDto = new ResponseListDto(permissionMapper.fromEntityToPermissionDtoList(page.getContent()), page.getTotalElements(), page.getTotalPages());
        apiMessageDto.setData(responseListDto);
        apiMessageDto.setMessage("List permissions success.");
        return apiMessageDto;
    }
}
