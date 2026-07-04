package com.mgr.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Projection / row DTO for the user CSV export. This is the only shape that crosses the
 * service -> CSV boundary; the {@code User}/{@code Account} entities never do. It carries only
 * non-sensitive columns (FR-005) selected directly by a JPA constructor expression, so
 * password/token/2FA fields are never loaded.
 */
@Getter
@Setter
@AllArgsConstructor
public class UserExportRow {
    private String fullName;   // Ho ten
    private String email;      // Email
    private String roleName;   // Vai tro (group name, may be null)
    private int status;        // raw Account.status, rendered via UserExportStatus
    private Date createdDate;  // Ngay tao, formatted yyyy-MM-dd
}
