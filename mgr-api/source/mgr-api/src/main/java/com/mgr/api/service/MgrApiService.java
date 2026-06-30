package com.mgr.api.service;

import com.mgr.api.dto.ErrorCode;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.model.Permission;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MgrApiService {
    @Autowired
    private CommonAsyncService commonAsyncService;

    private Map<String, Long> storeQRCodeRandom = new ConcurrentHashMap<>();

    @Value("${upload.dir}")
    private String uploadDir;

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }
        try {
            String relativePath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
            Path basePath = Paths.get(uploadDir, "general").toAbsolutePath().normalize();
            Path targetPath = basePath.resolve(relativePath).normalize();
            if (!targetPath.startsWith(basePath)) {
                log.warn("Cảnh báo: Cố tình xóa file ngoài thư mục: {}", targetPath);
                return;
            }
            boolean deleted = Files.deleteIfExists(targetPath);
            if (deleted) {
                log.info("Đã xóa file cũ thành công: {}", targetPath);
            }
        } catch (Exception e) {
            log.error("Không thể xóa file vật lý: {}", filePath, e);
        }
    }

    public void deleteFiles(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            return;
        }
        try {
            Path basePath = Paths.get(uploadDir, "general").toAbsolutePath().normalize();
            int deletedCount = 0;

            for (String filePath : filePaths) {
                if (StringUtils.isBlank(filePath)) continue;
                try {
                    String relativePath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
                    Path targetPath = basePath.resolve(relativePath).normalize();

                    if (targetPath.startsWith(basePath)) {
                        if (Files.deleteIfExists(targetPath)) {
                            deletedCount++;
                        }
                    }
                } catch (Exception e) {
                    log.error("Không thể xóa file: {}", filePath, e);
                }
            }
            log.info("Đã dọn dẹp thành công {}/{} file rác.", deletedCount, filePaths.size());

        } catch (Exception e) {
            log.error("Lỗi cấu hình hệ thống: ", e);
        }
    }

    public void validateFile(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return;
        }
        try {
            String relativePath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
            Path basePath = Paths.get(uploadDir, "general").toAbsolutePath().normalize();
            Path targetPath = basePath.resolve(relativePath).normalize();

            if (!targetPath.startsWith(basePath)) {
                throw new BadRequestException("Đường dẫn file không hợp lệ", ErrorCode.FILE_ERROR_INVALID_PATH);
            }

            Resource resource = new UrlResource(targetPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BadRequestException("File ảnh không tồn tại hoặc không thể đọc", ErrorCode.FILE_ERROR_NOT_FOUND);
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Lỗi hệ thống khi xác minh file", ErrorCode.FILE_ERROR_NOT_FOUND);
        }
    }

    public void sendEmail(String email, String msg, String subject, boolean html) {
        commonAsyncService.sendEmail(email, msg, subject, html);
    }


    public String convertGroupToUri(List<Permission> permissions) {
        if (permissions != null) {
            StringBuilder builderPermission = new StringBuilder();
            for (Permission p : permissions) {
                builderPermission.append(p.getAction().trim().replace("/v1", "") + ",");
            }
            return builderPermission.toString();
        }
        return null;
    }

    public synchronized boolean checkCodeValid(String code) {
        //delelete key has valule > 60s
        Set<String> keys = storeQRCodeRandom.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Long value = storeQRCodeRandom.get(key);
            if ((System.currentTimeMillis() - value) > 60000) {
                storeQRCodeRandom.remove(key);
            }
        }

        if (storeQRCodeRandom.containsKey(code)) {
            return false;
        }
        storeQRCodeRandom.put(code, System.currentTimeMillis());
        return true;
    }
}
