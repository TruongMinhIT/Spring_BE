package com.mgr.api.constant;

import java.util.Optional;

/**
 * Two-value account status used both for the export request filter and the rendered CSV value.
 * Maps between the request keyword ({@code active}/{@code locked}), the numeric DB status code
 * ({@link MgrConstant#STATUS_ACTIVE} / {@link MgrConstant#STATUS_LOCK}) and the display label.
 */
public enum UserExportStatus {
    ACTIVE("active", MgrConstant.STATUS_ACTIVE),
    LOCKED("locked", MgrConstant.STATUS_LOCK);

    private final String requestValue;
    private final int dbCode;

    UserExportStatus(String requestValue, int dbCode) {
        this.requestValue = requestValue;
        this.dbCode = dbCode;
    }

    /**
     * Resolve a request keyword (case-insensitive) to a status. Returns empty when the input is
     * blank (meaning "no filter"); the caller distinguishes an unknown, non-blank value below.
     */
    public static Optional<UserExportStatus> fromRequestValue(String requestValue) {
        if (requestValue == null || requestValue.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalized = requestValue.trim().toLowerCase();
        for (UserExportStatus status : values()) {
            if (status.requestValue.equals(normalized)) {
                return Optional.of(status);
            }
        }
        throw new IllegalArgumentException("Invalid status '" + requestValue
                + "'. Allowed values: active, locked.");
    }

    /** Render a raw DB status code to its display label, defaulting non-mapped codes to the nearest of the two. */
    public static String displayValue(int dbCode) {
        return dbCode == LOCKED.dbCode ? LOCKED.requestValue : ACTIVE.requestValue;
    }

    public int toDbCode() {
        return dbCode;
    }

    public String displayValue() {
        return requestValue;
    }
}
