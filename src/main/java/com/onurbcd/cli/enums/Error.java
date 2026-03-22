package com.onurbcd.cli.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Error {

    BALANCE_OPERATION_MISSING("There is no balance operation with operation %s and balance type %s"),
    BILL_ALREADY_CLOSED("Bill is already closed"),
    BILL_ALREADY_OPENED("Bill is already opened"),
    BILL_ALREADY_PAID("Bill is already paid"),
    BILL_TYPE_REQUIRED("Create a bill type before proceeding"),
    BUDGET_REF_MONTH_IS_NULL("Reference month is mandatory"),
    BUDGET_REF_YEAR_IS_NULL("Reference year is mandatory"),
    BUDGET_REQUIRED("Create a budget for %02d/%d before proceeding"),
    CATEGORY_CANNOT_DELETE_LEVEL_ONE("Level 1 category cannot be deleted"),
    CATEGORY_DELETE_NON_LAST_BRANCH("Only the last branch can be deleted"),
    CATEGORY_LEVEL_ONE_IS_UNCHANGEABLE("Level 1 category cannot be changed"),
    CATEGORY_PARENT_IS_NULL("Category parent is mandatory"),
    CATEGORY_PARENT_IS_UNCHANGEABLE("Category parent cannot be changed"),
    CATEGORY_REQUIRED("Create a category before proceeding"),
    CONVERTING_TO_MULTIPART_FILE("Error converting to MultipartFile: %s"),
    COPY_BUDGET_EQUAL_MONTH("Origin and destination months cannot be the same"),
    COPY_BUDGET_FROM_IS_EMPTY("There is no budget for the source month %02d/%d"),
    COPY_BUDGET_FROM_IS_NULL("Copy budget from is mandatory"),
    COPY_BUDGET_FROM_MONTH_IS_NULL("Copy budget From Month is mandatory"),
    COPY_BUDGET_FROM_YEAR_IS_NULL("Copy budget From Year is mandatory"),
    COPY_BUDGET_TO_ALREADY_EXISTS("There is already a budget for the target month %02d/%d"),
    COPY_BUDGET_TO_IS_NULL("Copy budget to is mandatory"),
    COPY_BUDGET_TO_MONTH_IS_NULL("Copy budget To Month is mandatory"),
    COPY_BUDGET_TO_YEAR_IS_NULL("Copy budget To Year is mandatory"),
    CRYPTO("Crypto error: %s"),
    DAY_BALANCE_TYPE_CHANGED("Balance type cannot be changed"),
    DAY_CHANGED("Balance day cannot be changed"),
    DAY_IN_FUTURE("Balance day cannot be greater than today"),
    DAY_SOURCE_CHANGED("Balance source cannot be changed"),
    DOCUMENT_GENERATE_HASH("No Such Algorithm Exception: %s"),
    DOCUMENT_IS_EMPTY("Document is empty"),
    DOCUMENT_MIME_TYPE_IS_BLANK("Document mime type is mandatory"),
    DOCUMENT_NAME_IS_BLANK("Document name is mandatory"),
    DOCUMENT_SIZE_IS_ZERO("Document size is zero (empty file)"),
    ENTITY_DOES_NOT_EXIST("Entity with id '%s' does not exist in the database"),
    FILE_ALREADY_EXISTS("File %s already exists in storage"),
    FOLDER_DOES_NOT_EXIST("Folder %s does not exist"),
    INCOME_SOURCE_REQUIRED("Create an income source before proceeding"),
    INTERNAL_SERVER_ERROR("Internal server error: %s"),
    INVALID_ENUM_VALUE("Invalid enum value: %s"),
    MONTH_ALREADY_EXISTS("Month %02d/%d already exists"),
    NO_ROWS_DELETED("No rows were deleted"),
    PARSE_LOCAL_DATE("Invalid date format: %s"),
    REFERENCE_CHANGED("reference year and month cannot be changed"),
    SEQUENCE_CHANGED("The sequence cannot be changed. Current value: '%d'; new value: '%d'"),
    SOURCE_REQUIRED("Create a source before proceeding"),
    STORAGE_FILE_DELETE("Storage file delete error: %s"),
    STORAGE_FILE_SAVE("Storage file save error: %s"),
    SWAP_SAME_POSITION("The current and target positions are the same"),
    WRONG_DIRETION_DOWN("The sequence cannot get any higher"),
    WRONG_DIRETION_UP("The sequence cannot get any lower");

    private final String message;

    public String format(Object... args) {
        return String.format(message, args);
    }
}
