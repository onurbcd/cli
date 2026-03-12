package com.onurbcd.eruservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;

import static com.onurbcd.eruservice.util.Constant.*;

@RequiredArgsConstructor
@Getter
public enum EruTable {

    SECRET(getSecretHeaders()),
    DAY(getDayHeaders()),
    INCOME_SOURCE(getIncomeSourceHeaders()),
    CATEGORY(getCategoryHeaders()),
    BILL_TYPE(getBillTypeHeaders()),
    SOURCE(getSourceHeaders()),
    SOURCE_BALANCE_SUM(getSourceBalanceSumHeaders()),
    BUDGET(getBudgetHeaders()),
    BUDGET_SUM_MONTH(getBudgetSumMonthHeaders()),
    BALANCE(getBalanceHeaders()),
    BALANCE_SUM(getBalanceSumHeaders());

    private final LinkedHashMap<String, Object> headers;

    private static LinkedHashMap<String, Object> getSecretHeaders() {
        var secretHeaders = getDefaultHeaders(7);
        secretHeaders.put(DESCRIPTION_KEY, DESCRIPTION_VALUE);
        secretHeaders.put(LINK_KEY, LINK_VALUE);
        secretHeaders.put(USERNAME_KEY, USERNAME_VALUE);
        secretHeaders.put(PASSWORD_KEY, PASSWORD_VALUE);
        return secretHeaders;
    }

    private static LinkedHashMap<String, Object> getDayHeaders() {
        var dayHeaders = LinkedHashMap.<String, Object>newLinkedHashMap(2);
        dayHeaders.put(CALENDAR_YEAR_KEY, CALENDAR_YEAR_VALUE);
        dayHeaders.put(CALENDAR_MONTH_KEY, CALENDAR_MONTH_VALUE);
        return dayHeaders;
    }

    private static LinkedHashMap<String, Object> getIncomeSourceHeaders() {
        return getDefaultHeaders(3);
    }

    private static LinkedHashMap<String, Object> getCategoryHeaders() {
        var categoryHeaders = getDefaultHeaders(8);
        categoryHeaders.put(PARENT_ID_KEY, PARENT_ID_VALUE);
        categoryHeaders.put(PARENT_NAME_KEY, PARENT_NAME_VALUE);
        categoryHeaders.put(LEVEL_KEY, LEVEL_VALUE);
        categoryHeaders.put(LAST_BRANCH_KEY, LAST_BRANCH_VALUE);
        categoryHeaders.put(DESCRIPTION_KEY, DESCRIPTION_VALUE);
        return categoryHeaders;
    }

    private static LinkedHashMap<String, Object> getBillTypeHeaders() {
        var categoryHeaders = getDefaultHeaders(6);
        categoryHeaders.put(PATH_KEY, PATH_VALUE);
        categoryHeaders.put(CATEGORY_ID_KEY, CATEGORY_ID_VALUE);
        categoryHeaders.put(CATEGORY_NAME_KEY, CATEGORY_NAME_VALUE);
        return categoryHeaders;
    }

    private static LinkedHashMap<String, Object> getSourceHeaders() {
        var sourceHeaders = getDefaultHeaders(8);
        sourceHeaders.put(INCOME_SOURCE_ID_KEY, INCOME_SOURCE_ID_VALUE);
        sourceHeaders.put(INCOME_SOURCE_NAME_KEY, INCOME_SOURCE_NAME_VALUE);
        sourceHeaders.put(SOURCE_TYPE_KEY, SOURCE_TYPE_VALUE);
        sourceHeaders.put(CURRENCY_TYPE_KEY, CURRENCY_TYPE_VALUE);
        sourceHeaders.put(BALANCE_KEY, BALANCE_VALUE);
        return sourceHeaders;
    }

    private static LinkedHashMap<String, Object> getSourceBalanceSumHeaders() {
        var sourceBalanceSumHeaders = LinkedHashMap.<String, Object>newLinkedHashMap(2);
        sourceBalanceSumHeaders.put(PARTIAL_KEY, PARTIAL_VALUE);
        sourceBalanceSumHeaders.put(TOTAL_KEY, TOTAL_VALUE);
        return sourceBalanceSumHeaders;
    }

    private static LinkedHashMap<String, Object> getBudgetHeaders() {
        var budgetHeaders = getDefaultHeaders(10);
        budgetHeaders.put(SEQUENCE_KEY, SEQUENCE_VALUE);
        budgetHeaders.put(REF_YEAR_KEY, REF_YEAR_VALUE);
        budgetHeaders.put(REF_MONTH_KEY, REF_MONTH_VALUE);
        budgetHeaders.put(BILL_TYPE_ID_KEY, BILL_TYPE_ID_VALUE);
        budgetHeaders.put(BILL_TYPE_NAME_KEY, BILL_TYPE_NAME_VALUE);
        budgetHeaders.put(AMOUNT_KEY, AMOUNT_VALUE);
        budgetHeaders.put(PAID_KEY, PAID_VALUE);
        return budgetHeaders;
    }

    private static LinkedHashMap<String, Object> getBudgetSumMonthHeaders() {
        var budgetSumMonthHeaders = LinkedHashMap.<String, Object>newLinkedHashMap(2);
        budgetSumMonthHeaders.put(TYPE_KEY, TYPE_VALUE);
        budgetSumMonthHeaders.put(VALUE_KEY, VALUE_VALUE);
        return budgetSumMonthHeaders;
    }

    private static LinkedHashMap<String, Object> getBalanceHeaders() {
        var balanceHeaders = LinkedHashMap.<String, Object>newLinkedHashMap(9);
        balanceHeaders.put(ID_KEY, ID_VALUE);
        balanceHeaders.put(SEQUENCE_KEY, SEQUENCE_VALUE);
        balanceHeaders.put(DAY_CALENDAR_DATE_KEY, DATE_VALUE);
        balanceHeaders.put(SOURCE_NAME_KEY, SOURCE_VALUE);
        balanceHeaders.put(CATEGORY_NAME_KEY, CATEGORY_VALUE);
        balanceHeaders.put(AMOUNT_KEY, AMOUNT_VALUE);
        balanceHeaders.put(CODE_KEY, CODE_VALUE);
        balanceHeaders.put(DESCRIPTION_KEY, DESCRIPTION_VALUE);
        balanceHeaders.put(BALANCE_TYPE_KEY, TYPE_VALUE);
        return balanceHeaders;
    }

    private static LinkedHashMap<String, Object> getBalanceSumHeaders() {
        var balanceSumHeaders = LinkedHashMap.<String, Object>newLinkedHashMap(2);
        balanceSumHeaders.put(TYPE_KEY, TYPE_VALUE);
        balanceSumHeaders.put(VALUE_KEY, VALUE_VALUE);
        return balanceSumHeaders;
    }

    private static LinkedHashMap<String, Object> getDefaultHeaders(int numMappings) {
        var headers = LinkedHashMap.<String, Object>newLinkedHashMap(numMappings);
        headers.put(ID_KEY, ID_VALUE);
        headers.put(NAME_KEY, NAME_VALUE);
        headers.put(ACTIVE_KEY, ACTIVE_VALUE);
        return headers;
    }
}
