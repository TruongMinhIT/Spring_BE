package com.mgr.api.form.fileUpload;

import com.mgr.api.validation.ValidFileUploadKind;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@ApiModel
public class CreateFileUploadForm {
    @ValidFileUploadKind
    @ApiModelProperty(name = "kind", required = true, notes = "1: Avatar, 2:Logo, 3:Thumbnail")
    private Integer kind;

    @ApiModelProperty(name = "file", required = true)
    private MultipartFile file;
}
