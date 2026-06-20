package com.mgr.api.controller;

import com.mgr.api.constant.MgrConstant;
import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ErrorCode;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.nation.NationDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.exception.NotFoundException;
import com.mgr.api.form.nation.CreateNationForm;
import com.mgr.api.form.nation.UpdateNationForm;
import com.mgr.api.mapper.NationMapper;
import com.mgr.api.model.Nation;
import com.mgr.api.model.criteria.NationCriteria;
import com.mgr.api.repository.AddressRepository;
import com.mgr.api.repository.NationRepository;
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
@RequestMapping("/v1/nation")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class NationController extends ABasicController {
    @Autowired
    private NationRepository nationRepository;

    @Autowired
    private NationMapper nationMapper;

    @Autowired
    private AddressRepository addressRepository;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NAT_V')")
    public ApiMessageDto<NationDto> getNation(@PathVariable("id") Long id) {
        Nation nation = nationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nation not found", ErrorCode.NATION_ERROR_NOT_FOUND));
        return makeSuccessResponse(nationMapper.fromEntityToNationDto(nation), "Get nation success");
    }

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NAT_C')")
    public ApiMessageDto<String> createNation(@Valid @RequestBody CreateNationForm createNationForm, BindingResult bindingResult) {
        Nation parent = null;
        if (createNationForm.getKind().equals(MgrConstant.NATION_TYPE_PROVINCE)) {
            if (createNationForm.getParentId() != null) {
                throw new BadRequestException("Province is highest level, it haven't parent", ErrorCode.NATION_ERROR_INVALID_PARENT);
            }
        } else {
            if (createNationForm.getParentId() == null) {
                throw new BadRequestException("This kind must have a parent", ErrorCode.NATION_ERROR_INVALID_PARENT);
            }
            parent = nationRepository.findById(createNationForm.getParentId())
                    .orElseThrow(() -> new NotFoundException("ParentId Not Found", ErrorCode.NATION_ERROR_INVALID_PARENT));
            if (createNationForm.getKind() != parent.getKind() + 1) {
                throw new BadRequestException("Kind does not match selected level", ErrorCode.NATION_ERROR_INVALID_PARENT);
            }
        }
        boolean isDuplicate;
        if (parent == null) {
            isDuplicate = nationRepository.existsByNameAndParentIsNull(createNationForm.getName());
        } else {
            isDuplicate = nationRepository.existsByNameAndParentId(createNationForm.getName(), parent.getId());
        }
        if (isDuplicate) {
            throw new BadRequestException("Name already exist in this region", ErrorCode.NATION_ERROR_NAME_EXISTED);
        }
        Nation nation = nationMapper.fromCreateNationFormToEntity(createNationForm);
        nation.setParent(parent);
        nationRepository.save(nation);
        return makeSuccessResponse("Create nation success");
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NAT_U')")
    public ApiMessageDto<String> updateNation(@Valid @RequestBody UpdateNationForm updateNationForm, BindingResult bindingResult) {
        Nation nation = nationRepository.findById(updateNationForm.getId())
                .orElseThrow(() -> new NotFoundException("Nation not found", ErrorCode.NATION_ERROR_NOT_FOUND));
        String targetName;
        if (StringUtils.isNotBlank(updateNationForm.getName())) {
            targetName = updateNationForm.getName().trim();
        } else {
            targetName = nation.getName();
        }
        Integer targetKind;
        if (updateNationForm.getKind() != null) {
            targetKind = updateNationForm.getKind();
        } else {
            targetKind = nation.getKind();
        }
        Nation parent = null;
        if (targetKind == MgrConstant.NATION_TYPE_PROVINCE) {
            if (updateNationForm.getParentId() != null) {
                throw new BadRequestException("Province cannot have a parent unit", ErrorCode.NATION_ERROR_INVALID_PARENT);
            }
        } else {
            if (updateNationForm.getParentId() == null && nation.getParent() == null) {
                throw new BadRequestException("The current rank requires a parent unit", ErrorCode.NATION_ERROR_INVALID_PARENT);
            }
            if (updateNationForm.getParentId() != null) {
                if (updateNationForm.getId().equals(updateNationForm.getParentId())) {
                    throw new BadRequestException("Nation cannot be its own parent", ErrorCode.NATION_ERROR_INVALID_PARENT);
                }
                parent = nationRepository.findById(updateNationForm.getParentId())
                        .orElseThrow(() -> new NotFoundException("Parent unit not found", ErrorCode.NATION_ERROR_INVALID_PARENT));
            } else {
                parent = nation.getParent();
            }
            if (targetKind != parent.getKind() + 1) {
                throw new BadRequestException("Kind does not match selected level", ErrorCode.NATION_ERROR_INVALID_PARENT);
            }
        }
        boolean isDuplicate;
        if (parent == null) {
            isDuplicate = nationRepository.existsByNameAndParentIsNullAndIdIsNot(targetName, nation.getId());
        } else {
            isDuplicate = nationRepository.existsByNameAndParentIdAndIdIsNot(targetName, parent.getId(), nation.getId());
        }
        if (isDuplicate) {
            throw new BadRequestException("Name already exists in this region", ErrorCode.NATION_ERROR_NAME_EXISTED);
        }
        nation.setName(targetName);
        nation.setKind(targetKind);
        nation.setParent(parent);
        nationRepository.save(nation);
        return makeSuccessResponse("Update nation success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NAT_L')")
    public ApiMessageDto<ResponseListDto<List<NationDto>>> listNation(NationCriteria nationCriteria, Pageable pageable) {
        Page<Nation> page = nationRepository.findAll(nationCriteria.getSpecification(), pageable);
        ResponseListDto<List<NationDto>> responseListDto = makeResponseListDto(page, nationMapper::fromEntityToNationDtoList);
        return makeSuccessResponse(responseListDto, "Nation list success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NAT_D')")
    public ApiMessageDto<String> deleteNation(@PathVariable("id") Long id) {
        Nation nation = nationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nation not found", ErrorCode.NATION_ERROR_NOT_FOUND));
        boolean isUseInAddress = addressRepository.existsByProvinceIdOrDistrictIdOrCommuneId(id, id, id);
        if (isUseInAddress) {
            throw new BadRequestException("Already have address use this nation!!", ErrorCode.NATION_ERROR_UNABLE_DELETE);
        }
        if (nation.getKind().equals(MgrConstant.NATION_TYPE_PROVINCE)) {
            nationRepository.deleteCommunesByProvinceId(id);
            nationRepository.deleteByParentId(id);
        } else if (nation.getKind().equals(MgrConstant.NATION_TYPE_DISTRICT)) {
            nationRepository.deleteByParentId(id);
        }
        nationRepository.deleteById(id);
        return makeSuccessResponse("Delete nation success.");
    }
}
