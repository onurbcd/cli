package com.onurbcd.eruservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constant {

    /*
     * MISCELLANEOUS
     */
    public static final String BOGUS_NAME = "bogus";

    /*
     * HEADERS
     */
    public static final String DESCRIPTION_HEADER = "Description";

    /*
     * MESSAGES
     */
    public static final String BILL_CLOSE_CODE = "created by bill close";
    public static final String OPERATION_CANCELLED = "Operation cancelled";

    /*
     * PATHS
     */
    public static final String BALANCE_DOCUMENT_PATH = "balance/";
    public static final String BILL_DOCUMENT_PATH = "bill/";

    /*
     * DATE PATTERNS
     */
    public static final String BALANCE_DOCUMENT_PATH_PATTERN = "yyyy/MM/dd";
    public static final String YEAR_PATTERN = "yyyy";
    public static final String MONTH_YEAR_PATTERN = "MM/yyyy";
    public static final String LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /*
     * VALIDATION
     */
    public static final String POSITIVE_AMOUNT_MIN = "0.0001";
    public static final String AMOUNT_MIN = "-999999999999999";
    public static final String AMOUNT_MAX = "999999999999999";

    /*
     * REGEX
     */
    public static final String REGEXP_URL = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    public static final String REGEXP_PATH = "^[a-z|A-Z|0-9|-]+$";

    /*
     * FLOW FIELDS
     */
    public static final String NAME_ID = "name";
    public static final String PARENT_ID_ID = "parentId";
    public static final String DESCRIPTION_ID = "description";
    public static final String PATH_ID = "path";
    public static final String CATEGORY_ID_ID = "categoryId";
    public static final String DOCUMENTS_ID = "documents";
    public static final String DOCUMENTS_IDS_ID = "documentsIds";
    public static final String INCOME_SOURCE_ID_ID = "incomeSourceId";
    public static final String SOURCE_TYPE_ID = "sourceType";
    public static final String CURRENCY_TYPE_ID = "currencyType";
    public static final String BALANCE_ID = "balance";
    public static final String DAY_ID = "day";
    public static final String SOURCE_ID = "source";
    public static final String CATEGORY_ID = "category";
    public static final String AMOUNT_ID = "amount";
    public static final String CODE_ID = "code";
    public static final String BALANCE_TYPE_ID = "balanceType";

    /*
     * FLOW LABELS
     */
    public static final String NAME_LABEL = "* Name:";
    public static final String PARENT_ID_LABEL = "* Parent:";
    public static final String DESCRIPTION_LABEL = "Description:";
    public static final String PATH_LABEL = "* Path:";
    public static final String CATEGORY_ID_LABEL = "* Category:";
    public static final String DOCUMENTS_LABEL = "Documents:";
    public static final String DOCUMENTS_IDS_LABEL = "Linked Documents:";
    public static final String INCOME_SOURCE_ID_LABEL = "* Income Source";
    public static final String SOURCE_TYPE_LABEL = "* Source Type";
    public static final String CURRENCY_TYPE_LABEL = "* Currency Type";
    public static final String BALANCE_LABEL = "* Balance";
    public static final String DAY_LABEL = "* Day (yyyy-MM-dd):";
    public static final String SOURCE_LABEL = "* Source:";
    public static final String CATEGORY_LABEL = "* Category:";
    public static final String AMOUNT_LABEL = "* Amount:";
    public static final String CODE_LABEL = "* Code:";
    public static final String BALANCE_TYPE_LABEL = "* Balance Type";
}
