package com.mgr.api.mapper;

import com.mgr.api.dto.address.AddressDto;
import com.mgr.api.form.address.CreateAddressForm;
import com.mgr.api.model.Address;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {NationMapper.class})
public interface AddressMapper {
    @Mapping(source = "street", target = "street")
    @Mapping(source = "zipCode", target = "zipCode")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateAddressFormToEntity")
    Address fromCreateAddressFormToEntity(CreateAddressForm createAddressForm);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "street", target = "street")
    @Mapping(source = "zipCode", target = "zipCode")
    @Mapping(source = "isDefault", target = "isDefault")
    @Mapping(source = "province", target = "province", qualifiedByName = "fromEntityToNationDto")
    @Mapping(source = "district", target = "district", qualifiedByName = "fromEntityToNationDto")
    @Mapping(source = "commune", target = "commune", qualifiedByName = "fromEntityToNationDto")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToAddressDto")
    AddressDto fromEntityToAddressDto(Address address);

    @IterableMapping(elementTargetType = AddressDto.class, qualifiedByName = "fromEntityToAddressDto")
    List<AddressDto> fromEntityToAddressDtoList(List<Address> addresses);
}
