package com.onurbcd.cli.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EruTableTest {

    private static Map<String, Object> headers(String... keysAndValues) {
        var map = new LinkedHashMap<String, Object>();

        for (var i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }

        return map;
    }

    static Stream<Arguments> tables() {
        return Stream.of(
                Arguments.of(EruTable.SECRET, headers("id", "Id", "name", "Name", "active", "Active",
                        "description", "Description", "link", "Link", "username", "Username",
                        "password", "Password")),
                Arguments.of(EruTable.DAY, headers("calendarYear", "Calendar Year", "calendarMonth", "Calendar Month")),
                Arguments.of(EruTable.INCOME_SOURCE, headers("id", "Id", "name", "Name", "active", "Active")),
                Arguments.of(EruTable.CATEGORY, headers("id", "Id", "name", "Name", "active", "Active",
                        "parentId", "Parent Id", "parentName", "Parent Name", "level", "Level",
                        "lastBranch", "Last Branch", "description", "Description")),
                Arguments.of(EruTable.BILL_TYPE, headers("id", "Id", "name", "Name", "active", "Active",
                        "path", "Path", "categoryId", "Category Id", "categoryName", "Category Name")),
                Arguments.of(EruTable.SOURCE, headers("id", "Id", "name", "Name", "active", "Active",
                        "incomeSourceId", "Income Source Id", "incomeSourceName", "Income Source Name",
                        "sourceType", "Source Type", "currencyType", "Currency Type", "balance", "Balance")),
                Arguments.of(EruTable.SOURCE_BALANCE_SUM, headers("partial", "Partial", "total", "Total")),
                Arguments.of(EruTable.BUDGET, headers("id", "Id", "name", "Name", "active", "Active",
                        "sequence", "Sequence", "refYear", "Ref Year", "refMonth", "Ref Month",
                        "billTypeId", "Bill Type Id", "billTypeName", "Bill Type Name",
                        "amount", "Amount", "paid", "Paid")),
                Arguments.of(EruTable.BUDGET_SUM_MONTH, headers("type", "Type", "value", "Value")),
                Arguments.of(EruTable.BALANCE, headers("id", "Id", "sequence", "Sequence",
                        "dayCalendarDate", "Date", "sourceName", "Source", "categoryName", "Category",
                        "amount", "Amount", "code", "Code", "description", "Description",
                        "balanceType", "Type", "paymentType", "Payment Type")),
                Arguments.of(EruTable.BALANCE_SUM, headers("type", "Type", "value", "Value")),
                Arguments.of(EruTable.BILL, headers()));
    }

    @Test
    @DisplayName("declares exactly the twelve tables, in order")
    void givenValues_whenListed_shouldContainTheTwelveTables() {
        assertThat(EruTable.values()).containsExactly(
                EruTable.SECRET, EruTable.DAY, EruTable.INCOME_SOURCE, EruTable.CATEGORY, EruTable.BILL_TYPE,
                EruTable.SOURCE, EruTable.SOURCE_BALANCE_SUM, EruTable.BUDGET, EruTable.BUDGET_SUM_MONTH,
                EruTable.BALANCE, EruTable.BALANCE_SUM, EruTable.BILL);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("tables")
    @DisplayName("exposes the headers in the table's column order")
    void givenTable_whenGetHeaders_shouldReturnColumnsInOrder(EruTable table, Map<String, Object> expected) {
        var result = table.getHeaders();

        assertThat(result).containsExactlyEntriesOf(expected);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("tables")
    @DisplayName("hands out a read-only view")
    void givenTable_whenModifyingHeaders_shouldThrow(EruTable table, Map<String, Object> expected) {
        var headers = table.getHeaders();

        assertThatThrownBy(() -> headers.put("extra", "Extra")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(headers::clear).isInstanceOf(UnsupportedOperationException.class);
        assertThat(table.getHeaders()).containsExactlyEntriesOf(expected);
    }
}
