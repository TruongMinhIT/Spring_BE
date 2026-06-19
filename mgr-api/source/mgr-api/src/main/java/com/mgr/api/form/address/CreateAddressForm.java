package com.mgr.api.form.address;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class CreateAddressForm {
    @NotBlank(message = "street cannot be empty")
    @ApiModelProperty(name = "street", required = true)
    private String street;

    @ApiModelProperty(name = "zipCode")
    private String zipCode;

    @NotNull(message = "isDefault cannot null")
    @ApiModelProperty(name = "isDefault", required = true)
    private Boolean isDefault;

    @NotNull(message = "ProvinceId is required")
    @ApiModelProperty(name = "provinceId", required = true)
    private Long provinceId;

    @NotNull(message = "District is requiredId")
    @ApiModelProperty(name = "districtId", required = true)
    private Long districtId;

    @ApiModelProperty(name = "communeId")
    private Long communeId;
}
