package com.mgr.api.dto;

public class ErrorCode {
    /**
     * General error code
     */
    public static final String GENERAL_ERROR_INVALID_USERNAME_OR_PASSWORD = "ERROR-GENERAL-0000";

    /**
     * Starting error code Account
     */
    public static final String ACCOUNT_ERROR_NOT_FOUND = "ERROR-ACCOUNT-0000";
    public static final String ACCOUNT_ERROR_USERNAME_EXISTED = "ERROR-ACCOUNT-0001";
    public static final String ACCOUNT_ERROR_WRONG_PASSWORD = "ERROR-ACCOUNT-0002";
    public static final String ACCOUNT_ERROR_UNABLE_CREATE = "ERROR-ACCOUNT-0003";
    public static final String ACCOUNT_ERROR_UNABLE_UPDATE = "ERROR-ACCOUNT-0004";
    public static final String ACCOUNT_ERROR_UNABLE_DELETE = "ERROR-ACCOUNT-0005";

    /**
     * Starting error code DATABASE_ERROR
     */
    public static final String ERROR_DB_QUERY = "ERROR-DB-QUERY-0000";

    /**
     * Permission error code
     */
    public static final String PERMISSION_ERROR_NOT_FOUND = "ERROR-PERMISSION-0000";
    public static final String PERMISSION_ERROR_NAME_EXISTED = "ERROR-PERMISSION-0001";
    public static final String PERMISSION_ERROR_CODE_EXISTED = "ERROR-PERMISSION-0002";

    /**
     * Group error code
     */
    public static final String GROUP_ERROR_NOT_FOUND = "ERROR-GROUP-0000";
    public static final String GROUP_ERROR_NAME_EXISTED = "ERROR-GROUP-0001";
}
