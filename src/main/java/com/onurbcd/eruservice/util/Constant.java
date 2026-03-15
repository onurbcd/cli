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
     * COMMANDS
     */
    public static final String BALANCE = "Balance";
    public static final String BILL_TYPE = "Bill Type";

    /*
     * HEADERS KEYS
     */
    public static final String ID_KEY = "id";
    public static final String NAME_KEY = "name";
    public static final String ACTIVE_KEY = "active";
    public static final String DESCRIPTION_KEY = "description";
    public static final String LINK_KEY = "link";
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String CALENDAR_YEAR_KEY = "calendarYear";
    public static final String CALENDAR_MONTH_KEY = "calendarMonth";
    public static final String PARENT_ID_KEY = "parentId";
    public static final String PARENT_NAME_KEY = "parentName";
    public static final String LEVEL_KEY = "level";
    public static final String LAST_BRANCH_KEY = "lastBranch";
    public static final String PATH_KEY = "path";
    public static final String CATEGORY_ID_KEY = "categoryId";
    public static final String CATEGORY_NAME_KEY = "categoryName";
    public static final String INCOME_SOURCE_ID_KEY = "incomeSourceId";
    public static final String INCOME_SOURCE_NAME_KEY = "incomeSourceName";
    public static final String SOURCE_TYPE_KEY = "sourceType";
    public static final String CURRENCY_TYPE_KEY = "currencyType";
    public static final String BALANCE_KEY = "balance";
    public static final String PARTIAL_KEY = "partial";
    public static final String TOTAL_KEY = "total";
    public static final String SEQUENCE_KEY = "sequence";
    public static final String REF_YEAR_KEY = "refYear";
    public static final String REF_MONTH_KEY = "refMonth";
    public static final String BILL_TYPE_ID_KEY = "billTypeId";
    public static final String BILL_TYPE_NAME_KEY = "billTypeName";
    public static final String AMOUNT_KEY = "amount";
    public static final String PAID_KEY = "paid";
    public static final String TYPE_KEY = "type";
    public static final String VALUE_KEY = "value";
    public static final String DAY_CALENDAR_DATE_KEY = "dayCalendarDate";
    public static final String SOURCE_NAME_KEY = "sourceName";
    public static final String CODE_KEY = "code";
    public static final String BALANCE_TYPE_KEY = "balanceType";

    /*
     * HEADERS VALUES
     */
    public static final String ID_VALUE = "Id";
    public static final String NAME_VALUE = "Name";
    public static final String ACTIVE_VALUE = "Active";
    public static final String DESCRIPTION_VALUE = "Description";
    public static final String LINK_VALUE = "Link";
    public static final String USERNAME_VALUE = "Username";
    public static final String PASSWORD_VALUE = "Password";
    public static final String CALENDAR_YEAR_VALUE = "Calendar Year";
    public static final String CALENDAR_MONTH_VALUE = "Calendar Month";
    public static final String PARENT_ID_VALUE = "Parent Id";
    public static final String PARENT_NAME_VALUE = "Parent Name";
    public static final String LEVEL_VALUE = "Level";
    public static final String LAST_BRANCH_VALUE = "Last Branch";
    public static final String PATH_VALUE = "Path";
    public static final String CATEGORY_ID_VALUE = "Category Id";
    public static final String CATEGORY_NAME_VALUE = "Category Name";
    public static final String INCOME_SOURCE_ID_VALUE = "Income Source Id";
    public static final String INCOME_SOURCE_NAME_VALUE = "Income Source Name";
    public static final String SOURCE_TYPE_VALUE = "Source Type";
    public static final String CURRENCY_TYPE_VALUE = "Currency Type";
    public static final String BALANCE_VALUE = "Balance";
    public static final String PARTIAL_VALUE = "Partial";
    public static final String TOTAL_VALUE = "Total";
    public static final String SEQUENCE_VALUE = "Sequence";
    public static final String REF_YEAR_VALUE = "Ref Year";
    public static final String REF_MONTH_VALUE = "Ref Month";
    public static final String BILL_TYPE_ID_VALUE = "Bill Type Id";
    public static final String BILL_TYPE_NAME_VALUE = "Bill Type Name";
    public static final String AMOUNT_VALUE = "Amount";
    public static final String PAID_VALUE = "Paid";
    public static final String TYPE_VALUE = "Type";
    public static final String VALUE_VALUE = "Value";
    public static final String DATE_VALUE = "Date";
    public static final String SOURCE_VALUE = "Source";
    public static final String CATEGORY_VALUE = "Category";
    public static final String CODE_VALUE = "Code";

    /*
     * MESSAGES
     */
    public static final String BILL_CLOSE_CODE = "created by bill close";
    public static final String OPERATION_CANCELLED = "Operation cancelled";
    public static final String SAVE_SUCCESS = "%s with id %s saved with success.";
    public static final String DELETE_SUCCESS = "%s with id %s deleted with success.";
    public static final String UPDATE_SUCCESS = "%s with id %s updated with success.";

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
    public static final String LINK_ID = "link";
    public static final String USERNAME_ID = "username";
    public static final String PASSWORD_ID = "password";
    public static final String REF_YEAR_ID = "refYear";
    public static final String REF_MONTH_ID = "refMonth";
    public static final String BILL_TYPE_ID_ID = "billTypeId";
    public static final String PAID_ID = "paid";

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
    public static final String LINK_LABEL = "Link:";
    public static final String USERNAME_LABEL = "* Username:";
    public static final String PASSWORD_LABEL = "* Password:";
    public static final String REF_YEAR_LABEL = "* Reference Year:";
    public static final String REF_MONTH_LABEL = "* Reference Month:";
    public static final String BILL_TYPE_ID_LABEL = "* Bill Type:";
    public static final String PAID_LABEL = "* Paid:";
}
