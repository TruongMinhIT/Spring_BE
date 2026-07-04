package com.mgr.api.service;

import com.mgr.api.constant.UserExportStatus;

import java.io.OutputStream;

/**
 * Business contract for streaming the current tenant's users as a CSV file. Resolves the optional
 * status filter, fetches the non-sensitive projection, and writes the CSV (UTF-8 BOM, Vietnamese
 * headers, yyyy-MM-dd dates) to the given output stream.
 */
public interface UserExportService {

    /**
     * Write the users of the current tenant as CSV to {@code out}.
     *
     * @param statusFilter optional account-status filter; {@code null} exports all users (FR-007)
     * @param out          the response output stream to stream CSV bytes into
     */
    void writeUsersCsv(UserExportStatus statusFilter, OutputStream out);
}
