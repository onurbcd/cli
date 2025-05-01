package com.onurbcd.eruservice.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Error {

    INTERNAL_SERVER_ERROR("Internal Server Error"),
    CRYPTO("Crypto"),
    CONSTRAINT_VIOLATION("Constraint Violation"),
    PSQL("PSQL Exception"),
    ENTITY_DOES_NOT_EXIST("Entity with id '%s' does not exist in the database"),
    SIZE_NOT_BETWEEN("%s: size must be between %d and %d"),
    SEQUENCE_CHANGED("the sequence cannot be changed by a PUT request; use PATCH instead; current value: '%d'; new value: '%d'"),
    REFERENCE_CHANGED("reference year and month cannot be changed"),
    WRONG_DIRETION_UP("The sequence cannot get any lower"),
    WRONG_DIRETION_DOWN("The sequence cannot get any higher"),
    BUDGET_REF_YEAR_IS_NULL("Reference year is mandatory"),
    BUDGET_REF_MONTH_IS_NULL("Reference month is mandatory"),
    COPY_BUDGET_FROM_IS_NULL("Copy budget from is mandatory"),
    COPY_BUDGET_TO_IS_NULL("Copy budget to is mandatory"),
    COPY_BUDGET_FROM_YEAR_IS_NULL("Copy budget From Year is mandatory"),
    COPY_BUDGET_FROM_MONTH_IS_NULL("Copy budget From Month is mandatory"),
    COPY_BUDGET_TO_YEAR_IS_NULL("Copy budget To Year is mandatory"),
    COPY_BUDGET_TO_MONTH_IS_NULL("Copy budget To Month is mandatory"),
    COPY_BUDGET_FROM_IS_EMPTY("There is no budget for the source month %02d/%d"),
    COPY_BUDGET_TO_ALREADY_EXISTS("There is already a budget for the target month %02d/%d"),
    COPY_BUDGET_EQUAL_MONTH("Origin and destination months cannot be the same"),
    SWAP_SAME_POSITION("The current and target positions are the same"),
    NO_ROWS_DELETED("No rows were deleted"),
    CATEGORY_PARENT_IS_NULL("Category parent is mandatory"),
    CATEGORY_LEVEL_ONE_IS_UNCHANGEABLE("Level 1 category cannot be changed"),
    CATEGORY_DELETE_NON_LAST_BRANCH("Only the last branch can be deleted"),
    CATEGORY_CANNOT_DELETE_LEVEL_ONE("Level 1 category cannot be deleted"),
    CATEGORY_PARENT_IS_UNCHANGEABLE("Category parent cannot be changed"),
    MONTH_ALREADY_EXISTS("Month %02d/%d already exists"),
    BAD_REQUEST("Invalid Arguments"),
    DOCUMENT_GENERATE_HASH("No Such Algorithm Exception"),
    HTTP_MEDIA_TYPE_NOT_SUPPORTED("Http Media Type Not Supported"),
    MISSING_SERVLET_REQUEST_PART("MissingServletRequestPart"),
    DOCUMENT_IS_EMPTY("Document is empty"),
    DOCUMENT_NAME_IS_BLANK("Document name is mandatory"),
    DOCUMENT_MIME_TYPE_IS_BLANK("Document mime type is mandatory"),
    DOCUMENT_SIZE_IS_ZERO("Document size is zero (empty file)"),
    FILE_ALREADY_EXISTS("File %s already exists in storage"),
    STORAGE_FILE_SAVE("Storage File Save"),
    STORAGE_FILE_DELETE("Storage File Delete"),
    DAY_CHANGED("Balance day cannot be changed"),
    DAY_SOURCE_CHANGED("Balance source cannot be changed"),
    DAY_BALANCE_TYPE_CHANGED("Balance type cannot be changed"),
    BALANCE_OPERATION_MISSING("There is no balance operation with operation %s and balance type %s"),
    DAY_IN_FUTURE("Balance day cannot be greater than today"),
    MAX_UPLOAD_SIZE_EXCEEDED("MaxUploadSizeExceeded"),
    FILE_DOES_NOT_EXIST("File %s does not exist in storage"),
    STORAGE_FILE_GET("Storage File Get"),
    BILL_ALREADY_CLOSED("Bill is already closed"),
    BILL_ALREADY_PAID("Bill is already paid"),
    BILL_ALREADY_OPENED("Bill is already opened"),
    FOLDER_IS_EMPTY_OR_DOES_NOT_EXIST("Folder %s is empty or does not exist"),
    PARSE_LOCAL_DATE("Invalid date format: %s"),
    CONVERTING_TO_MULTIPART_FILE("Error converting to MultipartFile: %s");

    private final String message;

    public String format(Object... args) {
        return String.format(message, args);
    }
}
