package com.onurbcd.cli.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class ErrorCodeTest {

    @Test
    @DisplayName("declares exactly these constants, in this order, each with its formatted message")
    void givenAllConstants_whenFormatted_shouldReturnTheirMessages() {
        var messages = new LinkedHashMap<String, String>();
        Arrays.stream(ErrorCode.values()).forEach(code -> messages.put(code.name(), code.format(sampleArgs(code))));

        assertThat(messages).containsExactly(
                entry("BALANCE_OPERATION_MISSING", "There is no balance operation with operation A and balance type B"),
                entry("BILL_ALREADY_CLOSED", "Bill is already closed"),
                entry("BILL_ALREADY_OPENED", "Bill is already opened"),
                entry("BILL_ALREADY_PAID", "Bill is already paid"),
                entry("BILL_TYPE_REQUIRED", "Create a bill type before proceeding"),
                entry("BUDGET_REF_MONTH_IS_NULL", "Reference month is mandatory"),
                entry("BUDGET_REF_YEAR_IS_NULL", "Reference year is mandatory"),
                entry("BUDGET_REQUIRED", "Create a budget for 03/2026 before proceeding"),
                entry("CATEGORY_CANNOT_DELETE_LEVEL_ONE", "Level 1 category cannot be deleted"),
                entry("CATEGORY_DELETE_NON_LAST_BRANCH", "Only the last branch can be deleted"),
                entry("CATEGORY_LEVEL_ONE_IS_UNCHANGEABLE", "Level 1 category cannot be changed"),
                entry("CATEGORY_PARENT_IS_NULL", "Category parent is mandatory"),
                entry("CATEGORY_PARENT_IS_UNCHANGEABLE", "Category parent cannot be changed"),
                entry("CATEGORY_REQUIRED", "Create a category before proceeding"),
                entry("CONVERTING_TO_MULTIPART_FILE", "Error converting to MultipartFile: x"),
                entry("COPY_BUDGET_EQUAL_MONTH", "Origin and destination months cannot be the same"),
                entry("COPY_BUDGET_FROM_IS_EMPTY", "There is no budget for the source month 03/2026"),
                entry("COPY_BUDGET_FROM_IS_NULL", "Copy budget from is mandatory"),
                entry("COPY_BUDGET_FROM_MONTH_IS_NULL", "Copy budget From Month is mandatory"),
                entry("COPY_BUDGET_FROM_YEAR_IS_NULL", "Copy budget From Year is mandatory"),
                entry("COPY_BUDGET_TO_ALREADY_EXISTS", "There is already a budget for the target month 03/2026"),
                entry("COPY_BUDGET_TO_IS_NULL", "Copy budget to is mandatory"),
                entry("COPY_BUDGET_TO_MONTH_IS_NULL", "Copy budget To Month is mandatory"),
                entry("COPY_BUDGET_TO_YEAR_IS_NULL", "Copy budget To Year is mandatory"),
                entry("CRYPTO", "Crypto error: x"),
                entry("DAY_BALANCE_TYPE_CHANGED", "Balance type cannot be changed"),
                entry("DAY_CHANGED", "Balance day cannot be changed"),
                entry("DAY_IN_FUTURE", "Balance day cannot be greater than today"),
                entry("DAY_SOURCE_CHANGED", "Balance source cannot be changed"),
                entry("DOCUMENT_GENERATE_HASH", "No Such Algorithm Exception: x"),
                entry("DOCUMENT_IS_EMPTY", "Document is empty"),
                entry("DOCUMENT_MIME_TYPE_IS_BLANK", "Document mime type is mandatory"),
                entry("DOCUMENT_NAME_IS_BLANK", "Document name is mandatory"),
                entry("DOCUMENT_SIZE_IS_ZERO", "Document size is zero (empty file)"),
                entry("ENTITY_DOES_NOT_EXIST", "Entity with id 'x' does not exist in the database"),
                entry("FILE_ALREADY_EXISTS", "File x already exists in storage"),
                entry("FOLDER_DOES_NOT_EXIST", "Folder x does not exist"),
                entry("INCOME_SOURCE_REQUIRED", "Create an income source before proceeding"),
                entry("INTERNAL_SERVER_ERROR", "Internal server error: x"),
                entry("INVALID_ENUM_VALUE", "Invalid enum value: x"),
                entry("MONTH_ALREADY_EXISTS", "Month 03/2026 already exists"),
                entry("NO_ROWS_DELETED", "No rows were deleted"),
                entry("PARSE_LOCAL_DATE", "Invalid date format: x"),
                entry("REFERENCE_CHANGED", "reference year and month cannot be changed"),
                entry("SEQUENCE_CHANGED", "The sequence cannot be changed. Current value: '1'; new value: '2'"),
                entry("SOURCE_REQUIRED", "Create a source before proceeding"),
                entry("STORAGE_FILE_DELETE", "Storage file delete error: x"),
                entry("STORAGE_FILE_SAVE", "Storage file save error: x"),
                entry("SWAP_SAME_POSITION", "The current and target positions are the same"),
                entry("WRONG_DIRETION_DOWN", "The sequence cannot get any higher"),
                entry("WRONG_DIRETION_UP", "The sequence cannot get any lower"),
                entry("OPEN_BILLS_REQUIRED", "Create an open bill for 03/2026 before proceeding"));
    }

    /** Sample arguments matching each message's placeholders (month 3, year 2026; "A"/"B"; "x"; 1/2). */
    private static Object[] sampleArgs(ErrorCode code) {
        return switch (code) {
            case BALANCE_OPERATION_MISSING ->
                    new Object[]{"A", "B"};
            case BUDGET_REQUIRED, COPY_BUDGET_FROM_IS_EMPTY, COPY_BUDGET_TO_ALREADY_EXISTS, MONTH_ALREADY_EXISTS, OPEN_BILLS_REQUIRED ->
                    new Object[]{3, 2026};
            case CONVERTING_TO_MULTIPART_FILE, CRYPTO, DOCUMENT_GENERATE_HASH, ENTITY_DOES_NOT_EXIST, FILE_ALREADY_EXISTS, FOLDER_DOES_NOT_EXIST, INTERNAL_SERVER_ERROR, INVALID_ENUM_VALUE, PARSE_LOCAL_DATE, STORAGE_FILE_DELETE, STORAGE_FILE_SAVE ->
                    new Object[]{"x"};
            case SEQUENCE_CHANGED ->
                    new Object[]{1, 2};
            default -> new Object[0];
        };
    }

    @Nested
    @DisplayName("format")
    class Format {

        @Test
        @DisplayName("without placeholders, returns the message untouched")
        void givenMessageWithoutPlaceholders_whenFormat_shouldReturnMessage() {
            var result = ErrorCode.BILL_ALREADY_PAID.format();

            assertThat(result).isEqualTo("Bill is already paid");
        }

        @Test
        @DisplayName("fills zero-padded month and year placeholders")
        void givenMonthAndYear_whenFormat_shouldPadMonth() {
            var result = ErrorCode.BUDGET_REQUIRED.format(3, 2026);

            assertThat(result).isEqualTo("Create a budget for 03/2026 before proceeding");
        }

        @Test
        @DisplayName("fills several placeholders in order")
        void givenTwoArgs_whenFormat_shouldSubstituteInOrder() {
            var result = ErrorCode.BALANCE_OPERATION_MISSING.format("INSERT", "INCOME");

            assertThat(result)
                    .isEqualTo("There is no balance operation with operation INSERT and balance type INCOME");
        }
    }
}
