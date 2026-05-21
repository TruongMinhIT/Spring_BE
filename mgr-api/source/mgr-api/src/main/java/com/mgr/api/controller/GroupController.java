package com.mgr.api.controller;

import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.group.GroupDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.exception.NotFoundException;
import com.mgr.api.form.group.CreateGroupForm;
import com.mgr.api.form.group.UpdateGroupForm;
import com.mgr.api.mapper.GroupMapper;
import com.mgr.api.model.Group;
import com.mgr.api.model.Permission;
import com.mgr.api.model.criteria.GroupCriteria;
import com.mgr.api.repository.GroupRepository;
import com.mgr.api.repository.PermissionRepository;
import com.mgr.api.dto.ResponseListDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.mgr.api.dto.ErrorCode;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/group")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class GroupController extends ABasicController {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private PermissionRepository permissionRepository;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_C')")
    public ApiMessageDto<String> create(@Valid @RequestBody CreateGroupForm createGroupForm, BindingResult bindingResult) {
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        Group group = groupRepository.findFirstByName(createGroupForm.getName());
        if (group != null) {
            throw new BadRequestException("Group name is exist!", ErrorCode.GROUP_ERROR_NAME_EXISTED);
        }
        group = groupMapper.fromCreateGroupFormToEntity(createGroupForm);
        List<Permission> permissions = new ArrayList<>();
        for (long permissionId : createGroupForm.getPermissions()) {
            Permission permission = permissionRepository.findById(permissionId).orElse(null);
            if (permission != null) {
                permissions.add(permission);
            }
        }
        group.setPermissions(permissions);
        groupRepository.save(group);
        apiMessageDto.setMessage("Create a new group success.");
        return apiMessageDto;
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_U')")
    public ApiMessageDto<String> update(@Valid @RequestBody UpdateGroupForm updateGroupForm, BindingResult bindingResult) {
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        Group group;
        if (isSuperAdmin()) {
            group = groupRepository.findById(updateGroupForm.getId()).orElse(null);
        } else {
            group = groupRepository.findByIdAndIsSystemRole(updateGroupForm.getId(), isSuperAdmin()).orElse(null);
        }
        if (group == null) {
            throw new NotFoundException("Group not found!", ErrorCode.GROUP_ERROR_NOT_FOUND);
        }
        if (StringUtils.isNoneBlank(updateGroupForm.getName())) {
            Group otherGroup = groupRepository.findFirstByName(updateGroupForm.getName());
            if (otherGroup != null && !Objects.equals(updateGroupForm.getId(), otherGroup.getId())) {
                throw new BadRequestException("Cant update this group name because it is exist!", ErrorCode.GROUP_ERROR_NAME_EXISTED);
            }
            group.setName(updateGroupForm.getName());
        }
        if (StringUtils.isNoneBlank(updateGroupForm.getDescription())) {
            group.setDescription(updateGroupForm.getDescription());
        }
        List<Permission> permissions = new ArrayList<>();
        for (long permissionId : updateGroupForm.getPermissions()) {
            Permission permission = permissionRepository.findById(permissionId).orElse(null);
            if (permission != null) {
                permissions.add(permission);
            }
        }
        group.setPermissions(permissions);
        groupRepository.save(group);
        apiMessageDto.setMessage("Update group success.");
        return apiMessageDto;
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_V')")
    public ApiMessageDto<Group> get(@PathVariable("id") Long id) {
        ApiMessageDto<Group> apiMessageDto = new ApiMessageDto<>();
        Group group;
        if (isSuperAdmin()) {
            group = groupRepository.findById(id).orElse(null);
        } else {
            group = groupRepository.findByIdAndIsSystemRole(id, isSuperAdmin()).orElse(null);
        }
        if (group == null) {
            throw new NotFoundException("Group not found!", ErrorCode.GROUP_ERROR_NOT_FOUND);
        }
        apiMessageDto.setData(group);
        apiMessageDto.setMessage("Get group success.");
        return apiMessageDto;
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('GR_L')")
    public ApiMessageDto<ResponseListDto<Group>> list(GroupCriteria groupCriteria, Pageable pageable) {
        ApiMessageDto<ResponseListDto<Group>> apiMessageDto = new ApiMessageDto<>();
        Page<Group> groups;
        if (isSuperAdmin()) {
            groups = groupRepository
                    .findAll(groupCriteria.getSpecification(), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.DESC, "createdDate"))));
        } else {
            groupCriteria.setIsSystemRole(isSuperAdmin());
            groups = groupRepository
                    .findAll(groupCriteria.getSpecification(), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.DESC, "createdDate"))));
        }
        ResponseListDto<Group> responseListDto = new ResponseListDto(groups.getContent(), groups.getTotalElements(), groups.getTotalPages());
        apiMessageDto.setData(responseListDto);
        apiMessageDto.setMessage("List group success.");
        return apiMessageDto;
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<GroupDto>> autoCompleteGroup(GroupCriteria groupCriteria, @PageableDefault(size = 10) Pageable pageable) {
        ApiMessageDto<ResponseListDto<GroupDto>> apiMessageDto = new ApiMessageDto<>();
        Page<Group> groups;
        if (isSuperAdmin()) {
            groups = groupRepository
                    .findAll(groupCriteria.getSpecification(), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.DESC, "createdDate"))));
        } else {
            groupCriteria.setIsSystemRole(isSuperAdmin());
            groups = groupRepository
                    .findAll(groupCriteria.getSpecification(), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.DESC, "createdDate"))));
        }
        ResponseListDto<GroupDto> responseListDto = new ResponseListDto(groupMapper.fromEntityToGroupDtoAutoCompleteList(groups.getContent()), groups.getTotalElements(), groups.getTotalPages());
        apiMessageDto.setData(responseListDto);
        apiMessageDto.setMessage("Auto complete group success.");
        return apiMessageDto;
    }
}
