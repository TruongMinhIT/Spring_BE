package com.mgr.api.controller;

import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ErrorCode;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.dbConfig.DbConfigDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.exception.NotFoundException;
import com.mgr.api.form.dbConfig.CreateDbConfigForm;
import com.mgr.api.form.dbConfig.UpdateDbConfigForm;
import com.mgr.api.mapper.DbConfigMapper;
import com.mgr.api.model.DbConfig;
import com.mgr.api.model.User;
import com.mgr.api.model.criteria.DbConfigCriteria;
import com.mgr.api.repository.master.DbConfigRepository;
import com.mgr.api.repository.master.UserRepository;
import com.mgr.api.service.DbConfigService;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("v1/db-config")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class DbConfigController extends ABasicController {
    @Autowired
    private DbConfigRepository dbConfigRepository;

    @Autowired
    private DbConfigMapper dbConfigMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DbConfigService dbConfigService;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DBC_V')")
    public ApiMessageDto<DbConfigDto> getDbConfig(@PathVariable("id") Long id) {
        DbConfig dbConfig = dbConfigRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("DbConfig not found", ErrorCode.DB_CONFIG_ERROR_NOT_FOUND));
        return makeSuccessResponse(dbConfigMapper.fromEntityToDbConfigDto(dbConfig), "Get dbConfig success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DBC_C')")
    public ApiMessageDto<String> createTenant(@Valid @RequestBody CreateDbConfigForm createDbConfigForm,
            BindingResult bindingResult) {
        if (dbConfigRepository.existsByName(createDbConfigForm.getName())) {
            throw new BadRequestException("Name (tenantId) is already existed", ErrorCode.DB_CONFIG_ERROR_NAME_EXISTED);
        }
        Long userId = getCurrentUser();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_ERROR_NOT_FOUND));
        DbConfig dbConfig = dbConfigMapper.fromCreateDbConfigFormToEntity(createDbConfigForm);
        dbConfig.setUser(user);
        dbConfigRepository.save(dbConfig);
        try {
            dbConfigService.provisionTenantDatabase(dbConfig);
        } catch (Exception exp) {
            log.error("Error create DB ", exp);
            throw new RuntimeException("Cannot create physical DB for this tenant");
        }
        return makeSuccessResponse("Create tenant and create DB successfully");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DBC_U')")
    public ApiMessageDto<String> updateTenant(@Valid @RequestBody UpdateDbConfigForm updateDbConfigForm,
            BindingResult bindingResult) {
        DbConfig dbConfig = dbConfigRepository.findById(updateDbConfigForm.getId())
                .orElseThrow(() -> new NotFoundException("DbConfig not found", ErrorCode.DB_CONFIG_ERROR_NOT_FOUND));
        dbConfigMapper.fromUpdateDbConfigFormToEntity(updateDbConfigForm, dbConfig);
        dbConfigRepository.save(dbConfig);
        try {
            dbConfigService.updateDataSource(dbConfig);
        } catch (Exception exp) {
            log.error("Error update DataSource ", exp);
            throw new RuntimeException("Cannot update Tenant");
        }
        return makeSuccessResponse("Update tenant success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DBC_L')")
    public ApiMessageDto<ResponseListDto<List<DbConfigDto>>> listTenant(DbConfigCriteria dbConfigCriteria,
            Pageable pageable) {
        Page<DbConfig> page = dbConfigRepository.findAll(dbConfigCriteria.getSpecification(), pageable);
        ResponseListDto<List<DbConfigDto>> responseListDto = makeResponseListDto(page,
                dbConfigMapper::fromEntityToDbConfigDtoList);
        return makeSuccessResponse(responseListDto, "Tenat list success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DBC_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        DbConfig dbConfig = dbConfigRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant not found", ErrorCode.DB_CONFIG_ERROR_NOT_FOUND));
        dbConfigService.removeDataSource(dbConfig.getName());
        dbConfigRepository.delete(dbConfig);
        return makeSuccessResponse("Remove tenant success");
    }
}
