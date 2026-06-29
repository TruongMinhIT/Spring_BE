package com.mgr.api.controller;

import com.mgr.api.constant.MgrConstant;
import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.form.fileUpload.CreateFileUploadForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("v1/file")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class FileUploadController extends ABasicController {
    @Value("${upload.dir}")
    private String uploadDir;

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiMessageDto<String> uploadFile(@Valid @ModelAttribute CreateFileUploadForm form) {
        MultipartFile file = form.getFile();

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String kindStr;
        if (form.getKind() == MgrConstant.UPLOAD_FILE_AVATAR) {
            kindStr = MgrConstant.UPLOAD_FILE_AVATAR_STR;
        } else {
            kindStr = MgrConstant.UPLOAD_FILE_LOGO_STR;
        }

        String randomStr = RandomStringUtils.randomNumeric(10).toUpperCase();
        String newFileName = kindStr + "_" + randomStr + extension;

        try {
            Path targetLocation = Paths.get(uploadDir, "general", kindStr);
            Files.createDirectories(targetLocation);

            Path filePath = targetLocation.resolve(newFileName);
            file.transferTo(filePath.toFile());

            String resultPath = "/" + kindStr + "/" + newFileName;
            return makeSuccessResponse(resultPath, "Upload file success");
        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) {
        try {
            Path filePath = Paths.get(uploadDir, "general", path);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
