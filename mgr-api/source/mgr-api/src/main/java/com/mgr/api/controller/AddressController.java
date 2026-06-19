package com.mgr.api.controller;

import com.mgr.api.constant.MgrConstant;
import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ErrorCode;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.address.AddressDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.exception.NotFoundException;
import com.mgr.api.form.address.CreateAddressForm;
import com.mgr.api.form.address.UpdateAddressForm;
import com.mgr.api.mapper.AddressMapper;
import com.mgr.api.model.Address;
import com.mgr.api.model.Nation;
import com.mgr.api.model.User;
import com.mgr.api.model.criteria.AddressCriteria;
import com.mgr.api.repository.AddressRepository;
import com.mgr.api.repository.NationRepository;
import com.mgr.api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@RequestMapping("/v1/address")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class AddressController extends ABasicController {
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final UserRepository userRepository;
    private final NationRepository nationRepository;

    public AddressController(AddressRepository addressRepository, AddressMapper addressMapper, UserRepository userRepository, NationRepository nationRepository) {
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
        this.userRepository = userRepository;
        this.nationRepository = nationRepository;
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADDR_V')")
    public ApiMessageDto<AddressDto> getAddress(@PathVariable("id") Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nation not found", ErrorCode.ADDRESS_ERROR_NOT_FOUND));
        return makeSuccessResponse(addressMapper.fromEntityToAddressDto(address), "Get address success");
    }

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADDR_C')")
    public ApiMessageDto<String> createAddress(@Valid @RequestBody CreateAddressForm createAddressForm, BindingResult bindingResult) {
        Long userId = getCurrentUser();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_ERROR_NOT_FOUND));
        Address address = addressMapper.fromCreateAddressFormToEntity(createAddressForm);
        address.setUser(user);
        address.setStatus(MgrConstant.STATUS_ACTIVE);
        validateAndSetNationHierarchy(address, createAddressForm.getProvinceId(), createAddressForm.getDistrictId(), createAddressForm.getCommuneId());
        //Logic is default
        long addressCount = addressRepository.countByUserId(getCurrentUser());
        if (addressCount == 0) {
            address.setIsDefault(true);
        } else if (createAddressForm.getIsDefault()) {
            addressRepository.resetDefaultAddressByUserId(userId);
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }
        addressRepository.save(address);
        return makeSuccessResponse("Create address success");
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADDR_U')")
    public ApiMessageDto<String> updateAddress(@Valid @RequestBody UpdateAddressForm updateAddressForm, BindingResult bindingResult) {
        Long userId = getCurrentUser();
        Address address = addressRepository.findById(updateAddressForm.getId())
                .orElseThrow(() -> new NotFoundException("Address not found", ErrorCode.ADDRESS_ERROR_NOT_FOUND));
        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have permission to edit this address", ErrorCode.ADDRESS_ERROR_UNABLE_UPDATE);
        }
        if (StringUtils.isNotBlank(updateAddressForm.getStreet())) {
            address.setStreet(updateAddressForm.getStreet());
        }
        if (StringUtils.isNotBlank(updateAddressForm.getZipCode())) {
            address.setZipCode(updateAddressForm.getZipCode());
        }
        Long provinceId = updateAddressForm.getProvinceId() != null ? updateAddressForm.getProvinceId() : address.getProvince().getId();
        Long districtId = updateAddressForm.getDistrictId() != null ? updateAddressForm.getDistrictId() : address.getDistrict().getId();
        Long communeId = updateAddressForm.getCommuneId();
        if (communeId == null && address.getCommune() != null) {
            communeId = address.getCommune().getId();
        }
        validateAndSetNationHierarchy(address, provinceId, districtId, communeId);
        if (updateAddressForm.getIsDefault() != null) {
            if (updateAddressForm.getIsDefault() && !address.getIsDefault()) {
                addressRepository.resetDefaultAddressByUserId(userId);
                address.setIsDefault(true);
            } else if (!updateAddressForm.getIsDefault() && address.getIsDefault()) {
                throw new BadRequestException("Cannot unset default address directly. Please set another address as default.");
            }
        }
        addressRepository.save(address);
        return makeSuccessResponse("Update address success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADDR_L')")
    public ApiMessageDto<ResponseListDto<AddressDto>> listAddress(AddressCriteria criteria, Pageable pageable) {
        Page<Address> page = addressRepository.findAll(criteria.getSpecification(), pageable);
        ResponseListDto<AddressDto> responseListDto = new ResponseListDto(addressMapper.fromEntityToAddressDtoList(page.getContent()),
                page.getTotalElements(),
                page.getTotalPages()
        );
        return makeSuccessResponse(responseListDto, "Get address list success");
    }

    @GetMapping(value = "/my-addresses", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADDR_M')")
    public ApiMessageDto<List<AddressDto>> getMyAddresses() {
        Long currentUserId = getCurrentUser();
        List<Address> addresses = addressRepository.findAllByUserIdOrderByIsDefaultDesc(currentUserId);
        return makeSuccessResponse(addressMapper.fromEntityToAddressDtoList(addresses), "Get my addresses success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADDR_D')")
    public ApiMessageDto<String> deleteAddress(@PathVariable("id") Long id) {
        Long currentUserId = getCurrentUser();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Address not found", ErrorCode.ADDRESS_ERROR_NOT_FOUND));
        if (!address.getUser().getId().equals(currentUserId)) {
            throw new BadRequestException("You don't have permission to delete this address");
        }
        if (address.getIsDefault()) {
            throw new BadRequestException("Cannot delete default address. Please set another address as default first.");
        }
        addressRepository.delete(address);
        return makeSuccessResponse("Delete address success");
    }

    private void validateAndSetNationHierarchy(Address address, Long provinceId, Long districtId, Long communeId) {
        Nation province = nationRepository.findById(provinceId)
                .orElseThrow(() -> new NotFoundException("Province not found", ErrorCode.NATION_ERROR_NOT_FOUND));
        if (!province.getKind().equals(MgrConstant.NATION_TYPE_PROVINCE)) {
            throw new BadRequestException("Invalid Province Id", ErrorCode.NATION_ERROR_INVALID);
        }
        Nation district = nationRepository.findById(districtId)
                .orElseThrow(() -> new NotFoundException("District not found", ErrorCode.NATION_ERROR_NOT_FOUND));
        if (!district.getKind().equals(MgrConstant.NATION_TYPE_DISTRICT) || !district.getParent().getId().equals(province.getId())) {
            throw new BadRequestException("Invalid District Id", ErrorCode.NATION_ERROR_INVALID);
        }
        address.setProvince(province);
        address.setDistrict(district);
        if (communeId != null) {
            Nation commune = nationRepository.findById(communeId)
                    .orElseThrow(() -> new NotFoundException("Commune not found", ErrorCode.NATION_ERROR_NOT_FOUND));
            if (!commune.getKind().equals(MgrConstant.NATION_TYPE_COMMUNE) || !commune.getParent().getId().equals(district.getId())) {
                throw new BadRequestException("Invalid Commune Id", ErrorCode.NATION_ERROR_INVALID);
            }
            address.setCommune(commune);
        }
        else {
            address.setCommune(null);
        }
    }
}
