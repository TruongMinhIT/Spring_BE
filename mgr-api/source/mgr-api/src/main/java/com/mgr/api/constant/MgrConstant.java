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

    public static final int POST_TYPE_SALE = 1;
    public static final int POST_TYPE_BUY = 2;

    public static final int POST_CONDITION_STATUS_BRAND_NEW = 1;
    public static final int POST_CONDITION_STATUS_USED = 2;

    public static final int NATION_TYPE_PROVINCE = 1;
    public static final int NATION_TYPE_DISTRICT = 2;
    public static final int NATION_TYPE_COMMUNE = 3;

    public static final int UPLOAD_FILE_AVATAR = 1;
    public static final int UPLOAD_FILE_LOGO = 2;
    public static final int UPLOAD_FILE_IMAGE_THUMBNAIL = 3;


    public static final String UPLOAD_FILE_AVATAR_STR = "AVATAR";
    public static final String UPLOAD_FILE_LOGO_STR = "LOGO";
    public static final String UPLOAD_FILE_IMAGE_THUMBNAIL_STR = "THUMBNAIL";

    private MgrConstant() {
        throw new IllegalStateException("Utility class");
    }
}
