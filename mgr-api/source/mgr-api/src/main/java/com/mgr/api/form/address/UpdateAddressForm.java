package com.mgr.api.form.address;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class UpdateAddressForm {
    @NotNull(message = "id cannot null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;

    @ApiModelProperty(name = "street", required = true)
    private String street;

    @ApiModelProperty(name = "zipCode")
    private String zipCode;

    @ApiModelProperty(name = "isDefault", required = true)
    private Boolean isDefault;

    @ApiModelProperty(name = "provinceId", required = true)
    private Long provinceId;

    @ApiModelProperty(name = "districtId", required = true)
    private Long districtId;

    @ApiModelProperty(name = "communeId")
    private Long communeId;
}
