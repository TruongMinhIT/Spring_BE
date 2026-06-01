package com.mgr.api.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.exception.UnauthorizationException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class CustomFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();
    private ObjectMapper objectMapper;

    public CustomFeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String s, Response response) {
        try {
            String errorMessage = "Lỗi gọi API nội bộ";
            String errorType = "";

            if (response.body() != null) {
                InputStream inputStream = response.body().asInputStream();
                String responseBody = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

                JsonNode jsonNode = objectMapper.readTree(responseBody);

                if (jsonNode.has("error")) {
                    errorType = jsonNode.get("error").asText();
                }
                if (jsonNode.has("error_description")) {
                    errorMessage = jsonNode.get("error_description").asText();
                } else if (jsonNode.has("message")) {
                    errorMessage = jsonNode.get("message").asText();
                }
            }

            if ("invalid_grant".equalsIgnoreCase(errorType) || "Bad credentials".equalsIgnoreCase(errorMessage)) {
                return new UnauthorizationException("Tài khoản hoặc mật khẩu không đúng");
            }

            switch (response.status()) {
                case 400:
                    return new BadRequestException(errorMessage);
                case 401:
                    return new UnauthorizationException("Phiên làm việc đã hết hạn hoặc Token không hợp lệ. Vui lòng đăng nhập lại.");
                case 403:
                    return new RuntimeException("Bạn không có quyền truy cập vào chức năng này");
                case 404:
                    return new RuntimeException("Không tìm thấy API nội bộ: " + errorMessage);
                default:
                    return defaultErrorDecoder.decode(s, response);
            }
        } catch (Exception e) {
            log.error("Lỗi khi parse exception của Feign Client: ", e);
            return defaultErrorDecoder.decode(s, response);
        }
    }
}