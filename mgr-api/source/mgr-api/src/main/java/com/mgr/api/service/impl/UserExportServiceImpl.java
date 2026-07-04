package com.mgr.api.service.impl;

import com.mgr.api.constant.UserExportStatus;
import com.mgr.api.dto.user.UserExportRow;
import com.mgr.api.repository.tenant.UserRepository;
import com.mgr.api.service.UserExportService;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
@Service
public class UserExportServiceImpl implements UserExportService {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    // Vietnamese header labels, fixed order (FR-004).
    private static final String[] HEADER = {"Họ tên", "Email", "Vai trò", "Trạng thái", "Ngày tạo"};
    // UTF-8 byte order mark so Excel renders Vietnamese diacritics correctly (FR-013).
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    @Autowired
    private UserRepository userRepository;

    @Override
    public void writeUsersCsv(UserExportStatus statusFilter, OutputStream out) {
        List<UserExportRow> rows = statusFilter == null
                ? userRepository.findAllForExport()
                : userRepository.findAllForExportByStatus(statusFilter.toDbCode());

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        try {
            // BOM must be written to the raw stream before the character writer.
            out.write(UTF8_BOM);
            OutputStreamWriter streamWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            CSVWriter csvWriter = new CSVWriter(streamWriter);
            csvWriter.writeNext(HEADER);
            for (UserExportRow row : rows) {
                csvWriter.writeNext(new String[]{
                        row.getFullName(),
                        row.getEmail(),
                        row.getRoleName() != null ? row.getRoleName() : "",
                        UserExportStatus.displayValue(row.getStatus()),
                        row.getCreatedDate() != null ? dateFormat.format(row.getCreatedDate()) : ""
                });
            }
            csvWriter.flush();
        } catch (IOException e) {
            log.error("Failed to write user CSV export: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to write user CSV export", e);
        }
    }
}
