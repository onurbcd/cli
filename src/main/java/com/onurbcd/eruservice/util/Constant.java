package com.onurbcd.eruservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constant {

    public static final String BALANCE_DOCUMENT_PATH = "balance/";
    public static final String BALANCE_DOCUMENT_PATH_PATTERN = "yyyy/MM/dd";
    public static final String BOGUS_NAME = "bogus";
    public static final String BILL_DOCUMENT_PATH = "bill/";
    public static final String YEAR_PATTERN = "yyyy";
    public static final String BILL_CLOSE_CODE = "created by bill close";
    public static final String MONTH_YEAR_PATTERN = "MM/yyyy";
    public static final String LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String NAME = "name";
    public static final String NAME_LABEL = "* Name:";
    public static final String PARENT_ID = "parentId";
    public static final String PARENT_ID_LABEL = "* Parent:";
    public static final String DESCRIPTION = "description";
    public static final String DESCRIPTION_LABEL = "Description:";
    public static final String PATH = "path";
    public static final String PATH_LABEL = "* Path:";
    public static final String CATEGORY_ID = "categoryId";
    public static final String CATEGORY_ID_LABEL = "* Category:";
    public static final String DOCUMENTS = "documents";

    public static final String REGEXP_URL = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    public static final String POSITIVE_AMOUNT_MIN = "0.0001";
    public static final String AMOUNT_MIN = "-999999999999999";
    public static final String AMOUNT_MAX = "999999999999999";
    public static final String REGEXP_PATH = "^[a-z|A-Z|0-9|-]+$";
}
