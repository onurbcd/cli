package com.onurbcd.eruservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;

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
    BUDGET_SUM_MONTH(getBudgetSumMonthHeaders());

    private final LinkedHashMap<String, Object> headers;

    private static LinkedHashMap<String, Object> getSecretHeaders() {
        var secretHeaders = getDefaultHeaders(9);
        secretHeaders.put("description", "Description");
        secretHeaders.put("link", "Link");
        secretHeaders.put("username", "Username");
        secretHeaders.put("password", "Password");
        return secretHeaders;
    }

    private static LinkedHashMap<String, Object> getDayHeaders() {
        var dayHeaders = LinkedHashMap.<String, Object>newLinkedHashMap(2);
        dayHeaders.put("calendarYear", "Calendar Year");
        dayHeaders.put("calendarMonth", "Calendar Month");
        return dayHeaders;
    }

    private static LinkedHashMap<String, Object> getIncomeSourceHeaders() {
        return getDefaultHeaders(5);
    }

    private static LinkedHashMap<String, Object> getCategoryHeaders() {
        var categoryHeaders = getDefaultHeaders(10);
        categoryHeaders.put("parentId", "Parent Id");
        categoryHeaders.put("parentName", "Parent Name");
        categoryHeaders.put("level", "Level");
        categoryHeaders.put("lastBranch", "Last Branch");
        categoryHeaders.put("description", "Description");
        return categoryHeaders;
    }

    private static LinkedHashMap<String, Object> getBillTypeHeaders() {
        var categoryHeaders = getDefaultHeaders(8);
        categoryHeaders.put("path", "Path");
        categoryHeaders.put("categoryId", "Category Id");
        categoryHeaders.put("categoryName", "Category Name");
        return categoryHeaders;
    }

    private static LinkedHashMap<String, Object> getSourceHeaders() {
        var sourceHeaders = getDefaultHeaders(10);
        sourceHeaders.put("incomeSourceId", "Income Source Id");
        sourceHeaders.put("incomeSourceName", "Income Source Name");
        sourceHeaders.put("sourceType", "Source Type");
        sourceHeaders.put("currencyType", "Currency Type");
        sourceHeaders.put("balance", "Balance");
        return sourceHeaders;
    }

    private static LinkedHashMap<String, Object> getSourceBalanceSumHeaders() {
        var sourceBalanceSumHeaders = LinkedHashMap.<String, Object>newLinkedHashMap(2);
        sourceBalanceSumHeaders.put("partial", "Partial");
        sourceBalanceSumHeaders.put("total", "Total");
        return sourceBalanceSumHeaders;
    }

    private static LinkedHashMap<String, Object> getBudgetHeaders() {
        var budgetHeaders = getDefaultHeaders(12);
        budgetHeaders.put("sequence", "Sequence");
        budgetHeaders.put("refYear", "Ref Year");
        budgetHeaders.put("refMonth", "Ref Month");
        budgetHeaders.put("billTypeId", "Bill Type Id");
        budgetHeaders.put("billTypeName", "Bill Type Name");
        budgetHeaders.put("amount", "Amount");
        budgetHeaders.put("paid", "Paid");
        return budgetHeaders;
    }

    private static LinkedHashMap<String, Object> getBudgetSumMonthHeaders() {
        var budgetSumMonthHeaders = LinkedHashMap.<String, Object>newLinkedHashMap(2);
        budgetSumMonthHeaders.put("type", "Type");
        budgetSumMonthHeaders.put("value", "Value");
        return budgetSumMonthHeaders;
    }

    private static LinkedHashMap<String, Object> getDefaultHeaders(int numMappings) {
        var headers = LinkedHashMap.<String, Object>newLinkedHashMap(numMappings);
        headers.put("id", "Id");
        headers.put("name", "Name");
        headers.put("active", "Active");
        headers.put("createdDate", "Created Date");
        headers.put("lastModifiedDate", "Last Modified Date");
        return headers;
    }
}
