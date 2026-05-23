package com.mgr.api.constant;

public class MgrConstant {
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    public static final Integer USER_KIND_ADMIN = 1;
    public static final Integer USER_KIND_USER = 2;

    public static final Integer GROUP_KIND_USER = 2;

    public static final Integer STATUS_ACTIVE = 1;
    public static final Integer STATUS_PENDING = 0;
    public static final Integer STATUS_LOCK = -1;
    public static final Integer STATUS_DELETE = -2;

    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;
    public static final int GENDER_OTHER = 3;
    private MgrConstant() {
        throw new IllegalStateException("Utility class");
    }
}
