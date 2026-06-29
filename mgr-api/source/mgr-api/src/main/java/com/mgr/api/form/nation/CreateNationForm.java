package com.mgr.api.form.nation;

import com.mgr.api.validation.ValidNationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class CreateNationForm {
    @NotBlank(message = "name cannot empty")
    @ApiModelProperty(name = "name", required = true)
    private String name;

    @ValidNationType
    @ApiModelProperty(name = "kind", required = true, notes = "1: Province, 2:District, 3:Commune")
    private Integer kind;

    @ApiModelProperty(name = "parentId")
    private Long parentId;
}
